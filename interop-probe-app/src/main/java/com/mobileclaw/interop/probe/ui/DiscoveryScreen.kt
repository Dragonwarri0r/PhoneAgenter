package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.ProbeUiState
import com.mobileclaw.interop.probe.R

@Composable
fun DiscoveryScreen(
    uiState: ProbeUiState,
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
            text = stringResource(R.string.probe_discovery_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        val hostSummary = uiState.hostSummary
        if (hostSummary == null) {
            Text(
                text = stringResource(R.string.probe_discovery_empty),
                style = MaterialTheme.typography.bodyMedium,
            )
            uiState.timeline.firstOrNull()?.let { ProbeResultCard(outcome = it) }
            return@Column
        }
        CompatibilityBanner(hostSummary = hostSummary)
        Text(
            text = hostSummary.displayName,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = hostSummary.summary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.probe_label_surface),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = hostSummary.surfaceId,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = stringResource(R.string.probe_label_methods),
            style = MaterialTheme.typography.titleMedium,
        )
        hostSummary.methodLines.forEach { method ->
            Text(text = method, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = stringResource(R.string.probe_label_capabilities),
            style = MaterialTheme.typography.titleMedium,
        )
        hostSummary.capabilityLines.forEach { line ->
            Text(text = line, style = MaterialTheme.typography.bodySmall)
        }
        if (hostSummary.tagLines.isNotEmpty()) {
            Text(
                text = stringResource(R.string.probe_label_tags),
                style = MaterialTheme.typography.titleMedium,
            )
            hostSummary.tagLines.forEach { tag ->
                Text(text = tag, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
