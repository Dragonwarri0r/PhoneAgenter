package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.contract.HubSurfaceDescriptor
import com.mobileclaw.interop.contract.InteropAuthorizationRequirement
import com.mobileclaw.interop.contract.InteropCapabilityDescriptor
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
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
class DiscoveryClientTest {
    private val strings = ProbeStrings(RuntimeEnvironment.getApplication())
    private val fakeClient = FakeHubInteropClient()
    private val subject = DiscoveryClient(
        interopClient = fakeClient,
        strings = strings,
        compatibilityInspector = CompatibilityInspector(strings),
    )

    @Test
    fun `discover host surfaces public descriptor`() {
        fakeClient.discoveryResponse = DiscoveryBundles.Response(
            status = HubInteropStatus.OK,
            compatibilitySignal = InteropCompatibilitySignal(),
            surfaceDescriptor = HubSurfaceDescriptor(
                surfaceId = "runtime.callable.basic",
                displayName = "Mobile Claw Hub",
                summary = "Governed host",
                capabilities = listOf(
                    InteropCapabilityDescriptor(
                        capabilityId = "generate.reply",
                        displayName = "Generate Reply",
                        summary = "Draft a reply",
                        authorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
                    ),
                ),
            ),
        )

        val result = subject.discoverHost(
            hostPackageName = "com.mobileclaw.app",
            requestedVersion = "1.0",
        )

        assertEquals(HubInteropStatus.OK, result.hostSummary?.status)
        assertEquals("Mobile Claw Hub", assertNotNull(result.hostSummary).displayName)
        assertEquals("Discovery", result.outcome.title)
    }

    @Test
    fun `discover host does not fail early when package visibility precheck is false`() {
        fakeClient.hostAvailable = false
        fakeClient.discoveryResponse = DiscoveryBundles.Response(
            status = HubInteropStatus.OK,
            compatibilitySignal = InteropCompatibilitySignal(),
            surfaceDescriptor = HubSurfaceDescriptor(
                surfaceId = "runtime.callable.basic",
                displayName = "Mobile Claw Hub",
                summary = "Governed host",
            ),
        )

        val result = subject.discoverHost(
            hostPackageName = "com.mobileclaw.app",
            requestedVersion = "1.0",
        )

        assertEquals(HubInteropStatus.OK, result.hostSummary?.status)
        assertEquals("Mobile Claw Hub", assertNotNull(result.hostSummary).displayName)
    }
}
