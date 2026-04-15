package com.mobileclaw.app.ui.agentworkspace.model

data class GovernanceCenterUiModel(
    val callers: List<GovernanceCallerUiModel> = emptyList(),
    val activities: List<GovernanceActivityUiModel> = emptyList(),
)

data class GovernanceCallerUiModel(
    val callerId: String,
    val displayLabel: String,
    val trustModeLabel: String,
    val lastSeenLabel: String,
    val lastDecisionLabel: String,
    val trustModeKey: String,
    val availableTrustModes: List<GovernanceOptionUiModel> = emptyList(),
    val scopeGrants: List<GovernanceScopeGrantUiModel> = emptyList(),
)

data class GovernanceScopeGrantUiModel(
    val scopeId: String,
    val scopeLabel: String,
    val stateLabel: String,
    val stateKey: String,
    val availableStates: List<GovernanceOptionUiModel> = emptyList(),
)

data class GovernanceOptionUiModel(
    val key: String,
    val label: String,
)

data class GovernanceActivityUiModel(
    val activityId: String,
    val headline: String,
    val details: String,
    val timestampLabel: String,
)
