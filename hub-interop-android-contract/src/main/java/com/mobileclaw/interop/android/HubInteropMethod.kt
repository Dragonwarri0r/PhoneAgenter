package com.mobileclaw.interop.android

enum class HubInteropMethod(val wireName: String) {
    DISCOVER_SURFACE("discover_surface"),
    REQUEST_AUTHORIZATION("request_authorization"),
    GET_GRANT_STATUS("get_grant_status"),
    INVOKE_CAPABILITY("invoke_capability"),
    GET_TASK("get_task"),
    GET_ARTIFACT("get_artifact"),
    REVOKE_GRANT("revoke_grant");

    companion object {
        fun fromWireName(raw: String?): HubInteropMethod? {
            return entries.firstOrNull { it.wireName == raw }
        }
    }
}
