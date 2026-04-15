package com.mobileclaw.app.runtime.memory

data class ExportBundle(
    val bundleId: String,
    val recordId: String,
    val logicalRecordId: String,
    val exportMode: ExportMode,
    val payloadText: String,
    val includedFields: List<String>,
    val redactedFields: List<String>,
    val exposurePolicy: MemoryExposurePolicy,
    val syncPolicy: MemorySyncPolicy,
    val generatedAtEpochMillis: Long,
)
