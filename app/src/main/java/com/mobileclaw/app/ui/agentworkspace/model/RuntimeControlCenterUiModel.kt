package com.mobileclaw.app.ui.agentworkspace.model

data class RuntimeControlCenterUiModel(
    val title: String = "",
    val headline: String = "",
    val supportingText: String = "",
    val attentionMode: WorkspaceAttentionMode = WorkspaceAttentionMode.NORMAL,
    val traceSections: List<RuntimeTraceSectionUiModel> = emptyList(),
    val artifactEntries: List<ManagedArtifactEntryUiModel> = emptyList(),
)

data class RuntimeTraceSectionUiModel(
    val sectionId: String,
    val title: String,
    val lines: List<String> = emptyList(),
    val emptyState: String = "",
    val isHighlighted: Boolean = false,
)

data class ManagedArtifactEntryUiModel(
    val artifactId: String,
    val title: String,
    val summary: String = "",
    val statusLine: String = "",
    val detailLines: List<String> = emptyList(),
    val actionLabel: String = "",
    val isEditable: Boolean = false,
    val unavailableReason: String = "",
)
