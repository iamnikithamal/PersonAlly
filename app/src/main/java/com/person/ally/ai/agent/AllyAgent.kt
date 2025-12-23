package com.person.ally.ai.agent

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.ApiError
import com.person.ally.ai.model.ChatMessage
import com.person.ally.ai.model.CompletionRequest
import com.person.ally.ai.model.ErrorCategory
import com.person.ally.ai.model.FunctionCall
import com.person.ally.ai.model.FunctionDefinition
import com.person.ally.ai.model.FunctionParameters
import com.person.ally.ai.model.MessageRole
import com.person.ally.ai.model.ParameterProperty
import com.person.ally.ai.model.StreamChunk
import com.person.ally.ai.model.ToolCall
import com.person.ally.ai.model.ToolDefinition
import com.person.ally.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

/**
 * Ally - The AI Agent for PersonAlly.
 * Manages conversations, tool calling, and context-aware responses.
 */
class AllyAgent(
    private val aiRepository: AiRepository,
    private val toolRegistry: ToolRegistry
) {
    private val gson = Gson()
    private val conversationHistory = mutableListOf<ChatMessage>()

    private val _agentState = MutableStateFlow<AgentState>(AgentState.Idle)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    // Debouncing for state updates to prevent UI jank
    private val lastStateUpdate = AtomicLong(0)
    private companion object {
        const val STATE_DEBOUNCE_MS = 50L
        const val MIN_CONTENT_LENGTH_FOR_UPDATE = 10
    }

    /**
     * Represents the current state of the agent
     */
    sealed class AgentState {
        data object Idle : AgentState()
        data class Thinking(val reasoning: String = "") : AgentState()
        data class Generating(val partialContent: String = "") : AgentState()
        data class ExecutingTool(val toolName: String, val arguments: String) : AgentState()
        data class Error(
            val message: String,
            val isRetryable: Boolean = false,
            val category: ErrorCategory = ErrorCategory.UNKNOWN,
            val suggestedAction: String? = null
        ) : AgentState() {
            companion object {
                fun fromApiError(error: ApiError): Error = Error(
                    message = error.getUserFriendlyMessage(),
                    isRetryable = error.isRetryable(),
                    category = error.category,
                    suggestedAction = error.getSuggestedAction()
                )

                fun fromMessage(message: String): Error {
                    // Create a temporary ApiError to leverage its categorization logic
                    val inferredError = ApiError(message = message, statusCode = 500)
                    return Error(
                        message = inferredError.getUserFriendlyMessage(),
                        isRetryable = inferredError.isRetryable(),
                        category = inferredError.category,
                        suggestedAction = inferredError.getSuggestedAction()
                    )
                }
            }
        }
        data class Complete(val response: AgentResponse) : AgentState()
    }

    /**
     * Response from the agent
     */
    data class AgentResponse(
        val content: String,
        val reasoning: String? = null,
        val toolResults: List<ToolResult> = emptyList(),
        val tokensUsed: Int = 0,
        val memoryExtracted: Boolean = false,
        val extractedMemoryIds: List<Long> = emptyList()
    )

    /**
     * Result from a tool execution
     */
    data class ToolResult(
        val toolName: String,
        val arguments: Map<String, Any?>,
        val result: String,
        val success: Boolean
    )

    /**
     * Generate the system prompt for Ally
     */
    fun generateSystemPrompt(
        userName: String,
        userContext: String? = null,
        personality: String = "warm",
        responseLength: String = "balanced"
    ): String {
        val personalityPrompt = when (personality.lowercase()) {
            "professional" -> "You communicate in a professional, clear, and efficient manner while remaining approachable."
            "analytical" -> "You approach conversations with analytical depth, providing logical insights and structured responses."
            "creative" -> "You communicate with creativity and imagination, offering unique perspectives and innovative ideas."
            else -> "You are warm, empathetic, and supportive in your communication style."
        }

        val lengthGuidance = when (responseLength.lowercase()) {
            "concise" -> "Keep your responses brief and to the point, typically 1-2 sentences unless more detail is specifically needed."
            "detailed" -> "Provide thorough, comprehensive responses with examples and context when helpful."
            "verbose" -> "Give extensive, detailed responses with multiple perspectives, examples, and deep exploration of topics."
            else -> "Balance brevity with helpfulness, adjusting response length to match the complexity of the query."
        }

        return buildString {
            appendLine("You are Ally, a deeply intelligent personal AI companion in the PersonAlly app.")
            appendLine()
            appendLine("## Your Identity")
            appendLine("- You are warm, thoughtful, and genuinely care about the user's well-being and growth.")
            appendLine("- You remember and reference past conversations and user context when relevant.")
            appendLine("- You help with self-discovery, goal-setting, reflection, and personal growth.")
            appendLine("- You are honest, respectful, and maintain appropriate boundaries.")
            appendLine()
            appendLine("## Communication Style")
            appendLine(personalityPrompt)
            appendLine(lengthGuidance)
            appendLine()
            appendLine("## User Information")
            appendLine("User's name: $userName")
            if (!userContext.isNullOrBlank()) {
                appendLine()
                appendLine("## User Context")
                appendLine(userContext)
            }
            appendLine()
            appendLine("## Tool Usage")
            appendLine("You have access to tools that help you understand and assist the user better.")
            appendLine("When you need information from the app (memories, goals, insights, etc.), use the appropriate tool.")
            appendLine("Always explain what you're doing when using tools, and incorporate tool results naturally into your response.")
            appendLine()
            appendLine("## Guidelines")
            appendLine("- Be supportive but honest - don't just agree with everything.")
            appendLine("- Ask thoughtful follow-up questions to understand the user better.")
            appendLine("- When appropriate, suggest creating memories, setting goals, or taking assessments.")
            appendLine("- Reference past context and memories when relevant to show continuity.")
            appendLine("- If you notice patterns or insights, share them thoughtfully.")
            appendLine("- Respect the user's autonomy and never be pushy.")
        }
    }

    /**
     * Process a user message and generate a response
     */
    suspend fun processMessage(
        userMessage: String,
        model: AiModel,
        systemPrompt: String,
        existingHistory: List<ChatMessage> = emptyList(),
        onChunk: suspend (StreamChunk) -> Unit = {}
    ): AgentResponse {
        _agentState.value = AgentState.Thinking()

        // Build conversation history
        val messages = buildConversationMessages(userMessage, existingHistory)

        // Get tool definitions if model supports tool calling
        val tools = if (model.supportsToolCalling) {
            toolRegistry.getToolDefinitions()
        } else null

        var fullContent = StringBuilder()
        var fullReasoning = StringBuilder()
        var tokensUsed = 0
        val toolResults = mutableListOf<ToolResult>()
        var pendingToolCalls = mutableListOf<ToolCall>()

        // Create completion request
        val request = aiRepository.createCompletionRequest(
            model = model,
            messages = messages,
            systemPrompt = systemPrompt,
            tools = tools,
            stream = true
        )

        // Track pending content for debounced updates
        var pendingContentLength = 0
        var pendingReasoningLength = 0

        // Helper function for debounced state updates
        fun shouldUpdateState(isFirst: Boolean, pendingLength: Int): Boolean {
            val now = System.currentTimeMillis()
            val lastUpdate = lastStateUpdate.get()
            return isFirst ||
                    now - lastUpdate >= STATE_DEBOUNCE_MS ||
                    pendingLength >= MIN_CONTENT_LENGTH_FOR_UPDATE
        }

        fun updateStateIfNeeded(newState: AgentState, pendingLength: Int, isFirst: Boolean = false): Boolean {
            if (shouldUpdateState(isFirst, pendingLength)) {
                _agentState.value = newState
                lastStateUpdate.set(System.currentTimeMillis())
                return true
            }
            return false
        }

        // Execute streaming completion
        aiRepository.streamCompletion(
            model = model,
            request = request,
            onChunk = { chunk ->
                when (chunk) {
                    is StreamChunk.Reasoning -> {
                        fullReasoning.append(chunk.text)
                        pendingReasoningLength += chunk.text.length
                        if (updateStateIfNeeded(
                                AgentState.Thinking(fullReasoning.toString()),
                                pendingReasoningLength
                            )) {
                            pendingReasoningLength = 0
                        }
                    }
                    is StreamChunk.Content -> {
                        fullContent.append(chunk.text)
                        pendingContentLength += chunk.text.length
                        if (updateStateIfNeeded(
                                AgentState.Generating(fullContent.toString()),
                                pendingContentLength,
                                isFirst = chunk.isFirst
                            )) {
                            pendingContentLength = 0
                        }
                    }
                    is StreamChunk.ToolCallStart -> {
                        pendingToolCalls.add(ToolCall(
                            id = chunk.id,
                            type = "function",
                            function = FunctionCall(name = chunk.name, arguments = "")
                        ))
                        _agentState.value = AgentState.ExecutingTool(chunk.name, "")
                        lastStateUpdate.set(System.currentTimeMillis())
                    }
                    is StreamChunk.ToolCallArguments -> {
                        val index = pendingToolCalls.indexOfFirst { it.id == chunk.id }
                        if (index >= 0) {
                            val existing = pendingToolCalls[index]
                            pendingToolCalls[index] = existing.copy(
                                function = existing.function.copy(
                                    arguments = existing.function.arguments + chunk.arguments
                                )
                            )
                        }
                    }
                    is StreamChunk.ToolCallEnd -> {
                        // Execute the tool
                        val toolCall = pendingToolCalls.find { it.id == chunk.id }
                        if (toolCall != null) {
                            val result = executeToolCall(toolCall)
                            toolResults.add(result)
                        }
                    }
                    is StreamChunk.Usage -> {
                        tokensUsed = chunk.totalTokens
                    }
                    else -> {}
                }
                onChunk(chunk)
            },
            onComplete = { content, reasoning, tokens ->
                tokensUsed = tokens
                // Final state update with complete content
                if (fullContent.isNotEmpty()) {
                    _agentState.value = AgentState.Generating(fullContent.toString())
                }
            },
            onError = { message, _ ->
                _agentState.value = AgentState.Error.fromMessage(message)
            }
        )

        // If we have tool calls that need processing, continue the conversation
        if (pendingToolCalls.isNotEmpty() && toolResults.isNotEmpty()) {
            val continueResponse = continueWithToolResults(
                model = model,
                systemPrompt = systemPrompt,
                messages = messages,
                toolCalls = pendingToolCalls,
                toolResults = toolResults,
                onChunk = onChunk
            )
            fullContent = StringBuilder(continueResponse.content)
            tokensUsed += continueResponse.tokensUsed
        }

        val response = AgentResponse(
            content = fullContent.toString(),
            reasoning = fullReasoning.toString().takeIf { it.isNotBlank() },
            toolResults = toolResults,
            tokensUsed = tokensUsed
        )

        _agentState.value = AgentState.Complete(response)
        return response
    }

    /**
     * Continue the conversation after tool execution
     */
    private suspend fun continueWithToolResults(
        model: AiModel,
        systemPrompt: String,
        messages: List<ChatMessage>,
        toolCalls: List<ToolCall>,
        toolResults: List<ToolResult>,
        onChunk: suspend (StreamChunk) -> Unit
    ): AgentResponse {
        // Build messages including assistant's tool calls and tool results
        val updatedMessages = messages.toMutableList()

        // Add assistant message with tool calls
        updatedMessages.add(ChatMessage(
            role = MessageRole.ASSISTANT,
            content = "",
            toolCalls = toolCalls
        ))

        // Add tool result messages
        toolCalls.forEachIndexed { index, toolCall ->
            val result = toolResults.getOrNull(index)
            updatedMessages.add(ChatMessage(
                role = MessageRole.TOOL,
                content = result?.result ?: "Tool execution failed",
                toolCallId = toolCall.id
            ))
        }

        var fullContent = StringBuilder()
        var tokensUsed = 0
        var pendingContentLength = 0

        val request = aiRepository.createCompletionRequest(
            model = model,
            messages = updatedMessages,
            systemPrompt = systemPrompt,
            tools = null, // Don't allow nested tool calls
            stream = true
        )

        aiRepository.streamCompletion(
            model = model,
            request = request,
            onChunk = { chunk ->
                when (chunk) {
                    is StreamChunk.Content -> {
                        fullContent.append(chunk.text)
                        pendingContentLength += chunk.text.length

                        // Debounced state update
                        val now = System.currentTimeMillis()
                        val lastUpdate = lastStateUpdate.get()
                        if (chunk.isFirst ||
                            now - lastUpdate >= STATE_DEBOUNCE_MS ||
                            pendingContentLength >= MIN_CONTENT_LENGTH_FOR_UPDATE) {
                            _agentState.value = AgentState.Generating(fullContent.toString())
                            lastStateUpdate.set(now)
                            pendingContentLength = 0
                        }
                    }
                    is StreamChunk.Usage -> {
                        tokensUsed = chunk.totalTokens
                    }
                    else -> {}
                }
                onChunk(chunk)
            },
            onComplete = { _, _, tokens ->
                tokensUsed = tokens
                // Final update with complete content
                _agentState.value = AgentState.Generating(fullContent.toString())
            },
            onError = { message, _ ->
                _agentState.value = AgentState.Error.fromMessage(message)
            }
        )

        return AgentResponse(
            content = fullContent.toString(),
            tokensUsed = tokensUsed
        )
    }

    /**
     * Execute a tool call and return the result
     */
    private suspend fun executeToolCall(toolCall: ToolCall): ToolResult {
        _agentState.value = AgentState.ExecutingTool(
            toolCall.function.name,
            toolCall.function.arguments
        )

        return try {
            val arguments = parseToolArguments(toolCall.function.arguments)
            val result = toolRegistry.executeTool(toolCall.function.name, arguments)
            ToolResult(
                toolName = toolCall.function.name,
                arguments = arguments,
                result = result,
                success = true
            )
        } catch (e: Exception) {
            ToolResult(
                toolName = toolCall.function.name,
                arguments = emptyMap(),
                result = "Error executing tool: ${e.message}",
                success = false
            )
        }
    }

    /**
     * Parse tool arguments from JSON string
     */
    private fun parseToolArguments(argumentsJson: String): Map<String, Any?> {
        return try {
            if (argumentsJson.isBlank()) return emptyMap()
            val jsonObject = JsonParser.parseString(argumentsJson).asJsonObject
            jsonObject.entrySet().associate { (key, value) ->
                key to when {
                    value.isJsonNull -> null
                    value.isJsonPrimitive -> {
                        val primitive = value.asJsonPrimitive
                        when {
                            primitive.isBoolean -> primitive.asBoolean
                            primitive.isNumber -> primitive.asNumber
                            else -> primitive.asString
                        }
                    }
                    value.isJsonArray -> gson.fromJson(value, List::class.java)
                    value.isJsonObject -> gson.fromJson(value, Map::class.java)
                    else -> value.toString()
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Build conversation messages for the API
     */
    private fun buildConversationMessages(
        userMessage: String,
        existingHistory: List<ChatMessage>
    ): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        // Add existing history (converted to API format)
        existingHistory.forEach { message ->
            messages.add(ChatMessage(
                role = message.role,
                content = message.content
            ))
        }

        // Add current user message
        messages.add(ChatMessage(
            role = MessageRole.USER,
            content = userMessage
        ))

        return messages
    }

    /**
     * For models that don't support native tool calling,
     * parse tool calls from the response content
     */
    fun parseToolCallsFromContent(content: String): List<ToolCall>? {
        // Look for JSON tool call format in the content
        val toolCallPattern = """\{\s*"tool":\s*"([^"]+)",\s*"arguments":\s*(\{[^}]+\})\s*\}""".toRegex()
        val matches = toolCallPattern.findAll(content)

        if (matches.count() == 0) return null

        return matches.mapIndexed { index, match ->
            ToolCall(
                id = "parsed_tool_$index",
                type = "function",
                function = FunctionCall(
                    name = match.groupValues[1],
                    arguments = match.groupValues[2]
                )
            )
        }.toList()
    }

    /**
     * Reset agent state
     */
    fun reset() {
        _agentState.value = AgentState.Idle
        conversationHistory.clear()
    }

    /**
     * Cancel ongoing generation
     */
    fun cancel() {
        aiRepository.cancelRequests()
        _agentState.value = AgentState.Idle
    }
}
