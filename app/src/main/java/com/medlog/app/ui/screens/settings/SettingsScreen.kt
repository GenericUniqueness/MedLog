package com.medlog.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.viewmodel.SettingsViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    activeProfile: ProfileEntity?
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val scope = rememberCoroutineScope()
    val viewModel = remember {
        SettingsViewModel(app.appSettingRepository, app.profileRepository)
    }

    val notificationEnabled by viewModel.notificationEnabled.collectAsStateWithLifecycle()
    val reminderLeadMinutes by viewModel.reminderLeadMinutes.collectAsStateWithLifecycle()

    LaunchedEffect(activeProfile?.id) {
        activeProfile?.id?.let { viewModel.loadSettings(it) }
    }

    var reminderExpanded by remember { mutableStateOf(false) }
    var showClearFirstConfirm by remember { mutableStateOf(false) }
    var showClearSecondConfirm by remember { mutableStateOf(false) }

    val reminderOptions = listOf(
        15 to "15 min",
        30 to "30 min",
        60 to "1 hour",
        120 to "2 hours"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile section
            SectionHeader(title = "Profile")
            SettingsRow(
                title = "Profile",
                subtitle = "View and edit your profile",
                onClick = {
                    val pid = activeProfile?.id ?: return@SettingsRow
                    navController.navigate(Screen.ProfileForm.createRoute(pid))
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Notification section
            SectionHeader(title = stringResource(R.string.notification_settings))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Medication and appointment reminders",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { viewModel.setNotificationEnabled(it) }
                )
            }

            // Reminder lead time
            if (notificationEnabled) {
                ExposedDropdownMenuBox(
                    expanded = reminderExpanded,
                    onExpandedChange = { reminderExpanded = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reminder Lead Time",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = reminderOptions.find { it.first == reminderLeadMinutes }?.second
                                ?: "$reminderLeadMinutes min",
                            fontSize = 14.sp
                        )
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderExpanded)
                    }
                    ExposedDropdownMenu(
                        expanded = reminderExpanded,
                        onDismissRequest = { reminderExpanded = false }
                    ) {
                        reminderOptions.forEach { (minutes, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 14.sp) },
                                onClick = {
                                    viewModel.setReminderLeadMinutes(minutes)
                                    reminderExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Data management section
            SectionHeader(title = stringResource(R.string.data_management))
            SettingsRow(
                title = stringResource(R.string.export_pdf),
                subtitle = "Export your health data as a PDF",
                onClick = {
                    scope.launch {
                        android.widget.Toast
                            .makeText(context, "Export feature — see ExportScreen", android.widget.Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Clear all data
            Button(
                onClick = { showClearFirstConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(R.string.clear_all_data),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onError
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // First confirmation
        if (showClearFirstConfirm) {
            AlertDialog(
                onDismissRequest = { showClearFirstConfirm = false },
                title = {
                    Text(stringResource(R.string.clear_all_data), fontSize = 16.sp)
                },
                text = {
                    Text(
                        stringResource(R.string.clear_data_confirmation),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearFirstConfirm = false
                            showClearSecondConfirm = true
                        },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            "Yes, continue",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearFirstConfirm = false },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(stringResource(R.string.cancel), fontSize = 14.sp)
                    }
                }
            )
        }

        // Second confirmation
        if (showClearSecondConfirm) {
            AlertDialog(
                onDismissRequest = { showClearSecondConfirm = false },
                title = {
                    Text("Are you absolutely sure?", fontSize = 16.sp)
                },
                text = {
                    Text(
                        "This action is irreversible. All profiles and their data will be permanently deleted.",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllData()
                            showClearSecondConfirm = false
                        },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            "Delete everything",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearSecondConfirm = false },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(stringResource(R.string.cancel), fontSize = 14.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}