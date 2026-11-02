package com.example.retailflow.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.retailflow.ui.screens.CaptureScreen
import com.example.retailflow.ui.screens.DashboardScreen
import com.example.retailflow.ui.screens.DemographicsScreen
import com.example.retailflow.ui.screens.HistoryScreen
import com.example.retailflow.ui.screens.LoginScreen
import com.example.retailflow.ui.screens.ProfileScreen
import com.example.retailflow.ui.screens.RecoverPasswordScreen
import com.example.retailflow.ui.screens.RegisterScreen
import com.example.retailflow.ui.screens.ShiftDetailScreen
import com.example.retailflow.ui.viewmodel.AuthViewModel
import com.example.retailflow.ui.viewmodel.HistoryViewModel

private const val NAV_ANIM_MS = 350

/**
 * Mapa de navegación de TODA la app (antes vivía dentro de MainActivity).
 * Las transiciones se definen una vez aquí y se aplican a todos los destinos:
 *  - al avanzar: la pantalla nueva entra deslizándose desde la derecha con fundido
 *  - al volver (pop): el movimiento es el inverso
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    historyViewModel: HistoryViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Login.path,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(animationSpec = tween(NAV_ANIM_MS)) { fullWidth -> fullWidth / 4 } +
                fadeIn(animationSpec = tween(NAV_ANIM_MS))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(NAV_ANIM_MS)) { fullWidth -> -fullWidth / 4 } +
                fadeOut(animationSpec = tween(NAV_ANIM_MS))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(NAV_ANIM_MS)) { fullWidth -> -fullWidth / 4 } +
                fadeIn(animationSpec = tween(NAV_ANIM_MS))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(NAV_ANIM_MS)) { fullWidth -> fullWidth / 4 } +
                fadeOut(animationSpec = tween(NAV_ANIM_MS))
        }
    ) {
        composable(Route.Login.path) {
            LoginScreen(
                vm = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Route.Dashboard.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onRecoverClick = { navController.navigate(Route.RecoverPassword.path) },
                onRegisterClick = { navController.navigate(Route.Register.path) }
            )
        }

        composable(Route.Register.path) {
            RegisterScreen(
                vm = authViewModel,
                onBackClick = { navController.popBackStack() },
                onRegistered = {
                    // La cuenta nueva ya quedó con sesión iniciada: se limpia Login+Registro de la pila
                    navController.navigate(Route.Dashboard.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.RecoverPassword.path) {
            RecoverPasswordScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Route.Dashboard.path) { DashboardScreen(viewModel = historyViewModel) }

        composable(Route.Capture.path) { CaptureScreen(viewModel = historyViewModel) }

        composable(Route.Demographics.path) { DemographicsScreen(viewModel = historyViewModel) }

        composable(Route.History.path) {
            HistoryScreen(
                viewModel = historyViewModel,
                onShiftClick = { shiftId ->
                    navController.navigate(Route.ShiftDetail.createRoute(shiftId))
                }
            )
        }

        composable(Route.ShiftDetail.path) { backStackEntry ->
            val shiftId = backStackEntry.arguments
                ?.getString(Route.ShiftDetail.ARG_SHIFT_ID)
                ?.toIntOrNull() ?: 0
            ShiftDetailScreen(
                shiftId = shiftId,
                viewModel = historyViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Route.Profile.path) {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Route.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
