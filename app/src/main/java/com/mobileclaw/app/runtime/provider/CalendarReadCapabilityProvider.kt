package com.mobileclaw.app.runtime.provider

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.mobileclaw.app.R
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
class CalendarReadCapabilityProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) : CapabilityProvider {
    override val providerId: String = "calendar_read_capability"

    override fun supports(plan: RuntimePlan): Boolean = plan.selectedCapabilityId == "calendar.read"

    override fun execute(
        request: CapabilityExecutionRequest,
    ): Flow<ProviderExecutionEvent> = flow {
        val readRequest = request.explicitReadRequest
        val descriptor = request.providerDescriptor
        if (readRequest == null || descriptor == null) {
            emit(
                ProviderExecutionEvent.ExecutionFailed(
                    capabilityId = request.plan.selectedCapabilityId,
                    providerId = providerId,
                    userMessage = appStrings.get(R.string.bridge_execution_missing_route),
                ),
            )
            return@flow
        }

        emit(
            ProviderExecutionEvent.ExecutionStarted(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
            ),
        )

        if (!hasCalendarPermission()) {
            emit(
                ProviderExecutionEvent.ExecutionCompleted(
                    capabilityId = request.plan.selectedCapabilityId,
                    providerId = providerId,
                    outputText = appStrings.get(
                        R.string.calendar_read_unavailable_permission,
                        readRequest.queryScope.displayLabel,
                    ),
                    readResult = ReadToolResult(
                        requestId = request.request.requestId,
                        capabilityId = request.plan.selectedCapabilityId,
                        providerId = descriptor.providerId,
                        outcomeKind = ReadToolOutcomeKind.UNAVAILABLE,
                        userMessage = appStrings.get(
                            R.string.calendar_read_unavailable_permission,
                            readRequest.queryScope.displayLabel,
                        ),
                        auditSummary = appStrings.get(R.string.calendar_read_audit_unavailable_permission),
                        routeExplanation = readRequest.routeExplanation,
                        recoveryMessage = appStrings.get(R.string.calendar_read_recovery_permission),
                    ),
                ),
            )
            return@flow
        }

        runCatching {
            queryCalendar(readRequest)
        }.onSuccess { result ->
            emit(
                ProviderExecutionEvent.ExecutionCompleted(
                    capabilityId = request.plan.selectedCapabilityId,
                    providerId = providerId,
                    outputText = result.userMessage,
                    readResult = result.copy(providerId = descriptor.providerId),
                ),
            )
        }.onFailure { throwable ->
            emit(
                ProviderExecutionEvent.ExecutionFailed(
                    capabilityId = request.plan.selectedCapabilityId,
                    providerId = providerId,
                    userMessage = throwable.message ?: appStrings.get(R.string.calendar_read_failed_generic),
                ),
            )
        }
    }

    private fun queryCalendar(
        readRequest: ExplicitReadToolRequest,
    ): ReadToolResult {
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, readRequest.queryScope.startEpochMillis)
        ContentUris.appendId(builder, readRequest.queryScope.endEpochMillis)
        val summaries = mutableListOf<ReadRecordSummary>()
        val tokenFilters = extractQueryTokens(readRequest.queryText)
        val cursor = context.contentResolver.query(
            builder.build(),
            arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.ALL_DAY,
            ),
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC",
        )
        cursor?.use {
            while (it.moveToNext() && summaries.size < readRequest.resultLimit) {
                val title = it.getString(1).orEmpty().ifBlank {
                    appStrings.get(R.string.system_source_calendar_event_untitled)
                }
                val begin = it.getLong(2)
                val location = it.getString(4).orEmpty()
                val description = it.getString(5).orEmpty()
                if (tokenFilters.isNotEmpty()) {
                    val haystack = listOf(title, location, description).joinToString(" ").lowercase()
                    if (tokenFilters.none(haystack::contains)) continue
                }
                summaries += ReadRecordSummary(
                    title = title,
                    supportingText = listOfNotNull(
                        formatTime(begin),
                        location.takeIf { it.isNotBlank() },
                    ).joinToString(separator = " · "),
                    timestampLabel = formatTime(begin),
                )
            }
        }

        if (summaries.isEmpty()) {
            return ReadToolResult(
                requestId = readRequest.requestId,
                capabilityId = readRequest.capabilityId,
                providerId = providerId,
                outcomeKind = ReadToolOutcomeKind.NO_RESULTS,
                userMessage = appStrings.get(
                    R.string.calendar_read_no_results,
                    readRequest.queryScope.displayLabel,
                ),
                auditSummary = appStrings.get(
                    R.string.calendar_read_audit_no_results,
                    readRequest.queryScope.displayLabel,
                ),
                routeExplanation = readRequest.routeExplanation,
                recoveryMessage = appStrings.get(R.string.calendar_read_recovery_no_results),
            )
        }

        val renderedLines = summaries.joinToString(separator = "\n") { summary ->
            appStrings.get(
                R.string.calendar_read_result_line,
                summary.title,
                summary.supportingText.ifBlank { appStrings.get(R.string.common_none) },
            )
        }
        return ReadToolResult(
            requestId = readRequest.requestId,
            capabilityId = readRequest.capabilityId,
            providerId = providerId,
            outcomeKind = ReadToolOutcomeKind.MATCHED,
            recordSummaries = summaries,
            resultCount = summaries.size,
            userMessage = appStrings.get(
                R.string.calendar_read_result_summary,
                summaries.size,
                readRequest.queryScope.displayLabel,
            ) + "\n" + renderedLines,
            auditSummary = appStrings.get(
                R.string.calendar_read_audit_results,
                summaries.size,
                readRequest.queryScope.displayLabel,
            ),
            routeExplanation = readRequest.routeExplanation,
        )
    }

    private fun extractQueryTokens(queryText: String): List<String> {
        return Regex("""[\p{L}\p{N}]{3,}""")
            .findAll(queryText.lowercase())
            .map { it.value }
            .filterNot {
                it in setOf(
                    "what",
                    "show",
                    "calendar",
                    "today",
                    "tomorrow",
                    "schedule",
                    "this",
                    "week",
                    "查看",
                    "看看",
                    "日历",
                    "日程",
                    "今天",
                    "明天",
                    "本周",
                )
            }
            .take(5)
            .toList()
    }

    private fun formatTime(epochMillis: Long): String {
        return DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
        ).format(Date(epochMillis))
    }

    private fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
