package com.mobileclaw.app.ui.agentworkspace.model

enum class WorkspaceFeedbackKind {
    SUCCESS,
    ERROR,
    INFO,
}

data class WorkspaceFeedbackUiModel(
    val messageId: String,
    val kind: WorkspaceFeedbackKind,
    val text: String,
    val scope: String,
)

