package com.mobileclaw.app.runtime.governance

import com.mobileclaw.app.runtime.capability.CallerIdentity
import kotlinx.coroutines.flow.Flow

interface GovernanceRepository {
    fun observeGovernanceCenter(limit: Int = 8): Flow<GovernanceCenterSnapshot>

    suspend fun recordCallerObservation(
        callerIdentity: CallerIdentity,
        displayLabel: String,
        decisionSummary: String,
        toolId: String? = null,
        toolDisplayName: String? = null,
    )

    suspend fun updateTrustMode(
        callerId: String,
        trustMode: GovernanceTrustMode,
    )

    suspend fun updateScopeGrant(
        callerId: String,
        scopeId: String,
        grantState: GovernanceGrantState,
    )

    suspend fun resolveSnapshot(
        callerIdentity: CallerIdentity,
        capabilityId: String,
    ): GovernanceDecisionSnapshot?
}
