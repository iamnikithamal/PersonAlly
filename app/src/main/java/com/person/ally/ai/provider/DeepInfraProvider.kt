package com.person.ally.ai.provider

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiProvider
import com.person.ally.ai.model.ChatMessage
import com.person.ally.ai.model.MessageRole
import com.person.ally.ai.model.StreamChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * DeepInfra AI provider implementation with proper SSE streaming.
 * Based on the working implementation from CodeX.
 */
class DeepInfraProvider(
    provider: AiProvider = createDefaultProvider()
) : AiProviderClient {

    private val gson = Gson()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private var currentCall: Call? = null

    private val _provider = provider
    override fun getProvider(): AiProvider = _provider

    companion object {
        const val PROVIDER_ID = "deepinfra"
        const val BASE_URL = "https://api.deepinfra.com/v1/openai"
        const val MODELS_URL = "https://api.deepinfra.com/models/featured"
        const val DEFAULT_MODEL = "meta-llama/Llama-3.3-70B-Instruct"

        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        fun createDefaultProvider(): AiProvider = AiProvider(
            id = PROVIDER_ID,
            name = "DeepInfra",
            baseUrl = BASE_URL,
            apiEndpoint = "/chat/completions",
            modelsEndpoint = null,
            requiresApiKey = false,
            isEnabled = true,
            supportsStreaming = true,
            supportsToolCalling = true,
            supportsVision = true,
            supportsDynamicModels = true
        )
    }

    /**
     * Stream chat completion with proper SSE handling
     */
    override fun streamComplete(request: com.person.ally.ai.model.CompletionRequest): Flow<StreamChunk> = callbackFlow {
        val url = "${_provider.baseUrl}/chat/completions"

        // Build messages array
        val messagesArray = com.google.gson.JsonArray()
        request.messages.forEach { msg ->
            val msgObj = JsonObject()
            msgObj.addProperty("role", msg.role.name.lowercase())
            msgObj.addProperty("content", msg.content)
            messagesArray.add(msgObj)
        }

        // Build request body
        val requestBody = buildJsonObject {
            addProperty("model", request.model)
            add("messages", messagesArray)
            addProperty("stream", true)
            addProperty("temperature", request.temperature)
            addProperty("max_tokens", request.maxTokens)
        }.toString().toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${_provider.apiKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .addHeader("User-Agent", USER_AGENT)
            .post(requestBody)
            .build()

        currentCall = client.newCall(httpRequest)

        val eventSourceListener = object : EventSourceListener() {
            private var accumulatedContent = StringBuilder()
            private var accumulatedReasoning = StringBuilder()

            override fun onOpen(eventSource: EventSource, response: Response) {
                trySend(StreamChunk.Started)
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    trySend(StreamChunk.Done)
                    trySend(StreamChunk.Completed)
                    close()
                    return
                }

                try {
                    val chunk = gson.fromJson(data, StreamChunkResponse::class.java)

                    // Handle reasoning content for thinking models
                    chunk.choices?.firstOrNull()?.delta?.reasoningContent?.let { reasoning ->
                        if (reasoning.isNotEmpty()) {
                            accumulatedReasoning.append(reasoning)
                            trySend(StreamChunk.Reasoning(accumulatedReasoning.toString(), reasoning))
                        }
                    }

                    // Handle regular content
                    chunk.choices?.firstOrNull()?.delta?.content?.let { content ->
                        if (content.isNotEmpty()) {
                            accumulatedContent.append(content)
                            trySend(StreamChunk.Content(accumulatedContent.toString(), content))
                        }
                    }

                    // Handle usage
                    chunk.usage?.let { usage ->
                        trySend(StreamChunk.Usage(
                            promptTokens = usage.promptTokens,
                            completionTokens = usage.completionTokens,
                            totalTokens = usage.totalTokens
                        ))
                    }

                } catch (e: Exception) {
                    // Skip malformed chunks silently
                }
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val errorMessage = when {
                    response != null && response.code == 401 -> "Invalid API key"
                    response != null && response.code == 429 -> "Rate limit exceeded. Please try again later."
                    response != null && response.code == 503 -> "Service temporarily unavailable"
                    response != null -> "Request failed with status ${response.code}"
                    t != null -> t.message ?: "Network error"
                    else -> "Unknown error"
                }
                trySend(StreamChunk.Error(errorMessage, isRetryable = response?.code == 429))
                close()
            }
        }

        EventSources.createFactory(client).newEventSource(httpRequest, eventSourceListener)

        awaitClose {
            currentCall?.cancel()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Non-streaming completion
     */
    override suspend fun complete(request: com.person.ally.ai.model.CompletionRequest): AiResult<com.person.ally.ai.model.CompletionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${_provider.baseUrl}/chat/completions"

                val messagesArray = com.google.gson.JsonArray()
                request.messages.forEach { msg ->
                    val msgObj = JsonObject()
                    msgObj.addProperty("role", msg.role.name.lowercase())
                    msgObj.addProperty("content", msg.content)
                    messagesArray.add(msgObj)
                }

                val requestBody = buildJsonObject {
                    addProperty("model", request.model)
                    add("messages", messagesArray)
                    addProperty("stream", false)
                    addProperty("temperature", request.temperature)
                    addProperty("max_tokens", request.maxTokens)
                }.toString().toRequestBody("application/json".toMediaType())

                val httpRequest = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${_provider.apiKey}")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", USER_AGENT)
                    .post(requestBody)
                    .build()

                val response = client.newCall(httpRequest).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    return@withContext AiResult.Error(
                        com.person.ally.ai.model.ApiError(
                            message = "Request failed: ${response.code}",
                            statusCode = response.code
                        )
                    )
                }

                val responseBody = response.body?.string() ?: ""
                val completionResponse = gson.fromJson(responseBody, CompletionResponse::class.java)

                AiResult.Success(completionResponse)
            } catch (e: Exception) {
                AiResult.Error(
                    com.person.ally.ai.model.ApiError(
                        message = e.message ?: "Unknown error",
                        statusCode = null
                    )
                )
            }
        }

    /**
     * Fetch available models from DeepInfra
     */
    override suspend fun fetchModels(): AiResult<List<AiModel>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(MODELS_URL)
                .addHeader("User-Agent", USER_AGENT)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext AiResult.Error(
                    com.person.ally.ai.model.ApiError(
                        message = "Failed to fetch models: ${response.code}",
                        statusCode = response.code
                    )
                )
            }

            val body = response.body?.string() ?: return@withContext AiResult.Error(
                com.person.ally.ai.model.ApiError("Empty response", null)
            )

            val models = parseModelsResponse(body)
            AiResult.Success(models)
        } catch (e: Exception) {
            AiResult.Error(
                com.person.ally.ai.model.ApiError(e.message ?: "Unknown error", null)
            )
        }
    }

    /**
     * Check if provider is available
     */
    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${_provider.baseUrl}/models")
                .addHeader("User-Agent", USER_AGENT)
                .head()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cancel ongoing requests
     */
    override fun cancelRequests() {
        currentCall?.cancel()
    }

    /**
     * Get rate limit status
     */
    override fun getRateLimitStatus(): RateLimitStatus? = null

    /**
     * Parse models from DeepInfra response
     */
    private fun parseModelsResponse(body: String): List<AiModel> {
        val models = mutableListOf<AiModel>()

        try {
            val jsonArray = JsonParser.parseString(body).asJsonArray

            for (element in jsonArray) {
                val modelObj = element.asJsonObject
                val modelType = modelObj.get("type")?.asString
                val reportedType = modelObj.get("reported_type")?.asString

                if (modelType != "text-generation") continue

                val modelName = modelObj.get("model_name")?.asString ?: continue

                val displayName = extractDisplayName(modelName)
                val isThinking = isThinkingModel(modelName)
                val supportsToolCalling = supportsToolCalling(modelName)

                models.add(
                    AiModel(
                        id = "${PROVIDER_ID}:$modelName",
                        providerId = PROVIDER_ID,
                        modelId = modelName,
                        displayName = displayName,
                        description = modelObj.get("description")?.asString,
                        contextLength = modelObj.get("max_tokens")?.asInt ?: 4096,
                        maxOutputTokens = modelObj.get("max_output_tokens")?.asInt ?: 2048,
                        isEnabled = true,
                        isDefault = modelName == DEFAULT_MODEL,
                        supportsStreaming = true,
                        supportsToolCalling = supportsToolCalling,
                        supportsVision = isVisionModel(modelName),
                        supportsReasoning = isThinking,
                        isThinkingModel = isThinking,
                        category = when {
                            isThinking -> com.person.ally.ai.model.ModelCategory.REASONING
                            isVisionModel(modelName) -> com.person.ally.ai.model.ModelCategory.VISION
                            else -> com.person.ally.ai.model.ModelCategory.CHAT
                        }
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty list on parsing error
        }

        return models.ifEmpty { getDefaultModels() }
    }

    /**
     * Get default models when API fetch fails
     */
    private fun getDefaultModels(): List<AiModel> = listOf(
        AiModel(
            id = "${PROVIDER_ID}:meta-llama/Llama-3.3-70B-Instruct",
            providerId = PROVIDER_ID,
            modelId = "meta-llama/Llama-3.3-70B-Instruct",
            displayName = "Llama 3.3 70B",
            description = "Latest Llama 3.3 instruction-tuned model",
            contextLength = 131072,
            maxOutputTokens = 4096,
            isEnabled = true,
            isDefault = true,
            supportsStreaming = true,
            supportsToolCalling = true,
            supportsVision = false,
            supportsReasoning = false,
            isThinkingModel = false,
            category = com.person.ally.ai.model.ModelCategory.CHAT
        ),
        AiModel(
            id = "${PROVIDER_ID}:deepseek-ai/DeepSeek-R1",
            providerId = PROVIDER_ID,
            modelId = "deepseek-ai/DeepSeek-R1",
            displayName = "DeepSeek R1",
            description = "Advanced reasoning model with thinking process",
            contextLength = 65536,
            maxOutputTokens = 8192,
            isEnabled = true,
            isDefault = false,
            supportsStreaming = true,
            supportsToolCalling = true,
            supportsVision = false,
            supportsReasoning = true,
            isThinkingModel = true,
            category = com.person.ally.ai.model.ModelCategory.REASONING
        )
    )

    private fun extractDisplayName(modelId: String): String {
        val parts = modelId.split("/")
        return if (parts.size > 1) {
            parts[1].replace("-", " ").replace("_", " ")
        } else {
            modelId
        }
    }

    private fun isThinkingModel(modelId: String): Boolean {
        val thinkingModels = setOf(
            "deepseek-ai/DeepSeek-R1",
            "deepseek-ai/DeepSeek-R1-Turbo",
            "Qwen/QwQ-32B",
            "microsoft/phi-4-reasoning-plus"
        )
        return thinkingModels.any { modelId.contains(it) }
    }

    private fun isVisionModel(modelId: String): Boolean {
        val visionModels = setOf(
            "meta-llama/Llama-3.2-90B-Vision-Instruct",
            "microsoft/Phi-4-multimodal-instruct"
        )
        return visionModels.any { modelId.contains(it) }
    }

    private fun supportsToolCalling(modelId: String): Boolean {
        // Most modern models support tool calling
        val nonToolCalling = setOf(
            "deepseek-ai/DeepSeek-R1",
            "Qwen/QwQ-32B"
        )
        return !nonToolCalling.any { modelId.contains(it) }
    }

    private fun buildJsonObject(block: JsonObject.() -> Unit): JsonObject {
        return JsonObject().apply(block)
    }
}

/**
 * Stream chunk response from API
 */
private data class StreamChunkResponse(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null
)

private data class Choice(
    val index: Int? = null,
    val delta: Delta? = null,
    val finishReason: String? = null
)

private data class Delta(
    val content: String? = null,
    val reasoningContent: String? = null,
    val role: String? = null
)

private data class Usage(
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null
)

/**
 * Completion response from API
 */
private data class CompletionResponse(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<CompletionChoice>? = null,
    val usage: Usage? = null
)

private data class CompletionChoice(
    val index: Int? = null,
    val message: Message? = null,
    val finishReason: String? = null,
    val logprobs: Any? = null
)

private data class Message(
    val role: String? = null,
    val content: String? = null
)
