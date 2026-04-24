package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.ProbeUiState
import com.mobileclaw.interop.probe.R

@Composable
fun InvocationScreen(
    uiState: ProbeUiState,
    onInvocationInputChanged: (String) -> Unit,
    onInvoke: () -> Unit,
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
            text = stringResource(R.string.probe_invocation_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = uiState.invocationInput,
            onValueChange = onInvocationInputChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.probe_field_input)) },
        )
        Button(onClick = onInvoke) {
            Text(stringResource(R.string.probe_action_invoke))
        }
        uiState.latestInvocationOutcome?.let { ProbeResultCard(outcome = it) }
        uiState.latestTask?.let { task ->
            Text(
                text = stringResource(R.string.probe_invocation_task_summary, task.displayName, task.statusLabel),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (uiState.timeline.isNotEmpty()) {
            ValidationTimeline(outcomes = uiState.timeline.take(3))
        }
    }
}
