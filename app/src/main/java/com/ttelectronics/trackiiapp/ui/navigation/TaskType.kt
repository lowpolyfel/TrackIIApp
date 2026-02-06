package com.ttelectronics.trackiiapp.ui.navigation

enum class TaskType(val route: String, val title: String) {
    TravelSheet(route = "travel-sheet", title = "Seguimiento de hojas viajeras"),
    CancelOrder(route = "cancel-order", title = "Cancelar Orden"),
    Rework(route = "rework", title = "Retrabajo");

    companion object {
        fun fromRoute(route: String?): TaskType {
            return entries.firstOrNull { it.route == route } ?: TravelSheet
        }
    }
}
