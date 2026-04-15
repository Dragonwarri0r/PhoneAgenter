package com.mobileclaw.app.ui.agentworkspace.model

import com.mobileclaw.app.runtime.policy.AuditEvent

data class AuditUiModel(
    val auditEventId: String = "",
    val toolDisplayName: String = "",
    val sideEffectLabel: String = "",
    val headline: String = "",
    val details: String = "",
)

fun AuditEvent.toUiModel(): AuditUiModel = AuditUiModel(
    auditEventId = auditEventId,
    toolDisplayName = toolDisplayName.orEmpty(),
    sideEffectLabel = sideEffectLabel.orEmpty(),
    headline = headline,
    details = details,
)
