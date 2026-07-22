package com.medlog.app.ui.screens.medications

import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationFormScreen(
    medicationId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        com.medlog.app.viewmodel.MedicationViewModel(
            app.medicationRepository,
            app.medicationLogRepository
        )
    }
    val scope = rememberCoroutineScope()

    // Active profile (needed for creating new medications)
    val activeProfile by app.profileRepository
        .getActive()
        .collectAsStateWithLifecycle(initialValue = null)

    // Form state
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("once-daily") }
    var route by remember { mutableStateOf("oral") }
    var prescriber by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<String?>(null) }
    var endDate by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#006B5E") }
    var isActive by remember { mutableStateOf(true) }

    // Validation
    var nameError by remember { mutableStateOf(false) }
    var dosageError by remember { mutableStateOf(false) }

    // Dropdown expanded states
    var frequencyExpanded by remember { mutableStateOf(false) }
    var routeExpanded by remember { mutableStateOf(false) }

    val isEditing = medicationId != 0L

    // Load existing medication if editing
    LaunchedEffect(medicationId) {
        if (isEditing) {
            app.medicationRepository.getById(medicationId).collect { med ->
                if (med != null) {
                    name = med.name
                    dosage = med.dosage
                    frequency = med.frequency
                    route = med.route
                    prescriber = med.prescriber ?: ""
                    startDate = med.startDate
                    endDate = med.endDate
                    notes = med.notes ?: ""
                    selectedColor = med.color
                    isActive = med.isActive
                }
            }
        }
    }

    val frequencyOptions = listOf(
        "once-daily" to "Once daily",
        "twice-daily" to "Twice daily",
        "three-daily" to "Three times daily",
        "weekly" to "Weekly",
        "as-needed" to "As needed",
        "custom" to "Custom schedule"
    )

    val routeOptions = listOf(
        "oral" to "Oral",
        "topical" to "Topical",
        "injection" to "Injection",
        "inhaler" to "Inhaler",
        "other" to "Other"
    )

    val presetColors = listOf(
        "#006B5E", "#1565C0", "#6A1B9A", "#C62828",
        "#E65100", "#F9A825", "#2E7D32", "#37474F",
        "#AD1457", "#4527A0", "#00838F", "#4E342E"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Medication" else "Add Medication",
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

            // ── Name ──────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text(stringResource(R.string.medication_name), fontSize = 14.sp) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Dosage ────────────────────────────────────────────
            OutlinedTextField(
                value = dosage,
                onValueChange = {
                    dosage = it
                    dosageError = false
                },
                label = { Text(stringResource(R.string.dosage), fontSize = 14.sp) },
                isError = dosageError,
                supportingText = if (dosageError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Frequency Dropdown ────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = frequencyExpanded,
                onExpandedChange = { frequencyExpanded = it }
            ) {
                OutlinedTextField(
                    value = frequencyOptions.find { it.first == frequency }?.second ?: frequency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.frequency), fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = { frequencyExpanded = false }
                ) {
                    frequencyOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                frequency = key
                                frequencyExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Route Dropdown ────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = routeExpanded,
                onExpandedChange = { routeExpanded = it }
            ) {
                OutlinedTextField(
                    value = routeOptions.find { it.first == route }?.second ?: route,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.route), fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = routeExpanded,
                    onDismissRequest = { routeExpanded = false }
                ) {
                    routeOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                route = key
                                routeExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Prescriber ────────────────────────────────────────
            OutlinedTextField(
                value = prescriber,
                onValueChange = { prescriber = it },
                label = { Text(stringResource(R.string.prescriber), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Start Date ────────────────────────────────────────
            val startCalendar = remember(startDate) {
                startDate?.let {
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
                value = startDate?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.start_date), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val date = LocalDate.of(year, month + 1, dayOfMonth)
                                startDate = date.toString()
                            },
                            startCalendar.get(java.util.Calendar.YEAR),
                            startCalendar.get(java.util.Calendar.MONTH),
                            startCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                interactionSource = remember { MutableInteractionSource() }.also { source ->
                    LaunchedEffect(source) {
                        source.interactions.collect { /* consume to prevent ripple */ }
                    }
                }
            )

            // ── End Date (optional) ───────────────────────────────
            val endCalendar = remember(endDate) {
                endDate?.let {
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
                value = endDate?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.end_date), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val date = LocalDate.of(year, month + 1, dayOfMonth)
                                endDate = date.toString()
                            },
                            endCalendar.get(java.util.Calendar.YEAR),
                            endCalendar.get(java.util.Calendar.MONTH),
                            endCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                interactionSource = remember { MutableInteractionSource() }.also { source ->
                    LaunchedEffect(source) {
                        source.interactions.collect { /* consume to prevent ripple */ }
                    }
                }
            )

            // ── Notes ─────────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes", fontSize = 14.sp) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── Color Picker ──────────────────────────────────────
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetColors.forEach { colorHex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(colorHex))
                    } catch (_: Exception) {
                        Color.Gray
                    }
                    val isSelected = selectedColor == colorHex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.small)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        MaterialTheme.shapes.small
                                    )
                                } else {
                                    Modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        MaterialTheme.shapes.small
                                    )
                                }
                            )
                            .clickable { selectedColor = colorHex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("\u2713", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Active Switch ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isActive) "Currently taking this medication"
                        else "No longer taking this medication",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save Button ───────────────────────────────────────
            Button(
                onClick = {
                    val validName = name.isNotBlank()
                    val validDosage = dosage.isNotBlank()
                    nameError = !validName
                    dosageError = !validDosage

                    if (!validName || !validDosage) return@Button

                    val customSchedule = if (frequency == "custom") "" else null

                    scope.launch {
                        if (isEditing) {
                            viewModel.updateMedication(
                                id = medicationId,
                                name = name.trim(),
                                dosage = dosage.trim(),
                                frequency = frequency,
                                customSchedule = customSchedule,
                                route = route,
                                prescriber = prescriber.trim().takeIf { it.isNotBlank() },
                                startDate = startDate,
                                endDate = endDate,
                                notes = notes.trim().takeIf { it.isNotBlank() },
                                color = selectedColor,
                                isActive = isActive
                            )
                        } else {
                            val profileId = activeProfile?.id ?: return@launch
                            viewModel.createMedication(
                                profileId = profileId,
                                name = name.trim(),
                                dosage = dosage.trim(),
                                frequency = frequency,
                                customSchedule = customSchedule,
                                route = route,
                                prescriber = prescriber.trim().takeIf { it.isNotBlank() },
                                startDate = startDate,
                                endDate = endDate,
                                notes = notes.trim().takeIf { it.isNotBlank() },
                                color = selectedColor
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