package com.mobileclaw.app.runtime.contribution

enum class RuntimeContributionType {
    LIFECYCLE,
    CONTEXT,
    MIXED,
}

enum class ContributionLifecyclePoint {
    INGRESS,
    PLANNING,
    CONTEXT_ATTACH,
    APPROVAL,
    EXECUTION,
    REFLECTION,
}

enum class RuntimeContributionAvailabilityState {
    ENABLED,
    DISABLED,
    DEGRADED,
    INCOMPATIBLE,
}

enum class ContributionOutcomeState {
    APPLIED,
    SKIPPED,
    DEGRADED,
    BLOCKED,
    UNAVAILABLE,
}

data class ContributionEligibilityProfile(
    val requiredTrustState: String = "",
    val requiredScopes: List<String> = emptyList(),
    val requiredDependencies: List<String> = emptyList(),
    val privacyNotes: String = "",
    val policyNotes: String = "",
    val unavailableReason: String = "",
)

data class RuntimeContributionRegistration(
    val contributionId: String,
    val displayName: String,
    val contributionType: RuntimeContributionType,
    val lifecyclePoints: Set<ContributionLifecyclePoint>,
    val summaryTemplate: String,
    val eligibilityProfile: ContributionEligibilityProfile,
    val defaultAvailabilityState: RuntimeContributionAvailabilityState,
    val supportsAvailabilityChange: Boolean = false,
)

data class ContextContribution(
    val contributionId: String,
    val summary: String,
    val provenanceLabel: String,
    val scopeLabel: String,
    val privacyLabel: String,
    val attachedAtLifecyclePoint: ContributionLifecyclePoint,
    val isRemovable: Boolean = false,
)

data class ContributionOutcomeRecord(
    val contributionId: String,
    val requestId: String,
    val lifecyclePoint: ContributionLifecyclePoint,
    val outcomeState: ContributionOutcomeState,
    val summary: String,
    val details: String = "",
    val policyReason: String = "",
    val provenanceSummary: String = "",
)

data class ContributionGovernanceDetails(
    val trustSummary: String = "",
    val scopeSummary: String = "",
    val privacySummary: String = "",
    val policySummary: String = "",
    val dependencySummary: String = "",
    val limitationSummary: String = "",
)
