package com.mobileclaw.interop.android

import android.net.Uri
import com.mobileclaw.interop.contract.InteropHandle

object HubInteropUriBuilder {
    fun surface(authority: String): Uri = Uri.Builder()
        .scheme("content")
        .authority(authority)
        .appendPath(HubInteropAndroidContract.Paths.SURFACE)
        .build()

    fun grant(authority: String, handle: InteropHandle? = null): Uri = Uri.Builder()
        .scheme("content")
        .authority(authority)
        .appendPath(HubInteropAndroidContract.Paths.GRANTS)
        .apply {
            if (handle != null) {
                appendPath(handle.opaqueValue)
            }
        }
        .build()

    fun task(authority: String, handle: InteropHandle? = null): Uri = Uri.Builder()
        .scheme("content")
        .authority(authority)
        .appendPath(HubInteropAndroidContract.Paths.TASKS)
        .apply {
            if (handle != null) {
                appendPath(handle.opaqueValue)
            }
        }
        .build()

    fun artifact(authority: String, handle: InteropHandle? = null): Uri = Uri.Builder()
        .scheme("content")
        .authority(authority)
        .appendPath(HubInteropAndroidContract.Paths.ARTIFACTS)
        .apply {
            if (handle != null) {
                appendPath(handle.opaqueValue)
            }
        }
        .build()
}
