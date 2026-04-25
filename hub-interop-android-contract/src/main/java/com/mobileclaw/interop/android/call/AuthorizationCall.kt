package com.mobileclaw.interop.android.call

import android.content.ContentResolver
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.android.HubInteropUriBuilder
import com.mobileclaw.interop.android.bundle.AuthorizationBundles

object AuthorizationCall {
    fun requestAuthorization(
        contentResolver: ContentResolver,
        authority: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        val bundle = contentResolver.call(
            HubInteropUriBuilder.grant(authority),
            HubInteropMethod.REQUEST_AUTHORIZATION.wireName,
            null,
            AuthorizationBundles.toBundle(request),
        ) ?: return null
        return AuthorizationBundles.fromResponseBundle(bundle)
    }

    fun getGrantStatus(
        contentResolver: ContentResolver,
        authority: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        val bundle = contentResolver.call(
            HubInteropUriBuilder.grant(authority, request.handle),
            HubInteropMethod.GET_GRANT_STATUS.wireName,
            null,
            AuthorizationBundles.toBundle(request),
        ) ?: return null
        return AuthorizationBundles.fromResponseBundle(bundle)
    }

    fun revokeGrant(
        contentResolver: ContentResolver,
        authority: String,
        request: AuthorizationBundles.Request,
    ): AuthorizationBundles.Response? {
        val bundle = contentResolver.call(
            HubInteropUriBuilder.grant(authority, request.handle),
            HubInteropMethod.REVOKE_GRANT.wireName,
            null,
            AuthorizationBundles.toBundle(request),
        ) ?: return null
        return AuthorizationBundles.fromResponseBundle(bundle)
    }
}
