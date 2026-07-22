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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.data.local.entity.MedicationLogEntity
import com.medlog.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationHistoryScreen(
    medicationId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication

    val logs by remember(medicationId) {
        app.medicationLogRepository.getByMedication(medicationId)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var medicationName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(medicationId) {
        app.medicationRepository.getById(medicationId).collect { med ->
            if (med != null) {
                medicationName = med.name
            }
        }
    }

    // Group logs by date
    val groupedLogs = remember(logs) {
        logs
            .sortedByDescending { it.takenAt }
            .groupBy { log ->
                try {
                    val dt = java.time.LocalDateTime.parse(
                        log.takenAt,
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
                    dt.toLocalDate().toString()
                } catch (_: Exception) {
                    try {
                        java.time.LocalDate.parse(log.takenAt).toString()
                    } catch (_: Exception) {
                        "Unknown"
                    }
                }
            }
            .toSortedMap(reverseOrder())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (logs.isEmpty()) {
            // ── Empty State ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No logs for this medication yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Log a dose to start building your medication history.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Medication name header ────────────────────────────
            item {
                if (medicationName != null) {
                    Text(
                        text = medicationName!!,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "${logs.size} log${if (logs.size != 1) "s" else ""} recorded",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            groupedLogs.forEach { (dateKey, dayLogs) ->
                // ── Date header ────────────────────────────────────
                item(key = "header-$dateKey") {
                    Text(
                        text = DateUtils.formatDate(dateKey),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }

                // ── Log entries for this date ──────────────────────
                items(dayLogs, key = { "log-${it.id}" }) { log ->
                    LogEntryCard(log = log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LogEntryCard(log: MedicationLogEntity) {
    val statusColor = when (log.status) {
        "taken" -> Color(0xFF2E7D32)
        "skipped" -> Color(0xFF757575)
        "missed" -> Color(0xFFC62828)
        "late" -> Color(0xFFF57F17)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusLabel = when (log.status) {
        "taken" -> "Taken"
        "skipped" -> "Skipped"
        "missed" -> "Missed"
        "late" -> "Late"
        else -> log.status.replaceFirstChar { it.uppercase() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status badge
            Badge(
                containerColor = statusColor.copy(alpha = 0.12f),
                contentColor = statusColor
            ) {
                Text(statusLabel, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Time
            Text(
                text = formatLogTime(log.takenAt),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Dose
            if (!log.dose.isNullOrBlank()) {
                Text(
                    text = log.dose,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Notes
        if (!log.notes.isNullOrBlank()) {
            Text(
                text = log.notes,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

private fun formatLogTime(takenAt: String): String {
    return try {
        val dt = java.time.LocalDateTime.parse(
            takenAt,
            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        dt.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
    } catch (_: Exception) {
        try {
            DateUtils.formatDateTime(takenAt)
        } catch (_: Exception) {
            takenAt
        }
    }
}