package com.medlog.app.ui.screens.sections

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
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
import com.medlog.app.viewmodel.SectionViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionEntryFormScreen(
    sectionId: Long,
    entryId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        SectionViewModel(app.sectionRepository, app.sectionEntryRepository)
    }
    val scope = rememberCoroutineScope()

    val entries by viewModel.entriesForSelected.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf(false) }

    val isEditing = entryId != 0L

    LaunchedEffect(sectionId) {
        app.profileRepository.getActive().collect { profile ->
            profile?.id?.let { viewModel.loadForProfile(it) }
        }
    }

    LaunchedEffect(sectionId) {
        val sections = app.sectionRepository.getAllOrdered(0)
        sections.collect { sectionList ->
            val section = sectionList.find { it.id == sectionId }
            if (section != null) {
                viewModel.selectSection(section)
            }
        }
    }

    LaunchedEffect(entryId, entries) {
        if (isEditing) {
            val existing = entries.find { it.id == entryId }
            if (existing != null) {
                title = existing.title
                content = existing.content ?: ""
                selectedDate = existing.date
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Entry" else "New Entry",
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

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("Title", fontSize = 14.sp) },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content", fontSize = 14.sp) },
                minLines = 4,
                maxLines = 12,
                modifier = Modifier.fillMaxWidth()
            )

            val calendar = remember(selectedDate) {
                selectedDate?.let {
                    try {
                        val ld = LocalDate.parse(it)
                        java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.YEAR, ld.year)
                            set(java.util.Calendar.MONTH, ld.monthValue - 1)
                            set(java.util.Calendar.DAY_OF_MONTH, ld.dayOfMonth)
                        }
                    } catch (_: Exception) {
                        java.util.Calendar.getInstance()
                    }
                } ?: java.util.Calendar.getInstance()
            }

            OutlinedTextField(
                value = selectedDate?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.date), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDate =
                                    LocalDate.of(year, month + 1, dayOfMonth).toString()
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    scope.launch {
                        val sortOrder = entries.size
                        if (isEditing) {
                            val existing = entries.find { it.id == entryId }
                            viewModel.updateEntry(
                                id = entryId,
                                title = title.trim(),
                                content = content.trim().takeIf { it.isNotBlank() },
                                date = selectedDate,
                                sortOrder = existing?.sortOrder ?: sortOrder
                            )
                        } else {
                            viewModel.addEntry(
                                sectionId = sectionId,
                                title = title.trim(),
                                content = content.trim().takeIf { it.isNotBlank() },
                                date = selectedDate,
                                sortOrder = sortOrder
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