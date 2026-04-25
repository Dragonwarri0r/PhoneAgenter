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
    val contributors: List<GovernanceContributorItem> = emptyList(),
)

data class GovernanceContributorItem(
    val contributionId: String,
    val title: String,
    val availabilityLabel: String,
    val summary: String,
    val governanceLines: List<String> = emptyList(),
    val limitationSummary: String = "",
)

data class GovernanceDecisionSnapshot(
    val callerId: String,
    val effectiveTrustMode: GovernanceTrustMode,
    val scopeGrantState: GovernanceGrantState?,
    val allowsRestrictedCapabilities: Boolean,
    val decisionExplanation: String,
)

fun governanceEditableScopes(): List<ActionScope> = listOf(
    ActionScope.REPLY_GENERATE,
    ActionScope.MESSAGE_SEND,
    ActionScope.CALENDAR_WRITE,
    ActionScope.EXTERNAL_SHARE,
    ActionScope.UI_ACT,
    ActionScope.SENSITIVE_WRITE,
)
