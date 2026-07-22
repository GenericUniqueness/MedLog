package com.medlog.app.ui.screens.conditions

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
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.ConditionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionListScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val profileRepository = app.profileRepository
    val activeProfile by profileRepository.getActive().collectAsStateWithLifecycle(initialValue = null)

    val viewModel = remember {
        ConditionViewModel(app.conditionRepository, app.conditionNoteRepository)
    }
    val conditions by viewModel.conditions.collectAsStateWithLifecycle()

    var filter by remember { mutableStateOf(ConditionFilter.ALL) }

    LaunchedEffect(activeProfile?.id) {
        activeProfile?.id?.let { viewModel.loadForProfile(it) }
    }

    val filteredConditions = when (filter) {
        ConditionFilter.ALL -> conditions
        ConditionFilter.ACTIVE -> conditions.filter { it.status == "active" }
        ConditionFilter.MANAGED -> conditions.filter { it.status == "managed" }
        ConditionFilter.RESOLVED -> conditions.filter { it.status == "resolved" }
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
                selected = filter == ConditionFilter.ALL,
                onClick = { filter = ConditionFilter.ALL },
                label = { Text(stringResource(R.string.all), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == ConditionFilter.ACTIVE,
                onClick = { filter = ConditionFilter.ACTIVE },
                label = { Text(stringResource(R.string.active), fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == ConditionFilter.MANAGED,
                onClick = { filter = ConditionFilter.MANAGED },
                label = { Text("Managed", fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
            FilterChip(
                selected = filter == ConditionFilter.RESOLVED,
                onClick = { filter = ConditionFilter.RESOLVED },
                label = { Text("Resolved", fontSize = 14.sp) },
                modifier = Modifier.height(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredConditions.isEmpty()) {
            // ── Empty State ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.no_conditions),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_conditions_hint),
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
                items(filteredConditions, key = { it.id }) { condition ->
                    ConditionCard(
                        condition = condition,
                        onClick = {
                            navController.navigate(
                                Screen.ConditionDetail.createRoute(condition.id)
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
private fun ConditionCard(
    condition: ConditionEntity,
    onClick: () -> Unit
) {
    val severityColor = when (condition.severity) {
        "mild" -> Color(0xFF2E7D32)
        "moderate" -> Color(0xFFF9A825)
        "severe" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusLabel = when (condition.status) {
        "active" -> "Active"
        "managed" -> "Managed"
        "resolved" -> "Resolved"
        else -> condition.status.replaceFirstChar { it.uppercase() }
    }

    val statusColor = when (condition.status) {
        "active" -> MaterialTheme.colorScheme.primary
        "managed" -> Color(0xFFF9A825)
        "resolved" -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = condition.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Severity badge
                Text(
                    text = condition.severity.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 14.sp,
                    color = severityColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Status badge
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 14.sp,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!condition.diagnosedAt.isNullOrBlank()) {
                    Text(
                        text = DateUtils.formatDate(condition.diagnosedAt),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!condition.doctor.isNullOrBlank()) {
                    Text(
                        text = condition.doctor,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private enum class ConditionFilter { ALL, ACTIVE, MANAGED, RESOLVED }