package com.mobileclaw.app.runtime.policy

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingApprovalCoordinatorTest {
    @Test
    fun `await outcome returns resolved approval`() = runBlocking {
        val coordinator = PendingApprovalCoordinator()

        val awaitingResult = async {
            coordinator.awaitOutcome(
                approvalRequestId = "approval-1",
                timeoutMillis = 1_000L,
            )
        }

        delay(25L)
        assertTrue(
            coordinator.resolve(
                approvalRequestId = "approval-1",
                outcome = ApprovalOutcomeType.APPROVED,
            ),
        )

        val result = awaitingResult.await()
        assertEquals(ApprovalOutcomeType.APPROVED, result.outcome)
        assertFalse(result.timedOut)
    }

    @Test
    fun `await outcome abandons expired approvals`() = runBlocking {
        val coordinator = PendingApprovalCoordinator()

        val result = coordinator.awaitOutcome(
            approvalRequestId = "approval-2",
            timeoutMillis = 40L,
        )

        assertEquals(ApprovalOutcomeType.ABANDONED, result.outcome)
        assertTrue(result.timedOut)
    }
}
