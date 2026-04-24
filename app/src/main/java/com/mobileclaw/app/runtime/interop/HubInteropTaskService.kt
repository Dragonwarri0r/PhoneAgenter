package com.mobileclaw.app.runtime.interop

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.HubInteropStatusMapper
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.TaskBundles
import com.mobileclaw.interop.contract.ArtifactAccessMode
import com.mobileclaw.interop.contract.InteropArtifactDescriptor
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropHandleFamily
import com.mobileclaw.interop.contract.InteropTaskDescriptor
import com.mobileclaw.interop.contract.InteropTaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private data class HostedInteropTask(
    val taskHandle: InteropHandle,
    val capabilityId: String,
    val displayName: String,
    val summary: String,
    val status: InteropTaskStatus,
    val sessionId: String? = null,
    val artifact: InteropArtifactDescriptor? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
)

@Singleton
class HubInteropTaskService @Inject constructor(
    private val compatibilityService: HubInteropCompatibilityService,
) {
    private val mutex = Mutex()
    private val tasks = linkedMapOf<String, HostedInteropTask>()
    private val artifacts = linkedMapOf<String, InteropArtifactDescriptor>()
    private val _recentTasks = MutableStateFlow<List<InteropTaskDescriptor>>(emptyList())
    val recentTasks: StateFlow<List<InteropTaskDescriptor>> = _recentTasks.asStateFlow()

    suspend fun createPendingTask(
        requestId: String,
        capabilityId: String,
        displayName: String,
        summary: String,
    ): InteropTaskDescriptor = mutex.withLock {
        val taskHandle = InteropHandle(
            family = InteropHandleFamily.TASK,
            value = requestId,
        )
        val record = HostedInteropTask(
            taskHandle = taskHandle,
            capabilityId = capabilityId,
            displayName = displayName,
            summary = summary,
            status = InteropTaskStatus.PENDING,
        )
        tasks[taskHandle.opaqueValue] = record
        refreshTaskSnapshotsLocked()
        record.toDescriptor()
    }

    suspend fun markSessionStarted(
        taskHandle: InteropHandle,
        sessionId: String,
        summary: String,
    ) = updateTask(taskHandle) { current ->
        current.copy(
            sessionId = sessionId,
            status = InteropTaskStatus.RUNNING,
            summary = summary,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    suspend fun markRunning(
        taskHandle: InteropHandle,
        summary: String,
    ) = updateTask(taskHandle) { current ->
        current.copy(
            status = InteropTaskStatus.RUNNING,
            summary = summary,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    suspend fun markInputRequired(
        taskHandle: InteropHandle,
        summary: String,
    ) = updateTask(taskHandle) { current ->
        current.copy(
            status = InteropTaskStatus.INPUT_REQUIRED,
            summary = summary,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    suspend fun markCompleted(
        taskHandle: InteropHandle,
        summary: String,
        outputText: String?,
    ) = updateTask(taskHandle) { current ->
        val artifact = outputText?.takeIf { it.isNotBlank() }?.let { output ->
            InteropArtifactDescriptor(
                handle = InteropHandle(
                    family = InteropHandleFamily.ARTIFACT,
                    value = taskHandle.value,
                ),
                displayName = "${current.displayName} Result",
                mimeType = "text/plain",
                accessMode = ArtifactAccessMode.READ_ONLY,
                summary = output,
                artifactType = "text/plain",
                createdAtEpochMillis = System.currentTimeMillis(),
            )
        }
        artifact?.let { artifacts[it.handle.opaqueValue] = it }
        current.copy(
            status = InteropTaskStatus.COMPLETED,
            summary = summary,
            artifact = artifact,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    suspend fun markFailed(
        taskHandle: InteropHandle,
        summary: String,
    ) = updateTask(taskHandle) { current ->
        current.copy(
            status = InteropTaskStatus.FAILED,
            summary = summary,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    suspend fun markCancelled(
        taskHandle: InteropHandle,
        summary: String,
    ) = updateTask(taskHandle) { current ->
        current.copy(
            status = InteropTaskStatus.CANCELLED,
            summary = summary,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    suspend fun taskResponse(request: TaskBundles.Request): TaskBundles.Response {
        val compatibility = compatibilityService.evaluate(request.requestedVersion)
        val task = mutex.withLock { tasks[request.handle.opaqueValue] }
        val status = if (task == null) {
            HubInteropStatus.NOT_FOUND
        } else {
            HubInteropStatusMapper.merge(HubInteropStatus.OK, compatibility)
        }
        return TaskBundles.Response(
            status = status,
            compatibilitySignal = compatibility,
            taskDescriptor = task?.toDescriptor(),
            message = task?.summary ?: "task_not_found",
        )
    }

    suspend fun artifactResponse(request: ArtifactBundles.Request): ArtifactBundles.Response {
        val compatibility = compatibilityService.evaluate(request.requestedVersion)
        val artifact = mutex.withLock { artifacts[request.handle.opaqueValue] }
        val status = if (artifact == null) {
            HubInteropStatus.NOT_FOUND
        } else {
            HubInteropStatusMapper.merge(HubInteropStatus.OK, compatibility)
        }
        return ArtifactBundles.Response(
            status = status,
            compatibilitySignal = compatibility,
            artifactDescriptor = artifact,
            message = artifact?.summary ?: "artifact_not_found",
        )
    }

    private suspend fun updateTask(
        taskHandle: InteropHandle,
        transform: (HostedInteropTask) -> HostedInteropTask,
    ) = mutex.withLock {
        val current = tasks[taskHandle.opaqueValue] ?: return@withLock
        tasks[taskHandle.opaqueValue] = transform(current)
        refreshTaskSnapshotsLocked()
    }

    private fun refreshTaskSnapshotsLocked() {
        _recentTasks.value = tasks.values
            .sortedByDescending { it.updatedAtEpochMillis }
            .map { it.toDescriptor() }
    }

    private fun HostedInteropTask.toDescriptor(): InteropTaskDescriptor {
        return InteropTaskDescriptor(
            handle = taskHandle,
            displayName = displayName,
            status = status,
            summary = summary,
            artifactHandles = artifact?.let { listOf(it.handle) } ?: emptyList(),
            updatedAtEpochMillis = updatedAtEpochMillis,
            createdAtEpochMillis = createdAtEpochMillis,
        )
    }
}
