package com.mobileclaw.app.runtime.policy

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PolicyRepository @Inject constructor(
    private val policyDao: PolicyDao,
) {
    suspend fun saveAssessment(assessment: RiskAssessment) {
        policyDao.upsertRiskAssessment(assessment)
    }

    suspend fun saveDecision(decision: PolicyDecision) {
        policyDao.upsertPolicyDecision(decision)
    }

    suspend fun getLatestDecisionForSession(sessionId: String): PolicyDecision? {
        return policyDao.getLatestDecisionForSession(sessionId)
    }
}
