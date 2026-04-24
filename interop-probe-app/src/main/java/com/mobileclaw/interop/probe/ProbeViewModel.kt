package com.mobileclaw.interop.probe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.android.bundle.TaskBundles
import com.mobileclaw.interop.contract.CallerContractIdentity
import com.mobileclaw.interop.contract.ExternalTrustState
import com.mobileclaw.interop.contract.InteropIds
import com.mobileclaw.interop.contract.InteropVersion
import com.mobileclaw.interop.probe.client.ArtifactClient
import com.mobileclaw.interop.probe.client.AuthorizationClient
import com.mobileclaw.interop.probe.client.CompatibilityInspector
import com.mobileclaw.interop.probe.client.DiscoveryClient
import com.mobileclaw.interop.probe.client.InvocationClient
import com.mobileclaw.interop.probe.client.TaskClient
import com.mobileclaw.interop.probe.model.ProbeValidationOutcome
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProbeViewModel(
    private val probePackageName: String,
    private val probeLabel: String,
    private val strings: ProbeStrings,
    private val discoveryClient: DiscoveryClient,
    private val authorizationClient: AuthorizationClient,
    private val invocationClient: InvocationClient,
    private val taskClient: TaskClient,
    private val artifactClient: ArtifactClient,
    private val compatibilityInspector: CompatibilityInspector,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProbeUiState())
    val uiState: StateFlow<ProbeUiState> = _uiState.asStateFlow()

    fun onHostPackageChanged(value: String) {
        _uiState.value = _uiState.value.copy(hostPackageName = value.trim())
    }

    fun onRequestedVersionChanged(value: String) {
        _uiState.value = _uiState.value.copy(requestedVersion = value.trim())
    }

    fun onInvocationInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(invocationInput = value)
    }

    fun discoverHost() = runProbeAction {
        val state = _uiState.value
        val result = discoveryClient.discoverHost(
            hostPackageName = state.hostPackageName,
            requestedVersion = normalizedVersion(state.requestedVersion),
        )
        _uiState.value = _uiState.value.copy(
            hostSummary = result.hostSummary,
            driftOutcomes = emptyList(),
        ).withOutcome(result.outcome)
    }

    fun requestAuthorization() = runProbeAction {
        val result = authorizationClient.requestAuthorization(
            hostPackageName = _uiState.value.hostPackageName,
            request = authorizationRequest(),
        )
        _uiState.value = _uiState.value.copy(
            latestGrantDescriptor = result.grantDescriptor ?: _uiState.value.latestGrantDescriptor,
            latestAuthorizationOutcome = result.outcome,
        ).withOutcome(result.outcome)
    }

    fun refreshGrantStatus() = runProbeAction {
        val result = authorizationClient.getGrantStatus(
            hostPackageName = _uiState.value.hostPackageName,
            request = authorizationRequest(
                includeGrantHandle = true,
            ),
        )
        _uiState.value = _uiState.value.copy(
            latestGrantDescriptor = result.grantDescriptor ?: _uiState.value.latestGrantDescriptor,
            latestAuthorizationOutcome = result.outcome,
        ).withOutcome(result.outcome)
    }

    fun revokeGrant() = runProbeAction {
        val result = authorizationClient.revokeGrant(
            hostPackageName = _uiState.value.hostPackageName,
            request = authorizationRequest(
                includeGrantHandle = true,
            ),
        )
        _uiState.value = _uiState.value.copy(
            latestGrantDescriptor = result.grantDescriptor ?: _uiState.value.latestGrantDescriptor,
            latestAuthorizationOutcome = result.outcome,
        ).withOutcome(result.outcome)
    }

    fun invokeCapability() = runProbeAction {
        val state = _uiState.value
        val result = invocationClient.invoke(
            hostPackageName = state.hostPackageName,
            request = InvocationBundles.Request(
                requestId = requestId("invoke"),
                callerIdentity = callerIdentity(state.requestedVersion),
                capabilityId = InteropIds.Capability.GENERATE_REPLY,
                input = state.invocationInput,
                requestedScopes = listOf(InteropIds.Scope.REPLY_GENERATE),
                requestedVersion = normalizedVersion(state.requestedVersion),
            ),
        )
        _uiState.value = _uiState.value.copy(
            latestInvocationOutcome = result.outcome,
            latestTask = result.taskState ?: _uiState.value.latestTask,
        ).withOutcome(result.outcome)
    }

    fun pollLatestTask() = runProbeAction {
        val latestTask = _uiState.value.latestTask ?: return@runProbeAction
        val result = taskClient.loadTask(
            hostPackageName = _uiState.value.hostPackageName,
            request = TaskBundles.Request(
                handle = latestTask.handle,
                requestedVersion = normalizedVersion(_uiState.value.requestedVersion),
            ),
        )
        _uiState.value = _uiState.value.copy(
            latestTask = result.taskState ?: latestTask,
        ).withOutcome(result.outcome)
    }

    fun loadLatestArtifact() = runProbeAction {
        val artifactHandle = _uiState.value.latestTask?.artifactHandle ?: return@runProbeAction
        val result = artifactClient.loadArtifact(
            hostPackageName = _uiState.value.hostPackageName,
            request = ArtifactBundles.Request(
                handle = artifactHandle,
                requestedVersion = normalizedVersion(_uiState.value.requestedVersion),
            ),
        )
        _uiState.value = _uiState.value.copy(
            latestArtifact = result.artifactDescriptor ?: _uiState.value.latestArtifact,
        ).withOutcome(result.outcome)
    }

    fun runDriftDiagnostics() = runProbeAction {
        val hostPackageName = _uiState.value.hostPackageName
        val minorResult = discoveryClient.discoverHost(
            hostPackageName = hostPackageName,
            requestedVersion = "1.1",
        )
        val majorResult = discoveryClient.discoverHost(
            hostPackageName = hostPackageName,
            requestedVersion = "2.0",
        )
        _uiState.value = _uiState.value.copy(
            driftOutcomes = listOf(minorResult.outcome, majorResult.outcome),
        ).withOutcome(minorResult.outcome).withOutcome(majorResult.outcome)
    }

    fun shareableSummary(): String {
        val state = _uiState.value
        return buildString {
            appendLine(strings.get(R.string.probe_summary_title))
            appendLine(strings.get(R.string.probe_summary_host, state.hostPackageName))
            state.hostSummary?.let { summary ->
                appendLine(strings.get(R.string.probe_summary_surface, summary.displayName))
                appendLine(strings.get(R.string.probe_summary_contract, summary.contractVersion))
            }
            state.latestTask?.let { task ->
                appendLine(strings.get(R.string.probe_summary_task, task.displayName, task.statusLabel))
            }
            state.latestArtifact?.summary?.takeIf { it.isNotBlank() }?.let { artifactSummary ->
                appendLine(strings.get(R.string.probe_summary_artifact, artifactSummary))
            }
            if (state.timeline.isNotEmpty()) {
                appendLine()
                appendLine(strings.get(R.string.probe_summary_timeline))
                state.timeline.take(8).forEach { outcome ->
                    appendLine("• ${outcome.title} · ${outcome.statusLine} · ${outcome.message}")
                }
            }
        }.trim()
    }

    private fun authorizationRequest(includeGrantHandle: Boolean = false): AuthorizationBundles.Request {
        return AuthorizationBundles.Request(
            requestId = requestId("grant"),
            callerIdentity = callerIdentity(_uiState.value.requestedVersion),
            capabilityId = InteropIds.Capability.GENERATE_REPLY,
            requestedScopes = listOf(InteropIds.Scope.REPLY_GENERATE),
            handle = _uiState.value.latestGrantDescriptor?.handle.takeIf { includeGrantHandle },
            requestedVersion = normalizedVersion(_uiState.value.requestedVersion),
        )
    }

    private fun callerIdentity(requestedVersion: String): CallerContractIdentity {
        return CallerContractIdentity(
            originApp = probePackageName,
            packageName = probePackageName,
            sourceLabel = probeLabel,
            trustState = ExternalTrustState.UNVERIFIED,
            trustReason = "probe_app_validation",
            contractVersion = normalizedVersion(requestedVersion),
        )
    }

    private fun normalizedVersion(raw: String): String {
        return InteropVersion.parse(raw)?.value ?: raw.ifBlank { InteropVersion.CURRENT.value }
    }

    private fun requestId(prefix: String): String {
        return "probe-$prefix-${System.currentTimeMillis()}"
    }

    private fun runProbeAction(block: suspend () -> Unit) {
        _uiState.value = _uiState.value.copy(isBusy = true)
        viewModelScope.launch(ioDispatcher) {
            runCatching { block() }
                .onFailure { throwable ->
                    val failure = compatibilityInspector.unavailableOutcome(
                        step = com.mobileclaw.interop.probe.model.ProbeValidationStep.COMPATIBILITY,
                        message = strings.actionFailed(throwable.message),
                    )
                    _uiState.value = _uiState.value.withOutcome(failure)
                }
            _uiState.value = _uiState.value.copy(isBusy = false)
        }
    }

    private fun ProbeUiState.withOutcome(outcome: ProbeValidationOutcome): ProbeUiState {
        return copy(
            timeline = listOf(outcome) + timeline,
        )
    }
}

class ProbeViewModelFactory(
    private val probePackageName: String,
    private val probeLabel: String,
    private val strings: ProbeStrings,
    private val discoveryClient: DiscoveryClient,
    private val authorizationClient: AuthorizationClient,
    private val invocationClient: InvocationClient,
    private val taskClient: TaskClient,
    private val artifactClient: ArtifactClient,
    private val compatibilityInspector: CompatibilityInspector,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProbeViewModel(
            probePackageName = probePackageName,
            probeLabel = probeLabel,
            strings = strings,
            discoveryClient = discoveryClient,
            authorizationClient = authorizationClient,
            invocationClient = invocationClient,
            taskClient = taskClient,
            artifactClient = artifactClient,
            compatibilityInspector = compatibilityInspector,
        ) as T
    }
}
