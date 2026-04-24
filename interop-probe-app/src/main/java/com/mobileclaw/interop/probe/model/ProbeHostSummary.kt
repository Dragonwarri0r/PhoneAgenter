package com.mobileclaw.interop.probe.model

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.contract.InteropAuthorizationRequirement
import com.mobileclaw.interop.contract.InteropCompatibilitySignal

data class ProbeHostSummary(
    val hostPackageName: String,
    val authority: String,
    val displayName: String,
    val summary: String,
    val surfaceId: String,
    val contractVersion: String,
    val status: HubInteropStatus,
    val compatibilitySignal: InteropCompatibilitySignal,
    val authorizationRequirement: InteropAuthorizationRequirement,
    val capabilityLines: List<String> = emptyList(),
    val methodLines: List<String> = emptyList(),
    val tagLines: List<String> = emptyList(),
)
