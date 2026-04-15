package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R

@Composable
fun QuickActionStrip(
    onQuickPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        stringResource(R.string.workspace_quick_action_summarize_today),
        stringResource(R.string.workspace_quick_action_plan_next_steps),
        stringResource(R.string.workspace_quick_action_explain_context),
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { option ->
            AssistChip(
                onClick = { onQuickPromptSelected(option) },
                label = { Text(option) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            )
        }
    }
}
