package com.person.ally.data.repository

import com.person.ally.data.local.dao.WellnessDao
import com.person.ally.data.model.DailyCheckin
import com.person.ally.data.model.JournalEntry
import com.person.ally.data.model.JournalEntryType
import com.person.ally.data.model.LifeDomain
import com.person.ally.data.model.MoodEntry
import com.person.ally.data.model.MoodLevel
import com.person.ally.data.model.ScheduleItem
import com.person.ally.data.model.ScheduleItemType
import com.person.ally.data.model.SchedulePriority
import com.person.ally.data.model.ScheduleRecurrence
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository for wellness-related data management.
 * Handles mood tracking, journaling, scheduling, and daily check-ins.
 */
class WellnessRepository(private val wellnessDao: WellnessDao) {

    // ==================== Mood Entry Operations ====================

    val allMoodEntries: Flow<List<MoodEntry>> = wellnessDao.getAllMoodEntries()

    fun getRecentMoodEntries(limit: Int = 10): Flow<List<MoodEntry>> =
        wellnessDao.getRecentMoodEntries(limit)

    fun getMoodEntriesInRange(startTime: Long, endTime: Long): Flow<List<MoodEntry>> =
        wellnessDao.getMoodEntriesInRange(startTime, endTime)

    fun getMoodEntriesByLevel(level: MoodLevel): Flow<List<MoodEntry>> =
        wellnessDao.getMoodEntriesByLevel(level)

    suspend fun getMoodEntryById(id: Long): MoodEntry? =
        wellnessDao.getMoodEntryById(id)

    suspend fun getTodaysMoodEntry(): MoodEntry? {
        val (startOfDay, endOfDay) = getTodayRange()
        return wellnessDao.getTodaysMoodEntry(startOfDay, endOfDay)
    }

    suspend fun logMood(
        moodLevel: MoodLevel,
        energyLevel: Int = 5,
        stressLevel: Int = 5,
        note: String? = null,
        activities: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        relatedDomains: List<LifeDomain> = emptyList()
    ): Long {
        val entry = MoodEntry(
            moodLevel = moodLevel,
            energyLevel = energyLevel.coerceIn(1, 10),
            stressLevel = stressLevel.coerceIn(1, 10),
            note = note,
            activities = activities,
            tags = tags,
            relatedDomains = relatedDomains
        )
        return wellnessDao.insertMoodEntry(entry)
    }

    suspend fun updateMoodEntry(entry: MoodEntry) {
        wellnessDao.updateMoodEntry(entry)
    }

    suspend fun deleteMoodEntry(entry: MoodEntry) {
        wellnessDao.deleteMoodEntry(entry)
    }

    suspend fun getMoodEntryCount(): Int = wellnessDao.getMoodEntryCount()

    suspend fun getAverageMoodThisWeek(): Float? {
        val (startOfWeek, endOfWeek) = getWeekRange()
        return wellnessDao.getAverageMoodInRange(startOfWeek, endOfWeek)
    }

    suspend fun getAverageMoodThisMonth(): Float? {
        val (startOfMonth, endOfMonth) = getMonthRange()
        return wellnessDao.getAverageMoodInRange(startOfMonth, endOfMonth)
    }

    // ==================== Journal Entry Operations ====================

    val allJournalEntries: Flow<List<JournalEntry>> = wellnessDao.getAllJournalEntries()

    fun getRecentJournalEntries(limit: Int = 10): Flow<List<JournalEntry>> =
        wellnessDao.getRecentJournalEntries(limit)

    fun getJournalEntriesByType(type: JournalEntryType): Flow<List<JournalEntry>> =
        wellnessDao.getJournalEntriesByType(type)

    fun getFavoriteJournalEntries(): Flow<List<JournalEntry>> =
        wellnessDao.getFavoriteJournalEntries()

    fun searchJournalEntries(query: String): Flow<List<JournalEntry>> =
        wellnessDao.searchJournalEntries(query)

    fun getJournalEntriesInRange(startTime: Long, endTime: Long): Flow<List<JournalEntry>> =
        wellnessDao.getJournalEntriesInRange(startTime, endTime)

    suspend fun getJournalEntryById(id: Long): JournalEntry? =
        wellnessDao.getJournalEntryById(id)

    suspend fun createJournalEntry(
        title: String,
        content: String,
        entryType: JournalEntryType = JournalEntryType.FREE_WRITE,
        mood: MoodLevel? = null,
        tags: List<String> = emptyList(),
        relatedDomains: List<LifeDomain> = emptyList(),
        promptUsed: String? = null,
        isPrivate: Boolean = false
    ): Long {
        val wordCount = content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val readingTimeMinutes = (wordCount / 200).coerceAtLeast(1) // Average reading speed

        val entry = JournalEntry(
            title = title,
            content = content,
            entryType = entryType,
            mood = mood,
            tags = tags,
            relatedDomains = relatedDomains,
            promptUsed = promptUsed,
            isPrivate = isPrivate,
            wordCount = wordCount,
            readingTimeMinutes = readingTimeMinutes
        )
        return wellnessDao.insertJournalEntry(entry)
    }

    suspend fun updateJournalEntry(entry: JournalEntry) {
        val wordCount = entry.content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val readingTimeMinutes = (wordCount / 200).coerceAtLeast(1)

        wellnessDao.updateJournalEntry(
            entry.copy(
                wordCount = wordCount,
                readingTimeMinutes = readingTimeMinutes,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteJournalEntry(entry: JournalEntry) {
        wellnessDao.deleteJournalEntry(entry)
    }

    suspend fun toggleJournalFavorite(id: Long, isFavorite: Boolean) {
        wellnessDao.updateJournalFavoriteStatus(id, isFavorite)
    }

    suspend fun getJournalEntryCount(): Int = wellnessDao.getJournalEntryCount()

    suspend fun getTotalWordCount(): Int = wellnessDao.getTotalWordCount() ?: 0

    // ==================== Schedule Item Operations ====================

    val allScheduleItems: Flow<List<ScheduleItem>> = wellnessDao.getAllScheduleItems()

    fun getUpcomingScheduleItems(limit: Int = 10): Flow<List<ScheduleItem>> =
        wellnessDao.getUpcomingScheduleItems(System.currentTimeMillis(), limit)

    fun getScheduleItemsInRange(startTime: Long, endTime: Long): Flow<List<ScheduleItem>> =
        wellnessDao.getScheduleItemsInRange(startTime, endTime)

    fun getTodaysScheduleItems(): Flow<List<ScheduleItem>> {
        val (startOfDay, endOfDay) = getTodayRange()
        return wellnessDao.getScheduleItemsInRange(startOfDay, endOfDay)
    }

    fun getScheduleItemsByType(type: ScheduleItemType): Flow<List<ScheduleItem>> =
        wellnessDao.getScheduleItemsByType(type)

    fun getScheduleItemsByPriority(priority: SchedulePriority): Flow<List<ScheduleItem>> =
        wellnessDao.getScheduleItemsByPriority(priority)

    fun getOverdueScheduleItems(): Flow<List<ScheduleItem>> =
        wellnessDao.getOverdueScheduleItems(System.currentTimeMillis())

    fun getCompletedScheduleItems(limit: Int = 20): Flow<List<ScheduleItem>> =
        wellnessDao.getCompletedScheduleItems(limit)

    fun getScheduleItemsForGoal(goalId: Long): Flow<List<ScheduleItem>> =
        wellnessDao.getScheduleItemsForGoal(goalId)

    fun getScheduleItemsForHabit(habitId: Long): Flow<List<ScheduleItem>> =
        wellnessDao.getScheduleItemsForHabit(habitId)

    suspend fun getScheduleItemById(id: Long): ScheduleItem? =
        wellnessDao.getScheduleItemById(id)

    suspend fun createScheduleItem(
        title: String,
        description: String = "",
        itemType: ScheduleItemType,
        domain: LifeDomain? = null,
        priority: SchedulePriority = SchedulePriority.MEDIUM,
        scheduledAt: Long,
        durationMinutes: Int = 30,
        recurrence: ScheduleRecurrence = ScheduleRecurrence.NONE,
        recurrenceDays: List<Int> = emptyList(),
        reminderMinutesBefore: Int? = null,
        isAllDay: Boolean = false,
        location: String? = null,
        notes: String? = null,
        tags: List<String> = emptyList(),
        linkedGoalId: Long? = null,
        linkedHabitId: Long? = null
    ): Long {
        val item = ScheduleItem(
            title = title,
            description = description,
            itemType = itemType,
            domain = domain,
            priority = priority,
            scheduledAt = scheduledAt,
            durationMinutes = durationMinutes,
            recurrence = recurrence,
            recurrenceDays = recurrenceDays,
            reminderMinutesBefore = reminderMinutesBefore,
            isAllDay = isAllDay,
            location = location,
            notes = notes,
            tags = tags,
            linkedGoalId = linkedGoalId,
            linkedHabitId = linkedHabitId
        )
        return wellnessDao.insertScheduleItem(item)
    }

    suspend fun updateScheduleItem(item: ScheduleItem) {
        wellnessDao.updateScheduleItem(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteScheduleItem(item: ScheduleItem) {
        wellnessDao.deleteScheduleItem(item)
    }

    suspend fun completeScheduleItem(id: Long) {
        wellnessDao.updateScheduleItemCompletion(
            id = id,
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
    }

    suspend fun uncompleteScheduleItem(id: Long) {
        wellnessDao.updateScheduleItemCompletion(
            id = id,
            isCompleted = false,
            completedAt = null
        )
    }

    suspend fun getPendingScheduleItemCount(): Int = wellnessDao.getPendingScheduleItemCount()

    suspend fun getCompletedScheduleItemCountToday(): Int {
        val (startOfDay, endOfDay) = getTodayRange()
        return wellnessDao.getCompletedScheduleItemCountInRange(startOfDay, endOfDay)
    }

    // ==================== Daily Check-in Operations ====================

    val allDailyCheckins: Flow<List<DailyCheckin>> = wellnessDao.getAllDailyCheckins()

    fun getRecentDailyCheckins(limit: Int = 7): Flow<List<DailyCheckin>> =
        wellnessDao.getRecentDailyCheckins(limit)

    fun getDailyCheckinsInRange(startTime: Long, endTime: Long): Flow<List<DailyCheckin>> =
        wellnessDao.getDailyCheckinsInRange(startTime, endTime)

    suspend fun getDailyCheckinById(id: Long): DailyCheckin? =
        wellnessDao.getDailyCheckinById(id)

    suspend fun getTodaysCheckin(): DailyCheckin? {
        val (startOfDay, endOfDay) = getTodayRange()
        return wellnessDao.getDailyCheckinForDate(startOfDay, endOfDay)
    }

    suspend fun createOrUpdateMorningCheckin(
        morningMood: MoodLevel,
        sleepQuality: Int? = null,
        sleepHours: Float? = null,
        energyLevel: Int? = null
    ): Long {
        val (startOfDay, _) = getTodayRange()
        val existing = getTodaysCheckin()

        return if (existing != null) {
            wellnessDao.updateDailyCheckin(
                existing.copy(
                    morningMood = morningMood,
                    sleepQuality = sleepQuality,
                    sleepHours = sleepHours,
                    energyLevel = energyLevel,
                    isMorningComplete = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
            existing.id
        } else {
            val checkin = DailyCheckin(
                date = startOfDay,
                morningMood = morningMood,
                sleepQuality = sleepQuality,
                sleepHours = sleepHours,
                energyLevel = energyLevel,
                isMorningComplete = true
            )
            wellnessDao.insertDailyCheckin(checkin)
        }
    }

    suspend fun createOrUpdateEveningCheckin(
        eveningMood: MoodLevel,
        stressLevel: Int? = null,
        productivityLevel: Int? = null,
        exerciseMinutes: Int = 0,
        waterIntake: Int = 0,
        meditationMinutes: Int = 0,
        gratitudeItems: List<String> = emptyList(),
        highlights: List<String> = emptyList(),
        challenges: List<String> = emptyList(),
        tomorrowGoals: List<String> = emptyList(),
        overallRating: Int? = null,
        notes: String? = null
    ): Long {
        val (startOfDay, _) = getTodayRange()
        val existing = getTodaysCheckin()

        return if (existing != null) {
            wellnessDao.updateDailyCheckin(
                existing.copy(
                    eveningMood = eveningMood,
                    stressLevel = stressLevel,
                    productivityLevel = productivityLevel,
                    exerciseMinutes = exerciseMinutes,
                    waterIntake = waterIntake,
                    meditationMinutes = meditationMinutes,
                    gratitudeItems = gratitudeItems,
                    highlights = highlights,
                    challenges = challenges,
                    tomorrowGoals = tomorrowGoals,
                    overallRating = overallRating,
                    notes = notes,
                    isEveningComplete = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
            existing.id
        } else {
            val checkin = DailyCheckin(
                date = startOfDay,
                eveningMood = eveningMood,
                stressLevel = stressLevel,
                productivityLevel = productivityLevel,
                exerciseMinutes = exerciseMinutes,
                waterIntake = waterIntake,
                meditationMinutes = meditationMinutes,
                gratitudeItems = gratitudeItems,
                highlights = highlights,
                challenges = challenges,
                tomorrowGoals = tomorrowGoals,
                overallRating = overallRating,
                notes = notes,
                isEveningComplete = true
            )
            wellnessDao.insertDailyCheckin(checkin)
        }
    }

    suspend fun updateDailyCheckin(checkin: DailyCheckin) {
        wellnessDao.updateDailyCheckin(checkin.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteDailyCheckin(checkin: DailyCheckin) {
        wellnessDao.deleteDailyCheckin(checkin)
    }

    suspend fun getCompletedCheckinCount(): Int = wellnessDao.getCompletedCheckinCount()

    suspend fun getAverageRatingThisWeek(): Float? {
        val (startOfWeek, endOfWeek) = getWeekRange()
        return wellnessDao.getAverageRatingInRange(startOfWeek, endOfWeek)
    }

    suspend fun getAverageSleepThisWeek(): Float? {
        val (startOfWeek, endOfWeek) = getWeekRange()
        return wellnessDao.getAverageSleepInRange(startOfWeek, endOfWeek)
    }

    suspend fun getTotalExerciseMinutesThisWeek(): Int {
        val (startOfWeek, endOfWeek) = getWeekRange()
        return wellnessDao.getTotalExerciseMinutesInRange(startOfWeek, endOfWeek) ?: 0
    }

    suspend fun getTotalMeditationMinutesThisWeek(): Int {
        val (startOfWeek, endOfWeek) = getWeekRange()
        return wellnessDao.getTotalMeditationMinutesInRange(startOfWeek, endOfWeek) ?: 0
    }

    // ==================== Streak Operations ====================

    suspend fun getMoodLoggingStreak(): Int {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return wellnessDao.getMoodLoggingStreak(thirtyDaysAgo)
    }

    suspend fun getJournalingStreak(): Int {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return wellnessDao.getJournalingStreak(thirtyDaysAgo)
    }

    // ==================== Helper Functions ====================

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis
        return Pair(startOfDay, endOfDay)
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        val startOfWeek = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = calendar.timeInMillis
        return Pair(startOfWeek, endOfWeek)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startOfMonth = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis
        return Pair(startOfMonth, endOfMonth)
    }
}
