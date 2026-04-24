package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.TaskBundles
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropHandleFamily
import com.mobileclaw.interop.contract.InteropTaskDescriptor
import com.mobileclaw.interop.contract.InteropTaskStatus
import com.mobileclaw.interop.probe.ProbeStrings
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TaskClientTest {
    private val strings = ProbeStrings(RuntimeEnvironment.getApplication())
    private val fakeClient = FakeHubInteropClient()
    private val subject = TaskClient(
        interopClient = fakeClient,
        strings = strings,
        compatibilityInspector = CompatibilityInspector(strings),
    )

    @Test
    fun `load task maps completed artifact handle`() {
        fakeClient.taskResponse = TaskBundles.Response(
            status = HubInteropStatus.OK,
            compatibilitySignal = InteropCompatibilitySignal(),
            taskDescriptor = InteropTaskDescriptor(
                handle = InteropHandle(InteropHandleFamily.TASK, "probe-task"),
                displayName = "Generate Reply",
                status = InteropTaskStatus.COMPLETED,
                summary = "Done",
                artifactHandles = listOf(InteropHandle(InteropHandleFamily.ARTIFACT, "probe-artifact")),
            ),
        )

        val result = subject.loadTask(
            hostPackageName = "com.mobileclaw.app",
            request = TaskBundles.Request(
                handle = InteropHandle(InteropHandleFamily.TASK, "probe-task"),
            ),
        )

        assertEquals("Completed", assertNotNull(result.taskState).statusLabel)
        assertEquals("artifact:probe-artifact", result.taskState?.artifactHandle?.opaqueValue)
    }
}
