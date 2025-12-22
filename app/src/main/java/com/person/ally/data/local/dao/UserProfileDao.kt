package com.person.ally.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.person.ally.data.model.UniversalContext
import com.person.ally.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET name = :name, preferredName = :preferredName, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateName(name: String, preferredName: String?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET onboardingCompleted = :completed, onboardingStep = :step, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateOnboardingStatus(completed: Boolean, step: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET currentStreak = :streak, longestStreak = CASE WHEN :streak > longestStreak THEN :streak ELSE longestStreak END, lastActiveAt = :timestamp, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateStreak(streak: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET totalConversations = totalConversations + 1, updatedAt = :timestamp WHERE id = 1")
    suspend fun incrementConversationCount(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET totalMemories = totalMemories + 1, updatedAt = :timestamp WHERE id = 1")
    suspend fun incrementMemoryCount(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET totalInsights = totalInsights + 1, updatedAt = :timestamp WHERE id = 1")
    suspend fun incrementInsightCount(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET totalAssessmentsCompleted = totalAssessmentsCompleted + 1, updatedAt = :timestamp WHERE id = 1")
    suspend fun incrementAssessmentCount(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET lastActiveAt = :timestamp WHERE id = 1")
    suspend fun updateLastActive(timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()

    @Query("SELECT * FROM universal_context WHERE id = 1")
    fun getUniversalContext(): Flow<UniversalContext?>

    @Query("SELECT * FROM universal_context WHERE id = 1")
    suspend fun getUniversalContextOnce(): UniversalContext?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUniversalContext(context: UniversalContext)

    @Update
    suspend fun updateUniversalContext(context: UniversalContext)

    @Query("UPDATE universal_context SET isUserEdited = 1, lastEditedAt = :timestamp, updatedAt = :timestamp WHERE id = 1")
    suspend fun markContextAsEdited(timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM universal_context")
    suspend fun deleteUniversalContext()
}
