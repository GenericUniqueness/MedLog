package com.medlog.app.ui.screens.export

import android.content.Intent
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medlog.app.MedLogApplication
import com.medlog.app.R
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.util.AppointmentTypeLabels
import com.medlog.app.util.DateUtils
import com.medlog.app.util.FileHelper
import com.medlog.app.util.FrequencyLabels
import com.medlog.app.util.RouteLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    activeProfile: ProfileEntity?
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedLogApplication
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var exportMedications by remember { mutableStateOf(true) }
    var exportConditions by remember { mutableStateOf(true) }
    var exportAppointments by remember { mutableStateOf(true) }
    var exportJournal by remember { mutableStateOf(true) }
    var exportAttachments by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportedFile by remember { mutableStateOf<File?>(null) }

    // Show share snackbar when export completes
    LaunchedEffect(exportedFile) {
        val file = exportedFile ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = context.getString(R.string.export_complete),
            actionLabel = "Share",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            val uri = FileHelper.getShareUri(context, file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        }
        exportedFile = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.export_pdf),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (activeProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No profile selected", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Exporting data for:",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeProfile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
                if (!activeProfile.dateOfBirth.isNullOrBlank()) {
                    Text(
                        text = "DOB: ${DateUtils.formatDate(activeProfile.dateOfBirth)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Export options
            Text(
                text = "Select what to include",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            ExportCheckboxRow(
                label = "Medications & History",
                subtitle = "Current medications, dosage history, and changes",
                checked = exportMedications,
                onCheckedChange = { exportMedications = it }
            )
            ExportCheckboxRow(
                label = "Conditions & Notes",
                subtitle = "Health conditions and clinical notes",
                checked = exportConditions,
                onCheckedChange = { exportConditions = it }
            )
            ExportCheckboxRow(
                label = "Appointments",
                subtitle = "All scheduled and past appointments",
                checked = exportAppointments,
                onCheckedChange = { exportAppointments = it }
            )
            ExportCheckboxRow(
                label = "Journal Entries",
                subtitle = "Personal health journal entries",
                checked = exportJournal,
                onCheckedChange = { exportJournal = it }
            )
            ExportCheckboxRow(
                label = "File Attachments",
                subtitle = "List of uploaded files (names and categories only)",
                checked = exportAttachments,
                onCheckedChange = { exportAttachments = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Export button
            Button(
                onClick = {
                    isExporting = true
                    scope.launch {
                        try {
                            val file = withContext(Dispatchers.IO) {
                                generatePdf(
                                    context = context,
                                    app = app,
                                    profile = activeProfile,
                                    includeMedications = exportMedications,
                                    includeConditions = exportConditions,
                                    includeAppointments = exportAppointments,
                                    includeJournal = exportJournal,
                                    includeAttachments = exportAttachments
                                )
                            }
                            exportedFile = file
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.export_failed) + ": ${e.message}",
                                duration = SnackbarDuration.Short
                            )
                        } finally {
                            isExporting = false
                        }
                    }
                },
                enabled = !isExporting && (exportMedications || exportConditions || exportAppointments || exportJournal || exportAttachments),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.exporting))
                } else {
                    Text(text = stringResource(R.string.export_pdf))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExportCheckboxRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Generates a PDF report using iText 7 on a background thread.
 * The file is created in context.filesDir/exports/.
 */
private suspend fun generatePdf(
    context: android.content.Context,
    app: MedLogApplication,
    profile: ProfileEntity,
    includeMedications: Boolean,
    includeConditions: Boolean,
    includeAppointments: Boolean,
    includeJournal: Boolean,
    includeAttachments: Boolean
): File {
    val pdfFile = FileHelper.createExportFile(context, "medlog-${profile.name.replace(" ", "-").lowercase()}.pdf")

    val db = app.database
    val sb = StringBuilder()

    // ── Header ────────────────────────────────────────────────────────
    sb.appendLine("MEDLOG HEALTH REPORT")
    sb.appendLine("══════════════════════════════════════════════════")
    sb.appendLine()
    sb.appendLine("Profile: ${profile.name}")
    profile.dateOfBirth?.let { sb.appendLine("Date of Birth: ${DateUtils.formatDate(it)}") }
    profile.gender?.let { sb.appendLine("Gender: ${profile.gender}") }
    profile.bloodType?.let { sb.appendLine("Blood Type: ${profile.bloodType}") }
    profile.allergies?.let { if (it.isNotBlank()) sb.appendLine("Allergies: $it") }
    profile.emergencyContact?.let { if (it.isNotBlank()) sb.appendLine("Emergency Contact: $it") }
    profile.notes?.let { if (it.isNotBlank()) sb.appendLine("Notes: $it") }
    sb.appendLine("Generated: ${DateUtils.formatDateTime(java.time.LocalDateTime.now().toString())}")
    sb.appendLine()

    // ── Medications ───────────────────────────────────────────────────
    if (includeMedications) {
        val medications = db.medicationDao().getByProfile(profile.id).first()
        if (medications.isNotEmpty()) {
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine("MEDICATIONS & HISTORY")
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine()

            for (med in medications.sortedBy { it.name.lowercase() }) {
                sb.appendLine("  ${med.name}")
                sb.appendLine("  Dosage: ${med.dosage}  |  Frequency: ${FrequencyLabels.get(med.frequency)}  |  Route: ${RouteLabels.get(med.route)}")
                med.prescriber?.let { sb.appendLine("  Prescriber: $it") }
                med.startDate?.let { sb.appendLine("  Start: ${DateUtils.formatDate(it)}") }
                med.endDate?.let { sb.appendLine("  End: ${DateUtils.formatDate(it)}") }
                if (med.isActive) sb.appendLine("  Status: Active") else sb.appendLine("  Status: Discontinued")
                med.notes?.let { if (it.isNotBlank()) sb.appendLine("  Notes: $it") }

                // Changes
                val changes = db.medicationChangeDao().getByMedication(med.id).first()
                if (changes.isNotEmpty()) {
                    sb.appendLine("  Changes:")
                    for (change in changes.sortedByDescending { it.changedAt }) {
                        sb.appendLine("    - ${change.changeType}: ${change.oldValue ?: "N/A"} → ${change.newValue ?: "N/A"} (${DateUtils.formatDate(change.changedAt)})")
                        change.doctor?.let { sb.appendLine("      Doctor: $it") }
                        change.reason?.let { if (it.isNotBlank()) sb.appendLine("      Reason: $it") }
                    }
                }

                // Recent logs (last 10)
                val logs = db.medicationLogDao().getByMedication(med.id).first().take(10)
                if (logs.isNotEmpty()) {
                    sb.appendLine("  Recent Logs:")
                    for (log in logs) {
                        sb.appendLine("    - ${DateUtils.formatDateTime(log.takenAt)} | ${log.status} ${log.dose?.let { "($it)" } ?: ""}")
                        log.notes?.let { if (it.isNotBlank()) sb.appendLine("      ${it}") }
                    }
                }
                sb.appendLine()
            }
        } else {
            sb.appendLine("No medications recorded.")
            sb.appendLine()
        }
    }

    // ── Conditions ────────────────────────────────────────────────────
    if (includeConditions) {
        val conditions = db.conditionDao().getByProfile(profile.id).first()
        if (conditions.isNotEmpty()) {
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine("CONDITIONS & NOTES")
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine()

            for (cond in conditions.sortedBy { it.name.lowercase() }) {
                sb.appendLine("  ${cond.name}")
                sb.appendLine("  Severity: ${cond.severity.replaceFirstChar { c -> c.uppercase() }}  |  Status: ${cond.status.replaceFirstChar { c -> c.uppercase() }}")
                cond.diagnosedAt?.let { sb.appendLine("  Diagnosed: ${DateUtils.formatDate(it)}") }
                cond.doctor?.let { sb.appendLine("  Doctor: $it") }
                cond.notes?.let { if (it.isNotBlank()) sb.appendLine("  Notes: $it") }

                val notes = db.conditionNoteDao().getByCondition(cond.id).first()
                if (notes.isNotEmpty()) {
                    sb.appendLine("  Clinical Notes:")
                    for (note in notes.sortedByDescending { it.date }) {
                        sb.appendLine("    - ${DateUtils.formatDate(note.date)}")
                        note.mood?.let { sb.appendLine("      Mood: $it") }
                        note.painLevel?.let { sb.appendLine("      Pain Level: $it/10") }
                        sb.appendLine("      ${note.content}")
                    }
                }
                sb.appendLine()
            }
        } else {
            sb.appendLine("No conditions recorded.")
            sb.appendLine()
        }
    }

    // ── Appointments ──────────────────────────────────────────────────
    if (includeAppointments) {
        val appointments = db.appointmentDao().getByProfile(profile.id).first()
        if (appointments.isNotEmpty()) {
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine("APPOINTMENTS")
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine()

            for (apt in appointments.sortedByDescending { it.date }) {
                sb.appendLine("  ${apt.title}")
                sb.appendLine("  Date: ${DateUtils.formatDate(apt.date)}${apt.time?.let { " at ${DateUtils.formatTime(it)}" } ?: ""}")
                apt.doctor?.let { sb.appendLine("  Doctor: $it") }
                apt.location?.let { sb.appendLine("  Location: $it") }
                apt.duration?.let { sb.appendLine("  Duration: ${it} min") }
                sb.appendLine("  Type: ${AppointmentTypeLabels.get(apt.type)}  |  Status: ${apt.status.replaceFirstChar { c -> c.uppercase() }}")
                apt.notes?.let { if (it.isNotBlank()) sb.appendLine("  Notes: $it") }
                sb.appendLine()
            }
        } else {
            sb.appendLine("No appointments recorded.")
            sb.appendLine()
        }
    }

    // ── Journal ───────────────────────────────────────────────────────
    if (includeJournal) {
        val entries = db.journalDao().getByProfile(profile.id).first()
        if (entries.isNotEmpty()) {
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine("JOURNAL ENTRIES")
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine()

            for (entry in entries.sortedByDescending { it.date }) {
                entry.title?.let { sb.appendLine("  $it") }
                sb.appendLine("  Date: ${DateUtils.formatDate(entry.date)}")
                entry.mood?.let { sb.appendLine("  Mood: ${it.replaceFirstChar { c -> c.uppercase() }}") }
                entry.tags?.let { if (it.isNotBlank()) sb.appendLine("  Tags: $it") }
                sb.appendLine("  ${entry.content}")
                sb.appendLine()
            }
        } else {
            sb.appendLine("No journal entries recorded.")
            sb.appendLine()
        }
    }

    // ── File Attachments (list only) ──────────────────────────────────
    if (includeAttachments) {
        val attachments = db.fileAttachmentDao().getByProfile(profile.id).first()
        if (attachments.isNotEmpty()) {
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine("FILE ATTACHMENTS")
            sb.appendLine("────────────────────────────────────────────────")
            sb.appendLine()
            sb.appendLine("  (File contents not included — names and metadata only)")
            sb.appendLine()

            for (att in attachments.sortedByDescending { it.createdAt }) {
                sb.appendLine("  ${att.fileName}")
                sb.appendLine("  Size: ${DateUtils.formatFileSize(att.fileSize)}  |  Type: ${att.fileType}")
                att.category?.let { sb.appendLine("  Category: $it") }
                att.description?.let { if (it.isNotBlank()) sb.appendLine("  Description: $it") }
                att.linkedType?.let { sb.appendLine("  Linked to: $it #${att.linkedId}") }
                sb.appendLine("  Added: ${DateUtils.formatDate(att.createdAt.take(10))}")
                sb.appendLine()
            }
        } else {
            sb.appendLine("No file attachments recorded.")
            sb.appendLine()
        }
    }

    // ── Footer ────────────────────────────────────────────────────────
    sb.appendLine("══════════════════════════════════════════════════")
    sb.appendLine("Generated by MedLog — Your Personal Health Companion")
    sb.appendLine()

    // ── Write PDF with iText 7 ────────────────────────────────────────
    com.itextpdf.kernel.pdf.PdfWriter(pdfFile).use { writer ->
        val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(writer)
        val document = com.itextpdf.layout.Document(pdfDoc)

        // Set default font
        val font = com.itextpdf.io.font.constants.StandardFonts.HELVETICA
        val fontBold = com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD

        val lines = sb.lines()
        for (line in lines) {
            when {
                line.startsWith("MEDLOG") -> {
                    document.add(
                        com.itextpdf.layout.element.Paragraph(line)
                            .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(fontBold))
                            .setFontSize(18f)
                            .setMarginTop(16f)
                            .setMarginBottom(8f)
                    )
                }
                line.startsWith("══") -> {
                    // Skip decorative lines
                }
                line.startsWith("────") -> {
                    document.add(
                        com.itextpdf.layout.element.Paragraph("─".repeat(60))
                            .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(font))
                            .setFontSize(8f)
                            .setMarginTop(16f)
                            .setMarginBottom(4f)
                    )
                }
                line.startsWith("  ") && line.endsWith(":") -> {
                    // Section headers inside data (like "Changes:", "Clinical Notes:", "Recent Logs:")
                    document.add(
                        com.itextpdf.layout.element.Paragraph(line.trim())
                            .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(fontBold))
                            .setFontSize(10f)
                            .setMarginTop(8f)
                            .setMarginBottom(2f)
                            .setMarginLeft(12f)
                    )
                }
                line.startsWith("  ") && !line.startsWith("    ") && !line.startsWith("      ") -> {
                    // Data item names (indented once)
                    val isBold = !line.contains("  |  ") && !line.contains(" | ") &&
                        !line.startsWith("  -") && !line.startsWith("  Status") &&
                        !line.startsWith("  Dosage") && !line.startsWith("  Severity") &&
                        !line.startsWith("  Date:") && !line.startsWith("  Size:") &&
                        !line.startsWith("  Type:") && !line.startsWith("  Duration") &&
                        !line.startsWith("  Doctor:") && !line.startsWith("  Location:") &&
                        !line.startsWith("  Prescriber:") && !line.startsWith("  Start:") &&
                        !line.startsWith("  End:") && !line.startsWith("  Diagnosed:") &&
                        !line.startsWith("  Mood:") && !line.startsWith("  Tags:") &&
                        !line.startsWith("  Notes:") && !line.startsWith("  Category:") &&
                        !line.startsWith("  Description:") && !line.startsWith("  Linked") &&
                        !line.startsWith("  Added:") && !line.startsWith("  Pain") &&
                        !line.startsWith("  (") && !line.startsWith("  Generated")

                    if (isBold) {
                        document.add(
                            com.itextpdf.layout.element.Paragraph(line.trim())
                                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(fontBold))
                                .setFontSize(11f)
                                .setMarginTop(6f)
                                .setMarginBottom(1f)
                                .setMarginLeft(12f)
                        )
                    } else {
                        document.add(
                            com.itextpdf.layout.element.Paragraph(line.trim())
                                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(font))
                                .setFontSize(10f)
                                .setMarginTop(1f)
                                .setMarginBottom(1f)
                                .setMarginLeft(12f)
                        )
                    }
                }
                line.isBlank() -> {
                    document.add(
                        com.itextpdf.layout.element.Paragraph("")
                            .setMarginTop(2f)
                            .setMarginBottom(2f)
                    )
                }
                else -> {
                    document.add(
                        com.itextpdf.layout.element.Paragraph(line)
                            .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(font))
                            .setFontSize(10f)
                            .setMarginTop(1f)
                            .setMarginBottom(1f)
                    )
                }
            }
        }

        document.close()
        pdfDoc.close()
    }

    return pdfFile
}