package com.mobileclaw.app.runtime.policy

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class PendingApprovalResult(
    val outcome: ApprovalOutcomeType,
    val timedOut: Boolean,
)

@Singleton
class PendingApprovalCoordinator @Inject constructor() {
    val approvalTimeoutMillis: Long = DEFAULT_APPROVAL_TIMEOUT_MILLIS

    private val mutex = Mutex()
    private val pending = mutableMapOf<String, CompletableDeferred<ApprovalOutcomeType>>()

    suspend fun register(approvalRequestId: String) = mutex.withLock {
        pending.getOrPut(approvalRequestId) { CompletableDeferred() }
    }

    suspend fun awaitOutcome(
        approvalRequestId: String,
        timeoutMillis: Long = approvalTimeoutMillis,
    ): PendingApprovalResult {
        val deferred = mutex.withLock {
            pending.getOrPut(approvalRequestId) { CompletableDeferred() }
        }
        return try {
            val outcome = withTimeoutOrNull(timeoutMillis) {
                deferred.await()
            }
            PendingApprovalResult(
                outcome = outcome ?: ApprovalOutcomeType.ABANDONED,
                timedOut = outcome == null,
            )
        } finally {
            clear(approvalRequestId)
        }
    }

    suspend fun resolve(
        approvalRequestId: String,
        outcome: ApprovalOutcomeType,
    ): Boolean = mutex.withLock {
        val deferred = pending[approvalRequestId] ?: return false
        if (deferred.isCompleted) return false
        deferred.complete(outcome)
        true
    }

    private suspend fun clear(approvalRequestId: String) = mutex.withLock {
        pending.remove(approvalRequestId)
    }

    companion object {
        const val DEFAULT_APPROVAL_TIMEOUT_MILLIS = 120_000L
    }
}
