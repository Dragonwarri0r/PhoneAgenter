package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.ExternalTrustState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropGrantDescriptor
import com.mobileclaw.interop.contract.InteropGrantDirection
import com.mobileclaw.interop.contract.InteropGrantLifetime
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropHandleFamily
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
class AuthorizationClientTest {
    private val strings = ProbeStrings(RuntimeEnvironment.getApplication())
    private val fakeClient = FakeHubInteropClient()
    private val subject = AuthorizationClient(
        interopClient = fakeClient,
        strings = strings,
        compatibilityInspector = CompatibilityInspector(strings),
    )

    @Test
    fun `request authorization surfaces pending grant`() {
        fakeClient.requestAuthorizationResponse = AuthorizationBundles.Response(
            status = HubInteropStatus.AUTHORIZATION_PENDING,
            compatibilitySignal = InteropCompatibilitySignal(),
            grantDescriptor = InteropGrantDescriptor(
                handle = InteropHandle(InteropHandleFamily.GRANT_REQUEST, "probe:generate.reply"),
                direction = InteropGrantDirection.INBOUND,
                lifetime = InteropGrantLifetime.PERSISTENT,
                scopes = listOf("reply.generate"),
                isActive = false,
            ),
        )

        val result = subject.requestAuthorization(
            hostPackageName = "com.mobileclaw.app",
            request = request(),
        )

        assertEquals(HubInteropStatus.AUTHORIZATION_PENDING.wireName, "authorization_pending")
        assertEquals(false, assertNotNull(result.grantDescriptor).isActive)
        assertEquals("Authorization", result.outcome.title)
    }

    @Test
    fun `request authorization still proceeds when package visibility precheck is false`() {
        fakeClient.hostAvailable = false
        fakeClient.requestAuthorizationResponse = AuthorizationBundles.Response(
            status = HubInteropStatus.AUTHORIZATION_PENDING,
            compatibilitySignal = InteropCompatibilitySignal(),
            grantDescriptor = InteropGrantDescriptor(
                handle = InteropHandle(InteropHandleFamily.GRANT_REQUEST, "probe:generate.reply"),
                direction = InteropGrantDirection.INBOUND,
                lifetime = InteropGrantLifetime.PERSISTENT,
                scopes = listOf("reply.generate"),
                isActive = false,
            ),
        )

        val result = subject.requestAuthorization(
            hostPackageName = "com.mobileclaw.app",
            request = request(),
        )

        assertEquals("Authorization", result.outcome.title)
        assertEquals(false, assertNotNull(result.grantDescriptor).isActive)
    }

    private fun request(): AuthorizationBundles.Request {
        return AuthorizationBundles.Request(
            requestId = "req-auth",
            callerIdentity = CallerContractIdentity(
                originApp = "com.mobileclaw.interop.probe",
                packageName = "com.mobileclaw.interop.probe",
                sourceLabel = "Interop Probe",
                trustState = ExternalTrustState.UNVERIFIED,
                trustReason = "test",
            ),
            capabilityId = "generate.reply",
        )
    }
}
