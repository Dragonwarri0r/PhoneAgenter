package com.mobileclaw.app.runtime.knowledge

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

private data class ScoredKnowledgeChunk(
    val asset: ManagedKnowledgeAsset,
    val chunk: KnowledgeChunkEntity,
    val score: Int,
    val matchedTerms: List<String>,
)

@Singleton
class KnowledgeRetrievalService @Inject constructor(
    private val knowledgeDao: KnowledgeDao,
    private val managedKnowledgeService: ManagedKnowledgeService,
    private val appStrings: AppStrings,
) {

    suspend fun retrieve(query: KnowledgeRetrievalQuery): KnowledgeRetrievalResult {
        val assets = buildList {
            knowledgeDao.getAllAssets().forEach { asset ->
                managedKnowledgeService.getAsset(asset.knowledgeAssetId)?.let(::add)
            }
        }
        if (assets.isEmpty()) {
            return KnowledgeRetrievalResult(
                limitationSummary = appStrings.get(R.string.knowledge_retrieval_no_assets),
            )
        }
        val searchableAssets = assets.filter { asset ->
            asset.retrievalIncluded &&
                asset.indexedChunkCount > 0 &&
                asset.availabilityState in setOf(
                    KnowledgeAvailabilityState.HEALTHY,
                    KnowledgeAvailabilityState.STALE,
                    KnowledgeAvailabilityState.PARTIAL,
                ) &&
                asset.ingestionState != KnowledgeIngestionState.FAILED
        }
        if (searchableAssets.isEmpty()) {
            return KnowledgeRetrievalResult(
                searchableAssetCount = 0,
                excludedAssetCount = countExcludedAssets(assets),
                unavailableAssetCount = countUnavailableAssets(assets),
                limitationSummary = buildLimitationSummary(assets),
            )
        }
        val queryTerms = extractTerms(query.userInput)
        if (queryTerms.isEmpty()) {
            return KnowledgeRetrievalResult(
                searchableAssetCount = searchableAssets.size,
                excludedAssetCount = countExcludedAssets(assets),
                unavailableAssetCount = countUnavailableAssets(assets),
                limitationSummary = combineLimitationSummaries(
                    appStrings.get(R.string.knowledge_retrieval_skipped_no_query),
                    buildLimitationSummary(assets),
                ),
            )
        }
        val chunks = knowledgeDao.getChunksForAssets(searchableAssets.map { it.knowledgeAssetId })
        val assetById = searchableAssets.associateBy { it.knowledgeAssetId }
        val scoredChunks = chunks.mapNotNull { chunk ->
            val asset = assetById[chunk.knowledgeAssetId] ?: return@mapNotNull null
            val chunkTerms = extractTerms(chunk.text)
            val matches = queryTerms.filter { it in chunkTerms }
            val overlapScore = matches.size * 5
            val availabilityPenalty = when (asset.availabilityState) {
                KnowledgeAvailabilityState.STALE -> 2
                KnowledgeAvailabilityState.PARTIAL -> 1
                else -> 0
            }
            val score = overlapScore + freshnessBoost(asset, query.nowEpochMillis) - availabilityPenalty
            if (score <= 0) {
                null
            } else {
                ScoredKnowledgeChunk(
                    asset = asset,
                    chunk = chunk,
                    score = score,
                    matchedTerms = matches.take(3),
                )
            }
        }.sortedByDescending { it.score }
        if (scoredChunks.isEmpty()) {
            return KnowledgeRetrievalResult(
                searchableAssetCount = searchableAssets.size,
                excludedAssetCount = countExcludedAssets(assets),
                unavailableAssetCount = countUnavailableAssets(assets),
                limitationSummary = combineLimitationSummaries(
                    appStrings.get(R.string.knowledge_retrieval_no_match),
                    buildLimitationSummary(assets),
                ),
            )
        }
        val groupedByAsset = scoredChunks
            .groupBy { it.asset.knowledgeAssetId }
            .entries
            .sortedByDescending { entry -> entry.value.maxOf { scored -> scored.score } }
            .take(query.maxAssets)
        val supportSummaries = groupedByAsset.map { entry ->
            val bestChunk = entry.value.first()
            RetrievalSupportSummary(
                requestId = query.requestId,
                knowledgeAssetId = bestChunk.asset.knowledgeAssetId,
                summary = appStrings.get(
                    R.string.knowledge_support_summary,
                    bestChunk.asset.title,
                    bestChunk.chunk.sourceLabel,
                ),
                confidenceLabel = confidenceFor(bestChunk.score),
                provenanceLabel = bestChunk.asset.provenanceLabel,
                isVisibleInline = true,
            )
        }
        val citations = groupedByAsset
            .flatMap { entry -> entry.value.take(2) }
            .take(query.maxCitations)
            .mapIndexed { index, scored ->
                RetrievalCitation(
                    citationId = "${query.requestId}-citation-$index",
                    knowledgeAssetId = scored.asset.knowledgeAssetId,
                    sourceLabel = scored.chunk.sourceLabel,
                    relevanceSummary = if (scored.matchedTerms.isEmpty()) {
                        appStrings.get(R.string.knowledge_citation_relevance_generic)
                    } else {
                        appStrings.get(
                            R.string.knowledge_citation_relevance_terms,
                            scored.matchedTerms.joinToString(separator = ", "),
                        )
                    },
                    redactionState = if (scored.asset.sourceType == KnowledgeSourceType.DOCUMENT_COLLECTION) {
                        KnowledgeRedactionState.SUMMARY_ONLY
                    } else {
                        KnowledgeRedactionState.EXCERPT
                    },
                    requestId = query.requestId,
                    excerpt = if (scored.asset.sourceType == KnowledgeSourceType.DOCUMENT_COLLECTION) {
                        ""
                    } else {
                        scored.chunk.previewText
                    },
                )
            }
        updateRecentUsage(
            supportSummaries = supportSummaries,
            citations = citations,
            nowEpochMillis = query.nowEpochMillis,
        )
        return KnowledgeRetrievalResult(
            supportSummaries = supportSummaries,
            citations = citations,
            searchableAssetCount = searchableAssets.size,
            excludedAssetCount = countExcludedAssets(assets),
            unavailableAssetCount = countUnavailableAssets(assets),
            limitationSummary = buildLimitationSummary(
                assets = assets,
                prioritizedAssets = groupedByAsset.map { entry -> entry.value.first().asset },
            ),
        )
    }

    private suspend fun updateRecentUsage(
        supportSummaries: List<RetrievalSupportSummary>,
        citations: List<RetrievalCitation>,
        nowEpochMillis: Long,
    ) {
        supportSummaries.forEach { summary ->
            val asset = knowledgeDao.getAssetById(summary.knowledgeAssetId) ?: return@forEach
            val citationsForAsset = citations
                .filter { it.knowledgeAssetId == summary.knowledgeAssetId }
                .map { it.sourceLabel }
                .distinct()
            knowledgeDao.upsertAsset(
                asset.copy(
                    lastRetrievedAtEpochMillis = nowEpochMillis,
                    retrievalCount = asset.retrievalCount + 1,
                    lastRetrievalSummary = summary.summary,
                    lastCitationLabels = citationsForAsset,
                    updatedAtEpochMillis = nowEpochMillis,
                ),
            )
        }
    }

    private fun buildLimitationSummary(
        assets: List<ManagedKnowledgeAsset>,
        prioritizedAssets: List<ManagedKnowledgeAsset> = emptyList(),
    ): String {
        return when {
            prioritizedAssets.any { it.availabilityState == KnowledgeAvailabilityState.STALE } ->
                appStrings.get(R.string.knowledge_limitation_stale_generic)
            assets.any { !it.retrievalIncluded } -> appStrings.get(R.string.knowledge_limitation_excluded_generic)
            assets.any { it.availabilityState == KnowledgeAvailabilityState.MISSING } ->
                appStrings.get(R.string.knowledge_limitation_missing_generic)
            assets.any { it.availabilityState == KnowledgeAvailabilityState.PARTIAL } ->
                appStrings.get(R.string.knowledge_limitation_partial_generic)
            assets.any { it.availabilityState == KnowledgeAvailabilityState.STALE } ->
                appStrings.get(R.string.knowledge_limitation_stale_generic)
            else -> ""
        }
    }

    private fun countExcludedAssets(assets: List<ManagedKnowledgeAsset>): Int {
        return assets.count { !it.retrievalIncluded }
    }

    private fun countUnavailableAssets(assets: List<ManagedKnowledgeAsset>): Int {
        return assets.count {
            it.availabilityState in setOf(
                KnowledgeAvailabilityState.MISSING,
                KnowledgeAvailabilityState.PARTIAL,
                KnowledgeAvailabilityState.STALE,
            ) || it.ingestionState == KnowledgeIngestionState.FAILED
        }
    }

    private fun combineLimitationSummaries(
        primary: String,
        secondary: String,
    ): String {
        return listOf(primary, secondary)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = " ")
    }

    private fun freshnessBoost(
        asset: ManagedKnowledgeAsset,
        nowEpochMillis: Long,
    ): Int {
        val freshness = asset.lastKnownFreshnessEpochMillis ?: return 0
        return when {
            nowEpochMillis - freshness <= 24L * 60L * 60L * 1000L -> 3
            nowEpochMillis - freshness <= 7L * 24L * 60L * 60L * 1000L -> 1
            else -> 0
        }
    }

    private fun confidenceFor(score: Int): KnowledgeConfidenceLabel {
        return when {
            score >= 12 -> KnowledgeConfidenceLabel.HIGH
            score >= 7 -> KnowledgeConfidenceLabel.MEDIUM
            else -> KnowledgeConfidenceLabel.LOW
        }
    }

    private fun extractTerms(text: String): Set<String> {
        val normalized = text.lowercase()
        val latinTerms = Regex("[\\p{L}\\p{N}_]{2,}")
            .findAll(normalized)
            .map { it.value }
            .toMutableSet()
        val hanCharacters = normalized.filter { char ->
            Character.UnicodeScript.of(char.code) == Character.UnicodeScript.HAN
        }
        hanCharacters.forEach { latinTerms += it.toString() }
        if (hanCharacters.length >= 2) {
            hanCharacters.windowed(size = 2, step = 1, partialWindows = false).forEach(latinTerms::add)
        }
        return latinTerms
    }
}
