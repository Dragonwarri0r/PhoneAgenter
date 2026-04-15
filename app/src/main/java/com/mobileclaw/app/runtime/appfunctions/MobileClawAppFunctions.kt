package com.mobileclaw.app.runtime.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.service.AppFunction
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus
import com.mobileclaw.app.runtime.localchat.SessionStreamEvent
import com.mobileclaw.app.runtime.memory.PortabilityCompatibilityLine
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

class MobileClawAppFunctions {

    @AppFunction
    suspend fun draftReply(
        appFunctionContext: AppFunctionContext,
        input: String,
    ): String {
        val dependencies = appFunctionContext.dependencies()
        val appStrings = dependencies.appStrings()
        val model = dependencies.localModelCatalog().models.first()
            .firstOrNull { it.isSelectable && it.availabilityStatus == ModelAvailabilityStatus.READY }
            ?: return appStrings.get(R.string.appfunctions_reply_model_unavailable)
        val gateway = dependencies.localChatGateway()
        val session = gateway.createOrReuseSession(model.modelId)
        var output = ""
        gateway.streamAssistantTurn(
            sessionId = session.sessionId,
            modelId = model.modelId,
            userText = input,
            generationPrompt = input,
            attachments = emptyList(),
            visibleTranscript = emptyList(),
        ).collect { event ->
            when (event) {
                is SessionStreamEvent.AssistantChunk -> output += event.chunk
                is SessionStreamEvent.AssistantCompleted -> output = event.content
                is SessionStreamEvent.AssistantFailed -> throw IllegalStateException(event.userMessage)
                else -> Unit
            }
        }
        return output.ifBlank { appStrings.get(R.string.appfunctions_reply_empty) }
    }

    @AppFunction
    suspend fun exportPortableSummary(
        appFunctionContext: AppFunctionContext,
        memoryId: String,
    ): String {
        val dependencies = appFunctionContext.dependencies()
        val repository = dependencies.scopedMemoryRepository()
        val item = repository.get(memoryId)
            ?: return dependencies.appStrings().get(R.string.appfunctions_memory_not_found)
        val exportDecisionService = dependencies.exportDecisionService()
        val policy = exportDecisionService.evaluateRedactionPolicy(item)
        if (!policy.allowSummaryExport && !policy.allowFullExport) {
            return policy.reason
        }
        val bundle = exportDecisionService.buildExportBundle(item)
        val compatibilities = exportDecisionService.extensionCompatibilities(item)
        return dependencies.portabilityBundleFormatter().buildBundleDocument(
            bundle = bundle,
            compatibilityLines = compatibilities.map { compatibility ->
                PortabilityCompatibilityLine(
                    title = compatibility.displayName,
                    detail = compatibility.reason,
                    isCompatible = compatibility.isCompatible,
                )
            },
        )
    }
}

private fun AppFunctionContext.dependencies(): MobileClawAppFunctionEntryPoint {
    return EntryPointAccessors.fromApplication(
        context.applicationContext,
        MobileClawAppFunctionEntryPoint::class.java,
    )
}
