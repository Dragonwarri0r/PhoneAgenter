package com.mobileclaw.app.runtime.provider

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.action.CalendarCapabilityParser
import com.mobileclaw.app.runtime.action.ParsedCalendarCreate
import com.mobileclaw.app.runtime.action.ParsedCalendarDelete
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class CalendarMutationCapabilityProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) : CapabilityProvider {
    override val providerId: String = "calendar_mutation_capability"

    override fun supports(plan: RuntimePlan): Boolean {
        return plan.selectedCapabilityId in setOf("calendar.write", "calendar.delete")
    }

    override fun execute(
        request: CapabilityExecutionRequest,
    ): Flow<ProviderExecutionEvent> = flow {
        val descriptor = request.providerDescriptor
        if (descriptor == null) {
            emitFailure(request, appStrings.get(R.string.bridge_execution_missing_route))
            return@flow
        }

        emit(
            ProviderExecutionEvent.ExecutionStarted(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
            ),
        )

        when (request.plan.selectedCapabilityId) {
            "calendar.write" -> executeCreate(request)
            "calendar.delete" -> executeDelete(request)
            else -> emitFailure(
                request,
                appStrings.get(
                    R.string.bridge_execution_capability_not_supported,
                    request.plan.selectedCapabilityId,
                ),
            )
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ProviderExecutionEvent>.executeCreate(
        request: CapabilityExecutionRequest,
    ) {
        if (!hasPermissions(android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)) {
            emitFailure(request, appStrings.get(R.string.calendar_write_permission_required))
            return
        }

        val parsed = request.structuredPayload?.let { payload ->
            ParsedCreatePayload(
                title = payload.fieldValue("title"),
                description = payload.fieldValue("description"),
                timeLabel = payload.fieldValue("timeHint"),
                startEpochMillis = payload.fieldValue("startEpochMillis").toLongOrNull(),
                endEpochMillis = payload.fieldValue("endEpochMillis").toLongOrNull(),
                allDay = payload.fieldValue("allDay").toBooleanStrictOrNull() == true,
            )
        } ?: CalendarCapabilityParser.parseCreate(request.request.userInput).toPayload()

        if (parsed.title.isBlank()) {
            emitFailure(request, appStrings.get(R.string.calendar_write_missing_title))
            return
        }
        if (parsed.startEpochMillis == null || parsed.endEpochMillis == null) {
            emitFailure(request, appStrings.get(R.string.calendar_write_missing_time))
            return
        }

        val calendar = findWritableCalendar()
        if (calendar == null) {
            emitFailure(request, appStrings.get(R.string.calendar_write_no_writable_calendar))
            return
        }

        runCatching {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendar.calendarId)
                put(CalendarContract.Events.TITLE, parsed.title)
                put(CalendarContract.Events.DESCRIPTION, parsed.description.ifBlank { request.request.userInput })
                put(CalendarContract.Events.DTSTART, parsed.startEpochMillis)
                put(CalendarContract.Events.DTEND, parsed.endEpochMillis)
                put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
                put(CalendarContract.Events.ALL_DAY, if (parsed.allDay) 1 else 0)
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }.onSuccess { insertedUri ->
            if (insertedUri == null) {
                emitFailure(request, appStrings.get(R.string.calendar_write_failed_generic))
                return
            }
            emit(
                ProviderExecutionEvent.ExecutionCompleted(
                    capabilityId = request.plan.selectedCapabilityId,
                    providerId = providerId,
                    outputText = appStrings.get(
                        R.string.calendar_write_created,
                        parsed.title,
                        parsed.timeLabel.ifBlank { formatTime(parsed.startEpochMillis) },
                    ),
                ),
            )
        }.onFailure { throwable ->
            emitFailure(
                request,
                throwable.message ?: appStrings.get(R.string.calendar_write_failed_generic),
            )
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ProviderExecutionEvent>.executeDelete(
        request: CapabilityExecutionRequest,
    ) {
        if (!hasPermissions(android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)) {
            emitFailure(request, appStrings.get(R.string.calendar_delete_permission_required))
            return
        }

        val parsed = request.structuredPayload?.let { payload ->
            ParsedDeletePayload(
                title = payload.fieldValue("title"),
                windowLabel = payload.fieldValue("timeHint"),
                queryStartEpochMillis = payload.fieldValue("queryStartEpochMillis").toLongOrNull(),
                queryEndEpochMillis = payload.fieldValue("queryEndEpochMillis").toLongOrNull(),
            )
        } ?: CalendarCapabilityParser.parseDelete(request.request.userInput).toPayload()

        if (parsed.title.isBlank()) {
            emitFailure(request, appStrings.get(R.string.calendar_delete_missing_target))
            return
        }
        if (parsed.queryStartEpochMillis == null || parsed.queryEndEpochMillis == null) {
            emitFailure(request, appStrings.get(R.string.calendar_delete_missing_time))
            return
        }

        runCatching {
            findDeletionCandidates(
                title = parsed.title,
                startEpochMillis = parsed.queryStartEpochMillis,
                endEpochMillis = parsed.queryEndEpochMillis,
            )
        }.onSuccess { candidates ->
            if (candidates.isEmpty()) {
                emit(
                    ProviderExecutionEvent.ExecutionCompleted(
                        capabilityId = request.plan.selectedCapabilityId,
                        providerId = providerId,
                        outputText = appStrings.get(
                            R.string.calendar_delete_no_match,
                            parsed.title,
                            parsed.windowLabel.ifBlank {
                                formatTime(parsed.queryStartEpochMillis)
                            },
                        ),
                    ),
                )
                return
            }

            if (candidates.size > 1) {
                val candidateLines = candidates.take(3).joinToString(separator = "\n") { candidate ->
                    appStrings.get(
                        R.string.calendar_delete_candidate_line,
                        candidate.title,
                        formatTime(candidate.startEpochMillis),
                    )
                }
                emit(
                    ProviderExecutionEvent.ExecutionCompleted(
                        capabilityId = request.plan.selectedCapabilityId,
                        providerId = providerId,
                        outputText = appStrings.get(
                            R.string.calendar_delete_ambiguous,
                            candidates.size,
                            parsed.title,
                        ) + "\n" + candidateLines,
                    ),
                )
                return
            }

            val candidate = candidates.first()
            val deletedRows = context.contentResolver.delete(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, candidate.eventId),
                null,
                null,
            )
            if (deletedRows > 0) {
                emit(
                    ProviderExecutionEvent.ExecutionCompleted(
                        capabilityId = request.plan.selectedCapabilityId,
                        providerId = providerId,
                        outputText = appStrings.get(
                            R.string.calendar_delete_deleted,
                            candidate.title,
                            formatTime(candidate.startEpochMillis),
                        ),
                    ),
                )
            } else {
                emitFailure(request, appStrings.get(R.string.calendar_delete_failed_generic))
            }
        }.onFailure { throwable ->
            emitFailure(
                request,
                throwable.message ?: appStrings.get(R.string.calendar_delete_failed_generic),
            )
        }
    }

    private fun findWritableCalendar(): WritableCalendar? {
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.VISIBLE,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            ),
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} DESC",
        )
        cursor?.use {
            while (it.moveToNext()) {
                val calendarId = it.getLong(0)
                val displayName = it.getString(1).orEmpty().ifBlank {
                    appStrings.get(R.string.system_source_calendar)
                }
                return WritableCalendar(calendarId = calendarId, displayName = displayName)
            }
        }
        return null
    }

    private fun findDeletionCandidates(
        title: String,
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): List<CalendarDeleteCandidate> {
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startEpochMillis)
        ContentUris.appendId(builder, endEpochMillis)
        val normalizedTitle = normalize(title)
        val exactMatches = mutableListOf<CalendarDeleteCandidate>()
        val fuzzyMatches = mutableListOf<CalendarDeleteCandidate>()
        val cursor = context.contentResolver.query(
            builder.build(),
            arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
            ),
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC",
        )
        cursor?.use {
            while (it.moveToNext()) {
                val eventId = it.getLong(0)
                val eventTitle = it.getString(1).orEmpty()
                val begin = it.getLong(2)
                val normalizedEventTitle = normalize(eventTitle)
                val candidate = CalendarDeleteCandidate(
                    eventId = eventId,
                    title = eventTitle.ifBlank { appStrings.get(R.string.system_source_calendar_event_untitled) },
                    startEpochMillis = begin,
                )
                when {
                    normalizedEventTitle == normalizedTitle -> exactMatches += candidate
                    normalizedTitle.isNotBlank() &&
                        (normalizedEventTitle.contains(normalizedTitle) ||
                            normalizedTitle.contains(normalizedEventTitle)) -> fuzzyMatches += candidate
                }
            }
        }
        return if (exactMatches.isNotEmpty()) exactMatches else fuzzyMatches
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun hasPermissions(vararg permissions: String): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun formatTime(epochMillis: Long?): String {
        if (epochMillis == null) return appStrings.get(R.string.structured_field_unknown)
        return DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
        ).format(Date(epochMillis))
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ProviderExecutionEvent>.emitFailure(
        request: CapabilityExecutionRequest,
        message: String,
    ) {
        emit(
            ProviderExecutionEvent.ExecutionFailed(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
                userMessage = message,
            ),
        )
    }

    private data class WritableCalendar(
        val calendarId: Long,
        val displayName: String,
    )

    private data class CalendarDeleteCandidate(
        val eventId: Long,
        val title: String,
        val startEpochMillis: Long,
    )

    private data class ParsedCreatePayload(
        val title: String,
        val description: String,
        val timeLabel: String,
        val startEpochMillis: Long? = null,
        val endEpochMillis: Long? = null,
        val allDay: Boolean = false,
    )

    private data class ParsedDeletePayload(
        val title: String,
        val windowLabel: String,
        val queryStartEpochMillis: Long? = null,
        val queryEndEpochMillis: Long? = null,
    )

    private fun ParsedCalendarCreate.toPayload(): ParsedCreatePayload {
        return ParsedCreatePayload(
            title = title,
            description = description,
            timeLabel = timeLabel,
            startEpochMillis = startEpochMillis,
            endEpochMillis = endEpochMillis,
            allDay = allDay,
        )
    }

    private fun ParsedCalendarDelete.toPayload(): ParsedDeletePayload {
        return ParsedDeletePayload(
            title = title,
            windowLabel = windowLabel,
            queryStartEpochMillis = queryStartEpochMillis,
            queryEndEpochMillis = queryEndEpochMillis,
        )
    }
}
