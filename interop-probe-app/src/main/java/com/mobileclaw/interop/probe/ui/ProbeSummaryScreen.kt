package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.ProbeUiState
import com.mobileclaw.interop.probe.R

@Composable
fun ProbeSummaryScreen(
    uiState: ProbeUiState,
    summaryText: String,
    onShareSummary: () -> Unit,
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
            text = stringResource(R.string.probe_summary_screen_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onShareSummary) {
            Text(stringResource(R.string.probe_share_summary))
        }
        ValidationTimeline(outcomes = uiState.timeline)
    }
}
