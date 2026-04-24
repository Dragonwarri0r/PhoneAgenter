package com.mobileclaw.app.runtime.interop

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import dagger.hilt.android.EntryPointAccessors

class HubInteropProvider : ContentProvider() {
    private val dispatcher: HubInteropMethodDispatcher by lazy {
        EntryPointAccessors.fromApplication(
            context?.applicationContext ?: error("hub_interop_provider_context_missing"),
            HubInteropEntryPoint::class.java,
        ).methodDispatcher()
    }

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        return dispatcher.dispatch(
            method = method,
            extras = extras,
        )
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
