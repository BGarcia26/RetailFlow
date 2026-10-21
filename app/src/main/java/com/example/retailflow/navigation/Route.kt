package com.example.retailflow.navigation

sealed class Route(val path: String) {
    object Login : Route("login")
    object RecoverPassword : Route("recover_password")
    object Dashboard : Route("dashboard")
    object Capture : Route("capture")
    object Demographics : Route("demographics")
    object History : Route("history")
    object ShiftDetail : Route("shift_detail/{shiftId}") {
        fun createRoute(shiftId: Int) = "shift_detail/$shiftId"
    }
    object Profile : Route("profile")
}