package com.mobileclaw.interop.android

import com.mobileclaw.interop.contract.CompatibilityReasonCode
import com.mobileclaw.interop.contract.CompatibilityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CompatibilityBundleAdapterTest {
    @Test
    fun supportedSignalRoundTripsThroughBundle() {
        val signal = CompatibilityBundleAdapter.supportedSignal(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
        )

        val restored = CompatibilityBundleAdapter.fromBundle(
            CompatibilityBundleAdapter.toBundle(signal),
        )

        assertEquals(signal, restored)
        assertTrue(restored?.isCompatible == true)
    }

    @Test
    fun evaluateSignalMarksDowngradeWhenMinorIsNewer() {
        val signal = CompatibilityBundleAdapter.evaluateSignal(
            requestedVersion = "1.2",
            supportedVersion = "1.0",
        )

        assertEquals(CompatibilityState.DOWNGRADED, signal.compatibilityState)
        assertEquals(CompatibilityReasonCode.MINOR_VERSION_DOWNGRADED, signal.reasonCode)
    }

    @Test
    fun evaluateSignalMarksUnknownFieldsAsIncompatible() {
        val signal = CompatibilityBundleAdapter.evaluateSignal(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
            requiredUnknownFieldCount = 1,
        )

        assertEquals(CompatibilityState.INCOMPATIBLE, signal.compatibilityState)
        assertEquals(CompatibilityReasonCode.REQUIRED_UNKNOWN_FIELDS, signal.reasonCode)
    }

    @Test
    fun optionalUnknownFieldDiagnosticsRoundTripThroughBundle() {
        val signal = CompatibilityBundleAdapter.evaluateSignal(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
            optionalUnknownFieldCount = 2,
        )

        val restored = CompatibilityBundleAdapter.fromBundle(
            CompatibilityBundleAdapter.toBundle(signal),
        )

        assertEquals(CompatibilityState.DOWNGRADED, restored?.compatibilityState)
        assertEquals(CompatibilityReasonCode.OPTIONAL_UNKNOWN_FIELDS, restored?.reasonCode)
        assertEquals(2, restored?.optionalUnknownFieldCount)
        assertEquals(0, restored?.requiredUnknownFieldCount)
    }

    @Test
    fun extensionNamespaceDiagnosticsRoundTripThroughBundle() {
        val signal = CompatibilityBundleAdapter.evaluateSignal(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
            extensionFieldCount = 1,
        )

        val restored = CompatibilityBundleAdapter.fromBundle(
            CompatibilityBundleAdapter.toBundle(signal),
        )

        assertEquals(CompatibilityState.SUPPORTED, restored?.compatibilityState)
        assertEquals(CompatibilityReasonCode.EXTENSION_NAMESPACE_FIELDS, restored?.reasonCode)
        assertEquals(1, restored?.extensionFieldCount)
    }
}
