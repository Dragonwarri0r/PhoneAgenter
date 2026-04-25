package com.mobileclaw.interop.android

import android.content.Context
import com.mobileclaw.interop.android.R
import com.mobileclaw.interop.contract.CompatibilityState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal

object HubInteropAndroidStrings {
    fun statusLabel(context: Context, status: HubInteropStatus): String {
        return context.getString(
            when (status) {
                HubInteropStatus.OK -> R.string.hub_interop_status_ok
                HubInteropStatus.BAD_REQUEST -> R.string.hub_interop_status_bad_request
                HubInteropStatus.UNAUTHORIZED -> R.string.hub_interop_status_unauthorized
                HubInteropStatus.AUTHORIZATION_REQUIRED -> R.string.hub_interop_status_authorization_required
                HubInteropStatus.AUTHORIZATION_PENDING -> R.string.hub_interop_status_authorization_pending
                HubInteropStatus.FORBIDDEN -> R.string.hub_interop_status_forbidden
                HubInteropStatus.NOT_FOUND -> R.string.hub_interop_status_not_found
                HubInteropStatus.EXPIRED -> R.string.hub_interop_status_expired
                HubInteropStatus.INCOMPATIBLE_VERSION -> R.string.hub_interop_status_incompatible_version
                HubInteropStatus.UNSUPPORTED_CAPABILITY -> R.string.hub_interop_status_unsupported_capability
                HubInteropStatus.PROVIDER_UNAVAILABLE -> R.string.hub_interop_status_provider_unavailable
                HubInteropStatus.PERMISSION_UNAVAILABLE -> R.string.hub_interop_status_permission_unavailable
                HubInteropStatus.POLICY_DENIED -> R.string.hub_interop_status_policy_denied
                HubInteropStatus.APPROVAL_REQUIRED -> R.string.hub_interop_status_approval_required
                HubInteropStatus.APPROVAL_REJECTED -> R.string.hub_interop_status_approval_rejected
                HubInteropStatus.EXECUTION_FAILED -> R.string.hub_interop_status_execution_failed
                HubInteropStatus.INTERNAL_ERROR -> R.string.hub_interop_status_internal_error
            },
        )
    }

    fun compatibilityLabel(context: Context, signal: InteropCompatibilitySignal): String {
        return context.getString(
            when (signal.compatibilityState) {
                CompatibilityState.SUPPORTED -> R.string.hub_interop_compatibility_supported
                CompatibilityState.DOWNGRADED -> R.string.hub_interop_compatibility_downgraded
                CompatibilityState.INCOMPATIBLE -> R.string.hub_interop_compatibility_incompatible
            },
        )
    }

    fun errorLabel(context: Context, error: HubInteropError): String {
        return context.getString(
            when (error.status) {
                HubInteropStatus.OK -> R.string.hub_interop_error_ok
                HubInteropStatus.BAD_REQUEST -> R.string.hub_interop_error_bad_request
                HubInteropStatus.UNAUTHORIZED -> R.string.hub_interop_error_unauthorized
                HubInteropStatus.AUTHORIZATION_REQUIRED -> R.string.hub_interop_error_authorization_required
                HubInteropStatus.AUTHORIZATION_PENDING -> R.string.hub_interop_error_authorization_pending
                HubInteropStatus.FORBIDDEN -> R.string.hub_interop_error_forbidden
                HubInteropStatus.NOT_FOUND -> R.string.hub_interop_error_not_found
                HubInteropStatus.EXPIRED -> R.string.hub_interop_error_expired
                HubInteropStatus.INCOMPATIBLE_VERSION -> R.string.hub_interop_error_incompatible_version
                HubInteropStatus.UNSUPPORTED_CAPABILITY -> R.string.hub_interop_error_unsupported_capability
                HubInteropStatus.PROVIDER_UNAVAILABLE -> R.string.hub_interop_error_provider_unavailable
                HubInteropStatus.PERMISSION_UNAVAILABLE -> R.string.hub_interop_error_permission_unavailable
                HubInteropStatus.POLICY_DENIED -> R.string.hub_interop_error_policy_denied
                HubInteropStatus.APPROVAL_REQUIRED -> R.string.hub_interop_error_approval_required
                HubInteropStatus.APPROVAL_REJECTED -> R.string.hub_interop_error_approval_rejected
                HubInteropStatus.EXECUTION_FAILED -> R.string.hub_interop_error_execution_failed
                HubInteropStatus.INTERNAL_ERROR -> R.string.hub_interop_error_internal_error
            },
        )
    }
}
