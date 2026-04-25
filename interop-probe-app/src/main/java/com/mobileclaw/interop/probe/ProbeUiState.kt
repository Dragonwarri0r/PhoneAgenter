package com.mobileclaw.interop.probe

import com.mobileclaw.interop.contract.InteropArtifactDescriptor
import com.mobileclaw.interop.contract.InteropGrantDescriptor
import com.mobileclaw.interop.contract.InteropVersion
import com.mobileclaw.interop.probe.model.ProbeHostSummary
import com.mobileclaw.interop.probe.model.ProbeTaskState
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome

data class ProbeUiState(
    val hostPackageName: String = "com.mobileclaw.app",
    val requestedVersion: String = InteropVersion.CURRENT.value,
    val invocationInput: String = "Draft a brief friendly reply saying hello and thanks.",
    val hostSummary: ProbeHostSummary? = null,
    val latestGrantDescriptor: InteropGrantDescriptor? = null,
    val latestAuthorizationOutcome: ProbeValidationOutcome? = null,
    val latestInvocationOutcome: ProbeValidationOutcome? = null,
    val latestTask: ProbeTaskState? = null,
    val latestArtifact: InteropArtifactDescriptor? = null,
    val driftOutcomes: List<ProbeValidationOutcome> = emptyList(),
    val timeline: List<ProbeValidationOutcome> = emptyList(),
    val isBusy: Boolean = false,
)
