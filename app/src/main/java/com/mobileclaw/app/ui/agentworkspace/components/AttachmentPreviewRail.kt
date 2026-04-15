package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.ui.agentworkspace.model.AttachmentUiModel

@Composable
fun AttachmentPreviewRail(
    attachments: List<AttachmentUiModel>,
    onRemoveAttachment: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (attachments.isEmpty()) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        attachments.forEach { attachment ->
            AssistChip(
                onClick = { onRemoveAttachment?.invoke(attachment.attachmentId) },
                label = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = attachment.previewSummary,
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
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                trailingIcon = if (onRemoveAttachment != null) {
                    {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}
