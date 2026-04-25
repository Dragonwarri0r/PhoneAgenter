package com.mobileclaw.interop.android

import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.android.bundle.InteropBundleCodec
import com.mobileclaw.interop.contract.ArtifactAccessMode
import com.mobileclaw.interop.contract.HubSurfaceDescriptorFactory
import com.mobileclaw.interop.contract.InteropApprovalRequirement
import com.mobileclaw.interop.contract.InteropArtifactDescriptor
import com.mobileclaw.interop.contract.InteropArtifactLifecycleState
import com.mobileclaw.interop.contract.InteropAvailabilityStatus
import com.mobileclaw.interop.contract.InteropBoundedness
import com.mobileclaw.interop.contract.InteropCapabilityDescriptor
import com.mobileclaw.interop.contract.InteropDataSensitivity
import com.mobileclaw.interop.contract.InteropGrantDescriptor
import com.mobileclaw.interop.contract.InteropGrantDirection
import com.mobileclaw.interop.contract.InteropGrantLifetime
import com.mobileclaw.interop.contract.InteropGrantState
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropHandleFamily
import com.mobileclaw.interop.contract.InteropIds
import com.mobileclaw.interop.contract.InteropSideEffectLevel
import com.mobileclaw.interop.contract.InteropTaskDescriptor
import com.mobileclaw.interop.contract.InteropTaskLifecycleState
import com.mobileclaw.interop.contract.InteropTaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HubInteropAndroidContractTest {
    @Test
    fun authorityUsesStableSuffix() {
        assertEquals(
            "com.example.host.hubinterop",
            HubInteropAndroidContract.authorityFor("com.example.host"),
        )
    }

    @Test
    fun methodLookupUsesWireName() {
        assertEquals(
            HubInteropMethod.INVOKE_CAPABILITY,
            HubInteropMethod.fromWireName("invoke_capability"),
        )
        assertNull(HubInteropMethod.fromWireName("missing"))
    }

    @Test
    fun statusLookupUsesWireName() {
        assertEquals(
            HubInteropStatus.AUTHORIZATION_REQUIRED,
            HubInteropStatus.fromWireName("authorization_required"),
        )
        assertEquals(
            HubInteropStatus.EXECUTION_FAILED,
            HubInteropStatus.fromWireName("failed"),
        )
        assertNull(HubInteropStatus.fromWireName("missing"))
    }

    @Test
    fun uriBuilderUsesStablePaths() {
        val uri = HubInteropUriBuilder.surface("com.example.host.hubinterop")

        assertEquals("content", uri.scheme)
        assertEquals("com.example.host.hubinterop", uri.authority)
        assertEquals("/surface", uri.path)
    }

    @Test
    fun requestFactoryBuildsDiscoveryBundle() {
        val bundle = HubInteropRequestFactory.discovery(
            DiscoveryBundles.Request(requestedVersion = "1.0"),
        )

        assertEquals("1.0", bundle.getString(HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION))
        assertTrue(bundle.containsKey(HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION))
    }

    @Test
    fun surfaceDescriptorV1RoundTripsThroughBundle() {
        val surface = HubSurfaceDescriptorFactory.hubSurface(
            surfaceId = InteropIds.Surface.RUNTIME_CALLABLE_BASIC,
            displayName = "Mobile Claw",
            summary = "Governed runtime",
            supportedMethods = HubInteropMethod.entries.map { it.wireName },
            capabilities = listOf(
                InteropCapabilityDescriptor(
                    capabilityId = InteropIds.Capability.CALENDAR_READ,
                    displayName = "Read Calendar",
                    summary = "Read bounded calendar events",
                    requiredScopes = listOf(InteropIds.Scope.CALENDAR_READ),
                    inputSchemaVersion = "1.0",
                    outputArtifactTypes = listOf("application/json"),
                    sideEffectLevel = InteropSideEffectLevel.READ,
                    dataSensitivity = InteropDataSensitivity.SENSITIVE,
                    boundedness = InteropBoundedness.BOUNDED,
                    approvalRequirement = InteropApprovalRequirement.HOST_POLICY,
                    availability = InteropAvailabilityStatus.AVAILABLE,
                ),
            ),
        )

        val restored = InteropBundleCodec.surfaceFromBundle(
            InteropBundleCodec.surfaceToBundle(surface),
        )

        val capability = restored.capabilities.single()
        assertEquals(surface.supportedMethods, restored.supportedMethods)
        assertEquals(InteropSideEffectLevel.READ, capability.sideEffectLevel)
        assertEquals(InteropDataSensitivity.SENSITIVE, capability.dataSensitivity)
        assertEquals(InteropBoundedness.BOUNDED, capability.boundedness)
        assertEquals(listOf("application/json"), capability.outputArtifactTypes)
    }

    @Test
    fun grantTaskAndArtifactDescriptorsRoundTripThroughBundle() {
        val grant = InteropGrantDescriptor(
            handle = InteropHandle(InteropHandleFamily.GRANT_REQUEST, "probe:calendar.read"),
            direction = InteropGrantDirection.INBOUND,
            lifetime = InteropGrantLifetime.PERSISTENT,
            scopes = listOf(InteropIds.Scope.CALENDAR_READ),
            isActive = false,
            state = InteropGrantState.PENDING,
            requestedAtEpochMillis = 1_000L,
            updatedAtEpochMillis = 1_500L,
        )
        val task = InteropTaskDescriptor(
            handle = InteropHandle(InteropHandleFamily.TASK, "task-1"),
            displayName = "Calendar Read",
            status = InteropTaskStatus.COMPLETED,
            summary = "Done",
            updatedAtEpochMillis = 2_000L,
            lifecycleState = InteropTaskLifecycleState.COMPLETED,
            availability = InteropAvailabilityStatus.AVAILABLE,
            createdAtEpochMillis = 1_000L,
            expiresAtEpochMillis = 3_000L,
        )
        val artifact = InteropArtifactDescriptor(
            handle = InteropHandle(InteropHandleFamily.ARTIFACT, "artifact-1"),
            displayName = "Calendar Summary",
            mimeType = "application/json",
            accessMode = ArtifactAccessMode.READ_ONLY,
            summary = "Three events",
            artifactType = "calendar.summary",
            lifecycleState = InteropArtifactLifecycleState.AVAILABLE,
            availability = InteropAvailabilityStatus.AVAILABLE,
            createdAtEpochMillis = 2_000L,
            expiresAtEpochMillis = 3_000L,
        )

        assertEquals(grant, InteropBundleCodec.grantFromBundle(InteropBundleCodec.grantToBundle(grant)))
        assertEquals(task, InteropBundleCodec.taskFromBundle(InteropBundleCodec.taskToBundle(task)))
        assertEquals(artifact, InteropBundleCodec.artifactFromBundle(InteropBundleCodec.artifactToBundle(artifact)))
    }
}
