package com.mobileclaw.app.ui.agentworkspace.model

import com.mobileclaw.app.runtime.workflow.WorkflowAvailabilityState
import com.mobileclaw.app.runtime.workflow.WorkflowCheckpointState
import com.mobileclaw.app.runtime.workflow.WorkflowRunState
import com.mobileclaw.app.runtime.workflow.WorkflowStepType

data class AutomationAreaUiModel(
    val title: String = "",
    val headline: String = "",
    val supportingText: String = "",
    val templateOptions: List<WorkflowTemplateUiModel> = emptyList(),
    val definitions: List<WorkflowDefinitionUiModel> = emptyList(),
    val runs: List<WorkflowRunUiModel> = emptyList(),
    val activeRunBanner: WorkflowRunBannerUiModel? = null,
)

data class WorkflowTemplateUiModel(
    val templateId: String,
    val title: String,
    val summary: String,
    val detailLines: List<String> = emptyList(),
)

data class WorkflowDefinitionUiModel(
    val workflowDefinitionId: String,
    val title: String,
    val entrySummary: String,
    val availabilityState: WorkflowAvailabilityState,
    val availabilityLabel: String,
    val availabilityReason: String,
    val statusLine: String,
    val lastRunSummary: String,
    val isEnabled: Boolean,
    val hasActiveRun: Boolean,
    val canStart: Boolean,
    val actionHint: String,
    val stepCountLabel: String,
    val steps: List<WorkflowStepUiModel> = emptyList(),
    val triggerSummary: String = "",
    val detailLines: List<String> = emptyList(),
)

data class WorkflowStepUiModel(
    val workflowStepId: String,
    val ordinalLabel: String,
    val title: String,
    val summary: String,
    val stepType: WorkflowStepType,
    val stepTypeLabel: String,
    val requiredInputsLine: String = "",
    val transitionLine: String = "",
)

data class WorkflowRunUiModel(
    val workflowRunId: String,
    val workflowDefinitionId: String,
    val title: String,
    val runState: WorkflowRunState,
    val runStateLabel: String,
    val statusLine: String,
    val lastCheckpointSummary: String,
    val nextRequiredAction: String,
    val outcomeHeadline: String,
    val outcomeDetails: String,
    val recoveryGuidance: String,
    val recentActivityLines: List<String> = emptyList(),
    val provenanceLines: List<String> = emptyList(),
    val checkpointState: WorkflowCheckpointState? = null,
    val checkpointLabel: String = "",
    val primaryActionLabel: String = "",
    val secondaryActionLabel: String = "",
    val tertiaryActionLabel: String = "",
)

data class WorkflowRunBannerUiModel(
    val workflowRunId: String,
    val title: String,
    val statusLine: String,
    val nextRequiredAction: String,
    val primaryActionLabel: String,
    val secondaryActionLabel: String,
)
