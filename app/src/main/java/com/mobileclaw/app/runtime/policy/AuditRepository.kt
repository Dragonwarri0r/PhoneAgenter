package com.mobileclaw.app.runtime.policy

import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AuditRepository @Inject constructor(
    private val auditDao: AuditDao,
    private val appStrings: AppStrings,
    private val formatter: AuditFormatter,
) {
    suspend fun record(
        sessionId: String,
        eventType: AuditEventType,
        headline: String,
        details: String,
        toolId: String? = null,
        toolDisplayName: String? = null,
        sideEffectLabel: String? = null,
        linkedRecordId: String? = null,
    ): AuditEvent {
        val event = AuditEvent(
            auditEventId = "audit-${System.currentTimeMillis()}-$sessionId-$eventType",
            sessionId = sessionId,
            eventType = eventType,
            toolId = toolId,
            toolDisplayName = toolDisplayName,
            sideEffectLabel = sideEffectLabel,
            headline = headline,
            details = details,
            linkedRecordId = linkedRecordId,
            localeTag = appStrings.localeTag(),
        )
        auditDao.upsertAuditEvent(event)
        return event
    }

    fun observeRecentForSession(
        sessionId: String,
        limit: Int = 6,
    ): Flow<List<AuditEvent>> = auditDao.observeRecentForSession(sessionId, limit)

    suspend fun recordRiskAssessed(assessment: RiskAssessment): AuditEvent {
        return record(
            sessionId = assessment.sessionId,
            eventType = AuditEventType.RISK_ASSESSED,
            headline = formatter.riskHeadline(assessment),
            details = assessment.rationale,
            linkedRecordId = assessment.assessmentId,
        )
    }

    suspend fun recordPolicyDecided(decision: PolicyDecision): AuditEvent {
        return record(
            sessionId = decision.sessionId,
            eventType = AuditEventType.POLICY_DECIDED,
            headline = formatter.policyHeadline(decision),
            details = decision.rationale,
            linkedRecordId = decision.decisionId,
        )
    }

    suspend fun recordApprovalRequested(request: ApprovalRequest): AuditEvent {
        return record(
            sessionId = request.sessionId,
            eventType = AuditEventType.APPROVAL_REQUESTED,
            headline = formatter.approvalRequestedHeadline(),
            details = request.summary,
            toolId = request.toolId,
            toolDisplayName = request.toolDisplayName,
            sideEffectLabel = request.sideEffectLabel,
            linkedRecordId = request.approvalRequestId,
        )
    }

    suspend fun recordApprovalResolved(
        sessionId: String,
        requestId: String,
        outcome: ApprovalOutcomeType,
        details: String,
    ): AuditEvent {
        return record(
            sessionId = sessionId,
            eventType = AuditEventType.APPROVAL_RESOLVED,
            headline = formatter.approvalResolvedHeadline(outcome),
            details = details,
            linkedRecordId = requestId,
        )
    }

    suspend fun recordExecutionCompleted(
        sessionId: String,
        details: String,
        toolId: String? = null,
        toolDisplayName: String? = null,
        sideEffectLabel: String? = null,
    ): AuditEvent = record(
        sessionId = sessionId,
        eventType = AuditEventType.EXECUTION_COMPLETED,
        headline = formatter.executionCompletedHeadline(),
        details = details,
        toolId = toolId,
        toolDisplayName = toolDisplayName,
        sideEffectLabel = sideEffectLabel,
    )

    suspend fun recordExecutionDenied(
        sessionId: String,
        details: String,
        toolId: String? = null,
        toolDisplayName: String? = null,
        sideEffectLabel: String? = null,
    ): AuditEvent = record(
        sessionId = sessionId,
        eventType = AuditEventType.EXECUTION_DENIED,
        headline = formatter.executionDeniedHeadline(),
        details = details,
        toolId = toolId,
        toolDisplayName = toolDisplayName,
        sideEffectLabel = sideEffectLabel,
    )

    suspend fun recordExecutionFailed(
        sessionId: String,
        details: String,
        toolId: String? = null,
        toolDisplayName: String? = null,
        sideEffectLabel: String? = null,
    ): AuditEvent = record(
        sessionId = sessionId,
        eventType = AuditEventType.EXECUTION_FAILED,
        headline = formatter.executionFailedHeadline(),
        details = details,
        toolId = toolId,
        toolDisplayName = toolDisplayName,
        sideEffectLabel = sideEffectLabel,
    )
}
