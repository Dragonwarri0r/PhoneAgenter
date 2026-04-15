package com.mobileclaw.app.runtime.provider

import com.mobileclaw.app.runtime.localchat.LocalChatGateway
import com.mobileclaw.app.runtime.localchat.SessionStreamEvent
import com.mobileclaw.app.runtime.localchat.TranscriptRole
import com.mobileclaw.app.runtime.localchat.VisibleTranscriptEntry
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.session.RuntimeTranscriptRole
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@Singleton
class LocalGenerationProvider @Inject constructor(
    private val localChatGateway: LocalChatGateway,
    private val promptComposer: LocalGenerationPromptComposer,
) : CapabilityProvider {
    private val supportedCapabilities = setOf(
        "generate.reply",
        "message.send",
        "calendar.write",
        "external.share",
        "ui.act",
        "sensitive.write",
    )

    override val providerId: String = "local_generation"

    override fun supports(plan: RuntimePlan): Boolean {
        return plan.selectedCapabilityId in supportedCapabilities
    }

    override fun execute(
        request: CapabilityExecutionRequest,
    ): Flow<ProviderExecutionEvent> = flow {
        emit(
            ProviderExecutionEvent.ExecutionStarted(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
            ),
        )
        val localSession = localChatGateway.createOrReuseSession(request.request.selectedModelId)
        val generationPrompt = promptComposer.compose(
            request = request.request,
            contextPayload = request.contextPayload,
        )
        localChatGateway.streamAssistantTurn(
            sessionId = localSession.sessionId,
            modelId = request.request.selectedModelId,
            userText = request.request.userInput,
            generationPrompt = generationPrompt,
            attachments = request.request.attachments,
            visibleTranscript = request.request.transcriptContext.map { entry ->
                VisibleTranscriptEntry(
                    role = if (entry.role == RuntimeTranscriptRole.USER) {
                        TranscriptRole.USER
                    } else {
                        TranscriptRole.ASSISTANT
                    },
                    content = entry.content,
                )
            },
        ).collect { event ->
            when (event) {
                is SessionStreamEvent.AssistantChunk -> {
                    emit(
                        ProviderExecutionEvent.OutputChunk(
                            capabilityId = request.plan.selectedCapabilityId,
                            providerId = providerId,
                            chunk = event.chunk,
                        ),
                    )
                }

                is SessionStreamEvent.AssistantCompleted -> {
                    emit(
                        ProviderExecutionEvent.ExecutionCompleted(
                            capabilityId = request.plan.selectedCapabilityId,
                            providerId = providerId,
                            outputText = event.content,
                        ),
                    )
                }

                is SessionStreamEvent.AssistantFailed -> {
                    emit(
                        ProviderExecutionEvent.ExecutionFailed(
                            capabilityId = request.plan.selectedCapabilityId,
                            providerId = providerId,
                            userMessage = event.userMessage,
                        ),
                    )
                }

                else -> Unit
            }
        }
    }

    override suspend fun resetModelSession(modelId: String) =
        localChatGateway.resetSession(
            sessionId = localChatGateway.createOrReuseSession(modelId).sessionId,
        )
}
