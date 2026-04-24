package com.mobileclaw.interop.probe.model

enum class ProbeValidationSeverity {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
}

enum class ProbeValidationStep {
    DISCOVERY,
    AUTHORIZATION,
    INVOCATION,
    TASK,
    ARTIFACT,
    COMPATIBILITY,
}

data class ProbeValidationOutcome(
    val outcomeId: String = "probe-outcome-${System.currentTimeMillis()}",
    val step: ProbeValidationStep,
    val title: String,
    val statusLine: String,
    val message: String,
    val detailLines: List<String> = emptyList(),
    val severity: ProbeValidationSeverity = ProbeValidationSeverity.INFO,
    val timestampMillis: Long = System.currentTimeMillis(),
)
