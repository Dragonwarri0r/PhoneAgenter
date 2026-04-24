package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.HubInteropAndroidContract
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.probe.ProbeStrings
import com.mobileclaw.interop.probe.R
import com.mobileclaw.interop.probe.model.ProbeHostSummary
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import com.mobileclaw.interop.probe.model.ProbeValidationStep

data class DiscoveryResult(
    val hostSummary: ProbeHostSummary? = null,
    val outcome: ProbeValidationOutcome,
)

class DiscoveryClient(
    private val interopClient: HubInteropClient,
    private val strings: ProbeStrings,
    private val compatibilityInspector: CompatibilityInspector,
) {
    fun discoverHost(
        hostPackageName: String,
        requestedVersion: String,
    ): DiscoveryResult {
        val response = interopClient.discover(
            hostPackageName = hostPackageName,
            request = DiscoveryBundles.Request(
                requestedVersion = requestedVersion,
            ),
        ) ?: return DiscoveryResult(
            outcome = compatibilityInspector.unavailableOutcome(
                step = ProbeValidationStep.DISCOVERY,
                message = unavailableMessage(hostPackageName, ProbeValidationStep.DISCOVERY),
            ),
        )

        val surface = response.surfaceDescriptor
        val summary = surface?.let {
            ProbeHostSummary(
                hostPackageName = hostPackageName,
                authority = HubInteropAndroidContract.authorityFor(hostPackageName),
                displayName = it.displayName,
                summary = it.summary,
                surfaceId = it.surfaceId,
                contractVersion = it.contractVersion,
                status = response.status,
                compatibilitySignal = response.compatibilitySignal,
                authorizationRequirement = it.authorizationRequirement,
                capabilityLines = it.capabilities.map { capability ->
                    strings.get(
                        R.string.probe_capability_line,
                        capability.displayName,
                        capability.capabilityId,
                        strings.authorizationRequirementLabel(capability.authorizationRequirement),
                    )
                },
                methodLines = it.supportedMethods,
                tagLines = it.tags,
            )
        }

        val outcome = compatibilityInspector.outcomeForResponse(
            step = ProbeValidationStep.DISCOVERY,
            status = response.status,
            compatibilitySignal = response.compatibilitySignal,
            explicitMessage = response.message,
            successMessage = strings.discoverySuccess(surface?.displayName ?: hostPackageName),
            extraLines = listOfNotNull(
                surface?.surfaceId?.let {
                    strings.get(R.string.probe_detail_surface_id, it)
                },
            ),
        )
        return DiscoveryResult(
            hostSummary = summary,
            outcome = outcome,
        )
    }

    private fun unavailableMessage(hostPackageName: String, step: ProbeValidationStep): String {
        return if (interopClient.isHostAvailable(hostPackageName)) {
            strings.emptyResponse(step)
        } else {
            strings.hostUnavailable(hostPackageName)
        }
    }
}
