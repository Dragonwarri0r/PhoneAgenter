package com.mobileclaw.interop.android

enum class HubInteropStatus(val wireName: String) {
    OK("ok"),
    BAD_REQUEST("bad_request"),
    UNAUTHORIZED("unauthorized"),
    AUTHORIZATION_REQUIRED("authorization_required"),
    AUTHORIZATION_PENDING("authorization_pending"),
    FORBIDDEN("forbidden"),
    NOT_FOUND("not_found"),
    EXPIRED("expired"),
    INCOMPATIBLE_VERSION("incompatible_version"),
    UNSUPPORTED_CAPABILITY("unsupported_capability"),
    PROVIDER_UNAVAILABLE("provider_unavailable"),
    PERMISSION_UNAVAILABLE("permission_unavailable"),
    POLICY_DENIED("policy_denied"),
    APPROVAL_REQUIRED("approval_required"),
    APPROVAL_REJECTED("approval_rejected"),
    EXECUTION_FAILED("execution_failed"),
    INTERNAL_ERROR("internal_error");

    companion object {
        fun fromWireName(raw: String?): HubInteropStatus? {
            return entries.firstOrNull { it.wireName == raw }
                ?: legacyAliases[raw]
        }

        private val legacyAliases: Map<String?, HubInteropStatus> = mapOf(
            "pending" to AUTHORIZATION_PENDING,
            "incompatible" to INCOMPATIBLE_VERSION,
            "denied" to FORBIDDEN,
            "failed" to EXECUTION_FAILED,
            "unsupported" to UNSUPPORTED_CAPABILITY,
        )
    }
}
