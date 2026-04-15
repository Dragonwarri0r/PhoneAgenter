package com.mobileclaw.app.runtime.policy

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ApprovalDao {
    @Upsert
    suspend fun upsertApprovalRequest(request: ApprovalRequest)

    @Upsert
    suspend fun upsertApprovalOutcome(outcome: ApprovalOutcome)

    @Query(
        """
        UPDATE approval_requests
        SET status = :status
        WHERE approvalRequestId = :approvalRequestId
        """,
    )
    suspend fun updateApprovalStatus(
        approvalRequestId: String,
        status: ApprovalRequestStatus,
    )

    @Query(
        """
        SELECT * FROM approval_requests
        WHERE status = :status
          AND createdAtEpochMillis <= :createdBeforeEpochMillis
        ORDER BY createdAtEpochMillis ASC
        """,
    )
    suspend fun listRequestsByStatusOlderThan(
        status: ApprovalRequestStatus,
        createdBeforeEpochMillis: Long,
    ): List<ApprovalRequest>

    @Query(
        """
        SELECT * FROM approval_requests
        WHERE sessionId = :sessionId
        ORDER BY createdAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestRequestForSession(sessionId: String): ApprovalRequest?

    @Query(
        """
        SELECT * FROM approval_outcomes
        WHERE approvalRequestId = :approvalRequestId
        LIMIT 1
        """,
    )
    suspend fun getOutcomeForRequest(approvalRequestId: String): ApprovalOutcome?
}
