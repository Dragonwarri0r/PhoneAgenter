package com.mobileclaw.app.ui.agentworkspace.components

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
import com.mobileclaw.app.ui.agentworkspace.model.KnowledgeAreaUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ManagedKnowledgeEntryUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeCenterSheet(
    knowledgeArea: KnowledgeAreaUiModel,
    selectedEntry: ManagedKnowledgeEntryUiModel?,
    onDismiss: () -> Unit,
    onAddFile: () -> Unit,
    onAddCollection: () -> Unit,
    onOpenAsset: (String) -> Unit,
    onCloseAsset: () -> Unit,
    onRefreshAsset: (String) -> Unit,
    onToggleRetrievalInclusion: (String, Boolean) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        if (selectedEntry == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = knowledgeArea.title,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (knowledgeArea.headline.isNotBlank()) {
                    Text(
                        text = knowledgeArea.headline,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (knowledgeArea.supportingText.isNotBlank()) {
                    Text(
                        text = knowledgeArea.supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = onAddFile) {
                        Text(stringResource(R.string.knowledge_action_add_file))
                    }
                    TextButton(onClick = onAddCollection) {
                        Text(stringResource(R.string.knowledge_action_add_collection))
                    }
                }
                if (knowledgeArea.entries.isEmpty()) {
                    Text(
                        text = knowledgeArea.emptyState,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    knowledgeArea.entries.forEach { entry ->
                        ManagedKnowledgeEntryCard(
                            entry = entry,
                            onOpen = { onOpenAsset(entry.knowledgeAssetId) },
                        )
                    }
                }
            }
        } else {
            KnowledgeAssetDetailContent(
                entry = selectedEntry,
                onCloseAsset = onCloseAsset,
                onRefresh = { onRefreshAsset(selectedEntry.knowledgeAssetId) },
                onToggleRetrievalInclusion = {
                    onToggleRetrievalInclusion(
                        selectedEntry.knowledgeAssetId,
                        !selectedEntry.retrievalIncluded,
                    )
                },
            )
        }
    }
}

@Composable
private fun ManagedKnowledgeEntryCard(
    entry: ManagedKnowledgeEntryUiModel,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (entry.statusLine.isNotBlank()) {
                Text(
                    text = entry.statusLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (entry.freshnessLine.isNotBlank()) {
                Text(
                    text = entry.freshnessLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.usageSummary.isNotBlank()) {
                Text(
                    text = entry.usageSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.limitationSummary.isNotBlank()) {
                Text(
                    text = entry.limitationSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onOpen) {
                Text(stringResource(R.string.knowledge_action_inspect))
            }
        }
    }
}

@Composable
private fun KnowledgeAssetDetailContent(
    entry: ManagedKnowledgeEntryUiModel,
    onCloseAsset: () -> Unit,
    onRefresh: () -> Unit,
    onToggleRetrievalInclusion: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.titleLarge,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCloseAsset) {
                Text(stringResource(R.string.knowledge_action_back))
            }
        }
        listOfNotNull(
            entry.statusLine.takeIf { it.isNotBlank() },
            entry.freshnessLine.takeIf { it.isNotBlank() },
            entry.usageSummary.takeIf { it.isNotBlank() },
            entry.provenanceLabel.takeIf { it.isNotBlank() },
        ).forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        entry.detailLines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (entry.citationLines.isNotEmpty()) {
            Text(
                text = stringResource(R.string.knowledge_detail_recent_support),
                style = MaterialTheme.typography.titleSmall,
            )
            entry.citationLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (entry.limitationSummary.isNotBlank()) {
            Text(
                text = entry.limitationSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (entry.canRefresh) {
                TextButton(onClick = onRefresh) {
                    Text(stringResource(R.string.knowledge_action_refresh))
                }
            }
            if (entry.canToggleRetrievalInclusion) {
                TextButton(onClick = onToggleRetrievalInclusion) {
                    Text(
                        stringResource(
                            if (entry.retrievalIncluded) {
                                R.string.knowledge_action_exclude
                            } else {
                                R.string.knowledge_action_include
                            },
                        ),
                    )
                }
            }
        }
    }
}
