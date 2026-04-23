package com.mobileclaw.app.runtime.contribution

import com.mobileclaw.app.runtime.knowledge.RetrievalCitation
import com.mobileclaw.app.runtime.knowledge.RetrievalSupportSummary

data class KnowledgeRequestContribution(
    val requestId: String,
    val supportSummaries: List<RetrievalSupportSummary> = emptyList(),
    val citations: List<RetrievalCitation> = emptyList(),
    val limitationSummary: String = "",
)
