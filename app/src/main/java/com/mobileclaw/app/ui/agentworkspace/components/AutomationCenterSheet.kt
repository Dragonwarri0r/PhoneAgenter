package com.mobileclaw.app.ui.agentworkspace.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.workflow.WorkflowAvailabilityState
import com.mobileclaw.app.runtime.workflow.WorkflowRunState
import com.mobileclaw.app.ui.agentworkspace.model.AutomationAreaUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkflowDefinitionUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkflowRunUiModel
import com.mobileclaw.app.ui.agentworkspace.model.WorkflowTemplateUiModel

private val attentionRunStates = setOf(
    WorkflowRunState.RUNNING,
    WorkflowRunState.PAUSED,
    WorkflowRunState.RESUMABLE,
    WorkflowRunState.AWAITING_APPROVAL,
    WorkflowRunState.FAILED,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationCenterSheet(
    automationArea: AutomationAreaUiModel,
    selectedDefinition: WorkflowDefinitionUiModel?,
    selectedRun: WorkflowRunUiModel?,
    onDismiss: () -> Unit,
    onCreateTemplate: (templateId: String) -> Unit,
    onOpenDefinition: (workflowDefinitionId: String) -> Unit,
    onOpenRun: (workflowRunId: String) -> Unit,
    onCloseDetail: () -> Unit,
    onStartWorkflow: (workflowDefinitionId: String) -> Unit,
    onToggleWorkflowEnabled: (workflowDefinitionId: String, enabled: Boolean) -> Unit,
    onResumeRun: (workflowRunId: String) -> Unit,
    onPauseRun: (workflowRunId: String) -> Unit,
    onCancelRun: (workflowRunId: String) -> Unit,
) {
    val workflowPauseLabel = stringResource(R.string.workflow_action_pause)
    val workflowResumeLabel = stringResource(R.string.workflow_action_resume)
    val totalAttentionRunCount = automationArea.runs.count { it.runState in attentionRunStates }
    val attentionRuns = automationArea.runs.filter {
        it.runState in attentionRunStates &&
            it.workflowRunId != automationArea.activeRunBanner?.workflowRunId
    }
    val recentRuns = automationArea.runs.filterNot { it.runState in attentionRunStates }
    val readyDefinitions = automationArea.definitions.filter {
        !it.hasActiveRun && it.availabilityState == WorkflowAvailabilityState.READY
    }
    val reviewDefinitions = automationArea.definitions.filterNot {
        !it.hasActiveRun && it.availabilityState == WorkflowAvailabilityState.READY
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when {
                selectedDefinition != null -> {
                    AutomationDefinitionDetail(
                        definition = selectedDefinition,
                        onBack = onCloseDetail,
                        onStart = { onStartWorkflow(selectedDefinition.workflowDefinitionId) },
                        onToggleEnabled = {
                            onToggleWorkflowEnabled(
                                selectedDefinition.workflowDefinitionId,
                                !selectedDefinition.isEnabled,
                            )
                        },
                    )
                }

                selectedRun != null -> {
                    AutomationRunDetail(
                        run = selectedRun,
                        onBack = onCloseDetail,
                        onResume = { onResumeRun(selectedRun.workflowRunId) },
                        onPause = { onPauseRun(selectedRun.workflowRunId) },
                        onCancel = { onCancelRun(selectedRun.workflowRunId) },
                    )
                }

                else -> {
                    Text(
                        text = automationArea.title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (automationArea.headline.isNotBlank()) {
                        Text(
                            text = automationArea.headline,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    if (automationArea.supportingText.isNotBlank()) {
                        Text(
                            text = automationArea.supportingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    AutomationOverviewCard(
                        workflowCount = automationArea.definitions.size,
                        runnableCount = readyDefinitions.size,
                        attentionRunCount = totalAttentionRunCount,
                        reviewDefinitionCount = reviewDefinitions.size,
                    )

                    automationArea.activeRunBanner?.let { banner ->
                        WorkflowRunBanner(
                            banner = banner,
                            onPrimaryAction = {
                                when (banner.primaryActionLabel) {
                                    workflowPauseLabel -> onPauseRun(banner.workflowRunId)
                                    workflowResumeLabel -> onResumeRun(banner.workflowRunId)
                                    else -> onOpenRun(banner.workflowRunId)
                                }
                            },
                            onSecondaryAction = {
                                onOpenRun(banner.workflowRunId)
                            },
                        )
                    }

                    if (attentionRuns.isNotEmpty()) {
                        SectionHeader(
                            title = stringResource(R.string.workflow_runs_attention_title),
                            count = attentionRuns.size,
                        )
                        attentionRuns.forEach { run ->
                            RunCard(
                                run = run,
                                onOpen = { onOpenRun(run.workflowRunId) },
                                onResume = { onResumeRun(run.workflowRunId) },
                                onPause = { onPauseRun(run.workflowRunId) },
                                onCancel = { onCancelRun(run.workflowRunId) },
                            )
                        }
                    }

                    if (readyDefinitions.isNotEmpty()) {
                        SectionHeader(
                            title = stringResource(R.string.workflow_ready_title),
                            count = readyDefinitions.size,
                        )
                        readyDefinitions.forEach { definition ->
                            DefinitionCard(
                                definition = definition,
                                onOpen = { onOpenDefinition(definition.workflowDefinitionId) },
                                onStart = { onStartWorkflow(definition.workflowDefinitionId) },
                                onToggleEnabled = {
                                    onToggleWorkflowEnabled(
                                        definition.workflowDefinitionId,
                                        !definition.isEnabled,
                                    )
                                },
                            )
                        }
                    }

                    if (reviewDefinitions.isNotEmpty()) {
                        SectionHeader(
                            title = stringResource(R.string.workflow_review_title),
                            count = reviewDefinitions.size,
                        )
                        reviewDefinitions.forEach { definition ->
                            DefinitionCard(
                                definition = definition,
                                onOpen = { onOpenDefinition(definition.workflowDefinitionId) },
                                onStart = { onStartWorkflow(definition.workflowDefinitionId) },
                                onToggleEnabled = {
                                    onToggleWorkflowEnabled(
                                        definition.workflowDefinitionId,
                                        !definition.isEnabled,
                                    )
                                },
                            )
                        }
                    }

                    if (automationArea.definitions.isEmpty()) {
                        Text(
                            text = stringResource(R.string.workflow_center_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (automationArea.templateOptions.isNotEmpty()) {
                        SectionHeader(
                            title = stringResource(R.string.workflow_templates_title),
                            count = automationArea.templateOptions.size,
                        )
                        automationArea.templateOptions.forEach { option ->
                            TemplateCard(
                                option = option,
                                onCreate = { onCreateTemplate(option.templateId) },
                            )
                        }
                    }

                    SectionHeader(
                        title = stringResource(R.string.workflow_recent_outcomes_title),
                        count = recentRuns.size,
                    )
                    when {
                        recentRuns.isNotEmpty() -> {
                            recentRuns.forEach { run ->
                                RunCard(
                                    run = run,
                                    onOpen = { onOpenRun(run.workflowRunId) },
                                    onResume = { onResumeRun(run.workflowRunId) },
                                    onPause = { onPauseRun(run.workflowRunId) },
                                    onCancel = { onCancelRun(run.workflowRunId) },
                                )
                            }
                        }

                        automationArea.runs.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.workflow_runs_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutomationOverviewCard(
    workflowCount: Int,
    runnableCount: Int,
    attentionRunCount: Int,
    reviewDefinitionCount: Int,
) {
    CenterCard(title = stringResource(R.string.workflow_overview_title)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OverviewMetricCell(
                    label = stringResource(R.string.workflow_overview_workflows),
                    value = workflowCount,
                    modifier = Modifier.weight(1f),
                )
                OverviewMetricCell(
                    label = stringResource(R.string.workflow_overview_runnable),
                    value = runnableCount,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OverviewMetricCell(
                    label = stringResource(R.string.workflow_overview_runs_to_review),
                    value = attentionRunCount,
                    modifier = Modifier.weight(1f),
                )
                OverviewMetricCell(
                    label = stringResource(R.string.workflow_overview_definitions_to_review),
                    value = reviewDefinitionCount,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun OverviewMetricCell(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TemplateCard(
    option: WorkflowTemplateUiModel,
    onCreate: () -> Unit,
) {
    CenterCard(title = option.title, supporting = option.summary, lines = option.detailLines) {
        TextButton(onClick = onCreate) {
            Text(stringResource(R.string.workflow_action_create))
        }
    }
}

@Composable
private fun DefinitionCard(
    definition: WorkflowDefinitionUiModel,
    onOpen: () -> Unit,
    onStart: () -> Unit,
    onToggleEnabled: () -> Unit,
) {
    val primaryActionLabel = when {
        !definition.isEnabled -> stringResource(R.string.workflow_action_enable)
        definition.canStart -> stringResource(R.string.workflow_action_start)
        else -> ""
    }
    CenterCard(
        title = definition.title,
        supporting = definition.entrySummary,
        statusLine = definition.statusLine,
        accented = definition.hasActiveRun || definition.availabilityState != WorkflowAvailabilityState.READY,
        lines = buildList {
            add(definition.stepCountLabel)
            definition.triggerSummary.takeIf { it.isNotBlank() }?.let {
                add(stringResource(R.string.workflow_detail_trigger, it))
            }
            definition.lastRunSummary.takeIf { it.isNotBlank() }?.let {
                add(stringResource(R.string.workflow_detail_last_run, it))
            }
            if (definition.actionHint.isNotBlank() &&
                (definition.hasActiveRun || definition.availabilityState != WorkflowAvailabilityState.READY)
            ) {
                add(definition.actionHint)
            }
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (primaryActionLabel.isNotBlank()) {
                TextButton(
                    onClick = {
                        if (!definition.isEnabled) {
                            onToggleEnabled()
                        } else {
                            onStart()
                        }
                    },
                ) {
                    Text(primaryActionLabel)
                }
            }
            TextButton(onClick = onOpen) {
                Text(stringResource(R.string.common_manage))
            }
        }
    }
}

@Composable
private fun RunCard(
    run: WorkflowRunUiModel,
    onOpen: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onCancel: () -> Unit,
) {
    val workflowPauseLabel = stringResource(R.string.workflow_action_pause)
    CenterCard(
        title = run.title,
        supporting = run.lastCheckpointSummary,
        statusLine = run.statusLine,
        accented = run.runState in attentionRunStates,
        lines = buildList {
            run.nextRequiredAction.takeIf { it.isNotBlank() }?.let {
                add(stringResource(R.string.workflow_detail_next_action, it))
            }
            run.outcomeHeadline.takeIf { it.isNotBlank() }?.let {
                add(stringResource(R.string.workflow_detail_outcome, it))
            }
            run.outcomeDetails.takeIf { it.isNotBlank() }?.let(::add)
            run.recoveryGuidance.takeIf { it.isNotBlank() }?.let {
                add(stringResource(R.string.workflow_detail_recovery, it))
            }
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (run.primaryActionLabel.isNotBlank()) {
                TextButton(
                    onClick = {
                        if (run.primaryActionLabel == workflowPauseLabel) {
                            onPause()
                        } else {
                            onResume()
                        }
                    },
                ) {
                    Text(run.primaryActionLabel)
                }
            }
            if (run.secondaryActionLabel.isNotBlank()) {
                TextButton(onClick = onCancel) {
                    Text(run.secondaryActionLabel)
                }
            }
            TextButton(onClick = onOpen) {
                Text(stringResource(R.string.common_manage))
            }
        }
    }
}

@Composable
private fun AutomationDefinitionDetail(
    definition: WorkflowDefinitionUiModel,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onToggleEnabled: () -> Unit,
) {
    val summaryFacts = listOf(
        WorkflowDetailFact(
            label = stringResource(R.string.workflow_detail_steps_label),
            value = definition.stepCountLabel,
        ),
        WorkflowDetailFact(
            label = stringResource(R.string.workflow_detail_trigger_label),
            value = definition.triggerSummary.ifBlank { stringResource(R.string.common_none) },
        ),
        WorkflowDetailFact(
            label = stringResource(R.string.workflow_detail_last_run_label),
            value = definition.lastRunSummary.ifBlank { stringResource(R.string.common_none) },
        ),
        WorkflowDetailFact(
            label = stringResource(R.string.workflow_detail_status_label),
            value = definition.availabilityLabel,
        ),
    )
    TextButton(onClick = onBack) {
        Text(stringResource(R.string.common_back))
    }
    DetailHero(
        title = definition.title,
        statusLine = definition.statusLine,
        supporting = definition.entrySummary,
        accented = definition.hasActiveRun || definition.availabilityState != WorkflowAvailabilityState.READY,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onToggleEnabled) {
            Text(
                if (definition.isEnabled) {
                    stringResource(R.string.workflow_action_disable)
                } else {
                    stringResource(R.string.workflow_action_enable)
                },
            )
        }
        if (definition.canStart) {
            TextButton(onClick = onStart) {
                Text(stringResource(R.string.workflow_action_start))
            }
        }
    }

    DetailSection(title = stringResource(R.string.workflow_detail_summary_title))
    FactGrid(facts = summaryFacts)

    if (definition.actionHint.isNotBlank() &&
        (definition.hasActiveRun || definition.availabilityState != WorkflowAvailabilityState.READY)
    ) {
        CenterCard(
            title = stringResource(R.string.workflow_detail_attention_title),
            lines = listOf(definition.actionHint),
            accented = true,
        )
    }

    DetailSection(title = stringResource(R.string.workflow_detail_flow_title))
    definition.steps.forEach { step ->
        TimelineEntry(
            eyebrow = step.ordinalLabel,
            title = step.title,
            supporting = step.stepTypeLabel,
            lines = listOfNotNull(
                step.summary.takeIf { it.isNotBlank() },
                step.requiredInputsLine.takeIf { it.isNotBlank() },
                step.transitionLine.takeIf { it.isNotBlank() },
            ),
        )
    }
}

@Composable
private fun AutomationRunDetail(
    run: WorkflowRunUiModel,
    onBack: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onCancel: () -> Unit,
) {
    val workflowPauseLabel = stringResource(R.string.workflow_action_pause)
    val summaryFacts = buildList {
        run.checkpointLabel.takeIf { it.isNotBlank() }?.let {
            add(
                WorkflowDetailFact(
                    label = stringResource(R.string.workflow_detail_checkpoint_label),
                    value = it,
                ),
            )
        }
        run.nextRequiredAction.takeIf { it.isNotBlank() }?.let {
            add(
                WorkflowDetailFact(
                    label = stringResource(R.string.workflow_detail_next_action_label),
                    value = it,
                ),
            )
        }
        run.outcomeHeadline.takeIf { it.isNotBlank() }?.let {
            add(
                WorkflowDetailFact(
                    label = stringResource(R.string.workflow_detail_outcome_label),
                    value = it,
                ),
            )
        }
        run.recoveryGuidance.takeIf { it.isNotBlank() }?.let {
            add(
                WorkflowDetailFact(
                    label = stringResource(R.string.workflow_detail_recovery_label),
                    value = it,
                ),
            )
        }
    }
    TextButton(onClick = onBack) {
        Text(stringResource(R.string.common_back))
    }
    DetailHero(
        title = run.title,
        statusLine = run.statusLine,
        supporting = run.lastCheckpointSummary,
        accented = run.runState in attentionRunStates,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (run.primaryActionLabel.isNotBlank()) {
            TextButton(
                onClick = {
                    if (run.primaryActionLabel == workflowPauseLabel) {
                        onPause()
                    } else {
                        onResume()
                    }
                },
            ) {
                Text(run.primaryActionLabel)
            }
        }
        if (run.secondaryActionLabel.isNotBlank()) {
            TextButton(onClick = onCancel) {
                Text(run.secondaryActionLabel)
            }
        }
    }

    if (summaryFacts.isNotEmpty()) {
        DetailSection(title = stringResource(R.string.workflow_detail_summary_title))
        FactGrid(facts = summaryFacts)
    }

    if (run.outcomeDetails.isNotBlank()) {
        CenterCard(
            title = stringResource(R.string.workflow_detail_notes_title),
            lines = listOf(run.outcomeDetails),
            accented = run.runState in attentionRunStates,
        )
    }

    if (run.recentActivityLines.isNotEmpty()) {
        DetailSection(title = stringResource(R.string.workflow_detail_timeline_title))
        run.recentActivityLines.forEachIndexed { index, line ->
            TimelineEntry(
                eyebrow = (index + 1).toString(),
                title = line,
            )
        }
    }
    if (run.provenanceLines.isNotEmpty()) {
        DetailSection(title = stringResource(R.string.workflow_run_provenance_title))
        run.provenanceLines.forEachIndexed { index, line ->
            TimelineEntry(
                eyebrow = (index + 1).toString(),
                title = line,
            )
        }
    }
}

private data class WorkflowDetailFact(
    val label: String,
    val value: String,
)

@Composable
private fun DetailHero(
    title: String,
    statusLine: String,
    supporting: String,
    accented: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (accented) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
            ) {
                Text(
                    text = statusLine,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            if (supporting.isNotBlank()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun FactGrid(
    facts: List<WorkflowDetailFact>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        facts.chunked(2).forEach { rowFacts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowFacts.forEach { fact ->
                    FactCard(
                        fact = fact,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowFacts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FactCard(
    fact: WorkflowDetailFact,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = fact.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = fact.value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun TimelineEntry(
    title: String,
    eyebrow: String,
    supporting: String = "",
    lines: List<String> = emptyList(),
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = eyebrow,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            if (supporting.isNotBlank()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CenterCard(
    title: String,
    supporting: String = "",
    statusLine: String = "",
    lines: List<String> = emptyList(),
    accented: Boolean = false,
    content: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (accented) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (supporting.isNotBlank()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (statusLine.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
                ) {
                    Text(
                        text = statusLine,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content?.invoke()
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
