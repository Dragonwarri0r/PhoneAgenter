package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.ChatRoleUi
import com.mobileclaw.app.ui.agentworkspace.model.ChatTurnStateUi
import com.mobileclaw.app.ui.agentworkspace.model.ChatTurnUiModel
import com.mobileclaw.app.ui.theme.AtriumGlass
import com.mobileclaw.app.ui.theme.atriumPrimaryBrush

@Composable
fun MessageBubble(
    turn: ChatTurnUiModel,
    modifier: Modifier = Modifier,
) {
    val isUser = turn.role == ChatRoleUi.USER
    val shape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = if (isUser) 28.dp else 12.dp,
        bottomEnd = if (isUser) 12.dp else 28.dp,
    )
    val background = if (isUser) {
        atriumPrimaryBrush()
    } else {
        Brush.linearGradient(
            listOf(
                AtriumGlass,
                MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.78f),
            ),
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Column {
            Text(
                text = if (isUser) {
                    stringResource(R.string.workspace_you)
                } else {
                    stringResource(R.string.workspace_agent)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Text(
                text = turn.content,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(top = 4.dp),
            )
            if (turn.state == ChatTurnStateUi.STREAMING) {
                Text(
                    text = stringResource(R.string.workspace_streaming),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
