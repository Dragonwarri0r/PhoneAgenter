package com.mobileclaw.app.runtime.policy

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditFormatter @Inject constructor(
    private val appStrings: AppStrings,
) {
    fun riskHeadline(assessment: RiskAssessment): String {
        return appStrings.get(
            R.string.audit_headline_risk_assessed,
            appStrings.riskLevelLabel(assessment.riskLevel),
        )
    }

    fun policyHeadline(decision: PolicyDecision): String {
        return appStrings.get(
            R.string.audit_headline_policy_decided,
            appStrings.policyDecisionLabel(decision.decision),
        )
    }

    fun approvalRequestedHeadline(): String {
        return appStrings.get(R.string.audit_headline_approval_requested)
    }

    fun approvalResolvedHeadline(outcome: ApprovalOutcomeType): String {
        return appStrings.get(
            R.string.audit_headline_approval_resolved,
            appStrings.approvalOutcomeLabel(outcome),
        )
    }

    fun executionCompletedHeadline(): String {
        return appStrings.get(R.string.audit_headline_execution_completed)
    }

    fun executionDeniedHeadline(): String {
        return appStrings.get(R.string.audit_headline_execution_denied)
    }

    fun executionFailedHeadline(): String {
        return appStrings.get(R.string.audit_headline_execution_failed)
    }
}
