package com.mobileclaw.app.runtime.capability

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.contribution.RuntimeContributionRegistry
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface MutationCapabilityBridge : CapabilityDiscoveryBridge

@Singleton
class LocalMutationCapabilityBridge @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val contributionRegistry: RuntimeContributionRegistry,
    private val appStrings: AppStrings,
) : MutationCapabilityBridge {
    override suspend fun discoverProviders(
        capabilityId: String,
        request: RuntimeRequest,
    ): List<ProviderDescriptor> {
        return when (capabilityId) {
            "calendar.write" -> listOf(
                buildMutationProvider(
                    capabilityId = capabilityId,
                    providerId = "mutation.calendar.write.local",
                    extensionId = "provider.calendar.write.local",
                    displayName = appStrings.get(R.string.tool_calendar_write_name),
                    permissionRequirements = listOf(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR,
                    ),
                ),
            )

            "calendar.delete" -> listOf(
                buildMutationProvider(
                    capabilityId = capabilityId,
                    providerId = "mutation.calendar.delete.local",
                    extensionId = "provider.calendar.delete.local",
                    displayName = appStrings.get(R.string.tool_calendar_delete_name),
                    permissionRequirements = listOf(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR,
                    ),
                ),
            )

            else -> emptyList()
        }
    }

    private fun buildMutationProvider(
        capabilityId: String,
        providerId: String,
        extensionId: String,
        displayName: String,
        permissionRequirements: List<String>,
    ): ProviderDescriptor {
        val contributionId = contributionRegistry.extensionContributionId(extensionId)
        val missingPermissions = permissionRequirements.filterNot(::isGranted)
        val availability = when {
            !contributionRegistry.isEnabled(contributionId) -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.DEGRADED,
                reason = appStrings.get(R.string.mutation_provider_disabled),
            )

            missingPermissions.isNotEmpty() -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.DEGRADED,
                reason = appStrings.get(
                    R.string.mutation_provider_permission_required,
                    displayName,
                ),
            )

            else -> CapabilityAvailability(
                providerId = providerId,
                state = CapabilityAvailabilityState.AVAILABLE,
                reason = appStrings.get(
                    R.string.mutation_provider_available,
                    displayName,
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
            providerLabel = appStrings.get(R.string.system_source_calendar),
            routeMetadata = mapOf(
                "bridge" to "local_mutation",
                "family" to "calendar",
            ),
            executorProviderId = "calendar_mutation_capability",
        )
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
