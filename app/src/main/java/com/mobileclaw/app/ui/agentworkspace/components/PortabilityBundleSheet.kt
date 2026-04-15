package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
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
import com.mobileclaw.app.ui.agentworkspace.model.PortabilityBundlePreviewUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortabilityBundleSheet(
    preview: PortabilityBundlePreviewUiModel,
    onDismiss: () -> Unit,
    onSelectSummary: () -> Unit,
    onSelectFull: () -> Unit,
    onShare: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.portability_preview_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = preview.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (preview.canSwitchToSummary) {
                    AssistChip(
                        onClick = onSelectSummary,
                        label = { Text(stringResource(R.string.portability_mode_summary)) },
                    )
                }
                if (preview.canSwitchToFull) {
                    AssistChip(
                        onClick = onSelectFull,
                        label = { Text(stringResource(R.string.portability_mode_full)) },
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.portability_preview_mode_line,
                    preview.exportModeLabel,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = preview.redactionReason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.portability_bundle_payload_header),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = preview.payloadPreview,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.portability_preview_included_header),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = preview.includedFields.joinToString().ifBlank {
                        stringResource(R.string.memory_detail_none)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.portability_preview_redacted_header),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = preview.redactedFields.joinToString().ifBlank {
                        stringResource(R.string.memory_detail_none)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.portability_preview_compatibility_header),
                    style = MaterialTheme.typography.titleSmall,
                )
                preview.compatibilityLines.forEach { line ->
                    Text(
                        text = "${line.title}: ${line.statusLabel} · ${line.detail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_close))
                }
                TextButton(
                    onClick = onShare,
                    enabled = preview.canShare,
                ) {
                    Text(stringResource(R.string.portability_share_action))
                }
            }
        }
    }
}
