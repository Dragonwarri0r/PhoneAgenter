package com.mobileclaw.app.runtime.governance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caller_governance_records")
data class CallerGovernanceRecord(
    @PrimaryKey val callerId: String,
    val originApp: String,
    val displayLabel: String,
    val packageName: String?,
    val signatureDigest: String?,
    val trustMode: GovernanceTrustMode,
    val trustReason: String,
    val lastSeenAtEpochMillis: Long,
    val lastDecisionSummary: String,
)
