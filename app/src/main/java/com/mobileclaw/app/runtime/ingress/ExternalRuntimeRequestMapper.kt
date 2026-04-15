package com.mobileclaw.app.runtime.ingress

import com.mobileclaw.app.runtime.session.RuntimeCapabilityHint
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.session.RuntimeSourceMetadata
import com.mobileclaw.app.runtime.session.RuntimeSourceTrustState
import com.mobileclaw.app.runtime.session.RuntimeTranscriptEntry
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentSourceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalRuntimeRequestMapper @Inject constructor() {
    fun map(
        envelope: InteropRequestEnvelope,
        selectedModelId: String,
        workspaceId: String,
        transcriptContext: List<RuntimeTranscriptEntry>,
        requestedCapabilities: List<RuntimeCapabilityHint> = emptyList(),
    ): RuntimeRequest {
        val mappedCapabilities = when {
            requestedCapabilities.isNotEmpty() -> requestedCapabilities
            envelope.requestedCapabilityId != null -> listOf(
                RuntimeCapabilityHint(capabilityId = envelope.requestedCapabilityId),
            )
            else -> emptyList()
        }
        return RuntimeRequest(
            requestId = "req-${envelope.interopRequestId}",
            userInput = envelope.sharedText,
            selectedModelId = selectedModelId,
            transcriptContext = transcriptContext,
            originApp = envelope.callerIdentity.originApp,
            workspaceId = workspaceId,
            subjectKey = envelope.sharedSubject ?: envelope.callerIdentity.packageName,
            requestedCapabilities = mappedCapabilities,
            attachments = envelope.attachments.map { it.toRuntimeAttachment(RuntimeAttachmentSourceType.EXTERNAL_HANDOFF) },
            sourceMetadata = RuntimeSourceMetadata(
                handoffId = envelope.interopRequestId,
                entryType = envelope.entryType.name.lowercase(),
                sourceLabel = envelope.callerIdentity.sourceLabel,
                trustState = when (envelope.callerIdentity.trustState) {
                    ExternalTrustState.TRUSTED -> RuntimeSourceTrustState.TRUSTED
                    ExternalTrustState.UNVERIFIED -> RuntimeSourceTrustState.UNVERIFIED
                    ExternalTrustState.DENIED -> RuntimeSourceTrustState.DENIED
                },
                trustReason = envelope.callerIdentity.trustReason,
                packageName = envelope.callerIdentity.packageName,
                referrerUri = envelope.callerIdentity.referrerUri,
                contractVersion = envelope.callerIdentity.contractVersion,
                compatibilitySummary = envelope.compatibilitySignal.compatibilityReason,
                grantSummary = envelope.uriGrantSummary.summaryText,
                packageSignatureDigest = envelope.callerIdentity.signatureDigest,
                requestedScopeIds = envelope.requestedScopes,
            ),
        )
    }
}
