package com.esteticapatri.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Agenda : Screen("agenda", "Agenda")
    object NewAppointment : Screen("new_appointment", "Nuevo Turno")
    object Clients : Screen("clients", "Clientas")
    object Services : Screen("services", "Servicios")
    object Settings : Screen("settings", "Ajustes")
}
