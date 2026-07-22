package com.medlog.app.ui.navigation

/**
 * Sealed class defining every screen in the app.
 * Each screen carries its navigation arguments so the NavGraph
 * can build type-safe routes.
 */
sealed class Screen(val route: String) {
    // Onboarding / profile creation (shown when no profiles exist)
    data object Onboarding : Screen("onboarding")

    // Main screens — these are bottom-nav destinations
    data object Dashboard : Screen("dashboard")
    data object Medications : Screen("medications")
    data object Conditions : Screen("conditions")
    data object Appointments : Screen("appointments")
    data object Journal : Screen("journal")
    data object Files : Screen("files")
    data object Sections : Screen("sections")
    data object Clutter : Screen("clutter")
    data object Settings : Screen("settings")

    // Detail/edit screens — navigated to from list screens
    data object MedicationDetail : Screen("medication/{medicationId}") {
        fun createRoute(medicationId: Long) = "medication/$medicationId"
    }
    data object MedicationForm : Screen("medication-form/{medicationId}") {
        fun createRoute(medicationId: Long? = null) =
            if (medicationId != null) "medication-form/$medicationId" else "medication-form/0"
    }
    data object MedicationLog : Screen("medication-log/{medicationId}") {
        fun createRoute(medicationId: Long) = "medication-log/$medicationId"
    }
    data object MedicationHistory : Screen("medication-history/{medicationId}") {
        fun createRoute(medicationId: Long) = "medication-history/$medicationId"
    }

    data object ConditionDetail : Screen("condition/{conditionId}") {
        fun createRoute(conditionId: Long) = "condition/$conditionId"
    }
    data object ConditionForm : Screen("condition-form/{conditionId}") {
        fun createRoute(conditionId: Long? = null) =
            if (conditionId != null) "condition-form/$conditionId" else "condition-form/0"
    }
    data object ConditionNoteForm : Screen("condition-note/{conditionId}/{noteId}") {
        fun createRoute(conditionId: Long, noteId: Long? = null) =
            if (noteId != null) "condition-note/$conditionId/$noteId" else "condition-note/$conditionId/0"
    }

    data object AppointmentForm : Screen("appointment-form/{appointmentId}") {
        fun createRoute(appointmentId: Long? = null) =
            if (appointmentId != null) "appointment-form/$appointmentId" else "appointment-form/0"
    }
    data object AppointmentDetail : Screen("appointment-detail/{appointmentId}") {
        fun createRoute(appointmentId: Long) = "appointment-detail/$appointmentId"
    }

    data object JournalForm : Screen("journal-form/{entryId}") {
        fun createRoute(entryId: Long? = null) =
            if (entryId != null) "journal-form/$entryId" else "journal-form/0"
    }

    data object SectionDetail : Screen("section/{sectionId}") {
        fun createRoute(sectionId: Long) = "section/$sectionId"
    }
    data object SectionForm : Screen("section-form/{sectionId}") {
        fun createRoute(sectionId: Long? = null) =
            if (sectionId != null) "section-form/$sectionId" else "section-form/0"
    }
    data object SectionEntryForm : Screen("section-entry/{sectionId}/{entryId}") {
        fun createRoute(sectionId: Long, entryId: Long? = null) =
            if (entryId != null) "section-entry/$sectionId/$entryId" else "section-entry/$sectionId/0"
    }

    data object ProfileList : Screen("profiles")   // Profile switcher
    data object ProfileForm : Screen("profile-form/{profileId}") {
        fun createRoute(profileId: Long? = null) =
            if (profileId != null) "profile-form/$profileId" else "profile-form/0"
    }
    data object MedicationStats : Screen("medication-stats")
    data object MedicationInteractionChecker : Screen("medication-interactions")
    data object Search : Screen("search")
    data object Export : Screen("export")
}