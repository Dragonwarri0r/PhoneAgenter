package com.mobileclaw.app.runtime.interop

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.appfunctions.AppFunctionExposureCatalog
import com.mobileclaw.app.runtime.capability.StandardToolCatalog
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.interop.android.HubInteropAndroidContract
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.contract.HubSurfaceDescriptor
import com.mobileclaw.interop.contract.HubSurfaceDescriptorFactory
import com.mobileclaw.interop.contract.InteropApprovalRequirement
import com.mobileclaw.interop.contract.InteropAuthorizationRequirement
import com.mobileclaw.interop.contract.InteropAvailabilityStatus
import com.mobileclaw.interop.contract.InteropBoundedness
import com.mobileclaw.interop.contract.InteropCapabilityDescriptor
import com.mobileclaw.interop.contract.InteropDataSensitivity
import com.mobileclaw.interop.contract.InteropIds
import com.mobileclaw.interop.contract.InteropSideEffectLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HubSurfaceDescriptorAssembler @Inject constructor(
    private val standardToolCatalog: StandardToolCatalog,
    private val appStrings: AppStrings,
) {
    fun assemble(): HubSurfaceDescriptor {
        val replyDescriptor = standardToolCatalog.descriptorForCapability(InteropIds.Capability.GENERATE_REPLY)
        val capability = InteropCapabilityDescriptor(
            capabilityId = replyDescriptor.legacyCapabilityId,
            displayName = replyDescriptor.displayName,
            summary = replyDescriptor.description,
            requiredScopes = replyDescriptor.requiredScopes,
            supportsAttachments = true,
            preferredMethods = listOf(
                HubInteropAndroidContract.Adapter.BASELINE_PROVIDER,
                HubInteropAndroidContract.Adapter.APP_FUNCTIONS,
                AppFunctionExposureCatalog.DRAFT_REPLY_METHOD,
            ),
            authorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
            inputSchemaVersion = "1.0",
            outputArtifactTypes = listOf("text/plain"),
            sideEffectLevel = InteropSideEffectLevel.NONE,
            dataSensitivity = InteropDataSensitivity.STANDARD,
            boundedness = InteropBoundedness.BOUNDED,
            approvalRequirement = InteropApprovalRequirement.HOST_POLICY,
            availability = InteropAvailabilityStatus.AVAILABLE,
        )
        return HubSurfaceDescriptorFactory.hubSurface(
            surfaceId = InteropIds.Surface.RUNTIME_CALLABLE_BASIC,
            displayName = appStrings.get(R.string.hub_interop_surface_display_name),
            summary = appStrings.get(R.string.hub_interop_surface_summary),
            supportedMethods = listOf(
                HubInteropMethod.DISCOVER_SURFACE.wireName,
                HubInteropMethod.INVOKE_CAPABILITY.wireName,
                HubInteropMethod.GET_TASK.wireName,
                HubInteropMethod.GET_ARTIFACT.wireName,
                HubInteropMethod.REQUEST_AUTHORIZATION.wireName,
                HubInteropMethod.GET_GRANT_STATUS.wireName,
                HubInteropMethod.REVOKE_GRANT.wireName,
            ),
            capabilities = listOf(capability),
            authorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
            supportsAttachments = true,
            tags = listOf("mobile_claw", "governed_host", "android_v1"),
        )
    }
}
