package com.mobileclaw.app.runtime.localchat

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportedLocalModelCatalogInferenceTest {
    @Test
    fun `recognizes common vision language model names`() {
        val capabilities = inferModelCapabilities(
            displayName = "Qwen2.5-VL-3B-Instruct",
            originalFileName = "qwen2.5-vl-3b-instruct.litertlm",
        )

        assertTrue(capabilities.supportsImage)
        assertFalse(capabilities.supportsAudio)
    }

    @Test
    fun `recognizes gemma 3n as multimodal`() {
        val capabilities = inferModelCapabilities(
            displayName = "Gemma-3n-E4B-it",
            originalFileName = "gemma-3n-e4b-it-int4.litertlm",
        )

        assertTrue(capabilities.supportsImage)
        assertTrue(capabilities.supportsAudio)
    }

    @Test
    fun `keeps plain text model names text only`() {
        val capabilities = inferModelCapabilities(
            displayName = "Llama 3.2 3B Instruct",
            originalFileName = "llama-3.2-3b-instruct.litertlm",
        )

        assertFalse(capabilities.supportsImage)
        assertFalse(capabilities.supportsAudio)
    }
}
