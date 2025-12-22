package com.person.ally.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.person.ally.data.local.dao.UserProfileDao
import com.person.ally.data.model.LifeDomain
import com.person.ally.data.model.LifeDomainProgress
import com.person.ally.data.model.PersonalityTrait
import com.person.ally.data.model.Trend
import com.person.ally.data.model.UniversalContext
import com.person.ally.data.model.UserProfile
import com.person.ally.data.model.ValueItem
import kotlinx.coroutines.flow.Flow
import java.io.File

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val context: Context
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? = userProfileDao.getUserProfileOnce()

    suspend fun insertUserProfile(profile: UserProfile) = userProfileDao.insertUserProfile(profile)

    suspend fun updateUserProfile(profile: UserProfile) = userProfileDao.updateUserProfile(profile)

    suspend fun updateName(name: String, preferredName: String?) =
        userProfileDao.updateName(name, preferredName)

    suspend fun updateOnboardingStatus(completed: Boolean, step: Int) =
        userProfileDao.updateOnboardingStatus(completed, step)

    suspend fun updateStreak(streak: Int) = userProfileDao.updateStreak(streak)

    suspend fun incrementConversationCount() = userProfileDao.incrementConversationCount()

    suspend fun incrementMemoryCount() = userProfileDao.incrementMemoryCount()

    suspend fun incrementInsightCount() = userProfileDao.incrementInsightCount()

    suspend fun incrementAssessmentCount() = userProfileDao.incrementAssessmentCount()

    suspend fun updateLastActive() = userProfileDao.updateLastActive()

    suspend fun deleteUserProfile() = userProfileDao.deleteUserProfile()

    fun getUniversalContext(): Flow<UniversalContext?> = userProfileDao.getUniversalContext()

    suspend fun getUniversalContextOnce(): UniversalContext? = userProfileDao.getUniversalContextOnce()

    suspend fun insertUniversalContext(context: UniversalContext) =
        userProfileDao.insertUniversalContext(context)

    suspend fun updateUniversalContext(context: UniversalContext) =
        userProfileDao.updateUniversalContext(context)

    suspend fun markContextAsEdited() = userProfileDao.markContextAsEdited()

    suspend fun deleteUniversalContext() = userProfileDao.deleteUniversalContext()

    suspend fun updatePersonalityTraits(traits: List<PersonalityTrait>) {
        val profile = userProfileDao.getUserProfileOnce() ?: return
        val updated = profile.copy(
            personalityTraits = traits,
            updatedAt = System.currentTimeMillis()
        )
        userProfileDao.updateUserProfile(updated)
    }

    suspend fun updateCoreValues(values: List<ValueItem>) {
        val profile = userProfileDao.getUserProfileOnce() ?: return
        val updated = profile.copy(
            coreValues = values,
            updatedAt = System.currentTimeMillis()
        )
        userProfileDao.updateUserProfile(updated)
    }

    suspend fun updateLifeDomainProgress(domain: LifeDomain, score: Float, trend: Trend) {
        val profile = userProfileDao.getUserProfileOnce() ?: return
        val progress = profile.lifeDomainProgress.toMutableList()
        val existingIndex = progress.indexOfFirst { it.domain == domain }
        val newProgress = LifeDomainProgress(
            domain = domain,
            score = score,
            trend = trend,
            lastUpdated = System.currentTimeMillis()
        )
        if (existingIndex >= 0) {
            progress[existingIndex] = newProgress
        } else {
            progress.add(newProgress)
        }
        val updated = profile.copy(
            lifeDomainProgress = progress,
            updatedAt = System.currentTimeMillis()
        )
        userProfileDao.updateUserProfile(updated)
    }

    suspend fun exportContextAsJson(): String {
        val context = userProfileDao.getUniversalContextOnce() ?: return "{}"
        return gson.toJson(context)
    }

    suspend fun exportContextAsMarkdown(): String {
        val context = userProfileDao.getUniversalContextOnce() ?: return ""
        return context.toMarkdown()
    }

    suspend fun exportContextAsPlainText(): String {
        val context = userProfileDao.getUniversalContextOnce() ?: return ""
        return context.toPlainText()
    }

    suspend fun exportFullProfile(): String {
        val profile = userProfileDao.getUserProfileOnce()
        val context = userProfileDao.getUniversalContextOnce()
        val exportData = mapOf(
            "profile" to profile,
            "universalContext" to context,
            "exportedAt" to System.currentTimeMillis(),
            "version" to 1
        )
        return gson.toJson(exportData)
    }

    suspend fun exportToFile(format: ExportFormat): File? {
        val content = when (format) {
            ExportFormat.JSON -> exportContextAsJson()
            ExportFormat.MARKDOWN -> exportContextAsMarkdown()
            ExportFormat.PLAIN_TEXT -> exportContextAsPlainText()
            ExportFormat.FULL_PROFILE -> exportFullProfile()
        }

        val extension = when (format) {
            ExportFormat.JSON, ExportFormat.FULL_PROFILE -> "json"
            ExportFormat.MARKDOWN -> "md"
            ExportFormat.PLAIN_TEXT -> "txt"
        }

        return try {
            val fileName = "personally_export_${System.currentTimeMillis()}.$extension"
            val file = File(context.cacheDir, fileName)
            file.writeText(content)
            file
        } catch (e: Exception) {
            null
        }
    }

    suspend fun importFromJson(json: String): Boolean {
        return try {
            val context = gson.fromJson(json, UniversalContext::class.java)
            userProfileDao.updateUniversalContext(context.copy(
                isUserEdited = true,
                lastEditedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun regenerateContext(memories: List<com.person.ally.data.model.Memory>): UniversalContext {
        val existing = userProfileDao.getUniversalContextOnce() ?: UniversalContext()
        val profile = userProfileDao.getUserProfileOnce()

        val coreIdentityMemories = memories
            .filter { it.category == com.person.ally.data.model.MemoryCategory.CORE_IDENTITY }
            .sortedByDescending { it.importance.ordinal }
            .take(10)
            .map { it.content }

        val updated = existing.copy(
            coreIdentityPoints = coreIdentityMemories.ifEmpty { existing.coreIdentityPoints },
            summary = generateSummary(profile, coreIdentityMemories),
            lastGeneratedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            version = existing.version + 1
        )

        userProfileDao.updateUniversalContext(updated)
        return updated
    }

    private fun generateSummary(profile: UserProfile?, corePoints: List<String>): String {
        val name = profile?.displayName ?: "User"
        return if (corePoints.isEmpty()) {
            "$name is just getting started with PersonAlly."
        } else {
            "$name - ${corePoints.take(3).joinToString(". ")}."
        }
    }
}

enum class ExportFormat {
    JSON,
    MARKDOWN,
    PLAIN_TEXT,
    FULL_PROFILE
}
