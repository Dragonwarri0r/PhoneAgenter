package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.ProbeUiState
import com.mobileclaw.interop.probe.R

@Composable
fun TaskScreen(
    uiState: ProbeUiState,
    onPollTask: () -> Unit,
    onLoadArtifact: () -> Unit,
    onRunDriftCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.probe_task_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        uiState.latestTask?.let { task ->
            Text(
                text = stringResource(R.string.probe_task_handle, task.handle.opaqueValue),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.probe_task_status_line, task.displayName, task.statusLabel),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = task.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } ?: Text(
            text = stringResource(R.string.probe_task_empty),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onPollTask) {
                Text(stringResource(R.string.probe_action_poll_task))
            }
            OutlinedButton(onClick = onLoadArtifact) {
                Text(stringResource(R.string.probe_action_load_artifact))
            }
        }
        uiState.latestArtifact?.let { artifact ->
            Text(
                text = stringResource(R.string.probe_artifact_handle, artifact.handle.opaqueValue),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.probe_artifact_mime, artifact.mimeType),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = artifact.summary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        ContractDriftCard(
            outcomes = uiState.driftOutcomes,
            onRunDriftCheck = onRunDriftCheck,
        )
    }
}
