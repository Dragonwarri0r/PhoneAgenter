package com.mobileclaw.app.runtime.policy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approval_requests")
data class ApprovalRequest(
    @PrimaryKey val approvalRequestId: String,
    val sessionId: String,
    val decisionId: String,
    val toolId: String,
    val toolDisplayName: String,
    val sideEffectLabel: String,
    val scopeLines: List<String>,
    val previewLines: List<String>,
    val title: String,
    val summary: String,
    val previewPayload: String,
    val primaryActionLabel: String,
    val secondaryActionLabel: String,
    val localeTag: String,
    val status: ApprovalRequestStatus,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

enum class ApprovalRequestStatus {
    PENDING,
    RESOLVED,
}

@Entity(tableName = "approval_outcomes")
data class ApprovalOutcome(
    @PrimaryKey val approvalOutcomeId: String,
    val approvalRequestId: String,
    val sessionId: String,
    val outcome: ApprovalOutcomeType,
    val actor: String,
    val reason: String?,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

enum class ApprovalOutcomeType {
    APPROVED,
    REJECTED,
    ABANDONED,
}
