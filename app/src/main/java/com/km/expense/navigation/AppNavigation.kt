package com.km.expense.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.km.expense.screens.auth.AuthScreen
import com.km.expense.screens.dashboard.DashboardScreen
import com.km.expense.screens.equipment.EquipmentTrackingScreen
import com.km.expense.screens.onboarding.OnboardingScreen
import com.km.expense.screens.progress.ProgressReportScreen
import com.km.expense.screens.splash.SplashScreen
import com.km.expense.screens.tasks.TaskManagementScreen
import com.km.expense.screens.workers.WorkerManagementScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        AppNavigationActions(navController)
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navigationActions = navigationActions)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navigationActions = navigationActions)
        }
        composable(Screen.Auth.route) {
            AuthScreen(navigationActions = navigationActions)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navigationActions = navigationActions)
        }
        composable(Screen.WorkerManagement.route) {
            WorkerManagementScreen(navigationActions = navigationActions)
        }
        composable(Screen.TaskManagement.route) {
            TaskManagementScreen(navigationActions = navigationActions)
        }
        composable(Screen.EquipmentTracking.route) {
            EquipmentTrackingScreen(navigationActions = navigationActions)
        }
        composable(Screen.ProgressReport.route) {
            ProgressReportScreen(navigationActions = navigationActions)
        }
    }
}

class AppNavigationActions(private val navController: NavHostController) {
    val navigateToSplash: () -> Unit = {
        navController.navigate(Screen.Splash.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToOnboarding: () -> Unit = {
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val navigateToAuth: () -> Unit = {
        navController.navigate(Screen.Auth.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val navigateToDashboard: () -> Unit = {
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Auth.route) { inclusive = true }
        }
    }

    val navigateToWorkerManagement: () -> Unit = {
        navController.navigate(Screen.WorkerManagement.route)
    }

    val navigateToTaskManagement: () -> Unit = {
        navController.navigate(Screen.TaskManagement.route)
    }

    val navigateToEquipmentTracking: () -> Unit = {
        navController.navigate(Screen.EquipmentTracking.route)
    }

    val navigateToProgressReport: () -> Unit = {
        navController.navigate(Screen.ProgressReport.route)
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object WorkerManagement : Screen("worker_management")
    object TaskManagement : Screen("task_management")
    object EquipmentTracking : Screen("equipment_tracking")
    object ProgressReport : Screen("progress_report")
}
