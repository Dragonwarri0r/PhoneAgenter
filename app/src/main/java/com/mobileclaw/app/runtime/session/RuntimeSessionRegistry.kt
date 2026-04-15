package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class RuntimeSessionRegistry @Inject constructor(
    private val appStrings: AppStrings,
) {
    companion object {
        private const val MAX_TRACKED_SESSIONS = 24
    }

    private val mutex = Mutex()
    private val _sessions = MutableStateFlow<Map<String, ExecutionSession>>(emptyMap())
    val sessions: StateFlow<Map<String, ExecutionSession>> = _sessions.asStateFlow()

    suspend fun createSession(requestId: String): ExecutionSession = mutex.withLock {
        val sessionId = "exec-${System.currentTimeMillis()}-$requestId"
        val summary = runtimeSummaryForStage(
            sessionId = sessionId,
            stageType = RuntimeStageType.INGRESS,
            details = appStrings.get(R.string.runtime_request_accepted),
            strings = appStrings,
        )
        val session = ExecutionSession(
            sessionId = sessionId,
            requestId = requestId,
            status = ExecutionSessionStatus.ACTIVE,
            currentStage = RuntimeStageType.INGRESS,
            startedAtEpochMillis = System.currentTimeMillis(),
            updatedAtEpochMillis = System.currentTimeMillis(),
            stageOrdinal = 0,
            summary = summary,
        )
        _sessions.value = (_sessions.value + (sessionId to session)).prunedSessions()
        session
    }

    suspend fun advanceStage(
        sessionId: String,
        stageType: RuntimeStageType,
        details: String,
        awaitingInput: Boolean = false,
    ): ExecutionSession? = mutex.withLock {
        val current = _sessions.value[sessionId] ?: return null
        if (current.status != ExecutionSessionStatus.ACTIVE) return current
        val summary = runtimeSummaryForStage(
            sessionId = sessionId,
            stageType = stageType,
            details = details,
            strings = appStrings,
            awaitingInput = awaitingInput,
        )
        val updated = current.copy(
            currentStage = stageType,
            updatedAtEpochMillis = System.currentTimeMillis(),
            stageOrdinal = current.stageOrdinal + 1,
            summary = summary,
        )
        _sessions.value = (_sessions.value + (sessionId to updated)).prunedSessions()
        updated
    }

    suspend fun finishSession(
        sessionId: String,
        terminalState: RuntimeTerminalState,
        userMessage: String,
        outputText: String? = null,
        providerResults: List<String> = emptyList(),
    ): ExecutionSession? = mutex.withLock {
        val current = _sessions.value[sessionId] ?: return null
        if (current.status != ExecutionSessionStatus.ACTIVE) return current
        val stageType = when (terminalState) {
            RuntimeTerminalState.SUCCESS -> RuntimeStageType.COMPLETED
            RuntimeTerminalState.FAILURE -> RuntimeStageType.FAILED
            RuntimeTerminalState.CANCELLED -> RuntimeStageType.CANCELLED
            RuntimeTerminalState.DENIED -> RuntimeStageType.DENIED
        }
        val outcome = SessionOutcome(
            sessionId = sessionId,
            terminalState = terminalState,
            userMessage = userMessage,
            outputText = outputText,
            providerResults = providerResults,
        )
        val updated = current.copy(
            status = when (terminalState) {
                RuntimeTerminalState.SUCCESS -> ExecutionSessionStatus.COMPLETED
                RuntimeTerminalState.FAILURE -> ExecutionSessionStatus.FAILED
                RuntimeTerminalState.CANCELLED -> ExecutionSessionStatus.CANCELLED
                RuntimeTerminalState.DENIED -> ExecutionSessionStatus.DENIED
            },
            currentStage = stageType,
            updatedAtEpochMillis = System.currentTimeMillis(),
            stageOrdinal = current.stageOrdinal + 1,
            outcome = outcome,
            summary = runtimeSummaryForStage(
                sessionId = sessionId,
                stageType = stageType,
                details = userMessage,
                strings = appStrings,
            ),
        )
        _sessions.value = (_sessions.value + (sessionId to updated)).prunedSessions()
        updated
    }

    suspend fun removeSession(sessionId: String) = mutex.withLock {
        _sessions.value = _sessions.value - sessionId
    }

    private fun Map<String, ExecutionSession>.prunedSessions(): Map<String, ExecutionSession> {
        if (size <= MAX_TRACKED_SESSIONS) return this
        return values
            .sortedByDescending { it.updatedAtEpochMillis }
            .take(MAX_TRACKED_SESSIONS)
            .associateBy { it.sessionId }
    }
}
