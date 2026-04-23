package com.mobileclaw.app.runtime.localchat

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportedLocalModelCatalogCapabilitiesTest {
    @Test
    fun `defaults imported models to text only until capabilities are explicitly enabled`() {
        val capabilities = resolveImportedModelCapabilities(
            manualSupportsImage = null,
            manualSupportsAudio = null,
        )

        assertFalse(capabilities.supportsImage)
        assertFalse(capabilities.supportsAudio)
    }

    @Test
    fun `preserves explicit manual capability overrides`() {
        val capabilities = resolveImportedModelCapabilities(
            manualSupportsImage = true,
            manualSupportsAudio = false,
        )

        assertTrue(capabilities.supportsImage)
        assertFalse(capabilities.supportsAudio)
    }
}
