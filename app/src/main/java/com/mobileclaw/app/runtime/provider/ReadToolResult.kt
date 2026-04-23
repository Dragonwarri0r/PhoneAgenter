package com.mobileclaw.app.runtime.provider

import com.mobileclaw.app.runtime.session.CapabilitySelectionOutcome

enum class ReadToolOutcomeKind {
    MATCHED,
    NO_RESULTS,
    UNAVAILABLE,
    FAILED,
}

data class ReadQueryScope(
    val scopeId: String,
    val displayLabel: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
)

data class ExplicitReadToolRequest(
    val requestId: String,
    val capabilityId: String,
    val queryText: String,
    val queryScope: ReadQueryScope,
    val resultLimit: Int,
    val permissionRequirements: List<String>,
    val routeExplanation: String,
    val selectionContext: CapabilitySelectionOutcome? = null,
)

data class ReadRecordSummary(
    val title: String,
    val supportingText: String,
    val timestampLabel: String = "",
)

data class ReadToolResult(
    val requestId: String,
    val capabilityId: String,
    val providerId: String,
    val outcomeKind: ReadToolOutcomeKind,
    val recordSummaries: List<ReadRecordSummary> = emptyList(),
    val resultCount: Int = 0,
    val userMessage: String,
    val auditSummary: String,
    val routeExplanation: String,
    val recoveryMessage: String = "",
)

