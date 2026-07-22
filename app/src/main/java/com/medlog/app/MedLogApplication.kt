package com.medlog.app

import android.app.Application
import com.medlog.app.data.local.MedLogDatabase
import com.medlog.app.data.local.repository.*

/**
 * Application class. Owns the single Room database instance and the
 * manual dependency injection container. All repositories are created
 * here and accessed by ViewModels via (application as MedLogApplication).
 */
class MedLogApplication : Application() {

    // Database — lazy so it's only created when first accessed
    val database: MedLogDatabase by lazy {
        MedLogDatabase.getInstance(this)
    }

    // Primary repositories — domain-area groupings used by most ViewModels
    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.profileDao())
    }
    val medicationRepository: MedicationRepository by lazy {
        MedicationRepository(database.medicationDao(), database.medicationLogDao(), database.medicationChangeDao())
    }
    val medicationLogRepository: MedicationLogRepository by lazy {
        MedicationLogRepository(database.medicationLogDao())
    }
    val conditionRepository: ConditionRepository by lazy {
        ConditionRepository(database.conditionDao(), database.conditionNoteDao())
    }
    val conditionNoteRepository: ConditionNoteRepository by lazy {
        ConditionNoteRepository(database.conditionNoteDao())
    }
    val appointmentRepository: AppointmentRepository by lazy {
        AppointmentRepository(database.appointmentDao())
    }
    val fileAttachmentRepository: FileAttachmentRepository by lazy {
        FileAttachmentRepository(database.fileAttachmentDao())
    }
    val sectionRepository: SectionRepository by lazy {
        SectionRepository(database.sectionDao(), database.sectionEntryDao())
    }
    val sectionEntryRepository: SectionEntryRepository by lazy {
        SectionEntryRepository(database.sectionEntryDao())
    }
    val clutterRepository: ClutterRepository by lazy {
        ClutterRepository(database.clutterDao())
    }
    val journalRepository: JournalRepository by lazy {
        JournalRepository(database.journalDao())
    }
    val appSettingRepository: AppSettingRepository by lazy {
        AppSettingRepository(database.appSettingDao())
    }
    val searchRepository: SearchRepository by lazy {
        SearchRepository(database)
    }
}