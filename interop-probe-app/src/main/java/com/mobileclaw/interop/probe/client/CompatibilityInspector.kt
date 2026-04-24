package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.contract.CompatibilityState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.probe.ProbeStrings
import com.mobileclaw.interop.probe.R
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import com.mobileclaw.interop.probe.model.ProbeValidationSeverity
import com.mobileclaw.interop.probe.model.ProbeValidationStep

class CompatibilityInspector(
    private val strings: ProbeStrings,
) {
    fun outcomeForResponse(
        step: ProbeValidationStep,
        status: HubInteropStatus,
        compatibilitySignal: InteropCompatibilitySignal,
        explicitMessage: String?,
        successMessage: String,
        extraLines: List<String> = emptyList(),
    ): ProbeValidationOutcome {
        val resolvedMessage = explicitMessage?.takeIf { it.isNotBlank() } ?: when (severityFor(status, compatibilitySignal)) {
            ProbeValidationSeverity.SUCCESS -> successMessage
            ProbeValidationSeverity.WARNING -> strings.compatibilitySummary(compatibilitySignal)
            ProbeValidationSeverity.ERROR -> strings.compatibilitySummary(compatibilitySignal)
            ProbeValidationSeverity.INFO -> successMessage
        }
        return ProbeValidationOutcome(
            step = step,
            title = strings.stepLabel(step),
            statusLine = strings.hubStatusLabel(status),
            message = resolvedMessage,
            detailLines = buildList {
                add(
                    strings.get(
                        R.string.probe_detail_requested_version,
                        compatibilitySignal.interopVersion,
                    ),
                )
                add(
                    strings.get(
                        R.string.probe_detail_supported_version,
                        compatibilitySignal.supportedVersion,
                    ),
                )
                add(
                    strings.get(
                        R.string.probe_detail_compatibility_state,
                        strings.compatibilityStateLabel(compatibilitySignal.compatibilityState),
                    ),
                )
                add(
                    strings.get(
                        R.string.probe_detail_reason,
                        compatibilitySignal.compatibilityReason,
                    ),
                )
                addAll(extraLines)
            },
            severity = severityFor(status, compatibilitySignal),
        )
    }

    fun unavailableOutcome(
        step: ProbeValidationStep,
        message: String,
    ): ProbeValidationOutcome {
        return ProbeValidationOutcome(
            step = step,
            title = strings.stepLabel(step),
            statusLine = strings.get(R.string.probe_status_unavailable),
            message = message,
            severity = ProbeValidationSeverity.ERROR,
        )
    }

    private fun severityFor(
        status: HubInteropStatus,
        compatibilitySignal: InteropCompatibilitySignal,
    ): ProbeValidationSeverity {
        return when {
            status == HubInteropStatus.OK && compatibilitySignal.isCompatible &&
                compatibilitySignal.compatibilityState == CompatibilityState.SUPPORTED -> {
                ProbeValidationSeverity.SUCCESS
            }

            status == HubInteropStatus.AUTHORIZATION_PENDING ||
                status == HubInteropStatus.AUTHORIZATION_REQUIRED ||
                status == HubInteropStatus.APPROVAL_REQUIRED ||
                compatibilitySignal.compatibilityState == CompatibilityState.DOWNGRADED -> {
                ProbeValidationSeverity.WARNING
            }

            else -> ProbeValidationSeverity.ERROR
        }
    }
}
