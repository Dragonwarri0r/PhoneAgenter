package com.mobileclaw.app.runtime.interop

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
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.ExternalTrustState
import com.mobileclaw.interop.contract.InteropIds
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HubInteropAuthorizationServiceTest {
    private val repository = FakeGovernanceRepository()
    private val appStrings = AppStrings(RuntimeEnvironment.getApplication())
    private val subject = HubInteropAuthorizationService(
        compatibilityService = HubInteropCompatibilityService(),
        governanceRepository = repository,
        standardToolCatalog = StandardToolCatalog(appStrings),
        appStrings = appStrings,
    )

    @Test
    fun `request authorization creates pending inbound grant`() = runBlocking {
        val response = subject.requestAuthorization(generateReplyRequest())

        assertEquals(HubInteropStatus.AUTHORIZATION_PENDING, response.status)
        assertFalse(assertNotNull(response.grantDescriptor).isActive)
        assertEquals(
            GovernanceGrantState.ASK,
            repository.getScopeGrant("com.example.probe", InteropIds.Scope.REPLY_GENERATE)?.grantState,
        )
    }

    @Test
    fun `grant status becomes ok after governance allow`() = runBlocking {
        repository.updateScopeGrant(
            callerId = "com.example.probe",
            scopeId = InteropIds.Scope.REPLY_GENERATE,
            grantState = GovernanceGrantState.ALLOW,
        )

        val response = subject.getGrantStatus(generateReplyRequest())

        assertEquals(HubInteropStatus.OK, response.status)
        assertEquals(true, assertNotNull(response.grantDescriptor).isActive)
    }

    @Test
    fun `grant status becomes ok when caller is trusted`() = runBlocking {
        subject.requestAuthorization(generateReplyRequest())
        repository.updateTrustMode(
            callerId = "com.example.probe",
            trustMode = GovernanceTrustMode.TRUSTED,
        )

        val response = subject.getGrantStatus(generateReplyRequest())

        assertEquals(HubInteropStatus.OK, response.status)
        assertEquals(true, assertNotNull(response.grantDescriptor).isActive)
    }

    @Test
    fun `caller denied overrides allowed scope grant`() = runBlocking {
        subject.requestAuthorization(generateReplyRequest())
        repository.updateScopeGrant(
            callerId = "com.example.probe",
            scopeId = InteropIds.Scope.REPLY_GENERATE,
            grantState = GovernanceGrantState.ALLOW,
        )
        repository.updateTrustMode(
            callerId = "com.example.probe",
            trustMode = GovernanceTrustMode.DENIED,
        )

        val response = subject.getGrantStatus(generateReplyRequest())

        assertEquals(HubInteropStatus.FORBIDDEN, response.status)
        assertEquals(false, assertNotNull(response.grantDescriptor).isActive)
    }

    @Test
    fun `revoke grant downgrades authorization to denied`() = runBlocking {
        repository.updateScopeGrant(
            callerId = "com.example.probe",
            scopeId = InteropIds.Scope.REPLY_GENERATE,
            grantState = GovernanceGrantState.ALLOW,
        )

        val response = subject.revokeGrant(generateReplyRequest())

        assertEquals(HubInteropStatus.OK, response.status)
        assertEquals(
            GovernanceGrantState.DENY,
            repository.getScopeGrant("com.example.probe", InteropIds.Scope.REPLY_GENERATE)?.grantState,
        )
    }

    private fun generateReplyRequest(): AuthorizationBundles.Request {
        return AuthorizationBundles.Request(
            requestId = "req-interop-auth",
            callerIdentity = CallerContractIdentity(
                originApp = "probe.app",
                packageName = "com.example.probe",
                sourceLabel = "Probe App",
                trustState = ExternalTrustState.UNVERIFIED,
                trustReason = "probe_test",
            ),
            capabilityId = InteropIds.Capability.GENERATE_REPLY,
        )
    }
}

private class FakeGovernanceRepository : GovernanceRepository {
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
