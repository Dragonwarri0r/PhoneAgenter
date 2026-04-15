package com.mobileclaw.app.runtime.capability

data class InvocationResult(
    val invocationId: String,
    val sessionId: String,
    val capabilityId: String,
    val providerId: String,
    val providerType: ProviderType,
    val success: Boolean,
    val outputText: String? = null,
    val failureReason: String? = null,
    val routeExplanation: String,
)

data class CapabilityRouteResult(
    val registration: CapabilityRegistration?,
    val descriptor: ProviderDescriptor?,
    val callerIdentity: CallerIdentity,
    val routeExplanation: String,
    val failureReason: String? = null,
    val visibilitySnapshot: ToolVisibilitySnapshot? = null,
) {
    val isSuccessful: Boolean = descriptor != null && failureReason == null
}
