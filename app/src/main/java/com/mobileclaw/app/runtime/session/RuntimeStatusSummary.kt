package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.runtime.strings.AppStrings

data class RuntimeStatusSummary(
    val sessionId: String,
    val headline: String,
    val stageLabel: String,
    val supportingText: String,
    val isBusy: Boolean,
    val awaitingInput: Boolean,
    val isTerminal: Boolean,
)

fun runtimeSummaryForStage(
    sessionId: String,
    stageType: RuntimeStageType,
    details: String,
    strings: AppStrings,
    awaitingInput: Boolean = false,
): RuntimeStatusSummary {
    val label = strings.runtimeStageLabel(stageType)
    return RuntimeStatusSummary(
        sessionId = sessionId,
        headline = label,
        stageLabel = label,
        supportingText = details,
        isBusy = stageType !in setOf(
            RuntimeStageType.AWAITING_APPROVAL,
            RuntimeStageType.COMPLETED,
            RuntimeStageType.FAILED,
            RuntimeStageType.CANCELLED,
            RuntimeStageType.DENIED,
        ),
        awaitingInput = awaitingInput,
        isTerminal = stageType in setOf(
            RuntimeStageType.COMPLETED,
            RuntimeStageType.FAILED,
            RuntimeStageType.CANCELLED,
            RuntimeStageType.DENIED,
        ),
    )
}
