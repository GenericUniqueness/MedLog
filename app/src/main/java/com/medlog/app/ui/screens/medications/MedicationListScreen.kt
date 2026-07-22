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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.FrequencyLabels
import com.medlog.app.util.RouteLabels
import com.medlog.app.viewmodel.MedicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val profileRepository = app.profileRepository
    val activeProfile by profileRepository.getActive().collectAsState(initial = null)

    val viewModel = remember {
        MedicationViewModel(app.medicationRepository, app.medicationLogRepository)
    }
    val medications by viewModel.medications.collectAsStateWithLifecycle()

    var filter by remember { mutableStateOf(MedFilter.ALL) }

    LaunchedEffect(activeProfile?.id) {
        activeProfile?.id?.let { viewModel.loadForProfile(it) }
    }

    val filteredMedications = when (filter) {
        MedFilter.ALL -> medications
        MedFilter.ACTIVE -> medications.filter { it.isActive }
        MedFilter.DISCONTINUED -> medications.filter { !it.isActive }
    }

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
                selected = filter == MedFilter.ALL,
                onClick = { filter = MedFilter.ALL },
                label = { Text(stringResource(R.string.all), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == MedFilter.ACTIVE,
                onClick = { filter = MedFilter.ACTIVE },
                label = { Text(stringResource(R.string.active), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == MedFilter.DISCONTINUED,
                onClick = { filter = MedFilter.DISCONTINUED },
                label = { Text("Discontinued", fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredMedications.isEmpty()) {
            // ── Empty State ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.no_medications),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_medications_hint),
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
                items(filteredMedications, key = { it.id }) { medication ->
                    MedicationCard(
                        medication = medication,
                        onClick = {
                            navController.navigate(
                                Screen.MedicationDetail.createRoute(medication.id)
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medication: MedicationEntity,
    onClick: () -> Unit
) {
    val medColor = try {
        Color(android.graphics.Color.parseColor(medication.color))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                drawCircle(color = medColor)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildMedicationSubtitle(medication),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!medication.isActive) {
                Text(
                    text = "Discontinued",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun buildMedicationSubtitle(med: MedicationEntity): String {
    val parts = mutableListOf<String>()
    parts.add(med.dosage)
    parts.add(FrequencyLabels.get(med.frequency))
    parts.add(RouteLabels.get(med.route))
    return parts.joinToString(" \u00B7 ")
}

private enum class MedFilter { ALL, ACTIVE, DISCONTINUED }