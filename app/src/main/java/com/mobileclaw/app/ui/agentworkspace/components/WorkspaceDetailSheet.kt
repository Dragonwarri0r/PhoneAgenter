package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.model.AuditUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ContextInspectorUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ModelHealthUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeStatusUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetailSheet(
    modelHealth: ModelHealthUiModel,
    runtimeStatus: RuntimeStatusUiModel,
    contextInspector: ContextInspectorUiModel,
    recentAudit: List<AuditUiModel>,
    sessionId: String?,
    onDismiss: () -> Unit,
    onChooseModel: () -> Unit,
    onInspectContext: () -> Unit,
    onRequestSystemPermissions: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.workspace_details_title),
                style = MaterialTheme.typography.titleLarge,
            )
            ModelHealthCard(
                modelHealth = modelHealth,
                onChooseModel = onChooseModel,
            )
            ContextWindowCard(
                runtimeStatus = runtimeStatus,
                contextInspector = contextInspector,
                recentAudit = recentAudit,
                sessionId = sessionId,
                onInspectClicked = onInspectContext,
                onRequestSystemPermissions = onRequestSystemPermissions,
            )
        }
    }
}
