package com.mobileclaw.app.runtime.memory

data class DataRedactionPolicy(
    val recordId: String,
    val exposurePolicy: MemoryExposurePolicy,
    val allowFullExport: Boolean,
    val allowSummaryExport: Boolean,
    val mustRedactEvidence: Boolean,
    val reason: String,
)
