package com.mobileclaw.app.runtime.extension

enum class RuntimeExtensionType {
    INGRESS,
    TOOL_PROVIDER,
    CONTEXT_SOURCE,
    EXPORT,
    IMPORT,
    SYNC_TRANSPORT,
}

enum class ExtensionPrivacyGuarantee {
    PRIVATE_BY_DEFAULT,
    SUMMARY_ONLY,
    EXPLICIT_POLICY_CHECK,
    TRUSTED_CONTEXT_ONLY,
}

enum class ExtensionTrustRequirement {
    NONE,
    TRUSTED_CALLER,
    PRIVILEGED_CONTEXT,
}

enum class ExtensionEnablementState {
    ACTIVE,
    DISABLED,
    DEGRADED,
    INCOMPATIBLE,
}

data class RuntimeExtensionRegistration(
    val extensionId: String,
    val extensionType: RuntimeExtensionType,
    val displayName: String,
    val contributedCapabilities: List<String>,
    val requiredRecordFields: List<String>,
    val requiredRuntimeMetadata: List<String>,
    val privacyGuarantee: ExtensionPrivacyGuarantee,
    val defaultEnablementState: ExtensionEnablementState,
    val trustRequirement: ExtensionTrustRequirement,
    val compatibilityVersionRange: IntRange = 1..1,
)

data class ExtensionCompatibilityReport(
    val extensionId: String,
    val displayName: String,
    val extensionType: RuntimeExtensionType,
    val isCompatible: Boolean,
    val reason: String,
    val missingFields: List<String> = emptyList(),
    val missingRuntimeMetadata: List<String> = emptyList(),
    val runtimeVersionSatisfied: Boolean = true,
)

data class ExtensionContributionSummary(
    val extensionId: String,
    val displayName: String,
    val extensionType: RuntimeExtensionType,
    val capabilitySummary: String,
    val privacySummary: String,
    val statusSummary: String,
    val enablementState: ExtensionEnablementState,
)

object DefaultRuntimeExtensionRegistrations {
    fun seeded(): List<RuntimeExtensionRegistration> = listOf(
        RuntimeExtensionRegistration(
            extensionId = "ingress.share.handoff",
            extensionType = RuntimeExtensionType.INGRESS,
            displayName = "Share Handoff Ingress",
            contributedCapabilities = listOf("external.share.handoff", "attachment.import"),
            requiredRecordFields = emptyList(),
            requiredRuntimeMetadata = listOf("interop.v1", "activity_share"),
            privacyGuarantee = ExtensionPrivacyGuarantee.PRIVATE_BY_DEFAULT,
            defaultEnablementState = ExtensionEnablementState.ACTIVE,
            trustRequirement = ExtensionTrustRequirement.NONE,
        ),
        RuntimeExtensionRegistration(
            extensionId = "provider.local.reply",
            extensionType = RuntimeExtensionType.TOOL_PROVIDER,
            displayName = "Local Reply Provider",
            contributedCapabilities = listOf("generate.reply"),
            requiredRecordFields = emptyList(),
            requiredRuntimeMetadata = listOf("tool_contract.v1", "provider.local"),
            privacyGuarantee = ExtensionPrivacyGuarantee.PRIVATE_BY_DEFAULT,
            defaultEnablementState = ExtensionEnablementState.ACTIVE,
            trustRequirement = ExtensionTrustRequirement.NONE,
        ),
        RuntimeExtensionRegistration(
            extensionId = "context.contacts.system",
            extensionType = RuntimeExtensionType.CONTEXT_SOURCE,
            displayName = "Contacts Context Source",
            contributedCapabilities = listOf("contacts.read", "system_source.contacts"),
            requiredRecordFields = emptyList(),
            requiredRuntimeMetadata = listOf("system_source.contacts"),
            privacyGuarantee = ExtensionPrivacyGuarantee.TRUSTED_CONTEXT_ONLY,
            defaultEnablementState = ExtensionEnablementState.DEGRADED,
            trustRequirement = ExtensionTrustRequirement.PRIVILEGED_CONTEXT,
        ),
        RuntimeExtensionRegistration(
            extensionId = "context.calendar.system",
            extensionType = RuntimeExtensionType.CONTEXT_SOURCE,
            displayName = "Calendar Context Source",
            contributedCapabilities = listOf("calendar.read", "system_source.calendar"),
            requiredRecordFields = emptyList(),
            requiredRuntimeMetadata = listOf("system_source.calendar"),
            privacyGuarantee = ExtensionPrivacyGuarantee.TRUSTED_CONTEXT_ONLY,
            defaultEnablementState = ExtensionEnablementState.DEGRADED,
            trustRequirement = ExtensionTrustRequirement.PRIVILEGED_CONTEXT,
        ),
        RuntimeExtensionRegistration(
            extensionId = "export.summary.bundle",
            extensionType = RuntimeExtensionType.EXPORT,
            displayName = "Summary Bundle Export",
            contributedCapabilities = listOf("export.summary"),
            requiredRecordFields = listOf(
                "logicalRecordId",
                "summaryText",
                "exposurePolicy",
                "syncPolicy",
                "schemaVersion",
            ),
            requiredRuntimeMetadata = listOf("portability.v1"),
            privacyGuarantee = ExtensionPrivacyGuarantee.SUMMARY_ONLY,
            defaultEnablementState = ExtensionEnablementState.ACTIVE,
            trustRequirement = ExtensionTrustRequirement.NONE,
        ),
        RuntimeExtensionRegistration(
            extensionId = "import.portability.bundle",
            extensionType = RuntimeExtensionType.IMPORT,
            displayName = "Portability Bundle Import",
            contributedCapabilities = listOf("import.summary", "import.full"),
            requiredRecordFields = listOf("logicalRecordId", "schemaVersion"),
            requiredRuntimeMetadata = listOf("portability.v1"),
            privacyGuarantee = ExtensionPrivacyGuarantee.EXPLICIT_POLICY_CHECK,
            defaultEnablementState = ExtensionEnablementState.DISABLED,
            trustRequirement = ExtensionTrustRequirement.TRUSTED_CALLER,
        ),
        RuntimeExtensionRegistration(
            extensionId = "sync.transport.summary",
            extensionType = RuntimeExtensionType.SYNC_TRANSPORT,
            displayName = "Summary Sync Transport",
            contributedCapabilities = listOf("sync.summary"),
            requiredRecordFields = listOf(
                "logicalRecordId",
                "logicalVersion",
                "originDeviceId",
                "originUserId",
                "syncPolicy",
                "schemaVersion",
            ),
            requiredRuntimeMetadata = listOf("sync_transport.v1"),
            privacyGuarantee = ExtensionPrivacyGuarantee.SUMMARY_ONLY,
            defaultEnablementState = ExtensionEnablementState.DISABLED,
            trustRequirement = ExtensionTrustRequirement.TRUSTED_CALLER,
        ),
        RuntimeExtensionRegistration(
            extensionId = "provider.portability.full",
            extensionType = RuntimeExtensionType.TOOL_PROVIDER,
            displayName = "Full Portability Provider",
            contributedCapabilities = listOf("export.full", "preview.portability"),
            requiredRecordFields = listOf(
                "logicalRecordId",
                "contentText",
                "summaryText",
                "exposurePolicy",
                "syncPolicy",
                "schemaVersion",
            ),
            requiredRuntimeMetadata = listOf("tool_contract.v1", "portability.v1"),
            privacyGuarantee = ExtensionPrivacyGuarantee.EXPLICIT_POLICY_CHECK,
            defaultEnablementState = ExtensionEnablementState.DISABLED,
            trustRequirement = ExtensionTrustRequirement.NONE,
        ),
    )
}
