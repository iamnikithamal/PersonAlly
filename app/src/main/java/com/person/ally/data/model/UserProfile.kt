package com.person.ally.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PersonalityTrait(
    val name: String,
    val score: Float,
    val description: String,
    val strengths: List<String> = emptyList(),
    val challenges: List<String> = emptyList()
)

data class ValueItem(
    val name: String,
    val importance: Float,
    val description: String
)

data class LifeDomainProgress(
    val domain: LifeDomain,
    val score: Float,
    val trend: Trend,
    val lastUpdated: Long,
    val goals: List<String> = emptyList(),
    val insights: List<String> = emptyList()
)

enum class LifeDomain {
    CAREER,
    RELATIONSHIPS,
    HEALTH,
    PERSONAL_GROWTH,
    FINANCE,
    CREATIVITY,
    SPIRITUALITY,
    RECREATION
}

enum class Trend {
    IMPROVING,
    STABLE,
    DECLINING,
    UNKNOWN
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Long = 1,
    val name: String = "",
    val preferredName: String? = null,
    val avatarUri: String? = null,
    val birthDate: Long? = null,
    val timezone: String = "UTC",
    val onboardingCompleted: Boolean = false,
    val onboardingStep: Int = 0,
    val personalityTraits: List<PersonalityTrait> = emptyList(),
    val coreValues: List<ValueItem> = emptyList(),
    val lifeDomainProgress: List<LifeDomainProgress> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalConversations: Int = 0,
    val totalMemories: Int = 0,
    val totalInsights: Int = 0,
    val totalAssessmentsCompleted: Int = 0,
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = preferredName ?: name

    fun getLifeDomainScore(domain: LifeDomain): Float {
        return lifeDomainProgress.find { it.domain == domain }?.score ?: 0f
    }
}

@Entity(tableName = "universal_context")
data class UniversalContext(
    @PrimaryKey
    val id: Long = 1,
    val summary: String = "",
    val personalitySnapshot: String = "",
    val coreIdentityPoints: List<String> = emptyList(),
    val currentGoals: List<String> = emptyList(),
    val currentChallenges: List<String> = emptyList(),
    val preferences: Map<String, String> = emptyMap(),
    val communicationStyle: String = "",
    val emotionalPatterns: String = "",
    val cognitiveStyle: String = "",
    val relationshipContext: String = "",
    val careerContext: String = "",
    val healthContext: String = "",
    val customSections: Map<String, String> = emptyMap(),
    val version: Int = 1,
    val lastGeneratedAt: Long = System.currentTimeMillis(),
    val lastEditedAt: Long? = null,
    val isUserEdited: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toPlainText(): String = buildString {
        appendLine("=== PersonAlly Universal Context ===")
        appendLine()
        if (summary.isNotBlank()) {
            appendLine("Summary:")
            appendLine(summary)
            appendLine()
        }
        if (coreIdentityPoints.isNotEmpty()) {
            appendLine("Core Identity:")
            coreIdentityPoints.forEach { appendLine("- $it") }
            appendLine()
        }
        if (personalitySnapshot.isNotBlank()) {
            appendLine("Personality:")
            appendLine(personalitySnapshot)
            appendLine()
        }
        if (currentGoals.isNotEmpty()) {
            appendLine("Current Goals:")
            currentGoals.forEach { appendLine("- $it") }
            appendLine()
        }
        if (communicationStyle.isNotBlank()) {
            appendLine("Communication Style:")
            appendLine(communicationStyle)
            appendLine()
        }
        customSections.forEach { (key, value) ->
            appendLine("$key:")
            appendLine(value)
            appendLine()
        }
    }

    fun toMarkdown(): String = buildString {
        appendLine("# PersonAlly Universal Context")
        appendLine()
        if (summary.isNotBlank()) {
            appendLine("## Summary")
            appendLine(summary)
            appendLine()
        }
        if (coreIdentityPoints.isNotEmpty()) {
            appendLine("## Core Identity")
            coreIdentityPoints.forEach { appendLine("- $it") }
            appendLine()
        }
        if (personalitySnapshot.isNotBlank()) {
            appendLine("## Personality")
            appendLine(personalitySnapshot)
            appendLine()
        }
        if (currentGoals.isNotEmpty()) {
            appendLine("## Current Goals")
            currentGoals.forEach { appendLine("- $it") }
            appendLine()
        }
        if (currentChallenges.isNotEmpty()) {
            appendLine("## Current Challenges")
            currentChallenges.forEach { appendLine("- $it") }
            appendLine()
        }
        if (communicationStyle.isNotBlank()) {
            appendLine("## Communication Style")
            appendLine(communicationStyle)
            appendLine()
        }
        customSections.forEach { (key, value) ->
            appendLine("## $key")
            appendLine(value)
            appendLine()
        }
    }
}

class UserProfileTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromPersonalityTraits(traits: List<PersonalityTrait>): String = gson.toJson(traits)

    @TypeConverter
    fun toPersonalityTraits(value: String): List<PersonalityTrait> {
        val type = object : TypeToken<List<PersonalityTrait>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromValueItems(values: List<ValueItem>): String = gson.toJson(values)

    @TypeConverter
    fun toValueItems(value: String): List<ValueItem> {
        val type = object : TypeToken<List<ValueItem>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromLifeDomainProgress(progress: List<LifeDomainProgress>): String = gson.toJson(progress)

    @TypeConverter
    fun toLifeDomainProgress(value: String): List<LifeDomainProgress> {
        val type = object : TypeToken<List<LifeDomainProgress>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String = gson.toJson(map)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }
}
