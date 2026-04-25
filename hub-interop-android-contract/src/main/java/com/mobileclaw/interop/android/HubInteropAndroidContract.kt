package com.mobileclaw.interop.android

object HubInteropAndroidContract {
    const val AUTHORITY_SUFFIX: String = "hubinterop"

    object Paths {
        const val SURFACE: String = "surface"
        const val GRANTS: String = "grants"
        const val TASKS: String = "tasks"
        const val ARTIFACTS: String = "artifacts"
    }

    object BundleKeys {
        const val REQUEST_ID: String = "request_id"
        const val CALLER_IDENTITY: String = "caller_identity"
        const val COMPATIBILITY_SIGNAL: String = "compatibility_signal"
        const val STATUS: String = "status"
        const val MESSAGE: String = "message"
        const val CONTRACT_VERSION: String = "contract_version"
        const val CAPABILITY_ID: String = "capability_id"
        const val INPUT: String = "input"
        const val SUBJECT: String = "subject"
        const val REQUESTED_SCOPES: String = "requested_scopes"
        const val SURFACE_DESCRIPTOR: String = "surface_descriptor"
        const val GRANT_DESCRIPTOR: String = "grant_descriptor"
        const val TASK_DESCRIPTOR: String = "task_descriptor"
        const val ARTIFACT_DESCRIPTOR: String = "artifact_descriptor"
        const val HANDLE: String = "handle"
    }

    object Adapter {
        const val BASELINE_PROVIDER: String = "baseline_provider"
        const val APP_FUNCTIONS: String = "app_functions"
    }

    object AppFunctions {
        const val DRAFT_REPLY: String = "draftReply"
        const val EXPORT_PORTABILITY_SUMMARY: String = "exportPortableSummary"
    }

    fun authorityFor(packageName: String): String {
        val normalized = packageName.trim().trim('.')
        return "$normalized.$AUTHORITY_SUFFIX"
    }
}
