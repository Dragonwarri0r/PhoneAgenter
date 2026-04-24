package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.bundle.TaskBundles
import com.mobileclaw.interop.probe.ProbeStrings
import com.mobileclaw.interop.probe.model.ProbeTaskState
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import com.mobileclaw.interop.probe.model.ProbeValidationStep

data class TaskResult(
    val taskState: ProbeTaskState? = null,
    val outcome: ProbeValidationOutcome,
)

class TaskClient(
    private val interopClient: HubInteropClient,
    private val strings: ProbeStrings,
    private val compatibilityInspector: CompatibilityInspector,
) {
    fun loadTask(
        hostPackageName: String,
        request: TaskBundles.Request,
    ): TaskResult {
        val response = interopClient.getTask(hostPackageName, request)
            ?: return TaskResult(
                outcome = compatibilityInspector.unavailableOutcome(
                    step = ProbeValidationStep.TASK,
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
        return TaskResult(
            taskState = taskState,
            outcome = compatibilityInspector.outcomeForResponse(
                step = ProbeValidationStep.TASK,
                status = response.status,
                compatibilitySignal = response.compatibilitySignal,
                explicitMessage = response.message,
                successMessage = strings.taskLoaded(),
            ),
        )
    }

    private fun unavailableMessage(hostPackageName: String): String {
        return if (interopClient.isHostAvailable(hostPackageName)) {
            strings.emptyResponse(ProbeValidationStep.TASK)
        } else {
            strings.hostUnavailable(hostPackageName)
        }
    }
}
