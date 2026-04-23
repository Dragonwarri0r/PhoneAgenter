package com.mobileclaw.app.runtime.capability

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityRegistry @Inject constructor(
    private val readCapabilityBridge: ReadCapabilityBridge,
    private val mutationCapabilityBridge: MutationCapabilityBridge,
    private val appFunctionBridge: AppFunctionBridge,
    private val intentFallbackBridge: IntentFallbackBridge,
    private val shareFallbackBridge: ShareFallbackBridge,
    private val standardToolCatalog: StandardToolCatalog,
    private val appStrings: AppStrings,
) {
    suspend fun resolve(
        capabilityId: String,
        request: RuntimeRequest,
    ): CapabilityRegistration? {
        val toolDescriptor = standardToolCatalog.descriptorForCapability(capabilityId)
        val providers = discoverProviders(capabilityId, request).sortedBy { it.priority }
        val primaryProvider = providers.firstOrNull()

        val aggregate = when {
            providers.isEmpty() -> CapabilityAvailabilityState.UNAVAILABLE
            providers.any { it.availability.state == CapabilityAvailabilityState.AVAILABLE } -> {
                CapabilityAvailabilityState.AVAILABLE
            }
            providers.any { it.availability.state == CapabilityAvailabilityState.DEGRADED } -> {
                CapabilityAvailabilityState.DEGRADED
            }
            providers.any { it.availability.state == CapabilityAvailabilityState.RESTRICTED } -> {
                CapabilityAvailabilityState.RESTRICTED
            }
            else -> CapabilityAvailabilityState.UNAVAILABLE
        }
        return CapabilityRegistration(
            capabilityId = capabilityId,
            toolDescriptor = toolDescriptor,
            visibility = ToolVisibilitySnapshot(
                toolId = toolDescriptor.toolId,
                state = when (aggregate) {
                    CapabilityAvailabilityState.AVAILABLE -> ToolVisibilityState.VISIBLE
                    CapabilityAvailabilityState.DEGRADED,
                    CapabilityAvailabilityState.RESTRICTED,
                    CapabilityAvailabilityState.UNAVAILABLE,
                    -> ToolVisibilityState.DEGRADED
                },
                reason = providers.firstOrNull()?.availability?.reason
                    ?: appStrings.get(R.string.tool_visibility_unavailable),
                relevanceScore = 1.0,
                allowedByGovernance = true,
                availableBindingCount = providers.count {
                    it.availability.state == CapabilityAvailabilityState.AVAILABLE ||
                        it.availability.state == CapabilityAvailabilityState.DEGRADED
                },
                primaryProviderId = primaryProvider?.providerId,
            ),
            displayName = toolDescriptor.displayName,
            requiredScopes = toolDescriptor.requiredScopes,
            riskLevelHint = toolDescriptor.riskLevelHint,
            confirmationPolicy = toolDescriptor.confirmationPolicy,
            invocationKind = toolDescriptor.invocationKind,
            freeformSelectionPolicy = toolDescriptor.freeformSelectionPolicy,
            supportedExtensionTypes = capabilityExtensionTypes(capabilityId),
            providerDescriptors = providers,
            availability = aggregate,
        )
    }

    private suspend fun discoverProviders(
        capabilityId: String,
        request: RuntimeRequest,
    ): List<ProviderDescriptor> {
        return buildList {
            when (capabilityId) {
                "generate.reply" -> add(localReplyProvider())
                else -> {
                    addAll(readCapabilityBridge.discoverProviders(capabilityId, request))
                    addAll(mutationCapabilityBridge.discoverProviders(capabilityId, request))
                    addAll(appFunctionBridge.discoverProviders(capabilityId, request))
                    addAll(intentFallbackBridge.discoverProviders(capabilityId, request))
                    addAll(shareFallbackBridge.discoverProviders(capabilityId, request))
                }
            }
        }
    }

    private fun localReplyProvider(): ProviderDescriptor {
        return ProviderDescriptor(
            providerId = "local.generate.reply",
            capabilityId = "generate.reply",
            providerType = ProviderType.LOCAL,
            priority = 0,
            requiredScopes = emptyList(),
            availability = CapabilityAvailability(
                providerId = "local.generate.reply",
                state = CapabilityAvailabilityState.AVAILABLE,
                reason = appStrings.get(R.string.bridge_local_generation_available),
            ),
            providerApp = "local.runtime",
            providerLabel = appStrings.get(R.string.bridge_provider_local),
            routeMetadata = mapOf(
                "bridge" to "local_runtime",
                "status" to "available",
            ),
            executorProviderId = "local_generation",
        )
    }

    private fun capabilityExtensionTypes(capabilityId: String): List<String> = when (capabilityId) {
        "generate.reply" -> listOf("provider")
        "calendar.read" -> listOf("provider", "context")
        "contacts.read" -> listOf("provider", "context")
        "message.send", "calendar.write", "calendar.delete", "external.share", "alarm.set", "alarm.show", "alarm.dismiss" -> {
            listOf("provider", "export")
        }
        "ui.act", "sensitive.write" -> listOf("provider")
        else -> emptyList()
    }
}
