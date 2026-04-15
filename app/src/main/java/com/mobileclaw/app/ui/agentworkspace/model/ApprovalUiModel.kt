package com.mobileclaw.app.ui.agentworkspace.model

import com.mobileclaw.app.runtime.policy.ApprovalRequest

data class ApprovalUiModel(
    val approvalRequestId: String = "",
    val sessionId: String = "",
    val toolId: String = "",
    val toolDisplayName: String = "",
    val sideEffectLabel: String = "",
    val scopeLines: List<String> = emptyList(),
    val previewLines: List<String> = emptyList(),
    val title: String = "",
    val summary: String = "",
    val previewPayload: String = "",
    val primaryActionLabel: String = "",
    val secondaryActionLabel: String = "",
)

fun ApprovalRequest.toUiModel(): ApprovalUiModel = ApprovalUiModel(
    approvalRequestId = approvalRequestId,
    sessionId = sessionId,
    toolId = toolId,
    toolDisplayName = toolDisplayName,
    sideEffectLabel = sideEffectLabel,
    scopeLines = scopeLines,
    previewLines = previewLines,
    title = title,
    summary = summary,
    previewPayload = previewPayload,
    primaryActionLabel = primaryActionLabel,
    secondaryActionLabel = secondaryActionLabel,
)
