package com.mobileclaw.app.runtime.systemsource

enum class SystemSourceId {
    CONTACTS,
    CALENDAR,
}

data class SystemSourceDescriptor(
    val sourceId: SystemSourceId,
    val displayName: String,
    val permissionName: String,
    val isGranted: Boolean,
    val availabilitySummary: String,
)

data class SystemSourceIngestionResult(
    val sourceId: SystemSourceId,
    val recordsWritten: Int,
    val recordsSkipped: Int,
    val statusMessage: String,
)

data class SystemSourceContribution(
    val sourceId: SystemSourceId,
    val displayName: String,
    val recordCount: Int,
    val summary: String,
)

data class SystemSourceIngestionBundle(
    val descriptors: List<SystemSourceDescriptor>,
    val results: List<SystemSourceIngestionResult>,
    val contributions: List<SystemSourceContribution>,
)
