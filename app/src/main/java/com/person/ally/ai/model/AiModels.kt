package com.person.ally.ai.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Represents an AI provider (e.g., DeepInfra, OpenAI, etc.)
 */
@Entity(tableName = "ai_providers")
data class AiProvider(
    @PrimaryKey
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiEndpoint: String = "/chat/completions",
    val modelsEndpoint: String? = "/models",
    val requiresApiKey: Boolean = false,
    val apiKey: String? = null,
    val isEnabled: Boolean = true,
    val supportsStreaming: Boolean = true,
    val supportsToolCalling: Boolean = true,
    val supportsVision: Boolean = false,
    val supportsDynamicModels: Boolean = true,
    val headers: Map<String, String> = emptyMap(),
    val rateLimit: RateLimitConfig = RateLimitConfig(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Rate limit configuration for a provider
 */
data class RateLimitConfig(
    val requestsPerMinute: Int = 60,
    val tokensPerMinute: Int = 100000,
    val retryAfterMs: Long = 60000,
    val maxRetries: Int = 3,
    val backoffMultiplier: Float = 2.0f
)

/**
 * Represents an AI model within a provider
 */
@Entity(tableName = "ai_models")
data class AiModel(
    @PrimaryKey
    val id: String,
    val providerId: String,
    val modelId: String,
    val displayName: String,
    val alias: String? = null,
    val description: String? = null,
    val contextLength: Int = 4096,
    val maxOutputTokens: Int? = null,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val supportsStreaming: Boolean = true,
    val supportsToolCalling: Boolean = false,
    val supportsVision: Boolean = false,
    val supportsReasoning: Boolean = false,
    val isThinkingModel: Boolean = false,
    val category: ModelCategory = ModelCategory.CHAT,
    val pricing: ModelPricing? = null,
    val capabilities: ModelCapabilities = ModelCapabilities(),
    val parameters: ModelParameters = ModelParameters(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getDisplayNameOrAlias(): String = alias ?: displayName

    fun getFullModelId(): String = modelId
}

/**
 * Model categories
 */
enum class ModelCategory {
    CHAT,
    REASONING,
    CODING,
    VISION,
    IMAGE_GENERATION,
    EMBEDDING,
    AUDIO
}

/**
 * Pricing information for a model
 */
data class ModelPricing(
    val inputPerMillion: Float = 0f,
    val outputPerMillion: Float = 0f,
    val currency: String = "USD"
)

/**
 * Capabilities of a model
 */
data class ModelCapabilities(
    val functionCalling: Boolean = false,
    val parallelToolCalls: Boolean = false,
    val jsonMode: Boolean = false,
    val systemMessage: Boolean = true,
    val multiTurn: Boolean = true,
    val vision: Boolean = false,
    val audio: Boolean = false,
    val reasoning: Boolean = false,
    val searchGrounding: Boolean = false
)

/**
 * Default parameters for a model
 */
data class ModelParameters(
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val topK: Int? = null,
    val maxTokens: Int? = null,
    val presencePenalty: Float = 0f,
    val frequencyPenalty: Float = 0f,
    val repetitionPenalty: Float? = null,
    val stopSequences: List<String> = emptyList()
)

/**
 * Represents a chat message in the conversation
 */
data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val name: String? = null,
    val toolCalls: List<ToolCall>? = null,
    val toolCallId: String? = null,
    val reasoning: String? = null,
    val images: List<String>? = null
)

/**
 * Message roles
 */
enum class MessageRole(val value: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool");

    companion object {
        fun fromValue(value: String): MessageRole {
            return entries.find { it.value == value } ?: USER
        }
    }
}

/**
 * Represents a tool call made by the AI
 */
data class ToolCall(
    val id: String,
    val type: String = "function",
    val function: FunctionCall
)

/**
 * Function call details
 */
data class FunctionCall(
    val name: String,
    val arguments: String
)

/**
 * Tool definition for the AI
 */
data class ToolDefinition(
    val type: String = "function",
    val function: FunctionDefinition
)

/**
 * Function definition
 */
data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: FunctionParameters,
    val strict: Boolean = false
)

/**
 * Function parameters schema
 */
data class FunctionParameters(
    val type: String = "object",
    val properties: Map<String, ParameterProperty>,
    val required: List<String> = emptyList(),
    val additionalProperties: Boolean = false
)

/**
 * Parameter property definition
 */
data class ParameterProperty(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null,
    val items: ParameterProperty? = null,
    val default: Any? = null
)

/**
 * Request configuration for AI completion
 */
data class CompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxTokens: Int? = null,
    val stream: Boolean = true,
    val tools: List<ToolDefinition>? = null,
    val toolChoice: Any? = null,
    val stop: List<String>? = null,
    val presencePenalty: Float? = null,
    val frequencyPenalty: Float? = null,
    val responseFormat: ResponseFormat? = null,
    val user: String? = null
)

/**
 * Response format specification
 */
data class ResponseFormat(
    val type: String = "text"
)

/**
 * Streaming response chunk
 */
sealed class StreamChunk {
    data class Content(val text: String, val isFirst: Boolean = false) : StreamChunk()
    data class Reasoning(val text: String, val isThinking: Boolean = true) : StreamChunk()
    data class ToolCallStart(val id: String, val name: String) : StreamChunk()
    data class ToolCallArguments(val id: String, val arguments: String) : StreamChunk()
    data class ToolCallEnd(val id: String) : StreamChunk()
    data class Usage(val promptTokens: Int, val completionTokens: Int, val totalTokens: Int) : StreamChunk()
    data class ModelInfo(val model: String) : StreamChunk()
    data class Error(val message: String, val code: String? = null, val isRetryable: Boolean = false) : StreamChunk()
    data object Done : StreamChunk()
}

/**
 * Complete response from a non-streaming request
 */
data class CompletionResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>,
    val usage: UsageInfo?,
    val created: Long = System.currentTimeMillis()
)

/**
 * A single choice in the response
 */
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finishReason: String?
)

/**
 * Token usage information
 */
data class UsageInfo(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

/**
 * Error categories for better error handling
 */
enum class ErrorCategory {
    AUTHENTICATION,
    RATE_LIMIT,
    NETWORK,
    SERVER,
    INVALID_REQUEST,
    CONTEXT_LENGTH,
    CONTENT_FILTER,
    MODEL_NOT_FOUND,
    QUOTA_EXCEEDED,
    UNKNOWN
}

/**
 * Error response from the API with enhanced error handling
 */
data class ApiError(
    val message: String,
    val type: String? = null,
    val code: String? = null,
    val param: String? = null,
    val statusCode: Int = 500
) {
    /**
     * Categorize the error for handling
     */
    val category: ErrorCategory get() = when {
        isAuthError() -> ErrorCategory.AUTHENTICATION
        isRateLimitError() -> ErrorCategory.RATE_LIMIT
        isQuotaExceeded() -> ErrorCategory.QUOTA_EXCEEDED
        isContextLengthError() -> ErrorCategory.CONTEXT_LENGTH
        isContentFilterError() -> ErrorCategory.CONTENT_FILTER
        isModelNotFoundError() -> ErrorCategory.MODEL_NOT_FOUND
        isInvalidRequest() -> ErrorCategory.INVALID_REQUEST
        isNetworkError() -> ErrorCategory.NETWORK
        statusCode >= 500 -> ErrorCategory.SERVER
        else -> ErrorCategory.UNKNOWN
    }

    fun isRateLimitError(): Boolean =
        statusCode == 429 || type == "rate_limit_exceeded" || code == "rate_limit_exceeded"

    fun isAuthError(): Boolean =
        statusCode == 401 || statusCode == 403 ||
        type == "authentication_error" || code == "invalid_api_key"

    fun isQuotaExceeded(): Boolean =
        code == "insufficient_quota" || code == "billing_hard_limit_reached" ||
        message.contains("quota", ignoreCase = true) ||
        message.contains("billing", ignoreCase = true)

    fun isContextLengthError(): Boolean =
        code == "context_length_exceeded" ||
        message.contains("context length", ignoreCase = true) ||
        message.contains("maximum context", ignoreCase = true) ||
        message.contains("token limit", ignoreCase = true)

    fun isContentFilterError(): Boolean =
        code == "content_filter" || type == "content_policy_violation" ||
        message.contains("content policy", ignoreCase = true) ||
        message.contains("safety", ignoreCase = true)

    fun isModelNotFoundError(): Boolean =
        statusCode == 404 || code == "model_not_found" ||
        message.contains("model not found", ignoreCase = true) ||
        message.contains("does not exist", ignoreCase = true)

    fun isInvalidRequest(): Boolean =
        statusCode == 400 || type == "invalid_request_error"

    fun isNetworkError(): Boolean =
        statusCode == 0 || statusCode == -1 ||
        message.contains("network", ignoreCase = true) ||
        message.contains("timeout", ignoreCase = true) ||
        message.contains("connection", ignoreCase = true)

    fun isRetryable(): Boolean =
        isRateLimitError() || statusCode >= 500 || isNetworkError()

    /**
     * Get a user-friendly error message
     */
    fun getUserFriendlyMessage(): String = when (category) {
        ErrorCategory.AUTHENTICATION ->
            "Authentication failed. Please check your API key in Settings."
        ErrorCategory.RATE_LIMIT ->
            "Too many requests. Please wait a moment and try again."
        ErrorCategory.QUOTA_EXCEEDED ->
            "API quota exceeded. Please check your billing settings with your AI provider."
        ErrorCategory.CONTEXT_LENGTH ->
            "Message is too long. Try shortening your conversation or starting a new chat."
        ErrorCategory.CONTENT_FILTER ->
            "Your message was flagged by content filters. Please try rephrasing."
        ErrorCategory.MODEL_NOT_FOUND ->
            "The selected model is not available. Please choose a different model in Settings."
        ErrorCategory.NETWORK ->
            "Network error. Please check your internet connection and try again."
        ErrorCategory.SERVER ->
            "The AI service is temporarily unavailable. Please try again in a few moments."
        ErrorCategory.INVALID_REQUEST ->
            "Invalid request. Please try again or contact support if the issue persists."
        ErrorCategory.UNKNOWN ->
            message.take(150).let { if (message.length > 150) "$it..." else it }
    }

    /**
     * Get suggested action for the error
     */
    fun getSuggestedAction(): String? = when (category) {
        ErrorCategory.AUTHENTICATION -> "Go to Settings to update your API key"
        ErrorCategory.RATE_LIMIT -> "Wait ${getRetryDelaySeconds()}s before retrying"
        ErrorCategory.QUOTA_EXCEEDED -> "Check billing in your AI provider dashboard"
        ErrorCategory.CONTEXT_LENGTH -> "Start a new conversation"
        ErrorCategory.MODEL_NOT_FOUND -> "Select a different model"
        ErrorCategory.NETWORK -> "Check your internet connection"
        ErrorCategory.SERVER -> "Retry in a few moments"
        else -> null
    }

    /**
     * Get retry delay in seconds based on error type
     */
    fun getRetryDelaySeconds(): Int = when (category) {
        ErrorCategory.RATE_LIMIT -> 10
        ErrorCategory.SERVER -> 5
        ErrorCategory.NETWORK -> 3
        else -> 0
    }
}

/**
 * Type converters for Room database
 */
class AiModelTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromModelCategory(category: ModelCategory): String = category.name

    @TypeConverter
    fun toModelCategory(value: String): ModelCategory =
        ModelCategory.valueOf(value)

    @TypeConverter
    fun fromModelPricing(pricing: ModelPricing?): String? =
        pricing?.let { gson.toJson(it) }

    @TypeConverter
    fun toModelPricing(value: String?): ModelPricing? =
        value?.let { gson.fromJson(it, ModelPricing::class.java) }

    @TypeConverter
    fun fromModelCapabilities(capabilities: ModelCapabilities): String =
        gson.toJson(capabilities)

    @TypeConverter
    fun toModelCapabilities(value: String): ModelCapabilities =
        gson.fromJson(value, ModelCapabilities::class.java)

    @TypeConverter
    fun fromModelParameters(parameters: ModelParameters): String =
        gson.toJson(parameters)

    @TypeConverter
    fun toModelParameters(value: String): ModelParameters =
        gson.fromJson(value, ModelParameters::class.java)

    @TypeConverter
    fun fromRateLimitConfig(config: RateLimitConfig): String =
        gson.toJson(config)

    @TypeConverter
    fun toRateLimitConfig(value: String): RateLimitConfig =
        gson.fromJson(value, RateLimitConfig::class.java)
}
