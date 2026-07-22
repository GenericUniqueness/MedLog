package com.medlog.app.ui.screens.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationLogScreen(
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

    // Load medication details and active profile
    var medicationName by remember { mutableStateOf<String?>(null) }
    var medicationDosage by remember { mutableStateOf<String?>(null) }
    var profileId by remember { mutableStateOf(0L) }

    LaunchedEffect(medicationId) {
        app.medicationRepository.getById(medicationId).collect { med ->
            if (med != null) {
                medicationName = med.name
                medicationDosage = med.dosage
            }
        }
    }
    LaunchedEffect(Unit) {
        app.profileRepository.getActive().collect { profile ->
            if (profile != null) {
                profileId = profile.id
            }
        }
    }

    // Form state
    var selectedStatus by remember { mutableStateOf("taken") }
    var doseOverride by remember(medicationDosage) { mutableStateOf(medicationDosage ?: "") }
    var logNotes by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    if (saved) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Logged", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (selectedStatus == "skipped") "Medication skipped"
                    else "Medication logged as ${selectedStatus}",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Done", fontSize = 14.sp)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Medication",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Medication Name ───────────────────────────────────
            Text(
                text = medicationName ?: "Loading\u2026",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            )

            // ── Status Selection ──────────────────────────────────
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusButton(
                    label = "Taken",
                    color = Color(0xFF2E7D32),
                    isSelected = selectedStatus == "taken",
                    onClick = { selectedStatus = "taken" },
                    modifier = Modifier.weight(1f)
                )
                StatusButton(
                    label = "Late",
                    color = Color(0xFFF57F17),
                    isSelected = selectedStatus == "late",
                    onClick = { selectedStatus = "late" },
                    modifier = Modifier.weight(1f)
                )
                StatusButton(
                    label = "Skipped",
                    color = Color(0xFF757575),
                    isSelected = selectedStatus == "skipped",
                    onClick = { selectedStatus = "skipped" },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Dose Override ─────────────────────────────────────
            OutlinedTextField(
                value = doseOverride,
                onValueChange = { doseOverride = it },
                label = { Text("Dose taken", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Notes ─────────────────────────────────────────────
            OutlinedTextField(
                value = logNotes,
                onValueChange = { logNotes = it },
                label = { Text("Notes (optional)", fontSize = 14.sp) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Submit Button ─────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        viewModel.logMedication(
                            medicationId = medicationId,
                            profileId = profileId,
                            status = selectedStatus,
                            dose = doseOverride.trim().takeIf { it.isNotBlank() },
                            notes = logNotes.trim().takeIf { it.isNotBlank() }
                        )
                        saved = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = profileId != 0L
            ) {
                Text(
                    text = if (selectedStatus == "skipped") "Log as Skipped"
                    else "Log as Taken",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) color else Color.Transparent
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}