package com.mobileclaw.app.runtime.action

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StructuredActionNormalizer @Inject constructor(
    private val appStrings: AppStrings,
) {
    fun normalize(
        request: RuntimeRequest,
        plan: RuntimePlan,
        contextPayload: RuntimeContextPayload,
    ): ActionNormalizationResult {
        val actionType = StructuredActionType.fromCapabilityId(plan.selectedCapabilityId)
            ?: return ActionNormalizationResult(
                applies = false,
                rationale = appStrings.get(R.string.structured_action_not_applicable),
            )

        return when (actionType) {
            StructuredActionType.MESSAGE_SEND -> normalizeMessage(request)
            StructuredActionType.CALENDAR_WRITE -> normalizeCalendar(request, contextPayload)
            StructuredActionType.CALENDAR_DELETE -> normalizeCalendarDelete(request)
            StructuredActionType.EXTERNAL_SHARE -> normalizeShare(request)
        }
    }

    private fun normalizeMessage(
        request: RuntimeRequest,
    ): ActionNormalizationResult {
        val raw = request.userInput.trim()
        val recipient = extractRecipientHint(raw)
        val body = extractMessageBody(raw).ifBlank { raw }
        val fields = buildMap {
            if (recipient.isNotBlank()) put("recipientHint", recipient)
            if (body.isNotBlank()) put("body", body)
        }
        val completeness = when {
            recipient.isNotBlank() && body.isNotBlank() -> PayloadCompletenessState.COMPLETE
            body.isNotBlank() -> PayloadCompletenessState.PARTIAL
            else -> PayloadCompletenessState.INSUFFICIENT
        }
        val warnings = buildList {
            if (recipient.isBlank()) add(appStrings.get(R.string.structured_warning_missing_recipient))
            if (body.isBlank()) add(appStrings.get(R.string.structured_warning_missing_content))
        }
        val payload = StructuredActionPayload(
            actionType = StructuredActionType.MESSAGE_SEND,
            completenessState = completeness,
            fields = fields,
            evidence = listOfNotNull(
                recipient.takeIf { it.isNotBlank() }?.let {
                    PayloadFieldEvidence("recipientHint", it, 0.62)
                },
                body.takeIf { it.isNotBlank() }?.let {
                    PayloadFieldEvidence("body", it.take(120), 0.78)
                },
            ),
            warnings = warnings,
        )
        return ActionNormalizationResult(
            applies = true,
            payload = payload,
            preview = StructuredExecutionPreview(
                title = appStrings.structuredActionTypeLabel(StructuredActionType.MESSAGE_SEND),
                summary = appStrings.get(
                    R.string.structured_summary_message,
                    recipient.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                ),
                fieldLines = buildList {
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_recipient),
                            recipient.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_content),
                            body.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                },
                warnings = warnings,
                completenessState = completeness,
            ),
            rationale = appStrings.get(R.string.structured_rationale_message),
        )
    }

    private fun normalizeCalendar(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
    ): ActionNormalizationResult {
        val raw = request.userInput.trim()
        val parsed = CalendarCapabilityParser.parseCreate(raw)
        val title = parsed.title
        val timeHint = parsed.timeLabel
        val description = parsed.description
        val fields = buildMap {
            if (title.isNotBlank()) put("title", title)
            if (timeHint.isNotBlank()) put("timeHint", timeHint)
            if (description.isNotBlank()) put("description", description)
            parsed.startEpochMillis?.let { put("startEpochMillis", it.toString()) }
            parsed.endEpochMillis?.let { put("endEpochMillis", it.toString()) }
            put("allDay", parsed.allDay.toString())
            if (contextPayload.personaSummary.isNotBlank()) put("personaSummary", contextPayload.personaSummary)
        }
        val completeness = when {
            title.isNotBlank() && parsed.startEpochMillis != null && parsed.endEpochMillis != null -> {
                PayloadCompletenessState.COMPLETE
            }

            title.isNotBlank() || timeHint.isNotBlank() -> PayloadCompletenessState.PARTIAL
            else -> PayloadCompletenessState.INSUFFICIENT
        }
        val warnings = buildList {
            if (timeHint.isBlank()) add(appStrings.get(R.string.structured_warning_missing_time))
            if (title.isBlank()) add(appStrings.get(R.string.structured_warning_missing_title))
        }
        val payload = StructuredActionPayload(
            actionType = StructuredActionType.CALENDAR_WRITE,
            completenessState = completeness,
            fields = fields,
            evidence = listOfNotNull(
                title.takeIf { it.isNotBlank() }?.let { PayloadFieldEvidence("title", it, 0.65) },
                timeHint.takeIf { it.isNotBlank() }?.let { PayloadFieldEvidence("timeHint", it, 0.58) },
            ),
            warnings = warnings,
        )
        return ActionNormalizationResult(
            applies = true,
            payload = payload,
            preview = StructuredExecutionPreview(
                title = appStrings.structuredActionTypeLabel(StructuredActionType.CALENDAR_WRITE),
                summary = appStrings.get(
                    R.string.structured_summary_calendar,
                    title.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                ),
                fieldLines = buildList {
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_title),
                            title.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_time),
                            timeHint.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_description),
                            description.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                },
                warnings = warnings,
                completenessState = completeness,
            ),
            rationale = appStrings.get(R.string.structured_rationale_calendar),
        )
    }

    private fun normalizeCalendarDelete(
        request: RuntimeRequest,
    ): ActionNormalizationResult {
        val raw = request.userInput.trim()
        val parsed = CalendarCapabilityParser.parseDelete(raw)
        val title = parsed.title
        val timeHint = parsed.windowLabel
        val fields = buildMap {
            if (title.isNotBlank()) put("title", title)
            if (timeHint.isNotBlank()) put("timeHint", timeHint)
            parsed.queryStartEpochMillis?.let { put("queryStartEpochMillis", it.toString()) }
            parsed.queryEndEpochMillis?.let { put("queryEndEpochMillis", it.toString()) }
        }
        val completeness = when {
            title.isNotBlank() &&
                parsed.queryStartEpochMillis != null &&
                parsed.queryEndEpochMillis != null -> PayloadCompletenessState.COMPLETE

            title.isNotBlank() || timeHint.isNotBlank() -> PayloadCompletenessState.PARTIAL
            else -> PayloadCompletenessState.INSUFFICIENT
        }
        val warnings = buildList {
            if (title.isBlank()) add(appStrings.get(R.string.structured_warning_missing_target))
            if (timeHint.isBlank()) add(appStrings.get(R.string.structured_warning_missing_time))
        }
        val payload = StructuredActionPayload(
            actionType = StructuredActionType.CALENDAR_DELETE,
            completenessState = completeness,
            fields = fields,
            evidence = listOfNotNull(
                title.takeIf { it.isNotBlank() }?.let { PayloadFieldEvidence("title", it, 0.7) },
                timeHint.takeIf { it.isNotBlank() }?.let { PayloadFieldEvidence("timeHint", it, 0.63) },
            ),
            warnings = warnings,
        )
        return ActionNormalizationResult(
            applies = true,
            payload = payload,
            preview = StructuredExecutionPreview(
                title = appStrings.structuredActionTypeLabel(StructuredActionType.CALENDAR_DELETE),
                summary = appStrings.get(
                    R.string.structured_summary_calendar_delete,
                    title.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                ),
                fieldLines = buildList {
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_target),
                            title.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_time),
                            timeHint.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                },
                warnings = warnings,
                completenessState = completeness,
            ),
            rationale = appStrings.get(R.string.structured_rationale_calendar_delete),
        )
    }

    private fun normalizeShare(
        request: RuntimeRequest,
    ): ActionNormalizationResult {
        val raw = request.userInput.trim()
        val destinationHint = extractDestinationHint(raw)
        val content = extractShareContent(raw).ifBlank { raw }
        val fields = buildMap {
            if (destinationHint.isNotBlank()) put("destinationHint", destinationHint)
            if (content.isNotBlank()) put("content", content)
        }
        val completeness = when {
            content.isNotBlank() && destinationHint.isNotBlank() -> PayloadCompletenessState.COMPLETE
            content.isNotBlank() -> PayloadCompletenessState.PARTIAL
            else -> PayloadCompletenessState.INSUFFICIENT
        }
        val warnings = buildList {
            if (destinationHint.isBlank()) add(appStrings.get(R.string.structured_warning_missing_destination))
            if (content.isBlank()) add(appStrings.get(R.string.structured_warning_missing_content))
        }
        val payload = StructuredActionPayload(
            actionType = StructuredActionType.EXTERNAL_SHARE,
            completenessState = completeness,
            fields = fields,
            evidence = listOfNotNull(
                destinationHint.takeIf { it.isNotBlank() }?.let {
                    PayloadFieldEvidence("destinationHint", it, 0.58)
                },
                content.takeIf { it.isNotBlank() }?.let {
                    PayloadFieldEvidence("content", it.take(120), 0.74)
                },
            ),
            warnings = warnings,
        )
        return ActionNormalizationResult(
            applies = true,
            payload = payload,
            preview = StructuredExecutionPreview(
                title = appStrings.structuredActionTypeLabel(StructuredActionType.EXTERNAL_SHARE),
                summary = appStrings.get(
                    R.string.structured_summary_share,
                    destinationHint.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                ),
                fieldLines = buildList {
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_destination),
                            destinationHint.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                    add(
                        appStrings.get(
                            R.string.structured_field_template,
                            appStrings.get(R.string.structured_field_content),
                            content.ifBlank { appStrings.get(R.string.structured_field_unknown) },
                        ),
                    )
                },
                warnings = warnings,
                completenessState = completeness,
            ),
            rationale = appStrings.get(R.string.structured_rationale_share),
        )
    }

    private fun extractRecipientHint(raw: String): String {
        val lower = raw.lowercase()
        val patterns = listOf(
            Regex("""(?:send (?:a )?message to|text|email|reply to|respond to)\s+([^\n:,.]+)""", RegexOption.IGNORE_CASE),
            Regex("""(?:给|发给|告诉|回复)\s*([^\s，。,:：]+)"""),
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(raw)?.groupValues?.getOrNull(1)?.trim()
        }.orEmpty().removePrefix("@").trim()
            .ifBlank {
                if (" mom" in lower) "mom" else ""
            }
    }

    private fun extractMessageBody(raw: String): String {
        val quoteMatch = Regex("""["“](.+?)["”]""").find(raw)?.groupValues?.getOrNull(1)?.trim()
        if (!quoteMatch.isNullOrBlank()) return quoteMatch

        val separators = listOf(" saying ", " that ", "：", ":")
        return separators.firstNotNullOfOrNull { separator ->
            raw.substringAfter(separator, "").trim().takeIf { it.isNotBlank() && it != raw }
        }.orEmpty()
    }
    private fun extractDestinationHint(raw: String): String {
        val patterns = listOf(
            Regex("""(?:share|post|publish|tweet|send)\s+(?:this|it)?\s*(?:to|with|on)\s+([^\n:,.]+)""", RegexOption.IGNORE_CASE),
            Regex("""(?:分享到|发到|发给|发布到)\s*([^\s，。,:：]+)"""),
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(raw)?.groupValues?.getOrNull(1)?.trim()
        }.orEmpty()
    }

    private fun extractShareContent(raw: String): String {
        val quoted = Regex("""["“](.+?)["”]""").find(raw)?.groupValues?.getOrNull(1)?.trim()
        if (!quoted.isNullOrBlank()) return quoted
        val separators = listOf(" share ", " share this ", " post ", " publish ", "分享到", "发布", ":")
        return separators.firstNotNullOfOrNull { separator ->
            raw.substringAfter(separator, "").trim().takeIf { it.isNotBlank() && it != raw }
        }.orEmpty()
    }
}
