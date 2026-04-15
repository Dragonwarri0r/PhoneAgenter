package com.mobileclaw.app.runtime.localchat

sealed interface SessionStreamEvent {
    data class SessionPreparing(
        val sessionId: String,
    ) : SessionStreamEvent

    data class AssistantStarted(
        val sessionId: String,
        val turnId: String,
    ) : SessionStreamEvent

    data class AssistantChunk(
        val sessionId: String,
        val turnId: String,
        val chunk: String,
    ) : SessionStreamEvent

    data class AssistantCompleted(
        val sessionId: String,
        val turnId: String,
        val content: String,
        val latencyMs: Long? = null,
    ) : SessionStreamEvent

    data class AssistantFailed(
        val sessionId: String,
        val turnId: String?,
        val userMessage: String,
        val recoverable: Boolean,
    ) : SessionStreamEvent
}

