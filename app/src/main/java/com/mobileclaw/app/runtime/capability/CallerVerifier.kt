package com.mobileclaw.app.runtime.capability

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.governance.GovernanceGrantState
import com.mobileclaw.app.runtime.governance.GovernanceRepository
import com.mobileclaw.app.runtime.governance.GovernanceTrustMode
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallerVerifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val governanceRepository: GovernanceRepository,
    private val appStrings: AppStrings,
) {
    private val trustedAliases = setOf("agent_workspace")

    suspend fun verify(
        request: RuntimeRequest,
        capabilityId: String,
    ): CallerIdentity {
        val restricted = capabilityId != "generate.reply"
        val origin = request.originApp
        val sourceMetadata = request.sourceMetadata
        val packageName = sourceMetadata?.packageName ?: origin.takeIf { "." in it }

        val baseIdentity = if (!restricted) {
            CallerIdentity(
                callerId = origin,
                originApp = origin,
                sourceLabel = sourceMetadata?.sourceLabel ?: origin,
                trustState = if (sourceMetadata == null) {
                    CallerTrustState.TRUSTED
                } else {
                    CallerTrustState.UNVERIFIED
                },
                trustReason = sourceMetadata?.trustReason
                    ?: appStrings.get(R.string.bridge_caller_trusted),
                allowsRestrictedCapabilities = true,
                packageName = packageName,
                signatureDigest = sourceMetadata?.packageSignatureDigest,
                referrerUri = sourceMetadata?.referrerUri,
                contractVersion = sourceMetadata?.contractVersion,
                grantSummary = sourceMetadata?.grantSummary.orEmpty(),
                requestedScopeIds = sourceMetadata?.requestedScopeIds.orEmpty(),
            )
        } else if (origin in trustedAliases || origin == context.packageName) {
            CallerIdentity(
                callerId = origin,
                originApp = origin,
                sourceLabel = sourceMetadata?.sourceLabel ?: origin,
                trustState = CallerTrustState.TRUSTED,
                trustReason = appStrings.get(R.string.bridge_caller_trusted_restricted),
                allowsRestrictedCapabilities = true,
                packageName = context.packageName,
                signatureDigest = packageSignatureDigest(context.packageName),
                referrerUri = sourceMetadata?.referrerUri,
                contractVersion = sourceMetadata?.contractVersion,
                grantSummary = sourceMetadata?.grantSummary.orEmpty(),
                requestedScopeIds = sourceMetadata?.requestedScopeIds.orEmpty(),
            )
        } else if (packageName == null) {
            CallerIdentity(
                callerId = origin,
                originApp = origin,
                sourceLabel = sourceMetadata?.sourceLabel ?: origin,
                trustState = CallerTrustState.DENIED,
                trustReason = sourceMetadata?.trustReason
                    ?: appStrings.get(R.string.bridge_caller_untrusted),
                allowsRestrictedCapabilities = false,
                referrerUri = sourceMetadata?.referrerUri,
                contractVersion = sourceMetadata?.contractVersion,
                grantSummary = sourceMetadata?.grantSummary.orEmpty(),
                requestedScopeIds = sourceMetadata?.requestedScopeIds.orEmpty(),
            )
        } else {
            val packageInfo = packageInfoOrNull(packageName)
            if (packageInfo == null) {
                CallerIdentity(
                    callerId = origin,
                    originApp = origin,
                    sourceLabel = sourceMetadata?.sourceLabel ?: packageName,
                    trustState = CallerTrustState.DENIED,
                    trustReason = appStrings.get(R.string.bridge_caller_package_missing, packageName),
                    allowsRestrictedCapabilities = false,
                    packageName = packageName,
                    referrerUri = sourceMetadata?.referrerUri,
                    contractVersion = sourceMetadata?.contractVersion,
                    grantSummary = sourceMetadata?.grantSummary.orEmpty(),
                    requestedScopeIds = sourceMetadata?.requestedScopeIds.orEmpty(),
                )
            } else {
                val originDigest = packageInfo.signingCertificateDigest()
                val appDigest = packageSignatureDigest(context.packageName)
                val trusted = originDigest != null && appDigest != null && originDigest == appDigest
                CallerIdentity(
                    callerId = packageName,
                    originApp = origin,
                    sourceLabel = sourceMetadata?.sourceLabel ?: packageName,
                    trustState = if (trusted) CallerTrustState.TRUSTED else CallerTrustState.DENIED,
                    trustReason = if (trusted) {
                        appStrings.get(R.string.bridge_caller_signature_verified, packageName)
                    } else {
                        appStrings.get(R.string.bridge_caller_signature_mismatch, packageName)
                    },
                    allowsRestrictedCapabilities = trusted,
                    packageName = packageName,
                    signatureDigest = originDigest,
                    referrerUri = sourceMetadata?.referrerUri,
                    contractVersion = sourceMetadata?.contractVersion,
                    grantSummary = sourceMetadata?.grantSummary.orEmpty(),
                    requestedScopeIds = sourceMetadata?.requestedScopeIds.orEmpty(),
                )
            }
        }

        val governance = governanceRepository.resolveSnapshot(baseIdentity, capabilityId)
            ?: return baseIdentity
        return when {
            governance.effectiveTrustMode == GovernanceTrustMode.DENIED ||
                governance.scopeGrantState == GovernanceGrantState.DENY -> {
                baseIdentity.copy(
                    trustState = CallerTrustState.DENIED,
                    trustReason = governance.decisionExplanation,
                    allowsRestrictedCapabilities = false,
                )
            }

            governance.effectiveTrustMode == GovernanceTrustMode.ASK_EACH_TIME ||
                governance.scopeGrantState == GovernanceGrantState.ASK -> {
                baseIdentity.copy(
                    trustState = CallerTrustState.UNVERIFIED,
                    trustReason = governance.decisionExplanation,
                    allowsRestrictedCapabilities = true,
                )
            }

            else -> {
                baseIdentity.copy(
                    trustState = CallerTrustState.TRUSTED,
                    trustReason = governance.decisionExplanation,
                    allowsRestrictedCapabilities = true,
                )
            }
        }
    }

    private fun packageSignatureDigest(packageName: String): String? {
        return packageInfoOrNull(packageName)?.signingCertificateDigest()
    }

    @Suppress("DEPRECATION")
    private fun packageInfoOrNull(packageName: String): PackageInfo? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
                )
            } else {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
            }
        }.getOrNull()
    }
}

private fun PackageInfo.signingCertificateDigest(): String? {
    val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val signing = signingInfo ?: return null
        if (signing.hasMultipleSigners()) {
            signing.apkContentsSigners
        } else {
            signing.signingCertificateHistory
        }
    } else {
        @Suppress("DEPRECATION")
        signatures
    } ?: return null

    val first = signatures.firstOrNull() ?: return null
    val digest = MessageDigest.getInstance("SHA-256").digest(first.toByteArray())
    return digest.joinToString(separator = ":") { byte -> "%02X".format(byte) }
}
