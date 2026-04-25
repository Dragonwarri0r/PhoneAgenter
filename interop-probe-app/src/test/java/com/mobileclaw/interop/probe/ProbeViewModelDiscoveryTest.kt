package com.mobileclaw.interop.probe

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.contract.HubSurfaceDescriptor
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.probe.client.ArtifactClient
import com.mobileclaw.interop.probe.client.AuthorizationClient
import com.mobileclaw.interop.probe.client.CompatibilityInspector
import com.mobileclaw.interop.probe.client.DiscoveryClient
import com.mobileclaw.interop.probe.client.FakeHubInteropClient
import com.mobileclaw.interop.probe.client.InvocationClient
import com.mobileclaw.interop.probe.client.TaskClient
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProbeViewModelDiscoveryTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `discover host updates summary and timeline`() {
        val strings = ProbeStrings(RuntimeEnvironment.getApplication())
        val fakeClient = FakeHubInteropClient().apply {
            discoveryResponse = DiscoveryBundles.Response(
                status = HubInteropStatus.OK,
                compatibilitySignal = InteropCompatibilitySignal(),
                surfaceDescriptor = HubSurfaceDescriptor(
                    surfaceId = "runtime.callable.basic",
                    displayName = "Mobile Claw Hub",
                    summary = "Governed host",
                ),
            )
        }
        val inspector = CompatibilityInspector(strings)
        val viewModel = ProbeViewModel(
            probePackageName = "com.mobileclaw.interop.probe",
            probeLabel = "Interop Probe",
            strings = strings,
            discoveryClient = DiscoveryClient(fakeClient, strings, inspector),
            authorizationClient = AuthorizationClient(fakeClient, strings, inspector),
            invocationClient = InvocationClient(fakeClient, strings, inspector),
            taskClient = TaskClient(fakeClient, strings, inspector),
            artifactClient = ArtifactClient(fakeClient, strings, inspector),
            compatibilityInspector = inspector,
            ioDispatcher = testDispatcher,
        )

        viewModel.discoverHost()

        val state = viewModel.uiState.value
        assertEquals("Mobile Claw Hub", assertNotNull(state.hostSummary).displayName)
        assertEquals(1, state.timeline.size)
    }
}
