package com.person.ally.data.repository

import com.person.ally.data.local.dao.ChatDao
import com.person.ally.data.model.ChatMessage
import com.person.ally.data.model.Conversation
import com.person.ally.data.model.MessageRole
import com.person.ally.data.model.MessageStatus
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {

    fun getAllConversations(): Flow<List<Conversation>> = chatDao.getAllConversations()

    fun getArchivedConversations(): Flow<List<Conversation>> = chatDao.getArchivedConversations()

    fun getPinnedConversations(): Flow<List<Conversation>> = chatDao.getPinnedConversations()

    suspend fun getConversationById(id: Long): Conversation? = chatDao.getConversationById(id)

    fun getConversationByIdFlow(id: Long): Flow<Conversation?> = chatDao.getConversationByIdFlow(id)

    fun searchConversations(query: String): Flow<List<Conversation>> =
        chatDao.searchConversations(query)

    fun getConversationCount(): Flow<Int> = chatDao.getConversationCount()

    suspend fun createConversation(title: String = "New Conversation"): Long {
        val conversation = Conversation(title = title)
        return chatDao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: Conversation) =
        chatDao.updateConversation(conversation)

    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean) =
        chatDao.updatePinnedStatus(id, isPinned)

    suspend fun updateArchivedStatus(id: Long, isArchived: Boolean) =
        chatDao.updateArchivedStatus(id, isArchived)

    suspend fun deleteConversation(conversation: Conversation) =
        chatDao.deleteConversation(conversation)

    suspend fun deleteConversationById(id: Long) = chatDao.deleteConversationById(id)

    suspend fun deleteArchivedConversations() = chatDao.deleteArchivedConversations()

    fun getMessagesForConversation(conversationId: Long): Flow<List<ChatMessage>> =
        chatDao.getMessagesForConversation(conversationId)

    suspend fun getRecentMessages(conversationId: Long, limit: Int): List<ChatMessage> =
        chatDao.getRecentMessages(conversationId, limit)

    suspend fun getMessageById(id: Long): ChatMessage? = chatDao.getMessageById(id)

    fun searchMessagesInConversation(conversationId: Long, query: String): Flow<List<ChatMessage>> =
        chatDao.searchMessagesInConversation(conversationId, query)

    fun getMessageCount(conversationId: Long): Flow<Int> = chatDao.getMessageCount(conversationId)

    suspend fun sendUserMessage(conversationId: Long, content: String): Long {
        val message = ChatMessage(
            conversationId = conversationId,
            role = MessageRole.USER,
            content = content,
            status = MessageStatus.SENT
        )
        val messageId = chatDao.insertMessage(message)
        chatDao.updateConversationOnNewMessage(
            conversationId = conversationId,
            preview = content.take(100)
        )
        return messageId
    }

    suspend fun addAssistantMessage(
        conversationId: Long,
        content: String,
        tokensUsed: Int = 0,
        responseTimeMs: Long = 0,
        memoryExtracted: Boolean = false,
        extractedMemoryIds: List<Long> = emptyList(),
        contextUsed: Boolean = false
    ): Long {
        val message = ChatMessage(
            conversationId = conversationId,
            role = MessageRole.ASSISTANT,
            content = content,
            status = MessageStatus.DELIVERED,
            tokensUsed = tokensUsed,
            responseTimeMs = responseTimeMs,
            memoryExtracted = memoryExtracted,
            extractedMemoryIds = extractedMemoryIds,
            contextUsed = contextUsed
        )
        val messageId = chatDao.insertMessage(message)
        chatDao.updateConversationOnNewMessage(
            conversationId = conversationId,
            preview = content.take(100)
        )
        return messageId
    }

    suspend fun updateMessage(message: ChatMessage) = chatDao.updateMessage(message)

    suspend fun markMemoryExtracted(messageId: Long, memoryIds: List<Long>) {
        val memoryIdsJson = com.google.gson.Gson().toJson(memoryIds)
        chatDao.markMemoryExtracted(messageId, memoryIdsJson)
    }

    suspend fun deleteMessage(message: ChatMessage) = chatDao.deleteMessage(message)

    suspend fun deleteMessagesForConversation(conversationId: Long) =
        chatDao.deleteMessagesForConversation(conversationId)

    suspend fun getMostRecentConversation(): Conversation? = chatDao.getMostRecentConversation()

    fun searchAllMessages(query: String, limit: Int = 50): Flow<List<ChatMessage>> =
        chatDao.searchAllMessages(query, limit)

    suspend fun getOrCreateActiveConversation(): Long {
        val recent = chatDao.getMostRecentConversation()
        return if (recent != null && !recent.isArchived) {
            recent.id
        } else {
            createConversation("Conversation")
        }
    }

    suspend fun generateConversationTitle(conversationId: Long): String {
        val messages = chatDao.getRecentMessages(conversationId, 5)
        val firstUserMessage = messages.firstOrNull { it.role == MessageRole.USER }
        return firstUserMessage?.content?.take(50)?.let {
            if (it.length == 50) "$it..." else it
        } ?: "Conversation"
    }
}
