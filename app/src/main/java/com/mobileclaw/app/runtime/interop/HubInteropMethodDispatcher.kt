package com.mobileclaw.app.runtime.interop

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.android.bundle.InteropBundleCodec
import com.mobileclaw.interop.android.bundle.TaskBundles
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HubInteropMethodDispatcher @Inject constructor(
    private val discoveryService: HubDiscoveryService,
    private val invocationService: HubCapabilityInvocationService,
    private val taskService: HubInteropTaskService,
    private val authorizationService: HubInteropAuthorizationService,
) {
    fun dispatch(
        method: String?,
        extras: Bundle?,
    ): Bundle = runBlocking {
        runCatching {
            when (HubInteropMethod.fromWireName(method)) {
                HubInteropMethod.DISCOVER_SURFACE -> {
                    DiscoveryBundles.toResponseBundle(
                        discoveryService.discover(
                            request = DiscoveryBundles.fromBundle(extras),
                        ),
                    )
                }

                HubInteropMethod.INVOKE_CAPABILITY -> {
                    InvocationBundles.toResponseBundle(
                        invocationService.invoke(
                            request = InvocationBundles.fromBundle(requireExtras(extras)),
                        ),
                    )
                }

                HubInteropMethod.GET_TASK -> {
                    TaskBundles.toResponseBundle(
                        taskService.taskResponse(
                            request = TaskBundles.fromBundle(requireExtras(extras)),
                        ),
                    )
                }

                HubInteropMethod.GET_ARTIFACT -> {
                    ArtifactBundles.toResponseBundle(
                        taskService.artifactResponse(
                            request = ArtifactBundles.fromBundle(requireExtras(extras)),
                        ),
                    )
                }

                HubInteropMethod.REQUEST_AUTHORIZATION -> {
                    AuthorizationBundles.toResponseBundle(
                        authorizationService.requestAuthorization(
                            request = AuthorizationBundles.fromBundle(requireExtras(extras)),
                        ),
                    )
                }

                HubInteropMethod.GET_GRANT_STATUS -> {
                    AuthorizationBundles.toResponseBundle(
                        authorizationService.getGrantStatus(
                            request = AuthorizationBundles.fromBundle(requireExtras(extras)),
                        ),
                    )
                }

                HubInteropMethod.REVOKE_GRANT -> {
                    AuthorizationBundles.toResponseBundle(
                        authorizationService.revokeGrant(
                            request = AuthorizationBundles.fromBundle(requireExtras(extras)),
                        ),
                    )
                }

                null -> unsupportedBundle("unsupported_method")
            }
        }.getOrElse { throwable ->
            errorBundle(throwable.message ?: "interop_dispatch_failed")
        }
    }

    private fun requireExtras(extras: Bundle?): Bundle {
        return extras ?: error("interop_request_bundle_missing")
    }

    private fun unsupportedBundle(message: String): Bundle {
        return bundleOf(
            "status" to HubInteropStatus.BAD_REQUEST.wireName,
            "compatibility_signal" to InteropBundleCodec.compatibilitySignalToBundle(InteropCompatibilitySignal()),
            "message" to message,
        )
    }

    private fun errorBundle(message: String): Bundle {
        return bundleOf(
            "status" to HubInteropStatus.INTERNAL_ERROR.wireName,
            "compatibility_signal" to InteropBundleCodec.compatibilitySignalToBundle(
                InteropCompatibilitySignal(),
            ),
            "message" to message,
        )
    }
}
