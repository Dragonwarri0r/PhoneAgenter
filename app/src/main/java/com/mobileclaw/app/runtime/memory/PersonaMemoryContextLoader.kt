package com.mobileclaw.app.runtime.memory

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
        val contextSummary = retrievedContext.toActiveContextSummary(appStrings)
        return RuntimeContextPayload(
            summary = buildString {
                append(contextSummary.headline)
                if (ingestion.contributions.isNotEmpty()) {
                    append(" · ")
                    append(ingestion.contributions.joinToString { it.summary })
                }
            },
            transcriptTurnCount = request.transcriptContext.size,
            hasTranscriptContext = request.transcriptContext.isNotEmpty(),
            personaSummary = contextSummary.personaSummary,
            activeContextSummary = contextSummary,
            selectedMemoryIds = retrievedContext.selectedMemoryItems.map { it.memoryId },
            systemSourceDescriptors = ingestion.descriptors,
            systemSourceContributions = ingestion.contributions,
        )
    }
}
