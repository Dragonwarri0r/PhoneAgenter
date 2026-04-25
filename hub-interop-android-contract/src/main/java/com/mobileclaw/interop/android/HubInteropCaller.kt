package com.mobileclaw.interop.android

import android.content.ContentResolver
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.android.bundle.TaskBundles
import com.mobileclaw.interop.android.call.ArtifactCall
import com.mobileclaw.interop.android.call.AuthorizationCall
import com.mobileclaw.interop.android.call.DiscoveryCall
import com.mobileclaw.interop.android.call.InvocationCall
import com.mobileclaw.interop.android.call.TaskCall
import com.mobileclaw.interop.contract.InteropHandle

class HubInteropCaller(
    private val contentResolver: ContentResolver,
    hostPackageName: String,
) {
    private val authority: String = HubInteropAndroidContract.authorityFor(hostPackageName)

    fun discoverSurface(
        request: DiscoveryBundles.Request = DiscoveryBundles.Request(),
    ): DiscoveryBundles.Response? {
        return DiscoveryCall.execute(
            contentResolver = contentResolver,
            authority = authority,
            request = request,
        )
    }

    fun requestAuthorization(
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        return AuthorizationCall.requestAuthorization(
            contentResolver = contentResolver,
            authority = authority,
            request = request,
        )
    }

    fun getGrantStatus(
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        return AuthorizationCall.getGrantStatus(
            contentResolver = contentResolver,
            authority = authority,
            request = request,
        )
    }

    fun revokeGrant(
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        return AuthorizationCall.revokeGrant(
            contentResolver = contentResolver,
            authority = authority,
            request = request,
        )
    }

    fun invokeCapability(
        request: InvocationBundles.Request,
    ): InvocationBundles.Response? {
        return InvocationCall.execute(
            contentResolver = contentResolver,
            authority = authority,
            request = request,
        )
    }

    fun getTask(handle: InteropHandle): TaskBundles.Response? {
        return TaskCall.execute(
            contentResolver = contentResolver,
            authority = authority,
            request = TaskBundles.Request(handle = handle),
        )
    }

    fun getArtifact(handle: InteropHandle): ArtifactBundles.Response? {
        return ArtifactCall.execute(
            contentResolver = contentResolver,
            authority = authority,
            request = ArtifactBundles.Request(handle = handle),
        )
    }
}
