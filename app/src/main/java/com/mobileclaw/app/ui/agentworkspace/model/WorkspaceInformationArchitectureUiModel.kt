package com.mobileclaw.app.ui.agentworkspace.model

enum class WorkspaceAttentionMode {
    NORMAL,
    AWAITING_APPROVAL,
    FAILURE,
    PREPARING,
    UNAVAILABLE,
}

data class WorkspaceStatusDigestUiModel(
    val attentionMode: WorkspaceAttentionMode = WorkspaceAttentionMode.NORMAL,
    val stageLabel: String = "",
    val headline: String = "",
    val supportingText: String = "",
    val primarySignals: List<String> = emptyList(),
    val secondarySignals: List<String> = emptyList(),
    val showsPermissionAction: Boolean = false,
)

data class WorkspaceSecondaryEntryUiModel(
    val entryId: String,
    val label: String,
    val supportingText: String = "",
    val isHighlighted: Boolean = false,
)
