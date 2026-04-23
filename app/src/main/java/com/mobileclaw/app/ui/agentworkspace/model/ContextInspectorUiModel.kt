package com.mobileclaw.app.ui.agentworkspace.model

data class ContextInspectorUiModel(
    val title: String = "",
    val personaSummary: String = "",
    val headline: String = "",
    val supportingText: String = "",
    val emptyState: String = "",
    val activeMemoryItems: List<ContextMemoryUiModel> = emptyList(),
    val hiddenPrivateCount: Int = 0,
    val totalEligibleCount: Int = 0,
    val excludedCount: Int = 0,
    val retrievalSummary: String = "",
)

data class ContextMemoryUiModel(
    val memoryId: String,
    val title: String,
    val content: String,
    val summary: String,
    val badges: List<String> = emptyList(),
    val policyLine: String = "",
    val provenanceLine: String = "",
    val isPinned: Boolean,
    val canPromote: Boolean,
    val canDemote: Boolean,
    val canExpire: Boolean,
    val canExport: Boolean,
)
