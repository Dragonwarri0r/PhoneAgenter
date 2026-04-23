package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R

@Composable
fun QuickActionStrip(
    onQuickPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        QuickActionOption(
            prompt = stringResource(R.string.workspace_quick_action_summarize_today),
            supportingText = stringResource(R.string.workspace_quick_action_summarize_today_desc),
        ),
        QuickActionOption(
            prompt = stringResource(R.string.workspace_quick_action_plan_next_steps),
            supportingText = stringResource(R.string.workspace_quick_action_plan_next_steps_desc),
        ),
        QuickActionOption(
            prompt = stringResource(R.string.workspace_quick_action_explain_context),
            supportingText = stringResource(R.string.workspace_quick_action_explain_context_desc),
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuickActionCard(
            option = options.first(),
            emphasized = true,
            onClick = { onQuickPromptSelected(options.first().prompt) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.drop(1).forEach { option ->
                QuickActionCard(
                    option = option,
                    emphasized = false,
                    onClick = { onQuickPromptSelected(option.prompt) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (options.size == 2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private data class QuickActionOption(
    val prompt: String,
    val supportingText: String,
)

@Composable
private fun QuickActionCard(
    option: QuickActionOption,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = option.prompt,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = option.supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
