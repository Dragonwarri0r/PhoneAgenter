package com.mobileclaw.app.runtime.governance

import com.mobileclaw.app.runtime.policy.ActionScope

data class GovernanceActivityItem(
    val activityId: String,
    val headline: String,
    val details: String,
    val scopeLabel: String = "",
    val timestamp: Long,
)

data class GovernanceCallerSnapshot(
    val record: CallerGovernanceRecord,
    val scopeGrants: List<ScopeGrantRecord>,
)

data class GovernanceCenterSnapshot(
    val callers: List<GovernanceCallerSnapshot>,
    val activities: List<GovernanceActivityItem>,
)

data class GovernanceDecisionSnapshot(
    val callerId: String,
    val effectiveTrustMode: GovernanceTrustMode,
    val scopeGrantState: GovernanceGrantState?,
    val allowsRestrictedCapabilities: Boolean,
    val decisionExplanation: String,
)

fun governanceEditableScopes(): List<ActionScope> = listOf(
    ActionScope.MESSAGE_SEND,
    ActionScope.CALENDAR_WRITE,
    ActionScope.EXTERNAL_SHARE,
    ActionScope.UI_ACT,
    ActionScope.SENSITIVE_WRITE,
)
