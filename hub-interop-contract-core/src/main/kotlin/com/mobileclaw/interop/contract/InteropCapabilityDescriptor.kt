package com.mobileclaw.interop.contract

enum class InteropAuthorizationRequirement {
    NONE,
    USER_CONSENT,
    TRUSTED_APP,
    SYSTEM_ONLY,
}

enum class InteropApprovalRequirement {
    NONE,
    HOST_POLICY,
    USER_APPROVAL,
}

enum class InteropAvailabilityStatus {
    AVAILABLE,
    DEGRADED,
    UNAVAILABLE,
    EXPIRED,
    DELETED,
}

enum class InteropSideEffectLevel {
    NONE,
    READ,
    WRITE,
    DISPATCH,
}

enum class InteropDataSensitivity {
    LOW,
    STANDARD,
    SENSITIVE,
    RESTRICTED,
}

enum class InteropBoundedness {
    BOUNDED,
    HOST_DEFINED,
    UNBOUNDED,
}

data class InteropCapabilityDescriptor(
    val capabilityId: String,
    val displayName: String,
    val summary: String,
    val requiredScopes: List<String> = emptyList(),
    val supportsAttachments: Boolean = false,
    val preferredMethods: List<String> = emptyList(),
    val authorizationRequirement: InteropAuthorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
    val compatibilitySignal: InteropCompatibilitySignal? = null,
    val inputSchemaVersion: String = "1.0",
    val outputArtifactTypes: List<String> = emptyList(),
    val sideEffectLevel: InteropSideEffectLevel = InteropSideEffectLevel.NONE,
    val dataSensitivity: InteropDataSensitivity = InteropDataSensitivity.STANDARD,
    val boundedness: InteropBoundedness = InteropBoundedness.HOST_DEFINED,
    val approvalRequirement: InteropApprovalRequirement = InteropApprovalRequirement.HOST_POLICY,
    val availability: InteropAvailabilityStatus = InteropAvailabilityStatus.AVAILABLE,
    val availabilityMessage: String = "",
)
