package com.mobileclaw.app.ui.agentworkspace

import com.mobileclaw.app.runtime.localchat.LocalModelProfile
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus
import com.mobileclaw.app.ui.agentworkspace.model.ApprovalUiModel
import com.mobileclaw.app.ui.agentworkspace.model.AttachmentUiModel
import com.mobileclaw.app.ui.agentworkspace.model.AuditUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ChatTurnUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ContextInspectorUiModel
import com.mobileclaw.app.ui.agentworkspace.model.GovernanceCenterUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ModelHealthUiModel
import com.mobileclaw.app.ui.agentworkspace.model.PortabilityBundlePreviewUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeControlCenterUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeStatusUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceAttentionMode
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceFeedbackUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceSecondaryEntryUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceStatusDigestUiModel

enum class WorkspaceScreenState {
    READY_IDLE,
    STREAMING,
    AWAITING_APPROVAL,
    UNAVAILABLE,
    PREPARING,
    RECOVERABLE_FAILURE,
}

data class AgentWorkspaceUiState(
    val workspaceId: String = "primary_workspace",
    val screenState: WorkspaceScreenState = WorkspaceScreenState.PREPARING,
    val availableModels: List<LocalModelProfile> = emptyList(),
    val activeModel: LocalModelProfile? = null,
    val modelHealth: ModelHealthUiModel = ModelHealthUiModel(),
    val activeSessionId: String? = null,
    val turns: List<ChatTurnUiModel> = emptyList(),
    val activeAssistantTurnId: String? = null,
    val runtimeStatus: RuntimeStatusUiModel = RuntimeStatusUiModel(),
    val contextInspector: ContextInspectorUiModel = ContextInspectorUiModel(),
    val portabilityPreview: PortabilityBundlePreviewUiModel? = null,
    val governanceCenter: GovernanceCenterUiModel = GovernanceCenterUiModel(),
    val pendingApproval: ApprovalUiModel? = null,
    val recentAudit: List<AuditUiModel> = emptyList(),
    val composerDraft: String = "",
    val isComposerEnabled: Boolean = false,
    val isBusy: Boolean = false,
    val feedback: WorkspaceFeedbackUiModel? = null,
    val attentionMode: WorkspaceAttentionMode = WorkspaceAttentionMode.PREPARING,
    val statusDigest: WorkspaceStatusDigestUiModel = WorkspaceStatusDigestUiModel(),
    val secondaryEntries: List<WorkspaceSecondaryEntryUiModel> = emptyList(),
    val runtimeControlCenter: RuntimeControlCenterUiModel = RuntimeControlCenterUiModel(),
    val pendingAttachments: List<AttachmentUiModel> = emptyList(),
    val activeAttachments: List<AttachmentUiModel> = emptyList(),
) {
    val isModelReady: Boolean = activeModel?.availabilityStatus == ModelAvailabilityStatus.READY
    val canSend: Boolean = isModelReady && isComposerEnabled && !isBusy &&
        (composerDraft.isNotBlank() || pendingAttachments.isNotEmpty())
}
