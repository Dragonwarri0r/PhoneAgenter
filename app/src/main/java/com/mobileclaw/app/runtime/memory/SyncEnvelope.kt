package com.mobileclaw.app.runtime.memory

data class SyncEnvelope(
    val recordId: String,
    val logicalRecordId: String,
    val originDeviceId: String?,
    val originUserId: String?,
    val logicalVersion: Long,
    val syncPolicy: MemorySyncPolicy,
    val exposurePolicy: MemoryExposurePolicy,
    val schemaVersion: Int,
)

fun MemoryItem.toSyncEnvelope(): SyncEnvelope = SyncEnvelope(
    recordId = memoryId,
    logicalRecordId = logicalRecordId,
    originDeviceId = originDeviceId,
    originUserId = originUserId,
    logicalVersion = logicalVersion,
    syncPolicy = syncPolicy,
    exposurePolicy = exposurePolicy,
    schemaVersion = schemaVersion,
)
