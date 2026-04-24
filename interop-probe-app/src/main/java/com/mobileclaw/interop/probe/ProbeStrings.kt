package com.mobileclaw.interop.probe

import android.content.Context
import androidx.annotation.StringRes
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.contract.InteropAuthorizationRequirement
import com.mobileclaw.interop.contract.CompatibilityState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropTaskStatus
import com.mobileclaw.interop.probe.model.ProbeValidationStep

class ProbeStrings(
    private val context: Context,
) {
    fun get(@StringRes resId: Int, vararg args: Any): String = context.getString(resId, *args)

    fun stepLabel(step: ProbeValidationStep): String = get(
        when (step) {
            ProbeValidationStep.DISCOVERY -> R.string.probe_step_discovery
            ProbeValidationStep.AUTHORIZATION -> R.string.probe_step_authorization
            ProbeValidationStep.INVOCATION -> R.string.probe_step_invocation
            ProbeValidationStep.TASK -> R.string.probe_step_task
            ProbeValidationStep.ARTIFACT -> R.string.probe_step_artifact
            ProbeValidationStep.COMPATIBILITY -> R.string.probe_step_compatibility
        },
    )

    fun hubStatusLabel(status: HubInteropStatus): String = get(
        when (status) {
            HubInteropStatus.OK -> R.string.probe_status_ok
            HubInteropStatus.BAD_REQUEST -> R.string.probe_status_bad_request
            HubInteropStatus.UNAUTHORIZED -> R.string.probe_status_unauthorized
            HubInteropStatus.AUTHORIZATION_REQUIRED -> R.string.probe_status_authorization_required
            HubInteropStatus.AUTHORIZATION_PENDING -> R.string.probe_status_authorization_pending
            HubInteropStatus.FORBIDDEN -> R.string.probe_status_forbidden
            HubInteropStatus.NOT_FOUND -> R.string.probe_status_not_found
            HubInteropStatus.EXPIRED -> R.string.probe_status_expired
            HubInteropStatus.INCOMPATIBLE_VERSION -> R.string.probe_status_incompatible_version
            HubInteropStatus.UNSUPPORTED_CAPABILITY -> R.string.probe_status_unsupported_capability
            HubInteropStatus.PROVIDER_UNAVAILABLE -> R.string.probe_status_provider_unavailable
            HubInteropStatus.PERMISSION_UNAVAILABLE -> R.string.probe_status_permission_unavailable
            HubInteropStatus.POLICY_DENIED -> R.string.probe_status_policy_denied
            HubInteropStatus.APPROVAL_REQUIRED -> R.string.probe_status_approval_required
            HubInteropStatus.APPROVAL_REJECTED -> R.string.probe_status_approval_rejected
            HubInteropStatus.EXECUTION_FAILED -> R.string.probe_status_execution_failed
            HubInteropStatus.INTERNAL_ERROR -> R.string.probe_status_internal_error
        },
    )

    fun compatibilityStateLabel(state: CompatibilityState): String = get(
        when (state) {
            CompatibilityState.SUPPORTED -> R.string.probe_compatibility_supported
            CompatibilityState.DOWNGRADED -> R.string.probe_compatibility_downgraded
            CompatibilityState.INCOMPATIBLE -> R.string.probe_compatibility_incompatible
        },
    )

    fun taskStatusLabel(status: InteropTaskStatus): String = get(
        when (status) {
            InteropTaskStatus.PENDING -> R.string.probe_task_status_pending
            InteropTaskStatus.RUNNING -> R.string.probe_task_status_running
            InteropTaskStatus.INPUT_REQUIRED -> R.string.probe_task_status_input_required
            InteropTaskStatus.COMPLETED -> R.string.probe_task_status_completed
            InteropTaskStatus.FAILED -> R.string.probe_task_status_failed
            InteropTaskStatus.CANCELLED -> R.string.probe_task_status_cancelled
        },
    )

    fun authorizationRequirementLabel(requirement: InteropAuthorizationRequirement): String = get(
        when (requirement) {
            InteropAuthorizationRequirement.NONE -> R.string.probe_auth_requirement_none
            InteropAuthorizationRequirement.USER_CONSENT -> R.string.probe_auth_requirement_user_consent
            InteropAuthorizationRequirement.TRUSTED_APP -> R.string.probe_auth_requirement_trusted_app
            InteropAuthorizationRequirement.SYSTEM_ONLY -> R.string.probe_auth_requirement_system_only
        },
    )

    fun hostUnavailable(hostPackageName: String): String {
        return get(R.string.probe_host_unavailable, hostPackageName)
    }

    fun emptyResponse(step: ProbeValidationStep): String {
        return get(R.string.probe_empty_response, stepLabel(step))
    }

    fun discoverySuccess(displayName: String): String {
        return get(R.string.probe_discovery_success, displayName)
    }

    fun authorizationRequested(): String {
        return get(R.string.probe_authorization_requested)
    }

    fun authorizationPending(): String {
        return get(R.string.probe_authorization_pending)
    }

    fun authorizationGranted(): String {
        return get(R.string.probe_authorization_granted)
    }

    fun authorizationRevoked(): String {
        return get(R.string.probe_authorization_revoked)
    }

    fun invocationAccepted(): String {
        return get(R.string.probe_invocation_accepted)
    }

    fun taskLoaded(): String {
        return get(R.string.probe_task_loaded)
    }

    fun artifactLoaded(): String {
        return get(R.string.probe_artifact_loaded)
    }

    fun compatibilitySummary(signal: InteropCompatibilitySignal): String {
        return get(
            R.string.probe_compatibility_summary,
            compatibilityStateLabel(signal.compatibilityState),
            signal.supportedVersion,
        )
    }

    fun actionFailed(message: String?): String {
        return message?.takeIf { it.isNotBlank() }
            ?: get(R.string.probe_action_failed_generic)
    }
}
