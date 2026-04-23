package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.contribution.ContextContribution
import com.mobileclaw.app.runtime.contribution.ContributionLifecyclePoint
import com.mobileclaw.app.runtime.contribution.ContributionOutcomeRecord
import com.mobileclaw.app.runtime.contribution.ContributionOutcomeState
import com.mobileclaw.app.runtime.contribution.KnowledgeRequestContribution
import com.mobileclaw.app.runtime.contribution.RuntimeContributionRegistry
import com.mobileclaw.app.runtime.knowledge.KnowledgeRetrievalQuery
import com.mobileclaw.app.runtime.knowledge.KnowledgeRetrievalService
import com.mobileclaw.app.runtime.session.RuntimeContextLoader
import com.mobileclaw.app.runtime.session.RuntimeContextPayload
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.systemsource.SystemSourceIngestionService
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonaMemoryContextLoader @Inject constructor(
    private val systemSourceIngestionService: SystemSourceIngestionService,
    private val memoryRetrievalService: MemoryRetrievalService,
    private val knowledgeRetrievalService: KnowledgeRetrievalService,
    private val contributionRegistry: RuntimeContributionRegistry,
    private val appStrings: AppStrings,
) : RuntimeContextLoader {
    override suspend fun load(request: RuntimeRequest): RuntimeContextPayload {
        val ingestion = systemSourceIngestionService.ingestForRequest(request)
        val retrievedContext = memoryRetrievalService.retrieveContext(
            RetrievalQuery(
                requestId = request.requestId,
                userInput = request.userInput,
                originApp = request.originApp,
                subjectKey = request.subjectKey,
                deviceId = request.deviceId,
                allowPrivate = true,
                maxItems = 4,
            ),
        )
        val retrievedKnowledge = knowledgeRetrievalService.retrieve(
            KnowledgeRetrievalQuery(
                requestId = request.requestId,
                userInput = request.userInput,
            ),
        )
        val memoryContributorEnabled = contributionRegistry.isEnabled(
            RuntimeContributionRegistry.MEMORY_ACTIVE_CONTEXT_ID,
        )
        val effectiveContext = if (memoryContributorEnabled) {
            retrievedContext
        } else {
            retrievedContext.copy(
                selectedMemoryItems = emptyList(),
                excludedCount = retrievedContext.totalEligibleCount,
            )
        }
        val contextSummary = effectiveContext.toActiveContextSummary(appStrings)
        val contributionOutcomes = buildList {
            add(
                ContributionOutcomeRecord(
                    contributionId = RuntimeContributionRegistry.MEMORY_ACTIVE_CONTEXT_ID,
                    requestId = request.requestId,
                    lifecyclePoint = ContributionLifecyclePoint.CONTEXT_ATTACH,
                    outcomeState = when {
                        !memoryContributorEnabled -> ContributionOutcomeState.SKIPPED
                        effectiveContext.selectedMemoryItems.isNotEmpty() -> ContributionOutcomeState.APPLIED
                        effectiveContext.totalEligibleCount > 0 -> ContributionOutcomeState.SKIPPED
                        else -> ContributionOutcomeState.UNAVAILABLE
                    },
                    summary = when {
                        !memoryContributorEnabled -> appStrings.get(R.string.runtime_contribution_disabled_for_request)
                        effectiveContext.selectedMemoryItems.isNotEmpty() -> contextSummary.headline
                        else -> appStrings.get(R.string.runtime_contribution_no_matching_context)
                    },
                    details = contextSummary.retrievalSummary,
                    provenanceSummary = appStrings.get(R.string.runtime_contribution_memory_title),
                ),
            )
            ingestion.descriptors.forEach { descriptor ->
                val contributionId = contributionRegistry.systemSourceContributionId(descriptor.sourceId)
                val appliedContribution = ingestion.contributions.firstOrNull { it.sourceId == descriptor.sourceId }
                val contributorEnabled = contributionRegistry.isEnabled(contributionId)
                add(
                    ContributionOutcomeRecord(
                        contributionId = contributionId,
                        requestId = request.requestId,
                        lifecyclePoint = ContributionLifecyclePoint.CONTEXT_ATTACH,
                        outcomeState = when {
                            !contributorEnabled -> ContributionOutcomeState.SKIPPED
                            !descriptor.isGranted -> ContributionOutcomeState.UNAVAILABLE
                            appliedContribution != null -> ContributionOutcomeState.APPLIED
                            else -> ContributionOutcomeState.SKIPPED
                        },
                        summary = when {
                            !contributorEnabled -> appStrings.get(R.string.runtime_contribution_disabled_for_request)
                            !descriptor.isGranted -> appStrings.get(R.string.runtime_contribution_permission_required)
                            appliedContribution != null -> appliedContribution.summary
                            else -> appStrings.get(R.string.runtime_contribution_no_matching_context)
                        },
                        details = descriptor.availabilitySummary,
                        provenanceSummary = descriptor.displayName,
                    ),
                )
            }
            add(
                ContributionOutcomeRecord(
                    contributionId = RuntimeContributionRegistry.KNOWLEDGE_RETRIEVAL_ID,
                    requestId = request.requestId,
                    lifecyclePoint = ContributionLifecyclePoint.CONTEXT_ATTACH,
                    outcomeState = when {
                        retrievedKnowledge.supportSummaries.isNotEmpty() -> ContributionOutcomeState.APPLIED
                        retrievedKnowledge.searchableAssetCount > 0 -> ContributionOutcomeState.SKIPPED
                        retrievedKnowledge.excludedAssetCount > 0 || retrievedKnowledge.unavailableAssetCount > 0 ->
                            ContributionOutcomeState.DEGRADED
                        else -> ContributionOutcomeState.UNAVAILABLE
                    },
                    summary = when {
                        retrievedKnowledge.supportSummaries.isNotEmpty() ->
                            retrievedKnowledge.supportSummaries.joinToString(separator = " / ") { it.summary }
                        retrievedKnowledge.limitationSummary.isNotBlank() ->
                            retrievedKnowledge.limitationSummary
                        retrievedKnowledge.searchableAssetCount > 0 ->
                            appStrings.get(R.string.knowledge_retrieval_no_match)
                        else -> appStrings.get(R.string.knowledge_retrieval_no_assets)
                    },
                    details = retrievedKnowledge.citations.joinToString(separator = " / ") { it.sourceLabel },
                    policyReason = retrievedKnowledge.limitationSummary,
                    provenanceSummary = appStrings.get(R.string.runtime_contribution_knowledge_title),
                ),
            )
        }
        val contextContributions = buildList {
            if (memoryContributorEnabled && effectiveContext.selectedMemoryItems.isNotEmpty()) {
                add(
                    ContextContribution(
                        contributionId = RuntimeContributionRegistry.MEMORY_ACTIVE_CONTEXT_ID,
                        summary = contextSummary.headline,
                        provenanceLabel = appStrings.get(R.string.runtime_contribution_memory_title),
                        scopeLabel = appStrings.get(R.string.runtime_contribution_point_context_attach),
                        privacyLabel = contextSummary.retrievalSummary,
                        attachedAtLifecyclePoint = ContributionLifecyclePoint.CONTEXT_ATTACH,
                        isRemovable = true,
                    ),
                )
            }
            ingestion.contributions.forEach { contribution ->
                add(
                    ContextContribution(
                        contributionId = contributionRegistry.systemSourceContributionId(contribution.sourceId),
                        summary = contribution.summary,
                        provenanceLabel = contribution.displayName,
                        scopeLabel = appStrings.contributionLifecyclePointLabel(
                            ContributionLifecyclePoint.CONTEXT_ATTACH,
                        ),
                        privacyLabel = contribution.displayName,
                        attachedAtLifecyclePoint = ContributionLifecyclePoint.CONTEXT_ATTACH,
                        isRemovable = false,
                    ),
                )
            }
        }
        val knowledgeContribution = KnowledgeRequestContribution(
            requestId = request.requestId,
            supportSummaries = retrievedKnowledge.supportSummaries,
            citations = retrievedKnowledge.citations,
            limitationSummary = retrievedKnowledge.limitationSummary,
        )
        return RuntimeContextPayload(
            summary = buildString {
                append(contextSummary.headline)
                if (ingestion.contributions.isNotEmpty()) {
                    append(" · ")
                    append(ingestion.contributions.joinToString { it.summary })
                }
                if (retrievedKnowledge.supportSummaries.isNotEmpty()) {
                    append(" · ")
                    append(retrievedKnowledge.supportSummaries.joinToString { it.summary })
                }
            },
            transcriptTurnCount = request.transcriptContext.size,
            hasTranscriptContext = request.transcriptContext.isNotEmpty(),
            personaSummary = contextSummary.personaSummary,
            activeContextSummary = contextSummary,
            selectedMemoryIds = effectiveContext.selectedMemoryItems.map { it.memoryId },
            systemSourceDescriptors = ingestion.descriptors,
            systemSourceContributions = ingestion.contributions,
            contextContributions = contextContributions,
            knowledgeContribution = knowledgeContribution,
            contributionOutcomes = contributionOutcomes,
        )
    }
}
