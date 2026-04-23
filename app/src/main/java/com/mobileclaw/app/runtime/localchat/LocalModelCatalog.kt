package com.mobileclaw.app.runtime.localchat

import android.net.Uri
import kotlinx.coroutines.flow.Flow

enum class ModelAvailabilityStatus {
    UNAVAILABLE,
    PREPARING,
    READY,
    FAILED,
}

data class ModelModalityCapabilities(
    val supportsImage: Boolean = false,
    val supportsAudio: Boolean = false,
)

data class LocalModelProfile(
    val modelId: String,
    val displayName: String,
    val providerLabel: String,
    val availabilityStatus: ModelAvailabilityStatus,
    val statusMessage: String,
    val isSelectable: Boolean,
    val modalityCapabilities: ModelModalityCapabilities = ModelModalityCapabilities(),
    val supportsManualCapabilityOverride: Boolean = false,
)

data class ModelHealthSnapshot(
    val modelId: String,
    val availabilityStatus: ModelAvailabilityStatus,
    val headline: String,
    val supportingText: String,
    val primaryActionLabel: String? = null,
    val modalityCapabilities: ModelModalityCapabilities = ModelModalityCapabilities(),
)

sealed interface ModelImportResult {
    data class Success(
        val model: LocalModelProfile,
    ) : ModelImportResult

    data class Failure(
        val message: String,
    ) : ModelImportResult
}

interface LocalModelCatalog {
    val models: Flow<List<LocalModelProfile>>

    fun observeHealth(modelId: String): Flow<ModelHealthSnapshot>

    suspend fun selectModel(modelId: String): LocalModelProfile?

    suspend fun clearModelRuntimeFailure(modelId: String): LocalModelProfile?

    suspend fun importModel(sourceUri: Uri): ModelImportResult

    suspend fun updateModelCapabilities(
        modelId: String,
        supportsImage: Boolean,
        supportsAudio: Boolean,
    ): LocalModelProfile?
}
