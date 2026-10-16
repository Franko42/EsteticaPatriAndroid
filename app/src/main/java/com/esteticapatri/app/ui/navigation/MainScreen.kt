package com.esteticapatri.app.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.esteticapatri.app.ui.agenda.AgendaScreen
import com.esteticapatri.app.ui.agenda.AgendaViewModel
import com.esteticapatri.app.ui.clients.ClientsScreen
import com.esteticapatri.app.ui.clients.ClientsViewModel
import com.esteticapatri.app.ui.newappointment.NewAppointmentScreen
import com.esteticapatri.app.ui.newappointment.NewAppointmentViewModel
import com.esteticapatri.app.ui.services.ServicesScreen
import com.esteticapatri.app.ui.services.ServicesViewModel
import com.esteticapatri.app.ui.settings.SettingsScreen
import com.esteticapatri.app.ui.settings.SettingsViewModel

@Composable
fun MainScreen(
    initialAppointmentId: Long? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        Screen.Agenda to Icons.Default.DateRange,
        Screen.Clients to Icons.Default.People,
        Screen.Services to Icons.Default.Spa,
        Screen.Settings to Icons.Default.Settings
    )

    val showNav = currentRoute in navItems.map { it.first.route }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 600.dp

        if (isExpanded) {
            // Adaptive NavigationRail for Tablets & Landscape
            Row(modifier = Modifier.fillMaxSize()) {
                if (showNav) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navItems.forEach { (screen, icon) ->
                            NavigationRailItem(
                                icon = { Icon(icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Agenda.route,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(Screen.Agenda.route) {
                        val agendaVm: AgendaViewModel = viewModel()
                        AgendaScreen(
                            viewModel = agendaVm,
                            onNavigateToNewAppointment = {
                                navController.navigate(Screen.NewAppointment.route)
                            }
                        )
                    }

                    composable(Screen.NewAppointment.route) {
                        val newAppointmentVm: NewAppointmentViewModel = viewModel()
                        NewAppointmentScreen(
                            viewModel = newAppointmentVm,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Clients.route) {
                        val clientsVm: ClientsViewModel = viewModel()
                        ClientsScreen(viewModel = clientsVm)
                    }

                    composable(Screen.Services.route) {
                        val servicesVm: ServicesViewModel = viewModel()
                        ServicesScreen(viewModel = servicesVm)
                    }

                    composable(Screen.Settings.route) {
                        val settingsVm: SettingsViewModel = viewModel()
                        SettingsScreen(viewModel = settingsVm)
                    }
                }
            }
        } else {
            // Compact Bottom NavigationBar for Phones
            Scaffold(
                bottomBar = {
                    if (showNav) {
                        NavigationBar {
                            navItems.forEach { (screen, icon) ->
                                NavigationBarItem(
                                    icon = { Icon(icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Agenda.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Agenda.route) {
                        val agendaVm: AgendaViewModel = viewModel()
                        AgendaScreen(
                            viewModel = agendaVm,
                            onNavigateToNewAppointment = {
                                navController.navigate(Screen.NewAppointment.route)
                            }
                        )
                    }

                    composable(Screen.NewAppointment.route) {
                        val newAppointmentVm: NewAppointmentViewModel = viewModel()
                        NewAppointmentScreen(
                            viewModel = newAppointmentVm,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Clients.route) {
                        val clientsVm: ClientsViewModel = viewModel()
                        ClientsScreen(viewModel = clientsVm)
                    }

                    composable(Screen.Services.route) {
                        val servicesVm: ServicesViewModel = viewModel()
                        ServicesScreen(viewModel = servicesVm)
                    }

                    composable(Screen.Settings.route) {
                        val settingsVm: SettingsViewModel = viewModel()
                        SettingsScreen(viewModel = settingsVm)
                    }
                }
            }
        }
    }
}
