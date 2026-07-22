package com.medlog.app.ui.screens.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.ui.navigation.Screen
import com.medlog.app.util.DateUtils
import com.medlog.app.util.GenderLabels
import com.medlog.app.viewmodel.ProfileViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileFormScreen(
    profileId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val viewModel = remember { ProfileViewModel(app.profileRepository) }
    val scope = rememberCoroutineScope()

    val profiles by viewModel.profiles.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#006B5E") }
    var nameError by remember { mutableStateOf(false) }

    var genderExpanded by remember { mutableStateOf(false) }
    var bloodTypeExpanded by remember { mutableStateOf(false) }

    val isEditing = profileId != 0L

    LaunchedEffect(profileId, profiles) {
        if (isEditing) {
            val existing = profiles.find { it.id == profileId }
            if (existing != null) {
                name = existing.name
                dateOfBirth = existing.dateOfBirth
                gender = existing.gender ?: ""
                bloodType = existing.bloodType ?: ""
                allergies = existing.allergies ?: ""
                emergencyContact = existing.emergencyContact ?: ""
                notes = existing.notes ?: ""
                selectedColor = existing.color
            }
        }
    }

    val genderOptions = listOf(
        "" to "Select gender",
        "male" to "Male",
        "female" to "Female",
        "other" to "Other",
        "prefer-not-to-say" to "Prefer not to say"
    )

    val bloodTypeOptions = listOf(
        "" to "Select blood type",
        "A+" to "A+", "A-" to "A-",
        "B+" to "B+", "B-" to "B-",
        "AB+" to "AB+", "AB-" to "AB-",
        "O+" to "O+", "O-" to "O-"
    )

    val presetColors = listOf(
        "#006B5E", "#1565C0", "#6A1B9A", "#C62828",
        "#E65100", "#F9A825", "#2E7D32", "#37474F",
        "#AD1457", "#4527A0", "#00838F", "#4E342E"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_profile) else stringResource(R.string.create_profile),
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text(stringResource(R.string.profile_name), fontSize = 14.sp) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.error_empty_field), fontSize = 14.sp) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Date of birth
            val calendar = remember(dateOfBirth) {
                dateOfBirth?.let {
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
                value = dateOfBirth?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.profile_date_of_birth), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                dateOfBirth = LocalDate.of(year, month + 1, dayOfMonth).toString()
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

            // Gender dropdown
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = it }
            ) {
                OutlinedTextField(
                    value = genderOptions.find { it.first == gender }?.second
                        ?: GenderLabels.get(gender),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.profile_gender), fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                gender = key
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            // Blood type dropdown
            ExposedDropdownMenuBox(
                expanded = bloodTypeExpanded,
                onExpandedChange = { bloodTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = bloodTypeOptions.find { it.first == bloodType }?.second ?: bloodType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.profile_blood_type), fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = bloodTypeExpanded,
                    onDismissRequest = { bloodTypeExpanded = false }
                ) {
                    bloodTypeOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 14.sp) },
                            onClick = {
                                bloodType = key
                                bloodTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Allergies
            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text(stringResource(R.string.profile_allergies), fontSize = 14.sp) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Emergency contact
            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                label = { Text(stringResource(R.string.profile_emergency_contact), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.profile_notes), fontSize = 14.sp) },
                minLines = 2,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Color picker
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetColors.forEach { colorHex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(colorHex))
                    } catch (_: Exception) {
                        Color.Gray
                    }
                    val isSelected = selectedColor == colorHex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            .clickable { selectedColor = colorHex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("\u2713", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    scope.launch {
                        val finalGender = gender.takeIf { it.isNotBlank() }
                        val finalBloodType = bloodType.takeIf { it.isNotBlank() }
                        val finalAllergies = allergies.trim().takeIf { it.isNotBlank() }
                        val finalEmergencyContact = emergencyContact.trim().takeIf { it.isNotBlank() }
                        val finalNotes = notes.trim()

                        if (isEditing) {
                            viewModel.updateProfile(
                                id = profileId,
                                name = name.trim(),
                                dateOfBirth = dateOfBirth,
                                gender = finalGender,
                                bloodType = finalBloodType,
                                allergies = finalAllergies,
                                emergencyContact = finalEmergencyContact,
                                notes = finalNotes,
                                color = selectedColor
                            )
                        } else {
                            viewModel.createProfile(
                                name = name.trim(),
                                dateOfBirth = dateOfBirth,
                                gender = finalGender,
                                bloodType = finalBloodType,
                                allergies = finalAllergies,
                                emergencyContact = finalEmergencyContact,
                                notes = finalNotes,
                                color = selectedColor
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