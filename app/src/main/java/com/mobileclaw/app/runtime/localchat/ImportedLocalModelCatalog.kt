package com.mobileclaw.app.runtime.localchat

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.Regex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private const val IMPORTED_MODELS_DIR = "imported_models"
private val IMPORTED_MODELS_KEY = stringPreferencesKey("imported_models_json")
private val NON_MODEL_CHARACTERS = Regex("[^a-z0-9]+")

@Singleton
class ImportedLocalModelCatalog @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: DataStore<Preferences>,
    private val appStrings: AppStrings,
) : LocalModelCatalog {

    private val bundledModels = listOf(
        LocalModelProfile(
            modelId = "local-gemini-mini",
            displayName = "Local Gemini Mini",
            providerLabel = appStrings.get(R.string.model_provider_starter_placeholder),
            availabilityStatus = ModelAvailabilityStatus.UNAVAILABLE,
            statusMessage = appStrings.get(R.string.model_import_required_message),
            isSelectable = false,
            modalityCapabilities = ModelModalityCapabilities(
                supportsImage = true,
                supportsAudio = true,
            ),
            supportsManualCapabilityOverride = false,
        ),
        LocalModelProfile(
            modelId = "local-gemini-large",
            displayName = "Local Gemini Large",
            providerLabel = appStrings.get(R.string.model_provider_starter_placeholder),
            availabilityStatus = ModelAvailabilityStatus.UNAVAILABLE,
            statusMessage = appStrings.get(R.string.model_import_required_message),
            isSelectable = false,
            modalityCapabilities = ModelModalityCapabilities(
                supportsImage = true,
                supportsAudio = true,
            ),
            supportsManualCapabilityOverride = false,
        ),
    )

    override val models: Flow<List<LocalModelProfile>> = preferences.data.map { storedPreferences ->
        val importedModels = readImportedModels(
            rawJson = storedPreferences[IMPORTED_MODELS_KEY].orEmpty(),
        ).map { record ->
            val modelFile = File(record.filePath)
            val isPresent = modelFile.exists()
            val runtimeFailureMessage = record.runtimeFailureMessage?.takeIf { it.isNotBlank() }
            val availabilityStatus = when {
                !isPresent -> ModelAvailabilityStatus.FAILED
                runtimeFailureMessage != null -> ModelAvailabilityStatus.FAILED
                else -> ModelAvailabilityStatus.READY
            }
            val statusMessage = when {
                !isPresent -> appStrings.get(R.string.model_imported_file_missing)
                runtimeFailureMessage != null -> runtimeFailureMessage
                else -> appStrings.get(R.string.model_imported_from, record.originalFileName)
            }
            LocalModelProfile(
                modelId = record.modelId,
                displayName = record.displayName,
                providerLabel = appStrings.get(R.string.model_provider_imported_file),
                availabilityStatus = availabilityStatus,
                statusMessage = statusMessage,
                isSelectable = isPresent,
                modalityCapabilities = record.resolveCapabilities(),
                supportsManualCapabilityOverride = true,
            )
        }
        (bundledModels + importedModels).sortedWith(
            compareByDescending<LocalModelProfile> { it.isSelectable }
                .thenBy { it.displayName.lowercase() },
        )
    }

    override fun observeHealth(modelId: String): Flow<ModelHealthSnapshot> {
        return models.map { allModels ->
            val model = allModels.firstOrNull { it.modelId == modelId }
                ?: allModels.firstOrNull()
                ?: unavailablePlaceholder(modelId)
            ModelHealthSnapshot(
                modelId = model.modelId,
                availabilityStatus = model.availabilityStatus,
                headline = when (model.availabilityStatus) {
                    ModelAvailabilityStatus.READY -> appStrings.get(R.string.model_headline_ready)
                    ModelAvailabilityStatus.PREPARING -> appStrings.get(R.string.model_headline_preparing)
                    ModelAvailabilityStatus.FAILED -> appStrings.get(R.string.model_headline_unavailable)
                    ModelAvailabilityStatus.UNAVAILABLE -> appStrings.get(R.string.model_headline_no_local_model)
                },
                supportingText = model.statusMessage,
                primaryActionLabel = when (model.availabilityStatus) {
                    ModelAvailabilityStatus.PREPARING -> appStrings.get(R.string.common_wait)
                    else -> appStrings.get(R.string.common_choose_model)
                },
                modalityCapabilities = model.modalityCapabilities,
            )
        }
    }

    override suspend fun selectModel(modelId: String): LocalModelProfile? {
        return models.first().firstOrNull { it.modelId == modelId && it.isSelectable }
    }

    override suspend fun clearModelRuntimeFailure(modelId: String): LocalModelProfile? {
        updateImportedModel(modelId) { record ->
            if (record.runtimeFailureMessage == null) {
                record
            } else {
                record.copy(runtimeFailureMessage = null)
            }
        }
        return models.first().firstOrNull { it.modelId == modelId }
    }

    override suspend fun updateModelCapabilities(
        modelId: String,
        supportsImage: Boolean,
        supportsAudio: Boolean,
    ): LocalModelProfile? {
        updateImportedModel(modelId) { record ->
            record.copy(
                manualSupportsImage = supportsImage,
                manualSupportsAudio = supportsAudio,
            )
        }
        return models.first().firstOrNull { it.modelId == modelId }
    }

    suspend fun resolveRuntimeModel(modelId: String): RuntimeModelDescriptor? {
        val imported = readImportedModels(
            rawJson = preferences.data.first()[IMPORTED_MODELS_KEY].orEmpty(),
        ).firstOrNull { it.modelId == modelId } ?: return null
        val modelFile = File(imported.filePath)
        if (!modelFile.exists()) {
            return null
        }
        return RuntimeModelDescriptor(
            modelId = imported.modelId,
            displayName = imported.displayName,
            filePath = modelFile.absolutePath,
            originalFileName = imported.originalFileName,
            modalityCapabilities = imported.resolveCapabilities(),
        )
    }

    override suspend fun importModel(sourceUri: Uri): ModelImportResult {
        return try {
            val originalFileName = resolveDisplayName(sourceUri).ifBlank {
                "model-${System.currentTimeMillis()}.bin"
            }
            val displayName = originalFileName.substringBeforeLast('.').toDisplayName()
            val storedFileName = "${System.currentTimeMillis()}-${originalFileName.sanitizedFileName()}"
            val destinationDir = File(context.filesDir, IMPORTED_MODELS_DIR).apply { mkdirs() }
            val destinationFile = File(destinationDir, storedFileName)

            copyUriToFile(sourceUri = sourceUri, destinationFile = destinationFile)

            val modelRecord = ImportedModelRecord(
                modelId = createModelId(displayName = displayName, fileName = storedFileName),
                displayName = displayName,
                originalFileName = originalFileName,
                filePath = destinationFile.absolutePath,
                runtimeFailureMessage = null,
            )
            persistImportedModel(modelRecord)

            ModelImportResult.Success(
                model = LocalModelProfile(
                    modelId = modelRecord.modelId,
                    displayName = modelRecord.displayName,
                    providerLabel = appStrings.get(R.string.model_provider_imported_file),
                    availabilityStatus = ModelAvailabilityStatus.READY,
                    statusMessage = appStrings.get(R.string.model_imported_from, modelRecord.originalFileName),
                    isSelectable = true,
                    modalityCapabilities = modelRecord.resolveCapabilities(),
                    supportsManualCapabilityOverride = true,
                ),
            )
        } catch (exception: Exception) {
            ModelImportResult.Failure(
                message = exception.message ?: appStrings.get(R.string.model_import_failed),
            )
        }
    }

    private suspend fun persistImportedModel(record: ImportedModelRecord) {
        preferences.edit { storedPreferences ->
            val existing = readImportedModels(storedPreferences[IMPORTED_MODELS_KEY].orEmpty())
                .filterNot { it.modelId == record.modelId }
            val updated = existing + record
            storedPreferences[IMPORTED_MODELS_KEY] = updated.toJson()
        }
    }

    suspend fun markRuntimeFailure(
        modelId: String,
        message: String,
    ) {
        updateImportedModel(modelId) { record ->
            record.copy(runtimeFailureMessage = message)
        }
    }

    private suspend fun updateImportedModel(
        modelId: String,
        transform: (ImportedModelRecord) -> ImportedModelRecord,
    ) {
        preferences.edit { storedPreferences ->
            val updated = readImportedModels(storedPreferences[IMPORTED_MODELS_KEY].orEmpty()).map { record ->
                if (record.modelId == modelId) {
                    transform(record)
                } else {
                    record
                }
            }
            storedPreferences[IMPORTED_MODELS_KEY] = updated.toJson()
        }
    }

    private fun readImportedModels(rawJson: String): List<ImportedModelRecord> {
        if (rawJson.isBlank()) {
            return emptyList()
        }
        return runCatching {
            val jsonArray = JSONArray(rawJson)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(index) ?: continue
                    add(
                        ImportedModelRecord(
                            modelId = item.optString("modelId"),
                            displayName = item.optString("displayName"),
                            originalFileName = item.optString("originalFileName"),
                            filePath = item.optString("filePath"),
                            runtimeFailureMessage = item.optString("runtimeFailureMessage")
                                .takeIf { item.has("runtimeFailureMessage") && it.isNotBlank() },
                            manualSupportsImage = item.optBoolean("manualSupportsImage")
                                .takeIf { item.has("manualSupportsImage") },
                            manualSupportsAudio = item.optBoolean("manualSupportsAudio")
                                .takeIf { item.has("manualSupportsAudio") },
                        ),
                    )
                }
            }.filter { it.modelId.isNotBlank() && it.filePath.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun copyUriToFile(
        sourceUri: Uri,
        destinationFile: File,
    ) {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IOException(appStrings.get(R.string.model_open_selected_file_failed))
        inputStream.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun resolveDisplayName(sourceUri: Uri): String {
        val resolver = context.contentResolver
        if (sourceUri.scheme == ContentResolver.SCHEME_CONTENT) {
            resolver.query(sourceUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0 && cursor.moveToFirst()) {
                        return cursor.getString(columnIndex).orEmpty()
                    }
                }
        }
        return sourceUri.lastPathSegment.orEmpty().substringAfterLast('/')
    }

    private fun unavailablePlaceholder(modelId: String): LocalModelProfile {
        return LocalModelProfile(
            modelId = modelId,
            displayName = appStrings.get(R.string.model_local_model),
            providerLabel = appStrings.get(R.string.model_provider_imported_file),
            availabilityStatus = ModelAvailabilityStatus.UNAVAILABLE,
            statusMessage = appStrings.get(R.string.model_headline_no_local_model),
            isSelectable = false,
            modalityCapabilities = ModelModalityCapabilities(),
            supportsManualCapabilityOverride = false,
        )
    }

    private fun createModelId(
        displayName: String,
        fileName: String,
    ): String {
        val baseName = displayName.lowercase().replace(NON_MODEL_CHARACTERS, "-").trim('-')
        val fileSuffix = fileName.substringAfterLast('-').substringBeforeLast('.')
            .lowercase()
            .replace(NON_MODEL_CHARACTERS, "-")
            .trim('-')
        val normalizedBaseName = if (baseName.isBlank()) "imported-model" else baseName
        return listOf("imported", normalizedBaseName, fileSuffix)
            .filter { it.isNotBlank() }
            .joinToString("-")
    }
}

private data class ImportedModelRecord(
    val modelId: String,
    val displayName: String,
    val originalFileName: String,
    val filePath: String,
    val runtimeFailureMessage: String? = null,
    val manualSupportsImage: Boolean? = null,
    val manualSupportsAudio: Boolean? = null,
)

data class RuntimeModelDescriptor(
    val modelId: String,
    val displayName: String,
    val filePath: String,
    val originalFileName: String,
    val modalityCapabilities: ModelModalityCapabilities,
)

private fun String.sanitizedFileName(): String {
    return replace(Regex("[^A-Za-z0-9._-]"), "_")
}

private fun String.toDisplayName(): String {
    return split(Regex("[_.-]+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { character ->
                if (character.isLowerCase()) {
                    character.titlecase()
                } else {
                    character.toString()
                }
            }
        }
        .ifBlank { "Imported Model" }
}

private fun ImportedModelRecord.resolveCapabilities(): ModelModalityCapabilities {
    return resolveImportedModelCapabilities(
        manualSupportsImage = manualSupportsImage,
        manualSupportsAudio = manualSupportsAudio,
    )
}

internal fun resolveImportedModelCapabilities(
    manualSupportsImage: Boolean?,
    manualSupportsAudio: Boolean?,
): ModelModalityCapabilities {
    return ModelModalityCapabilities(
        supportsImage = manualSupportsImage ?: false,
        supportsAudio = manualSupportsAudio ?: false,
    )
}

private fun List<ImportedModelRecord>.toJson(): String {
    return JSONArray().apply {
        forEach { model ->
            put(
                JSONObject().apply {
                    put("modelId", model.modelId)
                    put("displayName", model.displayName)
                    put("originalFileName", model.originalFileName)
                    put("filePath", model.filePath)
                    model.runtimeFailureMessage?.let { put("runtimeFailureMessage", it) }
                    model.manualSupportsImage?.let { put("manualSupportsImage", it) }
                    model.manualSupportsAudio?.let { put("manualSupportsAudio", it) }
                },
            )
        }
    }.toString()
}
