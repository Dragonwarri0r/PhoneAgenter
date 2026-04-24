package com.mobileclaw.interop.android.bundle

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobileclaw.interop.android.HubInteropAndroidContract
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropGrantDescriptor
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropVersion

object AuthorizationBundles {
    data class Request(
        val requestId: String,
        val callerIdentity: CallerContractIdentity,
        val capabilityId: String,
        val requestedScopes: List<String> = emptyList(),
        val handle: InteropHandle? = null,
        val requestedVersion: String = InteropVersion.CURRENT.value,
    )

    data class Response(
        val status: HubInteropStatus,
        val compatibilitySignal: InteropCompatibilitySignal,
        val grantDescriptor: InteropGrantDescriptor? = null,
        val message: String? = null,
    )

    fun toBundle(request: Request): Bundle = bundleOf(
        HubInteropAndroidContract.BundleKeys.REQUEST_ID to request.requestId,
        HubInteropAndroidContract.BundleKeys.CALLER_IDENTITY to InteropBundleCodec.callerIdentityToBundle(
            request.callerIdentity,
        ),
        HubInteropAndroidContract.BundleKeys.CAPABILITY_ID to request.capabilityId,
        HubInteropAndroidContract.BundleKeys.REQUESTED_SCOPES to ArrayList(request.requestedScopes),
        HubInteropAndroidContract.BundleKeys.HANDLE to request.handle?.let(InteropBundleCodec::handleToBundle),
        HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION to request.requestedVersion,
    )

    fun fromBundle(bundle: Bundle): Request {
        return Request(
            requestId = bundle.getString(HubInteropAndroidContract.BundleKeys.REQUEST_ID).orEmpty(),
            callerIdentity = InteropBundleCodec.callerIdentityFromBundle(
                bundle.getBundle(HubInteropAndroidContract.BundleKeys.CALLER_IDENTITY),
            ) ?: error("caller_identity_missing"),
            capabilityId = bundle.getString(HubInteropAndroidContract.BundleKeys.CAPABILITY_ID).orEmpty(),
            requestedScopes = bundle.getStringArrayList(HubInteropAndroidContract.BundleKeys.REQUESTED_SCOPES).orEmpty(),
            handle = InteropBundleCodec.handleFromBundle(bundle.getBundle(HubInteropAndroidContract.BundleKeys.HANDLE)),
            requestedVersion = bundle.getString(HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION)
                ?: InteropVersion.CURRENT.value,
        )
    }

    fun toResponseBundle(response: Response): Bundle = bundleOf(
        HubInteropAndroidContract.BundleKeys.STATUS to response.status.wireName,
        HubInteropAndroidContract.BundleKeys.COMPATIBILITY_SIGNAL to InteropBundleCodec.compatibilitySignalToBundle(
            response.compatibilitySignal,
        ),
        HubInteropAndroidContract.BundleKeys.GRANT_DESCRIPTOR to response.grantDescriptor?.let(
            InteropBundleCodec::grantToBundle,
        ),
        HubInteropAndroidContract.BundleKeys.MESSAGE to response.message,
    )

    fun fromResponseBundle(bundle: Bundle): Response {
        return Response(
            status = HubInteropStatus.fromWireName(bundle.getString(HubInteropAndroidContract.BundleKeys.STATUS))
                ?: HubInteropStatus.INTERNAL_ERROR,
            compatibilitySignal = InteropBundleCodec.compatibilitySignalFromBundle(
                bundle.getBundle(HubInteropAndroidContract.BundleKeys.COMPATIBILITY_SIGNAL),
            ) ?: InteropCompatibilitySignal(),
            grantDescriptor = InteropBundleCodec.grantFromBundle(
                bundle.getBundle(HubInteropAndroidContract.BundleKeys.GRANT_DESCRIPTOR),
            ),
            message = bundle.getString(HubInteropAndroidContract.BundleKeys.MESSAGE),
        )
    }
}
