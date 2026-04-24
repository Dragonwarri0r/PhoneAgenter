package com.mobileclaw.interop.contract

enum class InteropTaskStatus {
    PENDING,
    RUNNING,
    INPUT_REQUIRED,
    COMPLETED,
    FAILED,
    CANCELLED,
}

enum class InteropTaskLifecycleState {
    QUEUED,
    ACTIVE,
    INPUT_REQUIRED,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED,
    DELETED,
}

data class InteropTaskDescriptor(
    val handle: InteropHandle,
    val displayName: String,
    val status: InteropTaskStatus,
    val summary: String = "",
    val artifactHandles: List<InteropHandle> = emptyList(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
    val lifecycleState: InteropTaskLifecycleState = when (status) {
        InteropTaskStatus.PENDING -> InteropTaskLifecycleState.QUEUED
        InteropTaskStatus.RUNNING -> InteropTaskLifecycleState.ACTIVE
        InteropTaskStatus.INPUT_REQUIRED -> InteropTaskLifecycleState.INPUT_REQUIRED
        InteropTaskStatus.COMPLETED -> InteropTaskLifecycleState.COMPLETED
        InteropTaskStatus.FAILED -> InteropTaskLifecycleState.FAILED
        InteropTaskStatus.CANCELLED -> InteropTaskLifecycleState.CANCELLED
    },
    val availability: InteropAvailabilityStatus = InteropAvailabilityStatus.AVAILABLE,
    val createdAtEpochMillis: Long? = null,
    val expiresAtEpochMillis: Long? = null,
    val deletedAtEpochMillis: Long? = null,
)
