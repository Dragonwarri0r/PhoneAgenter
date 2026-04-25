package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.contract.InteropArtifactDescriptor
import com.mobileclaw.interop.probe.ProbeStrings
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import com.mobileclaw.interop.probe.model.ProbeValidationStep

data class ArtifactResult(
    val artifactDescriptor: InteropArtifactDescriptor? = null,
    val outcome: ProbeValidationOutcome,
)

class ArtifactClient(
    private val interopClient: HubInteropClient,
    private val strings: ProbeStrings,
    private val compatibilityInspector: CompatibilityInspector,
) {
    fun loadArtifact(
        hostPackageName: String,
        request: ArtifactBundles.Request,
    ): ArtifactResult {
        val response = interopClient.getArtifact(hostPackageName, request)
            ?: return ArtifactResult(
                outcome = compatibilityInspector.unavailableOutcome(
                    step = ProbeValidationStep.ARTIFACT,
                    message = unavailableMessage(hostPackageName),
                ),
            )
        return ArtifactResult(
            artifactDescriptor = response.artifactDescriptor,
            outcome = compatibilityInspector.outcomeForResponse(
                step = ProbeValidationStep.ARTIFACT,
                status = response.status,
                compatibilitySignal = response.compatibilitySignal,
                explicitMessage = response.message,
                successMessage = strings.artifactLoaded(),
            ),
        )
    }

    private fun unavailableMessage(hostPackageName: String): String {
        return if (interopClient.isHostAvailable(hostPackageName)) {
            strings.emptyResponse(ProbeValidationStep.ARTIFACT)
        } else {
            strings.hostUnavailable(hostPackageName)
        }
    }
}
