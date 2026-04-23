package com.mobileclaw.app.ui.agentworkspace.model

data class KnowledgeRequestSupportUiModel(
    val summaryLines: List<String> = emptyList(),
    val citationLines: List<String> = emptyList(),
    val limitationSummary: String = "",
)

data class ManagedKnowledgeEntryUiModel(
    val knowledgeAssetId: String,
    val title: String,
    val statusLine: String = "",
    val freshnessLine: String = "",
    val usageSummary: String = "",
    val provenanceLabel: String = "",
    val detailLines: List<String> = emptyList(),
    val citationLines: List<String> = emptyList(),
    val limitationSummary: String = "",
    val canRefresh: Boolean = false,
    val canToggleRetrievalInclusion: Boolean = false,
    val retrievalIncluded: Boolean = true,
)

data class KnowledgeAreaUiModel(
    val title: String = "",
    val headline: String = "",
    val supportingText: String = "",
    val emptyState: String = "",
    val entries: List<ManagedKnowledgeEntryUiModel> = emptyList(),
)
