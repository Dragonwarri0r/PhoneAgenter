package com.mobileclaw.app.runtime.action

data class PayloadFieldEvidence(
    val fieldName: String,
    val sourceSnippet: String,
    val confidence: Double = 0.7,
)
