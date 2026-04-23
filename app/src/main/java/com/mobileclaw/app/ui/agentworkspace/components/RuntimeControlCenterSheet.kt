package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.ManagedArtifactEntryUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeControlCenterUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeTraceSectionUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceAttentionMode

private enum class ControlCenterPage(
    val sectionIds: Set<String>,
    val titleRes: Int,
) {
    OVERVIEW(
        sectionIds = setOf("selection", "source", "tool", "approval", "constraints", "recent_activity"),
        titleRes = R.string.runtime_control_tab_overview,
    ),
    CONTEXT(
        sectionIds = setOf("context", "knowledge", "contributions", "extensions"),
        titleRes = R.string.runtime_control_tab_context,
    ),
    AUTOMATION(
        sectionIds = setOf("automation"),
        titleRes = R.string.runtime_control_tab_automation,
    ),
    MANAGE(
        sectionIds = emptySet(),
        titleRes = R.string.runtime_control_tab_manage,
    ),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuntimeControlCenterSheet(
    controlCenter: RuntimeControlCenterUiModel,
    onDismiss: () -> Unit,
    onOpenArtifact: (artifactId: String) -> Unit,
) {
    var selectedPage by remember { mutableStateOf(ControlCenterPage.OVERVIEW) }
    val visibleTraceSections = remember(controlCenter.traceSections, selectedPage) {
        controlCenter.traceSections.filter { it.sectionId in selectedPage.sectionIds }
    }

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
            RuntimeStatusHero(
                headline = controlCenter.headline,
                supportingText = controlCenter.supportingText,
                attentionMode = controlCenter.attentionMode,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ControlCenterPage.entries.forEach { page ->
                    FilterChip(
                        selected = page == selectedPage,
                        onClick = { selectedPage = page },
                        label = { Text(stringResource(page.titleRes)) },
                    )
                }
            }
            if (selectedPage == ControlCenterPage.MANAGE) {
                controlCenter.artifactEntries.forEach { entry ->
                    ManagedArtifactCard(
                        entry = entry,
                        onOpen = { onOpenArtifact(entry.artifactId) },
                    )
                }
            } else {
                visibleTraceSections.forEach { section ->
                    TraceSectionCard(section = section)
                }
            }
        }
    }
}

@Composable
private fun RuntimeStatusHero(
    headline: String,
    supportingText: String,
    attentionMode: WorkspaceAttentionMode,
) {
    val containerColor = when (attentionMode) {
        WorkspaceAttentionMode.AWAITING_APPROVAL -> MaterialTheme.colorScheme.primaryContainer
        WorkspaceAttentionMode.FAILURE -> MaterialTheme.colorScheme.errorContainer
        WorkspaceAttentionMode.PREPARING -> MaterialTheme.colorScheme.secondaryContainer
        WorkspaceAttentionMode.UNAVAILABLE -> MaterialTheme.colorScheme.surfaceContainer
        WorkspaceAttentionMode.NORMAL -> MaterialTheme.colorScheme.surfaceContainerLowest
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (headline.isNotBlank()) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (supportingText.isNotBlank()) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            entry.detailLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.unavailableReason.isNotBlank()) {
                Text(
                    text = entry.unavailableReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
