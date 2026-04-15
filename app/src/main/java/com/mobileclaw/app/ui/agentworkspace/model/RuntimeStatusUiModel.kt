package com.mobileclaw.app.ui.agentworkspace.model

import com.mobileclaw.app.runtime.session.RuntimeStatusSummary

data class RuntimeStatusUiModel(
    val headline: String = "",
    val stageLabel: String = "",
    val supportingText: String = "",
    val toolId: String = "",
    val toolDisplayName: String = "",
    val toolSideEffectLabel: String = "",
    val toolScopeLines: List<String> = emptyList(),
    val toolVisibilityLabel: String = "",
    val toolVisibilityReason: String = "",
    val sourceLabel: String = "",
    val trustStateLabel: String = "",
    val interopContractLabel: String = "",
    val uriGrantLabel: String = "",
    val routeSummary: String = "",
    val callerTrust: String = "",
    val structuredActionTitle: String = "",
    val structuredCompleteness: String = "",
    val structuredFieldLines: List<String> = emptyList(),
    val structuredWarnings: List<String> = emptyList(),
    val extensionStatusLines: List<String> = emptyList(),
    val systemSourceStatusLines: List<String> = emptyList(),
    val systemSourceContributionLines: List<String> = emptyList(),
    val hasMissingSystemSourcePermissions: Boolean = false,
    val isBusy: Boolean = false,
    val awaitingInput: Boolean = false,
    val isTerminal: Boolean = false,
)

fun RuntimeStatusSummary.toUiModel(): RuntimeStatusUiModel = RuntimeStatusUiModel(
    headline = headline,
    stageLabel = stageLabel,
    supportingText = supportingText,
    isBusy = isBusy,
    awaitingInput = awaitingInput,
    isTerminal = isTerminal,
)
