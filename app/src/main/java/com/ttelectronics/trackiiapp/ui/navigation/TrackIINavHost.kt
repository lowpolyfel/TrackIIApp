package com.ttelectronics.trackiiapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ttelectronics.trackiiapp.ui.screens.LoginScreen
import com.ttelectronics.trackiiapp.ui.screens.RegisterScreen
import com.ttelectronics.trackiiapp.ui.screens.TaskSelectionScreen
import com.ttelectronics.trackiiapp.ui.screens.WelcomeScreen

object TrackIIRoute {
    const val Login = "login"
    const val Register = "register"
    const val Welcome = "welcome"
    const val Tasks = "tasks"
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
            TaskSelectionScreen(onBackToLogin = { navController.navigate(TrackIIRoute.Login) })
        }
    }
}
