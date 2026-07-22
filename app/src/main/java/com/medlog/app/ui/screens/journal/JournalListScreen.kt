package com.medlog.app.ui.screens.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
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
import com.medlog.app.data.local.entity.JournalEntryEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalListScreen(
    navController: NavController,
    activeProfile: ProfileEntity?
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember {
        JournalViewModel(app.journalRepository)
    }

    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var selectedMoodFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activeProfile?.id) {
        activeProfile?.id?.let { viewModel.loadForProfile(it) }
    }

    val moodOptions = listOf(
        null to "All",
        "great" to "\uD83D\uDE00 Great",
        "good" to "\uD83D\uDE42 Good",
        "okay" to "\uD83D\uDE10 Okay",
        "bad" to "\uD83D\uDE1F Bad",
        "terrible" to "\uD83D\uDE22 Terrible"
    )

    val filteredEntries = if (selectedMoodFilter != null) {
        entries.filter { it.mood == selectedMoodFilter }
    } else {
        entries
    }

    val grouped = filteredEntries
        .sortedByDescending { it.date }
        .groupBy { it.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.journal),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.JournalForm.createRoute(null))
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mood filter chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                moodOptions.forEach { (mood, label) ->
                    SuggestionChip(
                        onClick = { selectedMoodFilter = mood },
                        label = { Text(label, fontSize = 14.sp) },
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            if (grouped.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_journal_entries),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_journal_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    grouped.forEach { (dateKey, dateEntries) ->
                        item {
                            Text(
                                text = DateUtils.formatDate(dateKey),
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(dateEntries, key = { it.id }) { entry ->
                            JournalEntryCard(
                                entry = entry,
                                onClick = {
                                    navController.navigate(
                                        Screen.JournalForm.createRoute(entry.id)
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalEntryCard(
    entry: JournalEntryEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Mood indicator
            Column(
                modifier = Modifier.width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val moodColor = moodColor(entry.mood)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .padding(top = 4.dp)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(color = moodColor)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = moodLabel(entry.mood),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = moodColor,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title?.takeIf { it.isNotBlank() }
                        ?: DateUtils.formatDate(entry.date),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Tags as chips
                if (!entry.tags.isNullOrBlank()) {
                    val tagList = entry.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    if (tagList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tagList.take(5).forEach { tag ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(tag, fontSize = 12.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                            if (tagList.size > 5) {
                                Text(
                                    text = "+${tagList.size - 5}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun moodColor(mood: String?): Color = when (mood) {
    "great" -> Color(0xFF2E7D32)
    "good" -> Color(0xFF66BB6A)
    "okay" -> Color(0xFFF9A825)
    "bad" -> Color(0xFFEF6C00)
    "terrible" -> Color(0xFFC62828)
    else -> Color(0xFF9E9E9E)
}

private fun moodLabel(mood: String?): String = when (mood) {
    "great" -> "Great"
    "good" -> "Good"
    "okay" -> "Okay"
    "bad" -> "Bad"
    "terrible" -> "Terrible"
    else -> "—"
}