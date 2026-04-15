package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceSecondaryEntryUiModel

@Composable
fun WorkspaceSecondaryEntryRow(
    entries: List<WorkspaceSecondaryEntryUiModel>,
    onEntryClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        entries.forEach { entry ->
            AssistChip(
                onClick = { onEntryClicked(entry.entryId) },
                label = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = entry.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (entry.supportingText.isNotBlank()) {
                            Text(
                                text = entry.supportingText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (entry.isHighlighted) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    },
                ),
            )
        }
    }
}
