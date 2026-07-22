package com.medlog.app.ui.screens.medications

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.medlog.app.MedLogApplication
import com.medlog.app.data.local.entity.MedicationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationInteractionCheckerScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication

    val activeProfile by app.profileRepository
        .getActive()
        .collectAsStateWithLifecycle(initialValue = null)

    val medications by remember(activeProfile?.id) {
        app.medicationRepository.getActive(activeProfile?.id ?: 0L)
    }.collectAsState(initial = emptyList())

    val selectedIds = remember { mutableStateMapOf<Long, Boolean>() }
    var results by remember { mutableStateOf<List<Interaction>?>(null) }
    var hasChecked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Interaction Checker",
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
                }
            )
        }
    ) { innerPadding ->
        if (activeProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No profile selected",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select your medications to check for potential interactions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (medications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "No active medications found. Add medications to use the interaction checker.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(medications, key = { it.id }) { med ->
                    val isChecked = selectedIds[med.id] == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedIds[med.id] = true
                                } else {
                                    selectedIds.remove(med.id)
                                }
                                results = null
                                hasChecked = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = med.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = med.dosage,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val selectedMeds = medications.filter { selectedIds[it.id] == true }
                            results = checkInteractions(selectedMeds)
                            hasChecked = true
                        },
                        enabled = selectedIds.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Check Interactions", fontSize = 14.sp)
                    }
                }

                    if (hasChecked) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        val currentResults = results
                        if (currentResults.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2E7D32).copy(alpha = 0.08f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.width(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "No known interactions found between selected medications",
                                        fontSize = 14.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Found ${currentResults.size} potential interaction${if (currentResults.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    if (results != null) {
                        items(results.orEmpty(), key = { "${it.drug1}-${it.drug2}" }) { interaction ->
                            InteractionCard(interaction = interaction)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "This checker uses a limited local database. Always consult your healthcare provider.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InteractionCard(
    interaction: Interaction
) {
    val isSevere = interaction.severity == "severe"
    val severityColor = if (isSevere) Color(0xFFC62828) else Color(0xFFF9A825)
    val severityLabel = interaction.severity.replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSevere) Color(0xFFC62828).copy(alpha = 0.06f)
            else Color(0xFFF9A825).copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.width(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${interaction.drug1} + ${interaction.drug2}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = { },
                    enabled = false
                ) {
                    Text(
                        text = severityLabel,
                        fontSize = 13.sp,
                        color = severityColor
                    )
                }
            }
            Text(
                text = interaction.description,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Consult your doctor",
                style = MaterialTheme.typography.labelMedium,
                fontSize = 13.sp,
                color = severityColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class Interaction(
    val drug1: String,
    val drug2: String,
    val severity: String,
    val description: String
)

private fun checkInteractions(medications: List<MedicationEntity>): List<Interaction> {
    val lowerNames = medications.map { it.name.lowercase() }
    val matchedInteractions = mutableListOf<Interaction>()

    for (rule in KNOWN_INTERACTIONS) {
        val hasDrug1 = lowerNames.any { it.contains(rule.drug1) }
        val hasDrug2 = lowerNames.any { it.contains(rule.drug2) }

        if (hasDrug1 && hasDrug2) {
            val name1 = medications.first { it.name.lowercase().contains(rule.drug1) }.name
            val name2 = medications.first { it.name.lowercase().contains(rule.drug2) }.name
            // Avoid duplicate pairs
            val pairKey = listOf(name1, name2).sorted().joinToString(" + ")
            if (matchedInteractions.none { listOf(it.drug1, it.drug2).sorted().joinToString(" + ") == pairKey }) {
                matchedInteractions.add(
                    Interaction(
                        drug1 = name1,
                        drug2 = name2,
                        severity = rule.severity,
                        description = rule.description
                    )
                )
            }
        }
    }

    return matchedInteractions
}

private val KNOWN_INTERACTIONS: List<Interaction> = listOf(
    Interaction(
        drug1 = "warfarin",
        drug2 = "aspirin",
        severity = "severe",
        description = "Taking warfarin with aspirin significantly increases the risk of serious bleeding. Both drugs thin the blood and their combined effect can be dangerous."
    ),
    Interaction(
        drug1 = "warfarin",
        drug2 = "ibuprofen",
        severity = "severe",
        description = "Ibuprofen can increase the blood-thinning effect of warfarin, leading to a higher risk of bleeding complications including stomach ulcers and internal bleeding."
    ),
    Interaction(
        drug1 = "metformin",
        drug2 = "alcohol",
        severity = "moderate",
        description = "Alcohol can increase the risk of lactic acidosis when taken with metformin, a rare but serious condition. It may also affect blood sugar control."
    ),
    Interaction(
        drug1 = "ssri",
        drug2 = "maoi",
        severity = "severe",
        description = "Combining SSRIs with MAOIs can cause a life-threatening condition called serotonin syndrome, characterized by agitation, rapid heart rate, and high blood pressure."
    ),
    Interaction(
        drug1 = "amoxicillin",
        drug2 = "allopurinol",
        severity = "moderate",
        description = "Combining amoxicillin with allopurinol may increase the risk of skin rashes. Monitor for any allergic skin reactions if taking both medications."
    ),
    Interaction(
        drug1 = "atorvastatin",
        drug2 = "grapefruit",
        severity = "moderate",
        description = "Grapefruit juice can significantly increase the blood levels of atorvastatin, raising the risk of muscle pain and damage (rhabdomyolysis)."
    ),
    Interaction(
        drug1 = "lisinopril",
        drug2 = "potassium",
        severity = "severe",
        description = "Lisinopril can raise potassium levels. Taking potassium supplements together may lead to dangerously high potassium (hyperkalemia), affecting heart rhythm."
    ),
    Interaction(
        drug1 = "metoprolol",
        drug2 = "verapamil",
        severity = "moderate",
        description = "Verapamil can increase metoprolol blood levels, causing excessive slowing of heart rate and lower blood pressure. Close monitoring of heart rate is recommended."
    ),
    Interaction(
        drug1 = "omeprazole",
        drug2 = "clopidogrel",
        severity = "moderate",
        description = "Omeprazole reduces the effectiveness of clopidogrel by interfering with its activation in the body. This may decrease the anti-clotting benefit of clopidogrel."
    ),
    Interaction(
        drug1 = "digoxin",
        drug2 = "amiodarone",
        severity = "severe",
        description = "Amiodarone increases digoxin blood levels, which can lead to digoxin toxicity causing nausea, visual disturbances, and dangerous heart arrhythmias."
    ),
    Interaction(
        drug1 = "levothyroxine",
        drug2 = "calcium",
        severity = "moderate",
        description = "Calcium supplements can reduce the absorption of levothyroxine. Take levothyroxine at least 4 hours before or after calcium supplements."
    ),
    Interaction(
        drug1 = "sertraline",
        drug2 = "tramadol",
        severity = "moderate",
        description = "Both sertraline and tramadol affect serotonin levels. Their combination may increase the risk of serotonin syndrome and seizures."
    ),
    Interaction(
        drug1 = "amlodipine",
        drug2 = "simvastatin",
        severity = "moderate",
        description = "Amlodipine can increase simvastatin blood levels, raising the risk of muscle-related side effects. The simvastatin dose may need to be limited."
    ),
    Interaction(
        drug1 = "prednisone",
        drug2 = "ibuprofen",
        severity = "moderate",
        description = "Both prednisone and ibuprofen can increase the risk of stomach ulcers and gastrointestinal bleeding when taken together."
    ),
    Interaction(
        drug1 = "ciprofloxacin",
        drug2 = "theophylline",
        severity = "moderate",
        description = "Ciprofloxacin can increase theophylline blood levels, potentially leading to toxicity with symptoms like nausea, tremors, and rapid heartbeat."
    )
)