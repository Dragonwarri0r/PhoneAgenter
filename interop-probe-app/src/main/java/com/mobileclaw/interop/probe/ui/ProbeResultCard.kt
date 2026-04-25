package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import com.mobileclaw.interop.probe.model.ProbeValidationSeverity
import com.mobileclaw.interop.probe.ui.theme.ProbeAmber
import com.mobileclaw.interop.probe.ui.theme.ProbeBlueLight
import com.mobileclaw.interop.probe.ui.theme.ProbeGreen
import com.mobileclaw.interop.probe.ui.theme.ProbeRed

@Composable
fun ProbeResultCard(
    outcome: ProbeValidationOutcome,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (outcome.severity) {
                ProbeValidationSeverity.SUCCESS -> ProbeGreen.copy(alpha = 0.12f)
                ProbeValidationSeverity.WARNING -> ProbeAmber.copy(alpha = 0.12f)
                ProbeValidationSeverity.ERROR -> ProbeRed.copy(alpha = 0.12f)
                ProbeValidationSeverity.INFO -> ProbeBlueLight.copy(alpha = 0.4f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = outcome.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = outcome.statusLine,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = outcome.message,
                style = MaterialTheme.typography.bodyMedium,
            )
            outcome.detailLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
