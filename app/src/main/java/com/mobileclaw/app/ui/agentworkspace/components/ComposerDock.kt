package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentKind
import com.mobileclaw.app.ui.agentworkspace.model.AttachmentUiModel

@Composable
fun ComposerDock(
    draft: String,
    enabled: Boolean,
    supportsImage: Boolean,
    supportsAudio: Boolean,
    pendingAttachments: List<AttachmentUiModel>,
    activeAttachments: List<AttachmentUiModel>,
    onDraftChanged: (String) -> Unit,
    onAddImageClicked: () -> Unit,
    onAddAudioClicked: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val composerDescription = stringResource(R.string.workspace_cd_message_composer)
    val attachments = if (pendingAttachments.isNotEmpty()) pendingAttachments else activeAttachments
    val hasUnsupportedAttachment = attachments.any { attachment ->
        when (attachment.kind) {
            RuntimeAttachmentKind.IMAGE -> !supportsImage
            RuntimeAttachmentKind.AUDIO -> !supportsAudio
        }
    }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AttachmentPreviewRail(
                attachments = attachments,
                onRemoveAttachment = if (pendingAttachments.isNotEmpty()) onRemoveAttachment else null,
            )
            if (hasUnsupportedAttachment) {
                Text(
                    text = stringResource(R.string.multimodal_model_capability_missing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (supportsImage || supportsAudio) {
                    Box {
                        IconButton(
                            onClick = { showAttachmentMenu = true },
                            enabled = enabled,
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceContainerLow,
                                            MaterialTheme.colorScheme.surfaceContainer,
                                        ),
                                    ),
                                    shape = CircleShape,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.workspace_add_attachment),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        DropdownMenu(
                            expanded = showAttachmentMenu,
                            onDismissRequest = { showAttachmentMenu = false },
                        ) {
                            if (supportsImage) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.multimodal_add_image)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Image,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        showAttachmentMenu = false
                                        onAddImageClicked()
                                    },
                                )
                            }
                            if (supportsAudio) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.multimodal_add_audio)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.GraphicEq,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        showAttachmentMenu = false
                                        onAddAudioClicked()
                                    },
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChanged,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .semantics { contentDescription = composerDescription },
                    enabled = enabled,
                    shape = RoundedCornerShape(24.dp),
                    placeholder = {
                        Text(stringResource(R.string.workspace_message_placeholder))
                    },
                    maxLines = 4,
                )
                SendButton(
                    enabled = enabled && (draft.isNotBlank() || pendingAttachments.isNotEmpty()),
                    onClick = onSendClicked,
                )
            }
        }
    }
}
