package com.medlog.app.ui.screens.clutter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.ClutterItemEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.util.DateUtils
import com.medlog.app.viewmodel.ClutterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClutterScreen(
    navController: androidx.navigation.NavController,
    activeProfile: ProfileEntity?
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember { ClutterViewModel(app.clutterRepository) }

    val items by viewModel.items.collectAsStateWithLifecycle()
    var newContent by remember { mutableStateOf("") }
    var editTarget by remember { mutableStateOf<ClutterItemEntity?>(null) }
    var editContent by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<ClutterItemEntity?>(null) }

    LaunchedEffect(activeProfile?.id) {
        activeProfile?.id?.let { viewModel.loadForProfile(it) }
    }

    val sortedItems = remember(items) {
        items.sortedWith(
            compareByDescending<ClutterItemEntity> { it.isPinned }
                .thenByDescending { it.createdAt }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.clutter),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Add input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newContent,
                    onValueChange = { newContent = it },
                    placeholder = { Text("Quick note...", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.FilledTonalButton(
                    onClick = {
                        if (newContent.isNotBlank()) {
                            val profileId = activeProfile?.id ?: return@FilledTonalButton
                            viewModel.addItem(
                                content = newContent.trim(),
                                profileId = profileId,
                                category = null
                            )
                            newContent = ""
                        }
                    },
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(stringResource(R.string.add), fontSize = 14.sp)
                }
            }

            if (sortedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_clutter),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.clutter_hint),
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
                    items(sortedItems, key = { it.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    deleteTarget = item
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(end = 16.dp)
                                        .then(
                                            Modifier.background(
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                        ),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        stringResource(R.string.delete),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            ClutterItemCard(
                                item = item,
                                onTap = {
                                    editTarget = item
                                    editContent = item.content
                                },
                                onTogglePin = { viewModel.togglePin(item.id) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Edit dialog
        editTarget?.let { item ->
            AlertDialog(
                onDismissRequest = { editTarget = null },
                title = {
                    Text("Edit Note", fontSize = 16.sp)
                },
                text = {
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (editContent.isNotBlank()) {
                                viewModel.updateItem(
                                    item.copy(content = editContent.trim())
                                )
                            }
                            editTarget = null
                        },
                        modifier = Modifier.height(44.dp),
                        enabled = editContent.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save), fontSize = 14.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { editTarget = null },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(stringResource(R.string.cancel), fontSize = 14.sp)
                    }
                }
            )
        }

        // Delete confirmation
        deleteTarget?.let { item ->
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = {
                    Text(stringResource(R.string.confirm_delete_title), fontSize = 16.sp)
                },
                text = {
                    Text(
                        "Delete this note? This cannot be undone.",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteItem(item.id)
                            deleteTarget = null
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
                        onClick = { deleteTarget = null },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(stringResource(R.string.cancel), fontSize = 14.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun ClutterItemCard(
    item: ClutterItemEntity,
    onTap: () -> Unit,
    onTogglePin: () -> Unit
) {
    Card(
        onClick = onTap,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!item.category.isNullOrBlank()) {
                        Badge {
                            Text(item.category, fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = DateUtils.formatDateTime(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (item.isPinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "Unpin",
                    modifier = Modifier
                        .size(44.dp)
                        .padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pin",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}