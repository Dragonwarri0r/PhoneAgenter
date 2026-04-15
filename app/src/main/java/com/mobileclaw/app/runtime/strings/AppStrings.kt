package com.mobileclaw.app.runtime.strings

import android.content.Context
import androidx.annotation.StringRes
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus
import com.mobileclaw.app.runtime.capability.ProviderType
import com.mobileclaw.app.runtime.capability.ToolSideEffectType
import com.mobileclaw.app.runtime.capability.ToolVisibilityState
import com.mobileclaw.app.runtime.action.PayloadCompletenessState
import com.mobileclaw.app.runtime.action.StructuredActionType
import com.mobileclaw.app.runtime.governance.GovernanceGrantState
import com.mobileclaw.app.runtime.governance.GovernanceTrustMode
import com.mobileclaw.app.runtime.memory.ExportMode
import com.mobileclaw.app.runtime.memory.ExtensionType
import com.mobileclaw.app.runtime.memory.MemoryExposurePolicy
import com.mobileclaw.app.runtime.memory.MemoryLifecycle
import com.mobileclaw.app.runtime.memory.MemoryScope
import com.mobileclaw.app.runtime.memory.MemorySourceType
import com.mobileclaw.app.runtime.memory.MemorySyncPolicy
import com.mobileclaw.app.runtime.persona.ConfirmationStyle
import com.mobileclaw.app.runtime.persona.PersonaProfile
import com.mobileclaw.app.runtime.persona.PersonaVerbosity
import com.mobileclaw.app.runtime.persona.PersonaWarmth
import com.mobileclaw.app.runtime.ingress.ExternalTrustState
import com.mobileclaw.app.runtime.ingress.InteropEntryType
import com.mobileclaw.app.runtime.ingress.UriGrantMode
import com.mobileclaw.app.runtime.extension.ExtensionEnablementState
import com.mobileclaw.app.runtime.extension.ExtensionPrivacyGuarantee
import com.mobileclaw.app.runtime.policy.ActionScope
import com.mobileclaw.app.runtime.policy.ApprovalOutcomeType
import com.mobileclaw.app.runtime.policy.PolicyDecisionType
import com.mobileclaw.app.runtime.policy.RiskLevel
import com.mobileclaw.app.runtime.session.RuntimeStageType
import com.mobileclaw.app.runtime.systemsource.SystemSourceId
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStrings @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun get(@StringRes resId: Int, vararg args: Any): String = context.getString(resId, *args)

    fun localeTag(): String {
        return context.resources.configuration.locales[0]?.toLanguageTag() ?: "en"
    }

    fun runtimeStageLabel(stageType: RuntimeStageType): String = get(
        when (stageType) {
            RuntimeStageType.INGRESS -> R.string.runtime_stage_ingress
            RuntimeStageType.CONTEXT_LOADING -> R.string.runtime_stage_context_loading
            RuntimeStageType.PLANNING -> R.string.runtime_stage_planning
            RuntimeStageType.CAPABILITY_SELECTION -> R.string.runtime_stage_capability_selection
            RuntimeStageType.EXECUTION_GATING -> R.string.runtime_stage_execution_gating
            RuntimeStageType.AWAITING_APPROVAL -> R.string.runtime_stage_awaiting_approval
            RuntimeStageType.EXECUTING -> R.string.runtime_stage_executing
            RuntimeStageType.COMPLETED -> R.string.runtime_stage_completed
            RuntimeStageType.FAILED -> R.string.runtime_stage_failed
            RuntimeStageType.CANCELLED -> R.string.runtime_stage_cancelled
            RuntimeStageType.DENIED -> R.string.runtime_stage_denied
        },
    )

    fun personaSummary(profile: PersonaProfile): String {
        val confirmation = when (profile.confirmationStyle) {
            ConfirmationStyle.ASK_BEFORE_ACTION -> get(R.string.persona_confirmation_before_action)
            ConfirmationStyle.ASK_BEFORE_SCHEDULING -> get(R.string.persona_confirmation_before_scheduling)
            ConfirmationStyle.DIRECT_WHEN_SAFE -> get(R.string.persona_confirmation_direct_when_safe)
        }
        val overcommitment = if (profile.avoidOvercommitment) {
            " ${get(R.string.persona_avoid_overcommitment)}"
        } else {
            ""
        }
        return get(
            R.string.persona_summary_template,
            profile.verbosity.localizedLabel(this),
            profile.warmth.localizedLabel(this),
            confirmation,
            overcommitment,
        )
    }

    fun activeContextHeadline(visibleCount: Int): String {
        return if (visibleCount == 0) {
            get(R.string.active_context_no_memory)
        } else {
            get(R.string.active_context_with_count, visibleCount)
        }
    }

    fun activeContextRetrievalSummary(hiddenPrivateCount: Int, excludedCount: Int): String {
        return when {
            hiddenPrivateCount == 0 && excludedCount == 0 -> get(R.string.active_context_summary_default)
            hiddenPrivateCount > 0 -> get(R.string.active_context_summary_redacted)
            else -> get(R.string.active_context_summary_highest_signal)
        }
    }

    fun modelAvailabilityLabel(status: ModelAvailabilityStatus): String = get(
        when (status) {
            ModelAvailabilityStatus.READY -> R.string.model_status_ready
            ModelAvailabilityStatus.PREPARING -> R.string.model_status_preparing
            ModelAvailabilityStatus.FAILED -> R.string.model_status_failed
            ModelAvailabilityStatus.UNAVAILABLE -> R.string.model_status_unavailable
        },
    )

    fun memoryLifecycleLabel(lifecycle: MemoryLifecycle): String = get(
        when (lifecycle) {
            MemoryLifecycle.DURABLE -> R.string.memory_lifecycle_durable
            MemoryLifecycle.WORKING -> R.string.memory_lifecycle_working
            MemoryLifecycle.EPHEMERAL -> R.string.memory_lifecycle_ephemeral
        },
    )

    fun memoryScopeLabel(scope: MemoryScope): String = get(
        when (scope) {
            MemoryScope.GLOBAL -> R.string.memory_scope_global
            MemoryScope.APP_SCOPED -> R.string.memory_scope_app_scoped
            MemoryScope.CONTACT_SCOPED -> R.string.memory_scope_contact_scoped
            MemoryScope.DEVICE_SCOPED -> R.string.memory_scope_device_scoped
        },
    )

    fun memorySourceLabel(sourceType: MemorySourceType): String = get(
        when (sourceType) {
            MemorySourceType.USER_EDIT -> R.string.memory_source_user_edit
            MemorySourceType.SYSTEM_SOURCE -> R.string.memory_source_system_source
            MemorySourceType.INFERRED -> R.string.memory_source_inferred
            MemorySourceType.RUNTIME_WRITEBACK -> R.string.memory_source_runtime_writeback
        },
    )

    fun memoryExposureLabel(policy: MemoryExposurePolicy): String = get(
        when (policy) {
            MemoryExposurePolicy.PRIVATE -> R.string.memory_exposure_private
            MemoryExposurePolicy.SHAREABLE_SUMMARY -> R.string.memory_exposure_shareable_summary
            MemoryExposurePolicy.SHAREABLE_FULL -> R.string.memory_exposure_shareable_full
        },
    )

    fun memorySyncPolicyLabel(policy: MemorySyncPolicy): String = get(
        when (policy) {
            MemorySyncPolicy.LOCAL_ONLY -> R.string.memory_sync_local_only
            MemorySyncPolicy.SUMMARY_SYNC_READY -> R.string.memory_sync_summary_ready
            MemorySyncPolicy.FULL_SYNC_READY -> R.string.memory_sync_full_ready
        },
    )

    fun exportModeLabel(mode: ExportMode): String = get(
        when (mode) {
            ExportMode.SUMMARY_ONLY -> R.string.memory_export_mode_summary_only
            ExportMode.FULL_RECORD -> R.string.memory_export_mode_full_record
        },
    )

    fun extensionTypeLabel(type: ExtensionType): String = get(
        when (type) {
            ExtensionType.TOOL_PROVIDER -> R.string.memory_extension_type_provider
            ExtensionType.EXPORT -> R.string.memory_extension_type_export
            ExtensionType.IMPORT -> R.string.memory_extension_type_import
            ExtensionType.SYNC_TRANSPORT -> R.string.memory_extension_type_sync_transport
            ExtensionType.INGRESS -> R.string.extension_type_ingress
            ExtensionType.CONTEXT_SOURCE -> R.string.extension_type_context_source
        },
    )

    fun extensionPrivacyGuaranteeLabel(guarantee: ExtensionPrivacyGuarantee): String = get(
        when (guarantee) {
            ExtensionPrivacyGuarantee.PRIVATE_BY_DEFAULT -> R.string.extension_privacy_private_by_default
            ExtensionPrivacyGuarantee.SUMMARY_ONLY -> R.string.extension_privacy_summary_only
            ExtensionPrivacyGuarantee.EXPLICIT_POLICY_CHECK -> R.string.extension_privacy_policy_checked
            ExtensionPrivacyGuarantee.TRUSTED_CONTEXT_ONLY -> R.string.extension_privacy_trusted_context_only
        },
    )

    fun extensionEnablementStateLabel(state: ExtensionEnablementState): String = get(
        when (state) {
            ExtensionEnablementState.ACTIVE -> R.string.extension_enablement_active
            ExtensionEnablementState.DISABLED -> R.string.extension_enablement_disabled
            ExtensionEnablementState.DEGRADED -> R.string.extension_enablement_degraded
            ExtensionEnablementState.INCOMPATIBLE -> R.string.extension_enablement_incompatible
        },
    )

    fun actionScopeLabel(scope: ActionScope): String = get(
        when (scope) {
            ActionScope.REPLY_GENERATE -> R.string.policy_scope_reply_generate
            ActionScope.CALENDAR_READ -> R.string.policy_scope_calendar_read
            ActionScope.MESSAGE_SEND -> R.string.policy_scope_message_send
            ActionScope.CALENDAR_WRITE -> R.string.policy_scope_calendar_write
            ActionScope.ALARM_SET -> R.string.policy_scope_alarm_set
            ActionScope.ALARM_SHOW -> R.string.policy_scope_alarm_show
            ActionScope.ALARM_DISMISS -> R.string.policy_scope_alarm_dismiss
            ActionScope.EXTERNAL_SHARE -> R.string.policy_scope_external_share
            ActionScope.UI_ACT -> R.string.policy_scope_ui_act
            ActionScope.SENSITIVE_WRITE -> R.string.policy_scope_sensitive_write
            ActionScope.BLOCKED_OPERATION -> R.string.policy_scope_blocked
            ActionScope.UNKNOWN -> R.string.policy_scope_unknown
        },
    )

    fun scopeIdLabel(scopeId: String): String = actionScopeLabel(ActionScope.fromScopeId(scopeId))

    fun riskLevelLabel(level: RiskLevel): String = get(
        when (level) {
            RiskLevel.LOW -> R.string.policy_risk_label_low
            RiskLevel.MEDIUM -> R.string.policy_risk_label_medium
            RiskLevel.HIGH -> R.string.policy_risk_label_high
            RiskLevel.BLOCKED -> R.string.policy_risk_label_blocked
        },
    )

    fun policyDecisionLabel(decision: PolicyDecisionType): String = get(
        when (decision) {
            PolicyDecisionType.AUTO_EXECUTE -> R.string.policy_decision_label_auto_execute
            PolicyDecisionType.PREVIEW_FIRST -> R.string.policy_decision_label_preview_first
            PolicyDecisionType.REQUIRE_CONFIRMATION -> R.string.policy_decision_label_require_confirmation
            PolicyDecisionType.DENY -> R.string.policy_decision_label_deny
        },
    )

    fun approvalOutcomeLabel(outcome: ApprovalOutcomeType): String = get(
        when (outcome) {
            ApprovalOutcomeType.APPROVED -> R.string.approval_outcome_approved
            ApprovalOutcomeType.REJECTED -> R.string.approval_outcome_rejected
            ApprovalOutcomeType.ABANDONED -> R.string.approval_outcome_abandoned
        },
    )

    fun providerTypeLabel(providerType: ProviderType): String = get(
        when (providerType) {
            ProviderType.LOCAL -> R.string.bridge_provider_local
            ProviderType.APP_FUNCTIONS -> R.string.bridge_provider_appfunctions
            ProviderType.INTENT -> R.string.bridge_provider_intent
            ProviderType.SHARE -> R.string.bridge_provider_share
            ProviderType.ACCESSIBILITY -> R.string.bridge_provider_accessibility
        },
    )

    fun toolSideEffectLabel(sideEffectType: ToolSideEffectType): String = get(
        when (sideEffectType) {
            ToolSideEffectType.READ -> R.string.tool_side_effect_read
            ToolSideEffectType.WRITE -> R.string.tool_side_effect_write
            ToolSideEffectType.DISPATCH -> R.string.tool_side_effect_dispatch
        },
    )

    fun toolVisibilityStateLabel(state: ToolVisibilityState): String = get(
        when (state) {
            ToolVisibilityState.VISIBLE -> R.string.tool_visibility_visible
            ToolVisibilityState.DEGRADED -> R.string.tool_visibility_degraded
            ToolVisibilityState.HIDDEN -> R.string.tool_visibility_hidden
            ToolVisibilityState.DENIED -> R.string.tool_visibility_denied
        },
    )

    fun externalSourceLabel(packageName: String?): String {
        return if (packageName.isNullOrBlank()) {
            get(R.string.external_handoff_source_generic)
        } else {
            get(R.string.external_handoff_source_package, packageName)
        }
    }

    fun externalTrustStateLabel(state: ExternalTrustState): String = get(
        when (state) {
            ExternalTrustState.TRUSTED -> R.string.external_handoff_trust_trusted
            ExternalTrustState.UNVERIFIED -> R.string.external_handoff_trust_unverified
            ExternalTrustState.DENIED -> R.string.external_handoff_trust_denied
        },
    )

    fun interopContractLabel(
        entryType: InteropEntryType?,
        version: String,
    ): String {
        val family = get(
            when (entryType) {
                InteropEntryType.SHARE_HANDOFF -> R.string.interop_contract_family_share
                InteropEntryType.CALLABLE_REQUEST -> R.string.interop_contract_family_callable
                null -> R.string.interop_contract_family_generic
            },
        )
        return get(R.string.interop_contract_label, family, version)
    }

    fun interopGrantSummary(
        grantCount: Int,
        mimeFamilies: List<String>,
        grantMode: UriGrantMode,
        expiresWithSession: Boolean,
    ): String {
        if (grantCount <= 0) return get(R.string.interop_grant_none)
        val familySummary = mimeFamilies
            .map { mime -> mime.substringBefore('/', missingDelimiterValue = mime) }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = ", ")
            .ifBlank { get(R.string.interop_grant_family_generic) }
        val base = get(
            when (grantMode) {
                UriGrantMode.READ_ONLY -> R.string.interop_grant_read_only
                UriGrantMode.WRITE_CAPABLE -> R.string.interop_grant_write_capable
                UriGrantMode.MIXED -> R.string.interop_grant_mixed
                UriGrantMode.UNKNOWN,
                UriGrantMode.NONE,
                -> R.string.interop_grant_unknown
            },
            grantCount,
            familySummary,
        )
        return if (expiresWithSession) {
            get(R.string.interop_grant_session_limited, base)
        } else {
            base
        }
    }

    fun structuredActionTypeLabel(type: StructuredActionType): String = get(
        when (type) {
            StructuredActionType.MESSAGE_SEND -> R.string.structured_type_message_send
            StructuredActionType.CALENDAR_WRITE -> R.string.structured_type_calendar_write
            StructuredActionType.EXTERNAL_SHARE -> R.string.structured_type_external_share
        },
    )

    fun payloadCompletenessLabel(state: PayloadCompletenessState): String = get(
        when (state) {
            PayloadCompletenessState.COMPLETE -> R.string.structured_completeness_complete
            PayloadCompletenessState.PARTIAL -> R.string.structured_completeness_partial
            PayloadCompletenessState.INSUFFICIENT -> R.string.structured_completeness_insufficient
        },
    )

    fun governanceTrustModeLabel(mode: GovernanceTrustMode): String = get(
        when (mode) {
            GovernanceTrustMode.TRUSTED -> R.string.governance_trust_trusted
            GovernanceTrustMode.ASK_EACH_TIME -> R.string.governance_trust_ask_each_time
            GovernanceTrustMode.DENIED -> R.string.governance_trust_denied
        },
    )

    fun governanceGrantStateLabel(state: GovernanceGrantState): String = get(
        when (state) {
            GovernanceGrantState.ALLOW -> R.string.governance_scope_allow
            GovernanceGrantState.ASK -> R.string.governance_scope_ask
            GovernanceGrantState.DENY -> R.string.governance_scope_deny
        },
    )

    fun governanceTrustedExplanation(displayLabel: String): String =
        get(R.string.governance_explanation_trusted, displayLabel)

    fun governanceAskEachTimeExplanation(displayLabel: String): String =
        get(R.string.governance_explanation_ask_each_time, displayLabel)

    fun governanceDeniedExplanation(displayLabel: String): String =
        get(R.string.governance_explanation_denied, displayLabel)

    fun governanceScopeDeniedExplanation(
        displayLabel: String,
        scopeLabel: String,
    ): String = get(R.string.governance_explanation_scope_denied, displayLabel, scopeLabel)

    fun governanceScopeAskExplanation(
        displayLabel: String,
        scopeLabel: String,
    ): String = get(R.string.governance_explanation_scope_ask, displayLabel, scopeLabel)

    fun systemSourceLabel(sourceId: SystemSourceId): String = get(
        when (sourceId) {
            SystemSourceId.CONTACTS -> R.string.system_source_contacts
            SystemSourceId.CALENDAR -> R.string.system_source_calendar
        },
    )
}

fun PersonaVerbosity.localizedLabel(strings: AppStrings): String = strings.get(
    when (this) {
        PersonaVerbosity.LOW -> R.string.persona_level_low
        PersonaVerbosity.MEDIUM -> R.string.persona_level_medium
        PersonaVerbosity.HIGH -> R.string.persona_level_high
    },
)

fun PersonaWarmth.localizedLabel(strings: AppStrings): String = strings.get(
    when (this) {
        PersonaWarmth.LOW -> R.string.persona_level_low
        PersonaWarmth.MEDIUM -> R.string.persona_level_medium
        PersonaWarmth.HIGH -> R.string.persona_level_high
    },
)
