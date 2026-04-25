package com.mobileclaw.interop.probe.client

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.contract.CompatibilityReasonCode
import com.mobileclaw.interop.contract.CompatibilityState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.probe.ProbeStrings
import com.mobileclaw.interop.probe.model.ProbeValidationSeverity
import com.mobileclaw.interop.probe.model.ProbeValidationStep
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CompatibilityInspectorTest {
    private val strings = ProbeStrings(RuntimeEnvironment.getApplication())
    private val subject = CompatibilityInspector(strings)

    @Test
    fun `major version drift is surfaced as error outcome`() {
        val outcome = subject.outcomeForResponse(
            step = ProbeValidationStep.COMPATIBILITY,
            status = HubInteropStatus.INCOMPATIBLE_VERSION,
            compatibilitySignal = InteropCompatibilitySignal(
                interopVersion = "2.0",
                isCompatible = false,
                compatibilityReason = "major_version_unsupported",
                compatibilityState = CompatibilityState.INCOMPATIBLE,
                supportedVersion = "1.0",
                reasonCode = CompatibilityReasonCode.MAJOR_VERSION_UNSUPPORTED,
            ),
            explicitMessage = null,
            successMessage = "unused",
        )

        assertEquals(ProbeValidationSeverity.ERROR, outcome.severity)
        assertEquals("Compatibility", outcome.title)
    }
}
