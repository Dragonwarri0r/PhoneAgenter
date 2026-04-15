package com.mobileclaw.app.ui.agentworkspace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.components.ApprovalSheet
import com.mobileclaw.app.ui.agentworkspace.components.ComposerDock
import com.mobileclaw.app.ui.agentworkspace.components.ContextInspectorSheet
import com.mobileclaw.app.ui.agentworkspace.components.ConversationLayer
import com.mobileclaw.app.ui.agentworkspace.components.GovernanceCenterSheet
import com.mobileclaw.app.ui.agentworkspace.components.InlineFailureBanner
import com.mobileclaw.app.ui.agentworkspace.components.ModelPickerSheet
import com.mobileclaw.app.ui.agentworkspace.components.PortabilityBundleSheet
import com.mobileclaw.app.ui.agentworkspace.components.QuickActionStrip
import com.mobileclaw.app.ui.agentworkspace.components.ResetSessionDialog
import com.mobileclaw.app.ui.agentworkspace.components.RuntimeControlCenterSheet
import com.mobileclaw.app.ui.agentworkspace.components.WorkspaceEmptyState
import com.mobileclaw.app.ui.agentworkspace.components.WorkspaceFeedbackHost
import com.mobileclaw.app.ui.agentworkspace.components.WorkspaceHeader
import com.mobileclaw.app.ui.agentworkspace.components.WorkspaceSecondaryEntryRow
import com.mobileclaw.app.ui.agentworkspace.components.WorkspaceStatusDigest

@Composable
fun AgentWorkspaceScreen(
    viewModel: AgentWorkspaceViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModelPicker by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showContextInspector by remember { mutableStateOf(false) }
    var showGovernanceCenter by remember { mutableStateOf(false) }
    var showRuntimeControlCenter by remember { mutableStateOf(false) }
    var topPanelsExpanded by remember { mutableStateOf(true) }
    var restoreExpandedAfterIme by remember { mutableStateOf(false) }
    var topChromeHeightPx by remember { mutableStateOf(0) }
    var bottomChromeHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onImportModel(uri)
        }
    }
    val imageAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onAddImageAttachment(uri)
        }
    }
    val audioAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onAddAudioAttachment(uri)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.onSystemPermissionsChanged()
    }

    LaunchedEffect(uiState.feedback?.messageId) {
        if (uiState.feedback != null) {
            kotlinx.coroutines.delay(2200)
            viewModel.dismissFeedback()
        }
    }
    LaunchedEffect(imeVisible) {
        if (imeVisible && topPanelsExpanded) {
            restoreExpandedAfterIme = true
            topPanelsExpanded = false
        } else if (!imeVisible && restoreExpandedAfterIme) {
            topPanelsExpanded = true
            restoreExpandedAfterIme = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        val supportsImage = uiState.activeModel?.modalityCapabilities?.supportsImage == true ||
            uiState.modelHealth.supportsImage
        val supportsAudio = uiState.activeModel?.modalityCapabilities?.supportsAudio == true ||
            uiState.modelHealth.supportsAudio
        val topChromeHeight = with(density) { topChromeHeightPx.toDp() }
        val bottomChromeHeight = with(density) { bottomChromeHeightPx.toDp() }
        val topContentPadding = 12.dp + if (topChromeHeight > 0.dp) topChromeHeight else 128.dp
        val bottomContentPadding = 12.dp + if (bottomChromeHeight > 0.dp) bottomChromeHeight else 132.dp

        WorkspacePlaceholderCard(
            title = "",
            body = "",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topContentPadding, bottom = bottomContentPadding),
            bodyContent = {
                if (uiState.screenState == WorkspaceScreenState.PREPARING || uiState.screenState == WorkspaceScreenState.UNAVAILABLE) {
                    WorkspaceEmptyState(
                        title = if (uiState.screenState == WorkspaceScreenState.PREPARING) {
                            stringResource(R.string.workspace_preparing_local_runtime)
                        } else {
                            stringResource(R.string.workspace_no_ready_local_model)
                        },
                        body = uiState.runtimeStatus.supportingText,
                        actionLabel = stringResource(R.string.common_choose_model),
                        onAction = { showModelPicker = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ConversationLayer(
                        turns = uiState.turns,
                        contentPadding = PaddingValues(bottom = 4.dp),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topChromeHeightPx = it.height },
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            WorkspaceHeader(
                sessionId = uiState.activeSessionId,
                panelsExpanded = topPanelsExpanded,
                onModelClicked = { showModelPicker = true },
                onGovernanceClicked = { showGovernanceCenter = true },
                onResetClicked = { showResetDialog = true },
                onTogglePanels = { topPanelsExpanded = !topPanelsExpanded },
            )
            AnimatedVisibility(visible = topPanelsExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    WorkspaceStatusDigest(
                        digest = uiState.statusDigest,
                        onOpenDetails = { showRuntimeControlCenter = true },
                        onRequestPermissions = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_CONTACTS,
                                    android.Manifest.permission.READ_CALENDAR,
                                ),
                            )
                        },
                    )
                    if (uiState.screenState == WorkspaceScreenState.RECOVERABLE_FAILURE) {
                        InlineFailureBanner(
                            headline = uiState.recentAudit.firstOrNull()?.headline,
                            message = uiState.runtimeStatus.supportingText,
                            supportingText = listOfNotNull(
                                uiState.runtimeStatus.sourceLabel.takeIf { it.isNotBlank() },
                                uiState.runtimeStatus.trustStateLabel.takeIf { it.isNotBlank() },
                            ).joinToString(separator = " · ").takeIf { it.isNotBlank() },
                        )
                    }
                    WorkspaceSecondaryEntryRow(
                        entries = uiState.secondaryEntries,
                        onEntryClicked = { entryId ->
                            when (entryId) {
                                "model" -> showModelPicker = true
                                "context" -> showContextInspector = true
                                "governance" -> showGovernanceCenter = true
                                "details" -> showRuntimeControlCenter = true
                            }
                        },
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { bottomChromeHeightPx = it.height },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(visible = uiState.isModelReady) {
                QuickActionStrip(
                    onQuickPromptSelected = viewModel::onQuickPromptSelected,
                )
            }
            ComposerDock(
                draft = uiState.composerDraft,
                enabled = uiState.isComposerEnabled,
                supportsImage = supportsImage,
                supportsAudio = supportsAudio,
                pendingAttachments = uiState.pendingAttachments,
                activeAttachments = uiState.activeAttachments,
                onDraftChanged = viewModel::onDraftChanged,
                onAddImageClicked = { imageAttachmentLauncher.launch(arrayOf("image/*")) },
                onAddAudioClicked = { audioAttachmentLauncher.launch(arrayOf("audio/*")) },
                onRemoveAttachment = viewModel::onRemovePendingAttachment,
                onSendClicked = viewModel::onSendClicked,
            )
        }
        WorkspaceFeedbackHost(
            feedback = uiState.feedback,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomChromeHeight + 8.dp)
                .padding(horizontal = 4.dp),
        )
        if (showModelPicker) {
            ModelPickerSheet(
                models = uiState.availableModels,
                currentModelId = uiState.activeModel?.modelId,
                onDismiss = { showModelPicker = false },
                onModelSelected = viewModel::onSelectModel,
                onToggleImageSupport = { modelId, enabled ->
                    viewModel.onUpdateModelCapabilities(
                        modelId = modelId,
                        supportsImage = enabled,
                    )
                },
                onToggleAudioSupport = { modelId, enabled ->
                    viewModel.onUpdateModelCapabilities(
                        modelId = modelId,
                        supportsAudio = enabled,
                    )
                },
                onImportModel = { importModelLauncher.launch(arrayOf("*/*")) },
            )
        }
        if (showResetDialog) {
            ResetSessionDialog(
                onDismiss = { showResetDialog = false },
                onConfirm = {
                    showResetDialog = false
                    viewModel.onResetSessionConfirmed()
                },
            )
        }
        if (showContextInspector) {
            ContextInspectorSheet(
                inspector = uiState.contextInspector,
                onDismiss = { showContextInspector = false },
                onCyclePersonaVerbosity = viewModel::onCyclePersonaVerbosity,
                onTogglePin = viewModel::onToggleMemoryPin,
                onPromote = viewModel::onPromoteMemory,
                onDemote = viewModel::onDemoteMemory,
                onExpire = viewModel::onExpireMemory,
                onPreviewExport = viewModel::onOpenPortabilityPreview,
            )
        }
        uiState.portabilityPreview?.let { preview ->
            PortabilityBundleSheet(
                preview = preview,
                onDismiss = viewModel::onDismissPortabilityPreview,
                onSelectSummary = {
                    viewModel.onSelectPortabilityMode(
                        memoryId = preview.memoryId,
                        preferFullExport = false,
                    )
                },
                onSelectFull = {
                    viewModel.onSelectPortabilityMode(
                        memoryId = preview.memoryId,
                        preferFullExport = true,
                    )
                },
                onShare = viewModel::onSharePortabilityPreview,
            )
        }
        if (showRuntimeControlCenter) {
            RuntimeControlCenterSheet(
                controlCenter = uiState.runtimeControlCenter,
                onDismiss = { showRuntimeControlCenter = false },
                onOpenArtifact = { artifactId ->
                    when (artifactId) {
                        "model" -> {
                            showRuntimeControlCenter = false
                            showModelPicker = true
                        }
                        "memory" -> {
                            showRuntimeControlCenter = false
                            showContextInspector = true
                        }
                        "governance" -> {
                            showRuntimeControlCenter = false
                            showGovernanceCenter = true
                        }
                        "approval" -> showRuntimeControlCenter = false
                        "system_permissions" -> {
                            showRuntimeControlCenter = false
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_CONTACTS,
                                    android.Manifest.permission.READ_CALENDAR,
                                ),
                            )
                        }
                    }
                },
            )
        }
        if (showGovernanceCenter) {
            GovernanceCenterSheet(
                governance = uiState.governanceCenter,
                onUpdateTrust = viewModel::onUpdateGovernanceTrust,
                onUpdateScope = viewModel::onUpdateGovernanceScope,
                onDismiss = { showGovernanceCenter = false },
            )
        }
        uiState.pendingApproval?.let { approval ->
            ApprovalSheet(
                approval = approval,
                onApprove = viewModel::onApprovePendingApproval,
                onReject = viewModel::onRejectPendingApproval,
                onDismiss = viewModel::onDismissPendingApproval,
            )
        }
    }
}

@Composable
private fun WorkspacePlaceholderCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    bodyContent: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (body.isNotBlank()) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            bodyContent?.invoke()
        }
    }
}
