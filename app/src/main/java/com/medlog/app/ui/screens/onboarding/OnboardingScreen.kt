package com.medlog.app.ui.screens.onboarding

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medlog.app.R
import com.medlog.app.util.GenderLabels
import com.medlog.app.viewmodel.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    profileViewModel: ProfileViewModel,
    onProfileCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val genderOptions = listOf(
        "" to "Select gender",
        "male" to "Male",
        "female" to "Female",
        "other" to "Other",
        "prefer-not-to-say" to "Prefer not to say"
    )

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // App icon
        Icon(
            Icons.Default.Medication,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "MedLog",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Section label
        Text(
            text = stringResource(R.string.onboarding_create_profile),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

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

        Spacer(modifier = Modifier.height(8.dp))

        // Date of birth
        val dobDisplay = dateOfBirth?.let {
            try {
                LocalDate.parse(it).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            } catch (_: Exception) { it }
        } ?: ""

        OutlinedTextField(
            value = dobDisplay,
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

        Spacer(modifier = Modifier.height(8.dp))

        // Gender dropdown
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = it }
        ) {
            val genderDisplay = genderOptions.find { it.first == gender }?.second
                ?: GenderLabels.get(gender)
            OutlinedTextField(
                value = genderDisplay,
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

        Spacer(modifier = Modifier.height(32.dp))

        // Get Started button
        Button(
            onClick = {
                if (name.isBlank()) {
                    nameError = true
                    return@Button
                }
                scope.launch {
                    profileViewModel.createProfile(
                        name = name.trim(),
                        dateOfBirth = dateOfBirth,
                        gender = gender.takeIf { it.isNotBlank() },
                        bloodType = null,
                        allergies = null,
                        emergencyContact = null,
                        notes = "",
                        color = "#006B5E"
                    )
                    onProfileCreated()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}