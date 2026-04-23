package com.mobileclaw.app.runtime.intent

import com.mobileclaw.app.runtime.policy.ActionScope
import com.mobileclaw.app.runtime.session.CapabilityPlanningProposal
import com.mobileclaw.app.runtime.session.CapabilityResolutionMode
import com.mobileclaw.app.runtime.session.CapabilitySelectionSource
import com.mobileclaw.app.runtime.session.RuntimeCapabilityHint
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.session.WorkspaceCapabilitySelector
import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeIntentHeuristicsTest {
    private val selector = WorkspaceCapabilitySelector()

    @Test
    fun `explicit capability hints outrank workspace inference`() {
        val decision = selector.select(
            request = runtimeRequest(
                userInput = "Text Alice that I'll be 10 minutes late.",
                requestedCapabilities = listOf(RuntimeCapabilityHint(capabilityId = "message.send")),
            ),
            inferredIntent = RuntimeIntentHeuristics.infer("Text Alice that I'll be 10 minutes late."),
        )

        assertEquals("message.send", decision.selectedCapabilityId)
        assertEquals(CapabilitySelectionSource.EXPLICIT_HINT, decision.selectionSource)
    }

    @Test
    fun `clear calendar lookup selects read capability for workspace input`() {
        val rawInput = "What's on my calendar today?"
        val decision = selector.select(
            request = runtimeRequest(rawInput),
            inferredIntent = RuntimeIntentHeuristics.infer(rawInput),
        )

        assertEquals("calendar.read", decision.selectedCapabilityId)
        assertEquals(CapabilityResolutionMode.EXPLICIT_READ, decision.resolutionMode)
    }

    @Test
    fun `ambiguous workspace prompt falls back to reply mode`() {
        val rawInput = "Can you help me around my meetings tomorrow?"
        val decision = selector.select(
            request = runtimeRequest(rawInput),
            inferredIntent = RuntimeIntentHeuristics.infer(rawInput),
        )

        assertEquals("generate.reply", decision.selectedCapabilityId)
        assertEquals(CapabilityResolutionMode.REPLY_FALLBACK, decision.resolutionMode)
    }

    @Test
    fun `clear scheduling prompt stays on governed action path`() {
        val rawInput = "Add lunch with Bob tomorrow at 1 PM to my calendar."
        val decision = selector.select(
            request = runtimeRequest(rawInput),
            inferredIntent = RuntimeIntentHeuristics.infer(rawInput),
        )

        assertEquals("calendar.write", decision.selectedCapabilityId)
        assertEquals(CapabilityResolutionMode.EXPLICIT_ACTION, decision.resolutionMode)
    }

    @Test
    fun `mixed-language calendar lookup still selects read capability`() {
        val rawInput = "show 我的 calendar today"
        val decision = selector.select(
            request = runtimeRequest(rawInput),
            inferredIntent = RuntimeIntentHeuristics.infer(rawInput),
        )

        assertEquals("calendar.read", decision.selectedCapabilityId)
        assertEquals(CapabilityResolutionMode.EXPLICIT_READ, decision.resolutionMode)
    }

    @Test
    fun `clear calendar deletion prompt stays on governed action path`() {
        val rawInput = "Delete the 'Codex validation event' event from my calendar tomorrow at 3 PM."
        val decision = selector.select(
            request = runtimeRequest(rawInput),
            inferredIntent = RuntimeIntentHeuristics.infer(rawInput),
        )

        assertEquals("calendar.delete", decision.selectedCapabilityId)
        assertEquals(CapabilityResolutionMode.EXPLICIT_ACTION, decision.resolutionMode)
    }

    @Test
    fun `model planner proposal can promote clear calendar read requests`() {
        val rawInput = "Can you show my schedule for today?"
        val decision = selector.select(
            request = runtimeRequest(rawInput),
            inferredIntent = RuntimeIntentHeuristics.infer(rawInput),
            plannerProposal = CapabilityPlanningProposal(
                capabilityId = "calendar.read",
                confidence = 0.76,
                rationale = "The user is asking for a schedule lookup.",
            ),
        )

        assertEquals("calendar.read", decision.selectedCapabilityId)
        assertEquals(CapabilitySelectionSource.MODEL_PLANNER, decision.selectionSource)
    }

    @Test
    fun `model planner proposal still falls back when the request is ambiguous`() {
        val rawInput = "Can you help me around my meetings tomorrow?"
        val decision = selector.select(
            request = runtimeRequest(rawInput),
            inferredIntent = RuntimeIntentHeuristics.infer(rawInput),
            plannerProposal = CapabilityPlanningProposal(
                capabilityId = "calendar.write",
                confidence = 0.91,
                rationale = "Meetings are mentioned.",
            ),
        )

        assertEquals("generate.reply", decision.selectedCapabilityId)
        assertEquals(CapabilitySelectionSource.REPLY_FALLBACK, decision.selectionSource)
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
        requestedCapabilities: List<RuntimeCapabilityHint> = emptyList(),
    ) = RuntimeRequest(
        requestId = "request-1",
        userInput = userInput,
        selectedModelId = "model-1",
        transcriptContext = emptyList(),
        requestedCapabilities = requestedCapabilities,
    )
}
