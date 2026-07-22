package com.medlog.app.data.model

data class SearchResultItem(
    val type: String,   // "medication" | "condition" | "appointment" | "journal" | "section-entry"
    val id: Long,
    val title: String,
    val subtitle: String,
    val date: String?
)