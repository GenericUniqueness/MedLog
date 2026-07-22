package com.medlog.app.ui.screens.conditions

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.ConditionViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionFormScreen(
    conditionId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        ConditionViewModel(app.conditionRepository, app.conditionNoteRepository)
    }
    val scope = rememberCoroutineScope()

    // Active profile (needed for creating new conditions)
    val activeProfile by app.profileRepository
        .getActive()
        .collectAsStateWithLifecycle(initialValue = null)

    // Form state
    var name by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("moderate") }
    var status by remember { mutableStateOf("active") }
    var diagnosedAt by remember { mutableStateOf<String?>(null) }
    var doctor by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Validation
    var nameError by remember { mutableStateOf(false) }

    // Dropdown expanded states
    var severityExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val isEditing = conditionId != 0L

    // Load existing condition if editing
    LaunchedEffect(conditionId) {
        if (isEditing) {
            app.conditionRepository.getById(conditionId).collect { cond ->
                if (cond != null) {
                    name = cond.name
                    severity = cond.severity
                    status = cond.status
                    diagnosedAt = cond.diagnosedAt
                    doctor = cond.doctor ?: ""
                    notes = cond.notes ?: ""
                }
            }
        }
    }

    val severityOptions = listOf(
        "mild" to "Mild",
        "moderate" to "Moderate",
        "severe" to "Severe"
    )

    val statusOptions = listOf(
        "active" to "Active",
        "managed" to "Managed",
        "resolved" to "Resolved"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Condition" else "Add Condition",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(stringResource(R.string.cancel), fontSize = 14.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Name (required) ────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text(stringResource(R.string.condition_name), fontSize = 14.sp) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Severity Dropdown ──────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = severityExpanded,
                onExpandedChange = { severityExpanded = it }
            ) {
                OutlinedTextField(
                    value = severityOptions.find { it.first == severity }?.second ?: severity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.severity), fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = severityExpanded,
                    onDismissRequest = { severityExpanded = false }
                ) {
                    severityOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                severity = key
                                severityExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Status Dropdown ────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = statusOptions.find { it.first == status }?.second ?: status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.status), fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statusOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                status = key
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Diagnosed Date ─────────────────────────────────────
            val diagnosedCalendar = remember(diagnosedAt) {
                diagnosedAt?.let {
                    try {
                        val ld = LocalDate.parse(it)
                        java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.YEAR, ld.year)
                            set(java.util.Calendar.MONTH, ld.monthValue - 1)
                            set(java.util.Calendar.DAY_OF_MONTH, ld.dayOfMonth)
                        }
                    } catch (_: Exception) {
                        java.util.Calendar.getInstance()
                    }
                } ?: java.util.Calendar.getInstance()
            }

            OutlinedTextField(
                value = diagnosedAt?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.diagnosed_at), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val date = LocalDate.of(year, month + 1, dayOfMonth)
                                diagnosedAt = date.toString()
                            },
                            diagnosedCalendar.get(java.util.Calendar.YEAR),
                            diagnosedCalendar.get(java.util.Calendar.MONTH),
                            diagnosedCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                interactionSource = remember { MutableInteractionSource() }.also { source ->
                    LaunchedEffect(source) {
                        source.interactions.collect { /* consume to prevent ripple */ }
                    }
                }
            )

            // ── Doctor ─────────────────────────────────────────────
            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = { Text(stringResource(R.string.diagnosing_doctor), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Notes ──────────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes", fontSize = 14.sp) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── Save Button ────────────────────────────────────────
            Button(
                onClick = {
                    val validName = name.isNotBlank()
                    nameError = !validName

                    if (!validName) return@Button

                    scope.launch {
                        if (isEditing) {
                            viewModel.updateCondition(
                                id = conditionId,
                                name = name.trim(),
                                severity = severity,
                                status = status,
                                diagnosedAt = diagnosedAt,
                                doctor = doctor.trim().takeIf { it.isNotBlank() },
                                notes = notes.trim().takeIf { it.isNotBlank() }
                            )
                        } else {
                            val profileId = activeProfile?.id ?: return@launch
                            viewModel.createCondition(
                                profileId = profileId,
                                name = name.trim(),
                                severity = severity,
                                status = status,
                                diagnosedAt = diagnosedAt,
                                doctor = doctor.trim().takeIf { it.isNotBlank() },
                                notes = notes.trim().takeIf { it.isNotBlank() }
                            )
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}