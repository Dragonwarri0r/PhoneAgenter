package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.localchat.LocalModelProfile
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus

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
    val currentModel = models.firstOrNull { it.modelId == currentModelId }
    val otherModels = models.filterNot { it.modelId == currentModelId }
    var advancedModelId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            currentModel?.let { selectedModel ->
                item(key = "current-model") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.model_current_selection),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = selectedModel.displayName,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = selectedModel.statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (selectedModel.availabilityStatus == ModelAvailabilityStatus.FAILED) {
                                TextButton(
                                    onClick = {
                                        onModelSelected(selectedModel.modelId)
                                        onDismiss()
                                    },
                                ) {
                                    Text(stringResource(R.string.common_retry))
                                }
                            }
                        }
                    }
                }
            }
            items(otherModels, key = { it.modelId }) { model ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(model.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = model.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (model.supportsManualCapabilityOverride) {
                            TextButton(
                                onClick = {
                                    advancedModelId = if (advancedModelId == model.modelId) {
                                        null
                                    } else {
                                        model.modelId
                                    }
                                },
                            ) {
                                Text(stringResource(R.string.model_advanced_settings))
                            }
                        }
                        if (advancedModelId == model.modelId && model.supportsManualCapabilityOverride) {
                            Row(
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
                        TextButton(
                            onClick = {
                                onModelSelected(model.modelId)
                                onDismiss()
                            },
                            enabled = model.isSelectable,
                        ) {
                            Text(stringResource(R.string.common_use_model))
                        }
                    }
                }
            }
            item(key = "import-model") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
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
            }
        }
    }
}
