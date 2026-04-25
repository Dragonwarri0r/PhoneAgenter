package com.mobileclaw.interop.android.bundle

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobileclaw.interop.contract.ArtifactAccessMode
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.CompatibilityReasonCode
import com.mobileclaw.interop.contract.CompatibilityState
import com.mobileclaw.interop.contract.ExternalTrustState
import com.mobileclaw.interop.contract.HubSurfaceDescriptor
import com.mobileclaw.interop.contract.InteropArtifactDescriptor
import com.mobileclaw.interop.contract.InteropArtifactLifecycleState
import com.mobileclaw.interop.contract.InteropAuthorizationRequirement
import com.mobileclaw.interop.contract.InteropAvailabilityStatus
import com.mobileclaw.interop.contract.InteropBoundedness
import com.mobileclaw.interop.contract.InteropCapabilityDescriptor
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropDataSensitivity
import com.mobileclaw.interop.contract.InteropGrantDescriptor
import com.mobileclaw.interop.contract.InteropGrantDirection
import com.mobileclaw.interop.contract.InteropGrantLifetime
import com.mobileclaw.interop.contract.InteropGrantState
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropHandleFamily
import com.mobileclaw.interop.contract.InteropApprovalRequirement
import com.mobileclaw.interop.contract.InteropSideEffectLevel
import com.mobileclaw.interop.contract.InteropTaskDescriptor
import com.mobileclaw.interop.contract.InteropTaskLifecycleState
import com.mobileclaw.interop.contract.InteropTaskStatus
import com.mobileclaw.interop.contract.InteropVersion
import com.mobileclaw.interop.contract.UriGrantMode
import com.mobileclaw.interop.contract.UriGrantSummary

object InteropBundleCodec {
    fun callerIdentityToBundle(identity: CallerContractIdentity): Bundle = bundleOf(
        "origin_app" to identity.originApp,
        "package_name" to identity.packageName,
        "source_label" to identity.sourceLabel,
        "trust_state" to identity.trustState.name,
        "trust_reason" to identity.trustReason,
        "referrer_uri" to identity.referrerUri,
        "signature_digest" to identity.signatureDigest,
        "contract_version" to identity.contractVersion,
    )

    fun callerIdentityFromBundle(bundle: Bundle?): CallerContractIdentity? {
        bundle ?: return null
        return CallerContractIdentity(
            originApp = bundle.getString("origin_app").orEmpty(),
            packageName = bundle.getString("package_name"),
            sourceLabel = bundle.getString("source_label").orEmpty(),
            trustState = enumValueOfOrNull<ExternalTrustState>(bundle.getString("trust_state"))
                ?: ExternalTrustState.UNVERIFIED,
            trustReason = bundle.getString("trust_reason").orEmpty(),
            referrerUri = bundle.getString("referrer_uri"),
            signatureDigest = bundle.getString("signature_digest"),
            contractVersion = bundle.getString("contract_version") ?: InteropVersion.CURRENT.value,
        )
    }

    fun compatibilitySignalToBundle(signal: InteropCompatibilitySignal): Bundle = bundleOf(
        "interop_version" to signal.interopVersion,
        "is_compatible" to signal.isCompatible,
        "compatibility_reason" to signal.compatibilityReason,
        "unknown_field_count" to signal.unknownFieldCount,
        "required_unknown_field_count" to signal.requiredUnknownFieldCount,
        "optional_unknown_field_count" to signal.optionalUnknownFieldCount,
        "extension_field_count" to signal.extensionFieldCount,
        "compatibility_state" to signal.compatibilityState.name,
        "supported_version" to signal.supportedVersion,
        "reason_code" to signal.reasonCode.name,
    )

    fun compatibilitySignalFromBundle(bundle: Bundle?): InteropCompatibilitySignal? {
        bundle ?: return null
        return InteropCompatibilitySignal(
            interopVersion = bundle.getString("interop_version").orEmpty(),
            isCompatible = bundle.getBoolean("is_compatible"),
            compatibilityReason = bundle.getString("compatibility_reason").orEmpty(),
            unknownFieldCount = bundle.getInt("unknown_field_count"),
            requiredUnknownFieldCount = bundle.getInt("required_unknown_field_count").let { count ->
                if (bundle.containsKey("required_unknown_field_count")) count else bundle.getInt("unknown_field_count")
            },
            optionalUnknownFieldCount = bundle.getInt("optional_unknown_field_count"),
            extensionFieldCount = bundle.getInt("extension_field_count"),
            compatibilityState = enumValueOfOrNull<CompatibilityState>(bundle.getString("compatibility_state"))
                ?: CompatibilityState.INCOMPATIBLE,
            supportedVersion = bundle.getString("supported_version") ?: InteropVersion.CURRENT.value,
            reasonCode = enumValueOfOrNull<CompatibilityReasonCode>(bundle.getString("reason_code"))
                ?: CompatibilityReasonCode.SUPPORTED,
        )
    }

    fun uriGrantSummaryToBundle(summary: UriGrantSummary): Bundle = bundleOf(
        "grant_count" to summary.grantCount,
        "granted_mime_families" to ArrayList(summary.grantedMimeFamilies),
        "grant_mode" to summary.grantMode.name,
        "expires_with_session" to summary.expiresWithSession,
        "summary_text" to summary.summaryText,
    )

    fun uriGrantSummaryFromBundle(bundle: Bundle?): UriGrantSummary? {
        bundle ?: return null
        return UriGrantSummary(
            grantCount = bundle.getInt("grant_count"),
            grantedMimeFamilies = bundle.getStringArrayList("granted_mime_families").orEmpty(),
            grantMode = enumValueOfOrNull<UriGrantMode>(bundle.getString("grant_mode")) ?: UriGrantMode.UNKNOWN,
            expiresWithSession = bundle.getBoolean("expires_with_session"),
            summaryText = bundle.getString("summary_text").orEmpty(),
        )
    }

    fun capabilityToBundle(descriptor: InteropCapabilityDescriptor): Bundle = bundleOf(
        "capability_id" to descriptor.capabilityId,
        "display_name" to descriptor.displayName,
        "summary" to descriptor.summary,
        "required_scopes" to ArrayList(descriptor.requiredScopes),
        "supports_attachments" to descriptor.supportsAttachments,
        "preferred_methods" to ArrayList(descriptor.preferredMethods),
        "authorization_requirement" to descriptor.authorizationRequirement.name,
        "compatibility_signal" to descriptor.compatibilitySignal?.let(::compatibilitySignalToBundle),
        "input_schema_version" to descriptor.inputSchemaVersion,
        "output_artifact_types" to ArrayList(descriptor.outputArtifactTypes),
        "side_effect_level" to descriptor.sideEffectLevel.name,
        "data_sensitivity" to descriptor.dataSensitivity.name,
        "boundedness" to descriptor.boundedness.name,
        "approval_requirement" to descriptor.approvalRequirement.name,
        "availability" to descriptor.availability.name,
        "availability_message" to descriptor.availabilityMessage,
    )

    fun capabilityFromBundle(bundle: Bundle): InteropCapabilityDescriptor {
        return InteropCapabilityDescriptor(
            capabilityId = bundle.getString("capability_id").orEmpty(),
            displayName = bundle.getString("display_name").orEmpty(),
            summary = bundle.getString("summary").orEmpty(),
            requiredScopes = bundle.getStringArrayList("required_scopes").orEmpty(),
            supportsAttachments = bundle.getBoolean("supports_attachments"),
            preferredMethods = bundle.getStringArrayList("preferred_methods").orEmpty(),
            authorizationRequirement = enumValueOfOrNull<InteropAuthorizationRequirement>(
                bundle.getString("authorization_requirement"),
            ) ?: InteropAuthorizationRequirement.USER_CONSENT,
            compatibilitySignal = compatibilitySignalFromBundle(bundle.getBundle("compatibility_signal")),
            inputSchemaVersion = bundle.getString("input_schema_version") ?: "1.0",
            outputArtifactTypes = bundle.getStringArrayList("output_artifact_types").orEmpty(),
            sideEffectLevel = enumValueOfOrNull<InteropSideEffectLevel>(bundle.getString("side_effect_level"))
                ?: InteropSideEffectLevel.NONE,
            dataSensitivity = enumValueOfOrNull<InteropDataSensitivity>(bundle.getString("data_sensitivity"))
                ?: InteropDataSensitivity.STANDARD,
            boundedness = enumValueOfOrNull<InteropBoundedness>(bundle.getString("boundedness"))
                ?: InteropBoundedness.HOST_DEFINED,
            approvalRequirement = enumValueOfOrNull<InteropApprovalRequirement>(
                bundle.getString("approval_requirement"),
            ) ?: InteropApprovalRequirement.HOST_POLICY,
            availability = enumValueOfOrNull<InteropAvailabilityStatus>(bundle.getString("availability"))
                ?: InteropAvailabilityStatus.AVAILABLE,
            availabilityMessage = bundle.getString("availability_message").orEmpty(),
        )
    }

    fun surfaceToBundle(descriptor: HubSurfaceDescriptor): Bundle = bundleOf(
        "surface_id" to descriptor.surfaceId,
        "display_name" to descriptor.displayName,
        "summary" to descriptor.summary,
        "contract_version" to descriptor.contractVersion,
        "supported_methods" to ArrayList(descriptor.supportedMethods),
        "capabilities" to descriptor.capabilities.toBundleArrayList(::capabilityToBundle),
        "authorization_requirement" to descriptor.authorizationRequirement.name,
        "supports_attachments" to descriptor.supportsAttachments,
        "tags" to ArrayList(descriptor.tags),
    )

    fun surfaceFromBundle(bundle: Bundle): HubSurfaceDescriptor {
        return HubSurfaceDescriptor(
            surfaceId = bundle.getString("surface_id").orEmpty(),
            displayName = bundle.getString("display_name").orEmpty(),
            summary = bundle.getString("summary").orEmpty(),
            contractVersion = bundle.getString("contract_version") ?: InteropVersion.CURRENT.value,
            supportedMethods = bundle.getStringArrayList("supported_methods").orEmpty(),
            capabilities = bundle.getBundleArrayList("capabilities").map(::capabilityFromBundle),
            authorizationRequirement = enumValueOfOrNull<InteropAuthorizationRequirement>(
                bundle.getString("authorization_requirement"),
            ) ?: InteropAuthorizationRequirement.USER_CONSENT,
            supportsAttachments = bundle.getBoolean("supports_attachments"),
            tags = bundle.getStringArrayList("tags").orEmpty(),
        )
    }

    fun handleToBundle(handle: InteropHandle): Bundle = bundleOf(
        "family" to handle.family.name,
        "value" to handle.value,
        "opaque_value" to handle.opaqueValue,
    )

    fun handleFromBundle(bundle: Bundle?): InteropHandle? {
        bundle ?: return null
        return InteropHandle(
            family = enumValueOfOrNull<InteropHandleFamily>(bundle.getString("family")) ?: return null,
            value = bundle.getString("value").orEmpty(),
        )
    }

    fun grantToBundle(descriptor: InteropGrantDescriptor): Bundle = bundleOf(
        "handle" to handleToBundle(descriptor.handle),
        "direction" to descriptor.direction.name,
        "lifetime" to descriptor.lifetime.name,
        "scopes" to ArrayList(descriptor.scopes),
        "authorization_requirement" to descriptor.authorizationRequirement.name,
        "is_active" to descriptor.isActive,
        "expires_at_epoch_millis" to descriptor.expiresAtEpochMillis,
        "state" to descriptor.state.name,
        "requested_at_epoch_millis" to descriptor.requestedAtEpochMillis,
        "updated_at_epoch_millis" to descriptor.updatedAtEpochMillis,
    )

    fun grantFromBundle(bundle: Bundle?): InteropGrantDescriptor? {
        bundle ?: return null
        return InteropGrantDescriptor(
            handle = handleFromBundle(bundle.getBundle("handle")) ?: return null,
            direction = enumValueOfOrNull<InteropGrantDirection>(bundle.getString("direction"))
                ?: InteropGrantDirection.INBOUND,
            lifetime = enumValueOfOrNull<InteropGrantLifetime>(bundle.getString("lifetime"))
                ?: InteropGrantLifetime.ONCE,
            scopes = bundle.getStringArrayList("scopes").orEmpty(),
            authorizationRequirement = enumValueOfOrNull<InteropAuthorizationRequirement>(
                bundle.getString("authorization_requirement"),
            ) ?: InteropAuthorizationRequirement.USER_CONSENT,
            isActive = bundle.getBoolean("is_active"),
            expiresAtEpochMillis = bundle.getNullableLong("expires_at_epoch_millis"),
            state = enumValueOfOrNull<InteropGrantState>(bundle.getString("state"))
                ?: if (bundle.getBoolean("is_active")) InteropGrantState.ACTIVE else InteropGrantState.PENDING,
            requestedAtEpochMillis = bundle.getNullableLong("requested_at_epoch_millis"),
            updatedAtEpochMillis = bundle.getNullableLong("updated_at_epoch_millis") ?: 0L,
        )
    }

    fun taskToBundle(descriptor: InteropTaskDescriptor): Bundle = bundleOf(
        "handle" to handleToBundle(descriptor.handle),
        "display_name" to descriptor.displayName,
        "status" to descriptor.status.name,
        "summary" to descriptor.summary,
        "artifact_handles" to descriptor.artifactHandles.toBundleArrayList(::handleToBundle),
        "updated_at_epoch_millis" to descriptor.updatedAtEpochMillis,
        "lifecycle_state" to descriptor.lifecycleState.name,
        "availability" to descriptor.availability.name,
        "created_at_epoch_millis" to descriptor.createdAtEpochMillis,
        "expires_at_epoch_millis" to descriptor.expiresAtEpochMillis,
        "deleted_at_epoch_millis" to descriptor.deletedAtEpochMillis,
    )

    fun taskFromBundle(bundle: Bundle?): InteropTaskDescriptor? {
        bundle ?: return null
        return InteropTaskDescriptor(
            handle = handleFromBundle(bundle.getBundle("handle")) ?: return null,
            displayName = bundle.getString("display_name").orEmpty(),
            status = enumValueOfOrNull<InteropTaskStatus>(bundle.getString("status")) ?: InteropTaskStatus.FAILED,
            summary = bundle.getString("summary").orEmpty(),
            artifactHandles = bundle.getBundleArrayList("artifact_handles").mapNotNull(::handleFromBundle),
            updatedAtEpochMillis = bundle.getLong("updated_at_epoch_millis"),
            lifecycleState = enumValueOfOrNull<InteropTaskLifecycleState>(bundle.getString("lifecycle_state"))
                ?: lifecycleStateForTaskStatus(
                    enumValueOfOrNull<InteropTaskStatus>(bundle.getString("status")) ?: InteropTaskStatus.FAILED,
                ),
            availability = enumValueOfOrNull<InteropAvailabilityStatus>(bundle.getString("availability"))
                ?: InteropAvailabilityStatus.AVAILABLE,
            createdAtEpochMillis = bundle.getNullableLong("created_at_epoch_millis"),
            expiresAtEpochMillis = bundle.getNullableLong("expires_at_epoch_millis"),
            deletedAtEpochMillis = bundle.getNullableLong("deleted_at_epoch_millis"),
        )
    }

    fun artifactToBundle(descriptor: InteropArtifactDescriptor): Bundle = bundleOf(
        "handle" to handleToBundle(descriptor.handle),
        "display_name" to descriptor.displayName,
        "mime_type" to descriptor.mimeType,
        "access_mode" to descriptor.accessMode.name,
        "content_uri" to descriptor.contentUri,
        "summary" to descriptor.summary,
        "artifact_type" to descriptor.artifactType,
        "lifecycle_state" to descriptor.lifecycleState.name,
        "availability" to descriptor.availability.name,
        "created_at_epoch_millis" to descriptor.createdAtEpochMillis,
        "expires_at_epoch_millis" to descriptor.expiresAtEpochMillis,
        "deleted_at_epoch_millis" to descriptor.deletedAtEpochMillis,
    )

    fun artifactFromBundle(bundle: Bundle?): InteropArtifactDescriptor? {
        bundle ?: return null
        return InteropArtifactDescriptor(
            handle = handleFromBundle(bundle.getBundle("handle")) ?: return null,
            displayName = bundle.getString("display_name").orEmpty(),
            mimeType = bundle.getString("mime_type").orEmpty(),
            accessMode = enumValueOfOrNull<ArtifactAccessMode>(bundle.getString("access_mode"))
                ?: ArtifactAccessMode.READ_ONLY,
            contentUri = bundle.getString("content_uri"),
            summary = bundle.getString("summary").orEmpty(),
            artifactType = bundle.getString("artifact_type")
                ?: bundle.getString("mime_type").orEmpty(),
            lifecycleState = enumValueOfOrNull<InteropArtifactLifecycleState>(bundle.getString("lifecycle_state"))
                ?: InteropArtifactLifecycleState.AVAILABLE,
            availability = enumValueOfOrNull<InteropAvailabilityStatus>(bundle.getString("availability"))
                ?: InteropAvailabilityStatus.AVAILABLE,
            createdAtEpochMillis = bundle.getNullableLong("created_at_epoch_millis"),
            expiresAtEpochMillis = bundle.getNullableLong("expires_at_epoch_millis"),
            deletedAtEpochMillis = bundle.getNullableLong("deleted_at_epoch_millis"),
        )
    }

    private fun lifecycleStateForTaskStatus(status: InteropTaskStatus): InteropTaskLifecycleState {
        return when (status) {
            InteropTaskStatus.PENDING -> InteropTaskLifecycleState.QUEUED
            InteropTaskStatus.RUNNING -> InteropTaskLifecycleState.ACTIVE
            InteropTaskStatus.INPUT_REQUIRED -> InteropTaskLifecycleState.INPUT_REQUIRED
            InteropTaskStatus.COMPLETED -> InteropTaskLifecycleState.COMPLETED
            InteropTaskStatus.FAILED -> InteropTaskLifecycleState.FAILED
            InteropTaskStatus.CANCELLED -> InteropTaskLifecycleState.CANCELLED
        }
    }

    private fun <T> List<T>.toBundleArrayList(transform: (T) -> Bundle): ArrayList<Bundle> {
        return ArrayList(map(transform))
    }

    @Suppress("DEPRECATION")
    private fun Bundle.getNullableLong(key: String): Long? {
        if (!containsKey(key) || get(key) == null) return null
        return getLong(key)
    }

    @Suppress("DEPRECATION")
    private fun Bundle.getBundleArrayList(key: String): ArrayList<Bundle> {
        return getParcelableArrayList(key) ?: arrayListOf()
    }

    private inline fun <reified T : Enum<T>> enumValueOfOrNull(raw: String?): T? {
        return raw?.let {
            enumValues<T>().firstOrNull { value -> value.name == raw }
        }
    }
}
