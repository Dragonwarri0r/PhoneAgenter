package com.mobileclaw.app.runtime.interop

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.capability.StandardToolCatalog
import com.mobileclaw.app.runtime.governance.GovernanceGrantState
import com.mobileclaw.app.runtime.governance.GovernanceRepository
import com.mobileclaw.app.runtime.governance.GovernanceTrustMode
import com.mobileclaw.app.runtime.policy.ActionScope
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.contract.InteropAuthorizationRequirement
import com.mobileclaw.interop.contract.InteropGrantDescriptor
import com.mobileclaw.interop.contract.InteropGrantDirection
import com.mobileclaw.interop.contract.InteropGrantLifetime
import com.mobileclaw.interop.contract.InteropGrantState
import com.mobileclaw.interop.contract.InteropHandle
import com.mobileclaw.interop.contract.InteropHandleFamily
import javax.inject.Inject
import javax.inject.Singleton

data class InteropAuthorizationDecision(
    val status: HubInteropStatus,
    val grantDescriptor: InteropGrantDescriptor,
    val message: String,
) {
    val isGranted: Boolean = grantDescriptor.isActive && status == HubInteropStatus.OK
}

@Singleton
class HubInteropAuthorizationService @Inject constructor(
    private val compatibilityService: HubInteropCompatibilityService,
    private val governanceRepository: GovernanceRepository,
    private val standardToolCatalog: StandardToolCatalog,
    private val appStrings: AppStrings,
) {
    suspend fun requestAuthorization(request: AuthorizationBundles.Request): AuthorizationBundles.Response {
        val compatibility = compatibilityService.evaluate(request.requestedVersion)
        val requestContext = requestContext(request)
        val current = resolveAuthorizationDecision(requestContext)
        if (current.status == HubInteropStatus.OK || current.status == HubInteropStatus.FORBIDDEN) {
            return AuthorizationBundles.Response(
                status = current.status,
                compatibilitySignal = compatibility,
                grantDescriptor = current.grantDescriptor,
                message = current.message,
            )
        }

        governanceRepository.recordCallerObservation(
            callerIdentity = requestContext.caller.asCallerIdentity(requestContext.requestedScopes),
            displayLabel = requestContext.caller.displayLabel,
            decisionSummary = appStrings.get(
                R.string.hub_interop_authorization_requested,
                capabilityDisplayName(requestContext.capabilityId),
            ),
            toolId = requestContext.capabilityId,
            toolDisplayName = capabilityDisplayName(requestContext.capabilityId),
        )
        requestContext.requestedScopes.forEach { scopeId ->
            val existing = governanceRepository.getScopeGrant(requestContext.caller.callerId, scopeId)
            if (existing == null) {
                governanceRepository.updateScopeGrant(
                    callerId = requestContext.caller.callerId,
                    scopeId = scopeId,
                    grantState = GovernanceGrantState.ASK,
                )
            }
        }
        val updated = resolveAuthorizationDecision(requestContext)
        return AuthorizationBundles.Response(
            status = updated.status,
            compatibilitySignal = compatibility,
            grantDescriptor = updated.grantDescriptor,
            message = updated.message,
        )
    }

    suspend fun getGrantStatus(request: AuthorizationBundles.Request): AuthorizationBundles.Response {
        val compatibility = compatibilityService.evaluate(request.requestedVersion)
        val decision = resolveAuthorizationDecision(requestContext(request))
        return AuthorizationBundles.Response(
            status = decision.status,
            compatibilitySignal = compatibility,
            grantDescriptor = decision.grantDescriptor,
            message = decision.message,
        )
    }

    suspend fun revokeGrant(request: AuthorizationBundles.Request): AuthorizationBundles.Response {
        val compatibility = compatibilityService.evaluate(request.requestedVersion)
        val requestContext = requestContext(request)
        governanceRepository.recordCallerObservation(
            callerIdentity = requestContext.caller.asCallerIdentity(requestContext.requestedScopes),
            displayLabel = requestContext.caller.displayLabel,
            decisionSummary = appStrings.get(
                R.string.hub_interop_authorization_revoked,
                capabilityDisplayName(requestContext.capabilityId),
            ),
            toolId = requestContext.capabilityId,
            toolDisplayName = capabilityDisplayName(requestContext.capabilityId),
        )
        requestContext.requestedScopes.forEach { scopeId ->
            governanceRepository.updateScopeGrant(
                callerId = requestContext.caller.callerId,
                scopeId = scopeId,
                grantState = GovernanceGrantState.DENY,
            )
        }
        val revoked = resolveAuthorizationDecision(requestContext)
        return AuthorizationBundles.Response(
            status = HubInteropStatus.OK,
            compatibilitySignal = compatibility,
            grantDescriptor = revoked.grantDescriptor,
            message = appStrings.get(
                R.string.hub_interop_authorization_revoked,
                capabilityDisplayName(requestContext.capabilityId),
            ),
        )
    }

    suspend fun resolveAuthorizationDecision(
        requestContext: InteropRequestContext,
    ): InteropAuthorizationDecision {
        if (requestContext.requestedScopes.isEmpty()) {
            return InteropAuthorizationDecision(
                status = HubInteropStatus.OK,
                grantDescriptor = grantDescriptor(
                    requestContext = requestContext,
                    isActive = true,
                ),
                message = appStrings.get(
                    R.string.hub_interop_authorization_granted,
                    capabilityDisplayName(requestContext.capabilityId),
                ),
            )
        }

        val grantStates = requestContext.requestedScopes.associateWith { scopeId ->
            governanceRepository.getScopeGrant(requestContext.caller.callerId, scopeId)?.grantState
        }
        val callerTrustMode = governanceRepository.getCallerRecord(requestContext.caller.callerId)?.trustMode
        return when {
            callerTrustMode == GovernanceTrustMode.DENIED -> {
                InteropAuthorizationDecision(
                    status = HubInteropStatus.FORBIDDEN,
                    grantDescriptor = grantDescriptor(
                        requestContext = requestContext,
                        isActive = false,
                        state = InteropGrantState.DENIED,
                    ),
                    message = appStrings.get(
                        R.string.hub_interop_authorization_denied,
                        capabilityDisplayName(requestContext.capabilityId),
                    ),
                )
            }

            grantStates.values.any { it == GovernanceGrantState.DENY } -> {
                InteropAuthorizationDecision(
                    status = HubInteropStatus.FORBIDDEN,
                    grantDescriptor = grantDescriptor(
                        requestContext = requestContext,
                        isActive = false,
                        state = InteropGrantState.DENIED,
                    ),
                    message = appStrings.get(
                        R.string.hub_interop_authorization_denied,
                        capabilityDisplayName(requestContext.capabilityId),
                    ),
                )
            }

            requestContext.requestedScopes.all { grantStates[it] == GovernanceGrantState.ALLOW } -> {
                InteropAuthorizationDecision(
                    status = HubInteropStatus.OK,
                    grantDescriptor = grantDescriptor(
                        requestContext = requestContext,
                        isActive = true,
                        state = InteropGrantState.ACTIVE,
                    ),
                    message = appStrings.get(
                        R.string.hub_interop_authorization_granted,
                        capabilityDisplayName(requestContext.capabilityId),
                    ),
                )
            }

            callerTrustMode == GovernanceTrustMode.TRUSTED -> {
                InteropAuthorizationDecision(
                    status = HubInteropStatus.OK,
                    grantDescriptor = grantDescriptor(
                        requestContext = requestContext,
                        isActive = true,
                        state = InteropGrantState.ACTIVE,
                    ),
                    message = appStrings.get(
                        R.string.hub_interop_authorization_granted,
                        capabilityDisplayName(requestContext.capabilityId),
                    ),
                )
            }

            grantStates.values.any { it == GovernanceGrantState.ASK } -> {
                InteropAuthorizationDecision(
                    status = HubInteropStatus.AUTHORIZATION_PENDING,
                    grantDescriptor = grantDescriptor(
                        requestContext = requestContext,
                        isActive = false,
                        state = InteropGrantState.PENDING,
                    ),
                    message = appStrings.get(
                        R.string.hub_interop_authorization_pending,
                        capabilityDisplayName(requestContext.capabilityId),
                        requestContext.caller.displayLabel,
                    ),
                )
            }

            else -> {
                InteropAuthorizationDecision(
                    status = HubInteropStatus.AUTHORIZATION_REQUIRED,
                    grantDescriptor = grantDescriptor(
                        requestContext = requestContext,
                        isActive = false,
                        state = InteropGrantState.PENDING,
                    ),
                    message = appStrings.get(
                        R.string.hub_interop_authorization_required,
                        capabilityDisplayName(requestContext.capabilityId),
                        requestContext.caller.displayLabel,
                    ),
                )
            }
        }
    }

    private fun requestContext(request: AuthorizationBundles.Request): InteropRequestContext {
        return InteropRequestContext.from(
            request = request,
            defaultScopes = defaultScopesFor(request.capabilityId),
        )
    }

    private fun defaultScopesFor(capabilityId: String): List<String> {
        val descriptor = standardToolCatalog.descriptorForCapability(capabilityId)
        if (descriptor.requiredScopes.isNotEmpty()) return descriptor.requiredScopes
        return ActionScope.fromCapabilityId(capabilityId)
            .takeIf { it != ActionScope.UNKNOWN }
            ?.let { listOf(it.scopeId) }
            .orEmpty()
    }

    private fun capabilityDisplayName(capabilityId: String): String {
        return standardToolCatalog.descriptorForCapability(capabilityId).displayName
    }

    private fun grantDescriptor(
        requestContext: InteropRequestContext,
        isActive: Boolean,
        state: InteropGrantState = if (isActive) InteropGrantState.ACTIVE else InteropGrantState.PENDING,
    ): InteropGrantDescriptor {
        return InteropGrantDescriptor(
            handle = InteropHandle(
                family = InteropHandleFamily.GRANT_REQUEST,
                value = "${requestContext.caller.callerId}:${requestContext.capabilityId}",
            ),
            direction = InteropGrantDirection.INBOUND,
            lifetime = InteropGrantLifetime.PERSISTENT,
            scopes = requestContext.requestedScopes,
            authorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
            isActive = isActive,
            state = state,
        )
    }
}
