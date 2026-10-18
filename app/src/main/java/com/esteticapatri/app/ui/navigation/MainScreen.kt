package com.esteticapatri.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import com.esteticapatri.app.ui.components.AppUpdateDialog
import com.esteticapatri.app.ui.components.SplashScreen
import com.esteticapatri.app.ui.newappointment.NewAppointmentScreen
import com.esteticapatri.app.ui.newappointment.NewAppointmentViewModel
import com.esteticapatri.app.ui.services.ServicesScreen
import com.esteticapatri.app.ui.services.ServicesViewModel
import com.esteticapatri.app.ui.settings.SettingsScreen
import com.esteticapatri.app.ui.settings.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    initialAppointmentId: Long? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsVm: SettingsViewModel = viewModel()
    val updateInfo by settingsVm.updateInfo.collectAsState()
    val downloadProgress by settingsVm.downloadProgress.collectAsState()
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000) // Branded welcome moment
        showSplash = false
        // Disparar búsqueda automática de actualizaciones en segundo plano al arrancar la app
        settingsVm.checkForUpdates(userInitiated = false)
        // Comprobar y realizar la copia de seguridad diaria automática (1 vez por día, máx 3 copias)
        com.esteticapatri.app.backup.DailyBackupScheduler.checkAndRunDailyBackupNow(context)
        settingsVm.refreshLocalBackups()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            navItems.forEach { (screen, icon) ->
                                NavigationRailItem(
                                    icon = { Icon(icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentRoute == screen.route,
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
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
                            SettingsScreen(viewModel = settingsVm)
                        }
                    }
                }
            } else {
                // Compact Bottom NavigationBar for Phones
                Scaffold(
                    bottomBar = {
                        if (showNav) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                navItems.forEach { (screen, icon) ->
                                    NavigationBarItem(
                                        icon = { Icon(icon, contentDescription = screen.title) },
                                        label = { Text(screen.title, fontWeight = FontWeight.Medium) },
                                        selected = currentRoute == screen.route,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
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
                            SettingsScreen(viewModel = settingsVm)
                        }
                    }
                }
            }
        }

        // Diálogo global de actualización cuando hay una nueva versión disponible
        val currentUpdate = updateInfo
        if (currentUpdate != null && currentUpdate.isUpdateAvailable) {
            AppUpdateDialog(
                updateInfo = currentUpdate,
                downloadProgress = downloadProgress,
                onConfirmUpdate = { settingsVm.downloadAndInstallUpdate(it) },
                onDismiss = { settingsVm.dismissUpdateDialog() }
            )
        }

        // Branded Splash / Loading Overlay
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            SplashScreen(onDismiss = { showSplash = false })
        }
    }
}
