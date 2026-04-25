package com.mobileclaw.interop.probe.model

import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropTaskStatus

data class ProbeTaskState(
    val handle: InteropHandle,
    val displayName: String,
    val status: InteropTaskStatus,
    val statusLabel: String,
    val summary: String,
    val artifactHandle: InteropHandle? = null,
    val artifactSummary: String = "",
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
)
