package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortabilityBundleFormatter @Inject constructor(
    private val appStrings: AppStrings,
) {
    fun buildPreview(
        item: MemoryItem,
        bundle: ExportBundle,
        redactionPolicy: DataRedactionPolicy,
        compatibilities: List<ExtensionCompatibility>,
    ): PortabilityBundlePreview {
        val compatibilityLines = compatibilities.map { compatibility ->
            PortabilityCompatibilityLine(
                title = compatibility.displayName,
                detail = compatibility.reason,
                isCompatible = compatibility.isCompatible,
            )
        }
        return PortabilityBundlePreview(
            memoryId = item.memoryId,
            title = item.title,
            bundle = bundle,
            payloadPreview = buildPayloadPreview(bundle),
            redactionReason = redactionPolicy.reason,
            compatibilityLines = compatibilityLines,
            canShare = redactionPolicy.allowSummaryExport || redactionPolicy.allowFullExport,
            canSwitchToFull = redactionPolicy.allowFullExport,
            canSwitchToSummary = redactionPolicy.allowSummaryExport,
            bundleDocumentText = buildBundleDocument(bundle, compatibilityLines),
        )
    }

    fun buildBundleDocument(
        bundle: ExportBundle,
        compatibilityLines: List<PortabilityCompatibilityLine>,
    ): String {
        val sections = mutableListOf<String>()
        sections += appStrings.get(R.string.portability_bundle_header)
        sections += appStrings.get(R.string.portability_bundle_id_line, bundle.bundleId)
        sections += appStrings.get(R.string.portability_bundle_record_line, bundle.logicalRecordId)
        sections += appStrings.get(
            R.string.portability_bundle_mode_line,
            appStrings.exportModeLabel(bundle.exportMode),
        )
        sections += appStrings.get(R.string.portability_bundle_payload_header)
        sections += bundle.payloadText
        sections += appStrings.get(
            R.string.portability_bundle_included_line,
            bundle.includedFields.joinToString().ifBlank { appStrings.get(R.string.memory_detail_none) },
        )
        sections += appStrings.get(
            R.string.portability_bundle_redacted_line,
            bundle.redactedFields.joinToString().ifBlank { appStrings.get(R.string.memory_detail_none) },
        )
        if (compatibilityLines.isNotEmpty()) {
            sections += appStrings.get(R.string.portability_bundle_compatibility_header)
            sections += compatibilityLines.joinToString(separator = "\n") { line ->
                val status = if (line.isCompatible) {
                    appStrings.get(R.string.portability_compatibility_yes)
                } else {
                    appStrings.get(R.string.portability_compatibility_no)
                }
                "- ${line.title}: $status - ${line.detail}"
            }
        }
        return sections.joinToString(separator = "\n\n")
    }

    private fun buildPayloadPreview(bundle: ExportBundle): String {
        val payload = bundle.payloadText.trim()
        return if (payload.length <= 280) {
            payload
        } else {
            payload.take(277).trimEnd() + "..."
        }
    }
}
