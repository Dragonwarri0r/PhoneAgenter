package com.mobileclaw.app.runtime.ingress

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.interop.contract.InteropIds
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallableInteropMapper @Inject constructor(
    private val appStrings: AppStrings,
) {
    val supportedSurfaces: List<CallableSurfaceDescriptor> = listOf(
        CallableSurfaceDescriptor(
            surfaceId = InteropIds.Surface.RUNTIME_CALLABLE_BASIC,
            displayName = "Mobile Claw Runtime",
            supportedFields = listOf("userInput", "requestedCapabilityId", "requestedScopes", "attachments"),
            supportedScopes = listOf(
                InteropIds.Scope.REPLY_GENERATE,
                InteropIds.Scope.CALENDAR_READ,
                InteropIds.Scope.CALENDAR_WRITE,
                InteropIds.Scope.MESSAGE_SEND,
                InteropIds.Scope.EXTERNAL_SHARE,
            ),
            supportsAttachments = true,
        ),
    )

    fun map(payload: CallableRequestPayload): InteropRequestEnvelope {
        val descriptor = supportedSurfaces.firstOrNull { it.surfaceId == payload.surfaceId }
        val isCompatible = descriptor != null &&
            payload.contractVersion == descriptor.interopVersion &&
            payload.unknownFieldCount == 0
        val compatibility = InteropCompatibilitySignal(
            interopVersion = payload.contractVersion,
            isCompatible = isCompatible,
            compatibilityReason = when {
                descriptor == null -> appStrings.get(R.string.interop_callable_surface_missing, payload.surfaceId)
                payload.unknownFieldCount > 0 -> appStrings.get(
                    R.string.interop_compatibility_unknown_fields,
                    payload.unknownFieldCount,
                )
                payload.contractVersion != descriptor.interopVersion -> appStrings.get(
                    R.string.interop_compatibility_version_mismatch,
                    payload.contractVersion,
                    descriptor.interopVersion,
                )
                else -> appStrings.get(R.string.interop_compatibility_supported)
            },
            unknownFieldCount = payload.unknownFieldCount,
        )
        val grantSummary = if (payload.attachments.isEmpty()) {
            UriGrantSummary.none(appStrings.get(R.string.interop_grant_none))
        } else {
            UriGrantSummary(
                grantCount = payload.attachments.size,
                grantedMimeFamilies = payload.attachments.mapNotNull { attachment ->
                    attachment.mimeType.substringBefore('/', missingDelimiterValue = attachment.mimeType)
                        .takeIf { it.isNotBlank() }
                }.distinct(),
                grantMode = UriGrantMode.READ_ONLY,
                expiresWithSession = true,
                summaryText = appStrings.interopGrantSummary(
                    grantCount = payload.attachments.size,
                    mimeFamilies = payload.attachments.map { it.mimeType },
                    grantMode = UriGrantMode.READ_ONLY,
                    expiresWithSession = true,
                ),
            )
        }
        return InteropRequestEnvelope(
            interopRequestId = payload.requestId,
            entryType = InteropEntryType.CALLABLE_REQUEST,
            callerIdentity = payload.callerIdentity,
            sharedText = payload.userInput,
            attachments = payload.attachments,
            requestedScopes = payload.requestedScopes,
            requestedCapabilityId = payload.requestedCapabilityId,
            uriGrantSummary = grantSummary,
            compatibilitySignal = compatibility,
        )
    }
}
