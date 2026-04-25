package com.mobileclaw.interop.contract

object HubSurfaceDescriptorFactory {
    fun hubSurface(
        surfaceId: String,
        displayName: String,
        summary: String,
        supportedMethods: List<String>,
        capabilities: List<InteropCapabilityDescriptor>,
        authorizationRequirement: InteropAuthorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
        supportsAttachments: Boolean = false,
        tags: List<String> = emptyList(),
    ): HubSurfaceDescriptor {
        return HubSurfaceDescriptor(
            surfaceId = surfaceId,
            displayName = displayName,
            summary = summary,
            supportedMethods = supportedMethods,
            capabilities = capabilities,
            authorizationRequirement = authorizationRequirement,
            supportsAttachments = supportsAttachments,
            tags = tags,
        )
    }

    fun callableSurface(
        surfaceId: String,
        displayName: String,
        supportedFields: List<String>,
        supportedScopes: List<String>,
        supportsAttachments: Boolean,
    ): CallableSurfaceDescriptor {
        return CallableSurfaceDescriptor(
            surfaceId = surfaceId,
            displayName = displayName,
            supportedFields = supportedFields,
            supportedScopes = supportedScopes,
            supportsAttachments = supportsAttachments,
        )
    }
}
