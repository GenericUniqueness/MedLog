package com.medlog.app.ui.screens.medications

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationStatsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication

    val activeProfile by app.profileRepository
        .getActive()
        .collectAsStateWithLifecycle(initialValue = null)

    var selectedDays by remember { mutableIntStateOf(7) }
    val dateRangeOptions = listOf(7, 30, 90)

    val medications by remember(activeProfile?.id) {
        app.medicationRepository.getActive(activeProfile?.id ?: 0L)
    }.collectAsState(initial = emptyList())

    val startDate = LocalDate.now().minusDays(selectedDays.toLong())
        .format(DateTimeFormatter.ISO_LOCAL_DATE)
    val endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val logs by remember(activeProfile?.id, selectedDays) {
        app.medicationLogRepository.getByDateRange(
            activeProfile?.id ?: 0L,
            startDate,
            endDate
        )
    }.collectAsState(initial = emptyList())

    // Calculate overall adherence
    val overallAdherence = remember(medications, logs, selectedDays) {
        calculateOverallAdherence(medications, logs, selectedDays)
    }

    // Calculate per-medication adherence
    val medicationAdherence = remember(medications, logs, selectedDays) {
        calculatePerMedicationAdherence(medications, logs, selectedDays)
    }

    // Calculate log summary counts
    val logSummary = remember(logs) {
        LogSummary(
            taken = logs.count { it.status == "taken" },
            late = logs.count { it.status == "late" },
            skipped = logs.count { it.status == "skipped" },
            missed = logs.count { it.status == "missed" }
        )
    }

    // Most consistent and needs attention
    val mostConsistent = remember(medicationAdherence) {
        medicationAdherence.maxByOrNull { it.second }
    }
    val needsAttention = remember(medicationAdherence) {
        medicationAdherence.filter { it.second < 50 }.minByOrNull { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medication Statistics",
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
        if (activeProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No profile selected",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date range filter
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateRangeOptions.forEach { days ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                            label = { Text("Last $days days", fontSize = 14.sp) }
                        )
                    }
                }
            }

            // Large circular adherence indicator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Overall Adherence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LargeAdherenceIndicator(
                            percentage = overallAdherence,
                            modifier = Modifier.size(160.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val periodLabel = when (selectedDays) {
                            7 -> "past 7 days"
                            30 -> "past 30 days"
                            else -> "past 90 days"
                        }
                        Text(
                            text = "Based on your medication logs from the $periodLabel",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Log Summary
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Log Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SummaryCount(
                                label = "Taken",
                                count = logSummary.taken,
                                color = Color(0xFF2E7D32)
                            )
                            SummaryCount(
                                label = "Late",
                                count = logSummary.late,
                                color = Color(0xFFF9A825)
                            )
                            SummaryCount(
                                label = "Skipped",
                                count = logSummary.skipped,
                                color = Color(0xFF757575)
                            )
                            SummaryCount(
                                label = "Missed",
                                count = logSummary.missed,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // Adherence Breakdown
            if (medicationAdherence.isNotEmpty()) {
                item {
                    Text(
                        text = "Adherence Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                items(medicationAdherence, key = { it.first.id }) { (med, pct) ->
                    MedicationAdherenceBar(
                        medication = med,
                        percentage = pct
                    )
                }
            }

            // Most Consistent card
            if (mostConsistent != null && medicationAdherence.size > 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2E7D32).copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Most Consistent",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "${mostConsistent.first.name} — ${mostConsistent.second}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Needs Attention card
            if (needsAttention != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFC62828).copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Needs Attention",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 12.sp,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "${needsAttention.first.name} — ${needsAttention.second}% adherence",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LargeAdherenceIndicator(
    percentage: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        percentage >= 80 -> Color(0xFF2E7D32)
        percentage >= 50 -> Color(0xFFF57F17)
        else -> Color(0xFFC62828)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Draw the circular progress using Canvas for the bigger ring
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 14.dp,
            strokeCap = StrokeCap.Round,
            trackColor = Color(0x1F000000)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = color
            )
            Text(
                text = "adherence",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MedicationAdherenceBar(
    medication: MedicationEntity,
    percentage: Int
) {
    val barColor = when {
        percentage >= 80 -> Color(0xFF2E7D32)
        percentage >= 50 -> Color(0xFFF9A825)
        else -> Color(0xFFC62828)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = barColor
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            ) {
                val trackHeight = 12.dp.toPx()
                val trackWidth = size.width
                // Track background
                drawRoundRect(
                    color = barColor.copy(alpha = 0.15f),
                    size = androidx.compose.ui.geometry.Size(trackWidth, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
                // Progress bar
                val barWidth = trackWidth * (percentage / 100f)
                if (barWidth > 0f) {
                    drawRoundRect(
                        color = barColor,
                        size = androidx.compose.ui.geometry.Size(barWidth, trackHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCount(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class LogSummary(
    val taken: Int,
    val late: Int,
    val skipped: Int,
    val missed: Int
)

private fun calculateOverallAdherence(
    medications: List<MedicationEntity>,
    logs: List<MedicationLogEntity>,
    days: Int
): Int {
    var expectedTotal = 0
    for (med in medications) {
        val dailyDoses: Double = when (med.frequency) {
            "once-daily" -> 1.0
            "twice-daily" -> 2.0
            "three-daily" -> 3.0
            "weekly" -> 1.0 / 7.0
            "as-needed" -> continue
            "custom" -> 1.0
            else -> 1.0
        }
        expectedTotal += (dailyDoses * days).toInt()
    }

    val takenCount = logs.count { it.status == "taken" || it.status == "late" }

    return if (expectedTotal > 0) {
        (takenCount * 100) / expectedTotal
    } else {
        100
    }
}

private fun calculatePerMedicationAdherence(
    medications: List<MedicationEntity>,
    logs: List<MedicationLogEntity>,
    days: Int
): List<Pair<MedicationEntity, Int>> {
    return medications.map { med ->
        val dailyDoses: Double = when (med.frequency) {
            "once-daily" -> 1.0
            "twice-daily" -> 2.0
            "three-daily" -> 3.0
            "weekly" -> 1.0 / 7.0
            "as-needed" -> return@map med to 0
            "custom" -> 1.0
            else -> 1.0
        }
        val expected = (dailyDoses * days).toInt()
        val medLogs = logs.filter { it.medicationId == med.id }
        val taken = medLogs.count { it.status == "taken" || it.status == "late" }
        val pct = if (expected > 0) (taken * 100) / expected else 100
        med to pct
    }.filter { it.second >= 0 }
}