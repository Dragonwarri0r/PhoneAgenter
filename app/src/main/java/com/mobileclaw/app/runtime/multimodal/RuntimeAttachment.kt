package com.mobileclaw.app.runtime.multimodal

enum class RuntimeAttachmentKind {
    IMAGE,
    AUDIO,
}

enum class RuntimeAttachmentSourceType {
    COMPOSER_IMPORT,
    EXTERNAL_HANDOFF,
}

data class RuntimeAttachment(
    val attachmentId: String,
    val kind: RuntimeAttachmentKind,
    val mimeType: String,
    val localPath: String,
    val displayName: String,
    val sourceType: RuntimeAttachmentSourceType,
)

data class PendingAttachment(
    val attachmentId: String,
    val kind: RuntimeAttachmentKind,
    val displayName: String,
    val mimeType: String,
    val localPath: String,
    val previewSummary: String,
    val sourceLabel: String,
    val addedAtEpochMillis: Long = System.currentTimeMillis(),
) {
    fun toRuntimeAttachment(sourceType: RuntimeAttachmentSourceType): RuntimeAttachment {
        return RuntimeAttachment(
            attachmentId = attachmentId,
            kind = kind,
            mimeType = mimeType,
            localPath = localPath,
            displayName = displayName,
            sourceType = sourceType,
        )
    }
}
