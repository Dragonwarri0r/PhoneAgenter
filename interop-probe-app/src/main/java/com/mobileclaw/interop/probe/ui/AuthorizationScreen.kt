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
fun AuthorizationScreen(
    uiState: ProbeUiState,
    onRequestAuthorization: () -> Unit,
    onRefreshGrant: () -> Unit,
    onRevokeGrant: () -> Unit,
    onOpenHost: () -> Unit,
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
            text = stringResource(R.string.probe_authorization_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.probe_authorization_summary),
            style = MaterialTheme.typography.bodyMedium,
        )
        uiState.latestAuthorizationOutcome?.let { ProbeResultCard(outcome = it) }
        uiState.latestGrantDescriptor?.let { descriptor ->
            Text(
                text = stringResource(R.string.probe_authorization_grant_handle, descriptor.handle.opaqueValue),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.probe_authorization_scope_summary, descriptor.scopes.joinToString()),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onRequestAuthorization) {
                Text(stringResource(R.string.probe_action_request_authorization))
            }
            OutlinedButton(onClick = onRefreshGrant) {
                Text(stringResource(R.string.probe_action_refresh_grant))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onRevokeGrant) {
                Text(stringResource(R.string.probe_action_revoke_grant))
            }
            OutlinedButton(onClick = onOpenHost) {
                Text(stringResource(R.string.probe_action_open_host))
            }
        }
    }
}
