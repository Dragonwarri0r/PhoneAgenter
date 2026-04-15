package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryWritebackService @Inject constructor(
    private val scopedMemoryRepository: ScopedMemoryRepository,
    private val appStrings: AppStrings,
) {
    suspend fun writeSuccessfulReplyMemory(
        request: RuntimeRequest,
        outputText: String,
    ) {
        val now = System.currentTimeMillis()
        val item = MemoryItem(
            memoryId = "mem-runtime-${request.requestId}",
            logicalRecordId = "logical-runtime-${request.requestId}",
            title = appStrings.get(R.string.memory_latest_runtime_exchange_title),
            contentText = appStrings.get(
                R.string.memory_latest_runtime_exchange_content,
                request.userInput,
                outputText,
            ),
            summaryText = appStrings.get(
                R.string.memory_latest_runtime_exchange_summary,
                request.originApp,
            ),
            lifecycle = MemoryLifecycle.WORKING,
            scope = MemoryScope.APP_SCOPED,
            exposurePolicy = MemoryExposurePolicy.PRIVATE,
            syncPolicy = MemorySyncPolicy.LOCAL_ONLY,
            subjectKey = request.workspaceId,
            originApp = request.originApp,
            originDeviceId = request.deviceId,
            originUserId = "single_user",
            sourceType = MemorySourceType.RUNTIME_WRITEBACK,
            confidence = 0.88,
            logicalVersion = 1,
            schemaVersion = 1,
            isPinned = false,
            isManuallyEdited = false,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
            expiresAtEpochMillis = now + 7 * 24 * 60 * 60 * 1000L,
            evidenceRef = "local://runtime/${request.requestId}",
        )
        scopedMemoryRepository.upsert(item)
    }
}
