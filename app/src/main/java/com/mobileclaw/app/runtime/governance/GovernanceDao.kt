package com.mobileclaw.app.runtime.governance

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GovernanceDao {
    @Upsert
    suspend fun upsertCallerRecord(record: CallerGovernanceRecord)

    @Upsert
    suspend fun upsertScopeGrant(record: ScopeGrantRecord)

    @Query(
        """
        SELECT * FROM caller_governance_records
        ORDER BY lastSeenAtEpochMillis DESC
        LIMIT :limit
        """,
    )
    fun observeRecentCallers(limit: Int): Flow<List<CallerGovernanceRecord>>

    @Query(
        """
        SELECT * FROM caller_governance_records
        WHERE callerId = :callerId
        LIMIT 1
        """,
    )
    suspend fun getCaller(callerId: String): CallerGovernanceRecord?

    @Query(
        """
        SELECT * FROM scope_grant_records
        ORDER BY updatedAtEpochMillis DESC
        """,
    )
    fun observeAllScopeGrants(): Flow<List<ScopeGrantRecord>>

    @Query(
        """
        SELECT * FROM scope_grant_records
        WHERE callerId = :callerId AND scopeId = :scopeId
        LIMIT 1
        """,
    )
    suspend fun getScopeGrant(callerId: String, scopeId: String): ScopeGrantRecord?
}
