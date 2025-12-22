package com.person.ally.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.person.ally.data.model.DailyBriefing
import com.person.ally.data.model.Goal
import com.person.ally.data.model.Habit
import com.person.ally.data.model.HabitCompletion
import com.person.ally.data.model.Insight
import com.person.ally.data.model.InsightType
import com.person.ally.data.model.LifeDomain
import com.person.ally.data.model.TimeOfDay
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Query("SELECT * FROM insights WHERE isDismissed = 0 ORDER BY createdAt DESC")
    fun getAllInsights(): Flow<List<Insight>>

    @Query("SELECT * FROM insights WHERE id = :id")
    suspend fun getInsightById(id: Long): Insight?

    @Query("SELECT * FROM insights WHERE type = :type AND isDismissed = 0 ORDER BY createdAt DESC")
    fun getInsightsByType(type: InsightType): Flow<List<Insight>>

    @Query("SELECT * FROM insights WHERE isRead = 0 AND isDismissed = 0 ORDER BY createdAt DESC")
    fun getUnreadInsights(): Flow<List<Insight>>

    @Query("SELECT * FROM insights WHERE isBookmarked = 1 ORDER BY createdAt DESC")
    fun getBookmarkedInsights(): Flow<List<Insight>>

    @Query("SELECT * FROM insights WHERE isDismissed = 0 ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentInsights(limit: Int = 10): Flow<List<Insight>>

    @Query("SELECT COUNT(*) FROM insights WHERE isRead = 0 AND isDismissed = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: Insight): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsights(insights: List<Insight>): List<Long>

    @Update
    suspend fun updateInsight(insight: Insight)

    @Query("UPDATE insights SET isRead = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markAsRead(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE insights SET isRead = 1, updatedAt = :timestamp WHERE isRead = 0")
    suspend fun markAllAsRead(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE insights SET isBookmarked = :bookmarked, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Long, bookmarked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE insights SET isDismissed = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun dismissInsight(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteInsight(insight: Insight)

    @Query("DELETE FROM insights WHERE id = :id")
    suspend fun deleteInsightById(id: Long)

    @Query("DELETE FROM insights WHERE isDismissed = 1")
    suspend fun deleteDismissedInsights()

    @Query("SELECT * FROM daily_briefings WHERE date = :date AND timeOfDay = :timeOfDay LIMIT 1")
    suspend fun getDailyBriefing(date: Long, timeOfDay: TimeOfDay): DailyBriefing?

    @Query("SELECT * FROM daily_briefings ORDER BY date DESC, timeOfDay DESC LIMIT 1")
    fun getLatestBriefing(): Flow<DailyBriefing?>

    @Query("SELECT * FROM daily_briefings WHERE date = :date ORDER BY timeOfDay ASC")
    fun getBriefingsForDate(date: Long): Flow<List<DailyBriefing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyBriefing(briefing: DailyBriefing): Long

    @Query("UPDATE daily_briefings SET isViewed = 1 WHERE id = :id")
    suspend fun markBriefingViewed(id: Long)

    @Query("DELETE FROM daily_briefings WHERE date < :beforeDate")
    suspend fun deleteOldBriefings(beforeDate: Long)

    @Query("SELECT * FROM goals WHERE isCompleted = 0 AND isPaused = 0 ORDER BY targetDate ASC NULLS LAST")
    fun getActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE domain = :domain AND isCompleted = 0 ORDER BY progress DESC")
    fun getGoalsByDomain(domain: LifeDomain): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("UPDATE goals SET progress = :progress, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, progress: Float, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE goals SET isCompleted = 1, completedAt = :timestamp, progress = 1.0, updatedAt = :timestamp WHERE id = :id")
    suspend fun completeGoal(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY title ASC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE domain = :domain AND isActive = 1 ORDER BY title ASC")
    fun getHabitsByDomain(domain: LifeDomain): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("""
        UPDATE habits
        SET currentStreak = currentStreak + 1,
            longestStreak = CASE WHEN currentStreak + 1 > longestStreak THEN currentStreak + 1 ELSE longestStreak END,
            totalCompletions = totalCompletions + 1,
            lastCompletedAt = :timestamp,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun recordHabitCompletion(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE habits SET currentStreak = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun resetHabitStreak(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedAt DESC")
    fun getHabitCompletions(habitId: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedAt >= :startDate AND completedAt <= :endDate")
    suspend fun getHabitCompletionsInRange(habitId: Long, startDate: Long, endDate: Long): List<HabitCompletion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(completion: HabitCompletion): Long

    @Delete
    suspend fun deleteHabitCompletion(completion: HabitCompletion)
}
