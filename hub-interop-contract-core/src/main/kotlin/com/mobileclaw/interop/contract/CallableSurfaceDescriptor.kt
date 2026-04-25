package com.mobileclaw.interop.contract

data class CallableSurfaceDescriptor(
    val surfaceId: String,
    val displayName: String,
    val supportedFields: List<String>,
    val supportedScopes: List<String>,
    val supportsAttachments: Boolean,
    val interopVersion: String = InteropVersion.CURRENT.value,
)
