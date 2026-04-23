package com.mobileclaw.app.runtime.capability

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface IntentFallbackBridge : CapabilityDiscoveryBridge

@Singleton
class RealIntentFallbackBridge @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) : IntentFallbackBridge {
    override suspend fun discoverProviders(
        capabilityId: String,
        request: RuntimeRequest,
    ): List<ProviderDescriptor> {
        if ("[noprovider]" in request.userInput.lowercase()) return emptyList()

        val probeIntent = probeIntent(capabilityId) ?: return emptyList()
        val matches = context.packageManager.queryIntentActivitiesForFallback(probeIntent)
        if (matches.isEmpty()) {
            return listOf(
                ProviderDescriptor(
                    providerId = "intent.$capabilityId.unavailable",
                    capabilityId = capabilityId,
                    providerType = ProviderType.INTENT,
                    priority = 10,
                    requiredScopes = emptyList(),
                    availability = CapabilityAvailability(
                        providerId = "intent.$capabilityId.unavailable",
                        state = CapabilityAvailabilityState.UNAVAILABLE,
                        reason = appStrings.get(R.string.bridge_intent_unavailable),
                    ),
                    providerApp = "android.intent.unavailable",
                    providerLabel = appStrings.get(R.string.bridge_provider_intent),
                    routeMetadata = mapOf("bridge" to "real_intent"),
                    executorProviderId = "android_intent_dispatch",
                ),
            )
        }

        return matches.take(3).mapIndexed { index, resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName.orEmpty()
            val label = resolveInfo.loadLabel(context.packageManager)?.toString()
                ?: packageName
            ProviderDescriptor(
                providerId = "intent.$capabilityId.$packageName",
                capabilityId = capabilityId,
                providerType = ProviderType.INTENT,
                priority = 10 + index,
                requiredScopes = emptyList(),
                availability = CapabilityAvailability(
                    providerId = "intent.$capabilityId.$packageName",
                    state = CapabilityAvailabilityState.AVAILABLE,
                    reason = appStrings.get(R.string.bridge_intent_available),
                ),
                providerApp = packageName,
                providerLabel = label,
                routeMetadata = mapOf(
                    "bridge" to "real_intent",
                    "resolvedPackage" to packageName,
                ),
                executorProviderId = "android_intent_dispatch",
            )
        }
    }

    private fun probeIntent(capabilityId: String): Intent? {
        return when (capabilityId) {
            "message.send" -> Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
            "calendar.write" -> Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            "alarm.set" -> Intent(AlarmClock.ACTION_SET_ALARM)
            "alarm.show" -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            "alarm.dismiss" -> Intent(AlarmClock.ACTION_DISMISS_ALARM)
            else -> null
        }
    }
}

@Suppress("DEPRECATION")
private fun PackageManager.queryIntentActivitiesForFallback(intent: Intent) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        queryIntentActivities(intent, 0)
    }
