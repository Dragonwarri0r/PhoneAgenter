package com.mobileclaw.app.runtime.interop

import com.mobileclaw.app.runtime.capability.CallerIdentity
import com.mobileclaw.app.runtime.capability.CallerTrustState
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.ExternalTrustState

data class InteropCallerContext(
    val callerId: String,
    val originApp: String,
    val displayLabel: String,
    val trustState: CallerTrustState,
    val trustReason: String,
    val packageName: String?,
    val signatureDigest: String?,
    val referrerUri: String?,
    val contractVersion: String,
) {
    fun asCallerIdentity(
        requestedScopeIds: List<String>,
        grantSummary: String = "",
    ): CallerIdentity {
        return CallerIdentity(
            callerId = callerId,
            originApp = originApp,
            sourceLabel = displayLabel,
            trustState = trustState,
            trustReason = trustReason,
            allowsRestrictedCapabilities = trustState != CallerTrustState.DENIED,
            packageName = packageName,
            signatureDigest = signatureDigest,
            referrerUri = referrerUri,
            contractVersion = contractVersion,
            grantSummary = grantSummary,
            requestedScopeIds = requestedScopeIds,
        )
    }

    companion object {
        fun from(identity: CallerContractIdentity): InteropCallerContext {
            return InteropCallerContext(
                callerId = identity.packageName ?: identity.originApp,
                originApp = identity.originApp,
                displayLabel = identity.sourceLabel.ifBlank {
                    identity.packageName ?: identity.originApp
                },
                trustState = when (identity.trustState) {
                    ExternalTrustState.TRUSTED -> CallerTrustState.TRUSTED
                    ExternalTrustState.UNVERIFIED -> CallerTrustState.UNVERIFIED
                    ExternalTrustState.DENIED -> CallerTrustState.DENIED
                },
                trustReason = identity.trustReason,
                packageName = identity.packageName,
                signatureDigest = identity.signatureDigest,
                referrerUri = identity.referrerUri,
                contractVersion = identity.contractVersion,
            )
        }
    }
}
