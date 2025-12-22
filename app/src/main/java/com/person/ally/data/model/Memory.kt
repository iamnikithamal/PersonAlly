package com.person.ally.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class MemoryCategory {
    CORE_IDENTITY,
    EVOLVING_UNDERSTANDING,
    CONTEXTUAL,
    EPISODIC
}

enum class MemoryImportance {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val category: MemoryCategory,
    val importance: MemoryImportance = MemoryImportance.MEDIUM,
    val tags: List<String> = emptyList(),
    val sourceConversationId: Long? = null,
    val relatedMemoryIds: List<Long> = emptyList(),
    val isUserCreated: Boolean = false,
    val isEdited: Boolean = false,
    val confidence: Float = 1.0f,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getDisplayCategory(): String = when (category) {
        MemoryCategory.CORE_IDENTITY -> "Core Identity"
        MemoryCategory.EVOLVING_UNDERSTANDING -> "Evolving Understanding"
        MemoryCategory.CONTEXTUAL -> "Contextual"
        MemoryCategory.EPISODIC -> "Episodic"
    }

    fun getImportanceWeight(): Float = when (importance) {
        MemoryImportance.LOW -> 0.25f
        MemoryImportance.MEDIUM -> 0.5f
        MemoryImportance.HIGH -> 0.75f
        MemoryImportance.CRITICAL -> 1.0f
    }
}

class MemoryTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMemoryCategory(category: MemoryCategory): String = category.name

    @TypeConverter
    fun toMemoryCategory(value: String): MemoryCategory = MemoryCategory.valueOf(value)

    @TypeConverter
    fun fromMemoryImportance(importance: MemoryImportance): String = importance.name

    @TypeConverter
    fun toMemoryImportance(value: String): MemoryImportance = MemoryImportance.valueOf(value)

    @TypeConverter
    fun fromStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromLongList(list: List<Long>): String = gson.toJson(list)

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
