package com.person.ally.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.person.ally.ui.screens.assessments.AssessmentDetailScreen
import com.person.ally.ui.screens.assessments.AssessmentsScreen
import com.person.ally.ui.screens.chat.ChatConversationScreen
import com.person.ally.ui.screens.chat.ChatScreen
import com.person.ally.ui.screens.home.HomeScreen
import com.person.ally.ui.screens.insights.GoalDetailScreen
import com.person.ally.ui.screens.insights.InsightDetailScreen
import com.person.ally.ui.screens.insights.InsightsScreen
import com.person.ally.ui.screens.memories.MemoriesScreen
import com.person.ally.ui.screens.memories.MemoryDetailScreen
import com.person.ally.ui.screens.onboarding.OnboardingScreen
import com.person.ally.ui.screens.profile.ContextEditScreen
import com.person.ally.ui.screens.journal.JournalEntryScreen
import com.person.ally.ui.screens.journal.JournalScreen
import com.person.ally.ui.screens.profile.ContextExportScreen
import com.person.ally.ui.screens.profile.ProfileScreen
import com.person.ally.ui.screens.settings.AiModelsScreen
import com.person.ally.ui.screens.settings.SettingsScreen
import com.person.ally.ui.screens.splash.SplashScreen

private const val ANIMATION_DURATION = 200

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIMATION_DURATION)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(ANIMATION_DURATION)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
        }
    ) {
        // Splash Screen
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(ANIMATION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(ANIMATION_DURATION)) }
        ) {
            SplashScreen(
                onSplashComplete = { isOnboarded ->
                    val destination = if (isOnboarded) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding
        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(animationSpec = tween(ANIMATION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(ANIMATION_DURATION)) }
        ) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Tab: Home (with integrated insights)
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = {
                    navController.navigate(Screen.ChatConversation.createRoute(-1L))
                },
                onNavigateToAssessments = { navController.navigate(Screen.Assessments.route) },
                onNavigateToInsight = { id -> navController.navigate(Screen.InsightDetail.createRoute(id)) },
                onNavigateToGoal = { id -> navController.navigate(Screen.GoalDetail.createRoute(id)) },
                onNavigateToInsights = { navController.navigate(Screen.Insights.route) },
                onNavigateToMemories = { navController.navigate(Screen.Memories.route) },
                onNavigateToJournal = { navController.navigate(Screen.Journal.route) }
            )
        }

        // Main Tab: Chat (conversations list)
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToConversation = { conversationId ->
                    navController.navigate(Screen.ChatConversation.createRoute(conversationId))
                },
                onNavigateToNewChat = {
                    navController.navigate(Screen.ChatConversation.createRoute(-1L))
                }
            )
        }

        // Chat Conversation Screen (actual chat interface)
        composable(
            route = Screen.ChatConversation.route,
            arguments = listOf(
                navArgument(Screen.ChatConversation.ARG_CONVERSATION_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong(Screen.ChatConversation.ARG_CONVERSATION_ID) ?: -1L
            ChatConversationScreen(
                conversationId = if (conversationId == -1L) null else conversationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMemory = { id -> navController.navigate(Screen.MemoryDetail.createRoute(id)) }
            )
        }

        // Main Tab: Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToExport = { navController.navigate(Screen.ContextExport.route) },
                onNavigateToEdit = { navController.navigate(Screen.ContextEdit.route) },
                onNavigateToAssessments = { navController.navigate(Screen.Assessments.route) },
                onNavigateToMemories = { navController.navigate(Screen.Memories.route) }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAiModels = { navController.navigate(Screen.AiModels.route) },
                onNavigateToMemories = { navController.navigate(Screen.Memories.route) }
            )
        }

        // AI Models
        composable(Screen.AiModels.route) {
            AiModelsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Memories (accessible from Settings and Home)
        composable(Screen.Memories.route) {
            MemoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMemory = { id -> navController.navigate(Screen.MemoryDetail.createRoute(id)) }
            )
        }

        // Memory Detail
        composable(
            route = Screen.MemoryDetail.route,
            arguments = listOf(navArgument(Screen.MemoryDetail.ARG_MEMORY_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getLong(Screen.MemoryDetail.ARG_MEMORY_ID) ?: return@composable
            MemoryDetailScreen(
                memoryId = memoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Insights (accessible from Home)
        composable(Screen.Insights.route) {
            InsightsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInsight = { id -> navController.navigate(Screen.InsightDetail.createRoute(id)) },
                onNavigateToGoal = { id -> navController.navigate(Screen.GoalDetail.createRoute(id)) }
            )
        }

        // Insight Detail
        composable(
            route = Screen.InsightDetail.route,
            arguments = listOf(navArgument(Screen.InsightDetail.ARG_INSIGHT_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val insightId = backStackEntry.arguments?.getLong(Screen.InsightDetail.ARG_INSIGHT_ID) ?: return@composable
            InsightDetailScreen(
                insightId = insightId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Goal Detail
        composable(
            route = Screen.GoalDetail.route,
            arguments = listOf(navArgument(Screen.GoalDetail.ARG_GOAL_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getLong(Screen.GoalDetail.ARG_GOAL_ID) ?: return@composable
            GoalDetailScreen(
                goalId = goalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Assessments
        composable(Screen.Assessments.route) {
            AssessmentsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAssessment = { id -> navController.navigate(Screen.AssessmentDetail.createRoute(id)) }
            )
        }

        // Assessment Detail
        composable(
            route = Screen.AssessmentDetail.route,
            arguments = listOf(navArgument(Screen.AssessmentDetail.ARG_ASSESSMENT_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val assessmentId = backStackEntry.arguments?.getString(Screen.AssessmentDetail.ARG_ASSESSMENT_ID) ?: return@composable
            AssessmentDetailScreen(
                assessmentId = assessmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Context Export
        composable(Screen.ContextExport.route) {
            ContextExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Context Edit
        composable(Screen.ContextEdit.route) {
            ContextEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Journal
        composable(Screen.Journal.route) {
            JournalScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntry = { id -> navController.navigate(Screen.JournalEntry.createRoute(id)) },
                onNavigateToNewEntry = { navController.navigate(Screen.JournalEntry.createRoute(-1L)) }
            )
        }

        // Journal Entry (new or edit)
        composable(
            route = Screen.JournalEntry.route,
            arguments = listOf(
                navArgument(Screen.JournalEntry.ARG_ENTRY_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong(Screen.JournalEntry.ARG_ENTRY_ID) ?: -1L
            JournalEntryScreen(
                entryId = if (entryId == -1L) null else entryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
