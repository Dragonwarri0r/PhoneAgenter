package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.ApprovalUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalSheet(
    approval: ApprovalUiModel,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = approval.title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = approval.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (approval.toolDisplayName.isNotBlank()) {
                Text(
                    text = approval.toolDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (approval.sideEffectLabel.isNotBlank()) {
                Text(
                    text = approval.sideEffectLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            approval.scopeLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = approval.previewPayload,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            approval.previewLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onApprove,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(approval.primaryActionLabel)
            }
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(approval.secondaryActionLabel)
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = androidx.compose.ui.res.stringResource(R.string.common_cancel))
            }
        }
    }
}
