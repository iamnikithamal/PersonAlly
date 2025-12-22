package com.person.ally.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class InsightType {
    PATTERN,
    DISCOVERY,
    GROWTH,
    REFLECTION,
    RECOMMENDATION,
    MILESTONE
}

enum class InsightSource {
    CONVERSATION,
    ASSESSMENT,
    BEHAVIOR,
    SYSTEM_GENERATED,
    USER_CREATED
}

@Entity(tableName = "insights")
data class Insight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val type: InsightType,
    val source: InsightSource,
    val relatedDomains: List<LifeDomain> = emptyList(),
    val relatedMemoryIds: List<Long> = emptyList(),
    val confidence: Float = 1.0f,
    val isActionable: Boolean = false,
    val actionSuggestion: String? = null,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val isDismissed: Boolean = false,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getTypeIcon(): String = when (type) {
        InsightType.PATTERN -> "AutoAwesome"
        InsightType.DISCOVERY -> "Lightbulb"
        InsightType.GROWTH -> "TrendingUp"
        InsightType.REFLECTION -> "Psychology"
        InsightType.RECOMMENDATION -> "Recommend"
        InsightType.MILESTONE -> "EmojiEvents"
    }

    fun getTypeLabel(): String = when (type) {
        InsightType.PATTERN -> "Pattern"
        InsightType.DISCOVERY -> "Discovery"
        InsightType.GROWTH -> "Growth"
        InsightType.REFLECTION -> "Reflection"
        InsightType.RECOMMENDATION -> "Recommendation"
        InsightType.MILESTONE -> "Milestone"
    }
}

@Entity(tableName = "daily_briefings")
data class DailyBriefing(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val timeOfDay: TimeOfDay,
    val greeting: String,
    val mainMessage: String,
    val highlights: List<String> = emptyList(),
    val suggestedActions: List<String> = emptyList(),
    val mood: String? = null,
    val weatherContext: String? = null,
    val isViewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TimeOfDay {
    MORNING,
    MIDDAY,
    EVENING
}

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val domain: LifeDomain,
    val targetDate: Long? = null,
    val progress: Float = 0f,
    val milestones: List<GoalMilestone> = emptyList(),
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderFrequency: String? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class GoalMilestone(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val domain: LifeDomain,
    val frequency: HabitFrequency,
    val targetDays: List<Int> = emptyList(),
    val reminderTime: String? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val isActive: Boolean = true,
    val lastCompletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    SPECIFIC_DAYS,
    INTERVAL
}

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val note: String? = null
)

class InsightTypeConverters {
    private val gson = com.google.gson.Gson()

    @TypeConverter
    fun fromInsightType(type: InsightType): String = type.name

    @TypeConverter
    fun toInsightType(value: String): InsightType = InsightType.valueOf(value)

    @TypeConverter
    fun fromInsightSource(source: InsightSource): String = source.name

    @TypeConverter
    fun toInsightSource(value: String): InsightSource = InsightSource.valueOf(value)

    @TypeConverter
    fun fromTimeOfDay(timeOfDay: TimeOfDay): String = timeOfDay.name

    @TypeConverter
    fun toTimeOfDay(value: String): TimeOfDay = TimeOfDay.valueOf(value)

    @TypeConverter
    fun fromLifeDomainList(domains: List<LifeDomain>): String = gson.toJson(domains.map { it.name })

    @TypeConverter
    fun toLifeDomainList(value: String): List<LifeDomain> {
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        val names: List<String> = gson.fromJson(value, type) ?: emptyList()
        return names.mapNotNull { runCatching { LifeDomain.valueOf(it) }.getOrNull() }
    }

    @TypeConverter
    fun fromLifeDomain(domain: LifeDomain): String = domain.name

    @TypeConverter
    fun toLifeDomain(value: String): LifeDomain = LifeDomain.valueOf(value)

    @TypeConverter
    fun fromHabitFrequency(frequency: HabitFrequency): String = frequency.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = HabitFrequency.valueOf(value)

    @TypeConverter
    fun fromGoalMilestones(milestones: List<GoalMilestone>): String = gson.toJson(milestones)

    @TypeConverter
    fun toGoalMilestones(value: String): List<GoalMilestone> {
        val type = object : com.google.gson.reflect.TypeToken<List<GoalMilestone>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromIntList(list: List<Int>): String = gson.toJson(list)

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : com.google.gson.reflect.TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
