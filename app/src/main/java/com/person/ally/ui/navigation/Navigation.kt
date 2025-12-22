package com.person.ally.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Chat : Screen("chat")
    data object Memories : Screen("memories")
    data object Insights : Screen("insights")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Assessments : Screen("assessments")
    data object AssessmentDetail : Screen("assessment/{assessmentId}") {
        fun createRoute(assessmentId: String) = "assessment/$assessmentId"
    }
    data object MemoryDetail : Screen("memory/{memoryId}") {
        fun createRoute(memoryId: Long) = "memory/$memoryId"
    }
    data object InsightDetail : Screen("insight/{insightId}") {
        fun createRoute(insightId: Long) = "insight/$insightId"
    }
    data object GoalDetail : Screen("goal/{goalId}") {
        fun createRoute(goalId: Long) = "goal/$goalId"
    }
    data object ContextExport : Screen("context/export")
    data object ContextEdit : Screen("context/edit")
}

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

    data object Memories : BottomNavItem(
        route = Screen.Memories.route,
        title = "Memories",
        selectedIcon = Icons.Filled.Memory,
        unselectedIcon = Icons.Outlined.Memory
    )

    data object Insights : BottomNavItem(
        route = Screen.Insights.route,
        title = "Insights",
        selectedIcon = Icons.Filled.Insights,
        unselectedIcon = Icons.Outlined.Insights
    )

    data object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val items = listOf(Home, Chat, Memories, Insights, Profile)
    }
}
