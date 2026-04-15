package com.mobileclaw.app.runtime.capability

data class ProviderDescriptor(
    val providerId: String,
    val capabilityId: String,
    val providerType: ProviderType,
    val priority: Int,
    val requiredScopes: List<String>,
    val availability: CapabilityAvailability,
    val providerApp: String,
    val providerLabel: String,
    val minimumMemorySchemaVersion: Int = 1,
    val portabilityModes: List<String> = emptyList(),
    val routeMetadata: Map<String, String> = emptyMap(),
    val executorProviderId: String,
)
