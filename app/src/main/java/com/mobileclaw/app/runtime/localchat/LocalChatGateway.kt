package com.mobileclaw.app.runtime.localchat

import com.mobileclaw.app.runtime.multimodal.RuntimeAttachment
import kotlinx.coroutines.flow.Flow

data class ChatSessionHandle(
    val sessionId: String,
    val modelId: String,
    val state: SessionLifecycleState,
)

enum class SessionLifecycleState {
    IDLE,
    STREAMING,
    COMPLETED,
    FAILED,
    RESET,
}

data class VisibleTranscriptEntry(
    val role: TranscriptRole,
    val content: String,
)

enum class TranscriptRole {
    USER,
    ASSISTANT,
}

data class SessionResetResult(
    val sessionId: String,
    val resetAtEpochMillis: Long,
    val wasCleared: Boolean,
    val userMessage: String? = null,
)

interface LocalChatGateway {
    suspend fun createOrReuseSession(modelId: String): ChatSessionHandle

    fun streamAssistantTurn(
        sessionId: String,
        modelId: String,
        userText: String,
        generationPrompt: String,
        attachments: List<RuntimeAttachment>,
        visibleTranscript: List<VisibleTranscriptEntry>,
    ): Flow<SessionStreamEvent>

    suspend fun generateDetached(
        modelId: String,
        generationPrompt: String,
        attachments: List<RuntimeAttachment> = emptyList(),
    ): String

    suspend fun resetSession(sessionId: String): SessionResetResult
}
