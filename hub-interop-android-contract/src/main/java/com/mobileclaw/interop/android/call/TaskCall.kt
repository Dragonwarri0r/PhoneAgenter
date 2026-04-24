package com.mobileclaw.interop.android.call

import android.content.ContentResolver
import com.mobileclaw.interop.android.HubInteropMethod
import com.mobileclaw.interop.android.HubInteropUriBuilder
import com.mobileclaw.interop.android.bundle.TaskBundles

object TaskCall {
    fun execute(
        contentResolver: ContentResolver,
        authority: String,
        request: TaskBundles.Request,
    ): TaskBundles.Response? {
        val bundle = contentResolver.call(
            HubInteropUriBuilder.task(authority, request.handle),
            HubInteropMethod.GET_TASK.wireName,
            null,
            TaskBundles.toBundle(request),
        ) ?: return null
        return TaskBundles.fromResponseBundle(bundle)
    }
}
