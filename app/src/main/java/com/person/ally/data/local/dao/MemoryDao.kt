package com.person.ally.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.person.ally.data.model.Memory
import com.person.ally.data.model.MemoryCategory
import com.person.ally.data.model.MemoryImportance
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): Memory?

    @Query("SELECT * FROM memories WHERE id = :id")
    fun getMemoryByIdFlow(id: Long): Flow<Memory?>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY importance DESC, updatedAt DESC")
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE importance = :importance ORDER BY updatedAt DESC")
    fun getMemoriesByImportance(importance: MemoryImportance): Flow<List<Memory>>

    @Query("""
        SELECT * FROM memories
        WHERE content LIKE '%' || :query || '%'
        OR tags LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN content LIKE :query || '%' THEN 0 ELSE 1 END,
            importance DESC,
            updatedAt DESC
    """)
    fun searchMemories(query: String): Flow<List<Memory>>

    @Query("""
        SELECT * FROM memories
        WHERE category = :category
        AND (content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        ORDER BY importance DESC, updatedAt DESC
    """)
    fun searchMemoriesInCategory(query: String, category: MemoryCategory): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE sourceConversationId = :conversationId ORDER BY createdAt ASC")
    fun getMemoriesFromConversation(conversationId: Long): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE isUserCreated = 1 ORDER BY updatedAt DESC")
    fun getUserCreatedMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE importance IN ('HIGH', 'CRITICAL') ORDER BY importance DESC, updatedAt DESC")
    fun getImportantMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories ORDER BY lastAccessedAt DESC LIMIT :limit")
    fun getRecentlyAccessedMemories(limit: Int = 10): Flow<List<Memory>>

    @Query("SELECT * FROM memories ORDER BY accessCount DESC LIMIT :limit")
    fun getMostAccessedMemories(limit: Int = 10): Flow<List<Memory>>

    @Query("SELECT COUNT(*) FROM memories")
    fun getMemoryCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM memories WHERE category = :category")
    fun getMemoryCountByCategory(category: MemoryCategory): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<Memory>): List<Long>

    @Update
    suspend fun updateMemory(memory: Memory)

    @Query("UPDATE memories SET lastAccessedAt = :timestamp, accessCount = accessCount + 1 WHERE id = :id")
    suspend fun recordMemoryAccess(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteMemory(memory: Memory)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM memories WHERE category = :category")
    suspend fun deleteMemoriesByCategory(category: MemoryCategory)

    @Query("DELETE FROM memories")
    suspend fun deleteAllMemories()

    @Query("""
        SELECT * FROM memories
        WHERE category = 'CORE_IDENTITY' OR importance IN ('HIGH', 'CRITICAL')
        ORDER BY importance DESC, updatedAt DESC
        LIMIT :limit
    """)
    suspend fun getContextMemories(limit: Int = 20): List<Memory>
}
