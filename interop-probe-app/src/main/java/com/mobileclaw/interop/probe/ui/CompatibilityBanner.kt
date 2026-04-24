package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.interop.probe.R
import com.mobileclaw.interop.probe.model.ProbeHostSummary
import com.mobileclaw.interop.probe.ui.theme.ProbeAmber
import com.mobileclaw.interop.probe.ui.theme.ProbeBlueLight
import com.mobileclaw.interop.probe.ui.theme.ProbeRed
import com.mobileclaw.interop.contract.CompatibilityState

@Composable
fun CompatibilityBanner(
    hostSummary: ProbeHostSummary,
    modifier: Modifier = Modifier,
) {
    val signal = hostSummary.compatibilitySignal
    val containerColor = when (signal.compatibilityState) {
        CompatibilityState.SUPPORTED -> ProbeBlueLight.copy(alpha = 0.5f)
        CompatibilityState.DOWNGRADED -> ProbeAmber.copy(alpha = 0.15f)
        CompatibilityState.INCOMPATIBLE -> ProbeRed.copy(alpha = 0.12f)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.probe_banner_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    R.string.probe_banner_state,
                    when (signal.compatibilityState) {
                        CompatibilityState.SUPPORTED -> stringResource(R.string.probe_compatibility_supported)
                        CompatibilityState.DOWNGRADED -> stringResource(R.string.probe_compatibility_downgraded)
                        CompatibilityState.INCOMPATIBLE -> stringResource(R.string.probe_compatibility_incompatible)
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    R.string.probe_banner_supported_version,
                    signal.supportedVersion,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = signal.compatibilityReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
