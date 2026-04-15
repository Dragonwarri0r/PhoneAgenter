package com.mobileclaw.app.runtime.localchat

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class StoredChatSession(
    val sessionId: String,
    val modelId: String,
    val state: SessionLifecycleState,
    val transcript: List<VisibleTranscriptEntry>,
)

@Singleton
class InMemoryChatSessionStore @Inject constructor() {
    private val mutex = Mutex()
    private val sessionsByModelId = linkedMapOf<String, StoredChatSession>()

    suspend fun createOrReuse(modelId: String): StoredChatSession = mutex.withLock {
        sessionsByModelId[modelId] ?: StoredChatSession(
            sessionId = "session-$modelId",
            modelId = modelId,
            state = SessionLifecycleState.IDLE,
            transcript = emptyList(),
        ).also { sessionsByModelId[modelId] = it }
    }

    suspend fun update(
        modelId: String,
        transform: (StoredChatSession) -> StoredChatSession,
    ): StoredChatSession = mutex.withLock {
        val current = sessionsByModelId[modelId] ?: StoredChatSession(
            sessionId = "session-$modelId",
            modelId = modelId,
            state = SessionLifecycleState.IDLE,
            transcript = emptyList(),
        )
        val updated = transform(current)
        sessionsByModelId[modelId] = updated
        updated
    }

    suspend fun clear(modelId: String): StoredChatSession? = mutex.withLock {
        val cleared = sessionsByModelId.remove(modelId) ?: return null
        cleared.copy(state = SessionLifecycleState.RESET, transcript = emptyList())
    }
}

