package com.medlog.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.ui.screens.appointments.AppointmentFormScreen
import com.medlog.app.ui.screens.appointments.AppointmentListScreen
import com.medlog.app.ui.screens.clutter.ClutterScreen
import com.medlog.app.ui.screens.conditions.ConditionDetailScreen
import com.medlog.app.ui.screens.conditions.ConditionFormScreen
import com.medlog.app.ui.screens.conditions.ConditionListScreen
import com.medlog.app.ui.screens.conditions.ConditionNoteFormScreen
import com.medlog.app.ui.screens.dashboard.DashboardScreen
import com.medlog.app.ui.screens.files.FileListScreen
import com.medlog.app.ui.screens.journal.JournalFormScreen
import com.medlog.app.ui.screens.journal.JournalListScreen
import com.medlog.app.ui.screens.medications.MedicationDetailScreen
import com.medlog.app.ui.screens.medications.MedicationFormScreen
import com.medlog.app.ui.screens.medications.MedicationHistoryScreen
import com.medlog.app.ui.screens.medications.MedicationListScreen
import com.medlog.app.ui.screens.medications.MedicationLogScreen
import com.medlog.app.ui.screens.onboarding.OnboardingScreen
import com.medlog.app.ui.screens.profile.ProfileFormScreen
import com.medlog.app.ui.screens.profile.ProfileListScreen
import com.medlog.app.ui.screens.sections.SectionDetailScreen
import com.medlog.app.ui.screens.sections.SectionEntryFormScreen
import com.medlog.app.ui.screens.sections.SectionFormScreen
import com.medlog.app.ui.screens.sections.SectionListScreen
import com.medlog.app.ui.screens.appointments.AppointmentDetailScreen
import com.medlog.app.ui.screens.medications.MedicationStatsScreen
import com.medlog.app.ui.screens.medications.MedicationInteractionCheckerScreen
import com.medlog.app.ui.screens.search.SearchScreen
import com.medlog.app.ui.screens.settings.SettingsScreen
import com.medlog.app.ui.screens.export.ExportScreen

/**
 * Main navigation graph. Decides between onboarding (no profiles)
 * and the main app (has profiles) on first composition.
 */
@Composable
fun MedLogNavGraph(
    navController: NavHostController,
    profileCount: Int,
    activeProfile: ProfileEntity?,
    onProfileCreated: () -> Unit
) {
    var startDestination by remember { mutableStateOf<String>(Screen.Onboarding.route) }

    // If profiles exist, start at dashboard; otherwise onboarding
    LaunchedEffect(profileCount) {
        startDestination = if (profileCount > 0) Screen.Dashboard.route else Screen.Onboarding.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        // ── Onboarding ───────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onProfileCreated = {
                    onProfileCreated()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Main tabs ────────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                activeProfile = activeProfile
            )
        }
        composable(Screen.Medications.route) {
            MedicationListScreen(navController = navController)
        }
        composable(Screen.Conditions.route) {
            ConditionListScreen(navController = navController)
        }
        composable(Screen.Appointments.route) {
            AppointmentListScreen(navController = navController)
        }
        composable(Screen.Journal.route) {
            JournalListScreen(navController = navController, activeProfile = activeProfile)
        }
        composable(Screen.Files.route) {
            FileListScreen(navController = navController, activeProfile = activeProfile)
        }
        composable(Screen.Sections.route) {
            SectionListScreen(navController = navController, activeProfile = activeProfile)
        }
        composable(Screen.Clutter.route) {
            ClutterScreen(
                navController = navController,
                activeProfile = activeProfile
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                activeProfile = activeProfile
            )
        }

        // ── Medication detail/form screens ───────────────────────
        composable(
            route = Screen.MedicationDetail.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: 0L
            MedicationDetailScreen(
                medicationId = medicationId,
                navController = navController
            )
        }
        composable(
            route = Screen.MedicationForm.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: 0L
            MedicationFormScreen(
                medicationId = medicationId,
                navController = navController
            )
        }
        composable(
            route = Screen.MedicationLog.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: 0L
            MedicationLogScreen(
                medicationId = medicationId,
                navController = navController
            )
        }
        composable(
            route = Screen.MedicationHistory.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: 0L
            MedicationHistoryScreen(
                medicationId = medicationId,
                navController = navController
            )
        }

        // ── Condition detail/form screens ────────────────────────
        composable(
            route = Screen.ConditionDetail.route,
            arguments = listOf(navArgument("conditionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val conditionId = backStackEntry.arguments?.getLong("conditionId") ?: 0L
            ConditionDetailScreen(
                conditionId = conditionId,
                navController = navController
            )
        }
        composable(
            route = Screen.ConditionForm.route,
            arguments = listOf(navArgument("conditionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val conditionId = backStackEntry.arguments?.getLong("conditionId") ?: 0L
            ConditionFormScreen(
                conditionId = conditionId,
                navController = navController
            )
        }
        composable(
            route = Screen.ConditionNoteForm.route,
            arguments = listOf(
                navArgument("conditionId") { type = NavType.LongType },
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val conditionId = backStackEntry.arguments?.getLong("conditionId") ?: 0L
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            ConditionNoteFormScreen(
                conditionId = conditionId,
                noteId = noteId,
                navController = navController
            )
        }

        // ── Appointment detail/form screens ─────────────────────
        composable(
            route = Screen.AppointmentForm.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getLong("appointmentId") ?: 0L
            AppointmentFormScreen(
                appointmentId = appointmentId,
                navController = navController
            )
        }
        composable(
            route = Screen.AppointmentDetail.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getLong("appointmentId") ?: 0L
            AppointmentDetailScreen(
                appointmentId = appointmentId,
                navController = navController
            )
        }

        // ── Journal form screen ──────────────────────────────────
        composable(
            route = Screen.JournalForm.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            JournalFormScreen(
                entryId = entryId,
                navController = navController
            )
        }

        // ── Section detail/form screens ──────────────────────────
        composable(
            route = Screen.SectionDetail.route,
            arguments = listOf(navArgument("sectionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
            SectionDetailScreen(
                sectionId = sectionId,
                navController = navController
            )
        }
        composable(
            route = Screen.SectionForm.route,
            arguments = listOf(navArgument("sectionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
            SectionFormScreen(
                sectionId = sectionId,
                navController = navController
            )
        }
        composable(
            route = Screen.SectionEntryForm.route,
            arguments = listOf(
                navArgument("sectionId") { type = NavType.LongType },
                navArgument("entryId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            SectionEntryFormScreen(
                sectionId = sectionId,
                entryId = entryId,
                navController = navController
            )
        }

        // ── Profile screens ──────────────────────────────────────
        composable(Screen.ProfileList.route) {
            ProfileListScreen(navController = navController)
        }
        composable(
            route = Screen.ProfileForm.route,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: 0L
            ProfileFormScreen(
                profileId = profileId,
                navController = navController
            )
        }

        // ── Medication analytics ─────────────────────────────
        composable(Screen.MedicationStats.route) {
            MedicationStatsScreen(navController = navController)
        }
        composable(Screen.MedicationInteractionChecker.route) {
            MedicationInteractionCheckerScreen(navController = navController)
        }

        // ── Search & Export ──────────────────────────────────────
        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                profileId = activeProfile?.id ?: 0L
            )
        }
        composable(Screen.Export.route) {
            ExportScreen(
                onBack = { navController.popBackStack() },
                activeProfile = activeProfile
            )
        }
    }
}
