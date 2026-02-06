package com.ttelectronics.trackiiapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ttelectronics.trackiiapp.ui.screens.LoginScreen
import com.ttelectronics.trackiiapp.ui.screens.RegisterScreen
import com.ttelectronics.trackiiapp.ui.screens.ScannerScreen
import com.ttelectronics.trackiiapp.ui.screens.TaskDetailScreen
import com.ttelectronics.trackiiapp.ui.screens.TaskSelectionScreen
import com.ttelectronics.trackiiapp.ui.screens.WelcomeScreen

object TrackIIRoute {
    const val Login = "login"
    const val Register = "register"
    const val Welcome = "welcome"
    const val Tasks = "tasks"
    const val Scanner = "scanner/{task}"
    const val Task = "task/{task}?lot={lot}&part={part}"

    fun scannerRoute(task: TaskType) = "scanner/${task.route}"

    fun taskRoute(task: TaskType, lot: String, part: String): String {
        return "task/${task.route}?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}"
    }
}

@Composable
fun TrackIINavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = TrackIIRoute.Login,
        modifier = modifier
    ) {
        composable(TrackIIRoute.Login) {
            LoginScreen(
                onLogin = { navController.navigate(TrackIIRoute.Welcome) },
                onRegister = { navController.navigate(TrackIIRoute.Register) }
            )
        }
        composable(TrackIIRoute.Register) {
            RegisterScreen(
                onCreateAccount = { navController.navigate(TrackIIRoute.Welcome) },
                onBackToLogin = { navController.popBackStack(TrackIIRoute.Login, inclusive = false) }
            )
        }
        composable(TrackIIRoute.Welcome) {
            WelcomeScreen(onStart = { navController.navigate(TrackIIRoute.Tasks) })
        }
        composable(TrackIIRoute.Tasks) {
            TaskSelectionScreen(
                onBackToLogin = { navController.navigate(TrackIIRoute.Login) },
                onTaskSelected = { taskType ->
                    navController.navigate(TrackIIRoute.scannerRoute(taskType))
                }
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
                    navController.navigate(TrackIIRoute.taskRoute(taskType, lot, part))
                }
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}
