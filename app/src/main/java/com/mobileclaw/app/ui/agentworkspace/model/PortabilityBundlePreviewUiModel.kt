package com.mobileclaw.app.ui.agentworkspace.model

data class PortabilityBundlePreviewUiModel(
    val memoryId: String,
    val title: String,
    val exportModeLabel: String,
    val payloadPreview: String,
    val redactionReason: String,
    val includedFields: List<String>,
    val redactedFields: List<String>,
    val compatibilityLines: List<PortabilityCompatibilityUiModel>,
    val canShare: Boolean,
    val canSwitchToFull: Boolean,
    val canSwitchToSummary: Boolean,
    val isFullModeSelected: Boolean,
)

data class PortabilityCompatibilityUiModel(
    val title: String,
    val detail: String,
    val statusLabel: String,
)
