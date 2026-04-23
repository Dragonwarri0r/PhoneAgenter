package com.mobileclaw.app.runtime.capability

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appfunctions.AppFunctionManager
import androidx.appfunctions.AppFunctionSearchSpec
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.appfunctions.AppFunctionExposureCatalog
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

interface AppFunctionBridge : CapabilityDiscoveryBridge

@Singleton
class RealAppFunctionBridge @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) : AppFunctionBridge {
    override suspend fun discoverProviders(
        capabilityId: String,
        request: RuntimeRequest,
    ): List<ProviderDescriptor> {
        val probe = probeAvailability(capabilityId)
        val executorProviderId = when (capabilityId) {
            "generate.reply" -> "local_generation"
            "external.share" -> "android_intent_dispatch"
            "message.send", "calendar.write" -> "android_intent_dispatch"
            else -> "local_generation"
        }
        return listOf(
            ProviderDescriptor(
                providerId = "appfunctions.$capabilityId",
                capabilityId = capabilityId,
                providerType = ProviderType.APP_FUNCTIONS,
                priority = 0,
                requiredScopes = emptyList(),
                availability = CapabilityAvailability(
                    providerId = "appfunctions.$capabilityId",
                    state = probe.state,
                    reason = probe.reason,
                ),
                providerApp = context.packageName,
                providerLabel = appStrings.get(R.string.bridge_provider_appfunctions),
                routeMetadata = probe.routeMetadata,
                executorProviderId = executorProviderId,
            ),
        )
    }

    private suspend fun probeAvailability(capabilityId: String): ProbeResult {
        val functionHint = AppFunctionExposureCatalog.functionHintForCapability(capabilityId)
            ?: return ProbeResult(
                state = CapabilityAvailabilityState.UNAVAILABLE,
                reason = appStrings.get(R.string.bridge_appfunctions_unavailable),
                routeMetadata = mapOf("bridge" to "real_appfunctions", "status" to "unmapped_capability"),
            )
        if (Build.VERSION.SDK_INT < 36) {
            return ProbeResult(
                state = CapabilityAvailabilityState.UNAVAILABLE,
                reason = appStrings.get(R.string.bridge_appfunctions_platform_unsupported),
                routeMetadata = mapOf("bridge" to "real_appfunctions", "status" to "platform_unsupported"),
            )
        }
        if (!isServiceRegistered()) {
            return ProbeResult(
                state = CapabilityAvailabilityState.UNAVAILABLE,
                reason = appStrings.get(R.string.bridge_appfunctions_service_unregistered),
                routeMetadata = mapOf("bridge" to "real_appfunctions", "status" to "service_unregistered"),
            )
        }
        return runCatching {
            val manager = AppFunctionManager.getInstance(context)
                ?: return@runCatching ProbeResult(
                    state = CapabilityAvailabilityState.UNAVAILABLE,
                    reason = appStrings.get(R.string.bridge_appfunctions_discovery_failed),
                    routeMetadata = mapOf("bridge" to "real_appfunctions", "status" to "manager_unavailable"),
                )
            val packages = manager.observeAppFunctions(
                AppFunctionSearchSpec(setOf(context.packageName)),
            ).first()
            val appFunctions = packages.firstOrNull { it.packageName == context.packageName }
                ?.appFunctions
                .orEmpty()
            val matched = appFunctions.firstOrNull { metadata ->
                metadata.id.contains(functionHint, ignoreCase = true) ||
                    metadata.schema?.name?.contains(functionHint, ignoreCase = true) == true ||
                    metadata.description.contains(functionHint, ignoreCase = true)
            }
            if (matched == null) {
                ProbeResult(
                    state = CapabilityAvailabilityState.UNAVAILABLE,
                    reason = appStrings.get(R.string.bridge_appfunctions_metadata_missing),
                    routeMetadata = mapOf(
                        "bridge" to "real_appfunctions",
                        "status" to "metadata_missing",
                        "functionHint" to functionHint,
                    ),
                )
            } else {
                val enabled = manager.isAppFunctionEnabled(context.packageName, matched.id)
                ProbeResult(
                    state = if (enabled) {
                        CapabilityAvailabilityState.AVAILABLE
                    } else {
                        CapabilityAvailabilityState.RESTRICTED
                    },
                    reason = if (enabled) {
                        appStrings.get(R.string.bridge_appfunctions_real_available)
                    } else {
                        appStrings.get(R.string.bridge_appfunctions_disabled)
                    },
                    routeMetadata = mapOf(
                        "bridge" to "real_appfunctions",
                        "status" to if (enabled) "framework_available" else "framework_disabled",
                        "functionId" to matched.id,
                        "schemaName" to (matched.schema?.name ?: "unknown"),
                    ),
                )
            }
        }.getOrElse {
            ProbeResult(
                state = CapabilityAvailabilityState.UNAVAILABLE,
                reason = appStrings.get(R.string.bridge_appfunctions_discovery_failed),
                routeMetadata = mapOf("bridge" to "real_appfunctions", "status" to "discovery_failed"),
            )
        }
    }

    private fun isServiceRegistered(): Boolean {
        val intent = Intent("android.app.appfunctions.AppFunctionService").setPackage(context.packageName)
        val results = if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.queryIntentServices(
                intent,
                PackageManager.ResolveInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentServices(intent, 0)
        }
        return results.any {
            it.serviceInfo?.name == "androidx.appfunctions.service.PlatformAppFunctionService" ||
                it.serviceInfo?.name == "androidx.appfunctions.service.ExtensionAppFunctionService"
        }
    }
}

private data class ProbeResult(
    val state: CapabilityAvailabilityState,
    val reason: String,
    val routeMetadata: Map<String, String>,
)
