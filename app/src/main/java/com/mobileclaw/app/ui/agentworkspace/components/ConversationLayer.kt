package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.ChatRoleUi
import com.mobileclaw.app.ui.agentworkspace.model.ChatTurnUiModel

@Composable
fun ConversationLayer(
    turns: List<ChatTurnUiModel>,
    contentPadding: PaddingValues = PaddingValues(bottom = 4.dp),
    emptyContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val transcriptDescription = stringResource(R.string.workspace_cd_conversation_transcript)

    LaunchedEffect(turns.size) {
        if (turns.isNotEmpty()) {
            listState.animateScrollToItem(turns.lastIndex)
        }
    }

    if (turns.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (emptyContent != null) {
                emptyContent()
            } else {
                Text(
                    text = stringResource(R.string.workspace_start_first_exchange),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .semantics { contentDescription = transcriptDescription },
            state = listState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(turns, key = { it.turnId }) { turn ->
                val alignment = if (turn.role == ChatRoleUi.USER) {
                    Alignment.CenterEnd
                } else {
                    Alignment.CenterStart
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    contentAlignment = alignment,
                ) {
                    MessageBubble(
                        turn = turn,
                        modifier = Modifier.fillMaxWidth(0.84f),
                    )
                }
            }
        }
    }
}
