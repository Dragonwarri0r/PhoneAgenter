package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.runtime.intent.RuntimeIntentInference
import com.mobileclaw.app.runtime.localchat.LocalChatGateway
import javax.inject.Inject
import javax.inject.Singleton

data class CapabilityPlanningProposal(
    val capabilityId: String,
    val confidence: Double,
    val rationale: String,
)

@Singleton
class LocalCapabilityPlanner @Inject constructor(
    private val localChatGateway: LocalChatGateway,
) {
    suspend fun propose(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
        inferredIntent: RuntimeIntentInference,
    ): CapabilityPlanningProposal? {
        if (!shouldUsePlanner(request = request, inferredIntent = inferredIntent)) {
            return null
        }

        val rawResponse = runCatching {
            localChatGateway.generateDetached(
                modelId = request.selectedModelId,
                generationPrompt = buildPlannerPrompt(
                    request = request,
                    contextPayload = contextPayload,
                ),
            )
        }.getOrNull() ?: return null

        return parseProposal(rawResponse)
    }

    private fun shouldUsePlanner(
        request: RuntimeRequest,
        inferredIntent: RuntimeIntentInference,
    ): Boolean {
        if (request.originApp != "agent_workspace") return false
        if (request.userInput.isBlank()) return false
        if (request.requestedCapabilities.isNotEmpty()) return false
        if (request.sourceMetadata != null) return false
        if (request.attachments.isNotEmpty()) return false
        if (inferredIntent.containsBlockedOperation) return false
        return inferredIntent.capabilityId != "generate.reply" || looksCapabilityRelevant(request.userInput)
    }

    private fun looksCapabilityRelevant(rawInput: String): Boolean {
        val lower = rawInput.lowercase()
        return listOf(
            "calendar",
            "schedule",
            "agenda",
            "event",
            "meeting",
            "alarm",
            "clock",
            "message",
            "text",
            "share",
            "contact",
            "日历",
            "日程",
            "安排",
            "会议",
            "闹钟",
            "消息",
            "短信",
            "分享",
            "联系人",
        ).any(lower::contains)
    }

    private fun buildPlannerPrompt(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
    ): String {
        val deviceAccessLines = contextPayload.systemSourceDescriptors
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n") { descriptor ->
                "- ${descriptor.displayName}: ${descriptor.availabilitySummary}"
            }
            ?: "- No extra device context is attached yet."

        return """
            You are the capability planner for a local Android agent.
            Choose the single safest capability for the latest user request.
            Return JSON only with this shape:
            {"capabilityId":"generate.reply","confidence":0.0,"reason":"short explanation"}

            Allowed capabilityId values:
            - generate.reply
            - calendar.read
            - calendar.write
            - calendar.delete
            - alarm.show
            - alarm.set
            - alarm.dismiss
            - message.send
            - external.share
            - ui.act
            - sensitive.write

            Rules:
            - Prefer generate.reply for drafting, brainstorming, summarization, explanation, or ambiguous conversation.
            - Use calendar.read only for an actual calendar lookup.
            - Use calendar.write only when the user clearly wants to create or move a calendar event.
            - Use calendar.delete only when the user clearly wants to remove or cancel a specific calendar event.
            - Use message.send, external.share, ui.act, or sensitive.write only when the user clearly wants the side effect to happen now.
            - Never choose a side-effect capability if the request is missing core details. In that case choose generate.reply.
            - If the user is only asking whether the agent can do something, choose generate.reply unless they are also asking for the lookup or action right now.

            Device access:
            $deviceAccessLines

            Latest user request:
            ${request.userInput}
        """.trimIndent()
    }

    private fun parseProposal(rawResponse: String): CapabilityPlanningProposal? {
        val jsonPayload = extractJsonObject(rawResponse) ?: return null
        val capabilityId = normalizeCapabilityId(
            extractStringValue(jsonPayload, "capabilityId") ?: return null,
        ) ?: return null
        val confidence = extractNumberValue(jsonPayload, "confidence")
            ?.coerceIn(0.0, 1.0)
            ?: 0.0
        val rationale = listOf("reason", "rationale")
            .firstNotNullOfOrNull { key -> extractStringValue(jsonPayload, key) }
            .orEmpty()
            .trim()
        return CapabilityPlanningProposal(
            capabilityId = capabilityId,
            confidence = confidence,
            rationale = rationale,
        )
    }

    private fun extractJsonObject(rawResponse: String): String? {
        val fenced = Regex("""```(?:json)?\s*(\{[\s\S]*\})\s*```""")
            .find(rawResponse)
            ?.groupValues
            ?.getOrNull(1)
        if (fenced != null) return fenced
        return Regex("""\{[\s\S]*}""")
            .find(rawResponse)
            ?.value
    }

    private fun extractStringValue(
        jsonPayload: String,
        key: String,
    ): String? {
        return Regex("""\"$key\"\s*:\s*\"([^\"]+)\"""")
            .find(jsonPayload)
            ?.groupValues
            ?.getOrNull(1)
    }

    private fun extractNumberValue(
        jsonPayload: String,
        key: String,
    ): Double? {
        return Regex("""\"$key\"\s*:\s*([0-9]+(?:\.[0-9]+)?)""")
            .find(jsonPayload)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
    }

    private fun normalizeCapabilityId(rawCapabilityId: String): String? {
        return when (
            rawCapabilityId.lowercase()
                .replace("_", ".")
                .replace(" ", "")
        ) {
            "generate.reply", "reply", "replyfallback" -> "generate.reply"
            "calendar.read", "calendarread" -> "calendar.read"
            "calendar.write", "calendarwrite", "calendar.create", "calendarcreate" -> {
                "calendar.write"
            }

            "calendar.delete", "calendardelete", "calendar.remove", "calendarremove" -> {
                "calendar.delete"
            }

            "alarm.show", "alarmshow" -> "alarm.show"
            "alarm.set", "alarmset" -> "alarm.set"
            "alarm.dismiss", "alarmdismiss" -> "alarm.dismiss"
            "message.send", "messagesend" -> "message.send"
            "external.share", "externalshare" -> "external.share"
            "ui.act", "uiact" -> "ui.act"
            "sensitive.write", "sensitivewrite" -> "sensitive.write"
            else -> null
        }
    }
}
