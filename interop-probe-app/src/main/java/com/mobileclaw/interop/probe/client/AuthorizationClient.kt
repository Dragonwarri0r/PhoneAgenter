package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.contract.InteropGrantDescriptor
import com.mobileclaw.interop.probe.ProbeStrings
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import com.mobileclaw.interop.probe.model.ProbeValidationStep

data class AuthorizationResult(
    val grantDescriptor: InteropGrantDescriptor? = null,
    val outcome: ProbeValidationOutcome,
)

class AuthorizationClient(
    private val interopClient: HubInteropClient,
    private val strings: ProbeStrings,
    private val compatibilityInspector: CompatibilityInspector,
) {
    fun requestAuthorization(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationResult {
        val response = interopClient.requestAuthorization(hostPackageName, request)
            ?: return AuthorizationResult(
                outcome = compatibilityInspector.unavailableOutcome(
                    step = ProbeValidationStep.AUTHORIZATION,
                    message = unavailableMessage(hostPackageName),
                ),
            )
        return AuthorizationResult(
            grantDescriptor = response.grantDescriptor,
            outcome = compatibilityInspector.outcomeForResponse(
                step = ProbeValidationStep.AUTHORIZATION,
                status = response.status,
                compatibilitySignal = response.compatibilitySignal,
                explicitMessage = response.message,
                successMessage = strings.authorizationRequested(),
            ),
        )
    }

    fun getGrantStatus(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationResult {
        val response = interopClient.getGrantStatus(hostPackageName, request)
            ?: return AuthorizationResult(
                outcome = compatibilityInspector.unavailableOutcome(
                    step = ProbeValidationStep.AUTHORIZATION,
                    message = unavailableMessage(hostPackageName),
                ),
            )
        return AuthorizationResult(
            grantDescriptor = response.grantDescriptor,
            outcome = compatibilityInspector.outcomeForResponse(
                step = ProbeValidationStep.AUTHORIZATION,
                status = response.status,
                compatibilitySignal = response.compatibilitySignal,
                explicitMessage = response.message,
                successMessage = strings.authorizationGranted(),
            ),
        )
    }

    fun revokeGrant(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationResult {
        val response = interopClient.revokeGrant(hostPackageName, request)
            ?: return AuthorizationResult(
                outcome = compatibilityInspector.unavailableOutcome(
                    step = ProbeValidationStep.AUTHORIZATION,
                    message = unavailableMessage(hostPackageName),
                ),
            )
        return AuthorizationResult(
            grantDescriptor = response.grantDescriptor,
            outcome = compatibilityInspector.outcomeForResponse(
                step = ProbeValidationStep.AUTHORIZATION,
                status = response.status,
                compatibilitySignal = response.compatibilitySignal,
                explicitMessage = response.message,
                successMessage = strings.authorizationRevoked(),
            ),
        )
    }

    private fun unavailableMessage(hostPackageName: String): String {
        return if (interopClient.isHostAvailable(hostPackageName)) {
            strings.emptyResponse(ProbeValidationStep.AUTHORIZATION)
        } else {
            strings.hostUnavailable(hostPackageName)
        }
    }
}
