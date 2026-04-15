package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.runtime.persona.PersonaRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class MemoryRetrievalService @Inject constructor(
    private val personaRepository: PersonaRepository,
    private val scopedMemoryRepository: ScopedMemoryRepository,
) {
    suspend fun retrieveContext(query: RetrievalQuery): RetrievedContext {
        val persona = personaRepository.getCurrentProfile()
        val eligibleItems = scopedMemoryRepository.queryEligibleItems(query)
        val rankedItems = eligibleItems
            .map { item -> item to score(item, query) }
            .sortedByDescending { (_, score) -> score }
            .map { it.first }
        val selectedItems = rankedItems.take(query.maxItems)
        val hiddenPrivateCount = eligibleItems.count { it.exposurePolicy == MemoryExposurePolicy.PRIVATE }
        return RetrievedContext(
            personaProfile = persona,
            selectedMemoryItems = selectedItems,
            excludedCount = max(eligibleItems.size - selectedItems.size, 0),
            hiddenPrivateCount = hiddenPrivateCount,
            totalEligibleCount = eligibleItems.size,
        )
    }

    private fun score(item: MemoryItem, query: RetrievalQuery): Int {
        val normalizedInput = query.userInput.lowercase()
        val normalizedContent = "${item.title} ${item.contentText} ${item.summaryText}".lowercase()
        val overlapBoost = normalizedInput
            .split(" ")
            .filter { it.length > 2 }
            .count { token -> token in normalizedContent } * 3
        val lifecycleBoost = when (item.lifecycle) {
            MemoryLifecycle.DURABLE -> 4
            MemoryLifecycle.WORKING -> 3
            MemoryLifecycle.EPHEMERAL -> 1
        }
        val scopeBoost = when (item.scope) {
            MemoryScope.APP_SCOPED -> 4
            MemoryScope.CONTACT_SCOPED -> 3
            MemoryScope.GLOBAL -> 2
            MemoryScope.DEVICE_SCOPED -> 1
        }
        val pinBoost = if (item.isPinned) 6 else 0
        val freshnessBoost = if (query.nowEpochMillis - item.updatedAtEpochMillis < 24 * 60 * 60 * 1000L) 2 else 0
        return overlapBoost + lifecycleBoost + scopeBoost + pinBoost + freshnessBoost
    }
}
