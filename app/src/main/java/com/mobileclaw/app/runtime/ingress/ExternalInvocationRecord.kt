package com.mobileclaw.app.runtime.ingress

data class ExternalInvocationRecord(
    val handoffId: String,
    val runtimeRequestId: String,
    val sessionId: String? = null,
    val sourceLabel: String,
    val trustState: ExternalTrustState,
    val accepted: Boolean,
    val failureReason: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)
