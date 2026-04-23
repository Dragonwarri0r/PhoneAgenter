package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.action.ActionNormalizationResult
import com.mobileclaw.app.runtime.capability.StandardToolCatalog
import com.mobileclaw.app.runtime.capability.ToolInvocationKind
import com.mobileclaw.app.runtime.contribution.ContextContribution
import com.mobileclaw.app.runtime.contribution.ContributionOutcomeRecord
import com.mobileclaw.app.runtime.contribution.KnowledgeRequestContribution
import com.mobileclaw.app.runtime.intent.RuntimeIntentHeuristics
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachment
import com.mobileclaw.app.runtime.provider.ExplicitReadToolRequest
import com.mobileclaw.app.runtime.systemsource.SystemSourceContribution
import com.mobileclaw.app.runtime.systemsource.SystemSourceDescriptor
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.app.runtime.memory.ActiveContextSummary
import javax.inject.Inject
import javax.inject.Singleton

enum class RuntimeSourceTrustState {
    TRUSTED,
    UNVERIFIED,
    DENIED,
}

data class RuntimeSourceMetadata(
    val handoffId: String? = null,
    val entryType: String? = null,
    val sourceLabel: String = "",
    val trustState: RuntimeSourceTrustState = RuntimeSourceTrustState.TRUSTED,
    val trustReason: String = "",
    val packageName: String? = null,
    val referrerUri: String? = null,
    val contractVersion: String? = null,
    val compatibilitySummary: String = "",
    val grantSummary: String = "",
    val packageSignatureDigest: String? = null,
    val requestedScopeIds: List<String> = emptyList(),
)

data class RuntimeTranscriptEntry(
    val role: RuntimeTranscriptRole,
    val content: String,
)

enum class RuntimeTranscriptRole {
    USER,
    ASSISTANT,
}

data class RuntimeCapabilityHint(
    val capabilityId: String,
    val providerHint: String? = null,
)

data class RuntimeRequest(
    val requestId: String,
    val userInput: String,
    val selectedModelId: String,
    val transcriptContext: List<RuntimeTranscriptEntry>,
    val originApp: String = "agent_workspace",
    val workspaceId: String = "primary_workspace",
    val subjectKey: String? = null,
    val deviceId: String? = "local_device",
    val requestedCapabilities: List<RuntimeCapabilityHint> = emptyList(),
    val attachments: List<RuntimeAttachment> = emptyList(),
    val sourceMetadata: RuntimeSourceMetadata? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

data class RuntimeContextPayload(
    val summary: String,
    val transcriptTurnCount: Int,
    val hasTranscriptContext: Boolean,
    val personaSummary: String = "",
    val activeContextSummary: ActiveContextSummary? = null,
    val selectedMemoryIds: List<String> = emptyList(),
    val systemSourceDescriptors: List<SystemSourceDescriptor> = emptyList(),
    val systemSourceContributions: List<SystemSourceContribution> = emptyList(),
    val contextContributions: List<ContextContribution> = emptyList(),
    val knowledgeContribution: KnowledgeRequestContribution? = null,
    val contributionOutcomes: List<ContributionOutcomeRecord> = emptyList(),
)

data class RuntimePlan(
    val selectedCapabilityId: String,
    val providerHint: String? = null,
    val structuredAction: ActionNormalizationResult? = null,
    val explicitReadRequest: ExplicitReadToolRequest? = null,
    val selectionOutcome: CapabilitySelectionOutcome? = null,
)

fun interface RuntimeContextLoader {
    suspend fun load(request: RuntimeRequest): RuntimeContextPayload
}

fun interface RuntimePlanner {
    suspend fun plan(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
    ): RuntimePlan
}

@Singleton
class NoOpRuntimeContextLoader @Inject constructor(
    private val appStrings: AppStrings,
) : RuntimeContextLoader {
    override suspend fun load(request: RuntimeRequest): RuntimeContextPayload {
        val turnCount = request.transcriptContext.size
        return RuntimeContextPayload(
            summary = if (turnCount == 0) {
                appStrings.get(R.string.workspace_fresh_session_ready)
            } else {
                appStrings.get(R.string.runtime_transcript_turns_available, turnCount)
            },
            transcriptTurnCount = turnCount,
            hasTranscriptContext = turnCount > 0,
        )
    }
}

@Singleton
class DefaultRuntimePlanner @Inject constructor(
    private val selector: WorkspaceCapabilitySelector,
    private val localCapabilityPlanner: LocalCapabilityPlanner,
    private val standardToolCatalog: StandardToolCatalog,
    private val appStrings: AppStrings,
) : RuntimePlanner {
    override suspend fun plan(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
    ): RuntimePlan {
        val inferredIntent = RuntimeIntentHeuristics.infer(request.userInput)
        val plannerProposal = localCapabilityPlanner.propose(
            request = request,
            contextPayload = contextPayload,
            inferredIntent = inferredIntent,
        )
        val decision = selector.select(
            request = request,
            inferredIntent = inferredIntent,
            plannerProposal = plannerProposal,
        )
        val descriptor = standardToolCatalog.descriptorForCapability(decision.selectedCapabilityId)
        return RuntimePlan(
            selectedCapabilityId = decision.selectedCapabilityId,
            providerHint = request.requestedCapabilities.firstOrNull()?.providerHint,
            selectionOutcome = CapabilitySelectionOutcome(
                selectedCapabilityId = decision.selectedCapabilityId,
                selectedToolId = descriptor.toolId,
                selectionSource = decision.selectionSource,
                resolutionMode = decision.resolutionMode,
                confidence = decision.confidence,
                explanation = selectionExplanation(
                    decision = decision,
                    toolDisplayName = descriptor.displayName,
                    invocationKind = descriptor.invocationKind,
                ),
                warnings = decision.warnings.map(::selectionWarning),
                candidateSummaries = decision.candidateSummaries.ifEmpty {
                    inferredIntent.matchedSignals.take(4)
                },
            ),
        )
    }

    private fun selectionExplanation(
        decision: WorkspaceSelectionDecision,
        toolDisplayName: String,
        invocationKind: ToolInvocationKind,
    ): String {
        return when (decision.reasonCode) {
            "explicit_hint" -> appStrings.get(
                R.string.workspace_selection_reason_explicit_hint,
                toolDisplayName,
            )

            "clear_read_intent" -> appStrings.get(
                R.string.workspace_selection_reason_read_inferred,
                toolDisplayName,
            )

            "model_read_proposal" -> appStrings.get(
                R.string.workspace_selection_reason_read_model,
                toolDisplayName,
            )

            "governed_action_intent" -> appStrings.get(
                R.string.workspace_selection_reason_action_inferred,
                toolDisplayName,
            )

            "model_action_proposal" -> appStrings.get(
                R.string.workspace_selection_reason_action_model,
                toolDisplayName,
            )

            "ambiguous_request" -> appStrings.get(R.string.workspace_selection_reason_ambiguous)
            "blocked_operation" -> appStrings.get(R.string.workspace_selection_reason_blocked)
            else -> if (invocationKind == ToolInvocationKind.REPLY) {
                appStrings.get(R.string.workspace_selection_reason_reply_default)
            } else {
                appStrings.get(R.string.workspace_selection_reason_ambiguous)
            }
        }
    }

    private fun selectionWarning(warningCode: String): String {
        return when (warningCode) {
            "confirmation_expected" -> appStrings.get(
                R.string.workspace_selection_warning_confirmation_required,
            )

            else -> warningCode
        }
    }
}
