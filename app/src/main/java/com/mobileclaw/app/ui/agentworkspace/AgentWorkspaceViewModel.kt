package com.mobileclaw.app.ui.agentworkspace

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.capability.CallerTrustState
import com.mobileclaw.app.runtime.extension.RuntimeExtensionRegistry
import com.mobileclaw.app.runtime.governance.GovernanceGrantState
import com.mobileclaw.app.runtime.governance.GovernanceRepository
import com.mobileclaw.app.runtime.governance.GovernanceTrustMode
import com.mobileclaw.app.runtime.governance.governanceEditableScopes
import com.mobileclaw.app.runtime.ingress.ExternalHandoffCoordinator
import com.mobileclaw.app.runtime.ingress.ExternalHandoffEvent
import com.mobileclaw.app.runtime.ingress.ExternalRuntimeRequestMapper
import com.mobileclaw.app.runtime.localchat.LocalModelCatalog
import com.mobileclaw.app.runtime.localchat.LocalModelProfile
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus
import com.mobileclaw.app.runtime.localchat.ModelHealthSnapshot
import com.mobileclaw.app.runtime.localchat.ModelImportResult
import com.mobileclaw.app.runtime.multimodal.AttachmentStore
import com.mobileclaw.app.runtime.multimodal.PendingAttachment
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentKind
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentSourceType
import com.mobileclaw.app.runtime.memory.ExportDecisionService
import com.mobileclaw.app.runtime.memory.MemoryLifecycle
import com.mobileclaw.app.runtime.memory.MemoryRetrievalService
import com.mobileclaw.app.runtime.memory.MemoryExposurePolicy
import com.mobileclaw.app.runtime.memory.PortabilityBundleFormatter
import com.mobileclaw.app.runtime.memory.PortabilityBundlePreview
import com.mobileclaw.app.runtime.memory.PortabilityBundleShareService
import com.mobileclaw.app.runtime.memory.RetrievalQuery
import com.mobileclaw.app.runtime.memory.ScopedMemoryRepository
import com.mobileclaw.app.runtime.memory.toMergeCandidate
import com.mobileclaw.app.runtime.memory.toActiveContextSummary
import com.mobileclaw.app.runtime.persona.PersonaRepository
import com.mobileclaw.app.runtime.persona.PersonaVerbosity
import com.mobileclaw.app.runtime.policy.ApprovalOutcomeType
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.session.RuntimeSourceMetadata
import com.mobileclaw.app.runtime.session.RuntimeSourceTrustState
import com.mobileclaw.app.runtime.session.RuntimeSessionEvent
import com.mobileclaw.app.runtime.session.RuntimeSessionFacade
import com.mobileclaw.app.runtime.session.RuntimeTranscriptEntry
import com.mobileclaw.app.runtime.session.RuntimeTranscriptRole
import com.mobileclaw.app.runtime.systemsource.SystemSourceRepository
import com.mobileclaw.app.ui.agentworkspace.model.toUiModel
import com.mobileclaw.app.ui.agentworkspace.model.AttachmentUiModel
import com.mobileclaw.app.ui.agentworkspace.model.AuditUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ChatRoleUi
import com.mobileclaw.app.ui.agentworkspace.model.ChatTurnStateUi
import com.mobileclaw.app.ui.agentworkspace.model.ChatTurnUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ContextInspectorUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ContextMemoryUiModel
import com.mobileclaw.app.ui.agentworkspace.model.GovernanceActivityUiModel
import com.mobileclaw.app.ui.agentworkspace.model.GovernanceCallerUiModel
import com.mobileclaw.app.ui.agentworkspace.model.GovernanceCenterUiModel
import com.mobileclaw.app.ui.agentworkspace.model.GovernanceOptionUiModel
import com.mobileclaw.app.ui.agentworkspace.model.GovernanceScopeGrantUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ModelHealthUiModel
import com.mobileclaw.app.ui.agentworkspace.model.PortabilityBundlePreviewUiModel
import com.mobileclaw.app.ui.agentworkspace.model.PortabilityCompatibilityUiModel
import com.mobileclaw.app.ui.agentworkspace.model.ManagedArtifactEntryUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeControlCenterUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeStatusUiModel
import com.mobileclaw.app.ui.agentworkspace.model.RuntimeTraceSectionUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceAttentionMode
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceFeedbackKind
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceFeedbackUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceSecondaryEntryUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkspaceStatusDigestUiModel
import com.mobileclaw.app.ui.agentworkspace.model.toRuntimeAttachment
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AgentWorkspaceViewModel @Inject constructor(
    private val localModelCatalog: LocalModelCatalog,
    private val runtimeSessionFacade: RuntimeSessionFacade,
    private val externalHandoffCoordinator: ExternalHandoffCoordinator,
    private val externalRuntimeRequestMapper: ExternalRuntimeRequestMapper,
    private val attachmentStore: AttachmentStore,
    private val personaRepository: PersonaRepository,
    private val scopedMemoryRepository: ScopedMemoryRepository,
    private val memoryRetrievalService: MemoryRetrievalService,
    private val exportDecisionService: ExportDecisionService,
    private val portabilityBundleFormatter: PortabilityBundleFormatter,
    private val portabilityBundleShareService: PortabilityBundleShareService,
    private val governanceRepository: GovernanceRepository,
    private val systemSourceRepository: SystemSourceRepository,
    private val runtimeExtensionRegistry: RuntimeExtensionRegistry,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentWorkspaceUiState())
    val uiState: StateFlow<AgentWorkspaceUiState> = _uiState
        .map(::decorateWorkspaceUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = decorateWorkspaceUiState(AgentWorkspaceUiState()),
        )
    private var streamJob: Job? = null
    private var modelHealthJob: Job? = null
    private var contextInspectorJob: Job? = null
    private var externalHandoffJob: Job? = null
    private var governanceJob: Job? = null
    private var systemSourceJob: Job? = null

    init {
        observeModels()
        observeContextInspector()
        observeExternalHandoffs()
        observeGovernanceCenter()
        observeSystemSources()
        refreshExtensionDiscovery()
    }

    fun onDraftChanged(value: String) {
        _uiState.value = _uiState.value.copy(composerDraft = value)
        viewModelScope.launch {
            refreshContextInspector()
        }
    }

    fun onSelectModel(modelId: String) {
        viewModelScope.launch {
            localModelCatalog.selectModel(modelId)?.let { selected ->
                bindSelectedModel(selected)
            }
        }
    }

    fun onImportModel(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                feedback = WorkspaceFeedbackUiModel(
                    messageId = "import-start-${System.currentTimeMillis()}",
                    kind = WorkspaceFeedbackKind.INFO,
                    text = appStrings.get(R.string.workspace_importing_model_file),
                    scope = "model_import",
                ),
            )
            when (val result = localModelCatalog.importModel(sourceUri)) {
                is ModelImportResult.Success -> {
                    bindSelectedModel(result.model)
                    _uiState.value = _uiState.value.copy(
                        feedback = WorkspaceFeedbackUiModel(
                            messageId = "import-success-${System.currentTimeMillis()}",
                            kind = WorkspaceFeedbackKind.SUCCESS,
                            text = appStrings.get(R.string.workspace_model_imported, result.model.displayName),
                            scope = "model_import",
                        ),
                    )
                }

                is ModelImportResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        feedback = WorkspaceFeedbackUiModel(
                            messageId = "import-failed-${System.currentTimeMillis()}",
                            kind = WorkspaceFeedbackKind.ERROR,
                            text = result.message,
                            scope = "model_import",
                        ),
                    )
                }
            }
        }
    }

    fun onUpdateModelCapabilities(
        modelId: String,
        supportsImage: Boolean? = null,
        supportsAudio: Boolean? = null,
    ) {
        val current = _uiState.value.availableModels.firstOrNull { it.modelId == modelId } ?: return
        if (!current.supportsManualCapabilityOverride) return
        viewModelScope.launch {
            val updated = localModelCatalog.updateModelCapabilities(
                modelId = modelId,
                supportsImage = supportsImage ?: current.modalityCapabilities.supportsImage,
                supportsAudio = supportsAudio ?: current.modalityCapabilities.supportsAudio,
            ) ?: return@launch
            if (_uiState.value.activeModel?.modelId == modelId) {
                bindSelectedModel(updated)
            }
            _uiState.value = _uiState.value.copy(
                feedback = WorkspaceFeedbackUiModel(
                    messageId = "model-capability-updated-${System.currentTimeMillis()}",
                    kind = WorkspaceFeedbackKind.SUCCESS,
                    text = appStrings.get(R.string.model_capabilities_updated, updated.displayName),
                    scope = "model_capability_override",
                ),
            )
        }
    }

    fun onAddImageAttachment(sourceUri: Uri) {
        importAttachment(sourceUri, RuntimeAttachmentKind.IMAGE)
    }

    fun onAddAudioAttachment(sourceUri: Uri) {
        importAttachment(sourceUri, RuntimeAttachmentKind.AUDIO)
    }

    fun onRemovePendingAttachment(attachmentId: String) {
        _uiState.value = _uiState.value.copy(
            pendingAttachments = _uiState.value.pendingAttachments.filterNot { it.attachmentId == attachmentId },
        )
    }

    fun dismissFeedback() {
        _uiState.value = _uiState.value.copy(feedback = null)
    }

    fun onQuickPromptSelected(prompt: String) {
        _uiState.value = _uiState.value.copy(composerDraft = prompt)
        viewModelScope.launch {
            refreshContextInspector()
        }
    }

    fun onCyclePersonaVerbosity() {
        viewModelScope.launch {
            personaRepository.updatePersona { current ->
                current.copy(
                    verbosity = when (current.verbosity) {
                        PersonaVerbosity.LOW -> PersonaVerbosity.MEDIUM
                        PersonaVerbosity.MEDIUM -> PersonaVerbosity.HIGH
                        PersonaVerbosity.HIGH -> PersonaVerbosity.LOW
                    },
                )
            }
            refreshContextInspector()
        }
    }

    fun onToggleMemoryPin(memoryId: String) {
        viewModelScope.launch {
            scopedMemoryRepository.update(memoryId) { current ->
                current.copy(isPinned = !current.isPinned)
            }
            refreshContextInspector()
        }
    }

    fun onPromoteMemory(memoryId: String) {
        viewModelScope.launch {
            scopedMemoryRepository.update(memoryId) { current ->
                current.copy(
                    lifecycle = MemoryLifecycle.DURABLE,
                    isPinned = true,
                )
            }
            refreshContextInspector()
        }
    }

    fun onDemoteMemory(memoryId: String) {
        viewModelScope.launch {
            scopedMemoryRepository.update(memoryId) { current ->
                current.copy(
                    lifecycle = when (current.lifecycle) {
                        MemoryLifecycle.DURABLE -> MemoryLifecycle.WORKING
                        MemoryLifecycle.WORKING -> MemoryLifecycle.EPHEMERAL
                        MemoryLifecycle.EPHEMERAL -> MemoryLifecycle.EPHEMERAL
                    },
                    isPinned = if (current.lifecycle == MemoryLifecycle.DURABLE) false else current.isPinned,
                )
            }
            refreshContextInspector()
        }
    }

    fun onExpireMemory(memoryId: String) {
        viewModelScope.launch {
            scopedMemoryRepository.update(memoryId) { current ->
                current.copy(
                    expiresAtEpochMillis = System.currentTimeMillis() - 1,
                    isPinned = false,
                )
            }
            refreshContextInspector()
        }
    }

    fun onOpenPortabilityPreview(
        memoryId: String,
        preferFullExport: Boolean = false,
    ) {
        viewModelScope.launch {
            val item = scopedMemoryRepository.get(memoryId) ?: return@launch
            val policy = exportDecisionService.evaluateRedactionPolicy(item)
            if (!policy.allowSummaryExport && !policy.allowFullExport) {
                _uiState.value = _uiState.value.copy(
                    feedback = WorkspaceFeedbackUiModel(
                        messageId = "portability-blocked-${System.currentTimeMillis()}",
                        kind = WorkspaceFeedbackKind.INFO,
                        text = policy.reason,
                        scope = "portability_export",
                    ),
                )
                return@launch
            }
            val preview = buildPortabilityPreview(item, preferFullExport)
            _uiState.value = _uiState.value.copy(portabilityPreview = preview)
        }
    }

    fun onDismissPortabilityPreview() {
        _uiState.value = _uiState.value.copy(portabilityPreview = null)
    }

    fun onSelectPortabilityMode(
        memoryId: String,
        preferFullExport: Boolean,
    ) {
        viewModelScope.launch {
            val item = scopedMemoryRepository.get(memoryId) ?: return@launch
            val preview = buildPortabilityPreview(item, preferFullExport)
            _uiState.value = _uiState.value.copy(portabilityPreview = preview)
        }
    }

    fun onSharePortabilityPreview() {
        viewModelScope.launch {
            val previewUi = _uiState.value.portabilityPreview ?: return@launch
            val item = scopedMemoryRepository.get(previewUi.memoryId) ?: return@launch
            val preview = buildPortabilityPreviewDomain(item, previewUi.isFullModeSelected)
            portabilityBundleShareService.shareBundle(
                preview = preview,
                chooserTitle = appStrings.get(R.string.portability_share_chooser_title),
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    feedback = WorkspaceFeedbackUiModel(
                        messageId = "portability-share-${System.currentTimeMillis()}",
                        kind = WorkspaceFeedbackKind.SUCCESS,
                        text = appStrings.get(R.string.portability_share_started),
                        scope = "portability_export",
                    ),
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    feedback = WorkspaceFeedbackUiModel(
                        messageId = "portability-share-failed-${System.currentTimeMillis()}",
                        kind = WorkspaceFeedbackKind.ERROR,
                        text = error.message ?: appStrings.get(R.string.portability_share_failed),
                        scope = "portability_export",
                    ),
                )
            }
        }
    }

    fun onUpdateGovernanceTrust(
        callerId: String,
        trustModeKey: String,
    ) {
        viewModelScope.launch {
            runCatching {
                governanceRepository.updateTrustMode(
                    callerId = callerId,
                    trustMode = GovernanceTrustMode.valueOf(trustModeKey),
                )
            }
        }
    }

    fun onUpdateGovernanceScope(
        callerId: String,
        scopeId: String,
        stateKey: String,
    ) {
        viewModelScope.launch {
            runCatching {
                governanceRepository.updateScopeGrant(
                    callerId = callerId,
                    scopeId = scopeId,
                    grantState = GovernanceGrantState.valueOf(stateKey),
                )
            }
        }
    }

    fun onSystemPermissionsChanged() {
        systemSourceRepository.refresh()
    }

    fun onSendClicked() {
        val state = _uiState.value
        val model = state.activeModel ?: return
        val prompt = state.composerDraft.trim()
        if ((prompt.isBlank() && state.pendingAttachments.isEmpty()) || state.isBusy || !state.isModelReady) {
            return
        }
        val originApp = when {
            "[untrusted]" in prompt.lowercase() -> "external.untrusted"
            else -> "agent_workspace"
        }
        val attachments = state.pendingAttachments.map { attachment ->
            attachment.toRuntimeAttachment(RuntimeAttachmentSourceType.COMPOSER_IMPORT)
        }
        submitRuntimeRequest(
            request = RuntimeRequest(
                requestId = "req-${System.currentTimeMillis()}",
                userInput = prompt,
                selectedModelId = model.modelId,
                transcriptContext = emptyList(),
                originApp = originApp,
                workspaceId = state.workspaceId,
                subjectKey = state.workspaceId,
                attachments = attachments,
            ),
            visibleInput = prompt.ifBlank { buildAttachmentVisibleInput(state.pendingAttachments) },
            resetVisibleSession = false,
            activeAttachments = state.pendingAttachments,
        )
    }

    private fun observeExternalHandoffs() {
        externalHandoffJob?.cancel()
        externalHandoffJob = viewModelScope.launch {
            externalHandoffCoordinator.pendingEvent.collectLatest { event ->
                when (event) {
                    null -> Unit
                    is ExternalHandoffEvent.Pending -> {
                        externalHandoffCoordinator.consume(event.eventId)
                        handleExternalPending(event)
                    }
                    is ExternalHandoffEvent.Rejected -> {
                        externalHandoffCoordinator.consume(event.eventId)
                        handleExternalRejected(event)
                    }
                }
            }
        }
    }

    private fun observeGovernanceCenter() {
        governanceJob?.cancel()
        governanceJob = viewModelScope.launch {
            governanceRepository.observeGovernanceCenter().collectLatest { snapshot ->
                _uiState.value = _uiState.value.copy(
                    governanceCenter = GovernanceCenterUiModel(
                        callers = snapshot.callers.map { caller ->
                            GovernanceCallerUiModel(
                                callerId = caller.record.callerId,
                                displayLabel = caller.record.displayLabel,
                                trustModeLabel = appStrings.governanceTrustModeLabel(caller.record.trustMode),
                                lastSeenLabel = appStrings.get(
                                    R.string.governance_last_seen,
                                    formatTimestamp(caller.record.lastSeenAtEpochMillis),
                                ),
                                lastDecisionLabel = appStrings.get(
                                    R.string.governance_last_decision,
                                    caller.record.lastDecisionSummary,
                                ),
                                trustModeKey = caller.record.trustMode.name,
                                availableTrustModes = GovernanceTrustMode.entries.map { mode ->
                                    GovernanceOptionUiModel(
                                        key = mode.name,
                                        label = appStrings.governanceTrustModeLabel(mode),
                                    )
                                },
                                scopeGrants = governanceEditableScopes().map { scope ->
                                    val current = caller.scopeGrants.firstOrNull { it.scopeId == scope.scopeId }?.grantState
                                        ?: GovernanceGrantState.ASK
                                    GovernanceScopeGrantUiModel(
                                        scopeId = scope.scopeId,
                                        scopeLabel = appStrings.actionScopeLabel(scope),
                                        stateLabel = appStrings.governanceGrantStateLabel(current),
                                        stateKey = current.name,
                                        availableStates = GovernanceGrantState.entries.map { state ->
                                            GovernanceOptionUiModel(
                                                key = state.name,
                                                label = appStrings.governanceGrantStateLabel(state),
                                            )
                                        },
                                    )
                                },
                            )
                        },
                        activities = snapshot.activities.map { activity ->
                            GovernanceActivityUiModel(
                                activityId = activity.activityId,
                                headline = activity.headline,
                                details = activity.details,
                                timestampLabel = formatTimestamp(activity.timestamp),
                            )
                        },
                    ),
                )
            }
        }
    }

    private fun observeSystemSources() {
        systemSourceJob?.cancel()
        systemSourceJob = viewModelScope.launch {
            systemSourceRepository.observeDescriptors().collectLatest { descriptors ->
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        systemSourceStatusLines = descriptors.map { it.availabilitySummary },
                        hasMissingSystemSourcePermissions = descriptors.any { !it.isGranted },
                    ),
                )
            }
        }
    }

    private fun handleExternalPending(event: ExternalHandoffEvent.Pending) {
        val state = _uiState.value
        val model = state.activeModel
        val envelope = event.envelope
        val callerIdentity = envelope.callerIdentity
        val interopLabel = appStrings.interopContractLabel(
            entryType = envelope.entryType,
            version = envelope.compatibilitySignal.interopVersion,
        )
        if (model == null || !state.isModelReady) {
            _uiState.value = state.copy(
                composerDraft = envelope.sharedText,
                pendingAttachments = envelope.attachments.map(::toAttachmentUiModel),
                runtimeStatus = state.runtimeStatus.copy(
                    headline = appStrings.get(R.string.workspace_external_handoff_denied),
                    stageLabel = appStrings.get(R.string.runtime_stage_ingress),
                    supportingText = appStrings.get(
                        R.string.workspace_external_handoff_waiting_model,
                        callerIdentity.sourceLabel,
                    ),
                    sourceLabel = callerIdentity.sourceLabel,
                    trustStateLabel = appStrings.externalTrustStateLabel(callerIdentity.trustState),
                    interopContractLabel = interopLabel,
                    uriGrantLabel = envelope.uriGrantSummary.summaryText,
                    callerTrust = callerIdentity.trustReason,
                    isBusy = false,
                    awaitingInput = false,
                    isTerminal = false,
                ),
                feedback = WorkspaceFeedbackUiModel(
                    messageId = "external-waiting-${System.currentTimeMillis()}",
                    kind = WorkspaceFeedbackKind.INFO,
                    text = appStrings.get(
                        R.string.workspace_external_handoff_waiting_model,
                        callerIdentity.sourceLabel,
                    ),
                    scope = "external_handoff",
                ),
            )
            return
        }

        if (!supportsAllAttachments(model, envelope.attachments)) {
            _uiState.value = state.copy(
                composerDraft = envelope.sharedText,
                pendingAttachments = envelope.attachments.map(::toAttachmentUiModel),
                runtimeStatus = state.runtimeStatus.copy(
                    headline = appStrings.get(R.string.multimodal_model_capability_missing),
                    stageLabel = appStrings.get(R.string.runtime_stage_denied),
                    supportingText = appStrings.get(R.string.multimodal_external_model_not_supported),
                    sourceLabel = callerIdentity.sourceLabel,
                    trustStateLabel = appStrings.externalTrustStateLabel(callerIdentity.trustState),
                    interopContractLabel = interopLabel,
                    uriGrantLabel = envelope.uriGrantSummary.summaryText,
                    callerTrust = callerIdentity.trustReason,
                    isBusy = false,
                    awaitingInput = false,
                    isTerminal = true,
                ),
                feedback = WorkspaceFeedbackUiModel(
                    messageId = "external-multimodal-unsupported-${System.currentTimeMillis()}",
                    kind = WorkspaceFeedbackKind.INFO,
                    text = appStrings.get(R.string.multimodal_external_model_not_supported),
                    scope = "external_handoff",
                ),
            )
            return
        }

        val request = externalRuntimeRequestMapper.map(
            envelope = envelope,
            selectedModelId = model.modelId,
            workspaceId = state.workspaceId,
            transcriptContext = emptyList(),
        )
        submitRuntimeRequest(
            request = request,
            visibleInput = envelope.sharedText.ifBlank {
                buildPendingAttachmentVisibleInput(envelope.attachments)
            },
            resetVisibleSession = true,
            activeAttachments = envelope.attachments.map(::toAttachmentUiModel),
            sourceMetadata = request.sourceMetadata,
            feedbackText = appStrings.get(
                R.string.workspace_external_handoff_received,
                callerIdentity.sourceLabel,
            ),
        )
    }

    private fun handleExternalRejected(event: ExternalHandoffEvent.Rejected) {
        val state = _uiState.value
        _uiState.value = state.copy(
            runtimeStatus = state.runtimeStatus.copy(
                headline = appStrings.get(R.string.workspace_external_handoff_denied),
                stageLabel = appStrings.get(R.string.runtime_stage_denied),
                supportingText = event.reason,
                sourceLabel = event.sourceLabel,
                trustStateLabel = appStrings.externalTrustStateLabel(event.trustState),
                callerTrust = event.trustReason,
                isBusy = false,
                awaitingInput = false,
                isTerminal = true,
            ),
            recentAudit = listOf(
                AuditUiModel(
                    auditEventId = "external-rejected-${System.currentTimeMillis()}",
                    headline = appStrings.get(R.string.external_handoff_audit_rejected),
                    details = event.reason,
                ),
            ) + state.recentAudit.take(5),
            feedback = WorkspaceFeedbackUiModel(
                messageId = "external-rejected-${System.currentTimeMillis()}",
                kind = WorkspaceFeedbackKind.ERROR,
                text = event.reason,
                scope = "external_handoff",
            ),
        )
    }

    private fun submitRuntimeRequest(
        request: RuntimeRequest,
        visibleInput: String,
        resetVisibleSession: Boolean,
        activeAttachments: List<AttachmentUiModel> = emptyList(),
        sourceMetadata: RuntimeSourceMetadata? = request.sourceMetadata,
        feedbackText: String? = null,
    ) {
        val state = _uiState.value
        val baseTurns = if (resetVisibleSession) emptyList() else state.turns
        val userTurn = ChatTurnUiModel(
            turnId = "user-${System.currentTimeMillis()}",
            role = ChatRoleUi.USER,
            content = visibleInput,
            state = ChatTurnStateUi.COMPLETE,
        )
        val assistantTurn = ChatTurnUiModel(
            turnId = "assistant-pending-${System.currentTimeMillis()}",
            role = ChatRoleUi.ASSISTANT,
            content = "",
            state = ChatTurnStateUi.STREAMING,
        )
        val transcript = (baseTurns + userTurn).map { turn ->
            RuntimeTranscriptEntry(
                role = if (turn.role == ChatRoleUi.USER) RuntimeTranscriptRole.USER else RuntimeTranscriptRole.ASSISTANT,
                content = turn.content,
            )
        }
        val normalizedRequest = request.copy(transcriptContext = transcript)
        _uiState.value = state.copy(
            turns = baseTurns + userTurn + assistantTurn,
            activeAssistantTurnId = assistantTurn.turnId,
            composerDraft = "",
            pendingAttachments = emptyList(),
            activeAttachments = activeAttachments,
            isBusy = true,
            isComposerEnabled = false,
            screenState = WorkspaceScreenState.STREAMING,
            runtimeStatus = state.runtimeStatus.copy(
                headline = appStrings.get(R.string.workspace_session_started),
                stageLabel = appStrings.get(R.string.workspace_session_started),
                supportingText = appStrings.get(R.string.workspace_routing_request),
                toolId = "",
                toolDisplayName = "",
                toolSideEffectLabel = "",
                toolScopeLines = emptyList(),
                toolVisibilityLabel = "",
                toolVisibilityReason = "",
                sourceLabel = sourceMetadata?.sourceLabel.orEmpty(),
                trustStateLabel = sourceMetadata?.let(::trustStateLabel).orEmpty(),
                interopContractLabel = sourceMetadata?.let(::interopContractLabel).orEmpty(),
                uriGrantLabel = sourceMetadata?.grantSummary.orEmpty(),
                routeSummary = "",
                callerTrust = sourceMetadata?.trustReason.orEmpty(),
                extensionStatusLines = state.runtimeStatus.extensionStatusLines,
                isBusy = true,
                awaitingInput = false,
                isTerminal = false,
            ),
            pendingApproval = null,
            recentAudit = if (resetVisibleSession) emptyList() else state.recentAudit,
            feedback = feedbackText?.let {
                WorkspaceFeedbackUiModel(
                    messageId = "runtime-submit-${System.currentTimeMillis()}",
                    kind = WorkspaceFeedbackKind.INFO,
                    text = it,
                    scope = "runtime_submission",
                )
            } ?: state.feedback,
        )

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            runtimeSessionFacade.submitRequest(normalizedRequest).collect { event ->
                handleRuntimeEvent(event)
            }
            refreshContextInspector()
        }
    }

    private fun formatTimestamp(epochMillis: Long): String {
        return DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
        ).format(Date(epochMillis))
    }

    private fun handleRuntimeEvent(event: RuntimeSessionEvent) {
        when (event) {
            is RuntimeSessionEvent.ExternalHandoffReceived -> {
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        sourceLabel = event.sourceMetadata.sourceLabel,
                        trustStateLabel = trustStateLabel(event.sourceMetadata),
                        interopContractLabel = interopContractLabel(event.sourceMetadata),
                        uriGrantLabel = event.sourceMetadata.grantSummary,
                        callerTrust = event.sourceMetadata.trustReason,
                    ),
                )
            }

            is RuntimeSessionEvent.ExternalInvocationLinked -> Unit

            is RuntimeSessionEvent.SessionStarted -> {
                _uiState.value = _uiState.value.copy(
                    activeSessionId = event.session.sessionId,
                    runtimeStatus = event.session.summary.toUiModel().copy(
                        toolId = _uiState.value.runtimeStatus.toolId,
                        toolDisplayName = _uiState.value.runtimeStatus.toolDisplayName,
                        toolSideEffectLabel = _uiState.value.runtimeStatus.toolSideEffectLabel,
                        toolScopeLines = _uiState.value.runtimeStatus.toolScopeLines,
                        toolVisibilityLabel = _uiState.value.runtimeStatus.toolVisibilityLabel,
                        toolVisibilityReason = _uiState.value.runtimeStatus.toolVisibilityReason,
                        sourceLabel = _uiState.value.runtimeStatus.sourceLabel,
                        trustStateLabel = _uiState.value.runtimeStatus.trustStateLabel,
                        interopContractLabel = _uiState.value.runtimeStatus.interopContractLabel,
                        uriGrantLabel = _uiState.value.runtimeStatus.uriGrantLabel,
                        routeSummary = _uiState.value.runtimeStatus.routeSummary,
                        callerTrust = _uiState.value.runtimeStatus.callerTrust,
                        extensionStatusLines = _uiState.value.runtimeStatus.extensionStatusLines,
                    ),
                )
            }

            is RuntimeSessionEvent.StageChanged -> Unit

            is RuntimeSessionEvent.SystemSourcesPrepared -> {
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        systemSourceStatusLines = event.descriptors.map { it.availabilitySummary },
                        systemSourceContributionLines = if (event.contributions.isEmpty()) {
                            listOf(appStrings.get(R.string.system_source_none_used))
                        } else {
                            event.contributions.map { it.summary }
                        },
                        hasMissingSystemSourcePermissions = event.descriptors.any { !it.isGranted },
                    ),
                )
            }

            is RuntimeSessionEvent.StatusSummaryUpdated -> {
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = event.summary.toUiModel().copy(
                        toolId = _uiState.value.runtimeStatus.toolId,
                        toolDisplayName = _uiState.value.runtimeStatus.toolDisplayName,
                        toolSideEffectLabel = _uiState.value.runtimeStatus.toolSideEffectLabel,
                        toolScopeLines = _uiState.value.runtimeStatus.toolScopeLines,
                        toolVisibilityLabel = _uiState.value.runtimeStatus.toolVisibilityLabel,
                        toolVisibilityReason = _uiState.value.runtimeStatus.toolVisibilityReason,
                        sourceLabel = _uiState.value.runtimeStatus.sourceLabel,
                        trustStateLabel = _uiState.value.runtimeStatus.trustStateLabel,
                        interopContractLabel = _uiState.value.runtimeStatus.interopContractLabel,
                        uriGrantLabel = _uiState.value.runtimeStatus.uriGrantLabel,
                        routeSummary = _uiState.value.runtimeStatus.routeSummary,
                        callerTrust = _uiState.value.runtimeStatus.callerTrust,
                        structuredActionTitle = _uiState.value.runtimeStatus.structuredActionTitle,
                        structuredCompleteness = _uiState.value.runtimeStatus.structuredCompleteness,
                        structuredFieldLines = _uiState.value.runtimeStatus.structuredFieldLines,
                        structuredWarnings = _uiState.value.runtimeStatus.structuredWarnings,
                        extensionStatusLines = _uiState.value.runtimeStatus.extensionStatusLines,
                        systemSourceStatusLines = _uiState.value.runtimeStatus.systemSourceStatusLines,
                        systemSourceContributionLines = _uiState.value.runtimeStatus.systemSourceContributionLines,
                        hasMissingSystemSourcePermissions = _uiState.value.runtimeStatus.hasMissingSystemSourcePermissions,
                    ),
                    screenState = if (event.summary.isBusy) {
                        WorkspaceScreenState.STREAMING
                    } else if (event.summary.awaitingInput || _uiState.value.pendingApproval != null) {
                        WorkspaceScreenState.AWAITING_APPROVAL
                    } else if (event.summary.isTerminal &&
                        _uiState.value.screenState == WorkspaceScreenState.RECOVERABLE_FAILURE
                    ) {
                        WorkspaceScreenState.RECOVERABLE_FAILURE
                    } else {
                        WorkspaceScreenState.READY_IDLE
                    },
                )
            }

            is RuntimeSessionEvent.RiskAssessed -> Unit

            is RuntimeSessionEvent.CallerVerified -> {
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        trustStateLabel = when (event.callerIdentity.trustState) {
                            CallerTrustState.TRUSTED -> appStrings.get(R.string.external_handoff_trust_trusted)
                            CallerTrustState.UNVERIFIED -> appStrings.get(R.string.external_handoff_trust_unverified)
                            CallerTrustState.DENIED -> appStrings.get(R.string.external_handoff_trust_denied)
                        },
                        sourceLabel = event.callerIdentity.sourceLabel,
                        interopContractLabel = event.callerIdentity.contractVersion?.let { version ->
                            appStrings.interopContractLabel(
                                entryType = null,
                                version = version,
                            )
                        }.orEmpty(),
                        uriGrantLabel = event.callerIdentity.grantSummary,
                        callerTrust = event.callerIdentity.trustReason,
                    ),
                )
            }

            is RuntimeSessionEvent.CapabilityRouted -> {
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        toolId = event.toolId,
                        toolDisplayName = event.toolDisplayName,
                        toolSideEffectLabel = event.toolSideEffectLabel,
                        toolScopeLines = event.toolScopeLines,
                        toolVisibilityLabel = event.visibilitySnapshot?.let {
                            appStrings.toolVisibilityStateLabel(it.state)
                        }.orEmpty(),
                        toolVisibilityReason = event.visibilitySnapshot?.reason.orEmpty(),
                        routeSummary = event.routeExplanation,
                    ),
                )
            }

            is RuntimeSessionEvent.StructuredActionPrepared -> {
                val preview = event.normalization.preview
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        structuredActionTitle = preview?.title.orEmpty(),
                        structuredCompleteness = preview?.completenessState?.let(appStrings::payloadCompletenessLabel).orEmpty(),
                        structuredFieldLines = preview?.fieldLines.orEmpty(),
                        structuredWarnings = preview?.warnings.orEmpty(),
                        supportingText = preview?.summary ?: _uiState.value.runtimeStatus.supportingText,
                    ),
                )
            }

            is RuntimeSessionEvent.PolicyResolved -> {
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        supportingText = event.decision.rationale,
                        awaitingInput = event.decision.awaitingInput,
                    ),
                )
            }

            is RuntimeSessionEvent.ApprovalRequested -> {
                _uiState.value = _uiState.value.copy(
                    pendingApproval = event.request.toUiModel(),
                    screenState = WorkspaceScreenState.AWAITING_APPROVAL,
                    isBusy = false,
                    isComposerEnabled = false,
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        headline = appStrings.get(R.string.workspace_approval_needed),
                        stageLabel = appStrings.get(R.string.runtime_stage_awaiting_approval),
                        supportingText = event.request.summary,
                        toolId = event.request.toolId,
                        toolDisplayName = event.request.toolDisplayName,
                        toolSideEffectLabel = event.request.sideEffectLabel,
                        toolScopeLines = event.request.scopeLines,
                        isBusy = false,
                        awaitingInput = true,
                        isTerminal = false,
                    ),
                )
            }

            is RuntimeSessionEvent.ApprovalResolved -> {
                _uiState.value = _uiState.value.copy(
                    pendingApproval = null,
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        awaitingInput = false,
                        supportingText = event.outcome.reason.orEmpty(),
                    ),
                    feedback = WorkspaceFeedbackUiModel(
                        messageId = "approval-${System.currentTimeMillis()}",
                        kind = when (event.outcome.outcome) {
                            ApprovalOutcomeType.APPROVED -> WorkspaceFeedbackKind.SUCCESS
                            ApprovalOutcomeType.REJECTED -> WorkspaceFeedbackKind.INFO
                            ApprovalOutcomeType.ABANDONED -> WorkspaceFeedbackKind.INFO
                        },
                        text = event.outcome.reason.orEmpty(),
                        scope = "approval",
                    ),
                )
            }

            is RuntimeSessionEvent.AuditRecorded -> {
                _uiState.value = _uiState.value.copy(
                    recentAudit = listOf(event.event.toUiModel()) +
                        _uiState.value.recentAudit.filterNot {
                            it.auditEventId == event.event.auditEventId
                        },
                ).let { stateWithAudit ->
                    stateWithAudit.copy(
                        recentAudit = stateWithAudit.recentAudit.take(6),
                    )
                }
            }

            is RuntimeSessionEvent.CapabilityStarted -> {
                _uiState.value = _uiState.value.copy(
                    runtimeStatus = _uiState.value.runtimeStatus.copy(
                        headline = appStrings.get(R.string.workspace_executing),
                        stageLabel = appStrings.get(R.string.workspace_executing),
                        supportingText = appStrings.get(
                            R.string.workspace_executing_provider_started,
                            event.providerId,
                            event.capabilityId,
                        ),
                        isBusy = true,
                        awaitingInput = false,
                        isTerminal = false,
                    ),
                )
            }

            is RuntimeSessionEvent.CapabilityOutputChunk -> {
                appendAssistantChunk(
                    turnId = _uiState.value.activeAssistantTurnId ?: return,
                    chunk = event.chunk,
                )
            }

            is RuntimeSessionEvent.CapabilityFailed -> {
                failAssistantTurn(
                    turnId = _uiState.value.activeAssistantTurnId,
                    message = event.userMessage,
                )
            }

            is RuntimeSessionEvent.SessionCompleted -> {
                _uiState.value = _uiState.value.copy(pendingApproval = null)
                finalizeAssistantTurn(
                    turnId = _uiState.value.activeAssistantTurnId ?: return,
                    content = event.outcome.outputText.orEmpty(),
                )
            }

            is RuntimeSessionEvent.SessionFailed -> {
                _uiState.value = _uiState.value.copy(pendingApproval = null)
                failAssistantTurn(
                    turnId = _uiState.value.activeAssistantTurnId,
                    message = event.outcome.userMessage,
                )
            }

            is RuntimeSessionEvent.SessionDenied -> {
                _uiState.value = _uiState.value.copy(pendingApproval = null)
                failAssistantTurn(
                    turnId = _uiState.value.activeAssistantTurnId,
                    message = event.outcome.userMessage,
                )
            }

            is RuntimeSessionEvent.SessionCancelled -> {
                _uiState.value = _uiState.value.copy(pendingApproval = null)
                cancelAssistantTurn(
                    turnId = _uiState.value.activeAssistantTurnId,
                    message = event.outcome.userMessage,
                )
            }

            is RuntimeSessionEvent.CapabilityRequested -> Unit

            is RuntimeSessionEvent.CapabilityCompleted -> Unit
        }
    }

    private fun trustStateLabel(sourceMetadata: RuntimeSourceMetadata): String {
        return when (sourceMetadata.trustState) {
            RuntimeSourceTrustState.TRUSTED -> appStrings.get(R.string.external_handoff_trust_trusted)
            RuntimeSourceTrustState.UNVERIFIED -> appStrings.get(R.string.external_handoff_trust_unverified)
            RuntimeSourceTrustState.DENIED -> appStrings.get(R.string.external_handoff_trust_denied)
        }
    }

    private fun interopContractLabel(sourceMetadata: RuntimeSourceMetadata): String {
        val version = sourceMetadata.contractVersion ?: return ""
        val entryType = sourceMetadata.entryType
            ?.uppercase()
            ?.let { raw ->
                runCatching { com.mobileclaw.app.runtime.ingress.InteropEntryType.valueOf(raw) }.getOrNull()
            }
        return appStrings.interopContractLabel(entryType = entryType, version = version)
    }

    fun onResetSessionConfirmed() {
        val activeModel = _uiState.value.activeModel ?: return
        viewModelScope.launch {
            streamJob?.cancel()
            runtimeSessionFacade.resetModelSession(activeModel.modelId)
            _uiState.value = _uiState.value.copy(
            activeSessionId = null,
            turns = emptyList(),
            activeAssistantTurnId = null,
            pendingAttachments = emptyList(),
            activeAttachments = emptyList(),
            pendingApproval = null,
                recentAudit = emptyList(),
                runtimeStatus = RuntimeStatusUiModel(
                    headline = appStrings.get(R.string.workspace_ready),
                    stageLabel = appStrings.get(R.string.workspace_ready),
                    supportingText = appStrings.get(R.string.workspace_fresh_session_ready),
                    toolId = "",
                    toolDisplayName = "",
                    toolSideEffectLabel = "",
                    toolScopeLines = emptyList(),
                    toolVisibilityLabel = "",
                    toolVisibilityReason = "",
                    sourceLabel = "",
                    trustStateLabel = "",
                    interopContractLabel = "",
                    uriGrantLabel = "",
                    routeSummary = "",
                    callerTrust = "",
                    extensionStatusLines = runtimeExtensionRegistry.discoverySummaries()
                        .take(4)
                        .map { summary ->
                            "${summary.displayName} (${appStrings.extensionTypeLabel(summary.extensionType)}) · ${summary.privacySummary} · ${summary.statusSummary}"
                        },
                    isBusy = false,
                    isTerminal = false,
                ),
                screenState = if (_uiState.value.isModelReady) {
                    WorkspaceScreenState.READY_IDLE
                } else {
                    _uiState.value.screenState
                },
                isBusy = false,
                isComposerEnabled = _uiState.value.isModelReady,
                feedback = WorkspaceFeedbackUiModel(
                    messageId = "reset-${System.currentTimeMillis()}",
                    kind = WorkspaceFeedbackKind.SUCCESS,
                    text = appStrings.get(R.string.runtime_session_reset),
                    scope = "reset",
                ),
            )
        }
    }

    fun onApprovePendingApproval() {
        val approval = _uiState.value.pendingApproval ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pendingApproval = null,
                screenState = WorkspaceScreenState.STREAMING,
                isBusy = true,
                isComposerEnabled = false,
                runtimeStatus = _uiState.value.runtimeStatus.copy(
                    awaitingInput = false,
                    supportingText = appStrings.get(R.string.runtime_policy_approval_resumed),
                ),
            )
            runtimeSessionFacade.resolveApprovalRequest(
                approvalRequestId = approval.approvalRequestId,
                outcome = ApprovalOutcomeType.APPROVED,
            )
        }
    }

    fun onRejectPendingApproval() {
        val approval = _uiState.value.pendingApproval ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pendingApproval = null,
                runtimeStatus = _uiState.value.runtimeStatus.copy(
                    awaitingInput = false,
                    supportingText = appStrings.get(R.string.runtime_policy_approval_rejected),
                ),
            )
            runtimeSessionFacade.resolveApprovalRequest(
                approvalRequestId = approval.approvalRequestId,
                outcome = ApprovalOutcomeType.REJECTED,
            )
        }
    }

    fun onDismissPendingApproval() {
        val approval = _uiState.value.pendingApproval ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pendingApproval = null,
                runtimeStatus = _uiState.value.runtimeStatus.copy(
                    awaitingInput = false,
                    supportingText = appStrings.get(R.string.runtime_policy_approval_abandoned),
                ),
            )
            runtimeSessionFacade.resolveApprovalRequest(
                approvalRequestId = approval.approvalRequestId,
                outcome = ApprovalOutcomeType.ABANDONED,
            )
        }
    }

    private fun observeModels() {
        viewModelScope.launch {
            localModelCatalog.models.collectLatest { models ->
                val activeModelId = _uiState.value.activeModel?.modelId
                val selected = models.firstOrNull { it.modelId == activeModelId }
                    ?: models.firstOrNull { it.isSelectable }
                    ?: models.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    availableModels = models,
                    activeModel = selected,
                )
                selected?.let { bindSelectedModel(it) }
            }
        }
    }

    private fun observeContextInspector() {
        contextInspectorJob?.cancel()
            contextInspectorJob = viewModelScope.launch {
            combine(
                personaRepository.personaProfile,
                scopedMemoryRepository.observeAll(),
            ) { _, _ -> Unit }.collectLatest {
                refreshContextInspector()
            }
        }
    }

    private suspend fun bindSelectedModel(model: LocalModelProfile) {
        modelHealthJob?.cancel()
        _uiState.value = _uiState.value.copy(activeModel = model)
        modelHealthJob = viewModelScope.launch {
            localModelCatalog.observeHealth(model.modelId).collectLatest { health ->
                if (health.availabilityStatus == ModelAvailabilityStatus.READY) {
                    handleReadyModel(model, health)
                } else {
                    handleNonReadyModel(model, health)
                }
            }
        }
    }

    private fun handleReadyModel(
        model: LocalModelProfile,
        health: ModelHealthSnapshot,
    ) {
        val hasPendingApproval = _uiState.value.pendingApproval != null
        _uiState.value = _uiState.value.copy(
            screenState = if (hasPendingApproval) {
                WorkspaceScreenState.AWAITING_APPROVAL
            } else {
                WorkspaceScreenState.READY_IDLE
            },
            activeModel = model,
            modelHealth = health.toUiModel(model),
                runtimeStatus = _uiState.value.runtimeStatus.copy(
                    headline = appStrings.get(R.string.workspace_ready),
                    stageLabel = appStrings.get(R.string.workspace_ready),
                    supportingText = currentReadySummary(),
                    toolId = _uiState.value.runtimeStatus.toolId,
                    toolDisplayName = _uiState.value.runtimeStatus.toolDisplayName,
                    toolSideEffectLabel = _uiState.value.runtimeStatus.toolSideEffectLabel,
                    toolScopeLines = _uiState.value.runtimeStatus.toolScopeLines,
                    toolVisibilityLabel = _uiState.value.runtimeStatus.toolVisibilityLabel,
                    toolVisibilityReason = _uiState.value.runtimeStatus.toolVisibilityReason,
                    routeSummary = _uiState.value.runtimeStatus.routeSummary,
                    callerTrust = _uiState.value.runtimeStatus.callerTrust,
                    isBusy = false,
                awaitingInput = hasPendingApproval,
                isTerminal = false,
            ),
            isComposerEnabled = !hasPendingApproval,
            isBusy = false,
        )
        reconcileAttachmentsForModel(model)
    }

    private fun handleNonReadyModel(
        model: LocalModelProfile,
        health: ModelHealthSnapshot,
    ) {
        _uiState.value = _uiState.value.copy(
            screenState = when (health.availabilityStatus) {
                ModelAvailabilityStatus.PREPARING -> WorkspaceScreenState.PREPARING
                ModelAvailabilityStatus.FAILED -> WorkspaceScreenState.RECOVERABLE_FAILURE
                ModelAvailabilityStatus.UNAVAILABLE -> WorkspaceScreenState.UNAVAILABLE
                ModelAvailabilityStatus.READY -> WorkspaceScreenState.READY_IDLE
            },
            activeModel = model,
            activeSessionId = null,
            modelHealth = health.toUiModel(model),
                runtimeStatus = _uiState.value.runtimeStatus.copy(
                    headline = when (health.availabilityStatus) {
                    ModelAvailabilityStatus.PREPARING -> appStrings.get(R.string.workspace_preparing_model)
                    ModelAvailabilityStatus.FAILED -> appStrings.get(R.string.workspace_recovery_needed)
                    ModelAvailabilityStatus.UNAVAILABLE -> appStrings.get(R.string.workspace_unavailable)
                    ModelAvailabilityStatus.READY -> appStrings.get(R.string.workspace_ready)
                },
                stageLabel = when (health.availabilityStatus) {
                    ModelAvailabilityStatus.PREPARING -> appStrings.get(R.string.workspace_preparing_model)
                    ModelAvailabilityStatus.FAILED -> appStrings.get(R.string.workspace_recovery_needed)
                    ModelAvailabilityStatus.UNAVAILABLE -> appStrings.get(R.string.workspace_unavailable)
                    ModelAvailabilityStatus.READY -> appStrings.get(R.string.workspace_ready)
                    },
                    supportingText = health.supportingText,
                    toolId = "",
                    toolDisplayName = "",
                    toolSideEffectLabel = "",
                    toolScopeLines = emptyList(),
                    toolVisibilityLabel = "",
                    toolVisibilityReason = "",
                    routeSummary = "",
                    callerTrust = "",
                isBusy = health.availabilityStatus == ModelAvailabilityStatus.PREPARING,
                awaitingInput = false,
                isTerminal = health.availabilityStatus == ModelAvailabilityStatus.FAILED,
            ),
            isComposerEnabled = false,
            isBusy = health.availabilityStatus == ModelAvailabilityStatus.PREPARING,
        )
        reconcileAttachmentsForModel(model)
    }

    private fun replaceAssistantTurnId(turnId: String) {
        val state = _uiState.value
        val currentTurnId = state.activeAssistantTurnId ?: return
        _uiState.value = state.copy(
            activeAssistantTurnId = turnId,
            turns = state.turns.map { turn ->
                if (turn.turnId == currentTurnId) {
                    turn.copy(turnId = turnId)
                } else {
                    turn
                }
            },
        )
    }

    private fun appendAssistantChunk(
        turnId: String,
        chunk: String,
    ) {
        val state = _uiState.value
        val turnExists = state.turns.any { it.turnId == turnId }
        val updatedTurns = if (turnExists) {
            state.turns.map { turn ->
                if (turn.turnId == turnId) {
                    turn.copy(
                        content = turn.content + chunk,
                        state = ChatTurnStateUi.STREAMING,
                    )
                } else {
                    turn
                }
            }
        } else {
            state.turns
        }
        _uiState.value = state.copy(turns = updatedTurns)
    }

    private fun finalizeAssistantTurn(
        turnId: String,
        content: String,
    ) {
        val state = _uiState.value
        val currentContent = state.turns.firstOrNull { it.turnId == turnId }?.content.orEmpty()
        val resolvedContent = currentContent.ifBlank { content }
        _uiState.value = state.copy(
            screenState = WorkspaceScreenState.READY_IDLE,
            isBusy = false,
            isComposerEnabled = true,
            activeAssistantTurnId = null,
            runtimeStatus = state.runtimeStatus.copy(
                headline = appStrings.get(R.string.runtime_stage_completed),
                stageLabel = appStrings.get(R.string.runtime_stage_completed),
                supportingText = appStrings.get(R.string.workspace_latest_runtime_completed),
                routeSummary = state.runtimeStatus.routeSummary,
                callerTrust = state.runtimeStatus.callerTrust,
                isBusy = false,
                awaitingInput = false,
                isTerminal = false,
            ),
            turns = state.turns.map { turn ->
                if (turn.turnId == turnId) {
                    turn.copy(content = resolvedContent, state = ChatTurnStateUi.COMPLETE)
                } else {
                    turn
                }
            },
            feedback = WorkspaceFeedbackUiModel(
                messageId = "send-${System.currentTimeMillis()}",
                kind = WorkspaceFeedbackKind.INFO,
                text = appStrings.get(R.string.workspace_runtime_response_complete),
                scope = "send",
            ),
        )
    }

    private fun failAssistantTurn(
        turnId: String?,
        message: String,
    ) {
        val state = _uiState.value
        _uiState.value = state.copy(
            screenState = WorkspaceScreenState.RECOVERABLE_FAILURE,
            isBusy = false,
            isComposerEnabled = state.isModelReady,
            activeAssistantTurnId = null,
            runtimeStatus = state.runtimeStatus.copy(
                headline = appStrings.get(R.string.runtime_stage_failed),
                stageLabel = appStrings.get(R.string.runtime_stage_failed),
                supportingText = message,
                routeSummary = state.runtimeStatus.routeSummary,
                callerTrust = state.runtimeStatus.callerTrust,
                isBusy = false,
                awaitingInput = false,
                isTerminal = true,
            ),
            turns = state.turns.map { turn ->
                if (turnId != null && turn.turnId == turnId) {
                    turn.copy(state = ChatTurnStateUi.FAILED)
                } else {
                    turn
                }
            },
            feedback = WorkspaceFeedbackUiModel(
                messageId = "send-failed-${System.currentTimeMillis()}",
                kind = WorkspaceFeedbackKind.ERROR,
                text = message,
                scope = "generation_failure",
            ),
        )
    }

    private fun cancelAssistantTurn(
        turnId: String?,
        message: String,
    ) {
        val state = _uiState.value
        _uiState.value = state.copy(
            screenState = WorkspaceScreenState.READY_IDLE,
            isBusy = false,
            isComposerEnabled = state.isModelReady,
            activeAssistantTurnId = null,
            runtimeStatus = state.runtimeStatus.copy(
                headline = appStrings.get(R.string.runtime_stage_cancelled),
                stageLabel = appStrings.get(R.string.runtime_stage_cancelled),
                supportingText = message,
                routeSummary = state.runtimeStatus.routeSummary,
                callerTrust = state.runtimeStatus.callerTrust,
                isBusy = false,
                awaitingInput = false,
                isTerminal = false,
            ),
            turns = state.turns.map { turn ->
                if (turnId != null && turn.turnId == turnId) {
                    turn.copy(state = ChatTurnStateUi.FAILED)
                } else {
                    turn
                }
            },
            feedback = WorkspaceFeedbackUiModel(
                messageId = "send-cancelled-${System.currentTimeMillis()}",
                kind = WorkspaceFeedbackKind.INFO,
                text = message,
                scope = "generation_cancelled",
            ),
        )
    }

    private fun currentReadySummary(): String {
        return if (_uiState.value.turns.isEmpty()) {
            appStrings.get(R.string.workspace_fresh_session_ready)
        } else {
            appStrings.get(R.string.workspace_active_runtime_session, _uiState.value.turns.size)
        }
    }

    private suspend fun refreshContextInspector() {
        val personaSummary = personaRepository.getCurrentProfile().summaryText(appStrings)
        val retrievedContext = memoryRetrievalService.retrieveContext(
            RetrievalQuery(
                requestId = "inspector-preview",
                userInput = _uiState.value.composerDraft.ifBlank {
                    _uiState.value.turns.lastOrNull()?.content.orEmpty()
                },
                originApp = "agent_workspace",
                subjectKey = _uiState.value.workspaceId,
                deviceId = "local_device",
                allowPrivate = true,
                maxItems = 5,
            ),
        )
        val activeSummary = retrievedContext.toActiveContextSummary(appStrings)
        val memories = retrievedContext.selectedMemoryItems
        val allCompatibilities = memories.flatMap { exportDecisionService.extensionCompatibilities(it) }
        val items = memories
            .map { item ->
                val mergeCandidate = item.toMergeCandidate()
                val exportBundle = exportDecisionService.buildExportBundle(item)
                val extensionCompatibilities = exportDecisionService.extensionCompatibilities(item)
                ContextMemoryUiModel(
                    memoryId = item.memoryId,
                    title = item.title,
                    detail = item.userVisibleText(),
                    badge = listOf(
                        appStrings.memoryLifecycleLabel(item.lifecycle),
                        appStrings.memoryScopeLabel(item.scope),
                        appStrings.memorySourceLabel(item.sourceType),
                    ).joinToString(separator = " · "),
                    syncDetail = appStrings.get(
                        R.string.memory_detail_sync,
                        appStrings.memoryExposureLabel(item.exposurePolicy),
                        appStrings.memorySyncPolicyLabel(item.syncPolicy),
                    ),
                    mergeDetail = appStrings.get(
                        R.string.memory_detail_merge,
                        mergeCandidate.logicalRecordId,
                        mergeCandidate.logicalVersion,
                        mergeCandidate.originDeviceId ?: "local_device",
                    ),
                    exportDetail = appStrings.get(
                        R.string.memory_detail_export,
                        appStrings.exportModeLabel(exportBundle.exportMode),
                        exportBundle.redactedFields.joinToString().ifBlank {
                            appStrings.get(R.string.memory_detail_none)
                        },
                    ),
                    extensionDetail = appStrings.get(
                        R.string.memory_detail_extensions,
                        extensionCompatibilities.count { it.isCompatible },
                        extensionCompatibilities
                            .filter { it.isCompatible }
                            .joinToString(separator = ", ") {
                                "${it.displayName} (${appStrings.extensionTypeLabel(it.extensionType)})"
                            }
                            .ifBlank { appStrings.get(R.string.memory_detail_none) },
                    ),
                    isPinned = item.isPinned,
                    canPromote = item.lifecycle != MemoryLifecycle.DURABLE,
                    canDemote = item.lifecycle != MemoryLifecycle.EPHEMERAL,
                    canExpire = !item.isPinned,
                    canExport = item.exposurePolicy != MemoryExposurePolicy.PRIVATE,
                )
            }
        _uiState.value = _uiState.value.copy(
            contextInspector = ContextInspectorUiModel(
                personaSummary = personaSummary,
                activeMemoryItems = items,
                hiddenPrivateCount = retrievedContext.hiddenPrivateCount,
                totalEligibleCount = retrievedContext.totalEligibleCount,
                excludedCount = retrievedContext.excludedCount,
                retrievalSummary = activeSummary.retrievalSummary,
                extensionSummary = appStrings.get(
                    R.string.memory_extension_summary,
                    allCompatibilities.size,
                ),
            ),
            runtimeStatus = _uiState.value.runtimeStatus.copy(
                extensionStatusLines = runtimeExtensionRegistry.discoverySummaries()
                    .take(4)
                    .map { summary ->
                        "${summary.displayName} (${appStrings.extensionTypeLabel(summary.extensionType)}) · ${summary.privacySummary} · ${summary.statusSummary}"
                    },
            ),
        )
    }

    private fun refreshExtensionDiscovery() {
        _uiState.value = _uiState.value.copy(
            runtimeStatus = _uiState.value.runtimeStatus.copy(
                extensionStatusLines = runtimeExtensionRegistry.discoverySummaries()
                    .take(4)
                    .map { summary ->
                        "${summary.displayName} (${appStrings.extensionTypeLabel(summary.extensionType)}) · ${summary.privacySummary} · ${summary.statusSummary}"
                    },
            ),
        )
    }

    private fun buildPortabilityPreview(item: com.mobileclaw.app.runtime.memory.MemoryItem, preferFullExport: Boolean): PortabilityBundlePreviewUiModel {
        val preview = buildPortabilityPreviewDomain(item, preferFullExport)
        return preview.toUiModel()
    }

    private fun buildPortabilityPreviewDomain(
        item: com.mobileclaw.app.runtime.memory.MemoryItem,
        preferFullExport: Boolean,
    ): PortabilityBundlePreview {
        val redactionPolicy = exportDecisionService.evaluateRedactionPolicy(item)
        val bundle = exportDecisionService.buildExportBundle(
            item = item,
            preferFullExport = preferFullExport,
        )
        val compatibilities = exportDecisionService.extensionCompatibilities(item)
        return portabilityBundleFormatter.buildPreview(
            item = item,
            bundle = bundle,
            redactionPolicy = redactionPolicy,
            compatibilities = compatibilities,
        )
    }

    private fun PortabilityBundlePreview.toUiModel(): PortabilityBundlePreviewUiModel {
        return PortabilityBundlePreviewUiModel(
            memoryId = memoryId,
            title = title,
            exportModeLabel = appStrings.exportModeLabel(bundle.exportMode),
            payloadPreview = payloadPreview,
            redactionReason = redactionReason,
            includedFields = bundle.includedFields,
            redactedFields = bundle.redactedFields,
            compatibilityLines = compatibilityLines.map { line ->
                PortabilityCompatibilityUiModel(
                    title = line.title,
                    detail = line.detail,
                    statusLabel = if (line.isCompatible) {
                        appStrings.get(R.string.portability_compatibility_yes)
                    } else {
                        appStrings.get(R.string.portability_compatibility_no)
                    },
                )
            },
            canShare = canShare,
            canSwitchToFull = canSwitchToFull,
            canSwitchToSummary = canSwitchToSummary,
            isFullModeSelected = bundle.exportMode == com.mobileclaw.app.runtime.memory.ExportMode.FULL_RECORD,
        )
    }

    private fun ModelHealthSnapshot.toUiModel(model: LocalModelProfile): ModelHealthUiModel {
        return ModelHealthUiModel(
            modelId = model.modelId,
            displayName = model.displayName,
            availabilityStatus = availabilityStatus,
            headline = headline,
            supportingText = supportingText,
            primaryActionLabel = primaryActionLabel,
            supportsImage = modalityCapabilities.supportsImage,
            supportsAudio = modalityCapabilities.supportsAudio,
        )
    }

    private fun importAttachment(sourceUri: Uri, preferredKind: RuntimeAttachmentKind) {
        viewModelScope.launch {
            attachmentStore.importAttachment(
                sourceUri = sourceUri,
                preferredKind = preferredKind,
                sourceLabel = appStrings.get(R.string.multimodal_attachment_source_composer),
            ).onSuccess { attachment ->
                val model = _uiState.value.activeModel
                if (model != null && !supportsAttachment(model, attachment.kind)) {
                    _uiState.value = _uiState.value.copy(
                        feedback = WorkspaceFeedbackUiModel(
                            messageId = "attachment-unsupported-${System.currentTimeMillis()}",
                            kind = WorkspaceFeedbackKind.INFO,
                            text = appStrings.get(R.string.multimodal_model_capability_missing),
                            scope = "multimodal_attachment",
                        ),
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        pendingAttachments = _uiState.value.pendingAttachments + toAttachmentUiModel(attachment),
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    feedback = WorkspaceFeedbackUiModel(
                        messageId = "attachment-failed-${System.currentTimeMillis()}",
                        kind = WorkspaceFeedbackKind.ERROR,
                        text = error.message ?: appStrings.get(R.string.multimodal_attachment_open_failed),
                        scope = "multimodal_attachment",
                    ),
                )
            }
        }
    }

    private fun reconcileAttachmentsForModel(model: LocalModelProfile) {
        val filteredPending = _uiState.value.pendingAttachments.filter { supportsAttachment(model, it.kind) }
        val filteredActive = _uiState.value.activeAttachments.filter { supportsAttachment(model, it.kind) }
        if (filteredPending.size != _uiState.value.pendingAttachments.size ||
            filteredActive.size != _uiState.value.activeAttachments.size
        ) {
            _uiState.value = _uiState.value.copy(
                pendingAttachments = filteredPending,
                activeAttachments = filteredActive,
                feedback = WorkspaceFeedbackUiModel(
                    messageId = "attachment-pruned-${System.currentTimeMillis()}",
                    kind = WorkspaceFeedbackKind.INFO,
                    text = appStrings.get(R.string.multimodal_attachments_pruned),
                    scope = "multimodal_attachment",
                ),
            )
        }
    }

    private fun supportsAllAttachments(
        model: LocalModelProfile,
        attachments: List<PendingAttachment>,
    ): Boolean = attachments.all { supportsAttachment(model, it.kind) }

    private fun supportsAttachment(
        model: LocalModelProfile,
        kind: RuntimeAttachmentKind,
    ): Boolean {
        return when (kind) {
            RuntimeAttachmentKind.IMAGE -> model.modalityCapabilities.supportsImage
            RuntimeAttachmentKind.AUDIO -> model.modalityCapabilities.supportsAudio
        }
    }

    private fun toAttachmentUiModel(attachment: PendingAttachment): AttachmentUiModel {
        return AttachmentUiModel(
            attachmentId = attachment.attachmentId,
            kind = attachment.kind,
            displayName = attachment.displayName,
            previewSummary = attachment.previewSummary,
            sourceLabel = attachment.sourceLabel,
            mimeType = attachment.mimeType,
            localPath = attachment.localPath,
        )
    }

    private fun buildAttachmentVisibleInput(attachments: List<AttachmentUiModel>): String {
        return appStrings.get(
            R.string.multimodal_visible_request_summary,
            attachments.joinToString(separator = ", ") { it.displayName },
        )
    }

    private fun buildPendingAttachmentVisibleInput(attachments: List<PendingAttachment>): String {
        return appStrings.get(
            R.string.multimodal_visible_request_summary,
            attachments.joinToString(separator = ", ") { it.displayName },
        )
    }

    private fun decorateWorkspaceUiState(state: AgentWorkspaceUiState): AgentWorkspaceUiState {
        val attentionMode = when {
            state.pendingApproval != null || state.screenState == WorkspaceScreenState.AWAITING_APPROVAL ->
                WorkspaceAttentionMode.AWAITING_APPROVAL
            state.screenState == WorkspaceScreenState.RECOVERABLE_FAILURE ->
                WorkspaceAttentionMode.FAILURE
            state.screenState == WorkspaceScreenState.PREPARING ->
                WorkspaceAttentionMode.PREPARING
            state.screenState == WorkspaceScreenState.UNAVAILABLE ->
                WorkspaceAttentionMode.UNAVAILABLE
            else -> WorkspaceAttentionMode.NORMAL
        }
        val primarySignals = buildList {
            state.runtimeStatus.sourceLabel.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.trustStateLabel.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.routeSummary.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.structuredActionTitle.takeIf { it.isNotBlank() }?.let(::add)
        }.take(4)
        val secondarySignals = buildList {
            state.runtimeStatus.systemSourceContributionLines.firstOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.systemSourceStatusLines.firstOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
            state.recentAudit.firstOrNull()?.headline?.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.callerTrust.takeIf { it.isNotBlank() }?.let(::add)
        }.take(2)
        val modelSummary = modelAvailabilityLabel(state.activeModel?.availabilityStatus)
        val contextSummary = if (state.contextInspector.activeMemoryItems.isEmpty()) {
            state.contextInspector.retrievalSummary
        } else {
            appStrings.get(
                R.string.workspace_secondary_context_summary,
                state.contextInspector.activeMemoryItems.size,
            )
        }
        val governanceSummary = if (state.governanceCenter.callers.isEmpty()) {
            appStrings.get(R.string.common_manage)
        } else {
            appStrings.get(
                R.string.workspace_secondary_governance_summary,
                state.governanceCenter.callers.size,
            )
        }
        val detailSummary = state.recentAudit.firstOrNull()?.headline
            ?: state.runtimeStatus.stageLabel
        return state.copy(
            attentionMode = attentionMode,
            statusDigest = WorkspaceStatusDigestUiModel(
                attentionMode = attentionMode,
                stageLabel = state.runtimeStatus.stageLabel,
                headline = state.runtimeStatus.headline,
                supportingText = state.runtimeStatus.supportingText,
                primarySignals = primarySignals,
                secondarySignals = secondarySignals,
                showsPermissionAction = state.runtimeStatus.hasMissingSystemSourcePermissions,
            ),
            secondaryEntries = listOf(
                WorkspaceSecondaryEntryUiModel(
                    entryId = "model",
                    label = appStrings.get(R.string.workspace_secondary_model),
                    supportingText = modelSummary,
                    isHighlighted = !state.isModelReady,
                ),
                WorkspaceSecondaryEntryUiModel(
                    entryId = "context",
                    label = appStrings.get(R.string.workspace_secondary_context),
                    supportingText = contextSummary,
                    isHighlighted = state.contextInspector.activeMemoryItems.isNotEmpty(),
                ),
                WorkspaceSecondaryEntryUiModel(
                    entryId = "governance",
                    label = appStrings.get(R.string.workspace_secondary_governance),
                    supportingText = governanceSummary,
                    isHighlighted = state.governanceCenter.activities.isNotEmpty(),
                ),
                WorkspaceSecondaryEntryUiModel(
                    entryId = "details",
                    label = appStrings.get(R.string.workspace_secondary_details),
                    supportingText = detailSummary,
                    isHighlighted = attentionMode != WorkspaceAttentionMode.NORMAL,
                ),
            ),
            runtimeControlCenter = buildRuntimeControlCenter(state, attentionMode),
        )
    }

    private fun buildRuntimeControlCenter(
        state: AgentWorkspaceUiState,
        attentionMode: WorkspaceAttentionMode,
    ): RuntimeControlCenterUiModel {
        return RuntimeControlCenterUiModel(
            title = appStrings.get(R.string.runtime_control_center_title),
            headline = state.runtimeStatus.headline.ifBlank {
                appStrings.get(R.string.workspace_ready)
            },
            supportingText = state.runtimeStatus.supportingText,
            attentionMode = attentionMode,
            traceSections = buildRuntimeTraceSections(state, attentionMode),
            artifactEntries = buildManagedArtifactEntries(state),
        )
    }

    private fun buildRuntimeTraceSections(
        state: AgentWorkspaceUiState,
        attentionMode: WorkspaceAttentionMode,
    ): List<RuntimeTraceSectionUiModel> {
        val sourceLines = listOfNotNull(
            state.runtimeStatus.sourceLabel.takeIf { it.isNotBlank() },
            state.runtimeStatus.trustStateLabel.takeIf { it.isNotBlank() },
            state.runtimeStatus.interopContractLabel.takeIf { it.isNotBlank() },
            state.runtimeStatus.uriGrantLabel.takeIf { it.isNotBlank() },
        )
        val toolLines = buildList {
            state.runtimeStatus.toolDisplayName.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.toolSideEffectLabel.takeIf { it.isNotBlank() }?.let(::add)
            addAll(state.runtimeStatus.toolScopeLines.filter { it.isNotBlank() })
            state.runtimeStatus.routeSummary.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.structuredActionTitle.takeIf { it.isNotBlank() }?.let { title ->
                val completeness = state.runtimeStatus.structuredCompleteness
                add(
                    if (completeness.isNotBlank()) {
                        "$title · $completeness"
                    } else {
                        title
                    },
                )
            }
            addAll(state.runtimeStatus.structuredFieldLines.filter { it.isNotBlank() }.take(2))
            addAll(state.runtimeStatus.structuredWarnings.filter { it.isNotBlank() }.take(1))
        }
        val approvalLines = buildList {
            state.pendingApproval?.summary?.takeIf { it.isNotBlank() }?.let(::add)
            state.pendingApproval?.toolDisplayName?.takeIf { it.isNotBlank() }?.let(::add)
            if (state.pendingApproval == null) {
                state.recentAudit.firstOrNull { audit ->
                    audit.headline.contains("approval", ignoreCase = true) ||
                        audit.details.contains("approval", ignoreCase = true)
                }?.let { audit ->
                    add(audit.headline)
                    audit.details.takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
        val contextLines = buildList {
            state.contextInspector.personaSummary.takeIf { it.isNotBlank() }?.let(::add)
            state.contextInspector.retrievalSummary.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.systemSourceContributionLines.firstOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.systemSourceStatusLines.firstOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
        }
        val extensionLines = state.runtimeStatus.extensionStatusLines.filter { it.isNotBlank() }.take(4)
        val constraintLines = buildList {
            state.runtimeStatus.toolVisibilityReason.takeIf { it.isNotBlank() }?.let(::add)
            state.runtimeStatus.callerTrust.takeIf { it.isNotBlank() }?.let(::add)
            if (state.runtimeStatus.hasMissingSystemSourcePermissions) {
                add(appStrings.get(R.string.runtime_control_permissions_needed))
            }
            if (attentionMode == WorkspaceAttentionMode.FAILURE ||
                attentionMode == WorkspaceAttentionMode.AWAITING_APPROVAL ||
                attentionMode == WorkspaceAttentionMode.UNAVAILABLE
            ) {
                state.runtimeStatus.supportingText.takeIf { it.isNotBlank() }?.let(::add)
            }
        }.distinct()
        val recentActivityLines = state.recentAudit.take(3).flatMap { audit ->
            listOfNotNull(
                audit.headline.takeIf { it.isNotBlank() },
                audit.details.takeIf { it.isNotBlank() },
            )
        }
        return listOf(
            RuntimeTraceSectionUiModel(
                sectionId = "source",
                title = appStrings.get(R.string.runtime_control_section_source),
                lines = sourceLines,
                emptyState = appStrings.get(R.string.runtime_control_empty_source),
            ),
            RuntimeTraceSectionUiModel(
                sectionId = "tool",
                title = appStrings.get(R.string.runtime_control_section_tool_path),
                lines = toolLines,
                emptyState = appStrings.get(R.string.runtime_control_empty_tool_path),
            ),
            RuntimeTraceSectionUiModel(
                sectionId = "approval",
                title = appStrings.get(R.string.runtime_control_section_approval),
                lines = approvalLines,
                emptyState = appStrings.get(R.string.runtime_control_empty_approval),
                isHighlighted = state.pendingApproval != null,
            ),
            RuntimeTraceSectionUiModel(
                sectionId = "context",
                title = appStrings.get(R.string.runtime_control_section_context),
                lines = contextLines,
                emptyState = appStrings.get(R.string.runtime_control_empty_context),
            ),
            RuntimeTraceSectionUiModel(
                sectionId = "extensions",
                title = appStrings.get(R.string.runtime_control_section_extensions),
                lines = extensionLines,
                emptyState = appStrings.get(R.string.runtime_control_empty_extensions),
            ),
            RuntimeTraceSectionUiModel(
                sectionId = "constraints",
                title = appStrings.get(R.string.runtime_control_section_constraints),
                lines = constraintLines,
                emptyState = appStrings.get(R.string.runtime_control_empty_constraints),
                isHighlighted = attentionMode != WorkspaceAttentionMode.NORMAL,
            ),
            RuntimeTraceSectionUiModel(
                sectionId = "recent_activity",
                title = appStrings.get(R.string.runtime_control_section_recent_activity),
                lines = recentActivityLines,
                emptyState = appStrings.get(R.string.runtime_control_empty_recent_activity),
            ),
        )
    }

    private fun buildManagedArtifactEntries(
        state: AgentWorkspaceUiState,
    ): List<ManagedArtifactEntryUiModel> {
        val extensionCount = state.runtimeStatus.extensionStatusLines.size
        return listOf(
            ManagedArtifactEntryUiModel(
                artifactId = "model",
                title = appStrings.get(R.string.runtime_control_artifact_model),
                summary = state.modelHealth.displayName.ifBlank {
                    appStrings.get(R.string.runtime_control_model_none_selected)
                },
                statusLine = modelAvailabilityLabel(state.activeModel?.availabilityStatus),
                actionLabel = appStrings.get(R.string.runtime_control_action_choose_model),
                isEditable = true,
            ),
            ManagedArtifactEntryUiModel(
                artifactId = "memory",
                title = appStrings.get(R.string.runtime_control_artifact_memory),
                summary = state.contextInspector.retrievalSummary,
                statusLine = if (state.contextInspector.activeMemoryItems.isEmpty()) {
                    appStrings.get(R.string.runtime_control_memory_none_active)
                } else {
                    appStrings.get(
                        R.string.runtime_control_memory_active_count,
                        state.contextInspector.activeMemoryItems.size,
                    )
                },
                actionLabel = appStrings.get(R.string.runtime_control_action_inspect_memory),
                isEditable = true,
            ),
            ManagedArtifactEntryUiModel(
                artifactId = "governance",
                title = appStrings.get(R.string.runtime_control_artifact_governance),
                summary = if (state.governanceCenter.activities.isEmpty()) {
                    appStrings.get(R.string.runtime_control_governance_idle)
                } else {
                    state.governanceCenter.activities.first().headline
                },
                statusLine = if (state.governanceCenter.callers.isEmpty()) {
                    appStrings.get(R.string.governance_no_callers)
                } else {
                    appStrings.get(
                        R.string.workspace_secondary_governance_summary,
                        state.governanceCenter.callers.size,
                    )
                },
                actionLabel = appStrings.get(R.string.runtime_control_action_open_governance),
                isEditable = true,
            ),
            ManagedArtifactEntryUiModel(
                artifactId = "approval",
                title = appStrings.get(R.string.runtime_control_artifact_approval),
                summary = state.pendingApproval?.summary ?: appStrings.get(R.string.runtime_control_approval_none_pending),
                statusLine = state.pendingApproval?.toolDisplayName
                    ?.takeIf { it.isNotBlank() }
                    ?: appStrings.get(R.string.runtime_control_state_inspect_only),
                actionLabel = "",
                isEditable = false,
                unavailableReason = if (state.pendingApproval != null) {
                    appStrings.get(R.string.runtime_control_approval_managed_inline)
                } else {
                    appStrings.get(R.string.runtime_control_approval_no_edit_action)
                },
            ),
            ManagedArtifactEntryUiModel(
                artifactId = "extensions",
                title = appStrings.get(R.string.runtime_control_artifact_extensions),
                summary = state.runtimeStatus.extensionStatusLines.firstOrNull()
                    ?: appStrings.get(R.string.runtime_control_extensions_none_detected),
                statusLine = appStrings.get(
                    R.string.runtime_control_extensions_count,
                    extensionCount,
                ),
                actionLabel = "",
                isEditable = false,
                unavailableReason = appStrings.get(R.string.runtime_control_extensions_inspect_only),
            ),
            ManagedArtifactEntryUiModel(
                artifactId = "system_permissions",
                title = appStrings.get(R.string.runtime_control_artifact_system_sources),
                summary = state.runtimeStatus.systemSourceContributionLines.firstOrNull()
                    ?: appStrings.get(R.string.runtime_control_system_sources_idle),
                statusLine = state.runtimeStatus.systemSourceStatusLines.firstOrNull()
                    ?: appStrings.get(R.string.runtime_control_state_inspect_only),
                actionLabel = if (state.runtimeStatus.hasMissingSystemSourcePermissions) {
                    appStrings.get(R.string.workspace_request_permissions)
                } else {
                    ""
                },
                isEditable = state.runtimeStatus.hasMissingSystemSourcePermissions,
                unavailableReason = if (state.runtimeStatus.hasMissingSystemSourcePermissions) {
                    appStrings.get(R.string.runtime_control_permissions_needed)
                } else {
                    ""
                },
            ),
        )
    }

    private fun modelAvailabilityLabel(status: ModelAvailabilityStatus?): String {
        return when (status) {
            ModelAvailabilityStatus.READY -> appStrings.get(R.string.model_status_ready)
            ModelAvailabilityStatus.PREPARING -> appStrings.get(R.string.model_status_preparing)
            ModelAvailabilityStatus.FAILED -> appStrings.get(R.string.model_status_failed)
            ModelAvailabilityStatus.UNAVAILABLE, null -> appStrings.get(R.string.model_status_unavailable)
        }
    }
}
