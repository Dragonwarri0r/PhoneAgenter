package com.mobileclaw.app.runtime.policy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "policy_decisions")
data class PolicyDecision(
    @PrimaryKey val decisionId: String,
    val sessionId: String,
    val assessmentId: String,
    val decision: PolicyDecisionType,
    val effectiveScopeId: String,
    val ruleSource: PolicyRuleSource,
    val rationale: String,
    val awaitingInput: Boolean,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

enum class PolicyDecisionType {
    AUTO_EXECUTE,
    PREVIEW_FIRST,
    REQUIRE_CONFIRMATION,
    DENY,
}

enum class PolicyRuleSource {
    CLASSIFIER,
    MEDIUM_RISK_RULE,
    HIGH_RISK_RULE,
    HARD_CONFIRM_RULE,
    BLOCKED_RULE,
    UNKNOWN_SCOPE_RULE,
}

fun PolicyDecision.requiresApproval(): Boolean {
    return decision == PolicyDecisionType.PREVIEW_FIRST ||
        decision == PolicyDecisionType.REQUIRE_CONFIRMATION
}
