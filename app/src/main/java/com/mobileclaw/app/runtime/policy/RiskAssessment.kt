package com.mobileclaw.app.runtime.policy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "risk_assessments")
data class RiskAssessment(
    @PrimaryKey val assessmentId: String,
    val sessionId: String,
    val requestId: String,
    val capabilityId: String,
    val scopeId: String,
    val riskLevel: RiskLevel,
    val rationale: String,
    val signals: List<String>,
    val confidence: Double?,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    BLOCKED,
}
