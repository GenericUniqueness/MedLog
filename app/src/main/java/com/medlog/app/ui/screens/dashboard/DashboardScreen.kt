package com.medlog.app.ui.screens.dashboard

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.data.local.entity.JournalEntryEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.AppointmentTypeLabels
import com.medlog.app.util.DateUtils
import com.medlog.app.util.FrequencyLabels
import com.medlog.app.util.RouteLabels
import com.medlog.app.viewmodel.DashboardViewModel
import com.medlog.app.viewmodel.TodayMedicationInfo

@Composable
fun DashboardScreen(
    navController: NavController,
    activeProfile: ProfileEntity?
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        DashboardViewModel(
            app.medicationRepository,
            app.medicationLogRepository,
            app.conditionRepository,
            app.appointmentRepository,
            app.journalRepository
        )
    }

    val state by viewModel.dashboardState.collectAsStateWithLifecycle()

    LaunchedEffect(activeProfile?.id) {
        activeProfile?.id?.let { viewModel.loadDashboard(it) }
    }

    if (activeProfile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No profile selected",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp
            )
        }
        return
    }

    val hasData = state.todayMedications.isNotEmpty() ||
            state.upcomingAppointments.isNotEmpty() ||
            state.recentJournal.isNotEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, ${activeProfile.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (state.activeConditionsCount > 0) {
                        AssistChip(
                            onClick = { navController.navigate(Screen.Conditions.route) },
                            label = {
                                Text(
                                    text = "${state.activeConditionsCount} active condition${if (state.activeConditionsCount != 1) "s" else ""}",
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
                AdherenceIndicator(
                    percentage = state.medicationAdherence,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        // ── Empty State ────────────────────────────────────────────
        if (!hasData) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Start by adding a medication",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Track your medications, appointments, and health journal all in one place.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            onClick = {
                                navController.navigate(Screen.MedicationForm.createRoute(null))
                            },
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(text = "Add Medication", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // ── Today's Medications ─────────────────────────────────────
        if (hasData) {
            item {
                Text(
                    text = stringResource(R.string.today) + "'s Medications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (state.todayMedications.isEmpty() && hasData) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No medications scheduled for today",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(state.todayMedications, key = { it.medication.id }) { info ->
                TodayMedicationCard(
                    info = info,
                    onLog = {
                        navController.navigate(
                            Screen.MedicationLog.createRoute(info.medication.id)
                        )
                    },
                    onSkip = {
                        navController.navigate(
                            Screen.MedicationLog.createRoute(info.medication.id)
                        )
                    },
                    onTap = {
                        navController.navigate(
                            Screen.MedicationDetail.createRoute(info.medication.id)
                        )
                    }
                )
            }
        }

        // ── Upcoming Appointments ───────────────────────────────────
        if (state.upcomingAppointments.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Upcoming Appointments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.Appointments.route) },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(text = "See all", fontSize = 14.sp)
                    }
                }
            }

            items(
                state.upcomingAppointments.take(5),
                key = { it.id }
            ) { appointment ->
                AppointmentRow(
                    appointment = appointment,
                    onClick = {
                        navController.navigate(
                            Screen.AppointmentDetail.createRoute(appointment.id)
                        )
                    }
                )
            }
        }

        // ── Recent Journal ──────────────────────────────────────────
        if (state.recentJournal.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Journal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.Journal.route) },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(text = "See all", fontSize = 14.sp)
                    }
                }
            }

            items(state.recentJournal.take(3), key = { it.id }) { entry ->
                JournalRow(entry = entry)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AdherenceIndicator(
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
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 6.dp,
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "adherence",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodayMedicationCard(
    info: TodayMedicationInfo,
    onLog: () -> Unit,
    onSkip: () -> Unit,
    onTap: () -> Unit
) {
    val med = info.medication
    val medColor = try {
        Color(android.graphics.Color.parseColor(med.color))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = medColor)
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = med.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${med.dosage} · ${FrequencyLabels.get(med.frequency)} · ${RouteLabels.get(med.route)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (info.hasBeenLoggedToday) {
                Text(
                    text = when (info.lastLogStatus) {
                        "taken" -> "\u2713 Taken"
                        "late" -> "Late"
                        "skipped" -> "Skipped"
                        else -> "Logged"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 14.sp,
                    color = when (info.lastLogStatus) {
                        "taken" -> Color(0xFF2E7D32)
                        "late" -> Color(0xFFF57F17)
                        "skipped" -> Color(0xFF757575)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                Row {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.height(44.dp),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skip", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onLog,
                        modifier = Modifier.height(44.dp),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Log",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentRow(
    appointment: AppointmentEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = DateUtils.formatDate(appointment.date),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (appointment.time != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateUtils.formatTime(appointment.time),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (appointment.doctor != null) {
                    Text(
                        text = "Dr. ${appointment.doctor}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AssistChip(
                onClick = onClick,
                label = {
                    Text(
                        text = AppointmentTypeLabels.get(appointment.type),
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun JournalRow(entry: JournalEntryEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = moodEmoji(entry.mood),
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (!entry.title.isNullOrBlank()) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatDate(entry.date),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun moodEmoji(mood: String?): String = when (mood) {
    "great" -> "\uD83D\uDE00"
    "good" -> "\uD83D\uDE42"
    "okay" -> "\uD83D\uDE10"
    "bad" -> "\uD83D\uDE1F"
    "terrible" -> "\uD83D\uDE22"
    else -> "\uD83D\uDE10"
}