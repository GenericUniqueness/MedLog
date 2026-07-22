package com.medlog.app.ui.screens.appointments

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.AppointmentTypeLabels
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.AppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val profileRepository = app.profileRepository
    val activeProfile by profileRepository.getActive().collectAsStateWithLifecycle(initialValue = null)

    val viewModel = remember {
        AppointmentViewModel(app.appointmentRepository)
    }
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()

    var filter by remember { mutableStateOf(AppointmentFilter.ALL) }

    LaunchedEffect(activeProfile?.id) {
        activeProfile?.id?.let { viewModel.loadForProfile(it) }
    }

    val filteredAppointments = when (filter) {
        AppointmentFilter.ALL -> appointments
        AppointmentFilter.UPCOMING -> appointments.filter { it.status == "upcoming" }
        AppointmentFilter.COMPLETED -> appointments.filter { it.status == "completed" }
        AppointmentFilter.CANCELLED -> appointments.filter { it.status == "cancelled" }
    }

    // Group by date
    val grouped = filteredAppointments
        .sortedByDescending { it.date + (it.time ?: "") }
        .groupBy { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Filter Chips ──────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = filter == AppointmentFilter.ALL,
                onClick = { filter = AppointmentFilter.ALL },
                label = { Text(stringResource(R.string.all), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == AppointmentFilter.UPCOMING,
                onClick = { filter = AppointmentFilter.UPCOMING },
                label = { Text(stringResource(R.string.upcoming), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == AppointmentFilter.COMPLETED,
                onClick = { filter = AppointmentFilter.COMPLETED },
                label = { Text(stringResource(R.string.completed), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == AppointmentFilter.CANCELLED,
                onClick = { filter = AppointmentFilter.CANCELLED },
                label = { Text(stringResource(R.string.cancelled), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredAppointments.isEmpty()) {
            // ── Empty State ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.no_appointments),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_appointments_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (date, appointmentsForDate) ->
                    item(key = "header-$date") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatDate(date),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${appointmentsForDate.size} appointment${if (appointmentsForDate.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(appointmentsForDate, key = { it.id }) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onClick = {
                                navController.navigate(
                                    Screen.AppointmentDetail.createRoute(appointment.id)
                                )
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentEntity,
    onClick: () -> Unit
) {
    val typeColor = when (appointment.type) {
        "checkup" -> MaterialTheme.colorScheme.primary
        "followup" -> Color(0xFF1565C0)
        "procedure" -> Color(0xFF6A1B9A)
        "lab" -> Color(0xFFE65100)
        "imaging" -> Color(0xFF00695C)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusColor = when (appointment.status) {
        "upcoming" -> MaterialTheme.colorScheme.primary
        "completed" -> Color(0xFF2E7D32)
        "cancelled" -> MaterialTheme.colorScheme.error
        "missed" -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusLabel = appointment.status.replaceFirstChar { it.uppercase() }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Title row with badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Type badge
                Text(
                    text = AppointmentTypeLabels.get(appointment.type),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 14.sp,
                    color = typeColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Doctor and location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!appointment.doctor.isNullOrBlank()) {
                    Text(
                        text = appointment.doctor,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (!appointment.location.isNullOrBlank()) {
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Time, duration, and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val timeParts = mutableListOf<String>()
                if (!appointment.time.isNullOrBlank()) {
                    timeParts.add(DateUtils.formatTime(appointment.time))
                }
                if (appointment.duration != null && appointment.duration > 0) {
                    timeParts.add("${appointment.duration} min")
                }
                if (timeParts.isNotEmpty()) {
                    Text(
                        text = timeParts.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // Status badge
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 14.sp,
                    color = statusColor
                )
            }
        }
    }
}

private enum class AppointmentFilter { ALL, UPCOMING, COMPLETED, CANCELLED }