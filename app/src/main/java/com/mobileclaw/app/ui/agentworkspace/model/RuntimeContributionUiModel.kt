package com.mobileclaw.app.ui.agentworkspace.model

data class RuntimeContributionUiModel(
    val contributionId: String,
    val title: String,
    val lifecycleLabel: String,
    val statusLabel: String,
    val summary: String,
    val details: String = "",
    val availabilityLabel: String = "",
    val actionLabel: String = "",
    val supportsAvailabilityChange: Boolean = false,
    val governanceLines: List<String> = emptyList(),
    val limitationSummary: String = "",
)
