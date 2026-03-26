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
import com.ttelectronics.trackiiapp.core.demo.DemoMode
import com.ttelectronics.trackiiapp.ui.screens.LoginScreen
import com.ttelectronics.trackiiapp.ui.screens.PartialScrapScreen
import com.ttelectronics.trackiiapp.ui.screens.ProductAdvanceFinalReviewScreen
import com.ttelectronics.trackiiapp.ui.screens.RegisterScreen
import com.ttelectronics.trackiiapp.ui.screens.RegisterTokenScreen
import com.ttelectronics.trackiiapp.ui.screens.ReworkReleaseScreen
import com.ttelectronics.trackiiapp.ui.screens.ScanReviewScreen
import com.ttelectronics.trackiiapp.ui.screens.ScrapOrderScreen
import com.ttelectronics.trackiiapp.ui.screens.ScannerScreen
import com.ttelectronics.trackiiapp.ui.screens.TaskDetailScreen
import com.ttelectronics.trackiiapp.ui.screens.TaskSelectionScreen
import com.ttelectronics.trackiiapp.ui.screens.WelcomeScreen
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
private fun resolvePostRegisterRoute(nextDestination: String?, isLoggedIn: Boolean): String {
    val destination = nextDestination?.trim().orEmpty()
    if (destination.isBlank()) {
        return if (isLoggedIn) TrackIIRoute.Tasks else TrackIIRoute.Welcome
    }

    return when {
        destination == TrackIIRoute.Welcome || destination == TrackIIRoute.Tasks -> destination
        destination.startsWith("scanner/") ||
            destination.startsWith("scan-review/") ||
            destination.startsWith("task/") ||
            destination.startsWith("rework-release") ||
            destination.startsWith("scrap-order") ||
            destination.startsWith("partial-scrap") ||
            destination.startsWith("product-advance-final-review") -> destination
        destination == TaskType.ProductAdvance.route -> TrackIIRoute.scannerRoute(TaskType.ProductAdvance)
        destination == TaskType.TravelSheet.route -> TrackIIRoute.scannerRoute(TaskType.TravelSheet)
        destination == TaskType.CancelOrder.route -> TrackIIRoute.scannerRoute(TaskType.CancelOrder)
        destination == TaskType.Rework.route -> TrackIIRoute.scannerRoute(TaskType.Rework)
        else -> if (isLoggedIn) TrackIIRoute.Tasks else TrackIIRoute.Welcome
    }
}

object TrackIIRoute {
    const val Login = "login"
    const val RegisterToken = "register-token"
    const val Register = "register?token={token}"
    const val Welcome = "welcome"
    const val Tasks = "tasks"
    const val Scanner = "scanner/{task}"
    const val ScanReview = "scan-review/{task}?lot={lot}&part={part}&ok={ok}&error={error}"
    const val Task = "task/{task}?lot={lot}&part={part}&qty={qty}"
    const val ReworkRelease = "rework-release?lot={lot}&part={part}"
    const val ScrapOrder = "scrap-order?lot={lot}&part={part}"
    const val PartialScrap = "partial-scrap?lot={lot}&part={part}&difference={difference}&qtyIn={qtyIn}"
    const val ProductAdvanceFinalReview = "product-advance-final-review?lot={lot}&part={part}&qtyIn={qtyIn}&scrap={scrap}&errorCodeId={errorCodeId}&errorCodeName={errorCodeName}&comments={comments}"

    fun scannerRoute(task: TaskType) = "scanner/${task.route}"

    fun scanReviewRoute(task: TaskType, lot: String, part: String, ok: Boolean, error: String = ""): String {
        return "scan-review/${task.route}?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}&ok=$ok&error=${Uri.encode(error)}"
    }

    fun taskRoute(task: TaskType, lot: String, part: String, qty: String = ""): String {
        return "task/${task.route}?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}&qty=${Uri.encode(qty)}"
    }

    fun reworkReleaseRoute(lot: String, part: String): String {
        return "rework-release?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}"
    }

    fun partialScrapRoute(lot: String, part: String, difference: Int, qtyIn: Int): String {
        return "partial-scrap?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}&difference=$difference&qtyIn=$qtyIn"
    }

    fun productAdvanceFinalReviewRoute(
        lot: String,
        part: String,
        qtyIn: Int,
        scrap: Int,
        errorCodeId: Int = 0,
        errorCodeName: String = "",
        comments: String = ""
    ): String {
        return "product-advance-final-review?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}&qtyIn=$qtyIn&scrap=$scrap&errorCodeId=$errorCodeId&errorCodeName=${Uri.encode(errorCodeName)}&comments=${Uri.encode(comments)}"
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
        modifier = modifier,
        // Agregamos estas 4 líneas con la duración de 800 milisegundos
        enterTransition = { fadeIn(animationSpec = tween(800)) },
        exitTransition = { fadeOut(animationSpec = tween(800)) },
        popEnterTransition = { fadeIn(animationSpec = tween(800)) },
        popExitTransition = { fadeOut(animationSpec = tween(800)) }
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
                onRegister = {
                    // Registro siempre corre como flujo anónimo sin bearer o sesión previa.
                    authRepository.logout()
                    navController.navigate(TrackIIRoute.RegisterToken)
                },
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
                onCreateAccount = { nextDestination ->
                    val currentSession = authRepository.sessionSnapshot()
                    val destination = resolvePostRegisterRoute(nextDestination, currentSession.isLoggedIn)
                    navController.navigate(destination) {
                        popUpTo(TrackIIRoute.RegisterToken) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToLogin = { navController.popBackStack(TrackIIRoute.Login, inclusive = false) },
                onHome = navigateHome
            )
        }
        composable(TrackIIRoute.Welcome) {
            val current = authRepository.sessionSnapshot()
            WelcomeScreen(
                onStart = { navController.navigate(TrackIIRoute.Tasks) },
                locationName = current.locationName
            )
        }
        composable(TrackIIRoute.Tasks) {
            val current = authRepository.sessionSnapshot()
            TaskSelectionScreen(
                onTaskSelected = { taskType ->
                    if (taskType == TaskType.ProductAdvance) {
                        DemoMode.activateProductAdvanceScenario(com.ttelectronics.trackiiapp.core.demo.DemoScanScenario.Success)
                    }
                    navController.navigate(TrackIIRoute.scannerRoute(taskType))
                },
                onDemoProductAdvanceSelected = { scenario ->
                    DemoMode.activateProductAdvanceScenario(scenario)
                    navController.navigate(TrackIIRoute.scannerRoute(TaskType.ProductAdvance))
                },
                onHome = navigateHome,
                onAccount = { navController.navigate(TrackIIRoute.Login) },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(TrackIIRoute.Login) {
                        popUpTo(TrackIIRoute.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
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
                onBack = {
                    navController.navigate(TrackIIRoute.Tasks) {
                        popUpTo(TrackIIRoute.Tasks) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onComplete = { lot, part, found, error ->
                    navController.navigate(TrackIIRoute.scanReviewRoute(taskType, lot, part, found, error))
                },
                onReworkTask = { lot, part ->
                    navController.navigate(TrackIIRoute.taskRoute(TaskType.Rework, lot, part))
                },
                onReworkRelease = { lot, part ->
                    navController.navigate(TrackIIRoute.reworkReleaseRoute(lot, part))
                },
                onHome = navigateHome
            )
        }
        composable(
            route = TrackIIRoute.ScanReview,
            arguments = listOf(
                navArgument("task") { nullable = false },
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" },
                navArgument("ok") { defaultValue = true },
                navArgument("error") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val taskType = TaskType.fromRoute(backStackEntry.arguments?.getString("task"))
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            val ok = backStackEntry.arguments?.getBoolean("ok") ?: true
            val error = backStackEntry.arguments?.getString("error").orEmpty()
            ScanReviewScreen(
                taskType = taskType,
                lotNumber = lot,
                partNumber = part,
                orderFound = ok,
                errorMessage = error,
                onConfirm = {
                    when (taskType) {
                        TaskType.Rework -> navController.navigate(TrackIIRoute.reworkReleaseRoute(lot, part))
                        TaskType.CancelOrder -> navController.navigate("scrap-order?lot=${Uri.encode(lot)}&part=${Uri.encode(part)}")
                        else -> navController.navigate(TrackIIRoute.taskRoute(taskType, lot, part))
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
                navArgument("part") { defaultValue = "" },
                navArgument("ok") { defaultValue = true },
                navArgument("error") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            ReworkReleaseScreen(
                lotNumber = lot,
                onReleaseSuccess = {
                    navController.navigate(TrackIIRoute.Welcome) {
                        popUpTo(TrackIIRoute.Welcome) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onContinueRework = {
                    navController.navigate(TrackIIRoute.taskRoute(TaskType.Rework, lot, part))
                },
                onHome = navigateHome
            )
        }
        composable(
            route = TrackIIRoute.ScrapOrder,
            arguments = listOf(
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            ScrapOrderScreen(
                lotNumber = lot,
                partNumber = part,
                onComplete = navigateHome,
                onBack = { navController.popBackStack() },
                onHome = navigateHome
            )
        }

        composable(
            route = TrackIIRoute.PartialScrap,
            arguments = listOf(
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" },
                navArgument("difference") { defaultValue = 0 },
                navArgument("qtyIn") { defaultValue = 0 }
            )
        ) { backStackEntry ->
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            val difference = backStackEntry.arguments?.getInt("difference") ?: 0
            val qtyIn = backStackEntry.arguments?.getInt("qtyIn") ?: 0
            PartialScrapScreen(
                lotNumber = lot,
                partNumber = part,
                difference = difference,
                qtyIn = qtyIn,
                onNavigateToReview = { codeId, codeName, comments ->
                    navController.navigate(
                        TrackIIRoute.productAdvanceFinalReviewRoute(
                            lot = lot,
                            part = part,
                            qtyIn = qtyIn,
                            scrap = difference,
                            errorCodeId = codeId,
                            errorCodeName = codeName,
                            comments = comments
                        )
                    )
                },
                onBackToEdit = {
                    navController.navigate(TrackIIRoute.taskRoute(TaskType.ProductAdvance, lot, part, qtyIn.toString())) {
                        popUpTo(TrackIIRoute.Task) { inclusive = true }
                    }
                },
                onHome = navigateHome
            )
        }

        composable(
            route = TrackIIRoute.ProductAdvanceFinalReview,
            arguments = listOf(
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" },
                navArgument("qtyIn") { defaultValue = 0 },
                navArgument("scrap") { defaultValue = 0 },
                navArgument("errorCodeId") { defaultValue = 0 },
                navArgument("errorCodeName") { defaultValue = "" },
                navArgument("comments") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            val qtyIn = backStackEntry.arguments?.getInt("qtyIn") ?: 0
            val scrap = backStackEntry.arguments?.getInt("scrap") ?: 0
            val errorCodeId = backStackEntry.arguments?.getInt("errorCodeId") ?: 0
            val errorCodeName = backStackEntry.arguments?.getString("errorCodeName").orEmpty()
            val comments = backStackEntry.arguments?.getString("comments").orEmpty()
            ProductAdvanceFinalReviewScreen(
                lotNumber = lot,
                partNumber = part,
                qtyIn = qtyIn,
                scrap = scrap,
                errorCodeId = errorCodeId,
                errorCodeName = errorCodeName,
                comments = comments,
                onCancel = {
                    navController.navigate(TrackIIRoute.Tasks) {
                        popUpTo(TrackIIRoute.Tasks) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onEdit = {
                    navController.navigate(TrackIIRoute.taskRoute(TaskType.ProductAdvance, lot, part, qtyIn.toString())) {
                        popUpTo(TrackIIRoute.Task) { inclusive = true }
                    }
                },
                onComplete = navigateHome,
                onHome = navigateHome
            )
        }

        composable(
            route = TrackIIRoute.Task,
            arguments = listOf(
                navArgument("task") { nullable = false },
                navArgument("lot") { defaultValue = "" },
                navArgument("part") { defaultValue = "" },
                navArgument("qty") { defaultValue = "" },
                navArgument("ok") { defaultValue = true },
                navArgument("error") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val taskType = TaskType.fromRoute(backStackEntry.arguments?.getString("task"))
            val lot = backStackEntry.arguments?.getString("lot").orEmpty()
            val part = backStackEntry.arguments?.getString("part").orEmpty()
            val qty = backStackEntry.arguments?.getString("qty").orEmpty()
            TaskDetailScreen(
                taskType = taskType,
                lotNumber = lot,
                partNumber = part,
                onBack = {
                    navController.navigate(TrackIIRoute.Tasks) {
                        popUpTo(TrackIIRoute.Tasks) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onComplete = navigateHome,
                onNavigateToPartialScrap = { difference, qtyIn ->
                    navController.navigate(TrackIIRoute.partialScrapRoute(lot, part, difference, qtyIn))
                },
                onNavigateToFinalReview = { qtyIn, scrap ->
                    navController.navigate(TrackIIRoute.productAdvanceFinalReviewRoute(lot, part, qtyIn, scrap))
                },
                initialQty = qty,
                onHome = navigateHome
            )
        }
    }
}
