package com.medlog.app.ui.screens.medications

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.MedicationChangeEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import com.medlog.app.util.FrequencyLabels
import com.medlog.app.util.RouteLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen(
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

    LaunchedEffect(medicationId) {
        val medication = app.medicationRepository.getById(medicationId)
        medication.collect { med ->
            if (med != null) {
                viewModel.selectMedication(med)
            }
        }
    }

    val medication by viewModel.selectedMedication.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load changes from repository directly
    val changes by remember(medicationId) {
        app.medicationRepository.getChanges(medicationId)
    }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = medication?.name ?: "Medication",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
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
                    IconButton(
                        onClick = {
                            navController.navigate(
                                Screen.MedicationForm.createRoute(medicationId)
                            )
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (medication == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Medication not found",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        val med = medication!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Name ──────────────────────────────────────────────
            Text(
                text = med.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            // ── Status chip ───────────────────────────────────────
            FilledTonalButton(
                onClick = { /* toggle status or view */ },
                enabled = false
            ) {
                Text(
                    text = if (med.isActive) "Active" else "Discontinued",
                    fontSize = 14.sp
                )
            }

            // ── Details Card ──────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(label = stringResource(R.string.dosage), value = med.dosage)
                    DetailRow(label = stringResource(R.string.frequency), value = FrequencyLabels.get(med.frequency))
                    if (med.customSchedule != null) {
                        DetailRow(label = "Custom schedule", value = med.customSchedule)
                    }
                    DetailRow(label = stringResource(R.string.route), value = RouteLabels.get(med.route))
                    if (!med.prescriber.isNullOrBlank()) {
                        DetailRow(label = stringResource(R.string.prescriber), value = med.prescriber)
                    }
                    if (med.startDate != null) {
                        DetailRow(
                            label = stringResource(R.string.start_date),
                            value = DateUtils.formatDate(med.startDate)
                        )
                    }
                    if (med.endDate != null) {
                        DetailRow(
                            label = stringResource(R.string.end_date),
                            value = DateUtils.formatDate(med.endDate)
                        )
                    } else {
                        DetailRow(label = stringResource(R.string.end_date), value = "Ongoing")
                    }
                    if (!med.notes.isNullOrBlank()) {
                        DetailRow(label = "Notes", value = med.notes)
                    }
                }
            }

            // ── Action Buttons ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate(
                            Screen.MedicationLog.createRoute(med.id)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.log_medication), fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = {
                        navController.navigate(
                            Screen.MedicationHistory.createRoute(med.id)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.width(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.medication_history), fontSize = 14.sp)
                }
            }

            // ── Changes Section ───────────────────────────────────
            if (changes.isNotEmpty()) {
                Text(
                    text = "Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                changes.forEach { change ->
                    ChangeCard(change = change)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Delete Confirmation Dialog ───────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete_title), fontSize = 16.sp) },
            text = {
                Text(
                    text = stringResource(R.string.confirm_delete_message, medication!!.name),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedication(medicationId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        stringResource(R.string.delete),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(stringResource(R.string.cancel), fontSize = 14.sp)
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ChangeCard(change: MedicationChangeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatChangeType(change.changeType),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = DateUtils.formatDate(change.changedAt),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (change.oldValue != null || change.newValue != null) {
                val changeText = buildString {
                    if (change.oldValue != null) append(change.oldValue)
                    if (change.oldValue != null && change.newValue != null) append(" \u2192 ")
                    if (change.newValue != null) append(change.newValue)
                }
                Text(
                    text = changeText,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!change.reason.isNullOrBlank()) {
                Text(
                    text = "Reason: ${change.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!change.doctor.isNullOrBlank()) {
                Text(
                    text = "Doctor: ${change.doctor}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatChangeType(type: String): String = when (type) {
    "dosage" -> "Dosage changed"
    "frequency" -> "Frequency changed"
    "added" -> "Added"
    "discontinued" -> "Discontinued"
    "replaced" -> "Replaced"
    else -> type.replaceFirstChar { it.uppercase() }
}