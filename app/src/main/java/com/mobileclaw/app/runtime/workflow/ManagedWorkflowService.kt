package com.mobileclaw.app.runtime.workflow

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.contribution.RuntimeContributionAvailabilityState
import com.mobileclaw.app.runtime.contribution.RuntimeContributionRegistry
import com.mobileclaw.app.runtime.knowledge.ManagedKnowledgeService
import com.mobileclaw.app.runtime.policy.ApprovalOutcomeType
import com.mobileclaw.app.runtime.policy.ApprovalRepository
import com.mobileclaw.app.runtime.policy.AuditRepository
import com.mobileclaw.app.runtime.policy.PendingApprovalCoordinator
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private data class WorkflowAvailabilityEvaluation(
    val state: WorkflowAvailabilityState,
    val reason: String,
)

private data class WorkflowObservationInput(
    val definitions: List<WorkflowDefinitionEntity>,
    val steps: List<WorkflowStepEntity>,
    val triggers: List<WorkflowTriggerEntity>,
    val runs: List<WorkflowRunEntity>,
    val checkpoints: List<WorkflowCheckpointEntity>,
    val includedKnowledgeCount: Int,
)

@Singleton
class ManagedWorkflowService @Inject constructor(
    private val workflowDao: WorkflowDao,
    private val approvalRepository: ApprovalRepository,
    private val pendingApprovalCoordinator: PendingApprovalCoordinator,
    private val auditRepository: AuditRepository,
    private val managedKnowledgeService: ManagedKnowledgeService,
    private val runtimeContributionRegistry: RuntimeContributionRegistry,
    private val appStrings: AppStrings,
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val runJobs = linkedMapOf<String, Job>()
    private val runMutex = Mutex()
    private val activeApprovalRequest = MutableStateFlow<com.mobileclaw.app.runtime.policy.ApprovalRequest?>(null)

    init {
        serviceScope.launch {
            recoverInterruptedRuns()
        }
    }

    fun observeAutomationCenter(): Flow<WorkflowAutomationSnapshot> {
        val baseWorkflowData = combine(
            workflowDao.observeDefinitions(),
            workflowDao.observeSteps(),
            workflowDao.observeTriggers(),
            workflowDao.observeRuns(),
            workflowDao.observeCheckpoints(),
        ) { definitions, steps, triggers, runs, checkpoints ->
            WorkflowObservationInput(
                definitions = definitions,
                steps = steps,
                triggers = triggers,
                runs = runs,
                checkpoints = checkpoints,
                includedKnowledgeCount = 0,
            )
        }
        val workflowData = combine(
            baseWorkflowData,
            managedKnowledgeService.observeCorpus(),
        ) { input, knowledgeCorpus ->
            input.copy(includedKnowledgeCount = knowledgeCorpus.includedAssetCount)
        }
        return combine(workflowData, activeApprovalRequest) { input, approvalRequest ->
            val stepsByDefinition = input.steps.groupBy { it.workflowDefinitionId }
            val triggersByDefinition = input.triggers.groupBy { it.workflowDefinitionId }
            val checkpointsByRun = input.checkpoints.groupBy { it.workflowRunId }
            val latestRunByDefinition = input.runs
                .groupBy { it.workflowDefinitionId }
                .mapValues { (_, groupedRuns) -> groupedRuns.maxByOrNull { it.updatedAtEpochMillis } }

            val reconciledDefinitions = input.definitions.map { definition ->
                val evaluation = evaluateAvailability(
                    definition = definition,
                    steps = stepsByDefinition[definition.workflowDefinitionId].orEmpty(),
                    triggers = triggersByDefinition[definition.workflowDefinitionId].orEmpty(),
                    includedKnowledgeCount = input.includedKnowledgeCount,
                )
                val latestRun = latestRunByDefinition[definition.workflowDefinitionId]
                val updated = definition.copy(
                    availabilityState = evaluation.state,
                    availabilityReason = evaluation.reason,
                    stepCount = stepsByDefinition[definition.workflowDefinitionId].orEmpty().size,
                    lastRunSummary = latestRun?.outcomeHeadline
                        ?.takeIf { it.isNotBlank() }
                        ?: latestRun?.lastCheckpointSummary.orEmpty(),
                    lastRunState = latestRun?.runState,
                    lastRunAtEpochMillis = latestRun?.updatedAtEpochMillis,
                )
                if (updated != definition) {
                    serviceScope.launch {
                        workflowDao.upsertDefinition(updated)
                    }
                }
                updated
            }

            val managedDefinitions = reconciledDefinitions.map { definition ->
                toManagedDefinition(
                    definition = definition,
                    steps = stepsByDefinition[definition.workflowDefinitionId].orEmpty(),
                    triggers = triggersByDefinition[definition.workflowDefinitionId].orEmpty(),
                )
            }.sortedWith(
                compareByDescending<ManagedWorkflowDefinition> { it.updatedAtEpochMillis }
                    .thenBy { it.title.lowercase() },
            )

            val managedRuns = input.runs.map { run ->
                toManagedRun(
                    run = run,
                    checkpoint = checkpointsByRun[run.workflowRunId]
                        ?.maxByOrNull { it.updatedAtEpochMillis },
                )
            }.sortedByDescending { it.updatedAtEpochMillis }

            val activeRun = managedRuns.firstOrNull {
                it.runState in setOf(
                    WorkflowRunState.RUNNING,
                    WorkflowRunState.AWAITING_APPROVAL,
                    WorkflowRunState.PAUSED,
                    WorkflowRunState.RESUMABLE,
                )
            }

            val activeRunCount = managedRuns.count {
                it.runState in setOf(
                    WorkflowRunState.RUNNING,
                    WorkflowRunState.AWAITING_APPROVAL,
                    WorkflowRunState.PAUSED,
                    WorkflowRunState.RESUMABLE,
                )
            }

            WorkflowAutomationSnapshot(
                summaryHeadline = if (managedDefinitions.isEmpty()) {
                    appStrings.get(R.string.workflow_center_empty)
                } else {
                    appStrings.get(
                        R.string.workflow_center_summary_headline,
                        managedDefinitions.size,
                        activeRunCount,
                    )
                },
                summarySupportingText = activeRun?.let { run ->
                    listOf(
                        run.title,
                        appStrings.workflowRunStateLabel(run.runState),
                        run.nextRequiredAction.takeIf { it.isNotBlank() },
                    ).filterNotNull().joinToString(separator = " · ")
                } ?: appStrings.get(R.string.workflow_center_summary_supporting),
                definitions = managedDefinitions,
                runs = managedRuns,
                activeRun = activeRun,
                templateOptions = templateOptions(),
                activeApprovalRequest = approvalRequest,
            )
        }
    }

    suspend fun createWorkflowFromTemplate(templateId: String): Result<ManagedWorkflowDefinition> = runCatching {
        val template = templateOptions().firstOrNull { it.templateId == templateId }
            ?: error(appStrings.get(R.string.workflow_feedback_template_missing))
        val now = System.currentTimeMillis()
        val definitionId = "workflow-definition-$now-$templateId"
        val definition = WorkflowDefinitionEntity(
            workflowDefinitionId = definitionId,
            templateId = template.templateId,
            title = template.title,
            entrySummary = template.entrySummary,
            isEnabled = true,
            availabilityState = WorkflowAvailabilityState.READY,
            availabilityReason = appStrings.get(R.string.workflow_availability_ready_reason),
            stepCount = template.steps.size,
            lastRunSummary = "",
            lastRunState = null,
            lastRunAtEpochMillis = null,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )
        workflowDao.upsertDefinition(definition)
        workflowDao.upsertSteps(
            template.steps.mapIndexed { index, step ->
                WorkflowStepEntity(
                    workflowStepId = "workflow-step-$definitionId-$index",
                    workflowDefinitionId = definitionId,
                    ordinal = index,
                    stepType = step.stepType,
                    title = step.title,
                    summary = step.summary,
                    requiredInputs = step.requiredInputs,
                    nextTransitionRule = step.nextTransitionRule,
                    actionPayload = step.actionPayload,
                )
            },
        )
        workflowDao.upsertTriggers(
            listOf(
                WorkflowTriggerEntity(
                    workflowTriggerId = "workflow-trigger-$definitionId-manual",
                    workflowDefinitionId = definitionId,
                    triggerType = template.triggerType,
                    triggerSummary = template.triggerSummary,
                    isEnabled = true,
                ),
            ),
        )
        buildManagedDefinition(definitionId)
    }

    suspend fun setWorkflowEnabled(
        workflowDefinitionId: String,
        enabled: Boolean,
    ): Result<ManagedWorkflowDefinition> = runCatching {
        val definition = workflowDao.getDefinitionById(workflowDefinitionId)
            ?: error(appStrings.get(R.string.workflow_feedback_definition_missing))
        val updated = definition.copy(
            isEnabled = enabled,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
        workflowDao.upsertDefinition(updated)
        buildManagedDefinition(workflowDefinitionId)
    }

    suspend fun startWorkflowRun(
        workflowDefinitionId: String,
    ): Result<ManagedWorkflowRun> = runCatching {
        val definition = workflowDao.getDefinitionById(workflowDefinitionId)
            ?: error(appStrings.get(R.string.workflow_feedback_definition_missing))
        val steps = workflowDao.getStepsForDefinition(workflowDefinitionId)
        val triggers = workflowDao.getTriggersForDefinition(workflowDefinitionId)
        val knowledgeCorpus = managedKnowledgeService.observeCorpus().first()
        val availability = evaluateAvailability(
            definition = definition,
            steps = steps,
            triggers = triggers,
            includedKnowledgeCount = knowledgeCorpus.includedAssetCount,
        )
        if (availability.state == WorkflowAvailabilityState.DISABLED ||
            availability.state == WorkflowAvailabilityState.BLOCKED
        ) {
            error(availability.reason)
        }
        val existingRun = workflowDao.getRunsByStates(
            listOf(
                WorkflowRunState.RUNNING,
                WorkflowRunState.AWAITING_APPROVAL,
                WorkflowRunState.PAUSED,
                WorkflowRunState.RESUMABLE,
            ),
        ).firstOrNull { it.workflowDefinitionId == workflowDefinitionId }
        require(existingRun == null) {
            appStrings.get(R.string.workflow_feedback_run_already_active)
        }
        val now = System.currentTimeMillis()
        val firstStep = steps.firstOrNull()
            ?: error(appStrings.get(R.string.workflow_availability_blocked_steps))
        val runId = "workflow-run-$now-$workflowDefinitionId"
        val run = WorkflowRunEntity(
            workflowRunId = runId,
            workflowDefinitionId = workflowDefinitionId,
            title = definition.title,
            runState = WorkflowRunState.RUNNING,
            startedAtEpochMillis = now,
            updatedAtEpochMillis = now,
            completedAtEpochMillis = null,
            lastCheckpointSummary = firstStep.title,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_prepare),
            outcomeHeadline = "",
            outcomeDetails = "",
            recoveryGuidance = "",
            recentActivityLines = listOf(appStrings.get(R.string.workflow_run_activity_started, definition.title)),
            provenanceLines = buildProvenanceLines(steps, triggers),
            activeApprovalRequestId = null,
        )
        val checkpoint = WorkflowCheckpointEntity(
            workflowCheckpointId = "workflow-checkpoint-$runId-${firstStep.ordinal}",
            workflowRunId = runId,
            workflowDefinitionId = workflowDefinitionId,
            stepId = firstStep.workflowStepId,
            stepOrdinal = firstStep.ordinal,
            checkpointState = WorkflowCheckpointState.READY,
            resumeSummary = appStrings.get(R.string.workflow_run_resume_from_step, firstStep.title),
            blockingReason = "",
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )
        workflowDao.upsertRun(run)
        workflowDao.upsertCheckpoint(checkpoint)
        syncDefinitionLastRun(
            workflowDefinitionId = workflowDefinitionId,
            run = run,
        )
        launchRunExecution(
            workflowRunId = runId,
            startStepOrdinal = firstStep.ordinal,
        )
        buildManagedRun(runId)
    }

    suspend fun pauseWorkflowRun(workflowRunId: String): Result<ManagedWorkflowRun> = runCatching {
        cancelRunJob(workflowRunId)
        val run = workflowDao.getRunById(workflowRunId)
            ?: error(appStrings.get(R.string.workflow_feedback_run_missing))
        if (run.activeApprovalRequestId != null) {
            pendingApprovalCoordinator.resolve(
                approvalRequestId = run.activeApprovalRequestId,
                outcome = ApprovalOutcomeType.ABANDONED,
            )
        }
        val now = System.currentTimeMillis()
        val checkpoint = workflowDao.getLatestCheckpointForRun(workflowRunId)
        val updatedRun = run.copy(
            runState = WorkflowRunState.PAUSED,
            updatedAtEpochMillis = now,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_resume),
            recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_paused),
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_paused)).takeLast(6),
            activeApprovalRequestId = null,
        )
        workflowDao.upsertRun(updatedRun)
        checkpoint?.let {
            workflowDao.upsertCheckpoint(
                it.copy(
                    checkpointState = WorkflowCheckpointState.PAUSED,
                    resumeSummary = appStrings.get(R.string.workflow_run_resume_from_step_ordinal, it.stepOrdinal + 1),
                    updatedAtEpochMillis = now,
                    blockingReason = appStrings.get(R.string.workflow_run_recovery_paused),
                ),
            )
        }
        syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
        buildManagedRun(workflowRunId)
    }

    suspend fun resumeWorkflowRun(workflowRunId: String): Result<ManagedWorkflowRun> = runCatching {
        val run = workflowDao.getRunById(workflowRunId)
            ?: error(appStrings.get(R.string.workflow_feedback_run_missing))
        require(run.runState in setOf(WorkflowRunState.PAUSED, WorkflowRunState.RESUMABLE)) {
            appStrings.get(R.string.workflow_feedback_run_not_resumable)
        }
        val checkpoint = workflowDao.getLatestCheckpointForRun(workflowRunId)
            ?: error(appStrings.get(R.string.workflow_feedback_checkpoint_missing))
        val now = System.currentTimeMillis()
        val updatedRun = run.copy(
            runState = WorkflowRunState.RUNNING,
            updatedAtEpochMillis = now,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_continue),
            recoveryGuidance = "",
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_resumed)).takeLast(6),
        )
        workflowDao.upsertRun(updatedRun)
        workflowDao.upsertCheckpoint(
            checkpoint.copy(
                checkpointState = WorkflowCheckpointState.READY,
                updatedAtEpochMillis = now,
                blockingReason = "",
            ),
        )
        syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
        launchRunExecution(
            workflowRunId = workflowRunId,
            startStepOrdinal = checkpoint.stepOrdinal,
        )
        buildManagedRun(workflowRunId)
    }

    suspend fun cancelWorkflowRun(workflowRunId: String): Result<ManagedWorkflowRun> = runCatching {
        cancelRunJob(workflowRunId)
        val run = workflowDao.getRunById(workflowRunId)
            ?: error(appStrings.get(R.string.workflow_feedback_run_missing))
        if (run.activeApprovalRequestId != null) {
            pendingApprovalCoordinator.resolve(
                approvalRequestId = run.activeApprovalRequestId,
                outcome = ApprovalOutcomeType.ABANDONED,
            )
        }
        val now = System.currentTimeMillis()
        val checkpoint = workflowDao.getLatestCheckpointForRun(workflowRunId)
        val updatedRun = run.copy(
            runState = WorkflowRunState.CANCELLED,
            updatedAtEpochMillis = now,
            completedAtEpochMillis = now,
            nextRequiredAction = "",
            outcomeHeadline = appStrings.get(R.string.workflow_run_cancelled_headline),
            outcomeDetails = appStrings.get(R.string.workflow_run_cancelled_details),
            recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_restart),
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_cancelled)).takeLast(6),
            activeApprovalRequestId = null,
        )
        workflowDao.upsertRun(updatedRun)
        checkpoint?.let {
            workflowDao.upsertCheckpoint(
                it.copy(
                    checkpointState = WorkflowCheckpointState.CANCELLED,
                    updatedAtEpochMillis = now,
                    blockingReason = appStrings.get(R.string.workflow_run_cancelled_details),
                ),
            )
        }
        syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
        auditRepository.recordExecutionFailed(
            sessionId = workflowRunId,
            details = updatedRun.outcomeDetails,
            toolId = "workflow",
            toolDisplayName = run.title,
            sideEffectLabel = appStrings.get(R.string.workflow_side_effect_label),
        )
        buildManagedRun(workflowRunId)
    }

    suspend fun resolveApprovalRequest(
        approvalRequestId: String,
        outcome: ApprovalOutcomeType,
    ): Boolean {
        return pendingApprovalCoordinator.resolve(
            approvalRequestId = approvalRequestId,
            outcome = outcome,
        )
    }

    private fun launchRunExecution(
        workflowRunId: String,
        startStepOrdinal: Int,
    ) {
        serviceScope.launch {
            runMutex.withLock {
                runJobs.remove(workflowRunId)?.cancel()
                runJobs[workflowRunId] = serviceScope.launch {
                    executeRun(
                        workflowRunId = workflowRunId,
                        startStepOrdinal = startStepOrdinal,
                    )
                }
            }
        }
    }

    private suspend fun executeRun(
        workflowRunId: String,
        startStepOrdinal: Int,
    ) {
        try {
            val run = workflowDao.getRunById(workflowRunId) ?: return
            val definition = workflowDao.getDefinitionById(run.workflowDefinitionId) ?: return
            val steps = workflowDao.getStepsForDefinition(definition.workflowDefinitionId)
            for (step in steps.filter { it.ordinal >= startStepOrdinal }) {
                val currentRun = workflowDao.getRunById(workflowRunId) ?: return
                if (currentRun.runState in setOf(
                        WorkflowRunState.PAUSED,
                        WorkflowRunState.CANCELLED,
                        WorkflowRunState.COMPLETED,
                        WorkflowRunState.FAILED,
                    )
                ) {
                    return
                }
                when (step.stepType) {
                    WorkflowStepType.CONTEXT_CONTRIBUTION -> handleContextStep(currentRun, step)
                    WorkflowStepType.GUARD -> {
                        val shouldContinue = handleGuardStep(currentRun, definition, step)
                        if (!shouldContinue) return
                    }
                    WorkflowStepType.APPROVAL_GATE -> {
                        val shouldContinue = handleApprovalStep(currentRun, step)
                        if (!shouldContinue) return
                    }
                    WorkflowStepType.ACTION -> handleActionStep(currentRun, step)
                }
            }
            completeRun(workflowRunId)
        } catch (_: CancellationException) {
            return
        } catch (throwable: Throwable) {
            failRun(workflowRunId, throwable.message ?: appStrings.get(R.string.workflow_run_failed_details))
        } finally {
            runMutex.withLock {
                runJobs.remove(workflowRunId)
            }
        }
    }

    private suspend fun handleContextStep(
        run: WorkflowRunEntity,
        step: WorkflowStepEntity,
    ) {
        val now = System.currentTimeMillis()
        workflowDao.upsertCheckpoint(
            WorkflowCheckpointEntity(
                workflowCheckpointId = "workflow-checkpoint-${run.workflowRunId}-${step.ordinal}",
                workflowRunId = run.workflowRunId,
                workflowDefinitionId = run.workflowDefinitionId,
                stepId = step.workflowStepId,
                stepOrdinal = step.ordinal,
                checkpointState = WorkflowCheckpointState.RUNNING,
                resumeSummary = appStrings.get(R.string.workflow_run_resume_from_step, step.title),
                blockingReason = "",
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
        val updatedRun = run.copy(
            updatedAtEpochMillis = now,
            lastCheckpointSummary = step.title,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_guard),
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_context, step.title)).takeLast(6),
        )
        workflowDao.upsertRun(updatedRun)
        syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
    }

    private suspend fun handleGuardStep(
        run: WorkflowRunEntity,
        definition: WorkflowDefinitionEntity,
        step: WorkflowStepEntity,
    ): Boolean {
        val now = System.currentTimeMillis()
        val knowledgeCorpus = managedKnowledgeService.observeCorpus().first()
        val knowledgeAvailable = knowledgeCorpus.includedAssetCount > 0 &&
            runtimeContributionRegistry.availabilityState(
                RuntimeContributionRegistry.KNOWLEDGE_RETRIEVAL_ID,
            ) != RuntimeContributionAvailabilityState.DISABLED
        if (step.requiredInputs.contains("knowledge") && !knowledgeAvailable) {
            val blockedRun = run.copy(
                runState = WorkflowRunState.RESUMABLE,
                updatedAtEpochMillis = now,
                lastCheckpointSummary = step.title,
                nextRequiredAction = appStrings.get(R.string.workflow_run_next_restore_knowledge),
                outcomeHeadline = appStrings.get(R.string.workflow_run_blocked_headline),
                outcomeDetails = appStrings.get(R.string.workflow_run_blocked_knowledge),
                recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_restore_knowledge),
                recentActivityLines = (run.recentActivityLines +
                    appStrings.get(R.string.workflow_run_activity_guard_blocked)).takeLast(6),
            )
            workflowDao.upsertRun(blockedRun)
            workflowDao.upsertCheckpoint(
                WorkflowCheckpointEntity(
                    workflowCheckpointId = "workflow-checkpoint-${run.workflowRunId}-${step.ordinal}",
                    workflowRunId = run.workflowRunId,
                    workflowDefinitionId = run.workflowDefinitionId,
                    stepId = step.workflowStepId,
                    stepOrdinal = step.ordinal,
                    checkpointState = WorkflowCheckpointState.BLOCKED,
                    resumeSummary = appStrings.get(R.string.workflow_run_resume_from_step, step.title),
                    blockingReason = appStrings.get(R.string.workflow_run_blocked_knowledge),
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                ),
            )
            syncDefinitionLastRun(definition.workflowDefinitionId, blockedRun)
            auditRepository.recordExecutionFailed(
                sessionId = run.workflowRunId,
                details = blockedRun.outcomeDetails,
                toolId = "workflow",
                toolDisplayName = definition.title,
                sideEffectLabel = appStrings.get(R.string.workflow_side_effect_label),
            )
            return false
        }
        val updatedRun = run.copy(
            updatedAtEpochMillis = now,
            lastCheckpointSummary = step.title,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_approval),
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_guard_passed)).takeLast(6),
        )
        workflowDao.upsertRun(updatedRun)
        workflowDao.upsertCheckpoint(
            WorkflowCheckpointEntity(
                workflowCheckpointId = "workflow-checkpoint-${run.workflowRunId}-${step.ordinal}",
                workflowRunId = run.workflowRunId,
                workflowDefinitionId = run.workflowDefinitionId,
                stepId = step.workflowStepId,
                stepOrdinal = step.ordinal,
                checkpointState = WorkflowCheckpointState.RUNNING,
                resumeSummary = appStrings.get(R.string.workflow_run_resume_from_step, step.title),
                blockingReason = "",
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
        syncDefinitionLastRun(definition.workflowDefinitionId, updatedRun)
        return true
    }

    private suspend fun handleApprovalStep(
        run: WorkflowRunEntity,
        step: WorkflowStepEntity,
    ): Boolean {
        val now = System.currentTimeMillis()
        val approvalRequest = approvalRepository.createWorkflowApprovalRequest(
            sessionId = run.workflowRunId,
            toolId = "workflow-approval-${step.workflowStepId}",
            toolDisplayName = step.title,
            sideEffectLabel = appStrings.get(R.string.workflow_side_effect_label),
            scopeLines = listOf(appStrings.get(R.string.workflow_scope_line_local_only)),
            previewLines = listOf(step.summary, step.nextTransitionRule).filter { it.isNotBlank() },
            title = appStrings.get(R.string.approval_title, step.title),
            summary = appStrings.get(R.string.workflow_approval_summary, run.title, step.title),
            previewPayload = appStrings.get(R.string.workflow_approval_preview, run.title, step.summary),
        )
        activeApprovalRequest.value = approvalRequest
        val waitingRun = run.copy(
            runState = WorkflowRunState.AWAITING_APPROVAL,
            updatedAtEpochMillis = now,
            lastCheckpointSummary = step.title,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_approval),
            outcomeHeadline = "",
            outcomeDetails = "",
            recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_waiting_approval),
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_approval_requested)).takeLast(6),
            activeApprovalRequestId = approvalRequest.approvalRequestId,
        )
        workflowDao.upsertRun(waitingRun)
        workflowDao.upsertCheckpoint(
            WorkflowCheckpointEntity(
                workflowCheckpointId = "workflow-checkpoint-${run.workflowRunId}-${step.ordinal}",
                workflowRunId = run.workflowRunId,
                workflowDefinitionId = run.workflowDefinitionId,
                stepId = step.workflowStepId,
                stepOrdinal = step.ordinal,
                checkpointState = WorkflowCheckpointState.WAITING_APPROVAL,
                resumeSummary = appStrings.get(R.string.workflow_run_resume_from_step, step.title),
                blockingReason = appStrings.get(R.string.workflow_run_recovery_waiting_approval),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
        syncDefinitionLastRun(run.workflowDefinitionId, waitingRun)
        auditRepository.recordApprovalRequested(approvalRequest)
        pendingApprovalCoordinator.register(approvalRequest.approvalRequestId)
        val pendingResult = pendingApprovalCoordinator.awaitOutcome(approvalRequest.approvalRequestId)
        activeApprovalRequest.value = null
        val resumedRun = workflowDao.getRunById(run.workflowRunId) ?: return false
        val approvalOutcome = approvalRepository.recordOutcome(
            approvalRequestId = approvalRequest.approvalRequestId,
            sessionId = approvalRequest.sessionId,
            outcome = pendingResult.outcome,
            reason = when (pendingResult.outcome) {
                ApprovalOutcomeType.APPROVED -> appStrings.get(R.string.workflow_approval_outcome_approved)
                ApprovalOutcomeType.REJECTED -> appStrings.get(R.string.workflow_approval_outcome_rejected)
                ApprovalOutcomeType.ABANDONED -> if (pendingResult.timedOut) {
                    appStrings.get(R.string.workflow_approval_outcome_timed_out)
                } else {
                    appStrings.get(R.string.workflow_approval_outcome_abandoned)
                }
            },
        )
        auditRepository.recordApprovalResolved(
            sessionId = run.workflowRunId,
            requestId = approvalRequest.approvalRequestId,
            outcome = approvalOutcome.outcome,
            details = approvalOutcome.reason.orEmpty(),
        )
        return when (pendingResult.outcome) {
            ApprovalOutcomeType.APPROVED -> {
                val continuedRun = resumedRun.copy(
                    runState = WorkflowRunState.RUNNING,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                    nextRequiredAction = appStrings.get(R.string.workflow_run_next_action),
                    recoveryGuidance = "",
                    recentActivityLines = (resumedRun.recentActivityLines +
                        appStrings.get(R.string.workflow_run_activity_approval_granted)).takeLast(6),
                    activeApprovalRequestId = null,
                )
                workflowDao.upsertRun(continuedRun)
                syncDefinitionLastRun(resumedRun.workflowDefinitionId, continuedRun)
                true
            }

            ApprovalOutcomeType.REJECTED -> {
                val rejectedRun = resumedRun.copy(
                    runState = WorkflowRunState.CANCELLED,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                    completedAtEpochMillis = System.currentTimeMillis(),
                    nextRequiredAction = "",
                    outcomeHeadline = appStrings.get(R.string.workflow_run_cancelled_headline),
                    outcomeDetails = appStrings.get(R.string.workflow_approval_outcome_rejected),
                    recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_restart),
                    recentActivityLines = (resumedRun.recentActivityLines +
                        appStrings.get(R.string.workflow_run_activity_approval_rejected)).takeLast(6),
                    activeApprovalRequestId = null,
                )
                workflowDao.upsertRun(rejectedRun)
                syncDefinitionLastRun(resumedRun.workflowDefinitionId, rejectedRun)
                false
            }

            ApprovalOutcomeType.ABANDONED -> {
                val pausedRun = resumedRun.copy(
                    runState = WorkflowRunState.RESUMABLE,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                    nextRequiredAction = appStrings.get(R.string.workflow_run_next_resume),
                    outcomeHeadline = appStrings.get(R.string.workflow_run_paused_headline),
                    outcomeDetails = approvalOutcome.reason.orEmpty(),
                    recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_resume_after_approval),
                    recentActivityLines = (resumedRun.recentActivityLines +
                        appStrings.get(R.string.workflow_run_activity_approval_abandoned)).takeLast(6),
                    activeApprovalRequestId = null,
                )
                workflowDao.upsertRun(pausedRun)
                syncDefinitionLastRun(resumedRun.workflowDefinitionId, pausedRun)
                false
            }
        }
    }

    private suspend fun handleActionStep(
        run: WorkflowRunEntity,
        step: WorkflowStepEntity,
    ) {
        val now = System.currentTimeMillis()
        val activityLine = when (step.actionPayload) {
            "prepare_brief" -> appStrings.get(R.string.workflow_run_activity_action_brief)
            "draft_follow_up" -> appStrings.get(R.string.workflow_run_activity_action_follow_up)
            else -> appStrings.get(R.string.workflow_run_activity_action_generic, step.title)
        }
        val updatedRun = run.copy(
            updatedAtEpochMillis = now,
            lastCheckpointSummary = step.title,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_wrap_up),
            recentActivityLines = (run.recentActivityLines + activityLine).takeLast(6),
        )
        workflowDao.upsertRun(updatedRun)
        workflowDao.upsertCheckpoint(
            WorkflowCheckpointEntity(
                workflowCheckpointId = "workflow-checkpoint-${run.workflowRunId}-${step.ordinal}",
                workflowRunId = run.workflowRunId,
                workflowDefinitionId = run.workflowDefinitionId,
                stepId = step.workflowStepId,
                stepOrdinal = step.ordinal,
                checkpointState = WorkflowCheckpointState.COMPLETED,
                resumeSummary = "",
                blockingReason = "",
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
        syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
    }

    private suspend fun completeRun(workflowRunId: String) {
        val run = workflowDao.getRunById(workflowRunId) ?: return
        val now = System.currentTimeMillis()
        val updatedRun = run.copy(
            runState = WorkflowRunState.COMPLETED,
            updatedAtEpochMillis = now,
            completedAtEpochMillis = now,
            nextRequiredAction = "",
            outcomeHeadline = appStrings.get(R.string.workflow_run_completed_headline),
            outcomeDetails = appStrings.get(R.string.workflow_run_completed_details, run.title),
            recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_rerun),
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_completed)).takeLast(6),
            activeApprovalRequestId = null,
        )
        workflowDao.upsertRun(updatedRun)
        syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
        auditRepository.recordExecutionCompleted(
            sessionId = workflowRunId,
            details = updatedRun.outcomeDetails,
            toolId = "workflow",
            toolDisplayName = updatedRun.title,
            sideEffectLabel = appStrings.get(R.string.workflow_side_effect_label),
        )
    }

    private suspend fun failRun(
        workflowRunId: String,
        details: String,
    ) {
        val run = workflowDao.getRunById(workflowRunId) ?: return
        val now = System.currentTimeMillis()
        val updatedRun = run.copy(
            runState = WorkflowRunState.FAILED,
            updatedAtEpochMillis = now,
            completedAtEpochMillis = now,
            nextRequiredAction = appStrings.get(R.string.workflow_run_next_review_failure),
            outcomeHeadline = appStrings.get(R.string.workflow_run_failed_headline),
            outcomeDetails = details,
            recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_review_failure),
            recentActivityLines = (run.recentActivityLines +
                appStrings.get(R.string.workflow_run_activity_failed)).takeLast(6),
            activeApprovalRequestId = null,
        )
        workflowDao.upsertRun(updatedRun)
        syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
        auditRepository.recordExecutionFailed(
            sessionId = workflowRunId,
            details = details,
            toolId = "workflow",
            toolDisplayName = updatedRun.title,
            sideEffectLabel = appStrings.get(R.string.workflow_side_effect_label),
        )
    }

    private suspend fun syncDefinitionLastRun(
        workflowDefinitionId: String,
        run: WorkflowRunEntity,
    ) {
        val definition = workflowDao.getDefinitionById(workflowDefinitionId) ?: return
        workflowDao.upsertDefinition(
            definition.copy(
                lastRunSummary = run.outcomeHeadline.takeIf { it.isNotBlank() } ?: run.lastCheckpointSummary,
                lastRunState = run.runState,
                lastRunAtEpochMillis = run.updatedAtEpochMillis,
                updatedAtEpochMillis = maxOf(definition.updatedAtEpochMillis, run.updatedAtEpochMillis),
            ),
        )
    }

    private suspend fun cancelRunJob(workflowRunId: String) {
        runMutex.withLock {
            runJobs.remove(workflowRunId)?.cancel()
        }
    }

    private suspend fun recoverInterruptedRuns() {
        val interruptedRuns = workflowDao.getRunsByStates(
            listOf(
                WorkflowRunState.RUNNING,
                WorkflowRunState.AWAITING_APPROVAL,
            ),
        )
        interruptedRuns.forEach { run ->
            val now = System.currentTimeMillis()
            val updatedRun = run.copy(
                runState = WorkflowRunState.RESUMABLE,
                updatedAtEpochMillis = now,
                nextRequiredAction = appStrings.get(R.string.workflow_run_next_resume),
                recoveryGuidance = appStrings.get(R.string.workflow_run_recovery_interrupted),
                recentActivityLines = (run.recentActivityLines +
                    appStrings.get(R.string.workflow_run_activity_interrupted)).takeLast(6),
                activeApprovalRequestId = null,
            )
            workflowDao.upsertRun(updatedRun)
            workflowDao.getLatestCheckpointForRun(run.workflowRunId)?.let { checkpoint ->
                workflowDao.upsertCheckpoint(
                    checkpoint.copy(
                        checkpointState = WorkflowCheckpointState.PAUSED,
                        updatedAtEpochMillis = now,
                        blockingReason = appStrings.get(R.string.workflow_run_recovery_interrupted),
                        resumeSummary = checkpoint.resumeSummary.ifBlank {
                            appStrings.get(R.string.workflow_run_resume_from_step_ordinal, checkpoint.stepOrdinal + 1)
                        },
                    ),
                )
            }
            syncDefinitionLastRun(updatedRun.workflowDefinitionId, updatedRun)
        }
    }

    private suspend fun buildManagedDefinition(
        workflowDefinitionId: String,
    ): ManagedWorkflowDefinition {
        val definition = workflowDao.getDefinitionById(workflowDefinitionId)
            ?: error(appStrings.get(R.string.workflow_feedback_definition_missing))
        val steps = workflowDao.getStepsForDefinition(workflowDefinitionId)
        val triggers = workflowDao.getTriggersForDefinition(workflowDefinitionId)
        val knowledgeCorpus = managedKnowledgeService.observeCorpus().first()
        val evaluation = evaluateAvailability(
            definition = definition,
            steps = steps,
            triggers = triggers,
            includedKnowledgeCount = knowledgeCorpus.includedAssetCount,
        )
        return toManagedDefinition(
            definition = definition.copy(
                availabilityState = evaluation.state,
                availabilityReason = evaluation.reason,
                stepCount = steps.size,
            ),
            steps = steps,
            triggers = triggers,
        )
    }

    private suspend fun buildManagedRun(
        workflowRunId: String,
    ): ManagedWorkflowRun {
        val run = workflowDao.getRunById(workflowRunId)
            ?: error(appStrings.get(R.string.workflow_feedback_run_missing))
        return toManagedRun(
            run = run,
            checkpoint = workflowDao.getLatestCheckpointForRun(workflowRunId),
        )
    }

    private fun evaluateAvailability(
        definition: WorkflowDefinitionEntity,
        steps: List<WorkflowStepEntity>,
        triggers: List<WorkflowTriggerEntity>,
        includedKnowledgeCount: Int,
    ): WorkflowAvailabilityEvaluation {
        if (!definition.isEnabled) {
            return WorkflowAvailabilityEvaluation(
                state = WorkflowAvailabilityState.DISABLED,
                reason = appStrings.get(R.string.workflow_availability_disabled_reason),
            )
        }
        if (steps.isEmpty()) {
            return WorkflowAvailabilityEvaluation(
                state = WorkflowAvailabilityState.BLOCKED,
                reason = appStrings.get(R.string.workflow_availability_blocked_steps),
            )
        }
        if (triggers.none { it.isEnabled }) {
            return WorkflowAvailabilityEvaluation(
                state = WorkflowAvailabilityState.BLOCKED,
                reason = appStrings.get(R.string.workflow_availability_blocked_triggers),
            )
        }
        val requiresKnowledge = steps.any { it.requiredInputs.contains("knowledge") }
        val knowledgeContributionEnabled = runtimeContributionRegistry.availabilityState(
            RuntimeContributionRegistry.KNOWLEDGE_RETRIEVAL_ID,
        ) != RuntimeContributionAvailabilityState.DISABLED
        if (requiresKnowledge && (!knowledgeContributionEnabled || includedKnowledgeCount == 0)) {
            return WorkflowAvailabilityEvaluation(
                state = WorkflowAvailabilityState.DEGRADED,
                reason = appStrings.get(R.string.workflow_availability_degraded_knowledge),
            )
        }
        return WorkflowAvailabilityEvaluation(
            state = WorkflowAvailabilityState.READY,
            reason = appStrings.get(R.string.workflow_availability_ready_reason),
        )
    }

    private fun toManagedDefinition(
        definition: WorkflowDefinitionEntity,
        steps: List<WorkflowStepEntity>,
        triggers: List<WorkflowTriggerEntity>,
    ): ManagedWorkflowDefinition {
        return ManagedWorkflowDefinition(
            workflowDefinitionId = definition.workflowDefinitionId,
            title = definition.title,
            templateId = definition.templateId,
            entrySummary = definition.entrySummary,
            availabilityState = definition.availabilityState,
            availabilityReason = definition.availabilityReason,
            isEnabled = definition.isEnabled,
            stepCount = definition.stepCount,
            triggerSummary = triggers.firstOrNull { it.isEnabled }?.triggerSummary
                ?: triggers.firstOrNull()?.triggerSummary.orEmpty(),
            steps = steps.map { step ->
                ManagedWorkflowStep(
                    workflowStepId = step.workflowStepId,
                    ordinal = step.ordinal,
                    stepType = step.stepType,
                    title = step.title,
                    summary = step.summary,
                    requiredInputs = step.requiredInputs,
                    nextTransitionRule = step.nextTransitionRule,
                )
            },
            triggers = triggers.map { trigger ->
                ManagedWorkflowTrigger(
                    workflowTriggerId = trigger.workflowTriggerId,
                    triggerType = trigger.triggerType,
                    triggerSummary = trigger.triggerSummary,
                    isEnabled = trigger.isEnabled,
                )
            },
            lastRunSummary = definition.lastRunSummary,
            lastRunState = definition.lastRunState,
            lastRunAtEpochMillis = definition.lastRunAtEpochMillis,
            createdAtEpochMillis = definition.createdAtEpochMillis,
            updatedAtEpochMillis = definition.updatedAtEpochMillis,
        )
    }

    private fun toManagedRun(
        run: WorkflowRunEntity,
        checkpoint: WorkflowCheckpointEntity?,
    ): ManagedWorkflowRun {
        return ManagedWorkflowRun(
            workflowRunId = run.workflowRunId,
            workflowDefinitionId = run.workflowDefinitionId,
            title = run.title,
            runState = run.runState,
            startedAtEpochMillis = run.startedAtEpochMillis,
            updatedAtEpochMillis = run.updatedAtEpochMillis,
            completedAtEpochMillis = run.completedAtEpochMillis,
            lastCheckpointSummary = run.lastCheckpointSummary,
            nextRequiredAction = run.nextRequiredAction,
            outcomeHeadline = run.outcomeHeadline,
            outcomeDetails = run.outcomeDetails,
            recoveryGuidance = run.recoveryGuidance,
            recentActivityLines = run.recentActivityLines,
            provenanceLines = run.provenanceLines,
            checkpoint = checkpoint?.let {
                ManagedWorkflowCheckpoint(
                    workflowCheckpointId = it.workflowCheckpointId,
                    workflowRunId = it.workflowRunId,
                    stepId = it.stepId,
                    stepOrdinal = it.stepOrdinal,
                    checkpointState = it.checkpointState,
                    resumeSummary = it.resumeSummary,
                    blockingReason = it.blockingReason,
                    updatedAtEpochMillis = it.updatedAtEpochMillis,
                )
            },
            activeApprovalRequestId = run.activeApprovalRequestId,
        )
    }

    private fun buildProvenanceLines(
        steps: List<WorkflowStepEntity>,
        triggers: List<WorkflowTriggerEntity>,
    ): List<String> {
        val lines = mutableListOf<String>()
        triggers.firstOrNull()?.let { trigger ->
            lines += appStrings.get(
                R.string.workflow_provenance_trigger,
                appStrings.workflowTriggerTypeLabel(trigger.triggerType),
            )
        }
        if (steps.any { it.stepType == WorkflowStepType.CONTEXT_CONTRIBUTION }) {
            lines += appStrings.get(R.string.workflow_provenance_context)
        }
        if (steps.any { it.stepType == WorkflowStepType.APPROVAL_GATE }) {
            lines += appStrings.get(R.string.workflow_provenance_approval)
        }
        lines += appStrings.get(R.string.workflow_provenance_local)
        return lines.distinct()
    }

    private fun templateOptions(): List<WorkflowTemplateOption> {
        return listOf(
            WorkflowTemplateOption(
                templateId = "knowledge_briefing",
                title = appStrings.get(R.string.workflow_template_title_knowledge_briefing),
                summary = appStrings.get(R.string.workflow_template_summary_knowledge_briefing),
                detailLines = listOf(
                    appStrings.get(R.string.workflow_template_detail_context_memory),
                    appStrings.get(R.string.workflow_template_detail_guard_knowledge),
                    appStrings.get(R.string.workflow_template_detail_approval_gate),
                    appStrings.get(R.string.workflow_template_detail_action_brief),
                ),
                entrySummary = appStrings.get(R.string.workflow_template_entry_manual),
                triggerType = WorkflowTriggerType.MANUAL,
                triggerSummary = appStrings.get(R.string.workflow_template_trigger_manual),
                steps = listOf(
                    WorkflowTemplateStep(
                        stepType = WorkflowStepType.CONTEXT_CONTRIBUTION,
                        title = appStrings.get(R.string.workflow_template_step_context),
                        summary = appStrings.get(R.string.workflow_template_step_context_summary),
                        requiredInputs = listOf("memory", "knowledge"),
                        nextTransitionRule = appStrings.get(R.string.workflow_transition_after_context),
                    ),
                    WorkflowTemplateStep(
                        stepType = WorkflowStepType.GUARD,
                        title = appStrings.get(R.string.workflow_template_step_guard),
                        summary = appStrings.get(R.string.workflow_template_step_guard_summary),
                        requiredInputs = listOf("knowledge"),
                        nextTransitionRule = appStrings.get(R.string.workflow_transition_after_guard),
                    ),
                    WorkflowTemplateStep(
                        stepType = WorkflowStepType.APPROVAL_GATE,
                        title = appStrings.get(R.string.workflow_template_step_approval),
                        summary = appStrings.get(R.string.workflow_template_step_approval_summary),
                        requiredInputs = listOf("approval"),
                        nextTransitionRule = appStrings.get(R.string.workflow_transition_after_approval),
                    ),
                    WorkflowTemplateStep(
                        stepType = WorkflowStepType.ACTION,
                        title = appStrings.get(R.string.workflow_template_step_action_brief),
                        summary = appStrings.get(R.string.workflow_template_step_action_brief_summary),
                        requiredInputs = listOf("local_action"),
                        nextTransitionRule = appStrings.get(R.string.workflow_transition_after_action),
                        actionPayload = "prepare_brief",
                    ),
                ),
            ),
            WorkflowTemplateOption(
                templateId = "follow_up_loop",
                title = appStrings.get(R.string.workflow_template_title_follow_up_loop),
                summary = appStrings.get(R.string.workflow_template_summary_follow_up_loop),
                detailLines = listOf(
                    appStrings.get(R.string.workflow_template_detail_context_memory),
                    appStrings.get(R.string.workflow_template_detail_approval_gate),
                    appStrings.get(R.string.workflow_template_detail_action_follow_up),
                ),
                entrySummary = appStrings.get(R.string.workflow_template_entry_manual),
                triggerType = WorkflowTriggerType.MANUAL,
                triggerSummary = appStrings.get(R.string.workflow_template_trigger_manual),
                steps = listOf(
                    WorkflowTemplateStep(
                        stepType = WorkflowStepType.CONTEXT_CONTRIBUTION,
                        title = appStrings.get(R.string.workflow_template_step_context),
                        summary = appStrings.get(R.string.workflow_template_step_context_summary),
                        requiredInputs = listOf("memory"),
                        nextTransitionRule = appStrings.get(R.string.workflow_transition_after_context),
                    ),
                    WorkflowTemplateStep(
                        stepType = WorkflowStepType.APPROVAL_GATE,
                        title = appStrings.get(R.string.workflow_template_step_approval),
                        summary = appStrings.get(R.string.workflow_template_step_approval_follow_up_summary),
                        requiredInputs = listOf("approval"),
                        nextTransitionRule = appStrings.get(R.string.workflow_transition_after_approval),
                    ),
                    WorkflowTemplateStep(
                        stepType = WorkflowStepType.ACTION,
                        title = appStrings.get(R.string.workflow_template_step_action_follow_up),
                        summary = appStrings.get(R.string.workflow_template_step_action_follow_up_summary),
                        requiredInputs = listOf("local_action"),
                        nextTransitionRule = appStrings.get(R.string.workflow_transition_after_action),
                        actionPayload = "draft_follow_up",
                    ),
                ),
            ),
        )
    }
}
