package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.runtime.capability.CallerIdentity
import com.mobileclaw.app.runtime.capability.ToolVisibilitySnapshot
import com.mobileclaw.app.runtime.action.ActionNormalizationResult
import com.mobileclaw.app.runtime.contribution.ContextContribution
import com.mobileclaw.app.runtime.contribution.ContributionOutcomeRecord
import com.mobileclaw.app.runtime.contribution.KnowledgeRequestContribution
import com.mobileclaw.app.runtime.capability.ProviderType
import com.mobileclaw.app.runtime.ingress.ExternalInvocationRecord
import com.mobileclaw.app.runtime.policy.ApprovalOutcome
import com.mobileclaw.app.runtime.policy.ApprovalRequest
import com.mobileclaw.app.runtime.policy.AuditEvent
import com.mobileclaw.app.runtime.policy.PolicyDecision
import com.mobileclaw.app.runtime.policy.RiskAssessment
import com.mobileclaw.app.runtime.systemsource.SystemSourceContribution
import com.mobileclaw.app.runtime.systemsource.SystemSourceDescriptor

enum class RuntimeStageType {
    INGRESS,
    CONTEXT_LOADING,
    PLANNING,
    CAPABILITY_SELECTION,
    EXECUTION_GATING,
    AWAITING_APPROVAL,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELLED,
    DENIED,
}

data class RuntimeStage(
    val sessionId: String,
    val stageType: RuntimeStageType,
    val label: String,
    val details: String,
    val ordinal: Int,
    val occurredAtEpochMillis: Long = System.currentTimeMillis(),
)

sealed interface RuntimeSessionEvent {
    data class ExternalHandoffReceived(
        val sourceMetadata: RuntimeSourceMetadata,
    ) : RuntimeSessionEvent

    data class ExternalInvocationLinked(
        val record: ExternalInvocationRecord,
    ) : RuntimeSessionEvent

    data class SessionStarted(
        val session: ExecutionSession,
    ) : RuntimeSessionEvent

    data class StageChanged(
        val stage: RuntimeStage,
    ) : RuntimeSessionEvent

    data class StatusSummaryUpdated(
        val summary: RuntimeStatusSummary,
    ) : RuntimeSessionEvent

    data class SystemSourcesPrepared(
        val descriptors: List<SystemSourceDescriptor>,
        val contributions: List<SystemSourceContribution>,
    ) : RuntimeSessionEvent

    data class ContributionsUpdated(
        val outcomes: List<ContributionOutcomeRecord>,
        val contextContributions: List<ContextContribution>,
        val knowledgeContribution: KnowledgeRequestContribution? = null,
    ) : RuntimeSessionEvent

    data class CallerVerified(
        val callerIdentity: CallerIdentity,
    ) : RuntimeSessionEvent

    data class CapabilityRouted(
        val sessionId: String,
        val capabilityId: String,
        val providerId: String,
        val providerType: ProviderType,
        val routeExplanation: String,
        val toolId: String,
        val toolDisplayName: String,
        val toolSideEffectLabel: String,
        val toolScopeLines: List<String>,
        val visibilitySnapshot: ToolVisibilitySnapshot? = null,
    ) : RuntimeSessionEvent

    data class StructuredActionPrepared(
        val sessionId: String,
        val normalization: ActionNormalizationResult,
    ) : RuntimeSessionEvent

    data class RiskAssessed(
        val assessment: RiskAssessment,
    ) : RuntimeSessionEvent

    data class PolicyResolved(
        val decision: PolicyDecision,
    ) : RuntimeSessionEvent

    data class ApprovalRequested(
        val request: ApprovalRequest,
    ) : RuntimeSessionEvent

    data class ApprovalResolved(
        val outcome: ApprovalOutcome,
    ) : RuntimeSessionEvent

    data class AuditRecorded(
        val event: AuditEvent,
    ) : RuntimeSessionEvent

    data class CapabilityRequested(
        val sessionId: String,
        val capabilityId: String,
        val providerId: String,
    ) : RuntimeSessionEvent

    data class CapabilityStarted(
        val sessionId: String,
        val capabilityId: String,
        val providerId: String,
    ) : RuntimeSessionEvent

    data class CapabilityOutputChunk(
        val sessionId: String,
        val capabilityId: String,
        val providerId: String,
        val chunk: String,
    ) : RuntimeSessionEvent

    data class CapabilityCompleted(
        val sessionId: String,
        val capabilityId: String,
        val providerId: String,
        val outputText: String,
    ) : RuntimeSessionEvent

    data class CapabilityFailed(
        val sessionId: String,
        val capabilityId: String,
        val providerId: String,
        val userMessage: String,
    ) : RuntimeSessionEvent

    data class SessionCompleted(
        val outcome: SessionOutcome,
    ) : RuntimeSessionEvent

    data class SessionFailed(
        val outcome: SessionOutcome,
    ) : RuntimeSessionEvent

    data class SessionCancelled(
        val outcome: SessionOutcome,
    ) : RuntimeSessionEvent

    data class SessionDenied(
        val outcome: SessionOutcome,
    ) : RuntimeSessionEvent
}
