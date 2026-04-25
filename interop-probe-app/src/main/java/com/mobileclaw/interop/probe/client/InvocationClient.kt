package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.probe.ProbeStrings
import com.mobileclaw.interop.probe.model.ProbeTaskState
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import com.mobileclaw.interop.probe.model.ProbeValidationStep

data class InvocationResult(
    val taskState: ProbeTaskState? = null,
    val outcome: ProbeValidationOutcome,
)

class InvocationClient(
    private val interopClient: HubInteropClient,
    private val strings: ProbeStrings,
    private val compatibilityInspector: CompatibilityInspector,
) {
    fun invoke(
        hostPackageName: String,
        request: InvocationBundles.Request,
    ): InvocationResult {
        val response = interopClient.invoke(hostPackageName, request)
            ?: return InvocationResult(
                outcome = compatibilityInspector.unavailableOutcome(
                    step = ProbeValidationStep.INVOCATION,
                    message = unavailableMessage(hostPackageName),
                ),
            )

        val taskState = response.taskDescriptor?.let { descriptor ->
            ProbeTaskState(
                handle = descriptor.handle,
                displayName = descriptor.displayName,
                status = descriptor.status,
                statusLabel = strings.taskStatusLabel(descriptor.status),
                summary = descriptor.summary,
                artifactHandle = descriptor.artifactHandles.firstOrNull(),
                updatedAtEpochMillis = descriptor.updatedAtEpochMillis,
            )
        }

        return InvocationResult(
            taskState = taskState,
            outcome = compatibilityInspector.outcomeForResponse(
                step = ProbeValidationStep.INVOCATION,
                status = response.status,
                compatibilitySignal = response.compatibilitySignal,
                explicitMessage = response.message,
                successMessage = strings.invocationAccepted(),
            ),
        )
    }

    private fun unavailableMessage(hostPackageName: String): String {
        return if (interopClient.isHostAvailable(hostPackageName)) {
            strings.emptyResponse(ProbeValidationStep.INVOCATION)
        } else {
            strings.hostUnavailable(hostPackageName)
        }
    }
}
