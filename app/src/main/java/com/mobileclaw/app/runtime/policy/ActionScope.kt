package com.mobileclaw.app.runtime.policy

import com.mobileclaw.app.runtime.intent.RuntimeIntentHeuristics
import com.mobileclaw.app.runtime.session.CapabilityResolutionMode
import com.mobileclaw.app.runtime.session.CapabilitySelectionOutcome

enum class ActionRiskMode {
    AUTO_ALLOWED,
    HARD_CONFIRM,
    BLOCKED,
}

enum class ActionScope(
    val scopeId: String,
    val riskMode: ActionRiskMode,
) {
    REPLY_GENERATE(
        scopeId = "reply.generate",
        riskMode = ActionRiskMode.AUTO_ALLOWED,
    ),
    CALENDAR_READ(
        scopeId = "calendar.read",
        riskMode = ActionRiskMode.AUTO_ALLOWED,
    ),
    MESSAGE_SEND(
        scopeId = "message.send",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    CALENDAR_WRITE(
        scopeId = "calendar.write",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    CALENDAR_DELETE(
        scopeId = "calendar.delete",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    ALARM_SET(
        scopeId = "alarm.set",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    ALARM_SHOW(
        scopeId = "alarm.show",
        riskMode = ActionRiskMode.AUTO_ALLOWED,
    ),
    ALARM_DISMISS(
        scopeId = "alarm.dismiss",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    EXTERNAL_SHARE(
        scopeId = "external.share",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    UI_ACT(
        scopeId = "ui.act",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    SENSITIVE_WRITE(
        scopeId = "sensitive.write",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    BLOCKED_OPERATION(
        scopeId = "blocked.operation",
        riskMode = ActionRiskMode.BLOCKED,
    ),
    UNKNOWN(
        scopeId = "unknown",
        riskMode = ActionRiskMode.HARD_CONFIRM,
    ),
    ;

    companion object {
        fun fromScopeId(scopeId: String): ActionScope {
            return entries.firstOrNull { it.scopeId == scopeId } ?: UNKNOWN
        }

        fun fromCapabilityId(capabilityId: String): ActionScope {
            return when (capabilityId) {
                "generate.reply" -> REPLY_GENERATE
                "calendar.read" -> CALENDAR_READ
                "message.send" -> MESSAGE_SEND
                "calendar.write" -> CALENDAR_WRITE
                "calendar.delete" -> CALENDAR_DELETE
                "alarm.set" -> ALARM_SET
                "alarm.show" -> ALARM_SHOW
                "alarm.dismiss" -> ALARM_DISMISS
                "external.share" -> EXTERNAL_SHARE
                "ui.act" -> UI_ACT
                "sensitive.write" -> SENSITIVE_WRITE
                "blocked.operation" -> BLOCKED_OPERATION
                else -> UNKNOWN
            }
        }

        fun infer(
            userInput: String,
            capabilityId: String,
        ): ActionScope {
            val inferred = RuntimeIntentHeuristics.infer(userInput)
            val capabilityScope = fromCapabilityId(capabilityId)
            return when {
                inferred.scope == BLOCKED_OPERATION -> BLOCKED_OPERATION
                capabilityScope == UNKNOWN -> inferred.scope
                capabilityScope == REPLY_GENERATE && inferred.scope != REPLY_GENERATE -> inferred.scope
                else -> capabilityScope
            }
        }

        fun fromSelectionOutcome(outcome: CapabilitySelectionOutcome?): ActionScope? {
            return when (outcome?.resolutionMode) {
                CapabilityResolutionMode.REPLY_FALLBACK -> REPLY_GENERATE
                CapabilityResolutionMode.EXPLICIT_READ,
                CapabilityResolutionMode.EXPLICIT_ACTION,
                -> fromCapabilityId(outcome.selectedCapabilityId)

                else -> null
            }
        }
    }
}
