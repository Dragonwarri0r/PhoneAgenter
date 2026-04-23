package com.mobileclaw.app.runtime.knowledge

enum class KnowledgeSourceType {
    FILE,
    DOCUMENT_COLLECTION,
}

enum class KnowledgeIngestionState {
    PENDING,
    INGESTING,
    READY,
    PARTIAL,
    FAILED,
}

enum class KnowledgeAvailabilityHealth {
    HEALTHY,
    PARTIAL,
    MISSING,
}

enum class KnowledgeAvailabilityState {
    HEALTHY,
    STALE,
    PARTIAL,
    EXCLUDED,
    MISSING,
}

enum class KnowledgeConfidenceLabel {
    HIGH,
    MEDIUM,
    LOW,
}

enum class KnowledgeRedactionState {
    EXCERPT,
    SUMMARY_ONLY,
}

data class RetrievalCitation(
    val citationId: String,
    val knowledgeAssetId: String,
    val sourceLabel: String,
    val relevanceSummary: String,
    val redactionState: KnowledgeRedactionState,
    val requestId: String,
    val excerpt: String = "",
)

data class RetrievalSupportSummary(
    val requestId: String,
    val knowledgeAssetId: String,
    val summary: String,
    val confidenceLabel: KnowledgeConfidenceLabel,
    val provenanceLabel: String,
    val isVisibleInline: Boolean,
)

data class KnowledgeRetrievalQuery(
    val requestId: String,
    val userInput: String,
    val maxAssets: Int = 3,
    val maxCitations: Int = 4,
    val nowEpochMillis: Long = System.currentTimeMillis(),
)

data class KnowledgeRetrievalResult(
    val supportSummaries: List<RetrievalSupportSummary> = emptyList(),
    val citations: List<RetrievalCitation> = emptyList(),
    val searchableAssetCount: Int = 0,
    val excludedAssetCount: Int = 0,
    val unavailableAssetCount: Int = 0,
    val limitationSummary: String = "",
)

data class ManagedKnowledgeAsset(
    val knowledgeAssetId: String,
    val title: String,
    val sourceType: KnowledgeSourceType,
    val provenanceLabel: String,
    val sourceUris: List<String>,
    val sourceLabels: List<String>,
    val documentCount: Int,
    val indexedDocumentCount: Int,
    val indexedChunkCount: Int,
    val ingestionState: KnowledgeIngestionState,
    val ingestionSummary: String,
    val lastIngestedAtEpochMillis: Long?,
    val lastErrorSummary: String,
    val currentScopeSummary: String,
    val availabilityState: KnowledgeAvailabilityState,
    val retrievalIncluded: Boolean,
    val supportsRefresh: Boolean,
    val supportsRetrievalInclusionChange: Boolean,
    val reasonIfUnavailable: String,
    val lastKnownFreshnessEpochMillis: Long?,
    val lastRetrievedAtEpochMillis: Long?,
    val retrievalCount: Int,
    val lastRetrievalSummary: String,
    val lastCitationLabels: List<String>,
    val updatedAtEpochMillis: Long,
)

data class KnowledgeCorpusSnapshot(
    val assets: List<ManagedKnowledgeAsset> = emptyList(),
    val totalAssetCount: Int = 0,
    val includedAssetCount: Int = 0,
    val degradedAssetCount: Int = 0,
)
