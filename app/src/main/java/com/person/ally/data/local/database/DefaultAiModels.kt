package com.person.ally.data.local.database

import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.ModelCapabilities
import com.person.ally.ai.model.ModelCategory
import com.person.ally.ai.model.ModelParameters
import com.person.ally.ai.provider.DeepInfraProvider

/**
 * Default AI models for each provider.
 * These are populated when the database is first created.
 */
object DefaultAiModels {

    /**
     * Get default DeepInfra models
     */
    fun getDeepInfraModels(): List<AiModel> {
        return listOf(
            // DeepSeek Reasoning Models
            createDeepInfraModel(
                modelId = "deepseek-ai/DeepSeek-R1",
                displayName = "DeepSeek R1",
                alias = "deepseek-r1",
                description = "Advanced reasoning model with thinking process output",
                contextLength = 65536,
                isThinking = true,
                supportsReasoning = true,
                category = ModelCategory.REASONING
            ),
            createDeepInfraModel(
                modelId = "deepseek-ai/DeepSeek-R1-Turbo",
                displayName = "DeepSeek R1 Turbo",
                alias = "deepseek-r1-turbo",
                description = "Faster version of DeepSeek R1 with lower latency",
                contextLength = 65536,
                isThinking = true,
                supportsReasoning = true,
                category = ModelCategory.REASONING
            ),
            createDeepInfraModel(
                modelId = "deepseek-ai/DeepSeek-R1-Distill-Llama-70B",
                displayName = "DeepSeek R1 Distill Llama 70B",
                alias = "deepseek-r1-distill-llama-70b",
                description = "Distilled R1 reasoning into Llama 70B",
                contextLength = 65536,
                isThinking = true,
                supportsReasoning = true,
                category = ModelCategory.REASONING
            ),
            createDeepInfraModel(
                modelId = "deepseek-ai/DeepSeek-V3-0324",
                displayName = "DeepSeek V3",
                alias = "deepseek-v3",
                description = "Latest DeepSeek general purpose model with excellent performance",
                contextLength = 65536,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),

            // Meta Llama Models
            createDeepInfraModel(
                modelId = "meta-llama/Llama-3.3-70B-Instruct",
                displayName = "Llama 3.3 70B",
                alias = "llama-3.3-70b",
                description = "Latest Llama 3.3 instruction-tuned model - excellent for general tasks",
                contextLength = 131072,
                supportsToolCalling = true,
                isDefault = true,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "meta-llama/Meta-Llama-3.1-8B-Instruct",
                displayName = "Llama 3.1 8B",
                alias = "llama-3.1-8b",
                description = "Efficient Llama 3.1 model - fast and capable",
                contextLength = 131072,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "meta-llama/Llama-3.2-90B-Vision-Instruct",
                displayName = "Llama 3.2 90B Vision",
                alias = "llama-3.2-90b",
                description = "Multimodal Llama model with vision capabilities",
                contextLength = 131072,
                supportsVision = true,
                category = ModelCategory.VISION
            ),
            createDeepInfraModel(
                modelId = "meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8",
                displayName = "Llama 4 Maverick",
                alias = "llama-4-maverick",
                description = "Llama 4 with Maverick architecture - cutting edge",
                contextLength = 131072,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "meta-llama/Llama-4-Scout-17B-16E-Instruct",
                displayName = "Llama 4 Scout",
                alias = "llama-4-scout",
                description = "Llama 4 Scout - optimized for exploration and research",
                contextLength = 131072,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),

            // Qwen Models
            createDeepInfraModel(
                modelId = "Qwen/Qwen3-235B-A22B",
                displayName = "Qwen 3 235B",
                alias = "qwen-3-235b",
                description = "Largest Qwen 3 model - exceptional capabilities",
                contextLength = 32768,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "Qwen/Qwen3-32B",
                displayName = "Qwen 3 32B",
                alias = "qwen-3-32b",
                description = "Qwen 3 32B - balanced performance and efficiency",
                contextLength = 32768,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "Qwen/Qwen3-14B",
                displayName = "Qwen 3 14B",
                alias = "qwen-3-14b",
                description = "Qwen 3 14B - efficient and capable",
                contextLength = 32768,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "Qwen/QwQ-32B",
                displayName = "QwQ 32B",
                alias = "qwq-32b",
                description = "Qwen reasoning model with thinking capabilities",
                contextLength = 32768,
                isThinking = true,
                supportsReasoning = true,
                category = ModelCategory.REASONING
            ),

            // Google Gemma Models
            createDeepInfraModel(
                modelId = "google/gemma-3-27b-it",
                displayName = "Gemma 3 27B",
                alias = "gemma-3-27b",
                description = "Google Gemma 3 instruction-tuned - latest generation",
                contextLength = 8192,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "google/gemma-3-12b-it",
                displayName = "Gemma 3 12B",
                alias = "gemma-3-12b",
                description = "Google Gemma 3 12B - balanced efficiency",
                contextLength = 8192,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "google/gemma-2-27b-it",
                displayName = "Gemma 2 27B",
                alias = "gemma-2-27b",
                description = "Google Gemma 2 instruction-tuned",
                contextLength = 8192,
                category = ModelCategory.CHAT
            ),

            // Microsoft Models
            createDeepInfraModel(
                modelId = "microsoft/phi-4",
                displayName = "Phi 4",
                alias = "phi-4",
                description = "Microsoft Phi 4 - small but remarkably powerful",
                contextLength = 16384,
                category = ModelCategory.CHAT
            ),
            createDeepInfraModel(
                modelId = "microsoft/phi-4-reasoning-plus",
                displayName = "Phi 4 Reasoning+",
                alias = "phi-4-reasoning-plus",
                description = "Phi 4 with enhanced reasoning capabilities",
                contextLength = 16384,
                isThinking = true,
                supportsReasoning = true,
                category = ModelCategory.REASONING
            ),
            createDeepInfraModel(
                modelId = "microsoft/Phi-4-multimodal-instruct",
                displayName = "Phi 4 Multimodal",
                alias = "phi-4-multimodal",
                description = "Phi 4 with vision capabilities",
                contextLength = 16384,
                supportsVision = true,
                category = ModelCategory.VISION
            ),

            // Mistral Models
            createDeepInfraModel(
                modelId = "mistralai/Mistral-Small-3.1-24B-Instruct-2503",
                displayName = "Mistral Small 3.1",
                alias = "mistral-small-3.1-24b",
                description = "Mistral Small 24B instruction model",
                contextLength = 32768,
                supportsToolCalling = true,
                category = ModelCategory.CHAT
            ),

            // Other Notable Models
            createDeepInfraModel(
                modelId = "cognitivecomputations/dolphin-2.9.1-llama-3-70b",
                displayName = "Dolphin 2.9 Llama 70B",
                alias = "dolphin-2.9",
                description = "Uncensored Dolphin model based on Llama 3 70B",
                contextLength = 8192,
                category = ModelCategory.CHAT
            )
        )
    }

    private fun createDeepInfraModel(
        modelId: String,
        displayName: String,
        alias: String? = null,
        description: String? = null,
        contextLength: Int = 4096,
        supportsVision: Boolean = false,
        isThinking: Boolean = false,
        supportsReasoning: Boolean = false,
        supportsToolCalling: Boolean = false,
        isDefault: Boolean = false,
        category: ModelCategory = ModelCategory.CHAT
    ): AiModel {
        return AiModel(
            id = "${DeepInfraProvider.PROVIDER_ID}:$modelId",
            providerId = DeepInfraProvider.PROVIDER_ID,
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
            category = category,
            capabilities = ModelCapabilities(
                functionCalling = supportsToolCalling,
                parallelToolCalls = supportsToolCalling,
                jsonMode = true,
                systemMessage = true,
                multiTurn = true,
                vision = supportsVision,
                reasoning = supportsReasoning
            ),
            parameters = ModelParameters(
                temperature = 0.7f,
                topP = 1.0f,
                maxTokens = minOf(4096, contextLength / 4)
            )
        )
    }
}
