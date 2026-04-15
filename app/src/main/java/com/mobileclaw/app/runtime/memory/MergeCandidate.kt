package com.mobileclaw.app.runtime.memory

data class MergeCandidate(
    val logicalRecordId: String,
    val recordId: String,
    val originDeviceId: String?,
    val originUserId: String?,
    val logicalVersion: Long,
    val updatedAtEpochMillis: Long,
    val exposurePolicy: MemoryExposurePolicy,
    val syncPolicy: MemorySyncPolicy,
    val summaryPayload: String,
    val rawPayloadRef: String?,
)

fun MemoryItem.toMergeCandidate(): MergeCandidate = MergeCandidate(
    logicalRecordId = logicalRecordId,
    recordId = memoryId,
    originDeviceId = originDeviceId,
    originUserId = originUserId,
    logicalVersion = logicalVersion,
    updatedAtEpochMillis = updatedAtEpochMillis,
    exposurePolicy = exposurePolicy,
    syncPolicy = syncPolicy,
    summaryPayload = summaryText,
    rawPayloadRef = evidenceRef.takeUnless { exposurePolicy != MemoryExposurePolicy.SHAREABLE_FULL },
)
