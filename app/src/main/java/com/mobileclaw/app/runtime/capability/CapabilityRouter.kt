package com.mobileclaw.app.runtime.capability

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.governance.GovernanceRepository
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityRouter @Inject constructor(
    private val capabilityRegistry: CapabilityRegistry,
    private val callerVerifier: CallerVerifier,
    private val governanceRepository: GovernanceRepository,
    private val appStrings: AppStrings,
) {
    suspend fun route(
        request: RuntimeRequest,
        capabilityId: String,
    ): CapabilityRouteResult {
        val restricted = capabilityId != "generate.reply"
        val callerIdentity = callerVerifier.verify(request, capabilityId)
        val displayLabel = callerIdentity.sourceLabel
            .takeIf { it.isNotBlank() }
            ?: callerIdentity.packageName
            ?: callerIdentity.originApp
        if (restricted && !callerIdentity.allowsRestrictedCapabilities) {
            return finalizeRoute(
                requestDisplayLabel = displayLabel,
                registration = null,
                descriptor = null,
                callerIdentity = callerIdentity,
                routeExplanation = callerIdentity.trustReason,
                failureReason = callerIdentity.trustReason,
                visibilitySnapshot = ToolVisibilitySnapshot(
                    toolId = capabilityId,
                    state = ToolVisibilityState.DENIED,
                    reason = callerIdentity.trustReason,
                    relevanceScore = 1.0,
                    allowedByGovernance = false,
                    availableBindingCount = 0,
                ),
            )
        }

        val registration = capabilityRegistry.resolve(capabilityId, request)
        if (registration == null) {
            return finalizeRoute(
                requestDisplayLabel = displayLabel,
                registration = null,
                descriptor = null,
                callerIdentity = callerIdentity,
                routeExplanation = appStrings.get(R.string.bridge_no_registration),
                failureReason = appStrings.get(R.string.bridge_no_registration),
                visibilitySnapshot = null,
            )
        }

        val descriptor = registration.providerDescriptors.firstOrNull { descriptor ->
            descriptor.availability.state == CapabilityAvailabilityState.AVAILABLE ||
                descriptor.availability.state == CapabilityAvailabilityState.DEGRADED
        }

        if (descriptor == null) {
            return finalizeRoute(
                requestDisplayLabel = displayLabel,
                registration = registration,
                descriptor = null,
                callerIdentity = callerIdentity,
                routeExplanation = appStrings.get(
                    R.string.bridge_no_provider_for_capability,
                    registration.displayName,
                ),
                failureReason = appStrings.get(
                    R.string.bridge_no_provider_for_capability,
                    registration.displayName,
                ),
                visibilitySnapshot = registration.visibility.copy(
                    state = ToolVisibilityState.DEGRADED,
                    reason = appStrings.get(
                        R.string.bridge_no_provider_for_capability,
                        registration.displayName,
                    ),
                    allowedByGovernance = callerIdentity.allowsRestrictedCapabilities,
                ),
            )
        }

        val explanation = if (descriptor.priority == 0) {
            appStrings.get(
                R.string.bridge_route_selected_primary,
                registration.displayName,
                appStrings.providerTypeLabel(descriptor.providerType),
            )
        } else {
            appStrings.get(
                R.string.bridge_route_selected_fallback,
                registration.displayName,
                appStrings.providerTypeLabel(descriptor.providerType),
                descriptor.availability.reason,
            )
        }

        return finalizeRoute(
            requestDisplayLabel = displayLabel,
            registration = registration,
            descriptor = descriptor,
            callerIdentity = callerIdentity,
            routeExplanation = explanation,
            failureReason = null,
            visibilitySnapshot = registration.visibility.copy(
                state = if (callerIdentity.allowsRestrictedCapabilities) {
                    registration.visibility.state
                } else {
                    ToolVisibilityState.DENIED
                },
                reason = explanation,
                allowedByGovernance = callerIdentity.allowsRestrictedCapabilities,
            ),
        )
    }

    private suspend fun finalizeRoute(
        requestDisplayLabel: String,
        registration: CapabilityRegistration?,
        descriptor: ProviderDescriptor?,
        callerIdentity: CallerIdentity,
        routeExplanation: String,
        failureReason: String?,
        visibilitySnapshot: ToolVisibilitySnapshot?,
    ): CapabilityRouteResult {
        governanceRepository.recordCallerObservation(
            callerIdentity = callerIdentity,
            displayLabel = requestDisplayLabel,
            decisionSummary = routeExplanation,
            toolId = registration?.toolDescriptor?.toolId,
            toolDisplayName = registration?.toolDescriptor?.displayName,
        )
        return CapabilityRouteResult(
            registration = registration,
            descriptor = descriptor,
            callerIdentity = callerIdentity,
            routeExplanation = routeExplanation,
            failureReason = failureReason,
            visibilitySnapshot = visibilitySnapshot,
        )
    }
}
