package com.medlog.app.ui.screens.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.AppointmentTypeLabels
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.AppointmentViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormScreen(
    appointmentId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        AppointmentViewModel(app.appointmentRepository)
    }
    val scope = rememberCoroutineScope()

    // Active profile (needed for creating new appointments)
    val activeProfile by app.profileRepository
        .getActive()
        .collectAsStateWithLifecycle(initialValue = null)

    // Form state
    var title by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<String?>(null) }
    var time by remember { mutableStateOf<String?>(null) }
    var duration by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("checkup") }
    var notes by remember { mutableStateOf("") }
    var reminderSet by remember { mutableStateOf(false) }

    // Validation
    var titleError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    // Dropdown expanded states
    var typeExpanded by remember { mutableStateOf(false) }

    val isEditing = appointmentId != 0L

    // Load existing appointment if editing
    LaunchedEffect(appointmentId) {
        if (isEditing) {
            app.appointmentRepository.getById(appointmentId).collect { appt ->
                if (appt != null) {
                    title = appt.title
                    doctor = appt.doctor ?: ""
                    location = appt.location ?: ""
                    date = appt.date
                    time = appt.time
                    duration = appt.duration?.toString() ?: ""
                    type = appt.type
                    notes = appt.notes ?: ""
                    reminderSet = appt.reminderSet
                }
            }
        }
    }

    val typeOptions = listOf(
        "checkup" to "Check-up",
        "followup" to "Follow-up",
        "procedure" to "Procedure",
        "lab" to "Lab Work",
        "imaging" to "Imaging",
        "other" to "Other"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Appointment" else "Add Appointment",
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

            // ── Title (required) ───────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text(stringResource(R.string.appointment_title), fontSize = 14.sp) },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Doctor ─────────────────────────────────────────────
            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = { Text(stringResource(R.string.doctor), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Location ───────────────────────────────────────────
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResource(R.string.location), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Date (required) ────────────────────────────────────
            val dateCalendar = remember(date) {
                date?.let {
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
                value = date?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.date), fontSize = 14.sp) },
                isError = dateError,
                supportingText = if (dateError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                date = selectedDate.toString()
                                dateError = false
                            },
                            dateCalendar.get(java.util.Calendar.YEAR),
                            dateCalendar.get(java.util.Calendar.MONTH),
                            dateCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                interactionSource = remember { MutableInteractionSource() }.also { source ->
                    LaunchedEffect(source) {
                        source.interactions.collect { /* consume to prevent ripple */ }
                    }
                }
            )

            // ── Time ───────────────────────────────────────────────
            val timeCalendar = remember(time) {
                time?.let {
                    try {
                        val parts = it.split(":")
                        val hour = parts[0].toInt()
                        val minute = parts[1].toInt()
                        java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, hour)
                            set(java.util.Calendar.MINUTE, minute)
                        }
                    } catch (_: Exception) {
                        java.util.Calendar.getInstance()
                    }
                } ?: java.util.Calendar.getInstance()
            }

            OutlinedTextField(
                value = time?.let { DateUtils.formatTime(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.time), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                time = String.format("%02d:%02d", hourOfDay, minute)
                            },
                            timeCalendar.get(java.util.Calendar.HOUR_OF_DAY),
                            timeCalendar.get(java.util.Calendar.MINUTE),
                            false
                        ).show()
                    },
                interactionSource = remember { MutableInteractionSource() }.also { source ->
                    LaunchedEffect(source) {
                        source.interactions.collect { /* consume to prevent ripple */ }
                    }
                }
            )

            // ── Duration ───────────────────────────────────────────
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.duration), fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // ── Type Dropdown ──────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = typeOptions.find { it.first == type }?.second
                        ?: AppointmentTypeLabels.get(type),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.appointment_type), fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    typeOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                type = key
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

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

            // ── Reminder Switch ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.set_reminder),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (reminderSet) "You will be reminded before this appointment"
                        else "No reminder will be set",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = reminderSet,
                    onCheckedChange = { reminderSet = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save Button ────────────────────────────────────────
            Button(
                onClick = {
                    val validTitle = title.isNotBlank()
                    val validDate = !date.isNullOrBlank()
                    titleError = !validTitle
                    dateError = !validDate

                    if (!validTitle || !validDate) return@Button

                    val durationMinutes = duration.toIntOrNull()

                    scope.launch {
                        if (isEditing) {
                            viewModel.updateAppointment(
                                id = appointmentId,
                                title = title.trim(),
                                doctor = doctor.trim().takeIf { it.isNotBlank() },
                                location = location.trim().takeIf { it.isNotBlank() },
                                date = date!!,
                                time = time,
                                duration = durationMinutes,
                                type = type,
                                notes = notes.trim().takeIf { it.isNotBlank() },
                                reminderSet = reminderSet
                            )
                        } else {
                            val profileId = activeProfile?.id ?: return@launch
                            viewModel.createAppointment(
                                profileId = profileId,
                                title = title.trim(),
                                doctor = doctor.trim().takeIf { it.isNotBlank() },
                                location = location.trim().takeIf { it.isNotBlank() },
                                date = date!!,
                                time = time,
                                duration = durationMinutes,
                                type = type,
                                notes = notes.trim().takeIf { it.isNotBlank() },
                                reminderSet = reminderSet
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
