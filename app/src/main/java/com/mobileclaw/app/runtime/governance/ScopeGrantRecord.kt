package com.mobileclaw.app.runtime.governance

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scope_grant_records",
    indices = [Index(value = ["callerId", "scopeId"], unique = true)],
)
data class ScopeGrantRecord(
    @PrimaryKey val grantId: String,
    val callerId: String,
    val scopeId: String,
    val grantState: GovernanceGrantState,
    val updatedAtEpochMillis: Long,
)
