package com.person.ally.ai.provider

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiProvider
import com.person.ally.ai.model.ApiError
import com.person.ally.ai.model.ChatMessage
import com.person.ally.ai.model.Choice
import com.person.ally.ai.model.CompletionRequest
import com.person.ally.ai.model.CompletionResponse
import com.person.ally.ai.model.FunctionCall
import com.person.ally.ai.model.MessageRole
import com.person.ally.ai.model.ModelCapabilities
import com.person.ally.ai.model.ModelCategory
import com.person.ally.ai.model.StreamChunk
import com.person.ally.ai.model.ToolCall
import com.person.ally.ai.model.UsageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Base implementation for OpenAI-compatible API providers.
 * Handles common functionality like request building, streaming, and error handling.
 */
abstract class BaseOpenAiProvider(
    override val provider: AiProvider
) : AiProviderClient {

    protected val gson = Gson()
    protected val rateLimitStatus = AtomicReference(RateLimitStatus())
    protected val currentEventSource = AtomicReference<EventSource?>(null)
    protected val currentCall = AtomicReference<Call?>(null)

    protected open val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    protected open val streamingClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // No timeout for streaming
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(provider.baseUrl)
                .headers(buildHeaders(false))
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 404 // 404 just means endpoint not found, provider is up
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun fetchModels(): AiResult<List<AiModel>> = withContext(Dispatchers.IO) {
        if (provider.modelsEndpoint == null) {
            return@withContext AiResult.Success(getDefaultModels())
        }

        try {
            val url = "${provider.baseUrl.trimEnd('/')}${provider.modelsEndpoint}"
            val request = Request.Builder()
                .url(url)
                .headers(buildHeaders(false))
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext handleErrorResponse(response)
                }

                val body = response.body?.string() ?: return@withContext AiResult.Error(
                    ApiError("Empty response body", statusCode = response.code)
                )

                val models = parseModelsResponse(body)
                AiResult.Success(models)
            }
        } catch (e: Exception) {
            AiResult.Error(ApiError(e.message ?: "Unknown error", statusCode = 500))
        }
    }

    override suspend fun complete(request: CompletionRequest): AiResult<CompletionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val rateLimitCheck: AiResult<CompletionResponse>? = checkRateLimitBeforeRequest()
                if (rateLimitCheck != null) return@withContext rateLimitCheck

                val jsonBody = buildRequestBody(request.copy(stream = false))
                val httpRequest = buildHttpRequest(jsonBody, false)

                val call = client.newCall(httpRequest)
                currentCall.set(call)

                call.execute().use { response ->
                    currentCall.set(null)
                    updateRateLimitFromResponse(response)

                    if (!response.isSuccessful) {
                        return@withContext handleErrorResponse(response)
                    }

                    val body = response.body?.string() ?: return@withContext AiResult.Error(
                        ApiError("Empty response body", statusCode = response.code)
                    )

                    val completionResponse = parseCompletionResponse(body)
                    AiResult.Success(completionResponse)
                }
            } catch (e: IOException) {
                currentCall.set(null)
                AiResult.Error(ApiError(e.message ?: "Network error", statusCode = 0))
            } catch (e: Exception) {
                currentCall.set(null)
                AiResult.Error(ApiError(e.message ?: "Unknown error", statusCode = 500))
            }
        }

    override fun streamComplete(request: CompletionRequest): Flow<StreamChunk> = callbackFlow {
        val rateLimitCheck: AiResult<Unit>? = checkRateLimitBeforeRequest()
        if (rateLimitCheck != null && rateLimitCheck is AiResult.Error) {
            trySend(StreamChunk.Error(
                rateLimitCheck.error.message,
                rateLimitCheck.error.code,
                rateLimitCheck.error.isRetryable()
            ))
            close()
            return@callbackFlow
        }

        val jsonBody = buildRequestBody(request.copy(stream = true))
        val httpRequest = buildHttpRequest(jsonBody, true)

        val pendingToolCalls = mutableMapOf<Int, ToolCallBuilder>()
        var isFirstContent = true
        var hasError = false

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                updateRateLimitFromResponse(response)
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    // Complete any pending tool calls
                    pendingToolCalls.values.forEach { builder ->
                        builder.build()?.let { toolCall ->
                            trySend(StreamChunk.ToolCallEnd(toolCall.id))
                        }
                    }
                    trySend(StreamChunk.Done)
                    return
                }

                try {
                    val chunk = parseStreamChunk(data, pendingToolCalls, isFirstContent)
                    chunk.forEach { streamChunk ->
                        if (streamChunk is StreamChunk.Content && isFirstContent) {
                            isFirstContent = false
                        }
                        trySend(streamChunk)
                    }
                } catch (e: Exception) {
                    trySend(StreamChunk.Error(e.message ?: "Parse error", isRetryable = false))
                }
            }

            override fun onClosed(eventSource: EventSource) {
                if (!hasError) {
                    close()
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                hasError = true
                response?.let { updateRateLimitFromResponse(it) }

                val error = when {
                    response != null && !response.isSuccessful -> {
                        parseErrorFromResponse(response)
                    }
                    t != null -> {
                        ApiError(t.message ?: "Connection failed", statusCode = 0)
                    }
                    else -> {
                        ApiError("Unknown streaming error", statusCode = 500)
                    }
                }

                trySend(StreamChunk.Error(error.message, error.code, error.isRetryable()))
                close()
            }
        }

        val eventSource = EventSources.createFactory(streamingClient)
            .newEventSource(httpRequest, listener)

        currentEventSource.set(eventSource)

        awaitClose {
            currentEventSource.set(null)
            eventSource.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override fun cancelRequests() {
        currentEventSource.getAndSet(null)?.cancel()
        currentCall.getAndSet(null)?.cancel()
    }

    override fun getRateLimitStatus(): RateLimitStatus = rateLimitStatus.get()

    // Abstract methods for provider-specific implementations
    protected abstract fun getDefaultModels(): List<AiModel>
    protected abstract fun parseModelsResponse(body: String): List<AiModel>
    abstract override fun getDefaultModel(): String

    // Protected helper methods
    protected open fun buildHeaders(streaming: Boolean): okhttp3.Headers {
        val builder = okhttp3.Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Accept", if (streaming) "text/event-stream" else "application/json")

        provider.apiKey?.let { key ->
            builder.add("Authorization", "Bearer $key")
        }

        provider.headers.forEach { (key, value) ->
            builder.add(key, value)
        }

        return builder.build()
    }

    protected open fun buildRequestBody(request: CompletionRequest): String {
        val jsonObject = JsonObject()

        jsonObject.addProperty("model", request.model)
        jsonObject.add("messages", gson.toJsonTree(request.messages.map { msg ->
            buildMessageJson(msg)
        }))

        if (request.stream) {
            jsonObject.addProperty("stream", true)
            // Add stream_options for usage tracking in streaming
            val streamOptions = JsonObject()
            streamOptions.addProperty("include_usage", true)
            jsonObject.add("stream_options", streamOptions)
        }

        request.temperature?.let { jsonObject.addProperty("temperature", it) }
        request.topP?.let { jsonObject.addProperty("top_p", it) }
        request.maxTokens?.let { jsonObject.addProperty("max_tokens", it) }
        request.presencePenalty?.let { jsonObject.addProperty("presence_penalty", it) }
        request.frequencyPenalty?.let { jsonObject.addProperty("frequency_penalty", it) }
        request.stop?.let { jsonObject.add("stop", gson.toJsonTree(it)) }
        request.tools?.let { jsonObject.add("tools", gson.toJsonTree(it)) }
        request.toolChoice?.let { jsonObject.add("tool_choice", gson.toJsonTree(it)) }
        request.responseFormat?.let { jsonObject.add("response_format", gson.toJsonTree(it)) }
        request.user?.let { jsonObject.addProperty("user", it) }

        return gson.toJson(jsonObject)
    }

    protected open fun buildMessageJson(message: ChatMessage): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "role" to message.role.value,
            "content" to message.content
        )

        message.name?.let { map["name"] = it }
        message.toolCallId?.let { map["tool_call_id"] = it }
        message.toolCalls?.let { toolCalls ->
            map["tool_calls"] = toolCalls.map { tc ->
                mapOf(
                    "id" to tc.id,
                    "type" to tc.type,
                    "function" to mapOf(
                        "name" to tc.function.name,
                        "arguments" to tc.function.arguments
                    )
                )
            }
        }

        // Handle vision messages with images
        message.images?.let { images ->
            if (images.isNotEmpty()) {
                val contentList = mutableListOf<Map<String, Any>>()
                contentList.add(mapOf("type" to "text", "text" to message.content))
                images.forEach { imageUrl ->
                    contentList.add(mapOf(
                        "type" to "image_url",
                        "image_url" to mapOf("url" to imageUrl)
                    ))
                }
                map["content"] = contentList
            }
        }

        return map
    }

    protected open fun buildHttpRequest(jsonBody: String, streaming: Boolean): Request {
        val url = "${provider.baseUrl.trimEnd('/')}${provider.apiEndpoint}"
        return Request.Builder()
            .url(url)
            .headers(buildHeaders(streaming))
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
    }

    protected open fun parseCompletionResponse(body: String): CompletionResponse {
        val json = JsonParser.parseString(body).asJsonObject

        val id = getStringOrNull(json, "id") ?: ""
        val model = getStringOrNull(json, "model") ?: ""
        val created = getLongOrNull(json, "created") ?: System.currentTimeMillis()

        val choices = json.getAsJsonArray("choices")?.mapNotNull { choiceElement ->
            try {
                val choiceObj = choiceElement.asJsonObject
                val messageObj = choiceObj.getAsJsonObject("message") ?: return@mapNotNull null

                val toolCalls = messageObj.getAsJsonArray("tool_calls")?.mapNotNull { tcElement ->
                    try {
                        val tcObj = tcElement.asJsonObject
                        val funcObj = tcObj.getAsJsonObject("function") ?: return@mapNotNull null
                        ToolCall(
                            id = getStringOrNull(tcObj, "id") ?: return@mapNotNull null,
                            type = getStringOrNull(tcObj, "type") ?: "function",
                            function = FunctionCall(
                                name = getStringOrNull(funcObj, "name") ?: return@mapNotNull null,
                                arguments = getStringOrNull(funcObj, "arguments") ?: ""
                            )
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Safely extract content - it may be null in some responses
                val content = getStringOrNull(messageObj, "content") ?: ""
                val reasoning = getStringOrNull(messageObj, "reasoning_content")
                    ?: getStringOrNull(messageObj, "reasoning")

                Choice(
                    index = getIntOrNull(choiceObj, "index") ?: 0,
                    message = ChatMessage(
                        role = MessageRole.fromValue(getStringOrNull(messageObj, "role") ?: "assistant"),
                        content = content,
                        toolCalls = toolCalls,
                        reasoning = reasoning
                    ),
                    finishReason = getStringOrNull(choiceObj, "finish_reason")
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()

        val usageObj = json.getAsJsonObject("usage")
        val usage = usageObj?.let {
            UsageInfo(
                promptTokens = getIntOrNull(it, "prompt_tokens") ?: 0,
                completionTokens = getIntOrNull(it, "completion_tokens") ?: 0,
                totalTokens = getIntOrNull(it, "total_tokens") ?: 0
            )
        }

        return CompletionResponse(
            id = id,
            model = model,
            choices = choices,
            usage = usage,
            created = created
        )
    }

    /**
     * Safely get a long from a JSON object, returning null if the value is null or not a number
     */
    private fun getLongOrNull(json: JsonObject, key: String): Long? {
        val element = json.get(key) ?: return null
        if (element.isJsonNull) return null
        return try {
            element.asLong
        } catch (e: Exception) {
            null
        }
    }

    protected open fun parseStreamChunk(
        data: String,
        pendingToolCalls: MutableMap<Int, ToolCallBuilder>,
        isFirstContent: Boolean
    ): List<StreamChunk> {
        val chunks = mutableListOf<StreamChunk>()
        val json = JsonParser.parseString(data).asJsonObject

        // Check for error
        json.getAsJsonObject("error")?.let { errorObj ->
            val error = ApiError(
                message = getStringOrNull(errorObj, "message") ?: "Unknown error",
                type = getStringOrNull(errorObj, "type"),
                code = getStringOrNull(errorObj, "code")
            )
            chunks.add(StreamChunk.Error(error.message, error.code, error.isRetryable()))
            return chunks
        }

        // Extract model info
        getStringOrNull(json, "model")?.let { model ->
            chunks.add(StreamChunk.ModelInfo(model))
        }

        // Extract usage info
        json.getAsJsonObject("usage")?.let { usageObj ->
            chunks.add(StreamChunk.Usage(
                promptTokens = getIntOrNull(usageObj, "prompt_tokens") ?: 0,
                completionTokens = getIntOrNull(usageObj, "completion_tokens") ?: 0,
                totalTokens = getIntOrNull(usageObj, "total_tokens") ?: 0
            ))
        }

        // Process choices
        val choices = json.getAsJsonArray("choices") ?: return chunks
        if (choices.size() == 0) return chunks

        val choice = choices.get(0).asJsonObject
        val delta = choice.getAsJsonObject("delta") ?: return chunks

        // Extract content - safely handle null values
        getStringOrNull(delta, "content")?.let { content ->
            if (content.isNotEmpty()) {
                chunks.add(StreamChunk.Content(content, isFirstContent))
            }
        }

        // Extract reasoning content (for thinking models) - safely handle null values
        val reasoning = getStringOrNull(delta, "reasoning_content")
            ?: getStringOrNull(delta, "reasoning")
        if (!reasoning.isNullOrEmpty()) {
            chunks.add(StreamChunk.Reasoning(reasoning, isThinking = true))
        }

        // Extract tool calls
        delta.getAsJsonArray("tool_calls")?.forEach { tcElement ->
            val tcObj = tcElement.asJsonObject
            val index = getIntOrNull(tcObj, "index") ?: 0

            val builder = pendingToolCalls.getOrPut(index) { ToolCallBuilder() }

            getStringOrNull(tcObj, "id")?.let { id ->
                builder.id = id
                val funcObj = tcObj.getAsJsonObject("function")
                funcObj?.let {
                    getStringOrNull(it, "name")?.let { name ->
                        builder.name = name
                        chunks.add(StreamChunk.ToolCallStart(id, name))
                    }
                }
            }

            tcObj.getAsJsonObject("function")?.let { funcObj ->
                getStringOrNull(funcObj, "arguments")?.let { args ->
                    builder.appendArguments(args)
                    builder.id?.let { id ->
                        chunks.add(StreamChunk.ToolCallArguments(id, args))
                    }
                }
            }
        }

        // Check finish reason
        getStringOrNull(choice, "finish_reason")?.let { finishReason ->
            if (finishReason == "tool_calls") {
                // Complete all pending tool calls
                pendingToolCalls.values.forEach { builder ->
                    builder.build()?.let { toolCall ->
                        chunks.add(StreamChunk.ToolCallEnd(toolCall.id))
                    }
                }
            }
        }

        return chunks
    }

    /**
     * Safely get a string from a JSON object, returning null if the value is null or not a string
     */
    private fun getStringOrNull(json: JsonObject, key: String): String? {
        val element = json.get(key) ?: return null
        if (element.isJsonNull) return null
        return try {
            element.asString
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Safely get an int from a JSON object, returning null if the value is null or not a number
     */
    private fun getIntOrNull(json: JsonObject, key: String): Int? {
        val element = json.get(key) ?: return null
        if (element.isJsonNull) return null
        return try {
            element.asInt
        } catch (e: Exception) {
            null
        }
    }

    protected open fun <T> handleErrorResponse(response: Response): AiResult<T> {
        val error = parseErrorFromResponse(response)
        return AiResult.Error(error)
    }

    protected open fun parseErrorFromResponse(response: Response): ApiError {
        return try {
            val body = response.body?.string() ?: ""
            val json = JsonParser.parseString(body).asJsonObject
            val errorObj = json.getAsJsonObject("error") ?: json

            ApiError(
                message = errorObj.get("message")?.asString ?: "Request failed",
                type = errorObj.get("type")?.asString,
                code = errorObj.get("code")?.asString,
                statusCode = response.code
            )
        } catch (e: Exception) {
            ApiError(
                message = "Request failed with status ${response.code}",
                statusCode = response.code
            )
        }
    }

    protected open fun updateRateLimitFromResponse(response: Response) {
        val headers = response.headers

        val remainingRequests = headers["x-ratelimit-remaining-requests"]?.toIntOrNull()
        val remainingTokens = headers["x-ratelimit-remaining-tokens"]?.toIntOrNull()
        val resetRequests = headers["x-ratelimit-reset-requests"]?.toLongOrNull()
        val resetTokens = headers["x-ratelimit-reset-tokens"]?.toLongOrNull()
        val retryAfter = headers["retry-after"]?.toLongOrNull()?.times(1000) // Convert to ms

        val isLimited = response.code == 429 ||
                (remainingRequests != null && remainingRequests <= 0) ||
                (remainingTokens != null && remainingTokens <= 0)

        val resetTime = maxOf(resetRequests ?: 0, resetTokens ?: 0)

        rateLimitStatus.set(RateLimitStatus(
            isLimited = isLimited,
            remainingRequests = remainingRequests,
            remainingTokens = remainingTokens,
            resetTime = if (resetTime > 0) System.currentTimeMillis() + resetTime else null,
            retryAfter = retryAfter
        ))
    }

    protected open fun <T> checkRateLimitBeforeRequest(): AiResult<T>? {
        val status = rateLimitStatus.get()
        if (status.isLimited && !status.canMakeRequest()) {
            return AiResult.Error(ApiError(
                message = "Rate limit exceeded. Retry after ${status.retryAfter ?: status.resetTime?.minus(System.currentTimeMillis()) ?: 60000}ms",
                type = "rate_limit_exceeded",
                code = "rate_limit_exceeded",
                statusCode = 429
            ))
        }
        return null
    }

    /**
     * Helper class for building tool calls from streaming chunks
     */
    protected class ToolCallBuilder {
        var id: String? = null
        var name: String? = null
        private val arguments = StringBuilder()

        fun appendArguments(args: String) {
            arguments.append(args)
        }

        fun build(): ToolCall? {
            val finalId = id ?: return null
            val finalName = name ?: return null
            return ToolCall(
                id = finalId,
                type = "function",
                function = FunctionCall(
                    name = finalName,
                    arguments = arguments.toString()
                )
            )
        }
    }
}
