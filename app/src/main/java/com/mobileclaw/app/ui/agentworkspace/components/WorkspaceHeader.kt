package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R

@Composable
fun WorkspaceHeader(
    sessionId: String?,
    panelsExpanded: Boolean,
    onModelClicked: () -> Unit,
    onGovernanceClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onTogglePanels: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.workspace_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sessionId ?: stringResource(R.string.workspace_no_active_session),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onTogglePanels) {
                Text(
                    if (panelsExpanded) {
                        stringResource(R.string.workspace_hide_panels)
                    } else {
                        stringResource(R.string.workspace_show_panels)
                    },
                )
            }
        }
        if (panelsExpanded) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onModelClicked) {
                    Text(stringResource(R.string.common_model))
                }
                TextButton(onClick = onGovernanceClicked) {
                    Text(stringResource(R.string.workspace_governance))
                }
                TextButton(onClick = onResetClicked, enabled = sessionId != null) {
                    Text(stringResource(R.string.common_reset))
                }
            }
        }
    }
}
