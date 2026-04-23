package com.mobileclaw.app.runtime.provider

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.memory.MemoryChipSummary
import com.mobileclaw.app.runtime.knowledge.KnowledgeConfidenceLabel
import com.mobileclaw.app.runtime.knowledge.KnowledgeRedactionState
import com.mobileclaw.app.runtime.session.CapabilityResolutionMode
import com.mobileclaw.app.runtime.session.CapabilitySelectionOutcome
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalGenerationPromptComposer @Inject constructor(
    private val appStrings: AppStrings,
) {

    fun compose(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
        selectionOutcome: CapabilitySelectionOutcome? = null,
    ): String {
        return buildString {
            appendLine(appStrings.get(R.string.prompt_system_intro))
            appendLine(appStrings.get(R.string.prompt_system_style))

            if (contextPayload.personaSummary.isNotBlank()) {
                appendLine()
                appendLine("[Persona]")
                appendLine(contextPayload.personaSummary)
            }

            val activeMemory = contextPayload.activeContextSummary?.memoryChips.orEmpty()
            if (activeMemory.isNotEmpty()) {
                appendLine()
                appendLine("[Relevant Memory]")
                activeMemory.forEach { chip ->
                    appendLine(formatMemoryChip(chip))
                }
            }

            val knowledgeContribution = contextPayload.knowledgeContribution
            if (knowledgeContribution != null &&
                (knowledgeContribution.supportSummaries.isNotEmpty() || knowledgeContribution.limitationSummary.isNotBlank())
            ) {
                appendLine()
                appendLine("[Knowledge Support]")
                knowledgeContribution.supportSummaries.forEach { support ->
                    appendLine(
                        "- ${support.summary} [${
                            when (support.confidenceLabel) {
                                KnowledgeConfidenceLabel.HIGH -> "high"
                                KnowledgeConfidenceLabel.MEDIUM -> "medium"
                                KnowledgeConfidenceLabel.LOW -> "low"
                            }
                        }]",
                    )
                    knowledgeContribution.citations
                        .filter { it.knowledgeAssetId == support.knowledgeAssetId }
                        .take(2)
                        .forEach { citation ->
                            val redactionLabel = when (citation.redactionState) {
                                KnowledgeRedactionState.EXCERPT -> "excerpt"
                                KnowledgeRedactionState.SUMMARY_ONLY -> "summary"
                            }
                            val citationBody = when (citation.redactionState) {
                                KnowledgeRedactionState.EXCERPT ->
                                    citation.excerpt.ifBlank { citation.relevanceSummary }
                                KnowledgeRedactionState.SUMMARY_ONLY -> citation.relevanceSummary
                            }
                            appendLine("  - ${citation.sourceLabel} [$redactionLabel]: $citationBody")
                        }
                }
                knowledgeContribution.limitationSummary
                    .takeIf { it.isNotBlank() }
                    ?.let { limitation ->
                        appendLine("- Note: $limitation")
                    }
            }

            val systemDescriptors = contextPayload.systemSourceDescriptors
            if (systemDescriptors.isNotEmpty()) {
                appendLine()
                appendLine("[Device Access]")
                systemDescriptors.forEach { descriptor ->
                    appendLine("- ${descriptor.displayName}: ${descriptor.availabilitySummary}")
                }
                contextPayload.systemSourceContributions
                    .takeIf { it.isNotEmpty() }
                    ?.let { contributions ->
                        appendLine()
                        appendLine("[Attached Device Context]")
                        contributions.forEach { contribution ->
                            appendLine("- ${contribution.displayName}: ${contribution.summary}")
                        }
                }
            }

            selectionOutcome?.let { outcome ->
                appendLine()
                appendLine("[Capability Path]")
                appendLine("- ${outcome.explanation}")
                outcome.warnings.forEach { warning ->
                    appendLine("- $warning")
                }
                if (outcome.resolutionMode == CapabilityResolutionMode.REPLY_FALLBACK) {
                    appendLine(appStrings.get(R.string.prompt_capability_path_reply_guidance))
                }
            }

            if (request.transcriptContext.isNotEmpty()) {
                appendLine()
                appendLine("[Recent Transcript]")
                request.transcriptContext.takeLast(4).forEach { entry ->
                    appendLine("${entry.role.name.lowercase()}: ${entry.content}")
                }
            }

            appendLine()
            appendLine("[Latest User Request]")
            appendLine(
                request.userInput.ifBlank {
                    appStrings.get(R.string.multimodal_prompt_request_from_attachments)
                },
            )
            if (request.attachments.isNotEmpty()) {
                appendLine()
                appendLine("[Attachments]")
                request.attachments.forEach { attachment ->
                    appendLine("- ${attachment.kind.name.lowercase()}: ${attachment.displayName}")
                }
            }
            appendLine()
            append(appStrings.get(R.string.prompt_final_instruction))
        }.trim()
    }

    private fun formatMemoryChip(chip: MemoryChipSummary): String {
        val badges = buildList {
            add(appStrings.memoryLifecycleLabel(chip.lifecycle))
            add(appStrings.memoryScopeLabel(chip.scope))
            add(appStrings.memorySourceLabel(chip.sourceType))
            if (chip.isPinned) add(appStrings.get(R.string.prompt_badge_pinned))
        }.joinToString(", ")
        return "- ${chip.label} [$badges]"
    }
}
