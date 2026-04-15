package com.mobileclaw.app.runtime.capability

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.action.PayloadCompletenessState
import com.mobileclaw.app.runtime.action.StructuredExecutionPreview
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolPreviewFactory @Inject constructor(
    private val appStrings: AppStrings,
) {
    fun createPreview(
        descriptor: ToolDescriptor,
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
        structuredPreview: StructuredExecutionPreview? = null,
        visibilitySnapshot: ToolVisibilitySnapshot? = null,
    ): ToolExecutionPreview {
        val previewLines = structuredPreview?.fieldLines ?: fallbackFieldLines(descriptor, request, contextPayload)
        val warnings = buildList {
            addAll(structuredPreview?.warnings.orEmpty())
            visibilitySnapshot?.takeIf {
                it.state == ToolVisibilityState.DEGRADED || it.state == ToolVisibilityState.DENIED
            }?.reason?.takeIf { it.isNotBlank() }?.let(::add)
        }
        return ToolExecutionPreview(
            toolId = descriptor.toolId,
            displayName = descriptor.displayName,
            sideEffectType = descriptor.sideEffectType,
            riskLevelHint = descriptor.riskLevelHint,
            scopeLines = descriptor.requiredScopes.map(appStrings::scopeIdLabel),
            previewFieldLines = previewLines,
            warnings = warnings,
            canExecute = visibilitySnapshot?.state != ToolVisibilityState.DENIED &&
                structuredPreview?.completenessState != PayloadCompletenessState.INSUFFICIENT,
        )
    }

    private fun fallbackFieldLines(
        descriptor: ToolDescriptor,
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
    ): List<String> {
        return when (descriptor.toolId) {
            "generate.reply" -> listOf(
                appStrings.get(R.string.tool_preview_field_request, request.userInput),
            )

            "calendar.read" -> listOf(
                appStrings.get(R.string.tool_preview_field_query, request.userInput),
            )

            "alarm.show" -> listOf(
                appStrings.get(R.string.tool_preview_field_request, appStrings.get(R.string.tool_alarm_show_name)),
            )

            else -> buildList {
                add(appStrings.get(R.string.tool_preview_field_request, request.userInput))
                contextPayload.summary
                    .takeIf { it.isNotBlank() }
                    ?.let { add(appStrings.get(R.string.tool_preview_field_context, it)) }
            }
        }
    }
}
