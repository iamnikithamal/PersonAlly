package com.person.ally.ai.provider

import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiProvider
import com.person.ally.ai.model.ApiError
import com.person.ally.ai.model.CompletionRequest
import com.person.ally.ai.model.CompletionResponse
import com.person.ally.ai.model.StreamChunk
import kotlinx.coroutines.flow.Flow

/**
 * Result wrapper for AI operations
 */
sealed class AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>()
    data class Error(val error: ApiError) : AiResult<Nothing>()
    data class Loading(val progress: Float = 0f) : AiResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): ApiError? = (this as? Error)?.error

    inline fun <R> map(transform: (T) -> R): AiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }

    inline fun onSuccess(action: (T) -> Unit): AiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (ApiError) -> Unit): AiResult<T> {
        if (this is Error) action(error)
        return this
    }
}

/**
 * Interface for AI providers
 */
interface AiProviderClient {
    /**
     * Provider configuration
     */
    val provider: AiProvider

    /**
     * Check if the provider is available and working
     */
    suspend fun isAvailable(): Boolean

    /**
     * Fetch available models from the provider
     */
    suspend fun fetchModels(): AiResult<List<AiModel>>

    /**
     * Send a completion request and get a complete response
     */
    suspend fun complete(request: CompletionRequest): AiResult<CompletionResponse>

    /**
     * Send a completion request and stream the response
     */
    fun streamComplete(request: CompletionRequest): Flow<StreamChunk>

    /**
     * Cancel any ongoing requests
     */
    fun cancelRequests()

    /**
     * Check current rate limit status
     */
    fun getRateLimitStatus(): RateLimitStatus

    /**
     * Get default model for this provider
     */
    fun getDefaultModel(): String
}

/**
 * Rate limit status information
 */
data class RateLimitStatus(
    val isLimited: Boolean = false,
    val remainingRequests: Int? = null,
    val remainingTokens: Int? = null,
    val resetTime: Long? = null,
    val retryAfter: Long? = null
) {
    fun canMakeRequest(): Boolean = !isLimited || (resetTime != null && System.currentTimeMillis() >= resetTime)
}

/**
 * Factory for creating AI provider clients
 */
interface AiProviderFactory {
    fun createClient(provider: AiProvider): AiProviderClient
    fun getSupportedProviders(): List<String>
}

/**
 * Registry for AI providers
 */
object AiProviderRegistry {
    private val factories = mutableMapOf<String, AiProviderFactory>()

    fun registerFactory(providerId: String, factory: AiProviderFactory) {
        factories[providerId] = factory
    }

    fun getFactory(providerId: String): AiProviderFactory? = factories[providerId]

    fun getAllFactories(): Map<String, AiProviderFactory> = factories.toMap()

    fun createClient(provider: AiProvider): AiProviderClient? {
        return factories[provider.id]?.createClient(provider)
    }
}

/**
 * Listener for streaming events
 */
interface StreamListener {
    fun onChunk(chunk: StreamChunk)
    fun onComplete()
    fun onError(error: ApiError)
}

/**
 * Configuration for retry behavior with exponential backoff and jitter
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 1000,
    val maxDelayMs: Long = 30000,
    val backoffMultiplier: Float = 2.0f,
    val jitterFactor: Float = 0.2f, // Add 0-20% jitter to prevent thundering herd
    val retryOnRateLimit: Boolean = true,
    val retryOnServerError: Boolean = true,
    val retryOnNetworkError: Boolean = true
) {
    companion object {
        /** Aggressive retry config for critical operations */
        val AGGRESSIVE = RetryConfig(
            maxRetries = 5,
            initialDelayMs = 500,
            maxDelayMs = 60000,
            backoffMultiplier = 1.5f
        )

        /** Conservative retry config for non-critical operations */
        val CONSERVATIVE = RetryConfig(
            maxRetries = 2,
            initialDelayMs = 2000,
            maxDelayMs = 10000,
            backoffMultiplier = 2.5f
        )

        /** No retries */
        val NONE = RetryConfig(maxRetries = 0)
    }
}

/**
 * Extension function to execute with retry logic and exponential backoff with jitter
 */
suspend fun <T> withRetry(
    config: RetryConfig = RetryConfig(),
    onRetry: ((attempt: Int, error: ApiError, delayMs: Long) -> Unit)? = null,
    block: suspend (attempt: Int) -> AiResult<T>
): AiResult<T> {
    var lastError: ApiError? = null
    var currentDelay = config.initialDelayMs

    repeat(config.maxRetries + 1) { attempt ->
        when (val result = block(attempt)) {
            is AiResult.Success -> return result
            is AiResult.Error -> {
                lastError = result.error
                val shouldRetry = when {
                    attempt >= config.maxRetries -> false
                    result.error.isRateLimitError() && config.retryOnRateLimit -> true
                    result.error.statusCode >= 500 && config.retryOnServerError -> true
                    result.error.isNetworkError() && config.retryOnNetworkError -> true
                    result.error.isRetryable() -> true
                    else -> false
                }
                if (shouldRetry) {
                    // Add jitter to prevent thundering herd
                    val jitter = (currentDelay * config.jitterFactor * Math.random()).toLong()
                    val delayWithJitter = currentDelay + jitter

                    // Notify about retry
                    onRetry?.invoke(attempt, result.error, delayWithJitter)

                    kotlinx.coroutines.delay(delayWithJitter)
                    currentDelay = (currentDelay * config.backoffMultiplier).toLong()
                        .coerceAtMost(config.maxDelayMs)
                } else {
                    return result
                }
            }
            is AiResult.Loading -> { /* Continue waiting */ }
        }
    }

    return AiResult.Error(lastError ?: ApiError(
        message = "Request failed after ${config.maxRetries} retries",
        type = "retry_exhausted",
        statusCode = 500
    ))
}
