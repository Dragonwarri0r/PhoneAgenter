package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome

@Composable
fun ValidationTimeline(
    outcomes: List<ProbeValidationOutcome>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        outcomes.forEach { outcome ->
            ProbeResultCard(outcome = outcome)
        }
    }
}
