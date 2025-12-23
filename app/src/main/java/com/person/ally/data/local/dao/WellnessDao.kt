package com.person.ally.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.person.ally.data.model.DailyCheckin
import com.person.ally.data.model.JournalEntry
import com.person.ally.data.model.JournalEntryType
import com.person.ally.data.model.LifeDomain
import com.person.ally.data.model.MoodEntry
import com.person.ally.data.model.MoodLevel
import com.person.ally.data.model.ScheduleItem
import com.person.ally.data.model.ScheduleItemType
import com.person.ally.data.model.SchedulePriority
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for wellness-related entities:
 * - MoodEntry: Mood tracking
 * - JournalEntry: Personal journaling
 * - ScheduleItem: Tasks, routines, events
 * - DailyCheckin: Daily check-ins
 */
@Dao
interface WellnessDao {

    // ==================== MoodEntry Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntry): Long

    @Update
    suspend fun updateMoodEntry(entry: MoodEntry)

    @Delete
    suspend fun deleteMoodEntry(entry: MoodEntry)

    @Query("SELECT * FROM mood_entries ORDER BY createdAt DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getMoodEntryById(id: Long): MoodEntry?

    @Query("SELECT * FROM mood_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentMoodEntries(limit: Int): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getMoodEntriesInRange(startTime: Long, endTime: Long): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE moodLevel = :level ORDER BY createdAt DESC")
    fun getMoodEntriesByLevel(level: MoodLevel): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC LIMIT 1")
    suspend fun getTodaysMoodEntry(startOfDay: Long, endOfDay: Long): MoodEntry?

    @Query("SELECT AVG(CASE moodLevel WHEN 'VERY_LOW' THEN 1 WHEN 'LOW' THEN 2 WHEN 'NEUTRAL' THEN 3 WHEN 'GOOD' THEN 4 WHEN 'GREAT' THEN 5 WHEN 'AMAZING' THEN 6 END) FROM mood_entries WHERE createdAt >= :startTime AND createdAt <= :endTime")
    suspend fun getAverageMoodInRange(startTime: Long, endTime: Long): Float?

    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getMoodEntryCount(): Int

    // ==================== JournalEntry Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry): Long

    @Update
    suspend fun updateJournalEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getJournalEntryById(id: Long): JournalEntry?

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentJournalEntries(limit: Int): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE entryType = :type ORDER BY createdAt DESC")
    fun getJournalEntriesByType(type: JournalEntryType): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteJournalEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchJournalEntries(query: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getJournalEntriesInRange(startTime: Long, endTime: Long): Flow<List<JournalEntry>>

    @Query("UPDATE journal_entries SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateJournalFavoriteStatus(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun getJournalEntryCount(): Int

    @Query("SELECT SUM(wordCount) FROM journal_entries")
    suspend fun getTotalWordCount(): Int?

    // ==================== ScheduleItem Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItem): Long

    @Update
    suspend fun updateScheduleItem(item: ScheduleItem)

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItem)

    @Query("SELECT * FROM schedule_items ORDER BY scheduledAt ASC")
    fun getAllScheduleItems(): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE id = :id")
    suspend fun getScheduleItemById(id: Long): ScheduleItem?

    @Query("SELECT * FROM schedule_items WHERE scheduledAt >= :startTime AND scheduledAt <= :endTime ORDER BY scheduledAt ASC")
    fun getScheduleItemsInRange(startTime: Long, endTime: Long): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE scheduledAt >= :today AND isCompleted = 0 ORDER BY scheduledAt ASC LIMIT :limit")
    fun getUpcomingScheduleItems(today: Long, limit: Int): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE itemType = :type ORDER BY scheduledAt ASC")
    fun getScheduleItemsByType(type: ScheduleItemType): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE priority = :priority AND isCompleted = 0 ORDER BY scheduledAt ASC")
    fun getScheduleItemsByPriority(priority: SchedulePriority): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE isCompleted = 0 AND scheduledAt < :now ORDER BY scheduledAt ASC")
    fun getOverdueScheduleItems(now: Long): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE isCompleted = 1 ORDER BY completedAt DESC LIMIT :limit")
    fun getCompletedScheduleItems(limit: Int): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE linkedGoalId = :goalId ORDER BY scheduledAt ASC")
    fun getScheduleItemsForGoal(goalId: Long): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items WHERE linkedHabitId = :habitId ORDER BY scheduledAt ASC")
    fun getScheduleItemsForHabit(habitId: Long): Flow<List<ScheduleItem>>

    @Query("UPDATE schedule_items SET isCompleted = :isCompleted, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateScheduleItemCompletion(id: Long, isCompleted: Boolean, completedAt: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM schedule_items WHERE isCompleted = 0")
    suspend fun getPendingScheduleItemCount(): Int

    @Query("SELECT COUNT(*) FROM schedule_items WHERE isCompleted = 1 AND completedAt >= :startTime AND completedAt <= :endTime")
    suspend fun getCompletedScheduleItemCountInRange(startTime: Long, endTime: Long): Int

    // ==================== DailyCheckin Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCheckin(checkin: DailyCheckin): Long

    @Update
    suspend fun updateDailyCheckin(checkin: DailyCheckin)

    @Delete
    suspend fun deleteDailyCheckin(checkin: DailyCheckin)

    @Query("SELECT * FROM daily_checkins ORDER BY date DESC")
    fun getAllDailyCheckins(): Flow<List<DailyCheckin>>

    @Query("SELECT * FROM daily_checkins WHERE id = :id")
    suspend fun getDailyCheckinById(id: Long): DailyCheckin?

    @Query("SELECT * FROM daily_checkins WHERE date >= :startOfDay AND date < :endOfDay LIMIT 1")
    suspend fun getDailyCheckinForDate(startOfDay: Long, endOfDay: Long): DailyCheckin?

    @Query("SELECT * FROM daily_checkins WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getDailyCheckinsInRange(startTime: Long, endTime: Long): Flow<List<DailyCheckin>>

    @Query("SELECT * FROM daily_checkins ORDER BY date DESC LIMIT :limit")
    fun getRecentDailyCheckins(limit: Int): Flow<List<DailyCheckin>>

    @Query("SELECT COUNT(*) FROM daily_checkins WHERE isMorningComplete = 1 OR isEveningComplete = 1")
    suspend fun getCompletedCheckinCount(): Int

    @Query("SELECT AVG(overallRating) FROM daily_checkins WHERE overallRating IS NOT NULL AND date >= :startTime AND date <= :endTime")
    suspend fun getAverageRatingInRange(startTime: Long, endTime: Long): Float?

    @Query("SELECT AVG(sleepHours) FROM daily_checkins WHERE sleepHours IS NOT NULL AND date >= :startTime AND date <= :endTime")
    suspend fun getAverageSleepInRange(startTime: Long, endTime: Long): Float?

    @Query("SELECT SUM(exerciseMinutes) FROM daily_checkins WHERE date >= :startTime AND date <= :endTime")
    suspend fun getTotalExerciseMinutesInRange(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(meditationMinutes) FROM daily_checkins WHERE date >= :startTime AND date <= :endTime")
    suspend fun getTotalMeditationMinutesInRange(startTime: Long, endTime: Long): Int?

    // ==================== Streak Calculations ====================

    @Query("""
        SELECT COUNT(DISTINCT date(date/1000, 'unixepoch', 'localtime'))
        FROM mood_entries
        WHERE date(date/1000, 'unixepoch', 'localtime') >= date(:startDate/1000, 'unixepoch', 'localtime')
    """)
    suspend fun getMoodLoggingStreak(startDate: Long): Int

    @Query("""
        SELECT COUNT(DISTINCT date(createdAt/1000, 'unixepoch', 'localtime'))
        FROM journal_entries
        WHERE date(createdAt/1000, 'unixepoch', 'localtime') >= date(:startDate/1000, 'unixepoch', 'localtime')
    """)
    suspend fun getJournalingStreak(startDate: Long): Int
}
