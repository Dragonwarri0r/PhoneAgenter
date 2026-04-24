package com.mobileclaw.interop.contract

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
    val contractVersion: String = InteropVersion.CURRENT.value,
)

data class UriGrantSummary(
    val grantCount: Int,
    val grantedMimeFamilies: List<String>,
    val grantMode: UriGrantMode,
    val expiresWithSession: Boolean,
    val summaryText: String,
) {
    companion object {
        fun none(summaryText: String): UriGrantSummary {
            return UriGrantSummary(
                grantCount = 0,
                grantedMimeFamilies = emptyList(),
                grantMode = UriGrantMode.NONE,
                expiresWithSession = false,
                summaryText = summaryText,
            )
        }
    }
}
