package com.person.ally.data.repository

import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiProvider
import com.person.ally.ai.model.ChatMessage
import com.person.ally.ai.model.CompletionRequest
import com.person.ally.ai.model.MessageRole
import com.person.ally.ai.model.StreamChunk
import com.person.ally.ai.model.ToolDefinition
import com.person.ally.ai.provider.AiProviderClient
import com.person.ally.ai.provider.AiProviderRegistry
import com.person.ally.ai.provider.AiResult
import com.person.ally.ai.provider.DeepInfraProvider
import com.person.ally.ai.provider.DeepInfraProviderFactory
import com.person.ally.ai.provider.RateLimitStatus
import com.person.ally.ai.provider.RetryConfig
import com.person.ally.ai.provider.withRetry
import com.person.ally.data.local.dao.AiModelDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository for managing AI providers, models, and completions.
 * Provides a unified interface for all AI-related operations.
 */
class AiRepository(private val aiModelDao: AiModelDao) {

    private val providerClients = ConcurrentHashMap<String, AiProviderClient>()
    private val _currentModel = MutableStateFlow<AiModel?>(null)
    val currentModel: StateFlow<AiModel?> = _currentModel.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingContent = MutableStateFlow("")
    val streamingContent: StateFlow<String> = _streamingContent.asStateFlow()

    private val _streamingReasoning = MutableStateFlow("")
    val streamingReasoning: StateFlow<String> = _streamingReasoning.asStateFlow()

    init {
        // Register DeepInfra provider factory
        AiProviderRegistry.registerFactory(DeepInfraProvider.PROVIDER_ID, DeepInfraProviderFactory())
    }

    // ==================== Provider Operations ====================

    fun getAllProviders(): Flow<List<AiProvider>> = aiModelDao.getAllProviders()

    fun getEnabledProviders(): Flow<List<AiProvider>> = aiModelDao.getEnabledProviders()

    suspend fun getProviderById(providerId: String): AiProvider? = aiModelDao.getProviderById(providerId)

    suspend fun insertProvider(provider: AiProvider) = aiModelDao.insertProvider(provider)

    suspend fun updateProvider(provider: AiProvider) = aiModelDao.updateProvider(provider)

    suspend fun deleteProvider(provider: AiProvider) {
        providerClients.remove(provider.id)?.cancelRequests()
        aiModelDao.deleteModelsByProvider(provider.id)
        aiModelDao.deleteProvider(provider)
    }

    suspend fun setProviderEnabled(providerId: String, enabled: Boolean) {
        aiModelDao.setProviderEnabled(providerId, enabled)
        if (!enabled) {
            providerClients.remove(providerId)?.cancelRequests()
        }
    }

    suspend fun updateProviderApiKey(providerId: String, apiKey: String?) =
        aiModelDao.updateProviderApiKey(providerId, apiKey)

    // ==================== Model Operations ====================

    fun getAllModels(): Flow<List<AiModel>> = aiModelDao.getAllModels()

    fun getEnabledModels(): Flow<List<AiModel>> = aiModelDao.getEnabledModels()

    fun getModelsByProvider(providerId: String): Flow<List<AiModel>> =
        aiModelDao.getModelsByProvider(providerId)

    fun getEnabledModelsByProvider(providerId: String): Flow<List<AiModel>> =
        aiModelDao.getEnabledModelsByProvider(providerId)

    suspend fun getModelById(modelId: String): AiModel? = aiModelDao.getModelById(modelId)

    suspend fun getDefaultModel(): AiModel? = aiModelDao.getDefaultModel()

    fun getDefaultModelFlow(): Flow<AiModel?> = aiModelDao.getDefaultModelFlow()

    fun searchModels(query: String): Flow<List<AiModel>> = aiModelDao.searchModels(query)

    fun getToolCallingModels(): Flow<List<AiModel>> = aiModelDao.getToolCallingModels()

    fun getVisionModels(): Flow<List<AiModel>> = aiModelDao.getVisionModels()

    fun getThinkingModels(): Flow<List<AiModel>> = aiModelDao.getThinkingModels()

    suspend fun insertModel(model: AiModel) = aiModelDao.insertModel(model)

    suspend fun insertModels(models: List<AiModel>) = aiModelDao.insertModels(models)

    suspend fun updateModel(model: AiModel) = aiModelDao.updateModel(model)

    suspend fun deleteModel(model: AiModel) = aiModelDao.deleteModel(model)

    suspend fun setModelEnabled(modelId: String, enabled: Boolean) =
        aiModelDao.setModelEnabled(modelId, enabled)

    suspend fun updateModelAlias(modelId: String, alias: String?) =
        aiModelDao.updateModelAlias(modelId, alias)

    suspend fun setDefaultModel(modelId: String) {
        aiModelDao.setDefaultModel(modelId)
        _currentModel.value = aiModelDao.getModelById(modelId)
    }

    suspend fun setCurrentModel(model: AiModel) {
        _currentModel.value = model
    }

    // ==================== Provider Client Management ====================

    /**
     * Get or create a provider client for the given provider
     */
    private suspend fun getProviderClient(providerId: String): AiProviderClient? {
        return providerClients.getOrPut(providerId) {
            val provider = aiModelDao.getProviderById(providerId) ?: return null
            AiProviderRegistry.createClient(provider) ?: return null
        }
    }

    /**
     * Refresh models from a provider's API
     */
    suspend fun refreshModelsFromProvider(providerId: String): AiResult<List<AiModel>> =
        withContext(Dispatchers.IO) {
            val client = getProviderClient(providerId)
                ?: return@withContext AiResult.Error(
                    com.person.ally.ai.model.ApiError("Provider not found", statusCode = 404)
                )

            val result = client.fetchModels()
            if (result is AiResult.Success) {
                // Update database with fetched models
                result.data.forEach { model ->
                    val existingModel = aiModelDao.getModelById(model.id)
                    if (existingModel != null) {
                        // Preserve user settings
                        aiModelDao.updateModel(
                            model.copy(
                                isEnabled = existingModel.isEnabled,
                                alias = existingModel.alias,
                                isDefault = existingModel.isDefault
                            )
                        )
                    } else {
                        aiModelDao.insertModel(model)
                    }
                }
            }
            result
        }

    /**
     * Check if a provider is available
     */
    suspend fun isProviderAvailable(providerId: String): Boolean {
        val client = getProviderClient(providerId) ?: return false
        return client.isAvailable()
    }

    /**
     * Get rate limit status for a provider
     */
    fun getRateLimitStatus(providerId: String): RateLimitStatus? {
        return providerClients[providerId]?.getRateLimitStatus()
    }

    // ==================== Completion Operations ====================

    /**
     * Create a completion request
     */
    fun createCompletionRequest(
        model: AiModel,
        messages: List<ChatMessage>,
        systemPrompt: String? = null,
        tools: List<ToolDefinition>? = null,
        temperature: Float? = null,
        maxTokens: Int? = null,
        stream: Boolean = true
    ): CompletionRequest {
        val allMessages = mutableListOf<ChatMessage>()

        // Add system prompt if provided
        systemPrompt?.let {
            allMessages.add(ChatMessage(role = MessageRole.SYSTEM, content = it))
        }

        allMessages.addAll(messages)

        return CompletionRequest(
            model = model.modelId,
            messages = allMessages,
            temperature = temperature ?: model.parameters.temperature,
            maxTokens = maxTokens ?: model.maxOutputTokens,
            stream = stream,
            tools = if (model.supportsToolCalling) tools else null,
            toolChoice = if (tools != null && model.supportsToolCalling) "auto" else null
        )
    }

    /**
     * Execute a streaming completion with retry logic
     */
    suspend fun streamCompletion(
        model: AiModel,
        request: CompletionRequest,
        onChunk: suspend (StreamChunk) -> Unit,
        onComplete: suspend (fullContent: String, reasoning: String?, tokensUsed: Int) -> Unit,
        onError: suspend (String, Boolean) -> Unit
    ) = withContext(Dispatchers.IO) {
        val client = getProviderClient(model.providerId)
        if (client == null) {
            onError("Provider not available", false)
            return@withContext
        }

        _isGenerating.value = true
        _streamingContent.value = ""
        _streamingReasoning.value = ""

        var fullContent = StringBuilder()
        var fullReasoning = StringBuilder()
        var tokensUsed = 0

        try {
            client.streamComplete(request).collect { chunk ->
                when (chunk) {
                    is StreamChunk.Content -> {
                        fullContent.append(chunk.text)
                        _streamingContent.value = fullContent.toString()
                        onChunk(chunk)
                    }
                    is StreamChunk.Reasoning -> {
                        fullReasoning.append(chunk.text)
                        _streamingReasoning.value = fullReasoning.toString()
                        onChunk(chunk)
                    }
                    is StreamChunk.Usage -> {
                        tokensUsed = chunk.totalTokens
                        onChunk(chunk)
                    }
                    is StreamChunk.Error -> {
                        onError(chunk.message, chunk.isRetryable)
                    }
                    is StreamChunk.Done -> {
                        onComplete(
                            fullContent.toString(),
                            fullReasoning.toString().takeIf { it.isNotBlank() },
                            tokensUsed
                        )
                    }
                    else -> onChunk(chunk)
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error", false)
        } finally {
            _isGenerating.value = false
        }
    }

    /**
     * Execute a non-streaming completion with retry logic
     */
    suspend fun complete(
        model: AiModel,
        request: CompletionRequest,
        retryConfig: RetryConfig = RetryConfig()
    ): AiResult<com.person.ally.ai.model.CompletionResponse> = withContext(Dispatchers.IO) {
        val client = getProviderClient(model.providerId)
            ?: return@withContext AiResult.Error(
                com.person.ally.ai.model.ApiError("Provider not available", statusCode = 503)
            )

        _isGenerating.value = true
        try {
            withRetry(retryConfig) { attempt ->
                client.complete(request.copy(stream = false))
            }
        } finally {
            _isGenerating.value = false
        }
    }

    /**
     * Cancel any ongoing requests
     */
    fun cancelRequests() {
        providerClients.values.forEach { it.cancelRequests() }
        _isGenerating.value = false
    }

    /**
     * Initialize the repository with default model
     */
    suspend fun initialize() {
        val defaultModel = aiModelDao.getDefaultModel()
        if (defaultModel != null) {
            _currentModel.value = defaultModel
        } else {
            // Set first enabled model as default
            val enabledModels = aiModelDao.getEnabledModelsOnce()
            enabledModels.firstOrNull()?.let { model ->
                aiModelDao.setDefaultModel(model.id)
                _currentModel.value = model
            }
        }
    }

    /**
     * Ensure default providers and models exist
     */
    suspend fun ensureDefaultsExist() = withContext(Dispatchers.IO) {
        // Check if DeepInfra provider exists
        if (!aiModelDao.providerExists(DeepInfraProvider.PROVIDER_ID)) {
            // Insert default DeepInfra provider
            aiModelDao.insertProvider(DeepInfraProvider.createDefaultProvider())

            // Insert default models
            val provider = DeepInfraProvider()
            val defaultModels = provider.fetchModels().getOrNull() ?: emptyList()
            if (defaultModels.isNotEmpty()) {
                aiModelDao.insertModels(defaultModels)
            }
        }
    }
}
