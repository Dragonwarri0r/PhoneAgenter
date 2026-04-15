package com.mobileclaw.app.runtime.memory

data class PortabilityBundlePreview(
    val memoryId: String,
    val title: String,
    val bundle: ExportBundle,
    val payloadPreview: String,
    val redactionReason: String,
    val compatibilityLines: List<PortabilityCompatibilityLine>,
    val canShare: Boolean,
    val canSwitchToFull: Boolean,
    val canSwitchToSummary: Boolean,
    val bundleDocumentText: String,
)

data class PortabilityCompatibilityLine(
    val title: String,
    val detail: String,
    val isCompatible: Boolean,
)
