package com.mobileclaw.app.runtime.policy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_events")
data class AuditEvent(
    @PrimaryKey val auditEventId: String,
    val sessionId: String,
    val eventType: AuditEventType,
    val toolId: String?,
    val toolDisplayName: String?,
    val sideEffectLabel: String?,
    val headline: String,
    val details: String,
    val linkedRecordId: String?,
    val localeTag: String,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

enum class AuditEventType {
    EXTERNAL_HANDOFF,
    BRIDGE_ROUTED,
    RISK_ASSESSED,
    POLICY_DECIDED,
    APPROVAL_REQUESTED,
    APPROVAL_RESOLVED,
    EXECUTION_COMPLETED,
    EXECUTION_DENIED,
    EXECUTION_FAILED,
}
