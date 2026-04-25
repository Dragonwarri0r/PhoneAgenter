package com.mobileclaw.app.runtime.interop

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.TaskBundles
import com.mobileclaw.interop.contract.InteropTaskStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class HubInteropTaskServiceTest {
    private val subject = HubInteropTaskService(
        compatibilityService = HubInteropCompatibilityService(),
    )

    @Test
    fun `completed task exposes artifact continuation`() = runBlocking {
        val task = subject.createPendingTask(
            requestId = "req-task-1",
            capabilityId = "generate.reply",
            displayName = "Reply",
            summary = "Queued",
        )

        subject.markCompleted(
            taskHandle = task.handle,
            summary = "Completed",
            outputText = "Hello from Mobile Claw",
        )

        val taskResponse = subject.taskResponse(
            TaskBundles.Request(handle = task.handle),
        )
        val resolvedTask = assertNotNull(taskResponse.taskDescriptor)
        assertEquals(HubInteropStatus.OK, taskResponse.status)
        assertEquals(InteropTaskStatus.COMPLETED, resolvedTask.status)
        assertEquals(1, resolvedTask.artifactHandles.size)

        val artifactResponse = subject.artifactResponse(
            ArtifactBundles.Request(handle = resolvedTask.artifactHandles.single()),
        )
        val artifact = assertNotNull(artifactResponse.artifactDescriptor)
        assertEquals(HubInteropStatus.OK, artifactResponse.status)
        assertEquals("text/plain", artifact.mimeType)
        assertEquals("Hello from Mobile Claw", artifact.summary)
    }
}
