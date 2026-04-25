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
fun ProbeHomeScreen(
    uiState: ProbeUiState,
    onHostPackageChanged: (String) -> Unit,
    onRequestedVersionChanged: (String) -> Unit,
    onInvocationInputChanged: (String) -> Unit,
    onDiscover: () -> Unit,
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
            text = stringResource(R.string.probe_home_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.probe_home_summary),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = uiState.hostPackageName,
            onValueChange = onHostPackageChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.probe_field_host_package)) },
        )
        OutlinedTextField(
            value = uiState.requestedVersion,
            onValueChange = onRequestedVersionChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.probe_field_requested_version)) },
        )
        OutlinedTextField(
            value = uiState.invocationInput,
            onValueChange = onInvocationInputChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.probe_field_input)) },
        )
        Button(onClick = onDiscover) {
            Text(stringResource(R.string.probe_action_discover))
        }
        uiState.timeline.firstOrNull()?.let { ProbeResultCard(outcome = it) }
    }
}
