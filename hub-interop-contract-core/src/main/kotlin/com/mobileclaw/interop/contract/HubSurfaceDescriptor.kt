package com.mobileclaw.interop.contract

data class HubSurfaceDescriptor(
    val surfaceId: String,
    val displayName: String,
    val summary: String,
    val contractVersion: String = InteropVersion.CURRENT.value,
    val supportedMethods: List<String> = emptyList(),
    val capabilities: List<InteropCapabilityDescriptor> = emptyList(),
    val authorizationRequirement: InteropAuthorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
    val supportsAttachments: Boolean = false,
    val tags: List<String> = emptyList(),
)
