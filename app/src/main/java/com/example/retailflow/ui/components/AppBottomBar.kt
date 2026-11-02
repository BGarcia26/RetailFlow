package com.example.retailflow.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.retailflow.navigation.Route

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Dashboard", Route.Dashboard.path, Icons.Default.Dashboard),
        BottomNavItem("Capturar", Route.Capture.path, Icons.Default.EditNote),
        BottomNavItem("Historial", Route.History.path, Icons.Default.History),
        BottomNavItem("Perfil", Route.Profile.path, Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // La visibilidad (Login/Crear cuenta ocultan la barra) la decide y anima MainActivity.

    NavigationBar {
        items.forEach { item ->
            // "Detalle de turno" es hija de Historial: la pestaña Historial sigue marcada
            val selected = currentRoute == item.route ||
                (item.route == Route.History.path && Route.isShiftDetail(currentRoute))

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Route.Dashboard.path) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
