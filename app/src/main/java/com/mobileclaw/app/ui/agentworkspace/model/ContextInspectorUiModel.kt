package com.mobileclaw.app.ui.agentworkspace.model

data class ContextInspectorUiModel(
    val personaSummary: String = "",
    val activeMemoryItems: List<ContextMemoryUiModel> = emptyList(),
    val hiddenPrivateCount: Int = 0,
    val totalEligibleCount: Int = 0,
    val excludedCount: Int = 0,
    val retrievalSummary: String = "",
    val extensionSummary: String = "",
)

data class ContextMemoryUiModel(
    val memoryId: String,
    val title: String,
    val detail: String,
    val badge: String,
    val syncDetail: String,
    val mergeDetail: String,
    val exportDetail: String,
    val extensionDetail: String,
    val isPinned: Boolean,
    val canPromote: Boolean,
    val canDemote: Boolean,
    val canExpire: Boolean,
    val canExport: Boolean,
)
