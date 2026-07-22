package com.medlog.app.ui.screens.conditions

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.ConditionViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionNoteFormScreen(
    conditionId: Long,
    noteId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        ConditionViewModel(app.conditionRepository, app.conditionNoteRepository)
    }
    val scope = rememberCoroutineScope()
    val isEditing = noteId != 0L

    // Form state
    var date by remember { mutableStateOf(DateUtils.todayIso()) }
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf<String?>(null) }
    var painLevel by remember { mutableFloatStateOf(0f) }
    var showPainSlider by remember { mutableStateOf(false) }

    // Validation
    var contentError by remember { mutableStateOf(false) }

    // Load existing note if editing
    LaunchedEffect(noteId) {
        if (isEditing) {
            // Also select the condition so the ViewModel has it in _notesForSelected for updateNote
            app.conditionRepository.getById(conditionId).collect { condition ->
                if (condition != null) {
                    viewModel.selectCondition(condition)
                }
            }
            app.conditionNoteRepository.getById(noteId).collect { note ->
                if (note != null) {
                    date = note.date
                    content = note.content
                    mood = note.mood
                    painLevel = note.painLevel?.toFloat() ?: 0f
                    showPainSlider = note.mood != null
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Note" else "Add Note",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Date ────────────────────────────────────────────────
            val dateCalendar = remember(date) {
                try {
                    val ld = LocalDate.parse(date)
                    java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.YEAR, ld.year)
                        set(java.util.Calendar.MONTH, ld.monthValue - 1)
                        set(java.util.Calendar.DAY_OF_MONTH, ld.dayOfMonth)
                    }
                } catch (_: Exception) {
                    java.util.Calendar.getInstance()
                }
            }

            OutlinedTextField(
                value = DateUtils.formatDate(date),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.date), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                date = selectedDate.toString()
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

            // ── Content (required) ──────────────────────────────────
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    contentError = false
                },
                label = { Text("Content", fontSize = 14.sp) },
                placeholder = { Text("How are you feeling? Any symptoms or observations?", fontSize = 14.sp) },
                isError = contentError,
                supportingText = if (contentError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                minLines = 3,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Mood ────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.mood),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MoodButton(
                    label = "Good",
                    color = Color(0xFF2E7D32),
                    isSelected = mood == "good",
                    onClick = {
                        mood = if (mood == "good") null else "good"
                        showPainSlider = mood != null
                        if (mood == null) painLevel = 0f
                    },
                    modifier = Modifier.weight(1f)
                )
                MoodButton(
                    label = "Okay",
                    color = Color(0xFFF9A825),
                    isSelected = mood == "okay",
                    onClick = {
                        mood = if (mood == "okay") null else "okay"
                        showPainSlider = mood != null
                        if (mood == null) painLevel = 0f
                    },
                    modifier = Modifier.weight(1f)
                )
                MoodButton(
                    label = "Bad",
                    color = Color(0xFFE65100),
                    isSelected = mood == "bad",
                    onClick = {
                        mood = if (mood == "bad") null else "bad"
                        showPainSlider = mood != null
                        if (mood == null) painLevel = 0f
                    },
                    modifier = Modifier.weight(1f)
                )
                MoodButton(
                    label = "Crisis",
                    color = Color(0xFFC62828),
                    isSelected = mood == "crisis",
                    onClick = {
                        mood = if (mood == "crisis") null else "crisis"
                        showPainSlider = mood != null
                        if (mood == null) painLevel = 0f
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Pain Level (only when mood is set) ──────────────────
            if (showPainSlider) {
                Text(
                    text = "Pain Level: ${painLevel.toInt()} / 10",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = painLevel,
                    onValueChange = { painLevel = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = when {
                            painLevel <= 3f -> Color(0xFF2E7D32)
                            painLevel <= 6f -> Color(0xFFF9A825)
                            else -> Color(0xFFC62828)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("5", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("10", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save Button ─────────────────────────────────────────
            Button(
                onClick = {
                    val valid = content.isNotBlank()
                    contentError = !valid
                    if (!valid) return@Button

                    val painInt = if (showPainSlider) painLevel.toInt() else null

                    scope.launch {
                        if (isEditing) {
                            viewModel.updateNote(
                                id = noteId,
                                date = date,
                                content = content.trim(),
                                mood = mood,
                                painLevel = painInt
                            )
                        } else {
                            viewModel.addNote(
                                conditionId = conditionId,
                                content = content.trim(),
                                mood = mood,
                                painLevel = painInt
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

@Composable
private fun MoodButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold
            else androidx.compose.ui.text.font.FontWeight.Normal,
            color = contentColor
        )
    }
}