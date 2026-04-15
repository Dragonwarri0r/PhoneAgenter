package com.mobileclaw.app.runtime.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_items")
data class MemoryItem(
    @PrimaryKey val memoryId: String,
    val logicalRecordId: String,
    val title: String,
    val contentText: String,
    val summaryText: String,
    val lifecycle: MemoryLifecycle,
    val scope: MemoryScope,
    val exposurePolicy: MemoryExposurePolicy,
    val syncPolicy: MemorySyncPolicy,
    val subjectKey: String?,
    val originApp: String?,
    val originDeviceId: String?,
    val originUserId: String?,
    val sourceType: MemorySourceType,
    val confidence: Double,
    val logicalVersion: Long,
    val schemaVersion: Int,
    val isPinned: Boolean,
    val isManuallyEdited: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long?,
    val evidenceRef: String?,
) {
    fun isExpired(nowEpochMillis: Long): Boolean {
        if (isPinned) return false
        return expiresAtEpochMillis?.let { it <= nowEpochMillis } ?: false
    }

    fun userVisibleText(): String {
        return when (exposurePolicy) {
            MemoryExposurePolicy.PRIVATE -> summaryText
            MemoryExposurePolicy.SHAREABLE_SUMMARY -> summaryText
            MemoryExposurePolicy.SHAREABLE_FULL -> contentText
        }
    }
}

enum class MemoryLifecycle {
    DURABLE,
    WORKING,
    EPHEMERAL,
}

enum class MemoryScope {
    GLOBAL,
    APP_SCOPED,
    CONTACT_SCOPED,
    DEVICE_SCOPED,
}

enum class MemoryExposurePolicy {
    PRIVATE,
    SHAREABLE_SUMMARY,
    SHAREABLE_FULL,
}

enum class MemorySyncPolicy {
    LOCAL_ONLY,
    SUMMARY_SYNC_READY,
    FULL_SYNC_READY,
}

enum class ExportMode {
    SUMMARY_ONLY,
    FULL_RECORD,
}

enum class MemorySourceType {
    USER_EDIT,
    SYSTEM_SOURCE,
    INFERRED,
    RUNTIME_WRITEBACK,
}
