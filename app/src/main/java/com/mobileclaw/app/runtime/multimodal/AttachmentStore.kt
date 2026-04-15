package com.mobileclaw.app.runtime.multimodal

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val ATTACHMENTS_DIR = "runtime_attachments"

@Singleton
class AttachmentStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) {
    fun importAttachment(
        sourceUri: Uri,
        preferredKind: RuntimeAttachmentKind? = null,
        sourceLabel: String,
    ): Result<PendingAttachment> = runCatching {
        val mimeType = context.contentResolver.getType(sourceUri).orEmpty()
        val kind = preferredKind ?: inferKind(mimeType)
            ?: throw IOException(appStrings.get(R.string.multimodal_attachment_unsupported_type))
        val displayName = resolveDisplayName(sourceUri).ifBlank {
            "${kind.name.lowercase()}-${System.currentTimeMillis()}"
        }
        val destinationDir = File(context.filesDir, ATTACHMENTS_DIR).apply { mkdirs() }
        val destinationFile = File(destinationDir, "${System.currentTimeMillis()}-${displayName.sanitizedFileName()}")
        copyUriToFile(sourceUri, destinationFile)
        PendingAttachment(
            attachmentId = "att-${System.currentTimeMillis()}-${destinationFile.name.hashCode()}",
            kind = kind,
            displayName = displayName,
            mimeType = mimeType.ifBlank { fallbackMimeType(kind) },
            localPath = destinationFile.absolutePath,
            previewSummary = when (kind) {
                RuntimeAttachmentKind.IMAGE -> appStrings.get(R.string.multimodal_attachment_image_preview, displayName)
                RuntimeAttachmentKind.AUDIO -> appStrings.get(R.string.multimodal_attachment_audio_preview, displayName)
            },
            sourceLabel = sourceLabel,
        )
    }

    private fun inferKind(mimeType: String): RuntimeAttachmentKind? {
        return when {
            mimeType.startsWith("image/") -> RuntimeAttachmentKind.IMAGE
            mimeType.startsWith("audio/") -> RuntimeAttachmentKind.AUDIO
            else -> null
        }
    }

    private fun fallbackMimeType(kind: RuntimeAttachmentKind): String {
        return when (kind) {
            RuntimeAttachmentKind.IMAGE -> "image/*"
            RuntimeAttachmentKind.AUDIO -> "audio/*"
        }
    }

    private fun copyUriToFile(sourceUri: Uri, destinationFile: File) {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IOException(appStrings.get(R.string.multimodal_attachment_open_failed))
        inputStream.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun resolveDisplayName(sourceUri: Uri): String {
        val resolver = context.contentResolver
        if (sourceUri.scheme == ContentResolver.SCHEME_CONTENT) {
            resolver.query(sourceUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0 && cursor.moveToFirst()) {
                        return cursor.getString(columnIndex).orEmpty()
                    }
                }
        }
        return sourceUri.lastPathSegment.orEmpty().substringAfterLast('/')
    }
}

private fun String.sanitizedFileName(): String {
    return replace(Regex("[^A-Za-z0-9._-]"), "_")
}
