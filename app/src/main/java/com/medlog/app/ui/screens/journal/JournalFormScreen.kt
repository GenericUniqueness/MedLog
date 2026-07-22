package com.medlog.app.ui.screens.journal

import android.app.DatePickerDialog
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.JournalViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun JournalFormScreen(
    entryId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        JournalViewModel(app.journalRepository)
    }
    val scope = rememberCoroutineScope()

    val activeProfile by app.profileRepository
        .getActive()
        .collectAsStateWithLifecycle(initialValue = null)

    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(DateUtils.todayIso()) }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var contentError by remember { mutableStateOf(false) }

    val isEditing = entryId != 0L

    LaunchedEffect(entryId) {
        if (isEditing) {
            app.journalRepository.getById(entryId).collect { entry ->
                if (entry != null) {
                    title = entry.title ?: ""
                    selectedDate = entry.date
                    selectedMood = entry.mood
                    content = entry.content
                    tags = entry.tags ?: ""
                }
            }
        }
    }

    val moodOptions = listOf(
        "great" to "\uD83D\uDE00" to Color(0xFF2E7D32),
        "good" to "\uD83D\uDE42" to Color(0xFF66BB6A),
        "okay" to "\uD83D\uDE10" to Color(0xFFF9A825),
        "bad" to "\uD83D\uDE1F" to Color(0xFFEF6C00),
        "terrible" to "\uD83D\uDE22" to Color(0xFFC62828)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Journal Entry" else "New Journal Entry",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.journal_title), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Date picker
            val calendar = remember(selectedDate) {
                try {
                    val ld = LocalDate.parse(selectedDate)
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
                value = DateUtils.formatDate(selectedDate),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.date), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
                            },
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                interactionSource = remember { MutableInteractionSource() }.also { source ->
                    LaunchedEffect(source) {
                        source.interactions.collect { /* consume to prevent ripple */ }
                    }
                }
            )

            // Mood buttons
            Text(
                text = stringResource(R.string.mood),
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moodOptions.forEach { (key, emojiColor) ->
                    val (emoji, color) = emojiColor
                    val isSelected = selectedMood == key
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(56.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        )
                                    } else {
                                        Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            CircleShape
                                        )
                                    }
                                )
                                .clickable { selectedMood = key },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(28.dp)
                            ) {
                                drawCircle(color = color)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = emoji,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Content
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    contentError = false
                },
                label = { Text(stringResource(R.string.journal_content), fontSize = 14.sp) },
                isError = contentError,
                supportingText = if (contentError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                minLines = 4,
                maxLines = 12,
                modifier = Modifier.fillMaxWidth()
            )

            // Tags
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text(stringResource(R.string.tags), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    if (content.isBlank()) {
                        contentError = true
                        return@Button
                    }
                    scope.launch {
                        val trimmedTags = tags.trim().takeIf { it.isNotBlank() }
                        if (isEditing) {
                            viewModel.updateEntry(
                                id = entryId,
                                title = title.trim().takeIf { it.isNotBlank() },
                                content = content.trim(),
                                date = selectedDate,
                                mood = selectedMood,
                                tags = trimmedTags
                            )
                        } else {
                            val profileId = activeProfile?.id ?: return@launch
                            viewModel.createEntry(
                                profileId = profileId,
                                title = title.trim().takeIf { it.isNotBlank() },
                                content = content.trim(),
                                date = selectedDate,
                                mood = selectedMood,
                                tags = trimmedTags
                            )
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.save), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}