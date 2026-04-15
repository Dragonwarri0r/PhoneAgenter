package com.mobileclaw.app.runtime.systemsource

import android.Manifest
import android.content.Context
import android.provider.CalendarContract
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.intent.RuntimeIntentHeuristics
import com.mobileclaw.app.runtime.memory.MemoryExposurePolicy
import com.mobileclaw.app.runtime.memory.MemoryItem
import com.mobileclaw.app.runtime.memory.MemoryLifecycle
import com.mobileclaw.app.runtime.memory.MemoryScope
import com.mobileclaw.app.runtime.memory.MemorySourceType
import com.mobileclaw.app.runtime.memory.MemorySyncPolicy
import com.mobileclaw.app.runtime.memory.ScopedMemoryRepository
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemSourceIngestionService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sourceRepository: SystemSourceRepository,
    private val memoryRepository: ScopedMemoryRepository,
    private val appStrings: AppStrings,
) {
    suspend fun ingestForRequest(request: RuntimeRequest): SystemSourceIngestionBundle {
        val descriptors = sourceRepository.currentDescriptors()
        val results = mutableListOf<SystemSourceIngestionResult>()
        val contributions = mutableListOf<SystemSourceContribution>()
        val input = request.userInput
        val inferred = RuntimeIntentHeuristics.infer(input)

        descriptors.firstOrNull { it.sourceId == SystemSourceId.CONTACTS }?.let { descriptor ->
            if (descriptor.isGranted && shouldCheckContacts(input)) {
                val contactItems = ingestContacts(request)
                if (contactItems.isNotEmpty()) {
                    results += SystemSourceIngestionResult(
                        sourceId = SystemSourceId.CONTACTS,
                        recordsWritten = contactItems.size,
                        recordsSkipped = 0,
                        statusMessage = appStrings.get(R.string.system_source_contacts_contributed, contactItems.size),
                    )
                    contributions += SystemSourceContribution(
                        sourceId = SystemSourceId.CONTACTS,
                        displayName = descriptor.displayName,
                        recordCount = contactItems.size,
                        summary = appStrings.get(R.string.system_source_contacts_contributed, contactItems.size),
                    )
                }
            }
        }

        descriptors.firstOrNull { it.sourceId == SystemSourceId.CALENDAR }?.let { descriptor ->
            if (descriptor.isGranted && shouldCheckCalendar(input, inferred.capabilityId)) {
                val calendarItems = ingestCalendar(request)
                if (calendarItems.isNotEmpty()) {
                    results += SystemSourceIngestionResult(
                        sourceId = SystemSourceId.CALENDAR,
                        recordsWritten = calendarItems.size,
                        recordsSkipped = 0,
                        statusMessage = appStrings.get(R.string.system_source_calendar_contributed, calendarItems.size),
                    )
                    contributions += SystemSourceContribution(
                        sourceId = SystemSourceId.CALENDAR,
                        displayName = descriptor.displayName,
                        recordCount = calendarItems.size,
                        summary = appStrings.get(R.string.system_source_calendar_contributed, calendarItems.size),
                    )
                }
            }
        }

        return SystemSourceIngestionBundle(
            descriptors = descriptors,
            results = results,
            contributions = contributions,
        )
    }

    private suspend fun ingestContacts(request: RuntimeRequest): List<MemoryItem> {
        if (!hasPermission(Manifest.permission.READ_CONTACTS)) return emptyList()
        val tokens = extractQueryTokens(request.userInput)
        if (tokens.isEmpty()) return emptyList()
        val results = mutableListOf<MemoryItem>()
        val now = System.currentTimeMillis()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
            ),
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC",
        )
        cursor?.use {
            while (it.moveToNext() && results.size < 3) {
                val id = it.getString(0) ?: continue
                val name = it.getString(1) ?: continue
                val lowerName = name.lowercase()
                if (tokens.none { token -> lowerName.contains(token) }) continue
                val memory = MemoryItem(
                    memoryId = "sys-contact-$id",
                    logicalRecordId = "sys-contact-$id",
                    title = name,
                    contentText = appStrings.get(R.string.system_source_contact_content, name),
                    summaryText = appStrings.get(R.string.system_source_contact_summary, name),
                    lifecycle = MemoryLifecycle.WORKING,
                    scope = MemoryScope.CONTACT_SCOPED,
                    exposurePolicy = MemoryExposurePolicy.PRIVATE,
                    syncPolicy = MemorySyncPolicy.LOCAL_ONLY,
                    subjectKey = name.lowercase(),
                    originApp = "android.contacts",
                    originDeviceId = request.deviceId,
                    originUserId = "single_user",
                    sourceType = MemorySourceType.SYSTEM_SOURCE,
                    confidence = 0.92,
                    logicalVersion = 1,
                    schemaVersion = 1,
                    isPinned = false,
                    isManuallyEdited = false,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                    expiresAtEpochMillis = now + TimeUnit.DAYS.toMillis(7),
                    evidenceRef = id,
                )
                memoryRepository.upsert(memory)
                results += memory
            }
        }
        return results
    }

    private suspend fun ingestCalendar(request: RuntimeRequest): List<MemoryItem> {
        if (!hasPermission(Manifest.permission.READ_CALENDAR)) return emptyList()
        val now = System.currentTimeMillis()
        val end = now + TimeUnit.DAYS.toMillis(14)
        val results = mutableListOf<MemoryItem>()
        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DESCRIPTION,
            ),
            "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
            arrayOf(now.toString(), end.toString()),
            "${CalendarContract.Events.DTSTART} ASC",
        )
        cursor?.use {
            while (it.moveToNext() && results.size < 3) {
                val id = it.getString(0) ?: continue
                val title = it.getString(1).orEmpty().ifBlank {
                    appStrings.get(R.string.system_source_calendar_event_untitled)
                }
                val startMillis = it.getLong(2)
                val description = it.getString(3).orEmpty()
                val memory = MemoryItem(
                    memoryId = "sys-calendar-$id",
                    logicalRecordId = "sys-calendar-$id",
                    title = title,
                    contentText = appStrings.get(
                        R.string.system_source_calendar_content,
                        title,
                        startMillis.toString(),
                        description.ifBlank { appStrings.get(R.string.common_none) },
                    ),
                    summaryText = appStrings.get(R.string.system_source_calendar_summary, title),
                    lifecycle = MemoryLifecycle.WORKING,
                    scope = MemoryScope.DEVICE_SCOPED,
                    exposurePolicy = MemoryExposurePolicy.PRIVATE,
                    syncPolicy = MemorySyncPolicy.LOCAL_ONLY,
                    subjectKey = request.deviceId,
                    originApp = "android.calendar",
                    originDeviceId = request.deviceId,
                    originUserId = "single_user",
                    sourceType = MemorySourceType.SYSTEM_SOURCE,
                    confidence = 0.9,
                    logicalVersion = 1,
                    schemaVersion = 1,
                    isPinned = false,
                    isManuallyEdited = false,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                    expiresAtEpochMillis = now + TimeUnit.DAYS.toMillis(2),
                    evidenceRef = id,
                )
                memoryRepository.upsert(memory)
                results += memory
            }
        }
        return results
    }

    private fun shouldCheckContacts(userInput: String): Boolean {
        val tokens = extractQueryTokens(userInput)
        return tokens.any()
    }

    private fun shouldCheckCalendar(
        userInput: String,
        capabilityId: String,
    ): Boolean {
        if (capabilityId == "calendar.write") return true
        val lower = userInput.lowercase()
        return listOf("schedule", "meeting", "calendar", "remind", "安排", "日程", "提醒").any { it in lower }
    }

    private fun extractQueryTokens(text: String): List<String> {
        return Regex("""[\p{L}]{3,}""")
            .findAll(text)
            .map { it.value.lowercase() }
            .filter { token ->
                token !in setOf(
                    "send", "message", "share", "this", "that", "with", "about",
                    "create", "meeting", "calendar", "please", "could", "would",
                )
            }
            .take(6)
            .toList()
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
