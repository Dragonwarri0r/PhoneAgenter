package com.mobileclaw.app.runtime.policy

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Upsert
    suspend fun upsertAuditEvent(event: AuditEvent)

    @Query(
        """
        SELECT * FROM audit_events
        WHERE sessionId = :sessionId
        ORDER BY createdAtEpochMillis DESC
        LIMIT :limit
        """,
    )
    fun observeRecentForSession(
        sessionId: String,
        limit: Int,
    ): Flow<List<AuditEvent>>

    @Query(
        """
        SELECT * FROM audit_events
        ORDER BY createdAtEpochMillis DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(
        limit: Int,
    ): Flow<List<AuditEvent>>
}
