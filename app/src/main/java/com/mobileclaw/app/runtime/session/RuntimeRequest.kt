package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.action.ActionNormalizationResult
import com.mobileclaw.app.runtime.intent.RuntimeIntentHeuristics
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachment
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
)

data class RuntimePlan(
    val selectedCapabilityId: String,
    val providerHint: String? = null,
    val structuredAction: ActionNormalizationResult? = null,
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
class DefaultRuntimePlanner @Inject constructor() : RuntimePlanner {
    override suspend fun plan(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
    ): RuntimePlan {
        val hint = request.requestedCapabilities.firstOrNull()
        if (hint == null && request.originApp == "agent_workspace" && request.sourceMetadata == null) {
            return RuntimePlan(
                selectedCapabilityId = "generate.reply",
                providerHint = null,
            )
        }
        val inferredIntent = RuntimeIntentHeuristics.infer(request.userInput)
        return RuntimePlan(
            selectedCapabilityId = hint?.capabilityId ?: inferredIntent.capabilityId,
            providerHint = hint?.providerHint,
        )
    }
}
