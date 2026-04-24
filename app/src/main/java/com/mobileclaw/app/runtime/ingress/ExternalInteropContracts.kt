package com.mobileclaw.app.runtime.ingress

import com.mobileclaw.app.runtime.multimodal.PendingAttachment
import com.mobileclaw.interop.contract.CallerContractIdentity as SharedCallerContractIdentity
import com.mobileclaw.interop.contract.CallableSurfaceDescriptor as SharedCallableSurfaceDescriptor
import com.mobileclaw.interop.contract.ExternalTrustState as SharedExternalTrustState
import com.mobileclaw.interop.contract.InteropCompatibilitySignal as SharedInteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropEntryType as SharedInteropEntryType
import com.mobileclaw.interop.contract.InteropVersion
import com.mobileclaw.interop.contract.UriGrantMode as SharedUriGrantMode
import com.mobileclaw.interop.contract.UriGrantSummary as SharedUriGrantSummary

typealias ExternalTrustState = SharedExternalTrustState
typealias InteropEntryType = SharedInteropEntryType
typealias UriGrantMode = SharedUriGrantMode
typealias CallerContractIdentity = SharedCallerContractIdentity
typealias UriGrantSummary = SharedUriGrantSummary
typealias InteropCompatibilitySignal = SharedInteropCompatibilitySignal
typealias CallableSurfaceDescriptor = SharedCallableSurfaceDescriptor

data class InteropRequestEnvelope(
    val interopRequestId: String,
    val entryType: InteropEntryType,
    val callerIdentity: CallerContractIdentity,
    val sharedText: String,
    val sharedSubject: String? = null,
    val attachments: List<PendingAttachment> = emptyList(),
    val requestedScopes: List<String> = emptyList(),
    val requestedCapabilityId: String? = null,
    val uriGrantSummary: UriGrantSummary,
    val compatibilitySignal: InteropCompatibilitySignal,
    val receivedAtEpochMillis: Long = System.currentTimeMillis(),
    val rawSourceSummary: String = "",
)

data class CallableRequestPayload(
    val requestId: String,
    val surfaceId: String,
    val callerIdentity: CallerContractIdentity,
    val userInput: String,
    val attachments: List<PendingAttachment> = emptyList(),
    val requestedScopes: List<String> = emptyList(),
    val requestedCapabilityId: String? = null,
    val unknownFieldCount: Int = 0,
    val contractVersion: String = InteropVersion.CURRENT.value,
)
