package com.mobileclaw.app.runtime.interop

import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.android.bundle.InvocationBundles

data class InteropRequestContext(
    val requestId: String,
    val caller: InteropCallerContext,
    val capabilityId: String,
    val requestedScopes: List<String>,
    val requestedVersion: String,
) {
    companion object {
        fun from(
            request: AuthorizationBundles.Request,
            defaultScopes: List<String>,
        ): InteropRequestContext {
            return InteropRequestContext(
                requestId = request.requestId,
                caller = InteropCallerContext.from(request.callerIdentity),
                capabilityId = request.capabilityId,
                requestedScopes = request.requestedScopes.normalized(defaultScopes),
                requestedVersion = request.requestedVersion,
            )
        }

        fun from(
            request: InvocationBundles.Request,
            defaultScopes: List<String>,
        ): InteropRequestContext {
            return InteropRequestContext(
                requestId = request.requestId,
                caller = InteropCallerContext.from(request.callerIdentity),
                capabilityId = request.capabilityId,
                requestedScopes = request.requestedScopes.normalized(defaultScopes),
                requestedVersion = request.requestedVersion,
            )
        }

        private fun List<String>.normalized(defaultScopes: List<String>): List<String> {
            return if (isEmpty()) {
                defaultScopes.distinct()
            } else {
                distinct()
            }
        }
    }
}
