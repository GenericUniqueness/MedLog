package com.medlog.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.medlog.app.MedLogApplication
import com.medlog.app.ui.navigation.MedLogNavGraph
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.ui.theme.MedLogTheme
import com.medlog.app.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedLogTheme {
                MainContent()
            }
        }
    }
}

private data class BottomNavDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomNavDestinations = listOf(
    BottomNavDestination(Screen.Dashboard, "Home", Icons.Default.Home),
    BottomNavDestination(Screen.Medications, "Meds", Icons.Default.Medication),
    BottomNavDestination(Screen.Conditions, "Conditions", Icons.Outlined.EditCalendar),
    BottomNavDestination(Screen.Appointments, "Appts", Icons.Outlined.EditCalendar),
    BottomNavDestination(Screen.Journal, "Journal", Icons.Default.MenuBook),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent() {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val profileViewModel = remember { ProfileViewModel(app.profileRepository) }

    val profiles by profileViewModel.profiles.collectAsStateWithLifecycle()
    val activeProfile by profileViewModel.activeProfile.collectAsStateWithLifecycle()
    val profileCount by profileViewModel.count.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Notification permission request (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* User can enable later in Settings */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val mainRoutes = bottomNavDestinations.map { it.screen.route } +
        listOf(Screen.Files.route, Screen.Sections.route, Screen.Clutter.route, Screen.Settings.route)

    val showBottomNav = profileCount > 0 && currentRoute in mainRoutes
    val showTopBar = profileCount > 0 && currentRoute != Screen.Onboarding.route

    // "More" overflow menu state
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = activeProfile?.name?.let { "MedLog — $it" } ?: "MedLog",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Screen.ProfileList.route) }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Switch profile")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Files") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.Files.route)
                                },
                                leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Sections") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.Sections.route)
                                },
                                leadingIcon = { Icon(Icons.Default.Description, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Scratch Pad") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.Clutter.route)
                                },
                                leadingIcon = { Icon(Icons.Default.NoteAdd, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Med Stats") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.MedicationStats.route)
                                },
                                leadingIcon = { Icon(Icons.Default.BarChart, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Interactions") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.MedicationInteractionChecker.route)
                                },
                                leadingIcon = { Icon(Icons.Default.Warning, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Screen.Settings.route)
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, null) }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavDestinations.forEach { dest ->
                        NavigationBarItem(
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            selected = currentRoute == dest.screen.route,
                            onClick = {
                                if (currentRoute != dest.screen.route) {
                                    navController.navigate(dest.screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomNav) {
                when (currentRoute) {
                    Screen.Medications.route -> FloatingActionButton(
                        onClick = { navController.navigate(Screen.MedicationForm.createRoute(null)) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "Add medication") }
                    Screen.Conditions.route -> FloatingActionButton(
                        onClick = { navController.navigate(Screen.ConditionForm.createRoute(null)) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "Add condition") }
                    Screen.Appointments.route -> FloatingActionButton(
                        onClick = { navController.navigate(Screen.AppointmentForm.createRoute(null)) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "Add appointment") }
                    Screen.Journal.route -> FloatingActionButton(
                        onClick = { navController.navigate(Screen.JournalForm.createRoute(null)) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "New entry") }
                    Screen.Files.route -> FloatingActionButton(
                        onClick = { /* File picker triggered by FileListScreen top bar */ },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "Upload file") }
                    Screen.Sections.route -> FloatingActionButton(
                        onClick = { navController.navigate(Screen.SectionForm.createRoute(null)) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "Add section") }
                    Screen.Clutter.route -> FloatingActionButton(
                        onClick = { /* Add handled inline by ClutterScreen */ },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "Add note") }
                    // Dashboard and Settings have no FAB
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            MedLogNavGraph(
                navController = navController,
                profileCount = profileCount,
                activeProfile = activeProfile,
                onProfileCreated = { /* Refresh via Flow */ }
            )
        }
    }
}