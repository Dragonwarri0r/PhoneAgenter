package com.mobileclaw.interop.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompatibilityEvaluatorTest {
    @Test
    fun exactVersionIsSupported() {
        val result = CompatibilityEvaluator.evaluate(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
        )

        assertEquals(CompatibilityState.SUPPORTED, result.state)
        assertTrue(result.isCompatible)
        assertEquals(CompatibilityReasonCode.SUPPORTED, result.reasonCode)
    }

    @Test
    fun newerMinorVersionDowngrades() {
        val result = CompatibilityEvaluator.evaluate(
            requestedVersion = "1.3",
            supportedVersion = "1.0",
        )

        assertEquals(CompatibilityState.DOWNGRADED, result.state)
        assertTrue(result.isCompatible)
        assertEquals(CompatibilityReasonCode.MINOR_VERSION_DOWNGRADED, result.reasonCode)
    }

    @Test
    fun mismatchedMajorVersionIsIncompatible() {
        val result = CompatibilityEvaluator.evaluate(
            requestedVersion = "2.0",
            supportedVersion = "1.0",
        )

        assertEquals(CompatibilityState.INCOMPATIBLE, result.state)
        assertFalse(result.isCompatible)
        assertEquals(CompatibilityReasonCode.MAJOR_VERSION_UNSUPPORTED, result.reasonCode)
    }

    @Test
    fun unknownFieldsRemainExplicitlyIncompatible() {
        val result = CompatibilityEvaluator.evaluate(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
            requiredUnknownFieldCount = 2,
        )

        assertEquals(CompatibilityState.INCOMPATIBLE, result.state)
        assertEquals(CompatibilityReasonCode.REQUIRED_UNKNOWN_FIELDS, result.reasonCode)
        assertEquals(2, result.requiredUnknownFieldCount)
    }

    @Test
    fun malformedVersionIsIncompatible() {
        val result = CompatibilityEvaluator.evaluate(
            requestedVersion = "not-a-version",
            supportedVersion = "1.0",
        )

        assertEquals(CompatibilityState.INCOMPATIBLE, result.state)
        assertEquals(CompatibilityReasonCode.MALFORMED_VERSION, result.reasonCode)
    }

    @Test
    fun optionalUnknownFieldsDowngradeButRemainCompatible() {
        val result = CompatibilityEvaluator.evaluate(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
            optionalUnknownFieldCount = 3,
        )

        assertEquals(CompatibilityState.DOWNGRADED, result.state)
        assertTrue(result.isCompatible)
        assertEquals(CompatibilityReasonCode.OPTIONAL_UNKNOWN_FIELDS, result.reasonCode)
        assertEquals(3, result.optionalUnknownFieldCount)
    }

    @Test
    fun extensionNamespaceFieldsRemainSupportedWithDiagnostic() {
        val result = CompatibilityEvaluator.evaluate(
            requestedVersion = "1.0",
            supportedVersion = "1.0",
            extensionFieldCount = 1,
        )

        assertEquals(CompatibilityState.SUPPORTED, result.state)
        assertTrue(result.isCompatible)
        assertEquals(CompatibilityReasonCode.EXTENSION_NAMESPACE_FIELDS, result.reasonCode)
        assertEquals(1, result.extensionFieldCount)
    }
}
