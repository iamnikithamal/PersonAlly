package com.person.ally.data.repository

import com.person.ally.data.local.dao.MemoryDao
import com.person.ally.data.model.Memory
import com.person.ally.data.model.MemoryCategory
import com.person.ally.data.model.MemoryImportance
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {

    fun getAllMemories(): Flow<List<Memory>> = memoryDao.getAllMemories()

    suspend fun getMemoryById(id: Long): Memory? = memoryDao.getMemoryById(id)

    fun getMemoryByIdFlow(id: Long): Flow<Memory?> = memoryDao.getMemoryByIdFlow(id)

    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<Memory>> =
        memoryDao.getMemoriesByCategory(category)

    fun getMemoriesByImportance(importance: MemoryImportance): Flow<List<Memory>> =
        memoryDao.getMemoriesByImportance(importance)

    fun searchMemories(query: String): Flow<List<Memory>> = memoryDao.searchMemories(query)

    fun searchMemoriesInCategory(query: String, category: MemoryCategory): Flow<List<Memory>> =
        memoryDao.searchMemoriesInCategory(query, category)

    fun getMemoriesFromConversation(conversationId: Long): Flow<List<Memory>> =
        memoryDao.getMemoriesFromConversation(conversationId)

    fun getUserCreatedMemories(): Flow<List<Memory>> = memoryDao.getUserCreatedMemories()

    fun getImportantMemories(): Flow<List<Memory>> = memoryDao.getImportantMemories()

    fun getRecentlyAccessedMemories(limit: Int = 10): Flow<List<Memory>> =
        memoryDao.getRecentlyAccessedMemories(limit)

    fun getMostAccessedMemories(limit: Int = 10): Flow<List<Memory>> =
        memoryDao.getMostAccessedMemories(limit)

    fun getMemoryCount(): Flow<Int> = memoryDao.getMemoryCount()

    fun getMemoryCountByCategory(category: MemoryCategory): Flow<Int> =
        memoryDao.getMemoryCountByCategory(category)

    suspend fun insertMemory(memory: Memory): Long = memoryDao.insertMemory(memory)

    suspend fun insertMemories(memories: List<Memory>): List<Long> =
        memoryDao.insertMemories(memories)

    suspend fun updateMemory(memory: Memory) = memoryDao.updateMemory(memory)

    suspend fun recordMemoryAccess(id: Long) = memoryDao.recordMemoryAccess(id)

    suspend fun deleteMemory(memory: Memory) = memoryDao.deleteMemory(memory)

    suspend fun deleteMemoryById(id: Long) = memoryDao.deleteMemoryById(id)

    suspend fun deleteMemoriesByCategory(category: MemoryCategory) =
        memoryDao.deleteMemoriesByCategory(category)

    suspend fun deleteAllMemories() = memoryDao.deleteAllMemories()

    suspend fun getContextMemories(limit: Int = 20): List<Memory> =
        memoryDao.getContextMemories(limit)

    suspend fun createMemoryFromConversation(
        content: String,
        conversationId: Long,
        category: MemoryCategory = MemoryCategory.EVOLVING_UNDERSTANDING,
        importance: MemoryImportance = MemoryImportance.MEDIUM,
        tags: List<String> = emptyList()
    ): Long {
        val memory = Memory(
            content = content,
            category = category,
            importance = importance,
            tags = tags,
            sourceConversationId = conversationId,
            isUserCreated = false
        )
        return memoryDao.insertMemory(memory)
    }

    suspend fun createUserMemory(
        content: String,
        category: MemoryCategory,
        importance: MemoryImportance = MemoryImportance.MEDIUM,
        tags: List<String> = emptyList()
    ): Long {
        val memory = Memory(
            content = content,
            category = category,
            importance = importance,
            tags = tags,
            isUserCreated = true
        )
        return memoryDao.insertMemory(memory)
    }

    suspend fun editMemory(
        id: Long,
        content: String,
        category: MemoryCategory,
        importance: MemoryImportance,
        tags: List<String>
    ) {
        val existing = memoryDao.getMemoryById(id) ?: return
        val updated = existing.copy(
            content = content,
            category = category,
            importance = importance,
            tags = tags,
            isEdited = true,
            updatedAt = System.currentTimeMillis()
        )
        memoryDao.updateMemory(updated)
    }
}
