package com.example.retailflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.retailflow.data.local.AppDatabase
import com.example.retailflow.navigation.Route
import com.example.retailflow.ui.components.AppBottomBar
import com.example.retailflow.ui.components.AppTopBar
import com.example.retailflow.ui.screens.*
import com.example.retailflow.ui.theme.RetailFlowTheme
import com.example.retailflow.ui.viewmodel.AuthViewModel
import com.example.retailflow.ui.viewmodel.HistoryViewModel
import com.example.retailflow.ui.viewmodel.HistoryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RetailFlowTheme {
                RetailFlowApp()
            }
        }
    }
}

@Composable
fun RetailFlowApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = LocalContext.current.applicationContext
    val factory = remember {
        val db = AppDatabase.getInstance(context)
        HistoryViewModelFactory(db.historyDao())
    }

    val sharedHistoryViewModel: HistoryViewModel = viewModel(factory = factory)

    val showTopBar = currentRoute != Route.Login.path && currentRoute != Route.RecoverPassword.path

    // Evalúa correctamente las rutas con parámetros dinámicos
    val isSecondaryScreen = currentRoute == Route.Demographics.path ||
            (currentRoute != null && currentRoute.startsWith("shift_detail"))

    val topBarTitle = when {
        currentRoute == Route.Dashboard.path -> "Rincón Sur - Accesos"
        currentRoute == Route.Capture.path -> "Consola de Muestreo"
        currentRoute == Route.Demographics.path -> "Bitácora y Demografía"
        currentRoute == Route.History.path -> "Historial de Flujos"
        currentRoute != null && currentRoute.startsWith("shift_detail") -> "Detalle del Turno"
        currentRoute == Route.Profile.path -> "Perfil de Supervisor"
        else -> "RetailFlow"
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                AppTopBar(
                    title = topBarTitle,
                    canNavigateBack = isSecondaryScreen,
                    onBackClick = { navController.popBackStack() },
                    onActionClick = {
                        if (currentRoute == Route.Demographics.path) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Route.Demographics.path) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Login.path,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Login.path) {
                val authViewModel: AuthViewModel = viewModel()
                LoginScreen(
                    vm = authViewModel,
                    onLoginClick = {
                        navController.navigate(Route.Dashboard.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    },
                    onRecoverClick = {
                        navController.navigate(Route.RecoverPassword.path)
                    }
                )
            }

            composable(Route.RecoverPassword.path) {
                RecoverPasswordScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Route.Dashboard.path) {
                DashboardScreen(viewModel = sharedHistoryViewModel)
            }

            composable(Route.Capture.path) {
                CaptureScreen(viewModel = sharedHistoryViewModel)
            }

            composable(Route.Demographics.path) {
                DemographicsScreen(viewModel = sharedHistoryViewModel)
            }

            composable(Route.History.path) {
                HistoryScreen(
                    viewModel = sharedHistoryViewModel,
                    onShiftClick = { shiftId ->
                        navController.navigate(Route.ShiftDetail.createRoute(shiftId))
                    }
                )
            }

            composable(Route.ShiftDetail.path) { backStackEntry ->
                val shiftId = backStackEntry.arguments?.getString("shiftId")?.toIntOrNull() ?: 0
                ShiftDetailScreen(
                    shiftId = shiftId,
                    viewModel = sharedHistoryViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Route.Profile.path) {
                ProfileScreen(
                    onLogoutClick = {
                        navController.navigate(Route.Login.path) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}