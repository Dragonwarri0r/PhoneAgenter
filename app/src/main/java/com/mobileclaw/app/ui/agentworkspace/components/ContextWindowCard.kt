package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.AuditUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ContextInspectorUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeStatusUiModel

@Composable
fun ContextWindowCard(
    runtimeStatus: RuntimeStatusUiModel,
    contextInspector: ContextInspectorUiModel,
    recentAudit: List<AuditUiModel>,
    sessionId: String?,
    onInspectClicked: () -> Unit,
    onRequestSystemPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (runtimeStatus.isTerminal) {
                MaterialTheme.colorScheme.surfaceContainerLowest
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.workspace_context_window),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = runtimeStatus.stageLabel,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    runtimeStatus.awaitingInput -> MaterialTheme.colorScheme.primary
                    runtimeStatus.isTerminal -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                },
            )
            Text(
                text = runtimeStatus.supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = runtimeStatus.headline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (runtimeStatus.sourceLabel.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_source_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.sourceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (runtimeStatus.trustStateLabel.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_source_trust),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.trustStateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (runtimeStatus.interopContractLabel.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_interop_contract),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.interopContractLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (runtimeStatus.uriGrantLabel.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_uri_grants),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.uriGrantLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (runtimeStatus.routeSummary.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_route_summary),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.routeSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (runtimeStatus.toolDisplayName.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_tool_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.toolDisplayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (runtimeStatus.toolSideEffectLabel.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_tool_side_effect),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.toolSideEffectLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (runtimeStatus.toolScopeLines.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.workspace_tool_scope),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                runtimeStatus.toolScopeLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (runtimeStatus.toolVisibilityLabel.isNotBlank() || runtimeStatus.toolVisibilityReason.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_tool_visibility),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                runtimeStatus.toolVisibilityLabel
                    .takeIf { it.isNotBlank() }
                    ?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                runtimeStatus.toolVisibilityReason
                    .takeIf { it.isNotBlank() }
                    ?.let { reason ->
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
            if (runtimeStatus.callerTrust.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_caller_trust),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.callerTrust,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (runtimeStatus.structuredActionTitle.isNotBlank()) {
                Text(
                    text = stringResource(R.string.workspace_structured_action),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = runtimeStatus.structuredActionTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (runtimeStatus.structuredCompleteness.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.workspace_structured_completeness),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = runtimeStatus.structuredCompleteness,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                runtimeStatus.structuredFieldLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (runtimeStatus.structuredWarnings.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.workspace_structured_warnings),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    runtimeStatus.structuredWarnings.forEach { warning ->
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (runtimeStatus.systemSourceStatusLines.isNotEmpty() || runtimeStatus.systemSourceContributionLines.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.workspace_system_sources),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                runtimeStatus.systemSourceStatusLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                runtimeStatus.systemSourceContributionLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (runtimeStatus.hasMissingSystemSourcePermissions) {
                    TextButton(
                        onClick = onRequestSystemPermissions,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.workspace_request_permissions))
                    }
                }
            }
            if (runtimeStatus.contributionSummaryLines.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.runtime_control_section_contributions),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                runtimeStatus.contributionSummaryLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                runtimeStatus.contributionDetailLines.take(2).forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                runtimeStatus.contributionDetailLines.drop(2).take(2).forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (runtimeStatus.knowledgeSupportLines.isNotEmpty() || runtimeStatus.knowledgeCitationLines.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.runtime_control_section_knowledge),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                runtimeStatus.knowledgeSupportLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                runtimeStatus.knowledgeCitationLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                runtimeStatus.knowledgeLimitationSummary
                    .takeIf { it.isNotBlank() }
                    ?.let { limitation ->
                        Text(
                            text = limitation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
            if (runtimeStatus.extensionStatusLines.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.workspace_extension_discovery),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                runtimeStatus.extensionStatusLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (recentAudit.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.workspace_recent_audit),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                recentAudit.take(1).forEach { item ->
                    if (item.toolDisplayName.isNotBlank()) {
                        Text(
                            text = item.toolDisplayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (item.sideEffectLabel.isNotBlank()) {
                        Text(
                            text = item.sideEffectLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = item.headline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (contextInspector.activeMemoryItems.isNotEmpty()) {
                Text(
                    text = contextInspector.activeMemoryItems.joinToString(separator = "  •  ") { it.title },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = onInspectClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.workspace_inspect_context))
            }
            if (sessionId != null) {
                Text(
                    text = sessionId,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
