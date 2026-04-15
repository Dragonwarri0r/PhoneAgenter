package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceFeedbackKind
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceFeedbackUiModel

@Composable
fun WorkspaceFeedbackHost(
    feedback: WorkspaceFeedbackUiModel?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = feedback != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        feedback ?: return@AnimatedVisibility
        val textColor = when (feedback.kind) {
            WorkspaceFeedbackKind.ERROR -> MaterialTheme.colorScheme.error
            WorkspaceFeedbackKind.SUCCESS -> MaterialTheme.colorScheme.tertiary
            WorkspaceFeedbackKind.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        val backgroundColor = when (feedback.kind) {
            WorkspaceFeedbackKind.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.58f)
            WorkspaceFeedbackKind.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
            WorkspaceFeedbackKind.INFO -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.86f)
        }
        Text(
            text = feedback.text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(18.dp),
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}
