package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceAttentionMode
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceStatusDigestUiModel

@Composable
fun WorkspaceStatusDigest(
    digest: WorkspaceStatusDigestUiModel,
    onOpenDetails: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (digest.attentionMode) {
        WorkspaceAttentionMode.AWAITING_APPROVAL -> MaterialTheme.colorScheme.primaryContainer
        WorkspaceAttentionMode.FAILURE -> MaterialTheme.colorScheme.errorContainer
        WorkspaceAttentionMode.PREPARING -> MaterialTheme.colorScheme.secondaryContainer
        WorkspaceAttentionMode.UNAVAILABLE -> MaterialTheme.colorScheme.surfaceContainer
        WorkspaceAttentionMode.NORMAL -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val eyebrowColor = when (digest.attentionMode) {
        WorkspaceAttentionMode.AWAITING_APPROVAL -> MaterialTheme.colorScheme.onPrimaryContainer
        WorkspaceAttentionMode.FAILURE -> MaterialTheme.colorScheme.onErrorContainer
        WorkspaceAttentionMode.PREPARING -> MaterialTheme.colorScheme.onSecondaryContainer
        WorkspaceAttentionMode.UNAVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
        WorkspaceAttentionMode.NORMAL -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f),
                ) {
                    Text(
                        text = digest.stageLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = eyebrowColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = onOpenDetails) {
                    Text(stringResource(R.string.workspace_digest_open_details))
                }
            }

            Text(
                text = digest.headline,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (digest.supportingText.isNotBlank()) {
                Text(
                    text = digest.supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val signalRows = digest.primarySignals.take(4).chunked(2)
            if (signalRows.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    signalRows.forEach { rowSignals ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowSignals.forEach { signal ->
                                StatusSignalCard(
                                    text = signal,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowSignals.size == 1) {
                                Spacer(modifier = Modifier.width(0.dp).weight(1f))
                            }
                        }
                    }
                }
            }

            if (digest.secondarySignals.isNotEmpty()) {
                Text(
                    text = digest.secondarySignals.joinToString(separator = " · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (digest.showsPermissionAction) {
                TextButton(onClick = onRequestPermissions) {
                    Text(stringResource(R.string.workspace_request_permissions))
                }
            }
        }
    }
}

@Composable
private fun StatusSignalCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.84f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
