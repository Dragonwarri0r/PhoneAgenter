package com.mobileclaw.app.runtime.policy

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PolicyDao {
    @Upsert
    suspend fun upsertRiskAssessment(assessment: RiskAssessment)

    @Upsert
    suspend fun upsertPolicyDecision(decision: PolicyDecision)

    @Query(
        """
        SELECT * FROM policy_decisions
        WHERE sessionId = :sessionId
        ORDER BY createdAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestDecisionForSession(sessionId: String): PolicyDecision?
}
