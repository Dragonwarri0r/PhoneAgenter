package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AttachmentPreviewRail(
                attachments = if (pendingAttachments.isNotEmpty()) pendingAttachments else activeAttachments,
                onRemoveAttachment = if (pendingAttachments.isNotEmpty()) onRemoveAttachment else null,
            )
            if (supportsImage || supportsAudio) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.multimodal_capability_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (supportsImage) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.multimodal_supports_image)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            )
                        }
                        if (supportsAudio) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.multimodal_supports_audio)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            )
                        }
                        if (supportsImage) {
                            AssistChip(
                                onClick = onAddImageClicked,
                                label = { Text(stringResource(R.string.multimodal_add_image)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                ),
                            )
                        }
                        if (supportsAudio) {
                            AssistChip(
                                onClick = onAddAudioClicked,
                                label = { Text(stringResource(R.string.multimodal_add_audio)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                ),
                            )
                        }
                    }
                }
            } else if (pendingAttachments.isNotEmpty() || activeAttachments.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.multimodal_model_capability_missing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChanged,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .semantics { contentDescription = composerDescription },
                    enabled = enabled,
                    shape = RoundedCornerShape(24.dp),
                    label = {
                        Text(stringResource(R.string.workspace_message_label))
                    },
                    placeholder = {
                        Text(stringResource(R.string.workspace_message_placeholder))
                    },
                    maxLines = 5,
                )
                SendButton(
                    enabled = enabled && (draft.isNotBlank() || pendingAttachments.isNotEmpty()),
                    onClick = onSendClicked,
                )
            }
        }
    }
}
