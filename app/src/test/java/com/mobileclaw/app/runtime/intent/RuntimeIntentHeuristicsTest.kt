package com.mobileclaw.app.runtime.intent

import com.mobileclaw.app.runtime.policy.ActionScope
import com.mobileclaw.app.runtime.session.DefaultRuntimePlanner
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimeRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeIntentHeuristicsTest {
    @Test
    fun `planner keeps internal workspace freeform chat in reply generation`() = runBlocking {
        val planner = DefaultRuntimePlanner()

        val plan = planner.plan(
            request = runtimeRequest("Text Alice that I'll be 10 minutes late."),
            contextPayload = runtimeContextPayload(),
        )

        assertEquals("generate.reply", plan.selectedCapabilityId)
    }

    @Test
    fun `planner still accepts explicit capability hints for internal workspace`() = runBlocking {
        val planner = DefaultRuntimePlanner()

        val plan = planner.plan(
            request = runtimeRequest(
                userInput = "Text Alice that I'll be 10 minutes late.",
                requestedCapabilities = listOf(
                    com.mobileclaw.app.runtime.session.RuntimeCapabilityHint(
                        capabilityId = "message.send",
                    ),
                ),
            ),
            contextPayload = runtimeContextPayload(),
        )

        assertEquals("message.send", plan.selectedCapabilityId)
    }

    @Test
    fun `draft-style message requests stay in reply generation`() {
        val inference = RuntimeIntentHeuristics.infer("帮我写一条给 Alice 的消息，说我会晚到十分钟。")

        assertEquals("generate.reply", inference.capabilityId)
        assertEquals(ActionScope.REPLY_GENERATE, inference.scope)
    }

    @Test
    fun `blocked financial actions stay in policy path instead of fake capability routing`() {
        val inference = RuntimeIntentHeuristics.infer("Please transfer money to Bob right now.")

        assertEquals("generate.reply", inference.capabilityId)
        assertEquals(ActionScope.BLOCKED_OPERATION, inference.scope)
    }

    @Test
    fun `action scope falls back to language heuristics when capability looks like reply generation`() {
        val scope = ActionScope.infer(
            userInput = "把明天下午三点和 Bob 的会议加入日历。",
            capabilityId = "generate.reply",
        )

        assertEquals(ActionScope.CALENDAR_WRITE, scope)
    }

    private fun runtimeRequest(
        userInput: String,
        requestedCapabilities: List<com.mobileclaw.app.runtime.session.RuntimeCapabilityHint> = emptyList(),
    ) = RuntimeRequest(
        requestId = "request-1",
        userInput = userInput,
        selectedModelId = "model-1",
        transcriptContext = emptyList(),
        requestedCapabilities = requestedCapabilities,
    )

    private fun runtimeContextPayload() = RuntimeContextPayload(
        summary = "Fresh session",
        transcriptTurnCount = 0,
        hasTranscriptContext = false,
    )
}
