package com.mobileclaw.app.runtime.appfunctions

import com.mobileclaw.interop.android.HubInteropAndroidContract
import com.mobileclaw.interop.contract.InteropIds

object AppFunctionExposureCatalog {
    const val DRAFT_REPLY_METHOD = HubInteropAndroidContract.AppFunctions.DRAFT_REPLY
    const val EXPORT_PORTABILITY_METHOD = HubInteropAndroidContract.AppFunctions.EXPORT_PORTABILITY_SUMMARY

    fun functionHintForCapability(capabilityId: String): String? = when (capabilityId) {
        InteropIds.Capability.GENERATE_REPLY -> DRAFT_REPLY_METHOD
        InteropIds.Capability.EXTERNAL_SHARE -> EXPORT_PORTABILITY_METHOD
        else -> null
    }
}
