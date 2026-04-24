package com.mobileclaw.app.runtime.interop

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.capability.StandardToolCatalog
import com.mobileclaw.app.runtime.ingress.CallableInteropMapper
import com.mobileclaw.app.runtime.ingress.CallableRequestPayload
import com.mobileclaw.app.runtime.ingress.ExternalRuntimeRequestMapper
import com.mobileclaw.app.runtime.localchat.LocalModelCatalog
import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus
import com.mobileclaw.app.runtime.session.RuntimeSessionEvent
import com.mobileclaw.app.runtime.session.RuntimeSessionFacade
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.HubInteropStatusMapper
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.contract.InteropIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HubCapabilityInvocationService @Inject constructor(
    private val compatibilityService: HubInteropCompatibilityService,
    private val callableInteropMapper: CallableInteropMapper,
    private val externalRuntimeRequestMapper: ExternalRuntimeRequestMapper,
    private val runtimeSessionFacade: RuntimeSessionFacade,
    private val localModelCatalog: LocalModelCatalog,
    private val standardToolCatalog: StandardToolCatalog,
    private val authorizationService: HubInteropAuthorizationService,
    private val taskService: HubInteropTaskService,
    private val appStrings: AppStrings,
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun invoke(request: InvocationBundles.Request): InvocationBundles.Response {
        val compatibility = compatibilityService.evaluate(request.requestedVersion)
        val mergedStatus = HubInteropStatusMapper.merge(HubInteropStatus.OK, compatibility)
        if (mergedStatus == HubInteropStatus.INCOMPATIBLE_VERSION) {
            return InvocationBundles.Response(
                status = mergedStatus,
                compatibilitySignal = compatibility,
                message = compatibility.compatibilityReason,
            )
        }

        val toolDescriptor = standardToolCatalog.descriptorForCapability(request.capabilityId)
        val requestContext = InteropRequestContext.from(
            request = request,
            defaultScopes = toolDescriptor.requiredScopes,
        )

        if (request.capabilityId != InteropIds.Capability.GENERATE_REPLY) {
            return InvocationBundles.Response(
                status = HubInteropStatus.UNSUPPORTED_CAPABILITY,
                compatibilitySignal = compatibility,
                message = "unsupported_capability:${request.capabilityId}",
            )
        }

        val authorizationDecision = authorizationService.resolveAuthorizationDecision(requestContext)
        if (!authorizationDecision.isGranted) {
            return InvocationBundles.Response(
                status = authorizationDecision.status,
                compatibilitySignal = compatibility,
                message = authorizationDecision.message,
            )
        }

        val selectedModel = localModelCatalog.models.first().firstOrNull { profile ->
            profile.isSelectable && profile.availabilityStatus == ModelAvailabilityStatus.READY
        } ?: return InvocationBundles.Response(
            status = HubInteropStatus.PROVIDER_UNAVAILABLE,
            compatibilitySignal = compatibility,
            message = appStrings.get(R.string.appfunctions_reply_model_unavailable),
        )

        val payload = CallableRequestPayload(
            requestId = request.requestId,
            surfaceId = InteropIds.Surface.RUNTIME_CALLABLE_BASIC,
            callerIdentity = request.callerIdentity,
            userInput = request.input,
            requestedScopes = requestContext.requestedScopes,
            requestedCapabilityId = request.capabilityId,
            contractVersion = request.requestedVersion,
        )
        val envelope = callableInteropMapper.map(payload)
        val runtimeRequest = externalRuntimeRequestMapper.map(
            envelope = envelope,
            selectedModelId = selectedModel.modelId,
            workspaceId = "hub_interop_host",
            transcriptContext = emptyList(),
        )
        val taskDescriptor = taskService.createPendingTask(
            requestId = request.requestId,
            capabilityId = request.capabilityId,
            displayName = toolDescriptor.displayName,
            summary = appStrings.get(R.string.hub_interop_task_queued, toolDescriptor.displayName),
        )
        serviceScope.launch {
            runtimeSessionFacade.submitRequest(runtimeRequest).collect { event ->
                when (event) {
                    is RuntimeSessionEvent.SessionStarted -> {
                        taskService.markSessionStarted(
                            taskHandle = taskDescriptor.handle,
                            sessionId = event.session.sessionId,
                            summary = event.session.summary.supportingText,
                        )
                    }

                    is RuntimeSessionEvent.StageChanged -> {
                        when (event.stage.stageType) {
                            com.mobileclaw.app.runtime.session.RuntimeStageType.AWAITING_APPROVAL -> {
                                taskService.markInputRequired(
                                    taskHandle = taskDescriptor.handle,
                                    summary = event.stage.details,
                                )
                            }

                            com.mobileclaw.app.runtime.session.RuntimeStageType.EXECUTING -> {
                                taskService.markRunning(
                                    taskHandle = taskDescriptor.handle,
                                    summary = event.stage.details,
                                )
                            }

                            else -> Unit
                        }
                    }

                    is RuntimeSessionEvent.ApprovalRequested -> {
                        taskService.markInputRequired(
                            taskHandle = taskDescriptor.handle,
                            summary = event.request.summary,
                        )
                    }

                    is RuntimeSessionEvent.SessionCompleted -> {
                        taskService.markCompleted(
                            taskHandle = taskDescriptor.handle,
                            summary = event.outcome.userMessage,
                            outputText = event.outcome.outputText,
                        )
                    }

                    is RuntimeSessionEvent.SessionFailed -> {
                        taskService.markFailed(
                            taskHandle = taskDescriptor.handle,
                            summary = event.outcome.userMessage,
                        )
                    }

                    is RuntimeSessionEvent.SessionDenied -> {
                        taskService.markFailed(
                            taskHandle = taskDescriptor.handle,
                            summary = event.outcome.userMessage,
                        )
                    }

                    is RuntimeSessionEvent.SessionCancelled -> {
                        taskService.markCancelled(
                            taskHandle = taskDescriptor.handle,
                            summary = event.outcome.userMessage,
                        )
                    }

                    else -> Unit
                }
            }
        }

        return InvocationBundles.Response(
            status = mergedStatus,
            compatibilitySignal = compatibility,
            taskDescriptor = taskDescriptor,
            message = taskDescriptor.summary,
        )
    }
}
