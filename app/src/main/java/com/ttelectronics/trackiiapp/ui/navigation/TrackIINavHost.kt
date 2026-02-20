package com.ttelectronics.trackiiapp.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.screens.LoginScreen
import com.ttelectronics.trackiiapp.ui.screens.RegisterScreen
import com.ttelectronics.trackiiapp.ui.screens.RegisterTokenScreen
import com.ttelectronics.trackiiapp.ui.screens.ReworkReleaseScreen
import com.ttelectronics.trackiiapp.ui.screens.ScanReviewScreen
import com.ttelectronics.trackiiapp.ui.screens.ScannerScreen
import com.ttelectronics.trackiiapp.ui.screens.TaskDetailScreen
import com.ttelectronics.trackiiapp.ui.screens.TaskSelectionScreen
import com.ttelectronics.trackiiapp.ui.screens.WelcomeScreen

object TrackIIRoute {
    const val Login = "login"
    const val RegisterToken = "register-token"
    const val Register = "register?token={token}"
    const val Welcome = "welcome"
    const val Tasks = "tasks"
    const val Scanner = "scanner/{task}"
    const val ScanReview = "scan-review/{task}?lot={lot}&part={part}"
    const val Task = "task/{task}?lot={lot}&part={part}"
    const val ReworkRelease = "rework-release?lot={lot}&part={part}"

    fun scannerRoute(task: TaskType) = "scanner/${task.route}"

    fun scanReviewRoute(task: TaskType, lot: String, part: String): String {
        return "scan-review/${task.route}?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}"
    }

    fun taskRoute(task: TaskType, lot: String, part: String): String {
        return "task/${task.route}?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}"
    }

    fun reworkReleaseRoute(lot: String, part: String): String {
        return "rework-release?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}"
    }
}

@Composable
fun TrackIINavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val authRepository = ServiceLocator.authRepository(context)
    val session = authRepository.sessionSnapshot()

    NavHost(
        navController = navController,
        startDestination = if (session.isLoggedIn) TrackIIRoute.Welcome else TrackIIRoute.Login,
        modifier = modifier
    ) {
        val navigateHome = {
            navController.navigate(TrackIIRoute.Welcome) {
                popUpTo(TrackIIRoute.Login) { inclusive = false }
                launchSingleTop = true
            }
        }
        composable(TrackIIRoute.Login) {
            LoginScreen(
                onLogin = {
                    navController.navigate(TrackIIRoute.Welcome) {
                        popUpTo(TrackIIRoute.Login) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(TrackIIRoute.RegisterToken) },
                onHome = navigateHome
            )
        }
        composable(TrackIIRoute.RegisterToken) {
            RegisterTokenScreen(
                onContinue = { token -> navController.navigate("register?token=${Uri.encode(token)}") },
                onBack = { navController.popBackStack(TrackIIRoute.Login, inclusive = false) },
                onHome = navigateHome
            )
        }
        composable(
            route = TrackIIRoute.Register,
            arguments = listOf(navArgument("token") { defaultValue = "" })
        ) { backStackEntry ->
            RegisterScreen(
                tokenCode = backStackEntry.arguments?.getString("token").orEmpty(),
                onCreateAccount = { navController.popBackStack(TrackIIRoute.Login, inclusive = false) },
                onBackToLogin = { navController.popBackStack(TrackIIRoute.Login, inclusive = false) },
                onHome = navigateHome
            )
        }
        composable(TrackIIRoute.Welcome) {
            val current = authRepository.sessionSnapshot()
            WelcomeScreen(
                onStart = { navController.navigate(TrackIIRoute.Tasks) },
                userName = current.username
            )
        }
        composable(TrackIIRoute.Tasks) {
            val current = authRepository.sessionSnapshot()
            TaskSelectionScreen(
                onTaskSelected = { taskType ->
                    navController.navigate(TrackIIRoute.scannerRoute(taskType))
                },
                onHome = navigateHome,
                onAccount = { navController.navigate(TrackIIRoute.Login) },
                username = current.username,
                locationName = current.locationName,
                deviceName = current.deviceName
            )
        }
        composable(
            route = TrackIIRoute.Scanner,
            arguments = listOf(navArgument("task") { nullable = false })
        ) { backStackEntry ->
            val taskType = TaskType.fromRoute(backStackEntry.arguments?.getString("task"))
            ScannerScreen(
                taskType = taskType,
                onBack = { navController.popBackStack() },
                onComplete = { lot, part ->
                    navController.navigate(TrackIIRoute.scanReviewRoute(taskType, lot, part))
                },
                onHome = navigateHome
            )
        }
        composable(
            route = TrackIIRoute.ScanReview,
            arguments = listOf(
                navArgument("task") { nullable = false },
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val taskType = TaskType.fromRoute(backStackEntry.arguments?.getString("task"))
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            ScanReviewScreen(
                lotNumber = lot,
                partNumber = part,
                onConfirm = {
                    if (taskType == TaskType.Rework) {
                        navController.navigate(TrackIIRoute.reworkReleaseRoute(lot, part))
                    } else {
                        navController.navigate(TrackIIRoute.taskRoute(taskType, lot, part))
                    }
                },
                onRescan = {
                    navController.navigate(TrackIIRoute.scannerRoute(taskType)) {
                        popUpTo(TrackIIRoute.ScanReview) { inclusive = true }
                    }
                },
                onHome = navigateHome
            )
        }
        composable(
            route = TrackIIRoute.ReworkRelease,
            arguments = listOf(
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            ReworkReleaseScreen(
                onRelease = navigateHome,
                onContinueRework = {
                    navController.navigate(TrackIIRoute.taskRoute(TaskType.Rework, lot, part))
                },
                onHome = navigateHome
            )
        }
        composable(
            route = TrackIIRoute.Task,
            arguments = listOf(
                navArgument("task") { nullable = false },
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val taskType = TaskType.fromRoute(backStackEntry.arguments?.getString("task"))
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            TaskDetailScreen(
                taskType = taskType,
                lotNumber = lot,
                partNumber = part,
                onBack = { navController.popBackStack() },
                onComplete = navigateHome,
                onHome = navigateHome
            )
        }
    }
}
