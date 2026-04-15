package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.runtime.localchat.SessionResetResult
import com.mobileclaw.app.runtime.policy.ApprovalOutcomeType
import kotlinx.coroutines.flow.Flow

interface RuntimeSessionFacade {
    fun submitRequest(request: RuntimeRequest): Flow<RuntimeSessionEvent>

    suspend fun resetModelSession(modelId: String): SessionResetResult?

    suspend fun resolveApprovalRequest(
        approvalRequestId: String,
        outcome: ApprovalOutcomeType,
    ): Boolean
}
