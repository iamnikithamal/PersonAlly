package com.person.ally.data.repository

import com.person.ally.data.local.dao.InsightDao
import com.person.ally.data.model.DailyBriefing
import com.person.ally.data.model.Goal
import com.person.ally.data.model.GoalMilestone
import com.person.ally.data.model.Habit
import com.person.ally.data.model.HabitCompletion
import com.person.ally.data.model.Insight
import com.person.ally.data.model.InsightSource
import com.person.ally.data.model.InsightType
import com.person.ally.data.model.LifeDomain
import com.person.ally.data.model.TimeOfDay
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID

class InsightRepository(private val insightDao: InsightDao) {

    fun getAllInsights(): Flow<List<Insight>> = insightDao.getAllInsights()

    suspend fun getInsightById(id: Long): Insight? = insightDao.getInsightById(id)

    fun getInsightsByType(type: InsightType): Flow<List<Insight>> =
        insightDao.getInsightsByType(type)

    fun getUnreadInsights(): Flow<List<Insight>> = insightDao.getUnreadInsights()

    fun getBookmarkedInsights(): Flow<List<Insight>> = insightDao.getBookmarkedInsights()

    fun getRecentInsights(limit: Int = 10): Flow<List<Insight>> =
        insightDao.getRecentInsights(limit)

    fun getUnreadCount(): Flow<Int> = insightDao.getUnreadCount()

    suspend fun createInsight(
        title: String,
        content: String,
        type: InsightType,
        source: InsightSource,
        relatedDomains: List<LifeDomain> = emptyList(),
        relatedMemoryIds: List<Long> = emptyList(),
        isActionable: Boolean = false,
        actionSuggestion: String? = null
    ): Long {
        val insight = Insight(
            title = title,
            content = content,
            type = type,
            source = source,
            relatedDomains = relatedDomains,
            relatedMemoryIds = relatedMemoryIds,
            isActionable = isActionable,
            actionSuggestion = actionSuggestion
        )
        return insightDao.insertInsight(insight)
    }

    suspend fun insertInsight(insight: Insight): Long = insightDao.insertInsight(insight)

    suspend fun insertInsights(insights: List<Insight>): List<Long> =
        insightDao.insertInsights(insights)

    suspend fun updateInsight(insight: Insight) = insightDao.updateInsight(insight)

    suspend fun markAsRead(id: Long) = insightDao.markAsRead(id)

    suspend fun markAllAsRead() = insightDao.markAllAsRead()

    suspend fun updateBookmarkStatus(id: Long, bookmarked: Boolean) =
        insightDao.updateBookmarkStatus(id, bookmarked)

    suspend fun dismissInsight(id: Long) = insightDao.dismissInsight(id)

    suspend fun deleteInsight(insight: Insight) = insightDao.deleteInsight(insight)

    suspend fun deleteInsightById(id: Long) = insightDao.deleteInsightById(id)

    suspend fun deleteDismissedInsights() = insightDao.deleteDismissedInsights()

    suspend fun getDailyBriefing(date: Long, timeOfDay: TimeOfDay): DailyBriefing? =
        insightDao.getDailyBriefing(date, timeOfDay)

    fun getLatestBriefing(): Flow<DailyBriefing?> = insightDao.getLatestBriefing()

    fun getBriefingsForDate(date: Long): Flow<List<DailyBriefing>> =
        insightDao.getBriefingsForDate(date)

    suspend fun createDailyBriefing(
        userName: String,
        timeOfDay: TimeOfDay,
        insights: List<Insight>,
        goals: List<Goal>,
        habits: List<Habit>
    ): Long {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (timeOfDay) {
            TimeOfDay.MORNING -> "Good morning, $userName"
            TimeOfDay.MIDDAY -> "Good afternoon, $userName"
            TimeOfDay.EVENING -> "Good evening, $userName"
        }

        val mainMessage = when (timeOfDay) {
            TimeOfDay.MORNING -> generateMorningMessage(goals, habits)
            TimeOfDay.MIDDAY -> generateMiddayMessage(insights, goals)
            TimeOfDay.EVENING -> generateEveningMessage(insights, habits)
        }

        val highlights = generateHighlights(timeOfDay, insights, goals, habits)
        val suggestedActions = generateSuggestedActions(timeOfDay, goals, habits)

        val briefing = DailyBriefing(
            date = normalizeToDay(System.currentTimeMillis()),
            timeOfDay = timeOfDay,
            greeting = greeting,
            mainMessage = mainMessage,
            highlights = highlights,
            suggestedActions = suggestedActions
        )

        return insightDao.insertDailyBriefing(briefing)
    }

    private fun normalizeToDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun generateMorningMessage(goals: List<Goal>, habits: List<Habit>): String {
        val activeGoals = goals.filter { !it.isCompleted && !it.isPaused }
        val activeHabits = habits.filter { it.isActive }
        return when {
            activeGoals.isEmpty() && activeHabits.isEmpty() ->
                "Today is a fresh start. Consider setting some goals or habits to track your progress."
            activeGoals.isNotEmpty() ->
                "You have ${activeGoals.size} active goal${if (activeGoals.size > 1) "s" else ""} to work towards today."
            else ->
                "Ready to maintain your ${activeHabits.size} habit${if (activeHabits.size > 1) "s" else ""} today?"
        }
    }

    private fun generateMiddayMessage(insights: List<Insight>, goals: List<Goal>): String {
        val recentInsights = insights.filter { !it.isRead }.take(3)
        return when {
            recentInsights.isNotEmpty() ->
                "You have ${recentInsights.size} new insight${if (recentInsights.size > 1) "s" else ""} waiting for you."
            goals.any { it.progress > 0.5f && !it.isCompleted } ->
                "Great progress on your goals! Keep up the momentum."
            else ->
                "How's your day going? Take a moment to check in with yourself."
        }
    }

    private fun generateEveningMessage(insights: List<Insight>, habits: List<Habit>): String {
        val habitsWithStreak = habits.filter { it.currentStreak > 0 }
        return when {
            habitsWithStreak.isNotEmpty() ->
                "Nice work maintaining ${habitsWithStreak.size} habit streak${if (habitsWithStreak.size > 1) "s" else ""} today!"
            else ->
                "As the day winds down, reflect on what went well and what you learned."
        }
    }

    private fun generateHighlights(
        timeOfDay: TimeOfDay,
        insights: List<Insight>,
        goals: List<Goal>,
        habits: List<Habit>
    ): List<String> {
        val highlights = mutableListOf<String>()
        val unreadInsights = insights.count { !it.isRead }
        if (unreadInsights > 0) {
            highlights.add("$unreadInsights unread insight${if (unreadInsights > 1) "s" else ""}")
        }
        val nearingGoals = goals.filter { it.progress >= 0.8f && !it.isCompleted }
        nearingGoals.take(2).forEach { goal ->
            highlights.add("Almost there: ${goal.title} (${(goal.progress * 100).toInt()}%)")
        }
        val topStreaks = habits.filter { it.currentStreak >= 7 }.sortedByDescending { it.currentStreak }
        topStreaks.take(2).forEach { habit ->
            highlights.add("${habit.currentStreak}-day streak: ${habit.title}")
        }
        return highlights.take(5)
    }

    private fun generateSuggestedActions(
        timeOfDay: TimeOfDay,
        goals: List<Goal>,
        habits: List<Habit>
    ): List<String> {
        val actions = mutableListOf<String>()
        when (timeOfDay) {
            TimeOfDay.MORNING -> {
                actions.add("Review your priorities for today")
                if (habits.isNotEmpty()) {
                    actions.add("Complete your morning habits")
                }
            }
            TimeOfDay.MIDDAY -> {
                actions.add("Take a mindful break")
                if (goals.isNotEmpty()) {
                    actions.add("Make progress on one of your goals")
                }
            }
            TimeOfDay.EVENING -> {
                actions.add("Reflect on today's wins")
                actions.add("Plan tomorrow's top priority")
            }
        }
        return actions.take(3)
    }

    suspend fun markBriefingViewed(id: Long) = insightDao.markBriefingViewed(id)

    suspend fun deleteOldBriefings(daysToKeep: Int = 30) {
        val cutoff = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        insightDao.deleteOldBriefings(cutoff)
    }

    fun getActiveGoals(): Flow<List<Goal>> = insightDao.getActiveGoals()

    fun getGoalsByDomain(domain: LifeDomain): Flow<List<Goal>> =
        insightDao.getGoalsByDomain(domain)

    fun getCompletedGoals(): Flow<List<Goal>> = insightDao.getCompletedGoals()

    suspend fun getGoalById(id: Long): Goal? = insightDao.getGoalById(id)

    suspend fun createGoal(
        title: String,
        description: String = "",
        domain: LifeDomain,
        targetDate: Long? = null,
        milestones: List<String> = emptyList()
    ): Long {
        val goalMilestones = milestones.map { milestone ->
            GoalMilestone(
                id = UUID.randomUUID().toString(),
                title = milestone
            )
        }
        val goal = Goal(
            title = title,
            description = description,
            domain = domain,
            targetDate = targetDate,
            milestones = goalMilestones
        )
        return insightDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: Goal) = insightDao.updateGoal(goal)

    suspend fun updateGoalProgress(id: Long, progress: Float) =
        insightDao.updateGoalProgress(id, progress.coerceIn(0f, 1f))

    suspend fun completeGoal(id: Long) = insightDao.completeGoal(id)

    suspend fun deleteGoal(goal: Goal) = insightDao.deleteGoal(goal)

    suspend fun updateGoalMilestone(goalId: Long, milestoneId: String, completed: Boolean) {
        val goal = insightDao.getGoalById(goalId) ?: return
        val milestones = goal.milestones.map { milestone ->
            if (milestone.id == milestoneId) {
                milestone.copy(
                    isCompleted = completed,
                    completedAt = if (completed) System.currentTimeMillis() else null
                )
            } else milestone
        }
        val completedCount = milestones.count { it.isCompleted }
        val progress = if (milestones.isNotEmpty()) {
            completedCount.toFloat() / milestones.size
        } else goal.progress

        val updated = goal.copy(
            milestones = milestones,
            progress = progress,
            updatedAt = System.currentTimeMillis()
        )
        insightDao.updateGoal(updated)
    }

    fun getActiveHabits(): Flow<List<Habit>> = insightDao.getActiveHabits()

    fun getHabitsByDomain(domain: LifeDomain): Flow<List<Habit>> =
        insightDao.getHabitsByDomain(domain)

    suspend fun getHabitById(id: Long): Habit? = insightDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = insightDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = insightDao.updateHabit(habit)

    suspend fun recordHabitCompletion(habitId: Long, note: String? = null) {
        insightDao.recordHabitCompletion(habitId)
        val completion = HabitCompletion(
            habitId = habitId,
            note = note
        )
        insightDao.insertHabitCompletion(completion)
    }

    suspend fun resetHabitStreak(habitId: Long) = insightDao.resetHabitStreak(habitId)

    suspend fun deleteHabit(habit: Habit) = insightDao.deleteHabit(habit)

    fun getHabitCompletions(habitId: Long): Flow<List<HabitCompletion>> =
        insightDao.getHabitCompletions(habitId)

    suspend fun getHabitCompletionsInRange(
        habitId: Long,
        startDate: Long,
        endDate: Long
    ): List<HabitCompletion> = insightDao.getHabitCompletionsInRange(habitId, startDate, endDate)
}
