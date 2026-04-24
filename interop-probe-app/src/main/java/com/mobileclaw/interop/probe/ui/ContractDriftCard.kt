package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.R
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome

@Composable
fun ContractDriftCard(
    outcomes: List<ProbeValidationOutcome>,
    onRunDriftCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.probe_drift_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.probe_drift_summary),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRunDriftCheck) {
                Text(stringResource(R.string.probe_action_run_drift_check))
            }
            if (outcomes.isNotEmpty()) {
                ValidationTimeline(outcomes = outcomes)
            }
        }
    }
}
