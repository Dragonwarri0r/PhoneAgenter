package com.mobileclaw.app.runtime.policy

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PolicyEngine @Inject constructor(
    private val appStrings: AppStrings,
) {
    fun resolve(assessment: RiskAssessment): PolicyDecision {
        val scope = ActionScope.fromScopeId(assessment.scopeId)
        val (decision, ruleSource, rationale) = when {
            scope.riskMode == ActionRiskMode.BLOCKED ||
                assessment.riskLevel == RiskLevel.BLOCKED -> Triple(
                    PolicyDecisionType.DENY,
                    PolicyRuleSource.BLOCKED_RULE,
                    appStrings.get(
                        R.string.policy_decision_deny,
                        appStrings.actionScopeLabel(scope),
                    ),
                )

            scope == ActionScope.UNKNOWN -> Triple(
                PolicyDecisionType.PREVIEW_FIRST,
                PolicyRuleSource.UNKNOWN_SCOPE_RULE,
                appStrings.get(R.string.policy_decision_preview_unknown),
            )

            scope.riskMode == ActionRiskMode.HARD_CONFIRM -> Triple(
                PolicyDecisionType.REQUIRE_CONFIRMATION,
                PolicyRuleSource.HARD_CONFIRM_RULE,
                appStrings.get(
                    R.string.policy_decision_require_confirmation,
                    appStrings.actionScopeLabel(scope),
                ),
            )

            assessment.riskLevel == RiskLevel.HIGH -> Triple(
                PolicyDecisionType.REQUIRE_CONFIRMATION,
                PolicyRuleSource.HIGH_RISK_RULE,
                appStrings.get(R.string.policy_decision_high_risk),
            )

            assessment.riskLevel == RiskLevel.MEDIUM -> Triple(
                PolicyDecisionType.PREVIEW_FIRST,
                PolicyRuleSource.MEDIUM_RISK_RULE,
                appStrings.get(R.string.policy_decision_preview_medium_risk),
            )

            else -> Triple(
                PolicyDecisionType.AUTO_EXECUTE,
                PolicyRuleSource.CLASSIFIER,
                appStrings.get(
                    R.string.policy_decision_auto_execute,
                    appStrings.actionScopeLabel(scope),
                ),
            )
        }

        return PolicyDecision(
            decisionId = "policy-${System.currentTimeMillis()}-${assessment.assessmentId}",
            sessionId = assessment.sessionId,
            assessmentId = assessment.assessmentId,
            decision = decision,
            effectiveScopeId = scope.scopeId,
            ruleSource = ruleSource,
            rationale = rationale,
            awaitingInput = decision == PolicyDecisionType.PREVIEW_FIRST ||
                decision == PolicyDecisionType.REQUIRE_CONFIRMATION,
        )
    }
}
