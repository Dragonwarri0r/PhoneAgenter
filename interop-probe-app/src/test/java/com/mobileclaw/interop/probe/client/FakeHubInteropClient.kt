package com.mobileclaw.interop.probe.client

import android.content.Intent
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.android.bundle.TaskBundles

class FakeHubInteropClient : HubInteropClient {
    var hostAvailable: Boolean = true
    var discoveryResponse: DiscoveryBundles.Response? = null
    var requestAuthorizationResponse: AuthorizationBundles.Response? = null
    var grantStatusResponse: AuthorizationBundles.Response? = null
    var revokeGrantResponse: AuthorizationBundles.Response? = null
    var invocationResponse: InvocationBundles.Response? = null
    var taskResponse: TaskBundles.Response? = null
    var artifactResponse: ArtifactBundles.Response? = null

    override fun isHostAvailable(hostPackageName: String): Boolean = hostAvailable

    override fun launchIntentFor(hostPackageName: String): Intent? = null

    override fun discover(
        hostPackageName: String,
        request: DiscoveryBundles.Request,
    ): DiscoveryBundles.Response? = discoveryResponse

    override fun requestAuthorization(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? = requestAuthorizationResponse

    override fun getGrantStatus(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? = grantStatusResponse

    override fun revokeGrant(
        hostPackageName: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? = revokeGrantResponse

    override fun invoke(
        hostPackageName: String,
        request: InvocationBundles.Request,
    ): InvocationBundles.Response? = invocationResponse

    override fun getTask(
        hostPackageName: String,
        request: TaskBundles.Request,
    ): TaskBundles.Response? = taskResponse

    override fun getArtifact(
        hostPackageName: String,
        request: ArtifactBundles.Request,
    ): ArtifactBundles.Response? = artifactResponse
}
