package com.mobileclaw.app.runtime.workflow

import com.mobileclaw.app.runtime.policy.ApprovalRequest

data class WorkflowTemplateOption(
    val templateId: String,
    val title: String,
    val summary: String,
    val detailLines: List<String>,
    val entrySummary: String,
    val triggerType: WorkflowTriggerType,
    val triggerSummary: String,
    val steps: List<WorkflowTemplateStep>,
)

data class WorkflowTemplateStep(
    val stepType: WorkflowStepType,
    val title: String,
    val summary: String,
    val requiredInputs: List<String> = emptyList(),
    val nextTransitionRule: String = "",
    val actionPayload: String = "",
)

data class ManagedWorkflowDefinition(
    val workflowDefinitionId: String,
    val title: String,
    val templateId: String,
    val entrySummary: String,
    val availabilityState: WorkflowAvailabilityState,
    val availabilityReason: String,
    val isEnabled: Boolean,
    val stepCount: Int,
    val triggerSummary: String,
    val steps: List<ManagedWorkflowStep>,
    val triggers: List<ManagedWorkflowTrigger>,
    val lastRunSummary: String,
    val lastRunState: WorkflowRunState? = null,
    val lastRunAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

data class ManagedWorkflowStep(
    val workflowStepId: String,
    val ordinal: Int,
    val stepType: WorkflowStepType,
    val title: String,
    val summary: String,
    val requiredInputs: List<String>,
    val nextTransitionRule: String,
)

data class ManagedWorkflowTrigger(
    val workflowTriggerId: String,
    val triggerType: WorkflowTriggerType,
    val triggerSummary: String,
    val isEnabled: Boolean,
)

data class ManagedWorkflowCheckpoint(
    val workflowCheckpointId: String,
    val workflowRunId: String,
    val stepId: String,
    val stepOrdinal: Int,
    val checkpointState: WorkflowCheckpointState,
    val resumeSummary: String,
    val blockingReason: String,
    val updatedAtEpochMillis: Long,
)

data class ManagedWorkflowRun(
    val workflowRunId: String,
    val workflowDefinitionId: String,
    val title: String,
    val runState: WorkflowRunState,
    val startedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val lastCheckpointSummary: String,
    val nextRequiredAction: String,
    val outcomeHeadline: String,
    val outcomeDetails: String,
    val recoveryGuidance: String,
    val recentActivityLines: List<String>,
    val provenanceLines: List<String>,
    val checkpoint: ManagedWorkflowCheckpoint? = null,
    val activeApprovalRequestId: String? = null,
)

data class WorkflowAutomationSnapshot(
    val summaryHeadline: String,
    val summarySupportingText: String,
    val definitions: List<ManagedWorkflowDefinition>,
    val runs: List<ManagedWorkflowRun>,
    val activeRun: ManagedWorkflowRun? = null,
    val templateOptions: List<WorkflowTemplateOption> = emptyList(),
    val activeApprovalRequest: ApprovalRequest? = null,
)
