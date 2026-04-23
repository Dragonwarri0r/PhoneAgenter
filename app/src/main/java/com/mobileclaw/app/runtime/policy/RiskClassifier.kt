package com.mobileclaw.app.runtime.policy

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.action.PayloadCompletenessState
import com.mobileclaw.app.runtime.intent.RuntimeIntentHeuristics
import com.mobileclaw.app.runtime.session.CapabilityResolutionMode
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskClassifier @Inject constructor(
    private val appStrings: AppStrings,
) {
    suspend fun classify(
        sessionId: String,
        request: RuntimeRequest,
        plan: RuntimePlan,
    ): RiskAssessment {
        val inferredIntent = RuntimeIntentHeuristics.infer(request.userInput)
        val selectedOutcome = plan.selectionOutcome
        val isWorkspaceFreeformChat = request.originApp == "agent_workspace" &&
            request.sourceMetadata == null &&
            request.requestedCapabilities.isEmpty()
        val scope = when {
            inferredIntent.containsBlockedOperation -> ActionScope.BLOCKED_OPERATION
            selectedOutcome?.resolutionMode == CapabilityResolutionMode.REPLY_FALLBACK -> {
                ActionScope.REPLY_GENERATE
            }
            ActionScope.fromSelectionOutcome(selectedOutcome) != null -> {
                ActionScope.fromSelectionOutcome(selectedOutcome)!!
            }
            else -> ActionScope.infer(
                userInput = request.userInput,
                capabilityId = plan.selectedCapabilityId,
            )
        }
        val signals = buildList {
            add("scope:${scope.scopeId}")
            add("capability:${plan.selectedCapabilityId}")
            addAll(inferredIntent.matchedSignals)
            if (isWorkspaceFreeformChat) add("mode:workspace_freeform_chat")
            selectedOutcome?.let { outcome ->
                add("selection_mode:${outcome.resolutionMode.name.lowercase()}")
                add("selection_source:${outcome.selectionSource.name.lowercase()}")
            }
            plan.structuredAction?.payload?.let { payload ->
                add("structured:${payload.actionType.capabilityId}")
                add("structured_completeness:${payload.completenessState.name.lowercase()}")
            }
            if (request.transcriptContext.isNotEmpty()) add("transcript:present")
            if (request.originApp.isNotBlank()) add("origin:${request.originApp}")
            if (inferredIntent.containsSensitiveContent) add("signal:sensitive_content")
            if (inferredIntent.containsBlockedOperation) add("signal:blocked_operation")
        }
        val completeness = plan.structuredAction?.payload?.completenessState
        val riskLevel = when {
            inferredIntent.containsBlockedOperation || scope.riskMode == ActionRiskMode.BLOCKED -> RiskLevel.BLOCKED
            selectedOutcome?.resolutionMode == CapabilityResolutionMode.REPLY_FALLBACK -> RiskLevel.LOW
            completeness == PayloadCompletenessState.INSUFFICIENT -> RiskLevel.HIGH
            scope == ActionScope.EXTERNAL_SHARE ||
                scope == ActionScope.ALARM_SET ||
                scope == ActionScope.ALARM_DISMISS ||
                scope == ActionScope.UI_ACT ||
                scope == ActionScope.SENSITIVE_WRITE ||
                inferredIntent.containsSensitiveContent -> RiskLevel.HIGH

            completeness == PayloadCompletenessState.PARTIAL -> RiskLevel.MEDIUM
            scope.riskMode == ActionRiskMode.HARD_CONFIRM -> RiskLevel.MEDIUM
            scope == ActionScope.UNKNOWN -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        val rationale = when (riskLevel) {
            RiskLevel.LOW -> appStrings.get(
                R.string.policy_risk_low,
                appStrings.actionScopeLabel(scope),
            )

            RiskLevel.MEDIUM -> appStrings.get(
                R.string.policy_risk_medium,
                appStrings.actionScopeLabel(scope),
            )

            RiskLevel.HIGH -> appStrings.get(
                R.string.policy_risk_high,
                appStrings.actionScopeLabel(scope),
            )

            RiskLevel.BLOCKED -> appStrings.get(
                R.string.policy_risk_blocked,
                appStrings.actionScopeLabel(scope),
            )
        }
        return RiskAssessment(
            assessmentId = "risk-${System.currentTimeMillis()}-${request.requestId}",
            sessionId = sessionId,
            requestId = request.requestId,
            capabilityId = plan.selectedCapabilityId,
            scopeId = scope.scopeId,
            riskLevel = riskLevel,
            rationale = rationale,
            signals = signals,
            confidence = when (riskLevel) {
                RiskLevel.LOW -> selectedOutcome?.confidence?.coerceAtLeast(0.6)
                    ?: inferredIntent.confidence.coerceAtLeast(0.6)
                RiskLevel.MEDIUM -> (inferredIntent.confidence + 0.06).coerceAtMost(0.88)
                RiskLevel.HIGH -> (inferredIntent.confidence + 0.1).coerceAtMost(0.93)
                RiskLevel.BLOCKED -> (inferredIntent.confidence + 0.05).coerceAtMost(0.99)
            },
        )
    }
}
