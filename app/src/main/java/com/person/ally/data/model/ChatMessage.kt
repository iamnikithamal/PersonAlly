package com.person.ally.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    val status: MessageStatus = MessageStatus.SENT,
    val memoryExtracted: Boolean = false,
    val extractedMemoryIds: List<Long> = emptyList(),
    val contextUsed: Boolean = false,
    val tokensUsed: Int = 0,
    val responseTimeMs: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isFromUser: Boolean get() = role == MessageRole.USER
    val isFromAssistant: Boolean get() = role == MessageRole.ASSISTANT
}

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "New Conversation",
    val summary: String? = null,
    val messageCount: Int = 0,
    val lastMessagePreview: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

class ChatTypeConverters {
    @TypeConverter
    fun fromMessageRole(role: MessageRole): String = role.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = MessageRole.valueOf(value)

    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = MessageStatus.valueOf(value)
}
