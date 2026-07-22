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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.ConditionNoteEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.ConditionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionDetailScreen(
    conditionId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        ConditionViewModel(app.conditionRepository, app.conditionNoteRepository)
    }

    LaunchedEffect(conditionId) {
        app.conditionRepository.getById(conditionId).collect { condition ->
            if (condition != null) {
                viewModel.selectCondition(condition)
            }
        }
    }

    val condition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    val notes by viewModel.notesForSelected.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = condition?.name ?: "Condition",
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
                                Screen.ConditionForm.createRoute(conditionId)
                            )
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        bottomBar = {
            SurfaceBar {
                Button(
                    onClick = {
                        navController.navigate(
                            Screen.ConditionNoteForm.createRoute(conditionId, noteId = null)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_note), fontSize = 14.sp)
                }
            }
        }
    ) { innerPadding ->
        if (condition == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Condition not found",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        val cond = condition!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Condition Name (large header) ──────────────────────
            Text(
                text = cond.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            // ── Severity and Status badges ─────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val severityColor = when (cond.severity) {
                    "mild" -> Color(0xFF2E7D32)
                    "moderate" -> Color(0xFFF9A825)
                    "severe" -> Color(0xFFC62828)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                FilledTonalButton(
                    onClick = { },
                    enabled = false
                ) {
                    Text(
                        text = cond.severity.replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        color = severityColor
                    )
                }

                val statusLabel = when (cond.status) {
                    "active" -> "Active"
                    "managed" -> "Managed"
                    "resolved" -> "Resolved"
                    else -> cond.status.replaceFirstChar { it.uppercase() }
                }
                val statusColor = when (cond.status) {
                    "active" -> MaterialTheme.colorScheme.primary
                    "managed" -> Color(0xFFF9A825)
                    "resolved" -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                FilledTonalButton(
                    onClick = { },
                    enabled = false
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 14.sp,
                        color = statusColor
                    )
                }
            }

            // ── Details Card ───────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!cond.diagnosedAt.isNullOrBlank()) {
                        DetailRow(
                            label = stringResource(R.string.diagnosed_at),
                            value = DateUtils.formatDate(cond.diagnosedAt)
                        )
                    }
                    if (!cond.doctor.isNullOrBlank()) {
                        DetailRow(
                            label = stringResource(R.string.diagnosing_doctor),
                            value = cond.doctor
                        )
                    }
                    if (!cond.notes.isNullOrBlank()) {
                        DetailRow(label = "Notes", value = cond.notes)
                    }
                }
            }

            // ── Notes Section ──────────────────────────────────────
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            if (notes.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_notes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                notes.forEach { note ->
                    NoteCard(
                        note = note,
                        onClick = {
                            navController.navigate(
                                Screen.ConditionNoteForm.createRoute(
                                    conditionId,
                                    note.id
                                )
                            )
                        }
                    )
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
                    text = stringResource(R.string.confirm_delete_message, cond.name),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCondition(conditionId)
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
private fun NoteCard(
    note: ConditionNoteEntity,
    onClick: () -> Unit
) {
    val moodIcon = when (note.mood) {
        "good" -> "\uD83D\uDE0A"
        "okay" -> "\uD83D\uDE10"
        "bad" -> "\uD83D\uDE1F"
        "crisis" -> "\uD83D\uDE31"
        else -> null
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date and mood row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = DateUtils.formatDate(note.date),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (moodIcon != null) {
                    Text(
                        text = moodIcon,
                        fontSize = 20.sp
                    )
                }
            }

            // Content
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp
            )

            // Pain level bar
            if (note.painLevel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pain: ${note.painLevel}/10",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(80.dp)
                    )
                    LinearProgressIndicator(
                        progress = { note.painLevel!!.toFloat() / 10f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = when {
                            note.painLevel <= 3 -> Color(0xFF2E7D32)
                            note.painLevel <= 6 -> Color(0xFFF9A825)
                            else -> Color(0xFFC62828)
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun SurfaceBar(content: @Composable () -> Unit) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            content()
        }
    }
}