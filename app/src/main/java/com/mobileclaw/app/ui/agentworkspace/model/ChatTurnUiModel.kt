package com.mobileclaw.app.ui.agentworkspace.model

enum class ChatRoleUi {
    USER,
    ASSISTANT,
}

enum class ChatTurnStateUi {
    COMPLETE,
    STREAMING,
    FAILED,
}

data class ChatTurnUiModel(
    val turnId: String,
    val role: ChatRoleUi,
    val content: String,
    val state: ChatTurnStateUi,
    val attachments: List<AttachmentUiModel> = emptyList(),
)
