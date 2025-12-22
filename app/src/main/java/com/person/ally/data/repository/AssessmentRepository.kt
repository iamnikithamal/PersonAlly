package com.person.ally.data.repository

import com.person.ally.data.local.dao.AssessmentDao
import com.person.ally.data.model.Assessment
import com.person.ally.data.model.AssessmentAnswer
import com.person.ally.data.model.AssessmentResult
import com.person.ally.data.model.AssessmentStatus
import com.person.ally.data.model.AssessmentType
import kotlinx.coroutines.flow.Flow

class AssessmentRepository(private val assessmentDao: AssessmentDao) {

    fun getAllAssessments(): Flow<List<Assessment>> = assessmentDao.getAllAssessments()

    suspend fun getAssessmentById(id: String): Assessment? = assessmentDao.getAssessmentById(id)

    fun getAssessmentByIdFlow(id: String): Flow<Assessment?> = assessmentDao.getAssessmentByIdFlow(id)

    fun getAssessmentsByType(type: AssessmentType): Flow<List<Assessment>> =
        assessmentDao.getAssessmentsByType(type)

    fun getAssessmentsByStatus(status: AssessmentStatus): Flow<List<Assessment>> =
        assessmentDao.getAssessmentsByStatus(status)

    fun getAvailableAssessments(): Flow<List<Assessment>> = assessmentDao.getAvailableAssessments()

    fun getCompletedAssessments(): Flow<List<Assessment>> = assessmentDao.getCompletedAssessments()

    fun getInProgressAssessments(): Flow<List<Assessment>> = assessmentDao.getInProgressAssessments()

    fun getLockedAssessments(): Flow<List<Assessment>> = assessmentDao.getLockedAssessments()

    fun getCompletedCount(): Flow<Int> = assessmentDao.getCompletedCount()

    fun getTotalCount(): Flow<Int> = assessmentDao.getTotalCount()

    suspend fun insertAssessment(assessment: Assessment) = assessmentDao.insertAssessment(assessment)

    suspend fun insertAssessments(assessments: List<Assessment>) =
        assessmentDao.insertAssessments(assessments)

    suspend fun updateAssessment(assessment: Assessment) = assessmentDao.updateAssessment(assessment)

    suspend fun startAssessment(id: String) {
        assessmentDao.updateStatus(id, AssessmentStatus.IN_PROGRESS)
    }

    suspend fun submitAnswer(assessmentId: String, answer: AssessmentAnswer) {
        val assessment = assessmentDao.getAssessmentById(assessmentId) ?: return
        val existingAnswers = assessment.answers.toMutableList()
        val existingIndex = existingAnswers.indexOfFirst { it.questionId == answer.questionId }
        if (existingIndex >= 0) {
            existingAnswers[existingIndex] = answer
        } else {
            existingAnswers.add(answer)
        }
        val updatedAssessment = assessment.copy(
            answers = existingAnswers,
            currentQuestionIndex = existingAnswers.size,
            updatedAt = System.currentTimeMillis()
        )
        assessmentDao.updateAssessment(updatedAssessment)
    }

    suspend fun completeAssessment(id: String, results: List<AssessmentResult>) {
        val assessment = assessmentDao.getAssessmentById(id) ?: return
        val updatedAssessment = assessment.copy(
            status = AssessmentStatus.COMPLETED,
            results = results,
            lastCompletedAt = System.currentTimeMillis(),
            completionCount = assessment.completionCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        assessmentDao.updateAssessment(updatedAssessment)
    }

    suspend fun updateProgress(id: String, questionIndex: Int) {
        assessmentDao.updateProgress(id, questionIndex)
    }

    suspend fun deleteAssessment(assessment: Assessment) = assessmentDao.deleteAssessment(assessment)

    suspend fun deleteAssessmentById(id: String) = assessmentDao.deleteAssessmentById(id)

    suspend fun deleteAllAssessments() = assessmentDao.deleteAllAssessments()

    suspend fun resetAssessment(id: String) = assessmentDao.resetAssessment(id)

    fun calculateResults(assessment: Assessment): List<AssessmentResult> {
        if (assessment.answers.isEmpty()) return emptyList()

        val categoryScores = mutableMapOf<String, MutableList<Float>>()

        assessment.questions.forEach { question ->
            val answer = assessment.answers.find { it.questionId == question.id }
            if (answer != null) {
                val category = question.category ?: "General"
                val score = when {
                    answer.numericAnswer != null -> {
                        val range = question.maxValue - question.minValue
                        if (range > 0) {
                            (answer.numericAnswer - question.minValue).toFloat() / range
                        } else {
                            answer.numericAnswer.toFloat() / 10f
                        }
                    }
                    answer.selectedOptions.isNotEmpty() -> {
                        answer.selectedOptions.first().toFloat() / (question.options.size - 1).coerceAtLeast(1)
                    }
                    else -> 0.5f
                }
                categoryScores.getOrPut(category) { mutableListOf() }.add(score * question.weight)
            }
        }

        return categoryScores.map { (category, scores) ->
            val avgScore = if (scores.isNotEmpty()) scores.average().toFloat() else 0f
            AssessmentResult(
                dimension = category,
                score = avgScore,
                maxScore = 1f,
                percentile = (avgScore * 100).coerceIn(0f, 100f),
                description = getResultDescription(category, avgScore),
                insights = generateInsights(category, avgScore)
            )
        }.sortedByDescending { it.score }
    }

    private fun getResultDescription(category: String, score: Float): String {
        val level = when {
            score >= 0.8f -> "very high"
            score >= 0.6f -> "high"
            score >= 0.4f -> "moderate"
            score >= 0.2f -> "low"
            else -> "very low"
        }
        return "Your $category score is $level."
    }

    private fun generateInsights(category: String, score: Float): List<String> {
        val insights = mutableListOf<String>()
        when {
            score >= 0.7f -> {
                insights.add("$category is a clear strength for you.")
                insights.add("Consider how you can leverage this in other areas.")
            }
            score >= 0.4f -> {
                insights.add("You have a balanced approach to $category.")
                insights.add("There may be opportunities for growth here.")
            }
            else -> {
                insights.add("$category may be an area for development.")
                insights.add("Consider what small steps could strengthen this area.")
            }
        }
        return insights
    }
}
