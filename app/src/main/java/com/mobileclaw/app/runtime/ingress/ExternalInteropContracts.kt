package com.mobileclaw.app.runtime.ingress

import com.mobileclaw.app.runtime.multimodal.PendingAttachment

private const val DEFAULT_INTEROP_VERSION = "1.0"

enum class ExternalTrustState {
    TRUSTED,
    UNVERIFIED,
    DENIED,
}

enum class InteropEntryType {
    SHARE_HANDOFF,
    CALLABLE_REQUEST,
}

enum class UriGrantMode {
    NONE,
    READ_ONLY,
    WRITE_CAPABLE,
    MIXED,
    UNKNOWN,
}

data class CallerContractIdentity(
    val originApp: String,
    val packageName: String? = null,
    val sourceLabel: String,
    val trustState: ExternalTrustState,
    val trustReason: String,
    val referrerUri: String? = null,
    val signatureDigest: String? = null,
    val contractVersion: String = DEFAULT_INTEROP_VERSION,
)

data class UriGrantSummary(
    val grantCount: Int,
    val grantedMimeFamilies: List<String>,
    val grantMode: UriGrantMode,
    val expiresWithSession: Boolean,
    val summaryText: String,
) {
    companion object {
        fun none(summaryText: String) = UriGrantSummary(
            grantCount = 0,
            grantedMimeFamilies = emptyList(),
            grantMode = UriGrantMode.NONE,
            expiresWithSession = false,
            summaryText = summaryText,
        )
    }
}

data class InteropCompatibilitySignal(
    val interopVersion: String = DEFAULT_INTEROP_VERSION,
    val isCompatible: Boolean = true,
    val compatibilityReason: String,
    val unknownFieldCount: Int = 0,
)

data class InteropRequestEnvelope(
    val interopRequestId: String,
    val entryType: InteropEntryType,
    val callerIdentity: CallerContractIdentity,
    val sharedText: String,
    val sharedSubject: String? = null,
    val attachments: List<PendingAttachment> = emptyList(),
    val requestedScopes: List<String> = emptyList(),
    val requestedCapabilityId: String? = null,
    val uriGrantSummary: UriGrantSummary,
    val compatibilitySignal: InteropCompatibilitySignal,
    val receivedAtEpochMillis: Long = System.currentTimeMillis(),
    val rawSourceSummary: String = "",
)

data class CallableSurfaceDescriptor(
    val surfaceId: String,
    val displayName: String,
    val supportedFields: List<String>,
    val supportedScopes: List<String>,
    val supportsAttachments: Boolean,
    val interopVersion: String = DEFAULT_INTEROP_VERSION,
)

data class CallableRequestPayload(
    val requestId: String,
    val surfaceId: String,
    val callerIdentity: CallerContractIdentity,
    val userInput: String,
    val attachments: List<PendingAttachment> = emptyList(),
    val requestedScopes: List<String> = emptyList(),
    val requestedCapabilityId: String? = null,
    val unknownFieldCount: Int = 0,
    val contractVersion: String = DEFAULT_INTEROP_VERSION,
)
