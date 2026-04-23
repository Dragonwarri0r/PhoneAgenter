package com.mobileclaw.app.runtime.capability

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.contribution.RuntimeContributionRegistry
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.systemsource.SystemSourceId
import com.mobileclaw.app.runtime.systemsource.SystemSourceRepository
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

interface CapabilityDiscoveryBridge {
    suspend fun discoverProviders(
        capabilityId: String,
        request: RuntimeRequest,
    ): List<ProviderDescriptor>
}

interface ReadCapabilityBridge : CapabilityDiscoveryBridge

@Singleton
class LocalReadCapabilityBridge @Inject constructor(
    private val sourceRepository: SystemSourceRepository,
    private val contributionRegistry: RuntimeContributionRegistry,
    private val appStrings: AppStrings,
) : ReadCapabilityBridge {
    override suspend fun discoverProviders(
        capabilityId: String,
        request: RuntimeRequest,
    ): List<ProviderDescriptor> {
        return when (capabilityId) {
            "calendar.read" -> listOf(calendarReadProvider())
            "contacts.read" -> listOf(contactsReadProvider())
            else -> emptyList()
        }
    }

    private suspend fun calendarReadProvider(): ProviderDescriptor {
        return buildReadProvider(
            capabilityId = "calendar.read",
            providerId = "read.calendar.local",
            extensionContributionId = contributionRegistry.systemSourceContributionId(SystemSourceId.CALENDAR),
            sourceId = SystemSourceId.CALENDAR,
            providerLabel = appStrings.get(R.string.system_source_calendar),
            unavailableReason = appStrings.get(R.string.read_provider_unimplemented),
            executorProviderId = "calendar_read_capability",
        )
    }

    private suspend fun contactsReadProvider(): ProviderDescriptor {
        return buildReadProvider(
            capabilityId = "contacts.read",
            providerId = "read.contacts.local",
            extensionContributionId = contributionRegistry.systemSourceContributionId(SystemSourceId.CONTACTS),
            sourceId = SystemSourceId.CONTACTS,
            providerLabel = appStrings.get(R.string.system_source_contacts),
            unavailableReason = appStrings.get(R.string.read_provider_unimplemented),
            executorProviderId = "contacts_read_capability",
        )
    }

    private suspend fun buildReadProvider(
        capabilityId: String,
        providerId: String,
        extensionContributionId: String,
        sourceId: SystemSourceId,
        providerLabel: String,
        unavailableReason: String,
        executorProviderId: String,
    ): ProviderDescriptor {
        val descriptor = sourceRepository.currentDescriptors().firstOrNull { it.sourceId == sourceId }
        val contributionEnabled = contributionRegistry.isEnabled(extensionContributionId)
        val availability = when {
            !contributionEnabled -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.DEGRADED,
                reason = appStrings.get(R.string.read_provider_disabled),
            )

            descriptor == null -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.UNAVAILABLE,
                reason = unavailableReason,
            )

            !descriptor.isGranted -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.DEGRADED,
                reason = appStrings.get(
                    R.string.read_provider_permission_required,
                    descriptor.displayName,
                ),
            )

            capabilityId == "contacts.read" -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.UNAVAILABLE,
                reason = unavailableReason,
            )

            else -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.AVAILABLE,
                reason = appStrings.get(
                    R.string.read_provider_available,
                    descriptor.displayName,
                ),
            )
        }

        return ProviderDescriptor(
            providerId = providerId,
            capabilityId = capabilityId,
            providerType = ProviderType.CONTENT_RESOLVER,
            priority = 0,
            requiredScopes = listOf(capabilityId),
            availability = availability,
            providerApp = "android.device",
            providerLabel = providerLabel,
            routeMetadata = mapOf(
                "bridge" to "local_read",
                "sourceId" to sourceId.name.lowercase(),
            ),
            executorProviderId = executorProviderId,
        )
    }
}
