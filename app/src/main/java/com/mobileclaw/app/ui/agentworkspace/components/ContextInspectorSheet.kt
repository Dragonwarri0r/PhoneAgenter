package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.mobileclaw.app.ui.agentworkspace.model.ContextInspectorUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextInspectorSheet(
    inspector: ContextInspectorUiModel,
    onDismiss: () -> Unit,
    onCyclePersonaVerbosity: () -> Unit,
    onTogglePin: (String) -> Unit,
    onPromote: (String) -> Unit,
    onDemote: (String) -> Unit,
    onExpire: (String) -> Unit,
    onPreviewExport: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.workspace_context_inspector),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = inspector.personaSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onCyclePersonaVerbosity) {
                    Text(stringResource(R.string.workspace_cycle_persona_verbosity))
                }
            }
            if (inspector.hiddenPrivateCount > 0) {
                Text(
                    text = stringResource(
                        R.string.workspace_private_items_redacted,
                        inspector.hiddenPrivateCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(
                    R.string.workspace_context_counts,
                    inspector.activeMemoryItems.size,
                    inspector.totalEligibleCount,
                    inspector.excludedCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = inspector.retrievalSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (inspector.extensionSummary.isNotBlank()) {
                Text(
                    text = inspector.extensionSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                inspector.activeMemoryItems.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = item.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = item.syncDetail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = item.mergeDetail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = item.exportDetail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = item.extensionDetail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text(item.badge) },
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                TextButton(
                                    onClick = { onTogglePin(item.memoryId) },
                                ) {
                                    Text(
                                        if (item.isPinned) {
                                            stringResource(R.string.common_unpin)
                                        } else {
                                            stringResource(R.string.common_pin)
                                        },
                                    )
                                }
                                if (item.canPromote) {
                                    TextButton(
                                        onClick = { onPromote(item.memoryId) },
                                    ) {
                                        Text(stringResource(R.string.common_promote))
                                    }
                                }
                                if (item.canDemote) {
                                    TextButton(
                                        onClick = { onDemote(item.memoryId) },
                                    ) {
                                        Text(stringResource(R.string.common_demote))
                                    }
                                }
                                if (item.canExpire) {
                                    TextButton(
                                        onClick = { onExpire(item.memoryId) },
                                    ) {
                                        Text(stringResource(R.string.common_expire))
                                    }
                                }
                                TextButton(
                                    onClick = { onPreviewExport(item.memoryId) },
                                ) {
                                    Text(
                                        if (item.canExport) {
                                            stringResource(R.string.portability_export_action)
                                        } else {
                                            stringResource(R.string.portability_export_blocked_action)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
