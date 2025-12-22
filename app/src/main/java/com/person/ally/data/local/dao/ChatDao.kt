package com.person.ally.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.person.ally.data.model.ChatMessage
import com.person.ally.data.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): Conversation?

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversationByIdFlow(id: Long): Flow<Conversation?>

    @Query("""
        SELECT * FROM conversations
        WHERE title LIKE '%' || :query || '%'
        OR summary LIKE '%' || :query || '%'
        OR lastMessagePreview LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchConversations(query: String): Flow<List<Conversation>>

    @Query("SELECT COUNT(*) FROM conversations WHERE isArchived = 0")
    fun getConversationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation): Long

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchivedStatus(id: Long, isArchived: Boolean)

    @Query("""
        UPDATE conversations
        SET messageCount = messageCount + 1,
            lastMessagePreview = :preview,
            updatedAt = :timestamp
        WHERE id = :conversationId
    """)
    suspend fun updateConversationOnNewMessage(
        conversationId: Long,
        preview: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)

    @Query("DELETE FROM conversations WHERE isArchived = 1")
    suspend fun deleteArchivedConversations()

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: Long, limit: Int): List<ChatMessage>

    @Query("SELECT * FROM chat_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): ChatMessage?

    @Query("""
        SELECT * FROM chat_messages
        WHERE conversationId = :conversationId
        AND content LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchMessagesInConversation(conversationId: Long, query: String): Flow<List<ChatMessage>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId")
    fun getMessageCount(conversationId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>): List<Long>

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET memoryExtracted = 1, extractedMemoryIds = :memoryIds WHERE id = :messageId")
    suspend fun markMemoryExtracted(messageId: Long, memoryIds: String)

    @Delete
    suspend fun deleteMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getMostRecentConversation(): Conversation?

    @Query("""
        SELECT cm.* FROM chat_messages cm
        INNER JOIN conversations c ON cm.conversationId = c.id
        WHERE cm.content LIKE '%' || :query || '%'
        ORDER BY cm.createdAt DESC
        LIMIT :limit
    """)
    fun searchAllMessages(query: String, limit: Int = 50): Flow<List<ChatMessage>>
}
