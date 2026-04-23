package com.mobileclaw.app.runtime.contribution

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.extension.ExtensionEnablementState
import com.mobileclaw.app.runtime.extension.ExtensionTrustRequirement
import com.mobileclaw.app.runtime.extension.RuntimeExtensionRegistry
import com.mobileclaw.app.runtime.extension.RuntimeExtensionType
import com.mobileclaw.app.runtime.systemsource.SystemSourceId
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class RuntimeContributionRegistry @Inject constructor(
    private val appStrings: AppStrings,
    private val runtimeExtensionRegistry: RuntimeExtensionRegistry,
) {
    companion object {
        const val MEMORY_ACTIVE_CONTEXT_ID = "memory.active_context"
        const val KNOWLEDGE_RETRIEVAL_ID = "knowledge.retrieval_support"
        const val POLICY_EXECUTION_GATE_ID = "policy.execution_gate"
    }

    private val baseAvailabilityOverrides = MutableStateFlow<Map<String, RuntimeContributionAvailabilityState>>(
        emptyMap(),
    )

    val baseAvailability: StateFlow<Map<String, RuntimeContributionAvailabilityState>> =
        baseAvailabilityOverrides.asStateFlow()

    fun registrations(): List<RuntimeContributionRegistration> {
        return listOf(
            memoryContributionRegistration(),
            knowledgeContributionRegistration(),
            policyContributionRegistration(),
        ) + runtimeExtensionRegistry.registrations().map(::adaptExtensionRegistration)
    }

    fun registration(contributionId: String): RuntimeContributionRegistration? {
        return registrations().firstOrNull { it.contributionId == contributionId }
    }

    fun manageableRegistrations(): List<RuntimeContributionRegistration> {
        return registrations().filter { it.supportsAvailabilityChange }
    }

    fun systemSourceContributionId(sourceId: SystemSourceId): String {
        return when (sourceId) {
            SystemSourceId.CONTACTS -> extensionContributionId("context.contacts.system")
            SystemSourceId.CALENDAR -> extensionContributionId("context.calendar.system")
        }
    }

    fun extensionContributionId(extensionId: String): String = "extension:$extensionId"

    fun availabilityState(contributionId: String): RuntimeContributionAvailabilityState {
        baseAvailabilityOverrides.value[contributionId]?.let { return it }
        if (contributionId.startsWith("extension:")) {
            val extensionId = contributionId.removePrefix("extension:")
            return runtimeExtensionRegistry.currentEnablementState(extensionId).toContributionAvailabilityState()
        }
        return registration(contributionId)?.defaultAvailabilityState
            ?: RuntimeContributionAvailabilityState.INCOMPATIBLE
    }

    fun isEnabled(contributionId: String): Boolean {
        return availabilityState(contributionId) !in setOf(
            RuntimeContributionAvailabilityState.DISABLED,
            RuntimeContributionAvailabilityState.INCOMPATIBLE,
        )
    }

    fun toggleAvailability(contributionId: String): RuntimeContributionAvailabilityState? {
        val registration = registration(contributionId) ?: return null
        if (!registration.supportsAvailabilityChange) return null
        if (contributionId.startsWith("extension:")) {
            val extensionId = contributionId.removePrefix("extension:")
            return runtimeExtensionRegistry.toggleEnablementState(extensionId)?.toContributionAvailabilityState()
        }
        val current = availabilityState(contributionId)
        val restoredState = registration.defaultAvailabilityState
        val next = if (current == RuntimeContributionAvailabilityState.DISABLED) {
            restoredState
        } else {
            RuntimeContributionAvailabilityState.DISABLED
        }
        baseAvailabilityOverrides.value = baseAvailabilityOverrides.value + (contributionId to next)
        return next
    }

    fun governanceDetails(contributionId: String): ContributionGovernanceDetails? {
        val registration = registration(contributionId) ?: return null
        val availability = availabilityState(contributionId)
        val extensionCompatibilityReason = extensionCompatibilityReason(contributionId)
        return ContributionGovernanceDetails(
            trustSummary = registration.eligibilityProfile.requiredTrustState,
            scopeSummary = registration.eligibilityProfile.requiredScopes.joinToString(separator = " / "),
            privacySummary = registration.eligibilityProfile.privacyNotes,
            policySummary = registration.eligibilityProfile.policyNotes,
            dependencySummary = registration.eligibilityProfile.requiredDependencies.joinToString(separator = ", "),
            limitationSummary = when {
                availability == RuntimeContributionAvailabilityState.DISABLED ->
                    appStrings.get(R.string.runtime_contribution_disabled_for_request)
                extensionCompatibilityReason.isNotBlank() -> extensionCompatibilityReason
                registration.eligibilityProfile.unavailableReason.isNotBlank() ->
                    registration.eligibilityProfile.unavailableReason
                else -> ""
            },
        )
    }

    private fun memoryContributionRegistration(): RuntimeContributionRegistration {
        return RuntimeContributionRegistration(
            contributionId = MEMORY_ACTIVE_CONTEXT_ID,
            displayName = appStrings.get(R.string.runtime_contribution_memory_title),
            contributionType = RuntimeContributionType.CONTEXT,
            lifecyclePoints = setOf(ContributionLifecyclePoint.CONTEXT_ATTACH),
            summaryTemplate = appStrings.get(R.string.runtime_contribution_memory_summary_template),
            eligibilityProfile = ContributionEligibilityProfile(
                requiredTrustState = appStrings.get(R.string.runtime_contribution_memory_trust_note),
                requiredScopes = listOf(appStrings.get(R.string.runtime_contribution_memory_scope_note)),
                requiredDependencies = listOf(appStrings.get(R.string.runtime_contribution_dependency_memory_store)),
                privacyNotes = appStrings.get(R.string.runtime_contribution_memory_policy_note),
                policyNotes = appStrings.get(R.string.runtime_contribution_memory_request_policy_note),
            ),
            defaultAvailabilityState = RuntimeContributionAvailabilityState.ENABLED,
            supportsAvailabilityChange = true,
        )
    }

    private fun policyContributionRegistration(): RuntimeContributionRegistration {
        return RuntimeContributionRegistration(
            contributionId = POLICY_EXECUTION_GATE_ID,
            displayName = appStrings.get(R.string.runtime_contribution_policy_title),
            contributionType = RuntimeContributionType.LIFECYCLE,
            lifecyclePoints = setOf(ContributionLifecyclePoint.APPROVAL, ContributionLifecyclePoint.EXECUTION),
            summaryTemplate = appStrings.get(R.string.runtime_contribution_policy_summary_template),
            eligibilityProfile = ContributionEligibilityProfile(
                requiredTrustState = appStrings.get(R.string.runtime_contribution_policy_trust_note),
                requiredScopes = listOf(appStrings.get(R.string.runtime_contribution_policy_scope_note)),
                policyNotes = appStrings.get(R.string.runtime_contribution_policy_gate_note),
            ),
            defaultAvailabilityState = RuntimeContributionAvailabilityState.ENABLED,
            supportsAvailabilityChange = false,
        )
    }

    private fun knowledgeContributionRegistration(): RuntimeContributionRegistration {
        return RuntimeContributionRegistration(
            contributionId = KNOWLEDGE_RETRIEVAL_ID,
            displayName = appStrings.get(R.string.runtime_contribution_knowledge_title),
            contributionType = RuntimeContributionType.CONTEXT,
            lifecyclePoints = setOf(ContributionLifecyclePoint.CONTEXT_ATTACH),
            summaryTemplate = appStrings.get(R.string.runtime_contribution_knowledge_summary_template),
            eligibilityProfile = ContributionEligibilityProfile(
                requiredTrustState = appStrings.get(R.string.runtime_contribution_knowledge_trust_note),
                requiredScopes = listOf(appStrings.get(R.string.runtime_contribution_knowledge_scope_note)),
                requiredDependencies = listOf(appStrings.get(R.string.runtime_contribution_dependency_knowledge_store)),
                privacyNotes = appStrings.get(R.string.runtime_contribution_knowledge_privacy_note),
                policyNotes = appStrings.get(R.string.runtime_contribution_knowledge_policy_note),
            ),
            defaultAvailabilityState = RuntimeContributionAvailabilityState.ENABLED,
            supportsAvailabilityChange = false,
        )
    }

    private fun adaptExtensionRegistration(
        registration: com.mobileclaw.app.runtime.extension.RuntimeExtensionRegistration,
    ): RuntimeContributionRegistration {
        return RuntimeContributionRegistration(
            contributionId = extensionContributionId(registration.extensionId),
            displayName = registration.displayName,
            contributionType = when (registration.extensionType) {
                RuntimeExtensionType.CONTEXT_SOURCE -> RuntimeContributionType.CONTEXT
                RuntimeExtensionType.TOOL_PROVIDER,
                RuntimeExtensionType.INGRESS,
                RuntimeExtensionType.EXPORT,
                RuntimeExtensionType.IMPORT,
                RuntimeExtensionType.SYNC_TRANSPORT,
                -> RuntimeContributionType.LIFECYCLE
            },
            lifecyclePoints = when (registration.extensionType) {
                RuntimeExtensionType.INGRESS -> setOf(ContributionLifecyclePoint.INGRESS)
                RuntimeExtensionType.CONTEXT_SOURCE -> setOf(ContributionLifecyclePoint.CONTEXT_ATTACH)
                RuntimeExtensionType.TOOL_PROVIDER -> setOf(
                    ContributionLifecyclePoint.PLANNING,
                    ContributionLifecyclePoint.EXECUTION,
                )
                RuntimeExtensionType.EXPORT,
                RuntimeExtensionType.IMPORT,
                RuntimeExtensionType.SYNC_TRANSPORT,
                -> setOf(ContributionLifecyclePoint.REFLECTION)
            },
            summaryTemplate = registration.contributedCapabilities.joinToString(separator = ", "),
            eligibilityProfile = ContributionEligibilityProfile(
                requiredTrustState = appStrings.extensionTrustRequirementLabel(registration.trustRequirement),
                requiredScopes = listOf(extensionScopeSummary(registration)),
                requiredDependencies = registration.requiredRuntimeMetadata,
                privacyNotes = appStrings.extensionPrivacyGuaranteeLabel(registration.privacyGuarantee),
                policyNotes = extensionPolicySummary(registration),
            ),
            defaultAvailabilityState = runtimeExtensionRegistry.currentEnablementState(
                registration.extensionId,
            ).toContributionAvailabilityState(),
            supportsAvailabilityChange = registration.extensionType == RuntimeExtensionType.CONTEXT_SOURCE,
        )
    }

    private fun extensionScopeSummary(
        registration: com.mobileclaw.app.runtime.extension.RuntimeExtensionRegistration,
    ): String {
        return when (registration.extensionId) {
            "context.contacts.system" -> appStrings.get(R.string.runtime_contribution_scope_contacts_context)
            "context.calendar.system" -> appStrings.get(R.string.runtime_contribution_scope_calendar_context)
            else -> appStrings.get(
                R.string.runtime_contribution_scope_capabilities,
                registration.contributedCapabilities.joinToString(separator = ", "),
            )
        }
    }

    private fun extensionPolicySummary(
        registration: com.mobileclaw.app.runtime.extension.RuntimeExtensionRegistration,
    ): String {
        return when (registration.trustRequirement) {
            ExtensionTrustRequirement.PRIVILEGED_CONTEXT ->
                appStrings.get(R.string.runtime_contribution_permission_required)
            else -> ""
        }
    }

    private fun extensionCompatibilityReason(contributionId: String): String {
        if (!contributionId.startsWith("extension:")) return ""
        val extensionId = contributionId.removePrefix("extension:")
        val registration = runtimeExtensionRegistry.registrations().firstOrNull {
            it.extensionId == extensionId
        } ?: return ""
        val compatibility = runtimeExtensionRegistry.evaluateCompatibility(registration)
        return compatibility.reason.takeIf { !compatibility.isCompatible } ?: ""
    }
}

private fun ExtensionEnablementState.toContributionAvailabilityState(): RuntimeContributionAvailabilityState {
    return when (this) {
        ExtensionEnablementState.ACTIVE -> RuntimeContributionAvailabilityState.ENABLED
        ExtensionEnablementState.DISABLED -> RuntimeContributionAvailabilityState.DISABLED
        ExtensionEnablementState.DEGRADED -> RuntimeContributionAvailabilityState.DEGRADED
        ExtensionEnablementState.INCOMPATIBLE -> RuntimeContributionAvailabilityState.INCOMPATIBLE
    }
}
