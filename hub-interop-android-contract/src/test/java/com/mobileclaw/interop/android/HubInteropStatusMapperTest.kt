package com.mobileclaw.interop.android

import com.mobileclaw.interop.contract.CompatibilityReasonCode
import com.mobileclaw.interop.contract.CompatibilityState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HubInteropStatusMapperTest {
    @Test
    fun downgradedCompatibilityKeepsOkStatusAndSignalCarriesDiagnostic() {
        val signal = InteropCompatibilitySignal(
            compatibilityState = CompatibilityState.DOWNGRADED,
            supportedVersion = "1.0",
            reasonCode = CompatibilityReasonCode.MINOR_VERSION_DOWNGRADED,
        )

        assertEquals(
            HubInteropStatus.OK,
            HubInteropStatusMapper.merge(HubInteropStatus.OK, signal),
        )
    }

    @Test
    fun downgradedCompatibilityPreservesAuthorizationRequired() {
        val signal = InteropCompatibilitySignal(
            compatibilityState = CompatibilityState.DOWNGRADED,
            supportedVersion = "1.0",
            reasonCode = CompatibilityReasonCode.MINOR_VERSION_DOWNGRADED,
        )

        assertEquals(
            HubInteropStatus.AUTHORIZATION_REQUIRED,
            HubInteropStatusMapper.merge(HubInteropStatus.AUTHORIZATION_REQUIRED, signal),
        )
    }

    @Test
    fun incompatibleCompatibilityOverridesBaseStatus() {
        val signal = InteropCompatibilitySignal(
            compatibilityState = CompatibilityState.INCOMPATIBLE,
            reasonCode = CompatibilityReasonCode.MAJOR_VERSION_UNSUPPORTED,
        )

        val error = HubInteropStatusMapper.errorFor(
            baseStatus = HubInteropStatus.OK,
            compatibilitySignal = signal,
        )

        assertEquals(HubInteropStatus.INCOMPATIBLE_VERSION, error?.status)
    }

    @Test
    fun supportedCompatibilityDoesNotCreateError() {
        val signal = InteropCompatibilitySignal(
            compatibilityState = CompatibilityState.SUPPORTED,
            reasonCode = CompatibilityReasonCode.SUPPORTED,
        )

        assertNull(
            HubInteropStatusMapper.errorFor(
                baseStatus = HubInteropStatus.OK,
                compatibilitySignal = signal,
            ),
        )
    }

    @Test
    fun defaultMessagesCoverPublicStatusTaxonomy() {
        val statuses = HubInteropStatus.entries.map(HubInteropStatusMapper::defaultMessageFor)

        assertEquals(HubInteropStatus.entries.size, statuses.distinct().size)
        assertEquals("unauthorized", HubInteropStatusMapper.defaultMessageFor(HubInteropStatus.UNAUTHORIZED))
        assertEquals(
            "authorization_pending",
            HubInteropStatusMapper.defaultMessageFor(HubInteropStatus.AUTHORIZATION_PENDING),
        )
        assertEquals("forbidden", HubInteropStatusMapper.defaultMessageFor(HubInteropStatus.FORBIDDEN))
        assertEquals("expired", HubInteropStatusMapper.defaultMessageFor(HubInteropStatus.EXPIRED))
        assertEquals(
            "provider_unavailable",
            HubInteropStatusMapper.defaultMessageFor(HubInteropStatus.PROVIDER_UNAVAILABLE),
        )
        assertEquals(
            "permission_unavailable",
            HubInteropStatusMapper.defaultMessageFor(HubInteropStatus.PERMISSION_UNAVAILABLE),
        )
        assertEquals(
            "execution_failed",
            HubInteropStatusMapper.defaultMessageFor(HubInteropStatus.EXECUTION_FAILED),
        )
    }

    @Test
    fun legacyWireStatusesMapToStableTaxonomy() {
        assertEquals(HubInteropStatus.AUTHORIZATION_PENDING, HubInteropStatus.fromWireName("pending"))
        assertEquals(HubInteropStatus.FORBIDDEN, HubInteropStatus.fromWireName("denied"))
        assertEquals(HubInteropStatus.EXECUTION_FAILED, HubInteropStatus.fromWireName("failed"))
        assertEquals(HubInteropStatus.UNSUPPORTED_CAPABILITY, HubInteropStatus.fromWireName("unsupported"))
        assertEquals(HubInteropStatus.INCOMPATIBLE_VERSION, HubInteropStatus.fromWireName("incompatible"))
    }
}
