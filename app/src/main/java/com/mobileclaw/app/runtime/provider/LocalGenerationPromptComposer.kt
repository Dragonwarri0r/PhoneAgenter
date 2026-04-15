package com.mobileclaw.app.runtime.provider

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.memory.MemoryChipSummary
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
