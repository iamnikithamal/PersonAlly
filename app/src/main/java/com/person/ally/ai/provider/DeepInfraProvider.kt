package com.person.ally.ai.provider

import com.google.gson.JsonParser
import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiProvider
import com.person.ally.ai.model.ModelCapabilities
import com.person.ally.ai.model.ModelCategory
import com.person.ally.ai.model.RateLimitConfig

/**
 * DeepInfra AI provider implementation.
 * Provides access to various open-source and proprietary models through DeepInfra's API.
 *
 * Based on the gpt4free implementation, DeepInfra uses OpenAI-compatible API format.
 */
class DeepInfraProvider(
    provider: AiProvider = createDefaultProvider()
) : BaseOpenAiProvider(provider) {

    companion object {
        const val PROVIDER_ID = "deepinfra"
        const val BASE_URL = "https://api.deepinfra.com/v1/openai"
        const val MODELS_URL = "https://api.deepinfra.com/models/featured"
        const val DEFAULT_MODEL = "meta-llama/Llama-3.3-70B-Instruct"

        /**
         * Creates the default DeepInfra provider configuration
         */
        fun createDefaultProvider(): AiProvider = AiProvider(
            id = PROVIDER_ID,
            name = "DeepInfra",
            baseUrl = BASE_URL,
            apiEndpoint = "/chat/completions",
            modelsEndpoint = null, // Uses different endpoint
            requiresApiKey = false,
            isEnabled = true,
            supportsStreaming = true,
            supportsToolCalling = true,
            supportsVision = true,
            supportsDynamicModels = true,
            rateLimit = RateLimitConfig(
                requestsPerMinute = 30,
                tokensPerMinute = 50000,
                retryAfterMs = 60000,
                maxRetries = 5,
                backoffMultiplier = 2.0f
            )
        )

        /**
         * Model aliases for shorter, friendlier names
         */
        val MODEL_ALIASES = mapOf(
            // DeepSeek models (Reasoning/Thinking)
            "deepseek-r1" to listOf("deepseek-ai/DeepSeek-R1", "deepseek-ai/DeepSeek-R1-0528"),
            "deepseek-r1-0528" to "deepseek-ai/DeepSeek-R1-0528",
            "deepseek-r1-0528-turbo" to "deepseek-ai/DeepSeek-R1-0528-Turbo",
            "deepseek-r1-turbo" to "deepseek-ai/DeepSeek-R1-Turbo",
            "deepseek-r1-distill-llama-70b" to "deepseek-ai/DeepSeek-R1-Distill-Llama-70B",
            "deepseek-r1-distill-qwen-32b" to "deepseek-ai/DeepSeek-R1-Distill-Qwen-32B",
            "deepseek-v3" to listOf("deepseek-ai/DeepSeek-V3", "deepseek-ai/DeepSeek-V3-0324"),
            "deepseek-v3-0324" to "deepseek-ai/DeepSeek-V3-0324",
            "deepseek-v3-0324-turbo" to "deepseek-ai/DeepSeek-V3-0324-Turbo",
            "deepseek-prover-v2" to "deepseek-ai/DeepSeek-Prover-V2-671B",

            // Meta Llama models
            "llama-3.1-8b" to "meta-llama/Meta-Llama-3.1-8B-Instruct",
            "llama-3.2-90b" to "meta-llama/Llama-3.2-90B-Vision-Instruct",
            "llama-3.3-70b" to "meta-llama/Llama-3.3-70B-Instruct",
            "llama-4-maverick" to "meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8",
            "llama-4-scout" to "meta-llama/Llama-4-Scout-17B-16E-Instruct",

            // Google Gemma models
            "gemma-2-27b" to "google/gemma-2-27b-it",
            "gemma-2-9b" to "google/gemma-2-9b-it",
            "gemma-3-4b" to "google/gemma-3-4b-it",
            "gemma-3-12b" to "google/gemma-3-12b-it",
            "gemma-3-27b" to "google/gemma-3-27b-it",

            // Microsoft models
            "phi-4" to "microsoft/phi-4",
            "phi-4-multimodal" to "microsoft/Phi-4-multimodal-instruct",
            "phi-4-reasoning-plus" to "microsoft/phi-4-reasoning-plus",
            "wizardlm-2-8x22b" to "microsoft/WizardLM-2-8x22B",

            // Qwen models
            "qwen-3-14b" to "Qwen/Qwen3-14B",
            "qwen-3-30b" to "Qwen/Qwen3-30B-A3B",
            "qwen-3-32b" to "Qwen/Qwen3-32B",
            "qwen-3-235b" to "Qwen/Qwen3-235B-A22B",
            "qwq-32b" to "Qwen/QwQ-32B",

            // Mistral models
            "mistral-small-3.1-24b" to "mistralai/Mistral-Small-3.1-24B-Instruct-2503",

            // Other models
            "dolphin-2.6" to "cognitivecomputations/dolphin-2.6-mixtral-8x7b",
            "dolphin-2.9" to "cognitivecomputations/dolphin-2.9.1-llama-3-70b",
            "airoboros-70b" to "deepinfra/airoboros-70b",
            "lzlv-70b" to "lizpreciatior/lzlv_70b_fp16_hf"
        )

        /**
         * Vision-capable models
         */
        val VISION_MODELS = setOf(
            "meta-llama/Llama-3.2-90B-Vision-Instruct",
            "microsoft/Phi-4-multimodal-instruct",
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b"
        )

        /**
         * Reasoning/Thinking models that output reasoning process
         */
        val THINKING_MODELS = setOf(
            "deepseek-ai/DeepSeek-R1",
            "deepseek-ai/DeepSeek-R1-0528",
            "deepseek-ai/DeepSeek-R1-0528-Turbo",
            "deepseek-ai/DeepSeek-R1-Turbo",
            "deepseek-ai/DeepSeek-R1-Distill-Llama-70B",
            "deepseek-ai/DeepSeek-R1-Distill-Qwen-32B",
            "deepseek-ai/DeepSeek-Prover-V2-671B",
            "microsoft/phi-4-reasoning-plus",
            "Qwen/QwQ-32B"
        )

        /**
         * Models with good tool calling support
         */
        val TOOL_CALLING_MODELS = setOf(
            "meta-llama/Meta-Llama-3.1-8B-Instruct",
            "meta-llama/Llama-3.3-70B-Instruct",
            "meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8",
            "meta-llama/Llama-4-Scout-17B-16E-Instruct",
            "deepseek-ai/DeepSeek-V3",
            "deepseek-ai/DeepSeek-V3-0324",
            "deepseek-ai/DeepSeek-V3-0324-Turbo",
            "Qwen/Qwen3-14B",
            "Qwen/Qwen3-32B",
            "Qwen/Qwen3-235B-A22B",
            "mistralai/Mistral-Small-3.1-24B-Instruct-2503"
        )
    }

    override fun getDefaultModel(): String = DEFAULT_MODEL

    override fun getDefaultModels(): List<AiModel> {
        return listOf(
            // DeepSeek Reasoning Models
            createModel(
                modelId = "deepseek-ai/DeepSeek-R1",
                displayName = "DeepSeek R1",
                description = "Advanced reasoning model with thinking process output",
                contextLength = 65536,
                isThinking = true,
                supportsReasoning = true
            ),
            createModel(
                modelId = "deepseek-ai/DeepSeek-R1-Turbo",
                displayName = "DeepSeek R1 Turbo",
                description = "Faster version of DeepSeek R1",
                contextLength = 65536,
                isThinking = true,
                supportsReasoning = true
            ),
            createModel(
                modelId = "deepseek-ai/DeepSeek-V3-0324",
                displayName = "DeepSeek V3",
                description = "Latest DeepSeek general purpose model",
                contextLength = 65536,
                supportsToolCalling = true
            ),

            // Meta Llama Models
            createModel(
                modelId = "meta-llama/Llama-3.3-70B-Instruct",
                displayName = "Llama 3.3 70B",
                description = "Latest Llama 3.3 instruction-tuned model",
                contextLength = 131072,
                supportsToolCalling = true,
                isDefault = true
            ),
            createModel(
                modelId = "meta-llama/Meta-Llama-3.1-8B-Instruct",
                displayName = "Llama 3.1 8B",
                description = "Efficient Llama 3.1 model",
                contextLength = 131072,
                supportsToolCalling = true
            ),
            createModel(
                modelId = "meta-llama/Llama-3.2-90B-Vision-Instruct",
                displayName = "Llama 3.2 90B Vision",
                description = "Multimodal Llama model with vision capabilities",
                contextLength = 131072,
                supportsVision = true
            ),
            createModel(
                modelId = "meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8",
                displayName = "Llama 4 Maverick",
                description = "Llama 4 with Maverick architecture",
                contextLength = 131072,
                supportsToolCalling = true
            ),

            // Qwen Models
            createModel(
                modelId = "Qwen/Qwen3-235B-A22B",
                displayName = "Qwen 3 235B",
                description = "Largest Qwen 3 model",
                contextLength = 32768,
                supportsToolCalling = true
            ),
            createModel(
                modelId = "Qwen/Qwen3-32B",
                displayName = "Qwen 3 32B",
                description = "Qwen 3 32B balanced model",
                contextLength = 32768,
                supportsToolCalling = true
            ),
            createModel(
                modelId = "Qwen/QwQ-32B",
                displayName = "QwQ 32B",
                description = "Qwen reasoning model",
                contextLength = 32768,
                isThinking = true,
                supportsReasoning = true
            ),

            // Google Gemma Models
            createModel(
                modelId = "google/gemma-3-27b-it",
                displayName = "Gemma 3 27B",
                description = "Google Gemma 3 instruction-tuned",
                contextLength = 8192
            ),
            createModel(
                modelId = "google/gemma-2-27b-it",
                displayName = "Gemma 2 27B",
                description = "Google Gemma 2 instruction-tuned",
                contextLength = 8192
            ),

            // Microsoft Models
            createModel(
                modelId = "microsoft/phi-4",
                displayName = "Phi 4",
                description = "Microsoft Phi 4 small but powerful model",
                contextLength = 16384
            ),
            createModel(
                modelId = "microsoft/phi-4-reasoning-plus",
                displayName = "Phi 4 Reasoning+",
                description = "Phi 4 with enhanced reasoning",
                contextLength = 16384,
                isThinking = true,
                supportsReasoning = true
            ),
            createModel(
                modelId = "microsoft/Phi-4-multimodal-instruct",
                displayName = "Phi 4 Multimodal",
                description = "Phi 4 with vision capabilities",
                contextLength = 16384,
                supportsVision = true
            ),

            // Mistral Models
            createModel(
                modelId = "mistralai/Mistral-Small-3.1-24B-Instruct-2503",
                displayName = "Mistral Small 3.1",
                description = "Mistral Small 24B instruction model",
                contextLength = 32768,
                supportsToolCalling = true
            )
        )
    }

    override fun parseModelsResponse(body: String): List<AiModel> {
        val models = mutableListOf<AiModel>()

        try {
            val jsonArray = JsonParser.parseString(body).asJsonArray

            for (element in jsonArray) {
                val modelObj = element.asJsonObject
                val modelType = modelObj.get("type")?.asString
                val reportedType = modelObj.get("reported_type")?.asString

                // Only include text generation models
                if (modelType != "text-generation") continue

                val modelName = modelObj.get("model_name")?.asString ?: continue

                models.add(createModel(
                    modelId = modelName,
                    displayName = extractDisplayName(modelName),
                    description = modelObj.get("description")?.asString,
                    contextLength = modelObj.get("max_tokens")?.asInt ?: 4096,
                    supportsVision = VISION_MODELS.contains(modelName),
                    isThinking = THINKING_MODELS.contains(modelName),
                    supportsReasoning = THINKING_MODELS.contains(modelName),
                    supportsToolCalling = TOOL_CALLING_MODELS.contains(modelName)
                ))
            }
        } catch (e: Exception) {
            // Fall back to default models if parsing fails
            return getDefaultModels()
        }

        return if (models.isEmpty()) getDefaultModels() else models
    }

    override suspend fun fetchModels(): AiResult<List<AiModel>> {
        return try {
            val request = okhttp3.Request.Builder()
                .url(MODELS_URL)
                .headers(buildHeaders(false))
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return AiResult.Success(getDefaultModels())
            }

            val body = response.body?.string() ?: return AiResult.Success(getDefaultModels())
            val models = parseModelsResponse(body)

            AiResult.Success(models)
        } catch (e: Exception) {
            AiResult.Success(getDefaultModels())
        }
    }

    private fun createModel(
        modelId: String,
        displayName: String,
        description: String? = null,
        contextLength: Int = 4096,
        supportsVision: Boolean = false,
        isThinking: Boolean = false,
        supportsReasoning: Boolean = false,
        supportsToolCalling: Boolean = false,
        isDefault: Boolean = false
    ): AiModel {
        val alias = MODEL_ALIASES.entries.find { (_, value) ->
            when (value) {
                is String -> value == modelId
                is List<*> -> value.contains(modelId)
                else -> false
            }
        }?.key

        return AiModel(
            id = "${PROVIDER_ID}:$modelId",
            providerId = PROVIDER_ID,
            modelId = modelId,
            displayName = displayName,
            alias = alias,
            description = description,
            contextLength = contextLength,
            isEnabled = true,
            isDefault = isDefault,
            supportsStreaming = true,
            supportsToolCalling = supportsToolCalling,
            supportsVision = supportsVision,
            supportsReasoning = supportsReasoning,
            isThinkingModel = isThinking,
            category = when {
                isThinking -> ModelCategory.REASONING
                supportsVision -> ModelCategory.VISION
                else -> ModelCategory.CHAT
            },
            capabilities = ModelCapabilities(
                functionCalling = supportsToolCalling,
                parallelToolCalls = supportsToolCalling,
                jsonMode = true,
                systemMessage = true,
                multiTurn = true,
                vision = supportsVision,
                reasoning = supportsReasoning
            )
        )
    }

    private fun extractDisplayName(modelId: String): String {
        // Extract a human-readable name from the model ID
        val parts = modelId.split("/")
        return if (parts.size > 1) {
            parts[1].replace("-", " ").replace("_", " ")
        } else {
            modelId
        }
    }
}

/**
 * Factory for creating DeepInfra provider clients
 */
class DeepInfraProviderFactory : AiProviderFactory {
    override fun createClient(provider: AiProvider): AiProviderClient {
        return DeepInfraProvider(provider)
    }

    override fun getSupportedProviders(): List<String> = listOf(DeepInfraProvider.PROVIDER_ID)
}
