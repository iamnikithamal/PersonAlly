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
 * Error response from the API
 */
data class ApiError(
    val message: String,
    val type: String? = null,
    val code: String? = null,
    val param: String? = null,
    val statusCode: Int = 500
) {
    fun isRateLimitError(): Boolean =
        statusCode == 429 || type == "rate_limit_exceeded" || code == "rate_limit_exceeded"

    fun isAuthError(): Boolean =
        statusCode == 401 || statusCode == 403 || type == "authentication_error"

    fun isRetryable(): Boolean =
        isRateLimitError() || statusCode >= 500
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
