package com.mobileclaw.app.ui.agentworkspace.model

import com.mobileclaw.app.runtime.multimodal.RuntimeAttachment
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentKind
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentSourceType

data class AttachmentUiModel(
    val attachmentId: String,
    val kind: RuntimeAttachmentKind,
    val displayName: String,
    val previewSummary: String,
    val sourceLabel: String,
    val mimeType: String = "",
    val localPath: String = "",
)

fun AttachmentUiModel.toRuntimeAttachment(sourceType: RuntimeAttachmentSourceType): RuntimeAttachment {
    return RuntimeAttachment(
        attachmentId = attachmentId,
        kind = kind,
        mimeType = mimeType,
        localPath = localPath,
        displayName = displayName,
        sourceType = sourceType,
    )
}
