package com.mobileclaw.app.runtime.provider

import com.mobileclaw.app.runtime.capability.ProviderDescriptor
import com.mobileclaw.app.runtime.action.StructuredActionPayload
import com.mobileclaw.app.runtime.action.StructuredExecutionPreview
import com.mobileclaw.app.runtime.session.CapabilitySelectionOutcome
import com.mobileclaw.app.runtime.localchat.SessionResetResult
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.session.RuntimeRequest
import kotlinx.coroutines.flow.Flow

data class CapabilityExecutionRequest(
    val sessionId: String,
    val request: RuntimeRequest,
    val contextPayload: RuntimeContextPayload,
    val plan: RuntimePlan,
    val providerDescriptor: ProviderDescriptor? = null,
    val structuredPayload: StructuredActionPayload? = plan.structuredAction?.payload,
    val structuredPreview: StructuredExecutionPreview? = plan.structuredAction?.preview,
    val explicitReadRequest: ExplicitReadToolRequest? = plan.explicitReadRequest,
    val selectionOutcome: CapabilitySelectionOutcome? = plan.selectionOutcome,
)

sealed interface ProviderExecutionEvent {
    data class ExecutionStarted(
        val capabilityId: String,
        val providerId: String,
    ) : ProviderExecutionEvent

    data class OutputChunk(
        val capabilityId: String,
        val providerId: String,
        val chunk: String,
    ) : ProviderExecutionEvent

    data class ExecutionCompleted(
        val capabilityId: String,
        val providerId: String,
        val outputText: String,
        val readResult: ReadToolResult? = null,
    ) : ProviderExecutionEvent

    data class ExecutionFailed(
        val capabilityId: String,
        val providerId: String,
        val userMessage: String,
    ) : ProviderExecutionEvent
}

interface CapabilityProvider {
    val providerId: String

    fun supports(plan: RuntimePlan): Boolean

    fun execute(
        request: CapabilityExecutionRequest,
    ): Flow<ProviderExecutionEvent>

    suspend fun resetModelSession(modelId: String): SessionResetResult? = null
}
