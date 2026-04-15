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
        val title = extractCalendarTitle(raw)
        val timeHint = extractTimeHint(raw)
        val description = raw.takeIf { it.isNotBlank() }.orEmpty()
        val fields = buildMap {
            if (title.isNotBlank()) put("title", title)
            if (timeHint.isNotBlank()) put("timeHint", timeHint)
            if (description.isNotBlank()) put("description", description)
            if (contextPayload.personaSummary.isNotBlank()) put("personaSummary", contextPayload.personaSummary)
        }
        val completeness = when {
            title.isNotBlank() && timeHint.isNotBlank() -> PayloadCompletenessState.COMPLETE
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

    private fun extractCalendarTitle(raw: String): String {
        val patterns = listOf(
            Regex("""(?:schedule|create|book|set up|reschedule)\s+(?:a |an )?(?:meeting|event)?\s*(.+?)?(?:\s+(?:at|on|for)\b|$)""", RegexOption.IGNORE_CASE),
            Regex("""(?:安排|创建|添加)(.+?)(?:在|到|明天|今天|今晚|下周|$)"""),
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(raw)?.groupValues?.getOrNull(1)?.trim()
        }.orEmpty().trim(',', '，', '。', ':', '：', ' ')
    }

    private fun extractTimeHint(raw: String): String {
        val patterns = listOf(
            Regex("""\b(today|tomorrow|tonight|next week|next monday|next tuesday|next wednesday|next thursday|next friday|next saturday|next sunday)\b(?:\s+at\s+\d{1,2}(?::\d{2})?\s*(?:am|pm)?)?""", RegexOption.IGNORE_CASE),
            Regex("""\b\d{1,2}(?::\d{2})?\s*(?:am|pm)\b""", RegexOption.IGNORE_CASE),
            Regex("""(?:今天|明天|今晚|下周[一二三四五六日天]?)(?:\s*[上下]午?\s*\d{1,2}[:：]?\d{0,2})?"""),
            Regex("""[上下]午?\s*\d{1,2}[:：]?\d{0,2}"""),
        )
        return patterns.firstNotNullOfOrNull { it.find(raw)?.value?.trim() }.orEmpty()
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
