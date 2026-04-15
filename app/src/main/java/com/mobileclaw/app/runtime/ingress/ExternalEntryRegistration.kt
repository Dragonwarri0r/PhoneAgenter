package com.mobileclaw.app.runtime.ingress

enum class ExternalEntryType {
    ACTIVITY_SHARE,
}

enum class ExternalEntryStatus {
    ENABLED,
    DISABLED,
    DEGRADED,
}

data class ExternalEntryRegistration(
    val entryId: String,
    val entryType: ExternalEntryType,
    val supportedActions: Set<String>,
    val supportedMimeTypes: Set<String>,
    val contentMode: String,
    val requiresUserVisibleLanding: Boolean,
    val status: ExternalEntryStatus,
)
