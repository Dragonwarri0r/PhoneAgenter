package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.action.StructuredActionNormalizer
import com.mobileclaw.app.runtime.capability.CapabilityRouter
import com.mobileclaw.app.runtime.capability.CallerTrustState
import com.mobileclaw.app.runtime.contribution.ContributionLifecyclePoint
import com.mobileclaw.app.runtime.contribution.ContributionOutcomeRecord
import com.mobileclaw.app.runtime.contribution.ContributionOutcomeState
import com.mobileclaw.app.runtime.contribution.RuntimeContributionRegistry
import com.mobileclaw.app.runtime.ingress.ExternalInvocationRecord
import com.mobileclaw.app.runtime.memory.MemoryWritebackService
import com.mobileclaw.app.runtime.policy.ActionScope
import com.mobileclaw.app.runtime.policy.ApprovalRequest
import com.mobileclaw.app.runtime.policy.ApprovalOutcomeType
import com.mobileclaw.app.runtime.policy.ApprovalRepository
import com.mobileclaw.app.runtime.policy.AuditRepository
import com.mobileclaw.app.runtime.policy.PendingApprovalCoordinator
import com.mobileclaw.app.runtime.policy.PolicyDecisionType
import com.mobileclaw.app.runtime.policy.PolicyEngine
import com.mobileclaw.app.runtime.policy.PolicyRepository
import com.mobileclaw.app.runtime.policy.RiskClassifier
import com.mobileclaw.app.runtime.provider.CapabilityExecutionRequest
import com.mobileclaw.app.runtime.provider.CapabilityProviderRegistry
import com.mobileclaw.app.runtime.capability.ProviderType
import com.mobileclaw.app.runtime.provider.ProviderExecutionEvent
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@Singleton
class RuntimeSessionOrchestrator @Inject constructor(
    private val registry: RuntimeSessionRegistry,
    private val providerRegistry: CapabilityProviderRegistry,
    private val capabilityRouter: CapabilityRouter,
    private val contextLoader: RuntimeContextLoader,
    private val planner: RuntimePlanner,
    private val structuredActionNormalizer: StructuredActionNormalizer,
    private val riskClassifier: RiskClassifier,
    private val policyEngine: PolicyEngine,
    private val policyRepository: PolicyRepository,
    private val approvalRepository: ApprovalRepository,
    private val auditRepository: AuditRepository,
    private val pendingApprovalCoordinator: PendingApprovalCoordinator,
    private val memoryWritebackService: MemoryWritebackService,
    private val appStrings: AppStrings,
) : RuntimeSessionFacade {

    override fun submitRequest(request: RuntimeRequest): Flow<RuntimeSessionEvent> = flow {
        approvalRepository.expirePendingRequestsOlderThan(
            createdBeforeEpochMillis = System.currentTimeMillis() - pendingApprovalCoordinator.approvalTimeoutMillis,
            timeoutReason = appStrings.get(R.string.runtime_policy_approval_timed_out),
        )
        val session = registry.createSession(request.requestId)
        request.sourceMetadata?.takeIf { it.handoffId != null }?.let { sourceMetadata ->
            emit(RuntimeSessionEvent.ExternalHandoffReceived(sourceMetadata))
            val initialRecord = ExternalInvocationRecord(
                handoffId = sourceMetadata.handoffId.orEmpty(),
                runtimeRequestId = request.requestId,
                sessionId = session.sessionId,
                sourceLabel = sourceMetadata.sourceLabel,
                trustState = when (sourceMetadata.trustState) {
                    RuntimeSourceTrustState.TRUSTED -> com.mobileclaw.app.runtime.ingress.ExternalTrustState.TRUSTED
                    RuntimeSourceTrustState.UNVERIFIED -> com.mobileclaw.app.runtime.ingress.ExternalTrustState.UNVERIFIED
                    RuntimeSourceTrustState.DENIED -> com.mobileclaw.app.runtime.ingress.ExternalTrustState.DENIED
                },
                accepted = true,
            )
            emit(RuntimeSessionEvent.ExternalInvocationLinked(initialRecord))
            emit(
                RuntimeSessionEvent.AuditRecorded(
                    auditRepository.record(
                        sessionId = session.sessionId,
                        eventType = com.mobileclaw.app.runtime.policy.AuditEventType.EXTERNAL_HANDOFF,
                        headline = appStrings.get(R.string.external_handoff_audit_received),
                        details = "${sourceMetadata.sourceLabel} · ${sourceMetadata.trustReason}",
                        linkedRecordId = sourceMetadata.handoffId,
                    ),
                ),
            )
        }
        emit(RuntimeSessionEvent.SessionStarted(session))
        emit(RuntimeSessionEvent.StatusSummaryUpdated(session.summary))
        var terminalEmitted = false
        var activeApprovalRequest: ApprovalRequest? = null
        val contributionOutcomes = mutableListOf<ContributionOutcomeRecord>()
        val contextContributions = mutableListOf<com.mobileclaw.app.runtime.contribution.ContextContribution>()
        try {
            val contextPayload = contextLoader.load(request)
            contributionOutcomes += contextPayload.contributionOutcomes
            contextContributions += contextPayload.contextContributions
            emit(
                RuntimeSessionEvent.SystemSourcesPrepared(
                    descriptors = contextPayload.systemSourceDescriptors,
                    contributions = contextPayload.systemSourceContributions,
                ),
            )
            emit(
                RuntimeSessionEvent.ContributionsUpdated(
                    outcomes = contributionOutcomes.toList(),
                    contextContributions = contextContributions.toList(),
                    knowledgeContribution = contextPayload.knowledgeContribution,
                ),
            )
            val contextSession = registry.advanceStage(
                sessionId = session.sessionId,
                stageType = RuntimeStageType.CONTEXT_LOADING,
                details = contextPayload.summary,
            ) ?: return@flow
            emitStage(contextSession, emit = ::emit)

            val planningSession = registry.advanceStage(
                sessionId = session.sessionId,
                stageType = RuntimeStageType.PLANNING,
                details = appStrings.get(R.string.runtime_planning_next_step),
            ) ?: return@flow
            emitStage(planningSession, emit = ::emit)
            val basePlan = planner.plan(request, contextPayload)
            val normalization = structuredActionNormalizer.normalize(
                request = request,
                plan = basePlan,
                contextPayload = contextPayload,
            )
            val plan = basePlan.copy(structuredAction = normalization.takeIf { it.applies })

            val selectingSession = registry.advanceStage(
                sessionId = session.sessionId,
                stageType = RuntimeStageType.CAPABILITY_SELECTION,
                details = normalization.preview?.summary
                    ?: appStrings.get(R.string.runtime_selected_capability, plan.selectedCapabilityId),
            ) ?: return@flow
            emitStage(selectingSession, emit = ::emit)
            if (normalization.applies) {
                emit(
                    RuntimeSessionEvent.StructuredActionPrepared(
                        sessionId = session.sessionId,
                        normalization = normalization,
                    ),
                )
                emit(
                    RuntimeSessionEvent.AuditRecorded(
                        auditRepository.record(
                            sessionId = session.sessionId,
                            eventType = com.mobileclaw.app.runtime.policy.AuditEventType.POLICY_DECIDED,
                            headline = appStrings.get(R.string.structured_audit_prepared),
                            details = buildString {
                                append(normalization.preview?.summary ?: normalization.rationale)
                                normalization.preview?.warnings
                                    ?.takeIf { it.isNotEmpty() }
                                    ?.let { warnings ->
                                        append(" · ")
                                        append(warnings.joinToString(separator = " / "))
                                    }
                            },
                        ),
                    ),
                )
            }

            val routeResult = capabilityRouter.route(
                request = request,
                capabilityId = plan.selectedCapabilityId,
            )
            val selectedToolDescriptor = routeResult.registration?.toolDescriptor
            val selectedToolVisibility = routeResult.visibilitySnapshot
            emit(RuntimeSessionEvent.CallerVerified(routeResult.callerIdentity))
            emit(
                RuntimeSessionEvent.AuditRecorded(
                    auditRepository.record(
                        sessionId = session.sessionId,
                        eventType = com.mobileclaw.app.runtime.policy.AuditEventType.BRIDGE_ROUTED,
                        headline = appStrings.get(R.string.bridge_audit_caller_verified),
                        details = routeResult.callerIdentity.trustReason,
                    ),
                ),
            )
            if (routeResult.callerIdentity.trustState == CallerTrustState.DENIED) {
                emit(
                    RuntimeSessionEvent.AuditRecorded(
                        auditRepository.recordExecutionDenied(
                            sessionId = session.sessionId,
                            details = routeResult.callerIdentity.trustReason,
                        ),
                    ),
                )
                val denied = registry.finishSession(
                    sessionId = session.sessionId,
                    terminalState = RuntimeTerminalState.DENIED,
                    userMessage = routeResult.callerIdentity.trustReason,
                ) ?: return@flow
                terminalEmitted = true
                emit(RuntimeSessionEvent.SessionDenied(denied.outcome!!))
                emit(RuntimeSessionEvent.StatusSummaryUpdated(denied.summary))
                return@flow
            }

            val routedDescriptor = routeResult.descriptor
            if (routedDescriptor == null) {
                routeResult.registration?.let { registration ->
                    emit(
                        RuntimeSessionEvent.CapabilityRouted(
                            sessionId = session.sessionId,
                            capabilityId = plan.selectedCapabilityId,
                            providerId = "",
                            providerType = ProviderType.LOCAL,
                            routeExplanation = routeResult.routeExplanation,
                            toolId = registration.toolDescriptor.toolId,
                            toolDisplayName = registration.toolDescriptor.displayName,
                            toolSideEffectLabel = appStrings.toolSideEffectLabel(
                                registration.toolDescriptor.sideEffectType,
                            ),
                            toolScopeLines = registration.toolDescriptor.requiredScopes.map(appStrings::scopeIdLabel),
                            visibilitySnapshot = routeResult.visibilitySnapshot,
                        ),
                    )
                }
                val reason = routeResult.failureReason ?: appStrings.get(R.string.bridge_no_registration)
                emit(
                    RuntimeSessionEvent.AuditRecorded(
                        auditRepository.record(
                            sessionId = session.sessionId,
                            eventType = com.mobileclaw.app.runtime.policy.AuditEventType.BRIDGE_ROUTED,
                            toolId = selectedToolDescriptor?.toolId,
                            toolDisplayName = selectedToolDescriptor?.displayName,
                            sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                            headline = appStrings.get(R.string.bridge_audit_route_selected),
                            details = reason,
                        ),
                    ),
                )
                val denied = registry.finishSession(
                    sessionId = session.sessionId,
                    terminalState = RuntimeTerminalState.DENIED,
                    userMessage = reason,
                ) ?: return@flow
                terminalEmitted = true
                emit(RuntimeSessionEvent.SessionDenied(denied.outcome!!))
                emit(RuntimeSessionEvent.StatusSummaryUpdated(denied.summary))
                return@flow
            }
            emit(
                RuntimeSessionEvent.CapabilityRouted(
                    sessionId = session.sessionId,
                    capabilityId = plan.selectedCapabilityId,
                    providerId = routedDescriptor.providerId,
                    providerType = routedDescriptor.providerType,
                    routeExplanation = routeResult.routeExplanation,
                    toolId = selectedToolDescriptor?.toolId.orEmpty(),
                    toolDisplayName = selectedToolDescriptor?.displayName.orEmpty(),
                    toolSideEffectLabel = selectedToolDescriptor?.sideEffectType
                        ?.let(appStrings::toolSideEffectLabel)
                        .orEmpty(),
                    toolScopeLines = selectedToolDescriptor?.requiredScopes?.map(appStrings::scopeIdLabel).orEmpty(),
                    visibilitySnapshot = selectedToolVisibility,
                ),
            )
            emit(
                RuntimeSessionEvent.AuditRecorded(
                    auditRepository.record(
                        sessionId = session.sessionId,
                        eventType = com.mobileclaw.app.runtime.policy.AuditEventType.BRIDGE_ROUTED,
                        toolId = selectedToolDescriptor?.toolId,
                        toolDisplayName = selectedToolDescriptor?.displayName,
                        sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                        headline = appStrings.get(R.string.bridge_audit_route_selected),
                        details = routeResult.routeExplanation,
                    ),
                ),
            )

            val assessment = riskClassifier.classify(
                sessionId = session.sessionId,
                request = request,
                plan = plan,
            )
            policyRepository.saveAssessment(assessment)
            emit(RuntimeSessionEvent.RiskAssessed(assessment))
            emit(RuntimeSessionEvent.AuditRecorded(auditRepository.recordRiskAssessed(assessment)))

            val decision = policyEngine.resolve(assessment)
            policyRepository.saveDecision(decision)
            emit(RuntimeSessionEvent.PolicyResolved(decision))
            emit(RuntimeSessionEvent.AuditRecorded(auditRepository.recordPolicyDecided(decision)))
            contributionOutcomes.removeAll { it.contributionId == RuntimeContributionRegistry.POLICY_EXECUTION_GATE_ID }
            contributionOutcomes += ContributionOutcomeRecord(
                contributionId = RuntimeContributionRegistry.POLICY_EXECUTION_GATE_ID,
                requestId = request.requestId,
                lifecyclePoint = ContributionLifecyclePoint.APPROVAL,
                outcomeState = when (decision.decision) {
                    PolicyDecisionType.DENY -> ContributionOutcomeState.BLOCKED
                    PolicyDecisionType.PREVIEW_FIRST,
                    PolicyDecisionType.REQUIRE_CONFIRMATION,
                    -> ContributionOutcomeState.DEGRADED
                    PolicyDecisionType.AUTO_EXECUTE -> ContributionOutcomeState.APPLIED
                },
                summary = decision.rationale,
                details = appStrings.actionScopeLabel(ActionScope.fromScopeId(decision.effectiveScopeId)),
                policyReason = decision.rationale,
                provenanceSummary = appStrings.get(R.string.runtime_contribution_policy_title),
            )
            emit(
                RuntimeSessionEvent.ContributionsUpdated(
                    outcomes = contributionOutcomes.toList(),
                    contextContributions = contextContributions.toList(),
                    knowledgeContribution = contextPayload.knowledgeContribution,
                ),
            )

            val gateSession = registry.advanceStage(
                sessionId = session.sessionId,
                stageType = RuntimeStageType.EXECUTION_GATING,
                details = decision.rationale,
                awaitingInput = decision.awaitingInput,
            ) ?: return@flow
            emitStage(gateSession, emit = ::emit)

            when (decision.decision) {
                PolicyDecisionType.DENY -> {
                    emit(
                        RuntimeSessionEvent.AuditRecorded(
                            auditRepository.recordExecutionDenied(
                                sessionId = session.sessionId,
                                details = decision.rationale,
                                toolId = selectedToolDescriptor?.toolId,
                                toolDisplayName = selectedToolDescriptor?.displayName,
                                sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                            ),
                        ),
                    )
                    val denied = registry.finishSession(
                        sessionId = session.sessionId,
                        terminalState = RuntimeTerminalState.DENIED,
                        userMessage = appStrings.get(R.string.runtime_policy_denied),
                    ) ?: return@flow
                    terminalEmitted = true
                    emit(RuntimeSessionEvent.SessionDenied(denied.outcome!!))
                    emit(RuntimeSessionEvent.StatusSummaryUpdated(denied.summary))
                    return@flow
                }

                PolicyDecisionType.PREVIEW_FIRST,
                PolicyDecisionType.REQUIRE_CONFIRMATION,
                -> {
                    val awaitingSession = registry.advanceStage(
                        sessionId = session.sessionId,
                        stageType = RuntimeStageType.AWAITING_APPROVAL,
                        details = appStrings.get(R.string.runtime_policy_waiting_approval),
                        awaitingInput = true,
                    ) ?: return@flow
                    emitStage(awaitingSession, emit = ::emit)

                    val approvalRequest = approvalRepository.createApprovalRequest(
                        sessionId = session.sessionId,
                        decision = decision,
                        request = request,
                        contextPayload = contextPayload,
                        scope = ActionScope.fromScopeId(decision.effectiveScopeId),
                        toolDescriptor = selectedToolDescriptor ?: routeResult.registration!!.toolDescriptor,
                        visibilitySnapshot = selectedToolVisibility,
                        normalization = normalization.takeIf { it.applies },
                    )
                    activeApprovalRequest = approvalRequest
                    emit(RuntimeSessionEvent.ApprovalRequested(approvalRequest))
                    emit(
                        RuntimeSessionEvent.AuditRecorded(
                            auditRepository.recordApprovalRequested(approvalRequest),
                        ),
                    )
                    pendingApprovalCoordinator.register(approvalRequest.approvalRequestId)

                    val pendingResult = pendingApprovalCoordinator.awaitOutcome(approvalRequest.approvalRequestId)
                    val outcomeType = pendingResult.outcome
                    val resolutionDetails = when {
                        pendingResult.timedOut -> appStrings.get(R.string.runtime_policy_approval_timed_out)
                        outcomeType == ApprovalOutcomeType.APPROVED -> {
                            appStrings.get(R.string.runtime_policy_approval_resumed)
                        }
                        outcomeType == ApprovalOutcomeType.REJECTED -> {
                            appStrings.get(R.string.runtime_policy_approval_rejected)
                        }
                        else -> appStrings.get(R.string.runtime_policy_approval_abandoned)
                    }
                    val approvalOutcome = approvalRepository.recordOutcome(
                        approvalRequestId = approvalRequest.approvalRequestId,
                        sessionId = session.sessionId,
                        outcome = outcomeType,
                        reason = resolutionDetails,
                        actor = if (pendingResult.timedOut) "system_timeout" else "local_device_user",
                    )
                    activeApprovalRequest = null
                    emit(RuntimeSessionEvent.ApprovalResolved(approvalOutcome))
                    emit(
                        RuntimeSessionEvent.AuditRecorded(
                            auditRepository.recordApprovalResolved(
                                sessionId = session.sessionId,
                                requestId = approvalRequest.approvalRequestId,
                                outcome = outcomeType,
                                details = resolutionDetails,
                            ),
                        ),
                    )

                    when (outcomeType) {
                        ApprovalOutcomeType.APPROVED -> {
                            val resumedSession = registry.advanceStage(
                                sessionId = session.sessionId,
                                stageType = RuntimeStageType.EXECUTION_GATING,
                                details = resolutionDetails,
                            ) ?: return@flow
                            emitStage(resumedSession, emit = ::emit)
                        }

                        ApprovalOutcomeType.REJECTED -> {
                            emit(
                                RuntimeSessionEvent.AuditRecorded(
                                    auditRepository.recordExecutionDenied(
                                        sessionId = session.sessionId,
                                        details = resolutionDetails,
                                        toolId = selectedToolDescriptor?.toolId,
                                        toolDisplayName = selectedToolDescriptor?.displayName,
                                        sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                                    ),
                                ),
                            )
                            val denied = registry.finishSession(
                                sessionId = session.sessionId,
                                terminalState = RuntimeTerminalState.DENIED,
                                userMessage = resolutionDetails,
                            ) ?: return@flow
                            terminalEmitted = true
                            emit(RuntimeSessionEvent.SessionDenied(denied.outcome!!))
                            emit(RuntimeSessionEvent.StatusSummaryUpdated(denied.summary))
                            return@flow
                        }

                        ApprovalOutcomeType.ABANDONED -> {
                            emit(
                                RuntimeSessionEvent.AuditRecorded(
                                    auditRepository.recordExecutionDenied(
                                        sessionId = session.sessionId,
                                        details = resolutionDetails,
                                        toolId = selectedToolDescriptor?.toolId,
                                        toolDisplayName = selectedToolDescriptor?.displayName,
                                        sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                                    ),
                                ),
                            )
                            val cancelled = registry.finishSession(
                                sessionId = session.sessionId,
                                terminalState = RuntimeTerminalState.CANCELLED,
                                userMessage = resolutionDetails,
                            ) ?: return@flow
                            terminalEmitted = true
                            emit(RuntimeSessionEvent.SessionCancelled(cancelled.outcome!!))
                            emit(RuntimeSessionEvent.StatusSummaryUpdated(cancelled.summary))
                            return@flow
                        }
                    }
                }

                PolicyDecisionType.AUTO_EXECUTE -> Unit
            }

            val provider = providerRegistry.getProvider(routedDescriptor)
            if (provider == null) {
                emit(
                    RuntimeSessionEvent.AuditRecorded(
                        auditRepository.recordExecutionFailed(
                            sessionId = session.sessionId,
                            details = routeResult.routeExplanation,
                            toolId = selectedToolDescriptor?.toolId,
                            toolDisplayName = selectedToolDescriptor?.displayName,
                            sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                        ),
                    ),
                )
                val denied = registry.finishSession(
                    sessionId = session.sessionId,
                    terminalState = RuntimeTerminalState.DENIED,
                    userMessage = routeResult.routeExplanation,
                ) ?: return@flow
                terminalEmitted = true
                emit(RuntimeSessionEvent.SessionDenied(denied.outcome!!))
                emit(RuntimeSessionEvent.StatusSummaryUpdated(denied.summary))
                return@flow
            }
            emit(
                RuntimeSessionEvent.CapabilityRequested(
                    sessionId = session.sessionId,
                    capabilityId = plan.selectedCapabilityId,
                    providerId = routedDescriptor.providerId,
                ),
            )

            val executingSession = registry.advanceStage(
                sessionId = session.sessionId,
                stageType = RuntimeStageType.EXECUTING,
                details = routeResult.routeExplanation,
            ) ?: return@flow
            emitStage(executingSession, emit = ::emit)

            provider.execute(
                CapabilityExecutionRequest(
                    sessionId = session.sessionId,
                    request = request,
                    contextPayload = contextPayload,
                    plan = plan,
                    providerDescriptor = routedDescriptor,
                ),
            ).collect { providerEvent ->
                if (terminalEmitted) return@collect
                when (providerEvent) {
                    is ProviderExecutionEvent.ExecutionStarted -> {
                        emit(
                            RuntimeSessionEvent.CapabilityStarted(
                                sessionId = session.sessionId,
                                capabilityId = providerEvent.capabilityId,
                                providerId = routedDescriptor.providerId,
                            ),
                        )
                    }

                    is ProviderExecutionEvent.OutputChunk -> {
                        emit(
                            RuntimeSessionEvent.CapabilityOutputChunk(
                                sessionId = session.sessionId,
                                capabilityId = providerEvent.capabilityId,
                                providerId = routedDescriptor.providerId,
                                chunk = providerEvent.chunk,
                            ),
                        )
                    }

                    is ProviderExecutionEvent.ExecutionCompleted -> {
                        emit(
                            RuntimeSessionEvent.CapabilityCompleted(
                                sessionId = session.sessionId,
                                capabilityId = providerEvent.capabilityId,
                                providerId = routedDescriptor.providerId,
                                outputText = providerEvent.outputText,
                            ),
                        )
                        val completed = registry.finishSession(
                            sessionId = session.sessionId,
                            terminalState = RuntimeTerminalState.SUCCESS,
                            userMessage = appStrings.get(R.string.runtime_request_completed),
                            outputText = providerEvent.outputText,
                            providerResults = listOf(routedDescriptor.providerId),
                        ) ?: return@collect
                        memoryWritebackService.writeSuccessfulReplyMemory(
                            request = request,
                            outputText = providerEvent.outputText,
                        )
                        emit(
                            RuntimeSessionEvent.AuditRecorded(
                                auditRepository.recordExecutionCompleted(
                                    sessionId = session.sessionId,
                                    details = appStrings.get(R.string.runtime_request_completed),
                                    toolId = selectedToolDescriptor?.toolId,
                                    toolDisplayName = selectedToolDescriptor?.displayName,
                                    sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                                ),
                            ),
                        )
                        terminalEmitted = true
                        emit(RuntimeSessionEvent.SessionCompleted(completed.outcome!!))
                        emit(RuntimeSessionEvent.StatusSummaryUpdated(completed.summary))
                    }

                    is ProviderExecutionEvent.ExecutionFailed -> {
                        emit(
                            RuntimeSessionEvent.CapabilityFailed(
                                sessionId = session.sessionId,
                                capabilityId = providerEvent.capabilityId,
                                providerId = routedDescriptor.providerId,
                                userMessage = providerEvent.userMessage,
                            ),
                        )
                        val failed = registry.finishSession(
                            sessionId = session.sessionId,
                            terminalState = RuntimeTerminalState.FAILURE,
                            userMessage = providerEvent.userMessage,
                        ) ?: return@collect
                        emit(
                            RuntimeSessionEvent.AuditRecorded(
                                auditRepository.recordExecutionFailed(
                                    sessionId = session.sessionId,
                                    details = providerEvent.userMessage,
                                    toolId = selectedToolDescriptor?.toolId,
                                    toolDisplayName = selectedToolDescriptor?.displayName,
                                    sideEffectLabel = selectedToolDescriptor?.sideEffectType?.let(appStrings::toolSideEffectLabel),
                                ),
                            ),
                        )
                        terminalEmitted = true
                        emit(RuntimeSessionEvent.SessionFailed(failed.outcome!!))
                        emit(RuntimeSessionEvent.StatusSummaryUpdated(failed.summary))
                    }
                }
            }
        } catch (cancellationException: CancellationException) {
            activeApprovalRequest?.let { pendingApproval ->
                withContext(NonCancellable) {
                    val resolutionDetails = appStrings.get(R.string.runtime_policy_approval_abandoned)
                    approvalRepository.recordOutcome(
                        approvalRequestId = pendingApproval.approvalRequestId,
                        sessionId = pendingApproval.sessionId,
                        outcome = ApprovalOutcomeType.ABANDONED,
                        reason = resolutionDetails,
                        actor = "runtime_session",
                    )
                    auditRepository.recordApprovalResolved(
                        sessionId = pendingApproval.sessionId,
                        requestId = pendingApproval.approvalRequestId,
                        outcome = ApprovalOutcomeType.ABANDONED,
                        details = resolutionDetails,
                    )
                }
                activeApprovalRequest = null
            }
            if (!terminalEmitted && currentCoroutineContext().isActive.not()) {
                val cancelled = registry.finishSession(
                    sessionId = session.sessionId,
                    terminalState = RuntimeTerminalState.CANCELLED,
                    userMessage = appStrings.get(R.string.runtime_session_cancelled_before_completion),
                )
                if (cancelled?.outcome != null) {
                    terminalEmitted = true
                    emit(RuntimeSessionEvent.SessionCancelled(cancelled.outcome))
                    emit(RuntimeSessionEvent.StatusSummaryUpdated(cancelled.summary))
                }
            }
            throw cancellationException
        }
    }

    override suspend fun resetModelSession(modelId: String) =
        providerRegistry.getProvider("local_generation")?.resetModelSession(modelId)

    override suspend fun resolveApprovalRequest(
        approvalRequestId: String,
        outcome: ApprovalOutcomeType,
    ): Boolean {
        return pendingApprovalCoordinator.resolve(
            approvalRequestId = approvalRequestId,
            outcome = outcome,
        )
    }

    private suspend fun emitStage(
        session: ExecutionSession,
        emit: suspend (RuntimeSessionEvent) -> Unit,
    ) {
        emit(
            RuntimeSessionEvent.StageChanged(
                stage = RuntimeStage(
                    sessionId = session.sessionId,
                    stageType = session.currentStage,
                    label = session.summary.stageLabel,
                    details = session.summary.supportingText,
                    ordinal = session.stageOrdinal,
                ),
            ),
        )
        emit(RuntimeSessionEvent.StatusSummaryUpdated(session.summary))
    }
}
