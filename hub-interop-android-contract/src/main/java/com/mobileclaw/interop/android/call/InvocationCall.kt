package com.mobileclaw.interop.android.call

import android.content.ContentResolver
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.android.HubInteropUriBuilder
import com.mobileclaw.interop.android.bundle.InvocationBundles

object InvocationCall {
    fun execute(
        contentResolver: ContentResolver,
        authority: String,
        request: InvocationBundles.Request,
    ): InvocationBundles.Response? {
        val bundle = contentResolver.call(
            HubInteropUriBuilder.surface(authority),
            HubInteropMethod.INVOKE_CAPABILITY.wireName,
            null,
            InvocationBundles.toBundle(request),
        ) ?: return null
        return InvocationBundles.fromResponseBundle(bundle)
    }
}
