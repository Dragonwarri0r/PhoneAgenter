package com.mobileclaw.interop.android.bundle

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobileclaw.interop.android.HubInteropAndroidContract
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.contract.HubSurfaceDescriptor
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropVersion

object DiscoveryBundles {
    data class Request(
        val requestedVersion: String = InteropVersion.CURRENT.value,
    )

    data class Response(
        val status: HubInteropStatus,
        val compatibilitySignal: InteropCompatibilitySignal,
        val surfaceDescriptor: HubSurfaceDescriptor? = null,
        val message: String? = null,
    )

    fun toBundle(request: Request): Bundle = bundleOf(
        HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION to request.requestedVersion,
    )

    fun fromBundle(bundle: Bundle?): Request {
        return Request(
            requestedVersion = bundle?.getString(HubInteropAndroidContract.BundleKeys.CONTRACT_VERSION)
                ?: InteropVersion.CURRENT.value,
        )
    }

    fun toResponseBundle(response: Response): Bundle = bundleOf(
        HubInteropAndroidContract.BundleKeys.STATUS to response.status.wireName,
        HubInteropAndroidContract.BundleKeys.COMPATIBILITY_SIGNAL to InteropBundleCodec.compatibilitySignalToBundle(
            response.compatibilitySignal,
        ),
        HubInteropAndroidContract.BundleKeys.SURFACE_DESCRIPTOR to response.surfaceDescriptor?.let(
            InteropBundleCodec::surfaceToBundle,
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
            surfaceDescriptor = bundle.getBundle(HubInteropAndroidContract.BundleKeys.SURFACE_DESCRIPTOR)
                ?.let(InteropBundleCodec::surfaceFromBundle),
            message = bundle.getString(HubInteropAndroidContract.BundleKeys.MESSAGE),
        )
    }
}
