package com.mobileclaw.app.runtime.localchat

import android.content.Context
import com.mobileclaw.app.R
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachment
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentKind
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private data class LiteRtSession(
    val descriptor: RuntimeModelDescriptor,
    val engine: Engine,
    val conversation: Conversation,
)

@Singleton
class LiteRtLocalChatGateway @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionStore: InMemoryChatSessionStore,
    private val modelCatalog: ImportedLocalModelCatalog,
    private val appStrings: AppStrings,
) : LocalChatGateway {

    private val runtimeMutex = Mutex()
    private val runtimeSessions = linkedMapOf<String, LiteRtSession>()

    override suspend fun createOrReuseSession(modelId: String): ChatSessionHandle {
        val session = sessionStore.createOrReuse(modelId)
        return ChatSessionHandle(
            sessionId = session.sessionId,
            modelId = session.modelId,
            state = session.state,
        )
    }

    override fun streamAssistantTurn(
        sessionId: String,
        modelId: String,
        userText: String,
        generationPrompt: String,
        attachments: List<RuntimeAttachment>,
        visibleTranscript: List<VisibleTranscriptEntry>,
    ): Flow<SessionStreamEvent> = callbackFlow {
        sessionStore.update(modelId) { session ->
            session.copy(
                state = SessionLifecycleState.STREAMING,
                transcript = visibleTranscript + VisibleTranscriptEntry(
                    role = TranscriptRole.USER,
                    content = userText,
                ),
            )
        }

        val turnId = "assistant-${System.currentTimeMillis()}"
        trySend(SessionStreamEvent.SessionPreparing(sessionId = sessionId))

        val runtimeSession = runCatching { ensureRuntimeSession(modelId) }
            .getOrElse { throwable ->
                sessionStore.update(modelId) { session ->
                    session.copy(state = SessionLifecycleState.FAILED)
                }
                trySend(
                        SessionStreamEvent.AssistantFailed(
                            sessionId = sessionId,
                            turnId = turnId,
                            userMessage = throwable.message ?: appStrings.get(R.string.runtime_failed_initialize_model_runtime),
                            recoverable = true,
                        ),
                )
                close(throwable)
                return@callbackFlow
            }

        trySend(SessionStreamEvent.AssistantStarted(sessionId = sessionId, turnId = turnId))

        val response = StringBuilder()
        val startedAt = System.currentTimeMillis()
        runtimeSession.conversation.sendMessageAsync(
            buildContents(generationPrompt, attachments),
            object : MessageCallback {
                override fun onMessage(message: Message) {
                    val chunk = message.toString()
                    if (chunk.isBlank() || chunk.startsWith("<ctrl")) return
                    response.append(chunk)
                    trySend(
                        SessionStreamEvent.AssistantChunk(
                            sessionId = sessionId,
                            turnId = turnId,
                            chunk = chunk,
                        ),
                    )
                }

                override fun onDone() {
                    val content = response.toString().trim()
                    this@callbackFlow.launch {
                        sessionStoreUpdateSuccess(modelId, content)
                        trySend(
                            SessionStreamEvent.AssistantCompleted(
                                sessionId = sessionId,
                                turnId = turnId,
                                content = content,
                                latencyMs = System.currentTimeMillis() - startedAt,
                            ),
                        )
                        close()
                    }
                }

                override fun onError(throwable: Throwable) {
                    this@callbackFlow.launch {
                        sessionStoreUpdateFailure(modelId)
                        trySend(
                            SessionStreamEvent.AssistantFailed(
                                sessionId = sessionId,
                                turnId = turnId,
                                userMessage = throwable.message ?: appStrings.get(R.string.runtime_local_generation_failed),
                                recoverable = true,
                            ),
                        )
                        close(throwable)
                    }
                }
            },
            emptyMap(),
        )

        awaitClose { }
    }

    override suspend fun resetSession(sessionId: String): SessionResetResult {
        val modelId = sessionId.removePrefix("session-")
        runtimeMutex.withLock {
            runtimeSessions.remove(modelId)?.close()
        }
        sessionStore.clear(modelId)
        return SessionResetResult(
            sessionId = sessionId,
            resetAtEpochMillis = System.currentTimeMillis(),
            wasCleared = true,
            userMessage = appStrings.get(R.string.runtime_session_reset),
        )
    }

    private suspend fun ensureRuntimeSession(modelId: String): LiteRtSession {
        runtimeMutex.withLock {
            runtimeSessions[modelId]?.let { return it }
        }
        val descriptor = modelCatalog.resolveRuntimeModel(modelId)
            ?: error(appStrings.get(R.string.runtime_selected_model_not_ready))
        require(descriptor.filePath.endsWith(".litertlm")) {
            appStrings.get(R.string.runtime_unsupported_model_format)
        }
        val created = createLiteRtSession(descriptor)
        runtimeMutex.withLock {
            runtimeSessions[modelId]?.let {
                created.close()
                return it
            }
            runtimeSessions[modelId] = created
        }
        return created
    }

    private suspend fun createLiteRtSession(descriptor: RuntimeModelDescriptor): LiteRtSession {
        val backends = listOf(Backend.GPU(), Backend.CPU())
        var lastError: Throwable? = null
        backends.forEach { backend ->
            try {
                val engine = Engine(
                    EngineConfig(
                        modelPath = descriptor.filePath,
                        backend = backend,
                        cacheDir = context.getExternalFilesDir(null)?.absolutePath,
                    ),
                )
                engine.initialize()
                val conversation = engine.createConversation(ConversationConfig())
                return LiteRtSession(
                    descriptor = descriptor,
                    engine = engine,
                    conversation = conversation,
                )
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }
        throw IllegalStateException(
            appStrings.get(R.string.runtime_unable_initialize_model, descriptor.displayName),
            lastError,
        )
    }

    private suspend fun sessionStoreUpdateSuccess(modelId: String, content: String) {
        sessionStore.update(modelId) { session ->
            session.copy(
                state = SessionLifecycleState.COMPLETED,
                transcript = session.transcript + VisibleTranscriptEntry(
                    role = TranscriptRole.ASSISTANT,
                    content = content,
                ),
            )
        }
    }

    private suspend fun sessionStoreUpdateFailure(modelId: String) {
        sessionStore.update(modelId) { session ->
            session.copy(state = SessionLifecycleState.FAILED)
        }
    }

    private fun buildContents(
        generationPrompt: String,
        attachments: List<RuntimeAttachment>,
    ): Contents {
        val content = buildList {
            add(Content.Text(generationPrompt))
            attachments.forEach { attachment ->
                when (attachment.kind) {
                    RuntimeAttachmentKind.IMAGE -> add(Content.ImageFile(attachment.localPath))
                    RuntimeAttachmentKind.AUDIO -> add(Content.AudioFile(attachment.localPath))
                }
            }
        }
        return Contents.of(content)
    }
}

private fun LiteRtSession.close() {
    runCatching { conversation.close() }
    runCatching { engine.close() }
}
