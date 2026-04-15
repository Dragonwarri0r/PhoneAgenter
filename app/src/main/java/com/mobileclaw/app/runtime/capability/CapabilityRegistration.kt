package com.mobileclaw.app.runtime.capability

enum class ProviderType {
    LOCAL,
    APP_FUNCTIONS,
    INTENT,
    SHARE,
    ACCESSIBILITY,
}

enum class CapabilityAvailabilityState {
    AVAILABLE,
    DEGRADED,
    UNAVAILABLE,
    RESTRICTED,
}

enum class ConfirmationPolicy {
    NONE,
    PREVIEW_FIRST,
    REQUIRE_CONFIRMATION,
}

data class CapabilityAvailability(
    val providerId: String,
    val state: CapabilityAvailabilityState,
    val reason: String,
    val checkedAtEpochMillis: Long = System.currentTimeMillis(),
)

data class CapabilityRegistration(
    val capabilityId: String,
    val toolDescriptor: ToolDescriptor,
    val visibility: ToolVisibilitySnapshot,
    val displayName: String,
    val requiredScopes: List<String>,
    val riskLevelHint: String,
    val confirmationPolicy: ConfirmationPolicy,
    val supportedExtensionTypes: List<String> = emptyList(),
    val providerDescriptors: List<ProviderDescriptor>,
    val availability: CapabilityAvailabilityState,
)
