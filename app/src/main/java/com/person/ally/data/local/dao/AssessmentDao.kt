package com.person.ally.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.person.ally.data.model.Assessment
import com.person.ally.data.model.AssessmentStatus
import com.person.ally.data.model.AssessmentType
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments ORDER BY status ASC, updatedAt DESC")
    fun getAllAssessments(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE id = :id")
    suspend fun getAssessmentById(id: String): Assessment?

    @Query("SELECT * FROM assessments WHERE id = :id")
    fun getAssessmentByIdFlow(id: String): Flow<Assessment?>

    @Query("SELECT * FROM assessments WHERE type = :type ORDER BY updatedAt DESC")
    fun getAssessmentsByType(type: AssessmentType): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE status = :status ORDER BY updatedAt DESC")
    fun getAssessmentsByStatus(status: AssessmentStatus): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE status = 'NOT_STARTED' OR (status = 'COMPLETED' AND isRepeatable = 1) ORDER BY updatedAt DESC")
    fun getAvailableAssessments(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE status = 'COMPLETED' ORDER BY lastCompletedAt DESC")
    fun getCompletedAssessments(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE status = 'IN_PROGRESS' ORDER BY updatedAt DESC")
    fun getInProgressAssessments(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE status = 'LOCKED' ORDER BY title ASC")
    fun getLockedAssessments(): Flow<List<Assessment>>

    @Query("SELECT COUNT(*) FROM assessments WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assessments")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: Assessment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessments(assessments: List<Assessment>)

    @Update
    suspend fun updateAssessment(assessment: Assessment)

    @Query("UPDATE assessments SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: String, status: AssessmentStatus, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE assessments
        SET status = 'COMPLETED',
            lastCompletedAt = :timestamp,
            completionCount = completionCount + 1,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun markCompleted(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE assessments SET currentQuestionIndex = :index, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateProgress(id: String, index: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteAssessment(assessment: Assessment)

    @Query("DELETE FROM assessments WHERE id = :id")
    suspend fun deleteAssessmentById(id: String)

    @Query("DELETE FROM assessments")
    suspend fun deleteAllAssessments()

    @Query("""
        UPDATE assessments
        SET status = 'NOT_STARTED',
            currentQuestionIndex = 0,
            answers = '[]',
            results = '[]',
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun resetAssessment(id: String, timestamp: Long = System.currentTimeMillis())
}
