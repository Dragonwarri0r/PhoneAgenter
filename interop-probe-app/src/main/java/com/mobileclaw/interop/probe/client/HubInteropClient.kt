package com.mobileclaw.interop.probe.client

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.mobileclaw.interop.android.HubInteropAndroidContract
import com.mobileclaw.interop.android.HubInteropCaller
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.android.bundle.TaskBundles

interface HubInteropClient {
    fun isHostAvailable(hostPackageName: String): Boolean

    fun launchIntentFor(hostPackageName: String): Intent?

    fun discover(
        hostPackageName: String,
        request: DiscoveryBundles.Request = DiscoveryBundles.Request(),
    ): DiscoveryBundles.Response?

    fun requestAuthorization(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response?

    fun getGrantStatus(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response?

    fun revokeGrant(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response?

    fun invoke(
        hostPackageName: String,
        request: InvocationBundles.Request,
    ): InvocationBundles.Response?

    fun getTask(
        hostPackageName: String,
        request: TaskBundles.Request,
    ): TaskBundles.Response?

    fun getArtifact(
        hostPackageName: String,
        request: ArtifactBundles.Request,
    ): ArtifactBundles.Response?
}

class AndroidHubInteropClient(
    context: Context,
) : HubInteropClient {
    private val appContext = context.applicationContext

    override fun isHostAvailable(hostPackageName: String): Boolean {
        val authority = HubInteropAndroidContract.authorityFor(hostPackageName)
        val packageManager = appContext.packageManager
        val providerInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveContentProvider(
                authority,
                PackageManager.ComponentInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveContentProvider(authority, 0)
        }
        return providerInfo != null
    }

    override fun launchIntentFor(hostPackageName: String): Intent? {
        return appContext.packageManager.getLaunchIntentForPackage(hostPackageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun discover(
        hostPackageName: String,
        request: DiscoveryBundles.Request,
    ): DiscoveryBundles.Response? {
        return caller(hostPackageName).discoverSurface(request)
    }

    override fun requestAuthorization(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        return caller(hostPackageName).requestAuthorization(request)
    }

    override fun getGrantStatus(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        return caller(hostPackageName).getGrantStatus(request)
    }

    override fun revokeGrant(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        return caller(hostPackageName).revokeGrant(request)
    }

    override fun invoke(
        hostPackageName: String,
        request: InvocationBundles.Request,
    ): InvocationBundles.Response? {
        return caller(hostPackageName).invokeCapability(request)
    }

    override fun getTask(
        hostPackageName: String,
        request: TaskBundles.Request,
    ): TaskBundles.Response? {
        return com.mobileclaw.interop.android.call.TaskCall.execute(
            contentResolver = appContext.contentResolver,
            authority = HubInteropAndroidContract.authorityFor(hostPackageName),
            request = request,
        )
    }

    override fun getArtifact(
        hostPackageName: String,
        request: ArtifactBundles.Request,
    ): ArtifactBundles.Response? {
        return com.mobileclaw.interop.android.call.ArtifactCall.execute(
            contentResolver = appContext.contentResolver,
            authority = HubInteropAndroidContract.authorityFor(hostPackageName),
            request = request,
        )
    }

    private fun caller(hostPackageName: String): HubInteropCaller {
        return HubInteropCaller(
            contentResolver = appContext.contentResolver,
            hostPackageName = hostPackageName,
        )
    }
}
