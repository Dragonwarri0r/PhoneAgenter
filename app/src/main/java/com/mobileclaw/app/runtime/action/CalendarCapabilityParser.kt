package com.mobileclaw.app.runtime.action

import java.text.DateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

data class ParsedCalendarCreate(
    val title: String,
    val description: String,
    val startEpochMillis: Long? = null,
    val endEpochMillis: Long? = null,
    val timeLabel: String = "",
    val allDay: Boolean = false,
)

data class ParsedCalendarDelete(
    val title: String,
    val queryStartEpochMillis: Long? = null,
    val queryEndEpochMillis: Long? = null,
    val windowLabel: String = "",
)

object CalendarCapabilityParser {
    fun parseCreate(
        rawInput: String,
        zoneId: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zoneId),
    ): ParsedCalendarCreate {
        val temporal = parseTemporalHints(rawInput, now)
        val title = extractCreateTitle(rawInput)
        val description = rawInput.trim()
        val duration = parseDuration(rawInput)
        val resolvedDate = temporal.date ?: temporal.time?.let { now.toLocalDate() }
        val start = when {
            temporal.allDay && resolvedDate != null -> resolvedDate.atStartOfDay(zoneId)
            temporal.time != null && resolvedDate != null -> resolvedDate.atTime(temporal.time).atZone(zoneId)
            else -> null
        }
        val end = when {
            temporal.allDay && resolvedDate != null -> resolvedDate.plusDays(1).atStartOfDay(zoneId)
            start != null -> start.plus(duration)
            else -> null
        }

        return ParsedCalendarCreate(
            title = title,
            description = description,
            startEpochMillis = start?.toInstant()?.toEpochMilli(),
            endEpochMillis = end?.toInstant()?.toEpochMilli(),
            timeLabel = when {
                temporal.timeLabel.isNotBlank() -> temporal.timeLabel
                start != null -> formatEpoch(start.toInstant().toEpochMilli())
                else -> ""
            },
            allDay = temporal.allDay,
        )
    }

    fun parseDelete(
        rawInput: String,
        zoneId: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zoneId),
    ): ParsedCalendarDelete {
        val temporal = parseTemporalHints(rawInput, now)
        val title = extractDeleteTitle(rawInput)
        val queryWindow = when {
            temporal.allDay && temporal.date != null -> {
                val start = temporal.date.atStartOfDay(zoneId)
                start to temporal.date.plusDays(1).atStartOfDay(zoneId)
            }

            temporal.time != null && temporal.date != null -> {
                val center = temporal.date.atTime(temporal.time).atZone(zoneId)
                center.minusHours(2) to center.plusHours(3)
            }

            temporal.date != null -> {
                val start = temporal.date.atStartOfDay(zoneId)
                start to temporal.date.plusDays(1).atStartOfDay(zoneId)
            }

            else -> null
        }

        return ParsedCalendarDelete(
            title = title,
            queryStartEpochMillis = queryWindow?.first?.toInstant()?.toEpochMilli(),
            queryEndEpochMillis = queryWindow?.second?.toInstant()?.toEpochMilli(),
            windowLabel = when {
                temporal.timeLabel.isNotBlank() -> temporal.timeLabel
                queryWindow != null -> formatEpoch(queryWindow.first.toInstant().toEpochMilli())
                else -> ""
            },
        )
    }

    private data class TemporalHints(
        val date: LocalDate? = null,
        val time: LocalTime? = null,
        val allDay: Boolean = false,
        val timeLabel: String = "",
    )

    private fun parseTemporalHints(
        rawInput: String,
        now: ZonedDateTime,
    ): TemporalHints {
        val lower = rawInput.lowercase()
        val allDay = listOf("all day", "全天", "整天").any(lower::contains)
        val date = when {
            "tomorrow" in lower || "明天" in rawInput -> now.toLocalDate().plusDays(1)
            "next week" in lower || "下周" in rawInput -> now.toLocalDate().plusWeeks(1)
            "today" in lower || "今天" in rawInput || "tonight" in lower || "今晚" in rawInput -> {
                now.toLocalDate()
            }

            else -> null
        }
        val time = parseEnglishTime(lower) ?: parseChineseTime(rawInput)
        val timeLabel = when {
            allDay && date != null -> formatDate(date, now.zone)
            time != null && date != null -> formatEpoch(date.atTime(time).atZone(now.zone).toInstant().toEpochMilli())
            time != null -> renderClock(time)
            date != null -> formatDate(date, now.zone)
            else -> ""
        }
        return TemporalHints(
            date = date,
            time = time,
            allDay = allDay,
            timeLabel = timeLabel,
        )
    }

    private fun parseEnglishTime(lowerInput: String): LocalTime? {
        val match = Regex("""\b(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""", RegexOption.IGNORE_CASE)
            .find(lowerInput)
            ?: return null
        val rawHour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: 0
        val meridiem = match.groupValues[3].lowercase()
        val hour = when {
            meridiem == "am" && rawHour == 12 -> 0
            meridiem == "pm" && rawHour < 12 -> rawHour + 12
            else -> rawHour
        }
        return if (hour in 0..23 && minute in 0..59) {
            LocalTime.of(hour, minute)
        } else {
            null
        }
    }

    private fun parseChineseTime(rawInput: String): LocalTime? {
        val match = Regex("""(上午|早上|中午|下午|晚上)?\s*(\d{1,2})(?:[:：点时]\s*(\d{1,2}))?""")
            .find(rawInput)
            ?: return null
        val period = match.groupValues[1]
        var hour = match.groupValues[2].toIntOrNull() ?: return null
        val minute = match.groupValues[3].toIntOrNull() ?: 0
        hour = when (period) {
            "上午", "早上" -> if (hour == 12) 0 else hour
            "中午" -> if (hour < 11) hour + 12 else hour
            "下午", "晚上" -> if (hour < 12) hour + 12 else hour
            else -> hour
        }
        return if (hour in 0..23 && minute in 0..59) {
            LocalTime.of(hour, minute)
        } else {
            null
        }
    }

    private fun parseDuration(rawInput: String): Duration {
        val english = Regex("""for\s+(\d{1,2})\s*(minutes|minute|mins|min|hours|hour|hrs|hr)\b""", RegexOption.IGNORE_CASE)
            .find(rawInput)
        if (english != null) {
            val amount = english.groupValues[1].toLongOrNull() ?: 60L
            return if ("hour" in english.groupValues[2].lowercase() || "hr" in english.groupValues[2].lowercase()) {
                Duration.ofHours(amount)
            } else {
                Duration.ofMinutes(amount)
            }
        }
        val chinese = Regex("""(?:持续|时长)?\s*(\d{1,2})\s*(分钟|小时|钟头)""")
            .find(rawInput)
        if (chinese != null) {
            val amount = chinese.groupValues[1].toLongOrNull() ?: 60L
            return if (chinese.groupValues[2] == "分钟") {
                Duration.ofMinutes(amount)
            } else {
                Duration.ofHours(amount)
            }
        }
        return Duration.ofHours(1)
    }

    private fun extractCreateTitle(rawInput: String): String {
        extractQuotedValue(rawInput)?.let(::trimTitle)?.takeIf { it.isNotBlank() }?.let { return it }

        val patterns = listOf(
            Regex("""(?:add|create|schedule|book|set up|remind me about)\s+(?:an?\s+)?(?:event|meeting)?\s*(.+?)(?:\s+(?:today|tomorrow|tonight|next|at|on|for|to my calendar)\b|$)""", RegexOption.IGNORE_CASE),
            Regex("""(?:安排|创建|添加|加入)\s*(.+?)(?:\s*(?:到|进)?日历|今天|明天|今晚|下周|上午|下午|晚上|中午|$)"""),
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(rawInput)?.groupValues?.getOrNull(1)?.trim()
        }?.let(::cleanTitle).orEmpty()
    }

    private fun extractDeleteTitle(rawInput: String): String {
        extractQuotedValue(rawInput)?.let(::trimTitle)?.takeIf { it.isNotBlank() }?.let { return it }

        val patterns = listOf(
            Regex("""(?:delete|remove|cancel)\s+(?:the\s+|my\s+)?(.+?)(?:\s+(?:from my calendar|from calendar|today|tomorrow|tonight|at|on|for)\b|$)""", RegexOption.IGNORE_CASE),
            Regex("""(?:删除|删掉|取消)\s*(.+?)(?:\s*(?:这个|那条)?(?:日程|事件|会议))?(?:\s*(?:今天|明天|今晚|下周|上午|下午|晚上|中午|$))"""),
            Regex("""把\s*(.+?)\s*(?:从)?(?:日历|日程)里?(?:删除|删掉|取消)"""),
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(rawInput)?.groupValues?.getOrNull(1)?.trim()
        }?.let(::cleanTitle).orEmpty()
    }

    private fun extractQuotedValue(rawInput: String): String? {
        return Regex("""["“”'‘’](.+?)["“”'‘’]""")
            .find(rawInput)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
    }

    private fun cleanTitle(value: String): String {
        return value
            .replace(
                Regex(
                    """\b(to my calendar|on my calendar|from my calendar|calendar|event|meeting)\b""",
                    RegexOption.IGNORE_CASE,
                ),
                "",
            )
            .replace(Regex("""(?:加入日历|加到日历|到日历|日历里|日程里|这个日程|这个事件|这条日程|事件|日程|会议)$"""), "")
            .let(::trimTitle)
    }

    private fun trimTitle(value: String): String {
        return value.trim(' ', ',', '，', '。', ':', '：', '"', '\'')
    }

    private fun formatDate(
        date: LocalDate,
        zoneId: ZoneId,
    ): String {
        return formatEpoch(date.atStartOfDay(zoneId).toInstant().toEpochMilli())
    }

    private fun renderClock(time: LocalTime): String {
        return "%02d:%02d".format(time.hour, time.minute)
    }

    private fun formatEpoch(epochMillis: Long): String {
        return DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
        ).format(Date(epochMillis))
    }
}
