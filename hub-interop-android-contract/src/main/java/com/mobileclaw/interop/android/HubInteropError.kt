package com.mobileclaw.interop.android

import com.mobileclaw.interop.contract.InteropCompatibilitySignal

data class HubInteropError(
    val status: HubInteropStatus,
    val message: String,
    val compatibilitySignal: InteropCompatibilitySignal? = null,
) {
    val isRetryable: Boolean = status in setOf(
        HubInteropStatus.AUTHORIZATION_PENDING,
        HubInteropStatus.APPROVAL_REQUIRED,
        HubInteropStatus.PROVIDER_UNAVAILABLE,
        HubInteropStatus.PERMISSION_UNAVAILABLE,
    )
}
