package com.mobileclaw.interop.android.call

import android.content.ContentResolver
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.android.HubInteropUriBuilder
import com.mobileclaw.interop.android.bundle.DiscoveryBundles

object DiscoveryCall {
    fun execute(
        contentResolver: ContentResolver,
        authority: String,
        request: DiscoveryBundles.Request = DiscoveryBundles.Request(),
    ): DiscoveryBundles.Response? {
        val bundle = contentResolver.call(
            HubInteropUriBuilder.surface(authority),
            HubInteropMethod.DISCOVER_SURFACE.wireName,
            null,
            DiscoveryBundles.toBundle(request),
        ) ?: return null
        return DiscoveryBundles.fromResponseBundle(bundle)
    }
}
