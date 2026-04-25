package com.mobileclaw.app.runtime.interop

import com.mobileclaw.interop.android.CompatibilityBundleAdapter
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropVersion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HubInteropCompatibilityService @Inject constructor() {
    fun evaluate(
        requestedVersion: String?,
        unknownFieldCount: Int = 0,
        requiredUnknownFieldCount: Int = 0,
        optionalUnknownFieldCount: Int = 0,
        extensionFieldCount: Int = 0,
    ): InteropCompatibilitySignal {
        return CompatibilityBundleAdapter.evaluateSignal(
            requestedVersion = requestedVersion ?: InteropVersion.CURRENT.value,
            supportedVersion = InteropVersion.CURRENT.value,
            unknownFieldCount = unknownFieldCount,
            requiredUnknownFieldCount = requiredUnknownFieldCount,
            optionalUnknownFieldCount = optionalUnknownFieldCount,
            extensionFieldCount = extensionFieldCount,
        )
    }
}
