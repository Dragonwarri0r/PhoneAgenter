package com.mobileclaw.app.runtime.provider

import android.Manifest
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.capability.StandardToolCatalog
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadToolRequestBuilder @Inject constructor(
    private val standardToolCatalog: StandardToolCatalog,
    private val appStrings: AppStrings,
) {
    fun build(
        request: RuntimeRequest,
        plan: RuntimePlan,
        contextPayload: RuntimeContextPayload,
    ): ExplicitReadToolRequest? {
        return when (plan.selectedCapabilityId) {
            "calendar.read" -> buildCalendarReadRequest(request, plan)
            else -> null
        }
    }

    private fun buildCalendarReadRequest(
        request: RuntimeRequest,
        plan: RuntimePlan,
    ): ExplicitReadToolRequest {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val lower = request.userInput.lowercase()
        val scope = when {
            "tomorrow" in lower || "明天" in lower -> queryScope(
                scopeId = "tomorrow",
                displayLabel = appStrings.get(R.string.read_scope_tomorrow),
                startDate = today.plusDays(1),
                startTime = LocalTime.MIN,
                endDate = today.plusDays(2),
                endTime = LocalTime.MIN,
                zoneId = zoneId,
            )

            "this afternoon" in lower || "afternoon" in lower || "下午" in lower -> queryScope(
                scopeId = "today_afternoon",
                displayLabel = appStrings.get(R.string.read_scope_this_afternoon),
                startDate = today,
                startTime = LocalTime.NOON,
                endDate = today,
                endTime = LocalTime.of(18, 0),
                zoneId = zoneId,
            )

            "this week" in lower || "next 7" in lower || "本周" in lower -> queryScope(
                scopeId = "this_week",
                displayLabel = appStrings.get(R.string.read_scope_this_week),
                startDate = today,
                startTime = LocalTime.MIN,
                endDate = today.plusDays(7),
                endTime = LocalTime.MIN,
                zoneId = zoneId,
            )

            else -> queryScope(
                scopeId = "today",
                displayLabel = appStrings.get(R.string.read_scope_today),
                startDate = today,
                startTime = LocalTime.MIN,
                endDate = today.plusDays(1),
                endTime = LocalTime.MIN,
                zoneId = zoneId,
            )
        }
        val descriptor = standardToolCatalog.descriptorForCapability(plan.selectedCapabilityId)
        return ExplicitReadToolRequest(
            requestId = request.requestId,
            capabilityId = plan.selectedCapabilityId,
            queryText = request.userInput.trim(),
            queryScope = scope,
            resultLimit = descriptor.defaultResultLimit.coerceAtLeast(1),
            permissionRequirements = listOf(Manifest.permission.READ_CALENDAR),
            routeExplanation = plan.selectionOutcome?.explanation
                ?: appStrings.get(R.string.read_tool_route_default, descriptor.displayName),
            selectionContext = plan.selectionOutcome,
        )
    }

    private fun queryScope(
        scopeId: String,
        displayLabel: String,
        startDate: LocalDate,
        startTime: LocalTime,
        endDate: LocalDate,
        endTime: LocalTime,
        zoneId: ZoneId,
    ): ReadQueryScope {
        return ReadQueryScope(
            scopeId = scopeId,
            displayLabel = displayLabel,
            startEpochMillis = startDate.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli(),
            endEpochMillis = endDate.atTime(endTime).atZone(zoneId).toInstant().toEpochMilli(),
        )
    }
}

