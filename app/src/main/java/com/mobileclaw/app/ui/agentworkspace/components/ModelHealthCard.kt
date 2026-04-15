package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus
import com.mobileclaw.app.ui.agentworkspace.model.ModelHealthUiModel

@Composable
fun ModelHealthCard(
    modelHealth: ModelHealthUiModel,
    onChooseModel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (modelHealth.displayName.isBlank()) {
                        stringResource(R.string.model_health)
                    } else {
                        modelHealth.displayName
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = when (modelHealth.availabilityStatus) {
                        ModelAvailabilityStatus.READY -> stringResource(R.string.model_status_ready)
                        ModelAvailabilityStatus.PREPARING -> stringResource(R.string.model_status_preparing)
                        ModelAvailabilityStatus.FAILED -> stringResource(R.string.model_status_failed)
                        ModelAvailabilityStatus.UNAVAILABLE -> stringResource(R.string.model_status_unavailable)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = modelHealth.headline,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = modelHealth.supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onChooseModel) {
                Text(modelHealth.primaryActionLabel ?: stringResource(R.string.common_choose_model))
            }
        }
    }
}
