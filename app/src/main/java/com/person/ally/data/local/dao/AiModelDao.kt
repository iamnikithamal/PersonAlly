package com.person.ally.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.person.ally.ai.model.AiModel
import com.person.ally.ai.model.AiProvider
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AI providers and models.
 * Handles all database operations for AI-related entities.
 */
@Dao
interface AiModelDao {

    // ==================== Provider Operations ====================

    /**
     * Get all providers as a Flow for reactive updates
     */
    @Query("SELECT * FROM ai_providers ORDER BY name ASC")
    fun getAllProviders(): Flow<List<AiProvider>>

    /**
     * Get all providers synchronously
     */
    @Query("SELECT * FROM ai_providers ORDER BY name ASC")
    suspend fun getAllProvidersOnce(): List<AiProvider>

    /**
     * Get enabled providers only
     */
    @Query("SELECT * FROM ai_providers WHERE isEnabled = 1 ORDER BY name ASC")
    fun getEnabledProviders(): Flow<List<AiProvider>>

    /**
     * Get provider by ID
     */
    @Query("SELECT * FROM ai_providers WHERE id = :providerId")
    suspend fun getProviderById(providerId: String): AiProvider?

    /**
     * Get provider by ID as Flow
     */
    @Query("SELECT * FROM ai_providers WHERE id = :providerId")
    fun getProviderByIdFlow(providerId: String): Flow<AiProvider?>

    /**
     * Insert a new provider
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: AiProvider)

    /**
     * Insert multiple providers
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<AiProvider>)

    /**
     * Update an existing provider
     */
    @Update
    suspend fun updateProvider(provider: AiProvider)

    /**
     * Delete a provider
     */
    @Delete
    suspend fun deleteProvider(provider: AiProvider)

    /**
     * Delete provider by ID
     */
    @Query("DELETE FROM ai_providers WHERE id = :providerId")
    suspend fun deleteProviderById(providerId: String)

    /**
     * Enable or disable a provider
     */
    @Query("UPDATE ai_providers SET isEnabled = :enabled, updatedAt = :timestamp WHERE id = :providerId")
    suspend fun setProviderEnabled(providerId: String, enabled: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Update provider API key
     */
    @Query("UPDATE ai_providers SET apiKey = :apiKey, updatedAt = :timestamp WHERE id = :providerId")
    suspend fun updateProviderApiKey(providerId: String, apiKey: String?, timestamp: Long = System.currentTimeMillis())

    // ==================== Model Operations ====================

    /**
     * Get all models as a Flow
     */
    @Query("SELECT * FROM ai_models ORDER BY displayName ASC")
    fun getAllModels(): Flow<List<AiModel>>

    /**
     * Get all models synchronously
     */
    @Query("SELECT * FROM ai_models ORDER BY displayName ASC")
    suspend fun getAllModelsOnce(): List<AiModel>

    /**
     * Get enabled models only
     */
    @Query("SELECT * FROM ai_models WHERE isEnabled = 1 ORDER BY displayName ASC")
    fun getEnabledModels(): Flow<List<AiModel>>

    /**
     * Get enabled models synchronously
     */
    @Query("SELECT * FROM ai_models WHERE isEnabled = 1 ORDER BY displayName ASC")
    suspend fun getEnabledModelsOnce(): List<AiModel>

    /**
     * Get models for a specific provider
     */
    @Query("SELECT * FROM ai_models WHERE providerId = :providerId ORDER BY displayName ASC")
    fun getModelsByProvider(providerId: String): Flow<List<AiModel>>

    /**
     * Get enabled models for a specific provider
     */
    @Query("SELECT * FROM ai_models WHERE providerId = :providerId AND isEnabled = 1 ORDER BY displayName ASC")
    fun getEnabledModelsByProvider(providerId: String): Flow<List<AiModel>>

    /**
     * Get model by ID
     */
    @Query("SELECT * FROM ai_models WHERE id = :modelId")
    suspend fun getModelById(modelId: String): AiModel?

    /**
     * Get model by ID as Flow
     */
    @Query("SELECT * FROM ai_models WHERE id = :modelId")
    fun getModelByIdFlow(modelId: String): Flow<AiModel?>

    /**
     * Get model by provider and model ID
     */
    @Query("SELECT * FROM ai_models WHERE providerId = :providerId AND modelId = :modelId")
    suspend fun getModelByProviderAndModelId(providerId: String, modelId: String): AiModel?

    /**
     * Get the default model
     */
    @Query("SELECT * FROM ai_models WHERE isDefault = 1 AND isEnabled = 1 LIMIT 1")
    suspend fun getDefaultModel(): AiModel?

    /**
     * Get the default model as Flow
     */
    @Query("SELECT * FROM ai_models WHERE isDefault = 1 AND isEnabled = 1 LIMIT 1")
    fun getDefaultModelFlow(): Flow<AiModel?>

    /**
     * Search models by name, alias, or description
     */
    @Query("""
        SELECT * FROM ai_models
        WHERE isEnabled = 1 AND (
            displayName LIKE '%' || :query || '%' OR
            alias LIKE '%' || :query || '%' OR
            description LIKE '%' || :query || '%' OR
            modelId LIKE '%' || :query || '%'
        )
        ORDER BY displayName ASC
    """)
    fun searchModels(query: String): Flow<List<AiModel>>

    /**
     * Get models by category
     */
    @Query("SELECT * FROM ai_models WHERE category = :category AND isEnabled = 1 ORDER BY displayName ASC")
    fun getModelsByCategory(category: String): Flow<List<AiModel>>

    /**
     * Get models with tool calling support
     */
    @Query("SELECT * FROM ai_models WHERE supportsToolCalling = 1 AND isEnabled = 1 ORDER BY displayName ASC")
    fun getToolCallingModels(): Flow<List<AiModel>>

    /**
     * Get models with vision support
     */
    @Query("SELECT * FROM ai_models WHERE supportsVision = 1 AND isEnabled = 1 ORDER BY displayName ASC")
    fun getVisionModels(): Flow<List<AiModel>>

    /**
     * Get thinking/reasoning models
     */
    @Query("SELECT * FROM ai_models WHERE isThinkingModel = 1 AND isEnabled = 1 ORDER BY displayName ASC")
    fun getThinkingModels(): Flow<List<AiModel>>

    /**
     * Insert a new model
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: AiModel)

    /**
     * Insert multiple models
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<AiModel>)

    /**
     * Update an existing model
     */
    @Update
    suspend fun updateModel(model: AiModel)

    /**
     * Delete a model
     */
    @Delete
    suspend fun deleteModel(model: AiModel)

    /**
     * Delete model by ID
     */
    @Query("DELETE FROM ai_models WHERE id = :modelId")
    suspend fun deleteModelById(modelId: String)

    /**
     * Delete all models for a provider
     */
    @Query("DELETE FROM ai_models WHERE providerId = :providerId")
    suspend fun deleteModelsByProvider(providerId: String)

    /**
     * Enable or disable a model
     */
    @Query("UPDATE ai_models SET isEnabled = :enabled, updatedAt = :timestamp WHERE id = :modelId")
    suspend fun setModelEnabled(modelId: String, enabled: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Update model alias
     */
    @Query("UPDATE ai_models SET alias = :alias, updatedAt = :timestamp WHERE id = :modelId")
    suspend fun updateModelAlias(modelId: String, alias: String?, timestamp: Long = System.currentTimeMillis())

    /**
     * Set a model as default (clears other defaults first)
     */
    @Transaction
    suspend fun setDefaultModel(modelId: String) {
        clearDefaultModels()
        setModelAsDefault(modelId)
    }

    @Query("UPDATE ai_models SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearDefaultModels()

    @Query("UPDATE ai_models SET isDefault = 1, updatedAt = :timestamp WHERE id = :modelId")
    suspend fun setModelAsDefault(modelId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Count enabled models
     */
    @Query("SELECT COUNT(*) FROM ai_models WHERE isEnabled = 1")
    suspend fun countEnabledModels(): Int

    /**
     * Count models for a provider
     */
    @Query("SELECT COUNT(*) FROM ai_models WHERE providerId = :providerId")
    suspend fun countModelsByProvider(providerId: String): Int

    /**
     * Check if a model exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM ai_models WHERE id = :modelId)")
    suspend fun modelExists(modelId: String): Boolean

    /**
     * Check if a provider exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM ai_providers WHERE id = :providerId)")
    suspend fun providerExists(providerId: String): Boolean
}
