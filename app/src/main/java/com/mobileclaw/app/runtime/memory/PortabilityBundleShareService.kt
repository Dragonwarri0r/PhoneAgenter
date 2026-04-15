package com.mobileclaw.app.runtime.memory

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortabilityBundleShareService @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun shareBundle(preview: PortabilityBundlePreview, chooserTitle: String): Result<Unit> {
        if (!preview.canShare) {
            return Result.failure(IllegalStateException("Bundle is not shareable"))
        }
        return runCatching {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, preview.bundleDocumentText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
