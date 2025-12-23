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
    val summary: String
        get() = if (content.length > 150) content.take(147) + "..." else content

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
) {
    val keyInsights: List<String>
        get() = highlights.take(3)
}

enum class TimeOfDay {
    MORNING,
    MIDDAY,
    EVENING
}

enum class GoalStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    PAUSED,
    ABANDONED
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
) {
    val status: GoalStatus
        get() = when {
            isCompleted -> GoalStatus.COMPLETED
            isPaused -> GoalStatus.PAUSED
            progress > 0f -> GoalStatus.IN_PROGRESS
            else -> GoalStatus.NOT_STARTED
        }
}

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

/**
 * Mood entry for tracking emotional states throughout the day
 */
enum class MoodLevel(val value: Int, val label: String, val emoji: String) {
    VERY_LOW(1, "Very Low", "😢"),
    LOW(2, "Low", "😔"),
    NEUTRAL(3, "Okay", "😐"),
    GOOD(4, "Good", "🙂"),
    GREAT(5, "Great", "😊"),
    AMAZING(6, "Amazing", "🤩")
}

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val moodLevel: MoodLevel,
    val energyLevel: Int = 5, // 1-10
    val stressLevel: Int = 5, // 1-10
    val note: String? = null,
    val activities: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val location: String? = null,
    val weather: String? = null,
    val sleepHours: Float? = null,
    val relatedDomains: List<LifeDomain> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val date: Long get() = createdAt

    fun getMoodColor(): String = when (moodLevel) {
        MoodLevel.VERY_LOW -> "#EF5350"
        MoodLevel.LOW -> "#FF7043"
        MoodLevel.NEUTRAL -> "#FFA726"
        MoodLevel.GOOD -> "#66BB6A"
        MoodLevel.GREAT -> "#26A69A"
        MoodLevel.AMAZING -> "#42A5F5"
    }
}

/**
 * Journal entry for personal reflections, thoughts, and experiences
 */
enum class JournalEntryType {
    REFLECTION,
    GRATITUDE,
    ACHIEVEMENT,
    CHALLENGE,
    LEARNING,
    IDEA,
    DREAM,
    FREE_WRITE
}

enum class JournalPromptCategory {
    MORNING_REFLECTION,
    EVENING_REVIEW,
    GRATITUDE,
    SELF_DISCOVERY,
    GOAL_PROGRESS,
    RELATIONSHIPS,
    CREATIVITY,
    CHALLENGES
}

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val entryType: JournalEntryType = JournalEntryType.FREE_WRITE,
    val mood: MoodLevel? = null,
    val tags: List<String> = emptyList(),
    val relatedDomains: List<LifeDomain> = emptyList(),
    val promptUsed: String? = null,
    val isPrivate: Boolean = false,
    val isFavorite: Boolean = false,
    val wordCount: Int = 0,
    val readingTimeMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val preview: String
        get() = if (content.length > 100) content.take(97) + "..." else content

    fun getTypeIcon(): String = when (entryType) {
        JournalEntryType.REFLECTION -> "Psychology"
        JournalEntryType.GRATITUDE -> "Favorite"
        JournalEntryType.ACHIEVEMENT -> "EmojiEvents"
        JournalEntryType.CHALLENGE -> "Warning"
        JournalEntryType.LEARNING -> "School"
        JournalEntryType.IDEA -> "Lightbulb"
        JournalEntryType.DREAM -> "NightsStay"
        JournalEntryType.FREE_WRITE -> "Edit"
    }
}

/**
 * Schedule item for routines, tasks, and events
 */
enum class ScheduleItemType {
    ROUTINE,
    TASK,
    EVENT,
    REMINDER,
    APPOINTMENT,
    SELF_CARE
}

enum class ScheduleRecurrence {
    NONE,
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    CUSTOM
}

enum class SchedulePriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val itemType: ScheduleItemType,
    val domain: LifeDomain? = null,
    val priority: SchedulePriority = SchedulePriority.MEDIUM,
    val scheduledAt: Long,
    val durationMinutes: Int = 30,
    val recurrence: ScheduleRecurrence = ScheduleRecurrence.NONE,
    val recurrenceDays: List<Int> = emptyList(), // Day of week (1-7)
    val reminderMinutesBefore: Int? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isAllDay: Boolean = false,
    val location: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val linkedGoalId: Long? = null,
    val linkedHabitId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean
        get() = !isCompleted && scheduledAt < System.currentTimeMillis()

    fun getPriorityColor(): String = when (priority) {
        SchedulePriority.LOW -> "#78909C"
        SchedulePriority.MEDIUM -> "#42A5F5"
        SchedulePriority.HIGH -> "#FFA726"
        SchedulePriority.URGENT -> "#EF5350"
    }

    fun getTypeIcon(): String = when (itemType) {
        ScheduleItemType.ROUTINE -> "Loop"
        ScheduleItemType.TASK -> "Task"
        ScheduleItemType.EVENT -> "Event"
        ScheduleItemType.REMINDER -> "NotificationsActive"
        ScheduleItemType.APPOINTMENT -> "CalendarMonth"
        ScheduleItemType.SELF_CARE -> "Spa"
    }
}

/**
 * Daily check-in for comprehensive daily tracking
 */
@Entity(tableName = "daily_checkins")
data class DailyCheckin(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val morningMood: MoodLevel? = null,
    val eveningMood: MoodLevel? = null,
    val sleepQuality: Int? = null, // 1-10
    val sleepHours: Float? = null,
    val energyLevel: Int? = null, // 1-10
    val stressLevel: Int? = null, // 1-10
    val productivityLevel: Int? = null, // 1-10
    val exerciseMinutes: Int = 0,
    val waterIntake: Int = 0, // glasses
    val meditationMinutes: Int = 0,
    val gratitudeItems: List<String> = emptyList(),
    val highlights: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    val tomorrowGoals: List<String> = emptyList(),
    val overallRating: Int? = null, // 1-10
    val notes: String? = null,
    val isMorningComplete: Boolean = false,
    val isEveningComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
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
    fun fromNullableLifeDomain(domain: LifeDomain?): String? = domain?.name

    @TypeConverter
    fun toNullableLifeDomain(value: String?): LifeDomain? = value?.let { LifeDomain.valueOf(it) }

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

    // MoodEntry converters
    @TypeConverter
    fun fromMoodLevel(level: MoodLevel): String = level.name

    @TypeConverter
    fun toMoodLevel(value: String): MoodLevel = MoodLevel.valueOf(value)

    @TypeConverter
    fun fromNullableMoodLevel(level: MoodLevel?): String? = level?.name

    @TypeConverter
    fun toNullableMoodLevel(value: String?): MoodLevel? = value?.let { MoodLevel.valueOf(it) }

    // JournalEntry converters
    @TypeConverter
    fun fromJournalEntryType(type: JournalEntryType): String = type.name

    @TypeConverter
    fun toJournalEntryType(value: String): JournalEntryType = JournalEntryType.valueOf(value)

    // ScheduleItem converters
    @TypeConverter
    fun fromScheduleItemType(type: ScheduleItemType): String = type.name

    @TypeConverter
    fun toScheduleItemType(value: String): ScheduleItemType = ScheduleItemType.valueOf(value)

    @TypeConverter
    fun fromScheduleRecurrence(recurrence: ScheduleRecurrence): String = recurrence.name

    @TypeConverter
    fun toScheduleRecurrence(value: String): ScheduleRecurrence = ScheduleRecurrence.valueOf(value)

    @TypeConverter
    fun fromSchedulePriority(priority: SchedulePriority): String = priority.name

    @TypeConverter
    fun toSchedulePriority(value: String): SchedulePriority = SchedulePriority.valueOf(value)
}
