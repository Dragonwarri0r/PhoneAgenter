package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.runtime.persona.PersonaProfile
import com.mobileclaw.app.runtime.strings.AppStrings

data class RetrievalQuery(
    val requestId: String,
    val userInput: String,
    val originApp: String,
    val subjectKey: String?,
    val deviceId: String?,
    val allowPrivate: Boolean,
    val maxItems: Int,
    val nowEpochMillis: Long = System.currentTimeMillis(),
)

data class RetrievedContext(
    val personaProfile: PersonaProfile,
    val selectedMemoryItems: List<MemoryItem>,
    val excludedCount: Int,
    val hiddenPrivateCount: Int,
    val totalEligibleCount: Int,
)

data class MemoryChipSummary(
    val memoryId: String,
    val label: String,
    val lifecycle: MemoryLifecycle,
    val scope: MemoryScope,
    val isPinned: Boolean,
    val sourceType: MemorySourceType,
)

data class ActiveContextSummary(
    val headline: String,
    val personaSummary: String,
    val memoryChips: List<MemoryChipSummary>,
    val hiddenPrivateCount: Int,
    val totalEligibleCount: Int,
    val excludedCount: Int,
    val retrievalSummary: String,
)

fun RetrievedContext.toActiveContextSummary(strings: AppStrings): ActiveContextSummary {
    val visibleMemory = selectedMemoryItems.map { item ->
        MemoryChipSummary(
            memoryId = item.memoryId,
            label = item.title,
            lifecycle = item.lifecycle,
            scope = item.scope,
            isPinned = item.isPinned,
            sourceType = item.sourceType,
        )
    }
    val visibleCount = visibleMemory.size
    return ActiveContextSummary(
        headline = strings.activeContextHeadline(visibleCount),
        personaSummary = personaProfile.summaryText(strings),
        memoryChips = visibleMemory,
        hiddenPrivateCount = hiddenPrivateCount,
        totalEligibleCount = totalEligibleCount,
        excludedCount = excludedCount,
        retrievalSummary = strings.activeContextRetrievalSummary(hiddenPrivateCount, excludedCount),
    )
}
