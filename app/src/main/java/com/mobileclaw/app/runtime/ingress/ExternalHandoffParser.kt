package com.mobileclaw.app.runtime.ingress

import android.content.Intent
import android.os.Build
import android.net.Uri
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.multimodal.AttachmentStore
import com.mobileclaw.app.runtime.multimodal.RuntimeAttachmentKind
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ExternalHandoffParseResult {
    data class Accepted(
        val envelope: InteropRequestEnvelope,
    ) : ExternalHandoffParseResult

    data class Rejected(
        val reason: String,
        val sourceLabel: String,
        val trustState: ExternalTrustState,
        val trustReason: String,
        val handoffId: String = "handoff-${System.currentTimeMillis()}",
    ) : ExternalHandoffParseResult
}

@Singleton
class ExternalHandoffParser @Inject constructor(
    private val registration: ExternalEntryRegistration,
    private val attachmentStore: AttachmentStore,
    private val appStrings: AppStrings,
) {
    fun parse(
        intent: Intent?,
        callerPackageName: String?,
        referrerUri: Uri?,
    ): ExternalHandoffParseResult? {
        intent ?: return null
        val action = intent.action ?: return null
        if (action !in registration.supportedActions) return null

        val mimeType = intent.type.orEmpty()
        val packageName = callerPackageName ?: referrerUri?.packageNameOrNull()
        val sourceLabel = appStrings.externalSourceLabel(packageName)
        val trustReason = if (packageName.isNullOrBlank()) {
            appStrings.get(R.string.external_handoff_trust_package_missing)
        } else {
            appStrings.get(R.string.external_handoff_trust_unverified_package, packageName)
        }

        if (!registration.supportedMimeTypes.any { registered -> mimeTypeMatches(registered, mimeType) }) {
            return ExternalHandoffParseResult.Rejected(
                reason = appStrings.get(R.string.external_handoff_unsupported_mime, mimeType.ifBlank { "unknown" }),
                sourceLabel = sourceLabel,
                trustState = ExternalTrustState.DENIED,
                trustReason = trustReason,
            )
        }

        val sharedText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        val attachment = intent.getParcelableExtraCompat(Intent.EXTRA_STREAM, Uri::class.java)
            ?.let { uri ->
                attachmentStore.importAttachment(
                    sourceUri = uri,
                    preferredKind = when {
                        mimeType.startsWith("image/") -> RuntimeAttachmentKind.IMAGE
                        mimeType.startsWith("audio/") -> RuntimeAttachmentKind.AUDIO
                        else -> null
                    },
                    sourceLabel = sourceLabel,
                ).getOrNull()
            }
        if (sharedText.isBlank() && attachment == null) {
            return ExternalHandoffParseResult.Rejected(
                reason = appStrings.get(R.string.multimodal_external_missing_payload),
                sourceLabel = sourceLabel,
                trustState = ExternalTrustState.DENIED,
                trustReason = trustReason,
            )
        }

        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)?.trim().takeUnless { it.isNullOrBlank() }
        val handoffId = "handoff-${System.currentTimeMillis()}"
        val callerIdentity = CallerContractIdentity(
            originApp = packageName ?: "external.share",
            packageName = packageName,
            sourceLabel = sourceLabel,
            trustState = ExternalTrustState.UNVERIFIED,
            trustReason = trustReason,
            referrerUri = referrerUri?.toString(),
        )
        val attachments = listOfNotNull(attachment)
        val uriGrantSummary = if (attachments.isEmpty()) {
            UriGrantSummary.none(appStrings.get(R.string.interop_grant_none))
        } else {
            UriGrantSummary(
                grantCount = attachments.size,
                grantedMimeFamilies = attachments.map { it.mimeType.substringBefore('/') }.distinct(),
                grantMode = UriGrantMode.READ_ONLY,
                expiresWithSession = true,
                summaryText = appStrings.interopGrantSummary(
                    grantCount = attachments.size,
                    mimeFamilies = attachments.map { it.mimeType },
                    grantMode = UriGrantMode.READ_ONLY,
                    expiresWithSession = true,
                ),
            )
        }
        val compatibility = InteropCompatibilitySignal(
            compatibilityReason = appStrings.get(R.string.interop_compatibility_supported),
        )
        val envelope = InteropRequestEnvelope(
            interopRequestId = handoffId,
            entryType = InteropEntryType.SHARE_HANDOFF,
            callerIdentity = callerIdentity,
            sharedText = sharedText,
            sharedSubject = subject,
            attachments = attachments,
            uriGrantSummary = uriGrantSummary,
            compatibilitySignal = compatibility,
            rawSourceSummary = listOfNotNull(
                packageName,
                referrerUri?.toString(),
                registration.entryType.name.lowercase(),
            ).joinToString(separator = " · "),
        )
        return ExternalHandoffParseResult.Accepted(
            envelope = envelope,
        )
    }
}

private fun <T : Any> Intent.getParcelableExtraCompat(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, clazz)
    } else {
        @Suppress("DEPRECATION")
        (getParcelableExtra(name) as Any?)?.takeIf(clazz::isInstance)?.let(clazz::cast)
    }
}

private fun mimeTypeMatches(registered: String, actual: String): Boolean {
    if (registered == actual) return true
    if (!registered.contains('*')) return false
    return when {
        registered == "image/*" -> actual.startsWith("image/")
        registered == "audio/*" -> actual.startsWith("audio/")
        else -> false
    }
}

private fun Uri.packageNameOrNull(): String? {
    return when (scheme) {
        "android-app" -> host
        else -> null
    }?.takeIf { it.isNotBlank() }
}
