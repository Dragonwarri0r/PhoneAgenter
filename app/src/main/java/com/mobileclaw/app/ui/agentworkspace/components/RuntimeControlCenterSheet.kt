package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.ManagedArtifactEntryUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeControlCenterUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeTraceSectionUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceAttentionMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuntimeControlCenterSheet(
    controlCenter: RuntimeControlCenterUiModel,
    onDismiss: () -> Unit,
    onOpenArtifact: (artifactId: String) -> Unit,
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
                text = controlCenter.title,
                style = MaterialTheme.typography.titleLarge,
            )
            if (controlCenter.headline.isNotBlank()) {
                Text(
                    text = controlCenter.headline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (controlCenter.supportingText.isNotBlank()) {
                Text(
                    text = controlCenter.supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(R.string.runtime_control_trace_title),
                style = MaterialTheme.typography.titleMedium,
            )
            controlCenter.traceSections.forEach { section ->
                TraceSectionCard(section = section)
            }

            Text(
                text = stringResource(R.string.runtime_control_artifacts_title),
                style = MaterialTheme.typography.titleMedium,
            )
            controlCenter.artifactEntries.forEach { entry ->
                ManagedArtifactCard(
                    entry = entry,
                    onOpen = { onOpenArtifact(entry.artifactId) },
                )
            }
        }
    }
}

@Composable
private fun TraceSectionCard(
    section: RuntimeTraceSectionUiModel,
) {
    val containerColor = if (section.isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (section.lines.isEmpty()) {
                Text(
                    text = section.emptyState,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                section.lines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagedArtifactCard(
    entry: ManagedArtifactEntryUiModel,
    onOpen: () -> Unit,
) {
    val containerColor = if (entry.isEditable) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
            )
            if (entry.summary.isNotBlank()) {
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.statusLine.isNotBlank()) {
                Text(
                    text = entry.statusLine,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (entry.unavailableReason.isNotBlank()) {
                Text(
                    text = entry.unavailableReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.isEditable) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            if (entry.actionLabel.isNotBlank()) {
                TextButton(
                    onClick = onOpen,
                    enabled = entry.isEditable,
                ) {
                    Text(entry.actionLabel)
                }
            }
        }
    }
}
