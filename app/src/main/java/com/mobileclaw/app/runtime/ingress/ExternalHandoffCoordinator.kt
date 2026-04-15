package com.mobileclaw.app.runtime.ingress

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ExternalHandoffEvent {
    val eventId: String

    data class Pending(
        override val eventId: String,
        val envelope: InteropRequestEnvelope,
    ) : ExternalHandoffEvent

    data class Rejected(
        override val eventId: String,
        val sourceLabel: String,
        val trustState: ExternalTrustState,
        val trustReason: String,
        val reason: String,
    ) : ExternalHandoffEvent
}

@Singleton
class ExternalHandoffCoordinator @Inject constructor() {
    private val _pendingEvent = MutableStateFlow<ExternalHandoffEvent?>(null)
    val pendingEvent: StateFlow<ExternalHandoffEvent?> = _pendingEvent.asStateFlow()

    fun publish(result: ExternalHandoffParseResult) {
        _pendingEvent.value = when (result) {
            is ExternalHandoffParseResult.Accepted -> ExternalHandoffEvent.Pending(
                eventId = result.envelope.interopRequestId,
                envelope = result.envelope,
            )

            is ExternalHandoffParseResult.Rejected -> ExternalHandoffEvent.Rejected(
                eventId = result.handoffId,
                sourceLabel = result.sourceLabel,
                trustState = result.trustState,
                trustReason = result.trustReason,
                reason = result.reason,
            )
        }
    }

    fun consume(eventId: String) {
        val current = _pendingEvent.value ?: return
        if (current.eventId == eventId) {
            _pendingEvent.value = null
        }
    }
}
