package com.mobileclaw.interop.android.bundle

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobileclaw.interop.android.HubInteropAndroidContract
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.contract.InteropArtifactDescriptor
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropVersion

object ArtifactBundles {
    data class Request(
        val handle: InteropHandle,
        val requestedVersion: String = InteropVersion.CURRENT.value,
    )

    data class Response(
        val status: HubInteropStatus,
        val compatibilitySignal: InteropCompatibilitySignal,
        val artifactDescriptor: InteropArtifactDescriptor? = null,
        val message: String? = null,
    )

    fun toBundle(request: Request): Bundle = bundleOf(
        HubInteropAndroidContract.BundleKeys.HANDLE to InteropBundleCodec.handleToBundle(request.handle),
        HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION to request.requestedVersion,
    )

    fun fromBundle(bundle: Bundle): Request {
        return Request(
            handle = InteropBundleCodec.handleFromBundle(bundle.getBundle(HubInteropAndroidContract.BundleKeys.HANDLE))
                ?: error("artifact_handle_missing"),
            requestedVersion = bundle.getString(HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION)
                ?: InteropVersion.CURRENT.value,
        )
    }

    fun toResponseBundle(response: Response): Bundle = bundleOf(
        HubInteropAndroidContract.BundleKeys.STATUS to response.status.wireName,
        HubInteropAndroidContract.BundleKeys.COMPATIBILITY_SIGNAL to InteropBundleCodec.compatibilitySignalToBundle(
            response.compatibilitySignal,
        ),
        HubInteropAndroidContract.BundleKeys.ARTIFACT_DESCRIPTOR to response.artifactDescriptor?.let(
            InteropBundleCodec::artifactToBundle,
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
            artifactDescriptor = InteropBundleCodec.artifactFromBundle(
                bundle.getBundle(HubInteropAndroidContract.BundleKeys.ARTIFACT_DESCRIPTOR),
            ),
            message = bundle.getString(HubInteropAndroidContract.BundleKeys.MESSAGE),
        )
    }
}
