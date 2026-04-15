package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InlineFailureBanner(
    headline: String? = null,
    message: String,
    supportingText: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (!headline.isNullOrBlank()) {
            Text(
                text = headline,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (!supportingText.isNullOrBlank()) {
            Text(
                text = supportingText,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
