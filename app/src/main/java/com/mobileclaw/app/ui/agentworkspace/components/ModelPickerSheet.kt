package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.localchat.LocalModelProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPickerSheet(
    models: List<LocalModelProfile>,
    currentModelId: String?,
    onDismiss: () -> Unit,
    onModelSelected: (String) -> Unit,
    onToggleImageSupport: (String, Boolean) -> Unit,
    onToggleAudioSupport: (String, Boolean) -> Unit,
    onImportModel: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "import-model") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            stringResource(R.string.workspace_import_model_file),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.workspace_import_model_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(
                            onClick = {
                                onImportModel()
                                onDismiss()
                            },
                        ) {
                            Text(stringResource(R.string.workspace_choose_file))
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            items(models, key = { it.modelId }) { model ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(model.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = model.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (model.supportsManualCapabilityOverride) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(R.string.model_capability_override_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                androidx.compose.foundation.layout.Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    FilterChip(
                                        selected = model.modalityCapabilities.supportsImage,
                                        onClick = {
                                            onToggleImageSupport(
                                                model.modelId,
                                                !model.modalityCapabilities.supportsImage,
                                            )
                                        },
                                        label = { Text(stringResource(R.string.multimodal_supports_image)) },
                                    )
                                    FilterChip(
                                        selected = model.modalityCapabilities.supportsAudio,
                                        onClick = {
                                            onToggleAudioSupport(
                                                model.modelId,
                                                !model.modalityCapabilities.supportsAudio,
                                            )
                                        },
                                        label = { Text(stringResource(R.string.multimodal_supports_audio)) },
                                    )
                                }
                            }
                        }
                        TextButton(
                            onClick = {
                                onModelSelected(model.modelId)
                                onDismiss()
                            },
                            enabled = model.isSelectable && model.modelId != currentModelId,
                        ) {
                            Text(
                                if (model.modelId == currentModelId) {
                                    stringResource(R.string.common_selected)
                                } else {
                                    stringResource(R.string.common_use_model)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
