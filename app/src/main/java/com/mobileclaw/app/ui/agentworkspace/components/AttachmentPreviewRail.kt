package com.mobileclaw.app.ui.agentworkspace.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentKind
import com.mobileclaw.app.ui.agentworkspace.model.AttachmentUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AttachmentPreviewRail(
    attachments: List<AttachmentUiModel>,
    onRemoveAttachment: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (attachments.isEmpty()) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        attachments.forEach { attachment ->
            AttachmentPreviewCard(
                attachment = attachment,
                onRemoveAttachment = onRemoveAttachment,
            )
        }
    }
}

@Composable
private fun AttachmentPreviewCard(
    attachment: AttachmentUiModel,
    onRemoveAttachment: ((String) -> Unit)?,
) {
    Card(
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd,
            ) {
                AttachmentVisual(
                    attachment = attachment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                )
                if (onRemoveAttachment != null) {
                    IconButton(
                        onClick = { onRemoveAttachment(attachment.attachmentId) },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            Text(
                text = attachment.displayName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = attachment.sourceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AttachmentVisual(
    attachment: AttachmentUiModel,
    modifier: Modifier = Modifier,
) {
    when (attachment.kind) {
        RuntimeAttachmentKind.IMAGE -> {
            val thumbnail by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
                initialValue = null,
                key1 = attachment.localPath,
            ) {
                value = withContext(Dispatchers.IO) {
                    decodeThumbnail(attachment.localPath)?.asImageBitmap()
                }
            }
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!,
                    contentDescription = attachment.displayName,
                    modifier = modifier,
                    contentScale = ContentScale.Crop,
                )
            } else {
                AttachmentIconPlaceholder(
                    icon = Icons.Rounded.Image,
                    modifier = modifier,
                )
            }
        }

        RuntimeAttachmentKind.AUDIO -> {
            AttachmentIconPlaceholder(
                icon = Icons.Rounded.GraphicEq,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun AttachmentIconPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
    }
}

private fun decodeThumbnail(path: String): android.graphics.Bitmap? {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        return null
    }
    val sampleSize = maxOf(1, minOf(bounds.outWidth / 480, bounds.outHeight / 480))
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    }
    return BitmapFactory.decodeFile(path, options)
}
