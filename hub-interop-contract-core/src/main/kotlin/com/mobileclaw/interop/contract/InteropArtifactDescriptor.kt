package com.mobileclaw.interop.contract

enum class ArtifactAccessMode {
    READ_ONLY,
    WRITE_CAPABLE,
}

enum class InteropArtifactLifecycleState {
    AVAILABLE,
    EXPIRED,
    DELETED,
}

data class InteropArtifactDescriptor(
    val handle: InteropHandle,
    val displayName: String,
    val mimeType: String,
    val accessMode: ArtifactAccessMode,
    val contentUri: String? = null,
    val summary: String = "",
    val artifactType: String = mimeType,
    val lifecycleState: InteropArtifactLifecycleState = InteropArtifactLifecycleState.AVAILABLE,
    val availability: InteropAvailabilityStatus = InteropAvailabilityStatus.AVAILABLE,
    val createdAtEpochMillis: Long? = null,
    val expiresAtEpochMillis: Long? = null,
    val deletedAtEpochMillis: Long? = null,
)
