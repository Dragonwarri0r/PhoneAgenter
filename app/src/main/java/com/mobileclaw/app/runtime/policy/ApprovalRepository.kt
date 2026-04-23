package com.mobileclaw.app.runtime.policy

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.action.ActionNormalizationResult
import com.mobileclaw.app.runtime.capability.ToolDescriptor
import com.mobileclaw.app.runtime.capability.ToolPreviewFactory
import com.mobileclaw.app.runtime.capability.ToolVisibilitySnapshot
import com.mobileclaw.app.runtime.provider.ExplicitReadToolRequest
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ApprovalRepository @Inject constructor(
    private val approvalDao: ApprovalDao,
    private val appStrings: AppStrings,
    private val toolPreviewFactory: ToolPreviewFactory,
) {
    private val outcomeMutex = Mutex()

    suspend fun createApprovalRequest(
        sessionId: String,
        decision: PolicyDecision,
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
        scope: ActionScope,
        toolDescriptor: ToolDescriptor,
        visibilitySnapshot: ToolVisibilitySnapshot?,
        normalization: ActionNormalizationResult? = null,
        explicitReadRequest: ExplicitReadToolRequest? = null,
    ): ApprovalRequest {
        val executionPreview = toolPreviewFactory.createPreview(
            descriptor = toolDescriptor,
            request = request,
            contextPayload = contextPayload,
            structuredPreview = normalization?.preview,
            visibilitySnapshot = visibilitySnapshot,
            explicitReadRequest = explicitReadRequest,
        )
        val approvalRequest = ApprovalRequest(
            approvalRequestId = "approval-${System.currentTimeMillis()}-$sessionId",
            sessionId = sessionId,
            decisionId = decision.decisionId,
            toolId = toolDescriptor.toolId,
            toolDisplayName = executionPreview.displayName,
            sideEffectLabel = appStrings.toolSideEffectLabel(executionPreview.sideEffectType),
            scopeLines = executionPreview.scopeLines,
            previewLines = executionPreview.previewFieldLines,
            title = appStrings.get(R.string.approval_title, toolDescriptor.displayName),
            summary = appStrings.get(
                R.string.approval_summary,
                toolDescriptor.displayName,
                request.userInput.trim(),
            ),
            previewPayload = buildPreviewPayload(
                request = request,
                contextPayload = contextPayload,
                scope = scope,
                normalization = normalization,
                explicitReadRequest = explicitReadRequest,
                toolDescriptor = toolDescriptor,
                executionPreview = executionPreview,
            ),
            primaryActionLabel = appStrings.get(R.string.approval_action_approve),
            secondaryActionLabel = appStrings.get(R.string.approval_action_reject),
            localeTag = appStrings.localeTag(),
            status = ApprovalRequestStatus.PENDING,
        )
        approvalDao.upsertApprovalRequest(approvalRequest)
        return approvalRequest
    }

    suspend fun recordOutcome(
        approvalRequestId: String,
        sessionId: String,
        outcome: ApprovalOutcomeType,
        reason: String? = null,
        actor: String = "local_device_user",
    ): ApprovalOutcome {
        return outcomeMutex.withLock {
            val existing = approvalDao.getOutcomeForRequest(approvalRequestId)
            if (existing != null) {
                approvalDao.updateApprovalStatus(
                    approvalRequestId = approvalRequestId,
                    status = ApprovalRequestStatus.RESOLVED,
                )
                existing
            } else {
                val approvalOutcome = ApprovalOutcome(
                    approvalOutcomeId = "approval-outcome-${System.currentTimeMillis()}-$approvalRequestId",
                    approvalRequestId = approvalRequestId,
                    sessionId = sessionId,
                    outcome = outcome,
                    actor = actor,
                    reason = reason,
                )
                approvalDao.upsertApprovalOutcome(approvalOutcome)
                approvalDao.updateApprovalStatus(
                    approvalRequestId = approvalRequestId,
                    status = ApprovalRequestStatus.RESOLVED,
                )
                approvalOutcome
            }
        }
    }

    suspend fun expirePendingRequestsOlderThan(
        createdBeforeEpochMillis: Long,
        timeoutReason: String,
    ): List<ApprovalOutcome> {
        return outcomeMutex.withLock {
            approvalDao.listRequestsByStatusOlderThan(
                status = ApprovalRequestStatus.PENDING,
                createdBeforeEpochMillis = createdBeforeEpochMillis,
            ).map { request ->
                approvalDao.getOutcomeForRequest(request.approvalRequestId)?.also {
                    approvalDao.updateApprovalStatus(
                        approvalRequestId = request.approvalRequestId,
                        status = ApprovalRequestStatus.RESOLVED,
                    )
                } ?: run {
                    val expiredOutcome = ApprovalOutcome(
                        approvalOutcomeId = "approval-outcome-${System.currentTimeMillis()}-${request.approvalRequestId}",
                        approvalRequestId = request.approvalRequestId,
                        sessionId = request.sessionId,
                        outcome = ApprovalOutcomeType.ABANDONED,
                        actor = "system_timeout",
                        reason = timeoutReason,
                    )
                    approvalDao.upsertApprovalOutcome(expiredOutcome)
                    approvalDao.updateApprovalStatus(
                        approvalRequestId = request.approvalRequestId,
                        status = ApprovalRequestStatus.RESOLVED,
                    )
                    expiredOutcome
                }
            }
        }
    }

    suspend fun latestRequestForSession(sessionId: String): ApprovalRequest? {
        return approvalDao.getLatestRequestForSession(sessionId)
    }

    suspend fun createWorkflowApprovalRequest(
        sessionId: String,
        toolId: String,
        toolDisplayName: String,
        sideEffectLabel: String,
        scopeLines: List<String>,
        previewLines: List<String>,
        title: String,
        summary: String,
        previewPayload: String,
    ): ApprovalRequest {
        val approvalRequest = ApprovalRequest(
            approvalRequestId = "approval-${System.currentTimeMillis()}-$sessionId",
            sessionId = sessionId,
            decisionId = "workflow-decision-$sessionId-${System.currentTimeMillis()}",
            toolId = toolId,
            toolDisplayName = toolDisplayName,
            sideEffectLabel = sideEffectLabel,
            scopeLines = scopeLines,
            previewLines = previewLines,
            title = title,
            summary = summary,
            previewPayload = previewPayload,
            primaryActionLabel = appStrings.get(R.string.approval_action_approve),
            secondaryActionLabel = appStrings.get(R.string.approval_action_reject),
            localeTag = appStrings.localeTag(),
            status = ApprovalRequestStatus.PENDING,
        )
        approvalDao.upsertApprovalRequest(approvalRequest)
        return approvalRequest
    }

    private fun buildPreviewPayload(
        request: RuntimeRequest,
        contextPayload: RuntimeContextPayload,
        scope: ActionScope,
        normalization: ActionNormalizationResult?,
        explicitReadRequest: ExplicitReadToolRequest?,
        toolDescriptor: ToolDescriptor,
        executionPreview: com.mobileclaw.app.runtime.capability.ToolExecutionPreview,
    ): String {
        val structuredPreview = normalization?.preview
        return if (structuredPreview != null) {
            appStrings.get(
                R.string.approval_preview_tool_payload,
                toolDescriptor.displayName,
                appStrings.toolSideEffectLabel(executionPreview.sideEffectType),
                appStrings.payloadCompletenessLabel(structuredPreview.completenessState),
            )
        } else if (explicitReadRequest != null) {
            appStrings.get(
                R.string.approval_preview_read_payload,
                toolDescriptor.displayName,
                explicitReadRequest.queryScope.displayLabel,
                explicitReadRequest.queryText,
            )
        } else {
            appStrings.get(
                R.string.approval_preview_payload,
                request.userInput.trim(),
                contextPayload.summary,
                contextPayload.selectedMemoryIds.size,
            ) + "\n" + appStrings.get(
                R.string.approval_preview_tool_fallback,
                toolDescriptor.displayName,
                appStrings.actionScopeLabel(scope),
            )
        }
    }
}
