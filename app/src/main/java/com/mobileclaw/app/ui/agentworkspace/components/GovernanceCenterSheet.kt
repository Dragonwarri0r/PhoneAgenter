package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.GovernanceCenterUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernanceCenterSheet(
    governance: GovernanceCenterUiModel,
    onUpdateTrust: (callerId: String, trustModeKey: String) -> Unit,
    onUpdateScope: (callerId: String, scopeId: String, stateKey: String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.governance_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.governance_recent_callers),
                style = MaterialTheme.typography.titleMedium,
            )
            if (governance.callers.isEmpty()) {
                Text(
                    text = stringResource(R.string.governance_no_callers),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            governance.callers.forEach { caller ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = caller.displayLabel,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = caller.lastSeenLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = caller.lastDecisionLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.governance_trust_mode),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        caller.availableTrustModes.forEach { option ->
                            AssistChip(
                                onClick = { onUpdateTrust(caller.callerId, option.key) },
                                label = { Text(option.label) },
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.governance_scope_grants),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    caller.scopeGrants.forEach { scopeGrant ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${scopeGrant.scopeLabel} · ${scopeGrant.stateLabel}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                scopeGrant.availableStates.forEach { option ->
                                    AssistChip(
                                        onClick = {
                                            onUpdateScope(caller.callerId, scopeGrant.scopeId, option.key)
                                        },
                                        label = { Text(option.label) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Text(
                text = stringResource(R.string.governance_contributors_title),
                style = MaterialTheme.typography.titleMedium,
            )
            if (governance.contributors.isEmpty()) {
                Text(
                    text = stringResource(R.string.governance_no_contributors),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            governance.contributors.forEach { contributor ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = contributor.title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = contributor.availabilityLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = contributor.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    contributor.governanceLines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    contributor.limitationSummary
                        .takeIf { it.isNotBlank() }
                        ?.let { limitation ->
                            Text(
                                text = limitation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                }
            }
            Text(
                text = stringResource(R.string.governance_recent_activity),
                style = MaterialTheme.typography.titleMedium,
            )
            if (governance.activities.isEmpty()) {
                Text(
                    text = stringResource(R.string.governance_no_activity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            governance.activities.forEach { activity ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = activity.headline,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = activity.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = activity.timestampLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
