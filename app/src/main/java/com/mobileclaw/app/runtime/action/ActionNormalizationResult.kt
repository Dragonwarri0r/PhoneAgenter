package com.mobileclaw.app.runtime.action

data class ActionNormalizationResult(
    val applies: Boolean,
    val payload: StructuredActionPayload? = null,
    val preview: StructuredExecutionPreview? = null,
    val rationale: String = "",
)
