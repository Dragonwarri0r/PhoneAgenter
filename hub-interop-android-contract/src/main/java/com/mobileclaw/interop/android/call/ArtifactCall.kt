package com.mobileclaw.interop.android.call

import android.content.ContentResolver
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.android.HubInteropUriBuilder
import com.mobileclaw.interop.android.bundle.ArtifactBundles

object ArtifactCall {
    fun execute(
        contentResolver: ContentResolver,
        authority: String,
        request: ArtifactBundles.Request,
    ): ArtifactBundles.Response? {
        val bundle = contentResolver.call(
            HubInteropUriBuilder.artifact(authority, request.handle),
            HubInteropMethod.GET_ARTIFACT.wireName,
            null,
            ArtifactBundles.toBundle(request),
        ) ?: return null
        return ArtifactBundles.fromResponseBundle(bundle)
    }
}
