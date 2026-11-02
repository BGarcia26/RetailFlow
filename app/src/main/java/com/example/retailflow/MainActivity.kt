package com.example.retailflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.retailflow.data.local.AppDatabase
import com.example.retailflow.data.repository.FlowRepository
import com.example.retailflow.data.repository.UserRepository
import com.example.retailflow.navigation.AppNavGraph
import com.example.retailflow.navigation.Route
import com.example.retailflow.ui.components.AppBottomBar
import com.example.retailflow.ui.components.AppTopBar
import com.example.retailflow.ui.theme.RetailFlowTheme
import com.example.retailflow.ui.viewmodel.AppViewModelFactory
import com.example.retailflow.ui.viewmodel.AuthViewModel
import com.example.retailflow.ui.viewmodel.HistoryViewModel

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

/** Arma la "carcasa" de la app (barras + Scaffold) y crea los ViewModels compartidos. */
@Composable
fun RetailFlowApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Base de datos -> repositorios -> fábrica. remember {} evita recrearlos en cada recomposición.
    val context = LocalContext.current.applicationContext
    val factory = remember {
        val db = AppDatabase.getInstance(context)
        AppViewModelFactory(FlowRepository(db), UserRepository(db.userDao()))
    }
    // Creados aquí (scope de la Activity): TODAS las pantallas comparten la misma instancia
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    // currentRoute es null el primer instante: no se muestran barras hasta saber en qué pantalla estamos
    val showBars = currentRoute != null && !Route.isAuthRoute(currentRoute)
    val isSecondaryScreen = currentRoute == Route.Demographics.path || Route.isShiftDetail(currentRoute)

    val topBarTitle = when {
        currentRoute == Route.Dashboard.path -> "Rincón Sur - Accesos"
        currentRoute == Route.Capture.path -> "Consola de Muestreo"
        currentRoute == Route.Demographics.path -> "Bitácora y Demografía"
        currentRoute == Route.History.path -> "Historial de Flujos"
        Route.isShiftDetail(currentRoute) -> "Detalle del Turno"
        currentRoute == Route.Profile.path -> "Perfil de Supervisor"
        else -> "RetailFlow"
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showBars,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                AppTopBar(
                    title = topBarTitle,
                    canNavigateBack = isSecondaryScreen,
                    onBackClick = { navController.popBackStack() },
                    onActionClick = {
                        if (currentRoute == Route.Demographics.path) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Route.Demographics.path) { launchSingleTop = true }
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBars,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                AppBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            historyViewModel = historyViewModel,
            authViewModel = authViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
