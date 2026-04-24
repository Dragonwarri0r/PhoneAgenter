package com.mobileclaw.interop.android

import com.mobileclaw.interop.contract.CompatibilityReasonCode
import com.mobileclaw.interop.contract.CompatibilityState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal

object HubInteropStatusMapper {
    fun merge(
        baseStatus: HubInteropStatus,
        compatibilitySignal: InteropCompatibilitySignal,
    ): HubInteropStatus {
        return when {
            compatibilitySignal.compatibilityState == CompatibilityState.INCOMPATIBLE ->
                HubInteropStatus.INCOMPATIBLE_VERSION

            else -> baseStatus
        }
    }

    fun errorFor(
        baseStatus: HubInteropStatus,
        compatibilitySignal: InteropCompatibilitySignal,
        fallbackMessage: String = compatibilitySignal.compatibilityReason,
    ): HubInteropError? {
        val resolvedStatus = merge(baseStatus, compatibilitySignal)
        if (resolvedStatus == HubInteropStatus.OK) return null
        return HubInteropError(
            status = resolvedStatus,
            message = fallbackMessage.ifBlank { defaultMessageFor(resolvedStatus, compatibilitySignal) },
            compatibilitySignal = compatibilitySignal,
        )
    }

    fun defaultMessageFor(
        status: HubInteropStatus,
        compatibilitySignal: InteropCompatibilitySignal? = null,
    ): String {
        return when (status) {
            HubInteropStatus.OK -> "ok"
            HubInteropStatus.BAD_REQUEST -> "bad_request"
            HubInteropStatus.UNAUTHORIZED -> "unauthorized"
            HubInteropStatus.AUTHORIZATION_REQUIRED -> "authorization_required"
            HubInteropStatus.AUTHORIZATION_PENDING -> "authorization_pending"
            HubInteropStatus.FORBIDDEN -> "forbidden"
            HubInteropStatus.NOT_FOUND -> "not_found"
            HubInteropStatus.EXPIRED -> "expired"
            HubInteropStatus.INCOMPATIBLE_VERSION -> compatibilitySignal?.compatibilityReason
                ?.takeIf { it.isNotBlank() }
                ?: CompatibilityReasonCode.MAJOR_VERSION_UNSUPPORTED.wireValue
            HubInteropStatus.UNSUPPORTED_CAPABILITY -> "unsupported_capability"
            HubInteropStatus.PROVIDER_UNAVAILABLE -> "provider_unavailable"
            HubInteropStatus.PERMISSION_UNAVAILABLE -> "permission_unavailable"
            HubInteropStatus.POLICY_DENIED -> "policy_denied"
            HubInteropStatus.APPROVAL_REQUIRED -> "approval_required"
            HubInteropStatus.APPROVAL_REJECTED -> "approval_rejected"
            HubInteropStatus.EXECUTION_FAILED -> "execution_failed"
            HubInteropStatus.INTERNAL_ERROR -> "internal_error"
        }
    }
}
