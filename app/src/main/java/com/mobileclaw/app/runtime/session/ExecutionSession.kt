package com.mobileclaw.app.runtime.session

enum class ExecutionSessionStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    CANCELLED,
    DENIED,
}

enum class RuntimeTerminalState {
    SUCCESS,
    FAILURE,
    CANCELLED,
    DENIED,
}

data class SessionOutcome(
    val sessionId: String,
    val terminalState: RuntimeTerminalState,
    val userMessage: String,
    val outputText: String? = null,
    val providerResults: List<String> = emptyList(),
    val finishedAtEpochMillis: Long = System.currentTimeMillis(),
)

data class ExecutionSession(
    val sessionId: String,
    val requestId: String,
    val status: ExecutionSessionStatus,
    val currentStage: RuntimeStageType,
    val startedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val stageOrdinal: Int,
    val outcome: SessionOutcome? = null,
    val summary: RuntimeStatusSummary,
)

