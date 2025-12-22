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
import com.person.ally.ui.screens.chat.ChatScreen
import com.person.ally.ui.screens.home.HomeScreen
import com.person.ally.ui.screens.insights.GoalDetailScreen
import com.person.ally.ui.screens.insights.InsightDetailScreen
import com.person.ally.ui.screens.insights.InsightsScreen
import com.person.ally.ui.screens.memories.MemoriesScreen
import com.person.ally.ui.screens.memories.MemoryDetailScreen
import com.person.ally.ui.screens.onboarding.OnboardingScreen
import com.person.ally.ui.screens.profile.ContextEditScreen
import com.person.ally.ui.screens.profile.ContextExportScreen
import com.person.ally.ui.screens.profile.ProfileScreen
import com.person.ally.ui.screens.settings.AiModelsScreen
import com.person.ally.ui.screens.settings.SettingsScreen
import com.person.ally.ui.screens.splash.SplashScreen

private const val ANIMATION_DURATION = 300

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

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToAssessments = { navController.navigate(Screen.Assessments.route) },
                onNavigateToInsight = { id -> navController.navigate(Screen.InsightDetail.createRoute(id)) },
                onNavigateToGoal = { id -> navController.navigate(Screen.GoalDetail.createRoute(id)) }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToMemory = { id -> navController.navigate(Screen.MemoryDetail.createRoute(id)) }
            )
        }

        composable(Screen.Memories.route) {
            MemoriesScreen(
                onNavigateToMemory = { id -> navController.navigate(Screen.MemoryDetail.createRoute(id)) }
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen(
                onNavigateToInsight = { id -> navController.navigate(Screen.InsightDetail.createRoute(id)) },
                onNavigateToGoal = { id -> navController.navigate(Screen.GoalDetail.createRoute(id)) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToExport = { navController.navigate(Screen.ContextExport.route) },
                onNavigateToEdit = { navController.navigate(Screen.ContextEdit.route) },
                onNavigateToAssessments = { navController.navigate(Screen.Assessments.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAiModels = { navController.navigate(Screen.AiModels.route) }
            )
        }

        composable(Screen.AiModels.route) {
            AiModelsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Assessments.route) {
            AssessmentsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAssessment = { id -> navController.navigate(Screen.AssessmentDetail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AssessmentDetail.route,
            arguments = listOf(navArgument("assessmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assessmentId = backStackEntry.arguments?.getString("assessmentId") ?: return@composable
            AssessmentDetailScreen(
                assessmentId = assessmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MemoryDetail.route,
            arguments = listOf(navArgument("memoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getLong("memoryId") ?: return@composable
            MemoryDetailScreen(
                memoryId = memoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.InsightDetail.route,
            arguments = listOf(navArgument("insightId") { type = NavType.LongType })
        ) { backStackEntry ->
            val insightId = backStackEntry.arguments?.getLong("insightId") ?: return@composable
            InsightDetailScreen(
                insightId = insightId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ContextExport.route) {
            ContextExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ContextEdit.route) {
            ContextEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GoalDetail.route,
            arguments = listOf(navArgument("goalId") { type = NavType.LongType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getLong("goalId") ?: return@composable
            GoalDetailScreen(
                goalId = goalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
