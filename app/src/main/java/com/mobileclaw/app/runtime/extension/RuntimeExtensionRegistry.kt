package com.mobileclaw.app.runtime.extension

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuntimeExtensionRegistry @Inject constructor(
    private val appStrings: AppStrings,
) {
    private val runtimeVersion = 1
    private val availableRuntimeMetadata = setOf(
        "interop.v1",
        "activity_share",
        "tool_contract.v1",
        "provider.local",
        "system_source.contacts",
        "portability.v1",
    )

    fun registrations(): List<RuntimeExtensionRegistration> = DefaultRuntimeExtensionRegistrations.seeded()

    fun discoverySummaries(): List<ExtensionContributionSummary> {
        return registrations().map { registration ->
            val compatibility = evaluateCompatibility(registration)
            val enablement = resolveEnablementState(registration, compatibility)
            ExtensionContributionSummary(
                extensionId = registration.extensionId,
                displayName = registration.displayName,
                extensionType = registration.extensionType,
                capabilitySummary = registration.contributedCapabilities.joinToString(separator = ", "),
                privacySummary = appStrings.extensionPrivacyGuaranteeLabel(registration.privacyGuarantee),
                statusSummary = buildString {
                    append(appStrings.extensionEnablementStateLabel(enablement))
                    append(" · ")
                    append(compatibility.reason)
                },
                enablementState = enablement,
            )
        }
    }

    fun evaluateCompatibility(
        registration: RuntimeExtensionRegistration,
        availableFields: Set<String> = emptySet(),
    ): ExtensionCompatibilityReport {
        val missingFields = registration.requiredRecordFields.filterNot(availableFields::contains)
        val missingRuntimeMetadata = registration.requiredRuntimeMetadata.filterNot(availableRuntimeMetadata::contains)
        val versionSatisfied = runtimeVersion in registration.compatibilityVersionRange
        val isCompatible = missingFields.isEmpty() && missingRuntimeMetadata.isEmpty() && versionSatisfied
        val reason = when {
            !versionSatisfied -> appStrings.get(
                R.string.extension_runtime_version_mismatch,
                registration.compatibilityVersionRange.first,
                registration.compatibilityVersionRange.last,
            )
            missingRuntimeMetadata.isNotEmpty() -> appStrings.get(
                R.string.extension_missing_runtime_metadata,
                missingRuntimeMetadata.joinToString(),
            )
            missingFields.isNotEmpty() -> appStrings.get(
                R.string.extension_missing_record_fields,
                missingFields.joinToString(),
            )
            else -> appStrings.get(R.string.extension_compatible)
        }
        return ExtensionCompatibilityReport(
            extensionId = registration.extensionId,
            displayName = registration.displayName,
            extensionType = registration.extensionType,
            isCompatible = isCompatible,
            reason = reason,
            missingFields = missingFields,
            missingRuntimeMetadata = missingRuntimeMetadata,
            runtimeVersionSatisfied = versionSatisfied,
        )
    }

    private fun resolveEnablementState(
        registration: RuntimeExtensionRegistration,
        compatibility: ExtensionCompatibilityReport,
    ): ExtensionEnablementState {
        if (!compatibility.isCompatible) return ExtensionEnablementState.INCOMPATIBLE
        return registration.defaultEnablementState
    }
}
