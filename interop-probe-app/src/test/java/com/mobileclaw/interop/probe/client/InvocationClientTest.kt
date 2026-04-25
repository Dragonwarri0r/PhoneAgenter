package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.ExternalTrustState
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
class InvocationClientTest {
    private val strings = ProbeStrings(RuntimeEnvironment.getApplication())
    private val fakeClient = FakeHubInteropClient()
    private val subject = InvocationClient(
        interopClient = fakeClient,
        strings = strings,
        compatibilityInspector = CompatibilityInspector(strings),
    )

    @Test
    fun `invoke returns task state when host accepts request`() {
        fakeClient.invocationResponse = InvocationBundles.Response(
            status = HubInteropStatus.OK,
            compatibilitySignal = InteropCompatibilitySignal(),
            taskDescriptor = InteropTaskDescriptor(
                handle = InteropHandle(InteropHandleFamily.TASK, "probe-task"),
                displayName = "Generate Reply",
                status = InteropTaskStatus.PENDING,
                summary = "Queued",
            ),
        )

        val result = subject.invoke(
            hostPackageName = "com.mobileclaw.app",
            request = InvocationBundles.Request(
                requestId = "req-invoke",
                callerIdentity = CallerContractIdentity(
                    originApp = "com.mobileclaw.interop.probe",
                    packageName = "com.mobileclaw.interop.probe",
                    sourceLabel = "Interop Probe",
                    trustState = ExternalTrustState.UNVERIFIED,
                    trustReason = "test",
                ),
                capabilityId = "generate.reply",
                input = "hello",
            ),
        )

        assertEquals("Generate Reply", assertNotNull(result.taskState).displayName)
        assertEquals("Pending", result.taskState?.statusLabel)
    }
}
