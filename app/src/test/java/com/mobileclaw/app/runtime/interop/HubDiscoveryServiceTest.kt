package com.mobileclaw.app.runtime.interop

import com.mobileclaw.app.runtime.capability.StandardToolCatalog
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.contract.InteropAuthorizationRequirement
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HubDiscoveryServiceTest {
    private val appStrings = AppStrings(RuntimeEnvironment.getApplication())
    private val subject = HubDiscoveryService(
        surfaceDescriptorAssembler = HubSurfaceDescriptorAssembler(
            standardToolCatalog = StandardToolCatalog(appStrings),
            appStrings = appStrings,
        ),
        compatibilityService = HubInteropCompatibilityService(),
    )

    @Test
    fun `discover exposes governed callable surface and authorization methods`() {
        val response = subject.discover(DiscoveryBundles.Request())

        assertEquals(HubInteropStatus.OK, response.status)
        val surface = assertNotNull(response.surfaceDescriptor)
        assertEquals(InteropAuthorizationRequirement.USER_CONSENT, surface.authorizationRequirement)
        assertTrue(surface.supportedMethods.contains(HubInteropMethod.INVOKE_CAPABILITY.wireName))
        assertTrue(surface.supportedMethods.contains(HubInteropMethod.REQUEST_AUTHORIZATION.wireName))
        assertTrue(surface.supportedMethods.contains(HubInteropMethod.GET_GRANT_STATUS.wireName))
        assertTrue(surface.supportedMethods.contains(HubInteropMethod.REVOKE_GRANT.wireName))
        assertEquals(
            InteropAuthorizationRequirement.USER_CONSENT,
            surface.capabilities.single().authorizationRequirement,
        )
    }
}
