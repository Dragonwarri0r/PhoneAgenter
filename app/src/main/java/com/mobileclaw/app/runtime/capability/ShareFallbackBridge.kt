package com.mobileclaw.app.runtime.capability

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ShareFallbackBridge : CapabilityDiscoveryBridge

@Singleton
class RealShareFallbackBridge @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) : ShareFallbackBridge {
    override suspend fun discoverProviders(
        capabilityId: String,
        request: RuntimeRequest,
    ): List<ProviderDescriptor> {
        if (capabilityId != "external.share" || "[noprovider]" in request.userInput.lowercase() || "[noshare]" in request.userInput.lowercase()) {
            return emptyList()
        }

        val matches = context.packageManager.queryShareActivities(
            Intent(Intent.ACTION_SEND).apply { type = "text/plain" },
        )
        if (matches.isEmpty()) {
            return listOf(
                ProviderDescriptor(
                    providerId = "share.$capabilityId.unavailable",
                    capabilityId = capabilityId,
                    providerType = ProviderType.SHARE,
                    priority = 20,
                    requiredScopes = emptyList(),
                    availability = CapabilityAvailability(
                        providerId = "share.$capabilityId.unavailable",
                        state = CapabilityAvailabilityState.UNAVAILABLE,
                        reason = appStrings.get(R.string.bridge_share_unavailable),
                    ),
                    providerApp = "android.share.unavailable",
                    providerLabel = appStrings.get(R.string.bridge_provider_share),
                    routeMetadata = mapOf("bridge" to "real_share"),
                    executorProviderId = "android_intent_dispatch",
                ),
            )
        }

        return matches.take(3).mapIndexed { index, resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName.orEmpty()
            val label = resolveInfo.loadLabel(context.packageManager)?.toString()
                ?: packageName
            ProviderDescriptor(
                providerId = "share.$capabilityId.$packageName",
                capabilityId = capabilityId,
                providerType = ProviderType.SHARE,
                priority = 20 + index,
                requiredScopes = emptyList(),
                availability = CapabilityAvailability(
                    providerId = "share.$capabilityId.$packageName",
                    state = CapabilityAvailabilityState.AVAILABLE,
                    reason = appStrings.get(R.string.bridge_share_available),
                ),
                providerApp = packageName,
                providerLabel = label,
                routeMetadata = mapOf(
                    "bridge" to "real_share",
                    "resolvedPackage" to packageName,
                ),
                executorProviderId = "android_intent_dispatch",
            )
        }
    }
}

@Suppress("DEPRECATION")
private fun PackageManager.queryShareActivities(intent: Intent) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        queryIntentActivities(intent, 0)
    }
