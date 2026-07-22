package com.medlog.app.data.local.repository

import com.medlog.app.data.local.MedLogDatabase
import com.medlog.app.data.model.SearchResultItem
import kotlinx.coroutines.flow.first

class SearchRepository(private val database: MedLogDatabase) {

    suspend fun search(profileId: Long, query: String): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val lowerQuery = query.lowercase()

        // ── Medications (by name) ────────────────────────────────────
        val medications = database.medicationDao().searchByName(profileId, query).first()
        for (med in medications) {
            results.add(
                SearchResultItem(
                    type = "medication",
                    id = med.id,
                    title = med.name,
                    subtitle = "${med.dosage} · ${med.frequency}",
                    date = med.startDate,
                )
            )
        }

        // ── Conditions (by name) — filter in Kotlin (no DAO search method) ──
        val conditions = database.conditionDao().getByProfile(profileId).first()
        for (cond in conditions) {
            if (cond.name.lowercase().contains(lowerQuery)) {
                results.add(
                    SearchResultItem(
                        type = "condition",
                        id = cond.id,
                        title = cond.name,
                        subtitle = "${cond.severity} · ${cond.status}",
                        date = cond.diagnosedAt,
                    )
                )
            }
        }

        // ── Appointments (by title) ──────────────────────────────────
        val appointments = database.appointmentDao().searchByTitle(profileId, query).first()
        for (apt in appointments) {
            results.add(
                SearchResultItem(
                    type = "appointment",
                    id = apt.id,
                    title = apt.title,
                    subtitle = apt.doctor ?: apt.type,
                    date = apt.date,
                )
            )
        }

        // ── Journal entries (by title + content) ─────────────────────
        val journals = database.journalDao().searchByContent(profileId, query).first()
        for (journal in journals) {
            results.add(
                SearchResultItem(
                    type = "journal",
                    id = journal.id,
                    title = journal.title ?: journal.date,
                    subtitle = journal.content.take(80),
                    date = journal.date,
                )
            )
        }

        // ── Section entries (by title + content) — filter in Kotlin ──
        val sections = database.sectionDao().getByProfile(profileId).first()
        for (section in sections) {
            val entries = database.sectionEntryDao().getBySection(section.id).first()
            for (entry in entries) {
                val titleMatch = entry.title.lowercase().contains(lowerQuery)
                val contentMatch = entry.content?.lowercase()?.contains(lowerQuery) == true
                if (titleMatch || contentMatch) {
                    results.add(
                        SearchResultItem(
                            type = "section-entry",
                            id = entry.id,
                            title = entry.title,
                            subtitle = section.title,
                            date = entry.date,
                        )
                    )
                }
            }
        }

        return results
    }
}