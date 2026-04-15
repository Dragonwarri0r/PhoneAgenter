package com.mobileclaw.app.runtime.governance

import com.mobileclaw.app.runtime.capability.CallerIdentity
import com.mobileclaw.app.runtime.policy.AuditDao
import com.mobileclaw.app.runtime.policy.AuditEventType
import com.mobileclaw.app.runtime.policy.ActionScope
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class DefaultGovernanceRepository @Inject constructor(
    private val governanceDao: GovernanceDao,
    private val auditDao: AuditDao,
    private val appStrings: AppStrings,
) : GovernanceRepository {

    override fun observeGovernanceCenter(limit: Int): Flow<GovernanceCenterSnapshot> {
        return combine(
            governanceDao.observeRecentCallers(limit),
            governanceDao.observeAllScopeGrants(),
            auditDao.observeRecent(limit),
        ) { callers, grants, audits ->
            GovernanceCenterSnapshot(
                callers = callers.map { caller ->
                    GovernanceCallerSnapshot(
                        record = caller,
                        scopeGrants = grants.filter { it.callerId == caller.callerId }
                            .sortedBy { it.scopeId },
                    )
                },
                activities = audits
                    .filter {
                        it.eventType == AuditEventType.APPROVAL_REQUESTED ||
                            it.eventType == AuditEventType.APPROVAL_RESOLVED ||
                            it.eventType == AuditEventType.EXECUTION_DENIED
                    }
                    .take(limit)
                    .map { audit ->
                        GovernanceActivityItem(
                            activityId = audit.auditEventId,
                            headline = audit.headline,
                            details = audit.details,
                            timestamp = audit.createdAtEpochMillis,
                        )
                    },
            )
        }
    }

    override suspend fun recordCallerObservation(
        callerIdentity: CallerIdentity,
        displayLabel: String,
        decisionSummary: String,
        toolId: String?,
        toolDisplayName: String?,
    ) {
        val existing = governanceDao.getCaller(callerIdentity.callerId)
        val trustMode = existing?.trustMode ?: when (callerIdentity.trustState.name) {
            "TRUSTED" -> GovernanceTrustMode.TRUSTED
            "DENIED" -> GovernanceTrustMode.DENIED
            else -> GovernanceTrustMode.ASK_EACH_TIME
        }
        val summarizedDecision = if (!toolId.isNullOrBlank() && !toolDisplayName.isNullOrBlank()) {
            appStrings.get(
                com.mobileclaw.app.R.string.governance_tool_decision_summary,
                toolDisplayName,
                toolId,
                decisionSummary,
            )
        } else {
            decisionSummary
        }
        governanceDao.upsertCallerRecord(
            CallerGovernanceRecord(
                callerId = callerIdentity.callerId,
                originApp = callerIdentity.originApp,
                displayLabel = displayLabel,
                packageName = callerIdentity.packageName,
                signatureDigest = callerIdentity.signatureDigest,
                trustMode = trustMode,
                trustReason = callerIdentity.trustReason,
                lastSeenAtEpochMillis = System.currentTimeMillis(),
                lastDecisionSummary = summarizedDecision,
            ),
        )
    }

    override suspend fun updateTrustMode(
        callerId: String,
        trustMode: GovernanceTrustMode,
    ) {
        val existing = governanceDao.getCaller(callerId) ?: return
        governanceDao.upsertCallerRecord(
            existing.copy(
                trustMode = trustMode,
                lastDecisionSummary = appStrings.governanceTrustModeLabel(trustMode),
                lastSeenAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun updateScopeGrant(
        callerId: String,
        scopeId: String,
        grantState: GovernanceGrantState,
    ) {
        val existing = governanceDao.getScopeGrant(callerId, scopeId)
        governanceDao.upsertScopeGrant(
            existing?.copy(
                grantState = grantState,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ) ?: ScopeGrantRecord(
                grantId = "grant-$callerId-$scopeId",
                callerId = callerId,
                scopeId = scopeId,
                grantState = grantState,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun resolveSnapshot(
        callerIdentity: CallerIdentity,
        capabilityId: String,
    ): GovernanceDecisionSnapshot? {
        val record = governanceDao.getCaller(callerIdentity.callerId) ?: return null
        val scopeId = ActionScope.fromCapabilityId(capabilityId).scopeId
        val grant = governanceDao.getScopeGrant(callerIdentity.callerId, scopeId)
        val allows = when {
            record.trustMode == GovernanceTrustMode.DENIED -> false
            grant?.grantState == GovernanceGrantState.DENY -> false
            else -> true
        }
        val explanation = when {
            record.trustMode == GovernanceTrustMode.DENIED -> {
                appStrings.governanceDeniedExplanation(record.displayLabel)
            }
            grant?.grantState == GovernanceGrantState.DENY -> {
                appStrings.governanceScopeDeniedExplanation(
                    record.displayLabel,
                    appStrings.actionScopeLabel(ActionScope.fromScopeId(scopeId)),
                )
            }
            record.trustMode == GovernanceTrustMode.ASK_EACH_TIME -> {
                appStrings.governanceAskEachTimeExplanation(record.displayLabel)
            }
            grant?.grantState == GovernanceGrantState.ASK -> {
                appStrings.governanceScopeAskExplanation(
                    record.displayLabel,
                    appStrings.actionScopeLabel(ActionScope.fromScopeId(scopeId)),
                )
            }
            else -> appStrings.governanceTrustedExplanation(record.displayLabel)
        }
        return GovernanceDecisionSnapshot(
            callerId = callerIdentity.callerId,
            effectiveTrustMode = record.trustMode,
            scopeGrantState = grant?.grantState,
            allowsRestrictedCapabilities = allows,
            decisionExplanation = explanation,
        )
    }
}
