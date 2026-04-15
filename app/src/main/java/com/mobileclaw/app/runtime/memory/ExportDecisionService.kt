package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.extension.RuntimeExtensionRegistry
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportDecisionService @Inject constructor(
    private val appStrings: AppStrings,
    private val runtimeExtensionRegistry: RuntimeExtensionRegistry,
) {
    fun evaluateRedactionPolicy(item: MemoryItem): DataRedactionPolicy {
        return when (item.exposurePolicy) {
            MemoryExposurePolicy.PRIVATE -> DataRedactionPolicy(
                recordId = item.memoryId,
                exposurePolicy = item.exposurePolicy,
                allowFullExport = false,
                allowSummaryExport = false,
                mustRedactEvidence = true,
                reason = appStrings.get(R.string.memory_redaction_private),
            )

            MemoryExposurePolicy.SHAREABLE_SUMMARY -> DataRedactionPolicy(
                recordId = item.memoryId,
                exposurePolicy = item.exposurePolicy,
                allowFullExport = false,
                allowSummaryExport = true,
                mustRedactEvidence = true,
                reason = appStrings.get(R.string.memory_redaction_summary_only),
            )

            MemoryExposurePolicy.SHAREABLE_FULL -> DataRedactionPolicy(
                recordId = item.memoryId,
                exposurePolicy = item.exposurePolicy,
                allowFullExport = true,
                allowSummaryExport = true,
                mustRedactEvidence = false,
                reason = appStrings.get(R.string.memory_redaction_full_allowed),
            )
        }
    }

    fun buildExportBundle(
        item: MemoryItem,
        preferFullExport: Boolean = false,
    ): ExportBundle {
        val policy = evaluateRedactionPolicy(item)
        val exportMode = if (preferFullExport && policy.allowFullExport) {
            ExportMode.FULL_RECORD
        } else {
            ExportMode.SUMMARY_ONLY
        }
        val payloadText = if (exportMode == ExportMode.FULL_RECORD) {
            item.contentText
        } else {
            item.summaryText
        }
        val included = buildList {
            add("logicalRecordId")
            add("summaryText")
            if (exportMode == ExportMode.FULL_RECORD) add("contentText")
            if (!policy.mustRedactEvidence && item.evidenceRef != null) add("evidenceRef")
            add("exposurePolicy")
            add("syncPolicy")
        }
        val redacted = buildList {
            if (exportMode != ExportMode.FULL_RECORD) add("contentText")
            if (policy.mustRedactEvidence && item.evidenceRef != null) add("evidenceRef")
        }
        return ExportBundle(
            bundleId = "bundle-${item.memoryId}-$exportMode",
            recordId = item.memoryId,
            logicalRecordId = item.logicalRecordId,
            exportMode = exportMode,
            payloadText = payloadText,
            includedFields = included,
            redactedFields = redacted,
            exposurePolicy = item.exposurePolicy,
            syncPolicy = item.syncPolicy,
            generatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    fun extensionCompatibilities(item: MemoryItem): List<ExtensionCompatibility> {
        val availableFields = setOf(
            "logicalRecordId",
            "contentText",
            "summaryText",
            "exposurePolicy",
            "syncPolicy",
            "originDeviceId",
            "originUserId",
            "logicalVersion",
            "schemaVersion",
        )
        return runtimeExtensionRegistry.registrations().map { registration ->
            runtimeExtensionRegistry.evaluateCompatibility(
                registration = registration,
                availableFields = availableFields,
            )
        }
    }
}
