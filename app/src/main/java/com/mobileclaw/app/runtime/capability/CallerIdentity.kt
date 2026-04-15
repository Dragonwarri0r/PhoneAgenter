package com.mobileclaw.app.runtime.capability

enum class CallerTrustState {
    TRUSTED,
    UNVERIFIED,
    DENIED,
}

data class CallerIdentity(
    val callerId: String,
    val originApp: String,
    val sourceLabel: String,
    val trustState: CallerTrustState,
    val trustReason: String,
    val allowsRestrictedCapabilities: Boolean,
    val packageName: String? = null,
    val signatureDigest: String? = null,
    val referrerUri: String? = null,
    val contractVersion: String? = null,
    val grantSummary: String = "",
    val requestedScopeIds: List<String> = emptyList(),
)
