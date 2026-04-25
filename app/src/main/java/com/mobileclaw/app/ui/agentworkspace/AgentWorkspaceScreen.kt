package com.mobileclaw.app.ui.agentworkspace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.ui.agentworkspace.components.ApprovalSheet
import com.mobileclaw.app.ui.agentworkspace.components.AutomationCenterSheet
import com.mobileclaw.app.ui.agentworkspace.components.ComposerDock
import com.mobileclaw.app.ui.agentworkspace.components.ContextInspectorSheet
import com.mobileclaw.app.ui.agentworkspace.components.ConversationLayer
import com.mobileclaw.app.ui.agentworkspace.components.GovernanceCenterSheet
import com.mobileclaw.app.ui.agentworkspace.components.InlineFailureBanner
import com.mobileclaw.app.ui.agentworkspace.components.KnowledgeCenterSheet
import com.mobileclaw.app.ui.agentworkspace.components.ModelPickerSheet
import com.mobileclaw.app.ui.agentworkspace.components.PortabilityBundleSheet
import com.mobileclaw.app.ui.agentworkspace.components.ResetSessionDialog
import com.mobileclaw.app.ui.agentworkspace.components.RuntimeControlCenterSheet
import com.mobileclaw.app.ui.agentworkspace.components.WorkflowRunBanner
import com.mobileclaw.app.ui.agentworkspace.components.WorkspaceConversationStarter
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
    var showKnowledgeCenter by remember { mutableStateOf(false) }
    var showAutomationCenter by remember { mutableStateOf(false) }
    var showGovernanceCenter by remember { mutableStateOf(false) }
    var showRuntimeControlCenter by remember { mutableStateOf(false) }
    var selectedKnowledgeAssetId by remember { mutableStateOf<String?>(null) }
    var selectedWorkflowDefinitionId by remember { mutableStateOf<String?>(null) }
    var selectedWorkflowRunId by remember { mutableStateOf<String?>(null) }
    var topPanelsExpanded by remember { mutableStateOf(true) }
    var topChromeHeightPx by remember { mutableStateOf(0) }
    var bottomChromeHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val workflowPauseLabel = stringResource(R.string.workflow_action_pause)
    val workflowResumeLabel = stringResource(R.string.workflow_action_resume)
    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onImportModel(uri)
        }
    }
    val imageAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            viewModel.onAddImageAttachment(uri)
        }
    }
    val audioAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            viewModel.onAddAudioAttachment(uri)
        }
    }
    val knowledgeFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onIngestKnowledgeDocuments(listOf(uri))
        }
    }
    val knowledgeCollectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onIngestKnowledgeDocuments(uris)
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
            topPanelsExpanded = false
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
        val headerModelLabel = uiState.activeModel?.displayName
            ?: uiState.modelHealth.displayName.ifBlank {
                stringResource(R.string.model_no_model_selected)
            }
        val headerModelStatus = uiState.modelHealth.headline.ifBlank {
            uiState.runtimeStatus.stageLabel
        }.ifBlank {
            uiState.runtimeStatus.supportingText
        }
        val topChromeHeight = with(density) { topChromeHeightPx.toDp() }
        val bottomChromeHeight = with(density) { bottomChromeHeightPx.toDp() }
        val topContentPadding = 12.dp + if (topChromeHeight > 0.dp) topChromeHeight else 128.dp
        val bottomContentPadding = 12.dp + if (bottomChromeHeight > 0.dp) bottomChromeHeight else 132.dp
        val showModelRecoveryState = uiState.screenState == WorkspaceScreenState.RECOVERABLE_FAILURE &&
            uiState.turns.isEmpty()
        val showConversationStarter = uiState.turns.isEmpty() &&
            uiState.isModelReady &&
            uiState.isComposerEnabled

        WorkspacePlaceholderCard(
            title = "",
            body = "",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topContentPadding, bottom = bottomContentPadding),
            bodyContent = {
                if (uiState.screenState == WorkspaceScreenState.PREPARING ||
                    uiState.screenState == WorkspaceScreenState.UNAVAILABLE ||
                    showModelRecoveryState
                ) {
                    WorkspaceEmptyState(
                        title = when {
                            uiState.screenState == WorkspaceScreenState.PREPARING -> {
                                stringResource(R.string.workspace_preparing_local_runtime)
                            }
                            showModelRecoveryState -> {
                                stringResource(R.string.workspace_recovery_needed)
                            }
                            else -> {
                                stringResource(R.string.workspace_no_ready_local_model)
                            }
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
                        emptyContent = if (showConversationStarter) {
                            {
                                WorkspaceConversationStarter(
                                    statusLine = uiState.statusDigest.headline.ifBlank {
                                        uiState.modelHealth.headline
                                    },
                                    onQuickPromptSelected = viewModel::onQuickPromptSelected,
                                )
                            }
                        } else {
                            null
                        },
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
                modelLabel = headerModelLabel,
                modelStatus = headerModelStatus,
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
                                    android.Manifest.permission.WRITE_CALENDAR,
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
                                "knowledge" -> showKnowledgeCenter = true
                                "automation" -> showAutomationCenter = true
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
            uiState.automationArea.activeRunBanner?.let { banner ->
                WorkflowRunBanner(
                    banner = banner,
                    onPrimaryAction = {
                        when (banner.primaryActionLabel) {
                            workflowPauseLabel -> {
                                viewModel.onPauseWorkflowRun(banner.workflowRunId)
                            }
                            workflowResumeLabel -> {
                                viewModel.onResumeWorkflowRun(banner.workflowRunId)
                            }
                            else -> {
                                showAutomationCenter = true
                                selectedWorkflowRunId = banner.workflowRunId
                            }
                        }
                    },
                    onSecondaryAction = {
                        showAutomationCenter = true
                        selectedWorkflowRunId = banner.workflowRunId
                    },
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
                onAddImageClicked = { imageAttachmentLauncher.launch("image/*") },
                onAddAudioClicked = { audioAttachmentLauncher.launch("audio/*") },
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
                        "knowledge" -> {
                            showRuntimeControlCenter = false
                            showKnowledgeCenter = true
                        }
                        "automation" -> {
                            showRuntimeControlCenter = false
                            showAutomationCenter = true
                        }
                        "governance" -> {
                            showRuntimeControlCenter = false
                            showGovernanceCenter = true
                        }
                        "interop_host" -> {
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
                                    android.Manifest.permission.WRITE_CALENDAR,
                                ),
                            )
                        }
                        else -> {
                            if (artifactId.startsWith("contribution:")) {
                                viewModel.onToggleContributionAvailability(
                                    artifactId.removePrefix("contribution:"),
                                )
                            }
                        }
                    }
                },
            )
        }
        if (showAutomationCenter) {
            AutomationCenterSheet(
                automationArea = uiState.automationArea,
                selectedDefinition = uiState.automationArea.definitions.firstOrNull {
                    it.workflowDefinitionId == selectedWorkflowDefinitionId
                },
                selectedRun = uiState.automationArea.runs.firstOrNull {
                    it.workflowRunId == selectedWorkflowRunId
                },
                onDismiss = {
                    showAutomationCenter = false
                    selectedWorkflowDefinitionId = null
                    selectedWorkflowRunId = null
                },
                onCreateTemplate = viewModel::onCreateWorkflowFromTemplate,
                onOpenDefinition = { workflowDefinitionId ->
                    selectedWorkflowDefinitionId = workflowDefinitionId
                    selectedWorkflowRunId = null
                },
                onOpenRun = { workflowRunId ->
                    selectedWorkflowRunId = workflowRunId
                    selectedWorkflowDefinitionId = null
                },
                onCloseDetail = {
                    selectedWorkflowDefinitionId = null
                    selectedWorkflowRunId = null
                },
                onStartWorkflow = viewModel::onStartWorkflow,
                onToggleWorkflowEnabled = viewModel::onToggleWorkflowEnabled,
                onResumeRun = viewModel::onResumeWorkflowRun,
                onPauseRun = viewModel::onPauseWorkflowRun,
                onCancelRun = viewModel::onCancelWorkflowRun,
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
        if (showKnowledgeCenter) {
            KnowledgeCenterSheet(
                knowledgeArea = uiState.knowledgeArea,
                selectedEntry = uiState.knowledgeArea.entries.firstOrNull {
                    it.knowledgeAssetId == selectedKnowledgeAssetId
                },
                onDismiss = {
                    showKnowledgeCenter = false
                    selectedKnowledgeAssetId = null
                },
                onAddFile = {
                    knowledgeFileLauncher.launch(
                        arrayOf("text/*", "application/json", "application/xml"),
                    )
                },
                onAddCollection = {
                    knowledgeCollectionLauncher.launch(
                        arrayOf("text/*", "application/json", "application/xml"),
                    )
                },
                onOpenAsset = { knowledgeAssetId ->
                    selectedKnowledgeAssetId = knowledgeAssetId
                },
                onCloseAsset = { selectedKnowledgeAssetId = null },
                onRefreshAsset = viewModel::onRefreshKnowledgeAsset,
                onToggleRetrievalInclusion = viewModel::onToggleKnowledgeRetrievalInclusion,
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
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
