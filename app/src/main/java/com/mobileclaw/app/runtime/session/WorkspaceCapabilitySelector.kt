package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.runtime.intent.RuntimeIntentInference
import javax.inject.Inject
import javax.inject.Singleton

data class WorkspaceSelectionDecision(
    val selectedCapabilityId: String,
    val selectionSource: CapabilitySelectionSource,
    val resolutionMode: CapabilityResolutionMode,
    val confidence: Double,
    val reasonCode: String,
    val warnings: List<String> = emptyList(),
    val candidateSummaries: List<String> = emptyList(),
)

@Singleton
class WorkspaceCapabilitySelector @Inject constructor() {
    fun select(
        request: RuntimeRequest,
        inferredIntent: RuntimeIntentInference,
        plannerProposal: CapabilityPlanningProposal? = null,
    ): WorkspaceSelectionDecision {
        val explicitHint = request.requestedCapabilities.firstOrNull()
        if (explicitHint != null) {
            return WorkspaceSelectionDecision(
                selectedCapabilityId = explicitHint.capabilityId,
                selectionSource = CapabilitySelectionSource.EXPLICIT_HINT,
                resolutionMode = resolutionModeFor(explicitHint.capabilityId),
                confidence = 0.99,
                reasonCode = "explicit_hint",
            )
        }

        if (inferredIntent.containsBlockedOperation) {
            return replyFallback(
                confidence = inferredIntent.confidence,
                reasonCode = "blocked_operation",
            )
        }

        selectFromPlannerProposal(
            request = request,
            inferredIntent = inferredIntent,
            proposal = plannerProposal,
        )?.let { return it }

        if (request.originApp != "agent_workspace" || request.sourceMetadata != null) {
            return if (inferredIntent.capabilityId == "generate.reply") {
                replyFallback(
                    confidence = inferredIntent.confidence,
                    reasonCode = "reply_default",
                )
            } else {
                WorkspaceSelectionDecision(
                    selectedCapabilityId = inferredIntent.capabilityId,
                    selectionSource = CapabilitySelectionSource.FREEFORM_INFERENCE,
                    resolutionMode = resolutionModeFor(inferredIntent.capabilityId),
                    confidence = inferredIntent.confidence,
                    reasonCode = if (resolutionModeFor(inferredIntent.capabilityId) ==
                        CapabilityResolutionMode.EXPLICIT_READ
                    ) {
                        "clear_read_intent"
                    } else {
                        "governed_action_intent"
                    },
                    candidateSummaries = inferredIntent.matchedSignals.take(4),
                )
            }
        }

        if (inferredIntent.capabilityId == "generate.reply") {
            return replyFallback(
                confidence = inferredIntent.confidence,
                reasonCode = "reply_default",
            )
        }

        return when (resolutionModeFor(inferredIntent.capabilityId)) {
            CapabilityResolutionMode.EXPLICIT_READ -> {
                if (isClearReadRequest(
                        rawInput = request.userInput,
                        capabilityId = inferredIntent.capabilityId,
                        confidence = inferredIntent.confidence,
                    )
                ) {
                    WorkspaceSelectionDecision(
                        selectedCapabilityId = inferredIntent.capabilityId,
                        selectionSource = CapabilitySelectionSource.FREEFORM_INFERENCE,
                        resolutionMode = CapabilityResolutionMode.EXPLICIT_READ,
                        confidence = inferredIntent.confidence.coerceAtLeast(0.82),
                        reasonCode = "clear_read_intent",
                        candidateSummaries = inferredIntent.matchedSignals.take(4),
                    )
                } else {
                    replyFallback(
                        confidence = inferredIntent.confidence,
                        reasonCode = "ambiguous_request",
                    )
                }
            }

            CapabilityResolutionMode.EXPLICIT_ACTION -> {
                if (isClearActionRequest(
                        rawInput = request.userInput,
                        capabilityId = inferredIntent.capabilityId,
                        confidence = inferredIntent.confidence,
                    )
                ) {
                    WorkspaceSelectionDecision(
                        selectedCapabilityId = inferredIntent.capabilityId,
                        selectionSource = CapabilitySelectionSource.FREEFORM_INFERENCE,
                        resolutionMode = CapabilityResolutionMode.EXPLICIT_ACTION,
                        confidence = inferredIntent.confidence.coerceAtLeast(0.84),
                        reasonCode = "governed_action_intent",
                        warnings = listOf("confirmation_expected"),
                        candidateSummaries = inferredIntent.matchedSignals.take(4),
                    )
                } else {
                    replyFallback(
                        confidence = inferredIntent.confidence,
                        reasonCode = "ambiguous_request",
                    )
                }
            }

            else -> replyFallback(
                confidence = inferredIntent.confidence,
                reasonCode = "reply_default",
            )
        }
    }

    private fun replyFallback(
        confidence: Double,
        reasonCode: String,
    ) = WorkspaceSelectionDecision(
        selectedCapabilityId = "generate.reply",
        selectionSource = CapabilitySelectionSource.REPLY_FALLBACK,
        resolutionMode = CapabilityResolutionMode.REPLY_FALLBACK,
        confidence = confidence,
        reasonCode = reasonCode,
    )

    private fun selectFromPlannerProposal(
        request: RuntimeRequest,
        inferredIntent: RuntimeIntentInference,
        proposal: CapabilityPlanningProposal?,
    ): WorkspaceSelectionDecision? {
        proposal ?: return null
        if (proposal.capabilityId == "generate.reply") return null
        if (proposal.confidence < 0.6) return null

        val candidateSummaries = inferredIntent.matchedSignals.take(3)

        return when (resolutionModeFor(proposal.capabilityId)) {
            CapabilityResolutionMode.EXPLICIT_READ -> {
                if (isClearReadRequest(
                        rawInput = request.userInput,
                        capabilityId = proposal.capabilityId,
                        confidence = proposal.confidence,
                    )
                ) {
                    WorkspaceSelectionDecision(
                        selectedCapabilityId = proposal.capabilityId,
                        selectionSource = CapabilitySelectionSource.MODEL_PLANNER,
                        resolutionMode = CapabilityResolutionMode.EXPLICIT_READ,
                        confidence = proposal.confidence.coerceAtLeast(0.78),
                        reasonCode = "model_read_proposal",
                        candidateSummaries = candidateSummaries,
                    )
                } else {
                    null
                }
            }

            CapabilityResolutionMode.EXPLICIT_ACTION -> {
                if (isClearActionRequest(
                        rawInput = request.userInput,
                        capabilityId = proposal.capabilityId,
                        confidence = proposal.confidence,
                    )
                ) {
                    WorkspaceSelectionDecision(
                        selectedCapabilityId = proposal.capabilityId,
                        selectionSource = CapabilitySelectionSource.MODEL_PLANNER,
                        resolutionMode = CapabilityResolutionMode.EXPLICIT_ACTION,
                        confidence = proposal.confidence.coerceAtLeast(0.8),
                        reasonCode = "model_action_proposal",
                        warnings = listOf("confirmation_expected"),
                        candidateSummaries = candidateSummaries,
                    )
                } else {
                    null
                }
            }

            else -> null
        }
    }

    private fun resolutionModeFor(capabilityId: String): CapabilityResolutionMode {
        return when (capabilityId) {
            "calendar.read", "alarm.show", "contacts.read" -> CapabilityResolutionMode.EXPLICIT_READ
            "generate.reply" -> CapabilityResolutionMode.REPLY_FALLBACK
            else -> CapabilityResolutionMode.EXPLICIT_ACTION
        }
    }

    private fun isClearReadRequest(
        rawInput: String,
        capabilityId: String,
        confidence: Double,
    ): Boolean {
        val lower = rawInput.lowercase()
        if (confidence < 0.7) return false
        return when (capabilityId) {
            "calendar.read" -> {
                val mentionsCalendar = listOf(
                    "calendar",
                    "schedule",
                    "agenda",
                    "日历",
                    "日程",
                    "安排",
                ).any(lower::contains)
                val mentionsLookupWindow = listOf(
                    "today",
                    "tomorrow",
                    "this week",
                    "next week",
                    "this afternoon",
                    "tonight",
                    "今天",
                    "明天",
                    "本周",
                    "下周",
                    "下午",
                    "今晚",
                ).any(lower::contains)
                val looksLikeQuestion = listOf(
                    "what",
                    "show",
                    "check",
                    "read",
                    "look up",
                    "list",
                    "看看",
                    "查看",
                    "看下",
                    "看一下",
                    "查",
                    "查下",
                    "查一下",
                    "看看我",
                    "有什么",
                ).any(lower::contains)
                mentionsCalendar && (mentionsLookupWindow || looksLikeQuestion)
            }

            "alarm.show" -> {
                listOf("alarm", "alarms", "闹钟").any(lower::contains) &&
                    listOf("show", "open", "check", "查看", "打开", "看下").any(lower::contains)
            }

            else -> confidence >= 0.88
        }
    }

    private fun isClearActionRequest(
        rawInput: String,
        capabilityId: String,
        confidence: Double,
    ): Boolean {
        val lower = rawInput.lowercase()
        if (confidence < 0.72) return false
        val hasActionVerb = listOf(
            "send",
            "text",
            "email",
            "share",
            "post",
            "publish",
            "create",
            "add",
            "schedule",
            "set",
            "dismiss",
            "open",
            "click",
            "tap",
            "reply to",
            "send to",
            "发",
            "发给",
            "分享",
            "发布",
            "创建",
            "添加",
            "安排",
            "设置",
            "关闭",
            "打开",
            "点击",
        ).any(lower::contains)
        val hasTimeReference = listOf(
            "tomorrow",
            "today",
            "tonight",
            "next",
            "am",
            "pm",
            "点",
            "今天",
            "明天",
            "下午",
            "晚上",
        ).any(lower::contains) || Regex("""\b\d{1,2}(:\d{2})?\b""").containsMatchIn(lower)
        return when (capabilityId) {
            "calendar.write" -> hasActionVerb && hasTimeReference
            "calendar.delete" -> {
                val hasDeleteVerb = listOf(
                    "delete",
                    "remove",
                    "cancel",
                    "删掉",
                    "删除",
                    "取消",
                ).any(lower::contains)
                val hasCalendarContext = listOf(
                    "calendar",
                    "event",
                    "meeting",
                    "schedule",
                    "日历",
                    "日程",
                    "事件",
                    "会议",
                ).any(lower::contains)
                hasDeleteVerb && (hasCalendarContext || hasTimeReference)
            }

            "message.send", "external.share", "alarm.set", "alarm.dismiss", "ui.act", "sensitive.write" -> {
                hasActionVerb
            }

            else -> false
        }
    }
}
