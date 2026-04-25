package com.mobileclaw.app.runtime.interop

import android.net.Uri
import com.mobileclaw.app.runtime.capability.CallerIdentity
import com.mobileclaw.app.runtime.capability.CallerTrustState
import com.mobileclaw.app.runtime.capability.StandardToolCatalog
import com.mobileclaw.app.runtime.governance.CallerGovernanceRecord
import com.mobileclaw.app.runtime.governance.GovernanceCenterSnapshot
import com.mobileclaw.app.runtime.governance.GovernanceDecisionSnapshot
import com.mobileclaw.app.runtime.governance.GovernanceGrantState
import com.mobileclaw.app.runtime.governance.GovernanceRepository
import com.mobileclaw.app.runtime.governance.GovernanceTrustMode
import com.mobileclaw.app.runtime.governance.ScopeGrantRecord
import com.mobileclaw.app.runtime.ingress.CallableInteropMapper
import com.mobileclaw.app.runtime.ingress.ExternalRuntimeRequestMapper
import com.mobileclaw.app.runtime.localchat.LocalModelCatalog
import com.mobileclaw.app.runtime.localchat.LocalModelProfile
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus
import com.mobileclaw.app.runtime.localchat.ModelHealthSnapshot
import com.mobileclaw.app.runtime.localchat.ModelImportResult
import com.mobileclaw.app.runtime.localchat.ModelModalityCapabilities
import com.mobileclaw.app.runtime.localchat.SessionResetResult
import com.mobileclaw.app.runtime.session.RuntimeRequest
import com.mobileclaw.app.runtime.session.RuntimeSessionEvent
import com.mobileclaw.app.runtime.session.RuntimeSessionFacade
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.ExternalTrustState
import com.mobileclaw.interop.contract.InteropIds
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HubCapabilityInvocationServiceTest {
    private val appStrings = AppStrings(RuntimeEnvironment.getApplication())
    private val governanceRepository = InvocationGovernanceRepository()
    private val runtimeSessionFacade = FakeRuntimeSessionFacade()
    private val taskService = HubInteropTaskService(HubInteropCompatibilityService())
    private val standardToolCatalog = StandardToolCatalog(appStrings)
    private val authorizationService = HubInteropAuthorizationService(
        compatibilityService = HubInteropCompatibilityService(),
        governanceRepository = governanceRepository,
        standardToolCatalog = standardToolCatalog,
        appStrings = appStrings,
    )
    private val subject = HubCapabilityInvocationService(
        compatibilityService = HubInteropCompatibilityService(),
        callableInteropMapper = CallableInteropMapper(appStrings),
        externalRuntimeRequestMapper = ExternalRuntimeRequestMapper(),
        runtimeSessionFacade = runtimeSessionFacade,
        localModelCatalog = FakeLocalModelCatalog(),
        standardToolCatalog = standardToolCatalog,
        authorizationService = authorizationService,
        taskService = taskService,
        appStrings = appStrings,
    )

    @Test
    fun `invoke requires grant before entering runtime`() = runBlocking {
        val response = subject.invoke(generateReplyRequest())

        assertEquals(HubInteropStatus.AUTHORIZATION_REQUIRED, response.status)
        assertNull(response.taskDescriptor)
        assertNull(runtimeSessionFacade.lastRequest)
    }

    @Test
    fun `invoke returns task descriptor after grant is allowed`() = runBlocking {
        governanceRepository.updateScopeGrant(
            callerId = "com.example.probe",
            scopeId = InteropIds.Scope.REPLY_GENERATE,
            grantState = GovernanceGrantState.ALLOW,
        )

        val response = subject.invoke(generateReplyRequest())

        assertEquals(HubInteropStatus.OK, response.status)
        val taskDescriptor = assertNotNull(response.taskDescriptor)
        assertEquals(InteropIds.Capability.GENERATE_REPLY, runtimeSessionFacade.lastRequest?.requestedCapabilities?.single()?.capabilityId)
        assertEquals("hub_interop_host", runtimeSessionFacade.lastRequest?.workspaceId)
        assertEquals("task:req-invoke-1", taskDescriptor.handle.opaqueValue)
    }

    private fun generateReplyRequest(): InvocationBundles.Request {
        return InvocationBundles.Request(
            requestId = "req-invoke-1",
            callerIdentity = CallerContractIdentity(
                originApp = "probe.app",
                packageName = "com.example.probe",
                sourceLabel = "Probe App",
                trustState = ExternalTrustState.UNVERIFIED,
                trustReason = "probe_test",
            ),
            capabilityId = InteropIds.Capability.GENERATE_REPLY,
            input = "Draft a reply saying hello",
        )
    }
}

private class FakeLocalModelCatalog : LocalModelCatalog {
    override val models: Flow<List<LocalModelProfile>> = flowOf(
        listOf(
            LocalModelProfile(
                modelId = "local-ready-model",
                displayName = "Ready Model",
                providerLabel = "Local",
                availabilityStatus = ModelAvailabilityStatus.READY,
                statusMessage = "ready",
                isSelectable = true,
                modalityCapabilities = ModelModalityCapabilities(),
            ),
        ),
    )

    override fun observeHealth(modelId: String): Flow<ModelHealthSnapshot> {
        return flowOf(
            ModelHealthSnapshot(
                modelId = modelId,
                availabilityStatus = ModelAvailabilityStatus.READY,
                headline = "ready",
                supportingText = "ready",
            ),
        )
    }

    override suspend fun selectModel(modelId: String): LocalModelProfile? {
        return modelsValue().firstOrNull { it.modelId == modelId }
    }

    override suspend fun clearModelRuntimeFailure(modelId: String): LocalModelProfile? {
        return modelsValue().firstOrNull { it.modelId == modelId }
    }

    override suspend fun importModel(sourceUri: Uri): ModelImportResult {
        return ModelImportResult.Failure("not_used")
    }

    override suspend fun updateModelCapabilities(
        modelId: String,
        supportsImage: Boolean,
        supportsAudio: Boolean,
    ): LocalModelProfile? {
        return modelsValue().firstOrNull { it.modelId == modelId }
    }

    private suspend fun modelsValue(): List<LocalModelProfile> {
        var result: List<LocalModelProfile> = emptyList()
        models.collect { value ->
            result = value
        }
        return result
    }
}

private class FakeRuntimeSessionFacade : RuntimeSessionFacade {
    var lastRequest: RuntimeRequest? = null

    override fun submitRequest(request: RuntimeRequest): Flow<RuntimeSessionEvent> {
        lastRequest = request
        return emptyFlow()
    }

    override suspend fun resetModelSession(modelId: String): SessionResetResult? = null

    override suspend fun resolveApprovalRequest(
        approvalRequestId: String,
        outcome: com.mobileclaw.app.runtime.policy.ApprovalOutcomeType,
    ): Boolean = false
}

private class InvocationGovernanceRepository : GovernanceRepository {
    private val callers = linkedMapOf<String, CallerGovernanceRecord>()
    private val grants = linkedMapOf<Pair<String, String>, ScopeGrantRecord>()

    override fun observeGovernanceCenter(limit: Int): Flow<GovernanceCenterSnapshot> {
        return flowOf(GovernanceCenterSnapshot(emptyList(), emptyList()))
    }

    override suspend fun getCallerRecord(callerId: String): CallerGovernanceRecord? {
        return callers[callerId]
    }

    override suspend fun getScopeGrant(
        callerId: String,
        scopeId: String,
    ): ScopeGrantRecord? {
        return grants[callerId to scopeId]
    }

    override suspend fun recordCallerObservation(
        callerIdentity: CallerIdentity,
        displayLabel: String,
        decisionSummary: String,
        toolId: String?,
        toolDisplayName: String?,
    ) {
        callers[callerIdentity.callerId] = CallerGovernanceRecord(
            callerId = callerIdentity.callerId,
            originApp = callerIdentity.originApp,
            displayLabel = displayLabel,
            packageName = callerIdentity.packageName,
            signatureDigest = callerIdentity.signatureDigest,
            trustMode = when (callerIdentity.trustState) {
                CallerTrustState.TRUSTED -> GovernanceTrustMode.TRUSTED
                CallerTrustState.DENIED -> GovernanceTrustMode.DENIED
                CallerTrustState.UNVERIFIED -> GovernanceTrustMode.ASK_EACH_TIME
            },
            trustReason = callerIdentity.trustReason,
            lastSeenAtEpochMillis = System.currentTimeMillis(),
            lastDecisionSummary = decisionSummary,
        )
    }

    override suspend fun updateTrustMode(
        callerId: String,
        trustMode: GovernanceTrustMode,
    ) {
        val existing = callers[callerId] ?: return
        callers[callerId] = existing.copy(
            trustMode = trustMode,
            lastSeenAtEpochMillis = System.currentTimeMillis(),
        )
    }

    override suspend fun updateScopeGrant(
        callerId: String,
        scopeId: String,
        grantState: GovernanceGrantState,
    ) {
        val key = callerId to scopeId
        grants[key] = grants[key]?.copy(
            grantState = grantState,
            updatedAtEpochMillis = System.currentTimeMillis(),
        ) ?: ScopeGrantRecord(
            grantId = "grant-$callerId-$scopeId",
            callerId = callerId,
            scopeId = scopeId,
            grantState = grantState,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    override suspend fun resolveSnapshot(
        callerIdentity: CallerIdentity,
        capabilityId: String,
    ): GovernanceDecisionSnapshot? = null
}
