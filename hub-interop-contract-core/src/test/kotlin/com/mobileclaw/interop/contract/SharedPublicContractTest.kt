package com.mobileclaw.interop.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedPublicContractTest {
    @Test
    fun handleOpaqueValueRoundTrips() {
        val handle = InteropHandle(
            family = InteropHandleFamily.TASK,
            value = "task-123",
        )

        assertEquals("task:task-123", handle.opaqueValue)
        assertEquals(handle, InteropHandle.parse(handle.opaqueValue))
    }

    @Test
    fun surfaceValidatorAcceptsWellFormedDescriptor() {
        val surface = HubSurfaceDescriptorFactory.hubSurface(
            surfaceId = InteropIds.Surface.RUNTIME_CALLABLE_BASIC,
            displayName = "Test Surface",
            summary = "A shared protocol surface",
            supportedMethods = listOf("discover_surface"),
            capabilities = listOf(
                InteropCapabilityDescriptor(
                    capabilityId = InteropIds.Capability.GENERATE_REPLY,
                    displayName = "Draft Reply",
                    summary = "Generate a reply draft",
                    inputSchemaVersion = "1.0",
                    outputArtifactTypes = listOf("text/plain"),
                    sideEffectLevel = InteropSideEffectLevel.NONE,
                    dataSensitivity = InteropDataSensitivity.STANDARD,
                    boundedness = InteropBoundedness.BOUNDED,
                    approvalRequirement = InteropApprovalRequirement.HOST_POLICY,
                    availability = InteropAvailabilityStatus.AVAILABLE,
                ),
            ),
        )

        val result = InteropContractValidator.validate(surface)

        assertTrue(result.isValid)
        assertTrue(result.issues.isEmpty())
        assertNotNull(surface.capabilities.firstOrNull())
    }

    @Test
    fun capabilityDescriptorV1ExposesCallerVisibleBehavior() {
        val descriptor = InteropCapabilityDescriptor(
            capabilityId = InteropIds.Capability.CALENDAR_READ,
            displayName = "Read Calendar",
            summary = "Read bounded calendar events",
            requiredScopes = listOf(InteropIds.Scope.CALENDAR_READ),
            inputSchemaVersion = "1.0",
            outputArtifactTypes = listOf("application/vnd.mobileclaw.calendar-summary+json"),
            sideEffectLevel = InteropSideEffectLevel.READ,
            dataSensitivity = InteropDataSensitivity.SENSITIVE,
            boundedness = InteropBoundedness.BOUNDED,
            approvalRequirement = InteropApprovalRequirement.HOST_POLICY,
            availability = InteropAvailabilityStatus.DEGRADED,
            availabilityMessage = "permission_unavailable",
        )

        val result = InteropContractValidator.validate(descriptor)

        assertTrue(result.isValid)
        assertEquals(InteropSideEffectLevel.READ, descriptor.sideEffectLevel)
        assertEquals(InteropDataSensitivity.SENSITIVE, descriptor.dataSensitivity)
        assertEquals(InteropBoundedness.BOUNDED, descriptor.boundedness)
        assertEquals(InteropAvailabilityStatus.DEGRADED, descriptor.availability)
    }

    @Test
    fun grantTaskAndArtifactDescriptorsExposeLifecycleFields() {
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
            summary = "Completed",
            lifecycleState = InteropTaskLifecycleState.COMPLETED,
            availability = InteropAvailabilityStatus.AVAILABLE,
            createdAtEpochMillis = 1_000L,
            updatedAtEpochMillis = 2_000L,
            expiresAtEpochMillis = 3_000L,
        )
        val artifact = InteropArtifactDescriptor(
            handle = InteropHandle(InteropHandleFamily.ARTIFACT, "artifact-1"),
            displayName = "Calendar Summary",
            mimeType = "application/json",
            accessMode = ArtifactAccessMode.READ_ONLY,
            artifactType = "calendar.summary",
            lifecycleState = InteropArtifactLifecycleState.AVAILABLE,
            availability = InteropAvailabilityStatus.AVAILABLE,
            createdAtEpochMillis = 2_000L,
            expiresAtEpochMillis = 3_000L,
        )

        assertTrue(InteropContractValidator.validate(grant).isValid)
        assertTrue(InteropContractValidator.validate(task).isValid)
        assertTrue(InteropContractValidator.validate(artifact).isValid)
        assertEquals(InteropGrantState.PENDING, grant.state)
        assertEquals(InteropTaskLifecycleState.COMPLETED, task.lifecycleState)
        assertEquals(InteropArtifactLifecycleState.AVAILABLE, artifact.lifecycleState)
    }
}
