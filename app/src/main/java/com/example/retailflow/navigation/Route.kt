package com.example.retailflow.navigation

sealed class Route(val path: String) {
    object Login : Route("login")
    object Register : Route("register")
    object RecoverPassword : Route("recover_password")
    object Dashboard : Route("dashboard")
    object Capture : Route("capture")
    object Demographics : Route("demographics")
    object History : Route("history")
    object ShiftDetail : Route("shift_detail/{shiftId}") {
        const val ARG_SHIFT_ID = "shiftId"
        fun createRoute(shiftId: Int) = "shift_detail/$shiftId"
    }
    object Profile : Route("profile")

    companion object {
        /** Pantallas sin barra superior/inferior (flujo de acceso). */
        fun isAuthRoute(route: String?) =
            route == Login.path || route == Register.path || route == RecoverPassword.path

        fun isShiftDetail(route: String?) = route != null && route.startsWith("shift_detail")
    }
}
