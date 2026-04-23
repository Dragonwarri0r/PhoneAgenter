package com.mobileclaw.app.runtime.knowledge

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val STALE_AFTER_MILLIS = 7L * 24L * 60L * 60L * 1000L

private data class ReadableKnowledgeSource(
    val uriString: String,
    val displayName: String,
    val text: String,
    val errorSummary: String = "",
)

@Singleton
class ManagedKnowledgeService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val knowledgeDao: KnowledgeDao,
    private val appStrings: AppStrings,
) {

    fun observeCorpus(): Flow<KnowledgeCorpusSnapshot> {
        return combine(
            knowledgeDao.observeAssets(),
            knowledgeDao.observeIngestionRecords(),
            knowledgeDao.observeAvailabilityRecords(),
        ) { assets, ingestions, availability ->
            val now = System.currentTimeMillis()
            val ingestionById = ingestions.associateBy { it.knowledgeAssetId }
            val availabilityById = availability.associateBy { it.knowledgeAssetId }
            val mappedAssets = assets.map { asset ->
                toManagedAsset(
                    asset = asset,
                    ingestion = ingestionById[asset.knowledgeAssetId],
                    availability = availabilityById[asset.knowledgeAssetId],
                    nowEpochMillis = now,
                )
            }
            KnowledgeCorpusSnapshot(
                assets = mappedAssets,
                totalAssetCount = mappedAssets.size,
                includedAssetCount = mappedAssets.count { it.retrievalIncluded },
                degradedAssetCount = mappedAssets.count {
                    it.availabilityState in setOf(
                        KnowledgeAvailabilityState.STALE,
                        KnowledgeAvailabilityState.PARTIAL,
                        KnowledgeAvailabilityState.MISSING,
                    )
                },
            )
        }
    }

    suspend fun ingestDocuments(sourceUris: List<Uri>): Result<ManagedKnowledgeAsset> = runCatching {
        val normalizedUris = sourceUris.distinctBy(Uri::toString)
        require(normalizedUris.isNotEmpty()) {
            appStrings.get(R.string.knowledge_feedback_ingest_failed_no_source)
        }
        val existingAssetId = findExistingAssetId(normalizedUris.map(Uri::toString).toSet())
        upsertKnowledgeAsset(
            sourceUris = normalizedUris,
            existingAssetId = existingAssetId,
        )
    }

    suspend fun refreshAsset(knowledgeAssetId: String): Result<ManagedKnowledgeAsset> = runCatching {
        val asset = knowledgeDao.getAssetById(knowledgeAssetId)
            ?: error(appStrings.get(R.string.knowledge_feedback_asset_missing))
        upsertKnowledgeAsset(
            sourceUris = asset.sourceUris.map(Uri::parse),
            existingAssetId = asset.knowledgeAssetId,
        )
    }

    suspend fun setRetrievalIncluded(
        knowledgeAssetId: String,
        included: Boolean,
    ): Result<ManagedKnowledgeAsset> = runCatching {
        val asset = knowledgeDao.getAssetById(knowledgeAssetId)
            ?: error(appStrings.get(R.string.knowledge_feedback_asset_missing))
        val availability = knowledgeDao.getAvailabilityById(knowledgeAssetId)
            ?: error(appStrings.get(R.string.knowledge_feedback_asset_missing))
        val updatedAvailability = availability.copy(
            retrievalIncluded = included,
            lastCheckedAtEpochMillis = System.currentTimeMillis(),
        )
        knowledgeDao.upsertAvailabilityRecord(updatedAvailability)
        toManagedAsset(
            asset = asset,
            ingestion = knowledgeDao.getIngestionRecordById(knowledgeAssetId),
            availability = updatedAvailability,
            nowEpochMillis = System.currentTimeMillis(),
        )
    }

    suspend fun getAsset(knowledgeAssetId: String): ManagedKnowledgeAsset? {
        val asset = knowledgeDao.getAssetById(knowledgeAssetId) ?: return null
        return toManagedAsset(
            asset = asset,
            ingestion = knowledgeDao.getIngestionRecordById(knowledgeAssetId),
            availability = knowledgeDao.getAvailabilityById(knowledgeAssetId),
            nowEpochMillis = System.currentTimeMillis(),
        )
    }

    private suspend fun findExistingAssetId(uriSet: Set<String>): String? {
        return knowledgeDao.getAllAssets().firstOrNull { asset ->
            asset.sourceUris.toSet() == uriSet
        }?.knowledgeAssetId
    }

    private suspend fun upsertKnowledgeAsset(
        sourceUris: List<Uri>,
        existingAssetId: String?,
    ): ManagedKnowledgeAsset {
        val now = System.currentTimeMillis()
        val existingAsset = if (existingAssetId != null) {
            knowledgeDao.getAssetById(existingAssetId)
        } else {
            null
        }
        val existingAvailability = if (existingAssetId != null) {
            knowledgeDao.getAvailabilityById(existingAssetId)
        } else {
            null
        }
        val readableSources = sourceUris.map(::readSource)
        val indexedSources = readableSources.filter { it.text.isNotBlank() }
        val assetId = existingAsset?.knowledgeAssetId ?: "knowledge-$now-${sourceUris.size}"
        val chunks = buildChunks(
            knowledgeAssetId = assetId,
            sources = indexedSources,
        )
        val ingestionState = when {
            indexedSources.isEmpty() -> KnowledgeIngestionState.FAILED
            indexedSources.size < readableSources.size -> KnowledgeIngestionState.PARTIAL
            else -> KnowledgeIngestionState.READY
        }
        val errorSummary = readableSources
            .mapNotNull { it.errorSummary.takeIf { summary -> summary.isNotBlank() } }
            .joinToString(separator = " / ")
        val sourceType = if (sourceUris.size > 1) {
            KnowledgeSourceType.DOCUMENT_COLLECTION
        } else {
            KnowledgeSourceType.FILE
        }
        val asset = KnowledgeAssetEntity(
            knowledgeAssetId = assetId,
            title = buildAssetTitle(readableSources, sourceType),
            sourceType = sourceType,
            provenanceLabel = buildProvenanceLabel(sourceType, readableSources.size),
            sourceUris = readableSources.map { it.uriString },
            sourceLabels = readableSources.map { it.displayName },
            documentCount = readableSources.size,
            indexedDocumentCount = indexedSources.size,
            indexedChunkCount = chunks.size,
            lastKnownFreshnessEpochMillis = now,
            lastRetrievedAtEpochMillis = existingAsset?.lastRetrievedAtEpochMillis,
            retrievalCount = existingAsset?.retrievalCount ?: 0,
            lastRetrievalSummary = existingAsset?.lastRetrievalSummary.orEmpty(),
            lastCitationLabels = existingAsset?.lastCitationLabels.orEmpty(),
            createdAtEpochMillis = existingAsset?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
        )
        val ingestionRecord = KnowledgeIngestionRecordEntity(
            knowledgeAssetId = assetId,
            ingestionState = ingestionState,
            ingestionSummary = buildIngestionSummary(
                indexedDocumentCount = indexedSources.size,
                totalDocumentCount = readableSources.size,
                chunkCount = chunks.size,
            ),
            lastIngestedAtEpochMillis = now,
            lastErrorSummary = errorSummary,
            currentScopeSummary = appStrings.get(
                R.string.knowledge_scope_summary,
                indexedSources.size,
                readableSources.size,
            ),
        )
        val availabilityRecord = KnowledgeAvailabilityEntity(
            knowledgeAssetId = assetId,
            baseState = when {
                indexedSources.isEmpty() -> KnowledgeAvailabilityHealth.MISSING
                indexedSources.size < readableSources.size -> KnowledgeAvailabilityHealth.PARTIAL
                else -> KnowledgeAvailabilityHealth.HEALTHY
            },
            retrievalIncluded = existingAvailability?.retrievalIncluded ?: true,
            supportsRefresh = true,
            supportsRetrievalInclusionChange = true,
            reasonIfUnavailable = when {
                indexedSources.isEmpty() -> errorSummary.ifBlank {
                    appStrings.get(R.string.knowledge_limitation_missing_generic)
                }
                indexedSources.size < readableSources.size -> errorSummary.ifBlank {
                    appStrings.get(R.string.knowledge_limitation_partial_generic)
                }
                else -> ""
            },
            lastCheckedAtEpochMillis = now,
        )
        knowledgeDao.upsertAsset(asset)
        knowledgeDao.upsertIngestionRecord(ingestionRecord)
        knowledgeDao.upsertAvailabilityRecord(availabilityRecord)
        knowledgeDao.deleteChunksForAsset(assetId)
        if (chunks.isNotEmpty()) {
            knowledgeDao.upsertChunks(chunks)
        }
        return toManagedAsset(
            asset = asset,
            ingestion = ingestionRecord,
            availability = availabilityRecord,
            nowEpochMillis = now,
        )
    }

    private fun buildAssetTitle(
        readableSources: List<ReadableKnowledgeSource>,
        sourceType: KnowledgeSourceType,
    ): String {
        val firstLabel = readableSources.firstOrNull()?.displayName
            ?: appStrings.get(R.string.knowledge_asset_untitled)
        return when (sourceType) {
            KnowledgeSourceType.FILE -> firstLabel
            KnowledgeSourceType.DOCUMENT_COLLECTION -> appStrings.get(
                R.string.knowledge_asset_collection_title,
                firstLabel,
                readableSources.size,
            )
        }
    }

    private fun buildProvenanceLabel(
        sourceType: KnowledgeSourceType,
        sourceCount: Int,
    ): String {
        return when (sourceType) {
            KnowledgeSourceType.FILE -> appStrings.get(R.string.knowledge_provenance_file)
            KnowledgeSourceType.DOCUMENT_COLLECTION -> appStrings.get(
                R.string.knowledge_provenance_collection,
                sourceCount,
            )
        }
    }

    private fun buildIngestionSummary(
        indexedDocumentCount: Int,
        totalDocumentCount: Int,
        chunkCount: Int,
    ): String {
        return when {
            indexedDocumentCount == 0 -> appStrings.get(R.string.knowledge_ingestion_summary_failed)
            indexedDocumentCount < totalDocumentCount -> appStrings.get(
                R.string.knowledge_ingestion_summary_partial,
                indexedDocumentCount,
                totalDocumentCount,
                chunkCount,
            )
            else -> appStrings.get(
                R.string.knowledge_ingestion_summary_ready,
                indexedDocumentCount,
                chunkCount,
            )
        }
    }

    private fun buildChunks(
        knowledgeAssetId: String,
        sources: List<ReadableKnowledgeSource>,
    ): List<KnowledgeChunkEntity> {
        var ordinal = 0
        return sources.flatMap { source ->
            val paragraphs = source.text
                .split(Regex("\\n\\s*\\n"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .ifEmpty { listOf(source.text.trim()) }
            paragraphs.flatMap { paragraph ->
                windowText(paragraph).map { chunkText ->
                    ordinal += 1
                    KnowledgeChunkEntity(
                        chunkId = "$knowledgeAssetId-$ordinal",
                        knowledgeAssetId = knowledgeAssetId,
                        sourceLabel = source.displayName,
                        ordinal = ordinal,
                        text = chunkText,
                        previewText = chunkText.replace('\n', ' ').take(180),
                    )
                }
            }
        }
    }

    private fun windowText(text: String): List<String> {
        if (text.length <= 720) return listOf(text)
        val windows = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + 720, text.length)
            windows += text.substring(start, end).trim()
            if (end >= text.length) break
            start = maxOf(end - 120, start + 1)
        }
        return windows.filter { it.isNotBlank() }
    }

    private fun readSource(sourceUri: Uri): ReadableKnowledgeSource {
        persistReadPermission(sourceUri)
        val displayName = resolveDisplayName(sourceUri).ifBlank {
            sourceUri.lastPathSegment.orEmpty().substringAfterLast('/')
        }.ifBlank {
            appStrings.get(R.string.knowledge_asset_untitled)
        }
        if (!isSupportedTextSource(sourceUri, displayName)) {
            return ReadableKnowledgeSource(
                uriString = sourceUri.toString(),
                displayName = displayName,
                text = "",
                errorSummary = appStrings.get(R.string.knowledge_ingestion_error_unsupported, displayName),
            )
        }
        return runCatching {
            val input = context.contentResolver.openInputStream(sourceUri)
                ?: throw IOException(appStrings.get(R.string.knowledge_ingestion_error_open, displayName))
            val content = input.bufferedReader().use { reader -> reader.readText() }
                .replace("\r\n", "\n")
                .trim()
            if (content.isBlank()) {
                ReadableKnowledgeSource(
                    uriString = sourceUri.toString(),
                    displayName = displayName,
                    text = "",
                    errorSummary = appStrings.get(R.string.knowledge_ingestion_error_empty, displayName),
                )
            } else {
                ReadableKnowledgeSource(
                    uriString = sourceUri.toString(),
                    displayName = displayName,
                    text = content.take(80_000),
                )
            }
        }.getOrElse {
            ReadableKnowledgeSource(
                uriString = sourceUri.toString(),
                displayName = displayName,
                text = "",
                errorSummary = it.message ?: appStrings.get(R.string.knowledge_ingestion_error_open, displayName),
            )
        }
    }

    private fun persistReadPermission(sourceUri: Uri) {
        if (sourceUri.scheme != ContentResolver.SCHEME_CONTENT) return
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                sourceUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    private fun resolveDisplayName(sourceUri: Uri): String {
        if (sourceUri.scheme != ContentResolver.SCHEME_CONTENT) {
            return sourceUri.lastPathSegment.orEmpty().substringAfterLast('/')
        }
        context.contentResolver.query(
            sourceUri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columnIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(columnIndex).orEmpty()
            }
        }
        return sourceUri.lastPathSegment.orEmpty().substringAfterLast('/')
    }

    private fun isSupportedTextSource(sourceUri: Uri, displayName: String): Boolean {
        val mimeType = context.contentResolver.getType(sourceUri).orEmpty()
        val extension = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return mimeType.startsWith("text/") ||
            mimeType in setOf("application/json", "application/xml") ||
            extension in setOf("txt", "md", "markdown", "json", "csv", "log", "xml", "html", "htm")
    }

    private fun toManagedAsset(
        asset: KnowledgeAssetEntity,
        ingestion: KnowledgeIngestionRecordEntity?,
        availability: KnowledgeAvailabilityEntity?,
        nowEpochMillis: Long,
    ): ManagedKnowledgeAsset {
        val availabilityState = deriveAvailabilityState(
            availability = availability,
            freshnessEpochMillis = asset.lastKnownFreshnessEpochMillis,
            nowEpochMillis = nowEpochMillis,
        )
        return ManagedKnowledgeAsset(
            knowledgeAssetId = asset.knowledgeAssetId,
            title = asset.title,
            sourceType = asset.sourceType,
            provenanceLabel = asset.provenanceLabel,
            sourceUris = asset.sourceUris,
            sourceLabels = asset.sourceLabels,
            documentCount = asset.documentCount,
            indexedDocumentCount = asset.indexedDocumentCount,
            indexedChunkCount = asset.indexedChunkCount,
            ingestionState = ingestion?.ingestionState ?: KnowledgeIngestionState.PENDING,
            ingestionSummary = ingestion?.ingestionSummary.orEmpty(),
            lastIngestedAtEpochMillis = ingestion?.lastIngestedAtEpochMillis,
            lastErrorSummary = ingestion?.lastErrorSummary.orEmpty(),
            currentScopeSummary = ingestion?.currentScopeSummary.orEmpty(),
            availabilityState = availabilityState,
            retrievalIncluded = availability?.retrievalIncluded ?: true,
            supportsRefresh = availability?.supportsRefresh ?: true,
            supportsRetrievalInclusionChange = availability?.supportsRetrievalInclusionChange ?: true,
            reasonIfUnavailable = availability?.reasonIfUnavailable.orEmpty(),
            lastKnownFreshnessEpochMillis = asset.lastKnownFreshnessEpochMillis,
            lastRetrievedAtEpochMillis = asset.lastRetrievedAtEpochMillis,
            retrievalCount = asset.retrievalCount,
            lastRetrievalSummary = asset.lastRetrievalSummary,
            lastCitationLabels = asset.lastCitationLabels,
            updatedAtEpochMillis = asset.updatedAtEpochMillis,
        )
    }

    private fun deriveAvailabilityState(
        availability: KnowledgeAvailabilityEntity?,
        freshnessEpochMillis: Long?,
        nowEpochMillis: Long,
    ): KnowledgeAvailabilityState {
        if (availability?.retrievalIncluded == false) {
            return KnowledgeAvailabilityState.EXCLUDED
        }
        return when (availability?.baseState ?: KnowledgeAvailabilityHealth.HEALTHY) {
            KnowledgeAvailabilityHealth.PARTIAL -> KnowledgeAvailabilityState.PARTIAL
            KnowledgeAvailabilityHealth.MISSING -> KnowledgeAvailabilityState.MISSING
            KnowledgeAvailabilityHealth.HEALTHY -> {
                if (freshnessEpochMillis != null && nowEpochMillis - freshnessEpochMillis > STALE_AFTER_MILLIS) {
                    KnowledgeAvailabilityState.STALE
                } else {
                    KnowledgeAvailabilityState.HEALTHY
                }
            }
        }
    }
}
