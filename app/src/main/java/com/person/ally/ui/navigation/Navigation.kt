package com.person.ally.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen routes for navigation
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Chat : Screen("chat")
    data object ChatConversation : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: Long) = "chat/$conversationId"
        const val ARG_CONVERSATION_ID = "conversationId"
    }
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object AiModels : Screen("settings/ai-models")
    data object Memories : Screen("memories")
    data object MemoryDetail : Screen("memory/{memoryId}") {
        fun createRoute(memoryId: Long) = "memory/$memoryId"
        const val ARG_MEMORY_ID = "memoryId"
    }
    data object Assessments : Screen("assessments")
    data object AssessmentDetail : Screen("assessment/{assessmentId}") {
        fun createRoute(assessmentId: String) = "assessment/$assessmentId"
        const val ARG_ASSESSMENT_ID = "assessmentId"
    }
    data object Insights : Screen("insights")
    data object InsightDetail : Screen("insight/{insightId}") {
        fun createRoute(insightId: Long) = "insight/$insightId"
        const val ARG_INSIGHT_ID = "insightId"
    }
    data object GoalDetail : Screen("goal/{goalId}") {
        fun createRoute(goalId: Long) = "goal/$goalId"
        const val ARG_GOAL_ID = "goalId"
    }
    data object ContextExport : Screen("context/export")
    data object ContextEdit : Screen("context/edit")
    data object Journal : Screen("journal")
    data object JournalEntry : Screen("journal/{entryId}") {
        fun createRoute(entryId: Long) = "journal/$entryId"
        const val ARG_ENTRY_ID = "entryId"
    }
}

/**
 * Bottom navigation items - simplified to 3 tabs:
 * Home (with integrated insights), Chat (conversations list), Profile
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Chat : BottomNavItem(
        route = Screen.Chat.route,
        title = "Chat",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    )

    data object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val items = listOf(Home, Chat, Profile)
    }
}
