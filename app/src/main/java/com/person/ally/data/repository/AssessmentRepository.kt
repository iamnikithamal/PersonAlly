package com.person.ally.data.repository

import com.person.ally.data.local.dao.AssessmentDao
import com.person.ally.data.model.Assessment
import com.person.ally.data.model.AssessmentAnswer
import com.person.ally.data.model.AssessmentResult
import com.person.ally.data.model.AssessmentStatus
import com.person.ally.data.model.AssessmentType
import com.person.ally.data.model.LifeDomain
import com.person.ally.data.model.PersonalityTrait
import com.person.ally.data.model.Trend
import com.person.ally.data.model.UniversalContext
import com.person.ally.data.model.ValueItem
import kotlinx.coroutines.flow.Flow

class AssessmentRepository(
    private val assessmentDao: AssessmentDao,
    private val userProfileRepository: UserProfileRepository? = null
) {

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

        // Update user profile and context based on assessment type
        updateUserContextFromAssessment(updatedAssessment, results)
    }

    private suspend fun updateUserContextFromAssessment(
        assessment: Assessment,
        results: List<AssessmentResult>
    ) {
        val profileRepo = userProfileRepository ?: return

        // Increment assessment count
        profileRepo.incrementAssessmentCount()

        when (assessment.type) {
            AssessmentType.PERSONALITY -> {
                // Update personality traits
                val traits = results.map { result ->
                    PersonalityTrait(
                        name = result.dimension,
                        score = result.score,
                        description = result.description,
                        strengths = result.insights.filter { it.contains("strength", ignoreCase = true) },
                        challenges = result.insights.filter { it.contains("development", ignoreCase = true) || it.contains("challenge", ignoreCase = true) }
                    )
                }
                profileRepo.updatePersonalityTraits(traits)

                // Update universal context personality snapshot
                val snapshot = buildPersonalitySnapshot(traits)
                updateUniversalContextSection(profileRepo, personalitySnapshot = snapshot)
            }

            AssessmentType.VALUES -> {
                // Update core values
                val values = results.map { result ->
                    ValueItem(
                        name = result.dimension,
                        importance = result.score,
                        description = result.description
                    )
                }
                profileRepo.updateCoreValues(values)
            }

            AssessmentType.GOALS -> {
                // Update current goals in universal context
                val goals = results.flatMap { it.insights }
                updateUniversalContextSection(profileRepo, currentGoals = goals)
            }

            AssessmentType.EMOTIONAL -> {
                // Update emotional patterns in universal context
                val patterns = results.joinToString(". ") {
                    "${it.dimension}: ${it.description}"
                }
                updateUniversalContextSection(profileRepo, emotionalPatterns = patterns)
            }

            AssessmentType.COGNITIVE -> {
                // Update cognitive style in universal context
                val style = results.joinToString(". ") {
                    "${it.dimension}: ${it.description}"
                }
                updateUniversalContextSection(profileRepo, cognitiveStyle = style)
            }

            AssessmentType.RELATIONSHIPS -> {
                // Update relationship context and life domain progress
                val context = results.joinToString(". ") {
                    "${it.dimension}: ${it.description}"
                }
                updateUniversalContextSection(profileRepo, relationshipContext = context)

                // Also update life domain progress for relationships
                val avgScore = results.map { it.score }.average().toFloat()
                profileRepo.updateLifeDomainProgress(
                    domain = LifeDomain.RELATIONSHIPS,
                    score = avgScore,
                    trend = Trend.STABLE
                )
            }

            AssessmentType.BEHAVIORAL, AssessmentType.INTERESTS -> {
                // Update summary insights in universal context
                val insights = results.flatMap { it.insights }
                if (insights.isNotEmpty()) {
                    val existingContext = profileRepo.getUniversalContextOnce()
                    val newPoints = (existingContext?.coreIdentityPoints ?: emptyList()) + insights.take(3)
                    updateUniversalContextSection(profileRepo, coreIdentityPoints = newPoints.distinct().takeLast(10))
                }
            }
        }
    }

    private fun buildPersonalitySnapshot(traits: List<PersonalityTrait>): String {
        if (traits.isEmpty()) return ""

        val topTraits = traits.sortedByDescending { it.score }.take(3)
        return buildString {
            append("Key personality traits: ")
            append(topTraits.joinToString(", ") {
                "${it.name} (${(it.score * 100).toInt()}%)"
            })
            append(". ")
            topTraits.firstOrNull()?.let { top ->
                if (top.strengths.isNotEmpty()) {
                    append("Primary strength: ${top.strengths.first()}. ")
                }
            }
        }
    }

    private suspend fun updateUniversalContextSection(
        profileRepo: UserProfileRepository,
        personalitySnapshot: String? = null,
        currentGoals: List<String>? = null,
        emotionalPatterns: String? = null,
        cognitiveStyle: String? = null,
        relationshipContext: String? = null,
        coreIdentityPoints: List<String>? = null
    ) {
        val existing = profileRepo.getUniversalContextOnce() ?: UniversalContext()
        val updated = existing.copy(
            personalitySnapshot = personalitySnapshot ?: existing.personalitySnapshot,
            currentGoals = currentGoals ?: existing.currentGoals,
            emotionalPatterns = emotionalPatterns ?: existing.emotionalPatterns,
            cognitiveStyle = cognitiveStyle ?: existing.cognitiveStyle,
            relationshipContext = relationshipContext ?: existing.relationshipContext,
            coreIdentityPoints = coreIdentityPoints ?: existing.coreIdentityPoints,
            updatedAt = System.currentTimeMillis(),
            lastGeneratedAt = System.currentTimeMillis()
        )
        profileRepo.updateUniversalContext(updated)
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
