package com.person.ally.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class AssessmentType {
    PERSONALITY,
    VALUES,
    COGNITIVE,
    EMOTIONAL,
    BEHAVIORAL,
    INTERESTS,
    GOALS,
    RELATIONSHIPS
}

enum class QuestionType {
    YES_NO,
    MULTIPLE_CHOICE,
    SLIDER,
    TEXT_INPUT,
    SCENARIO,
    RANKING
}

enum class AssessmentStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    LOCKED
}

data class AssessmentQuestion(
    val id: String,
    val text: String,
    val type: QuestionType,
    val options: List<String> = emptyList(),
    val minValue: Int = 0,
    val maxValue: Int = 10,
    val minLabel: String? = null,
    val maxLabel: String? = null,
    val required: Boolean = true,
    val category: String? = null,
    val weight: Float = 1.0f
)

data class AssessmentAnswer(
    val questionId: String,
    val textAnswer: String? = null,
    val numericAnswer: Int? = null,
    val selectedOptions: List<Int> = emptyList(),
    val answeredAt: Long = System.currentTimeMillis()
)

data class AssessmentResult(
    val dimension: String,
    val score: Float,
    val maxScore: Float,
    val percentile: Float? = null,
    val description: String,
    val insights: List<String> = emptyList()
)

@Entity(tableName = "assessments")
data class Assessment(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: AssessmentType,
    val iconName: String,
    val estimatedMinutes: Int,
    val questionCount: Int,
    val questions: List<AssessmentQuestion>,
    val status: AssessmentStatus = AssessmentStatus.NOT_STARTED,
    val answers: List<AssessmentAnswer> = emptyList(),
    val results: List<AssessmentResult> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val unlockRequirement: String? = null,
    val isRepeatable: Boolean = false,
    val lastCompletedAt: Long? = null,
    val completionCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (questions.isEmpty()) 0f else answers.size.toFloat() / questions.size

    val isCompleted: Boolean
        get() = status == AssessmentStatus.COMPLETED

    val isLocked: Boolean
        get() = status == AssessmentStatus.LOCKED

    val canStart: Boolean
        get() = status == AssessmentStatus.NOT_STARTED || (status == AssessmentStatus.COMPLETED && isRepeatable)
}

class AssessmentTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromAssessmentType(type: AssessmentType): String = type.name

    @TypeConverter
    fun toAssessmentType(value: String): AssessmentType = AssessmentType.valueOf(value)

    @TypeConverter
    fun fromAssessmentStatus(status: AssessmentStatus): String = status.name

    @TypeConverter
    fun toAssessmentStatus(value: String): AssessmentStatus = AssessmentStatus.valueOf(value)

    @TypeConverter
    fun fromQuestionList(questions: List<AssessmentQuestion>): String = gson.toJson(questions)

    @TypeConverter
    fun toQuestionList(value: String): List<AssessmentQuestion> {
        val type = object : TypeToken<List<AssessmentQuestion>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromAnswerList(answers: List<AssessmentAnswer>): String = gson.toJson(answers)

    @TypeConverter
    fun toAnswerList(value: String): List<AssessmentAnswer> {
        val type = object : TypeToken<List<AssessmentAnswer>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromResultList(results: List<AssessmentResult>): String = gson.toJson(results)

    @TypeConverter
    fun toResultList(value: String): List<AssessmentResult> {
        val type = object : TypeToken<List<AssessmentResult>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
