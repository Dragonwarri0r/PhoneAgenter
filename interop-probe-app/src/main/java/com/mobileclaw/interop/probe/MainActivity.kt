package com.mobileclaw.interop.probe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileclaw.interop.probe.client.AndroidHubInteropClient
import com.mobileclaw.interop.probe.client.ArtifactClient
import com.mobileclaw.interop.probe.client.AuthorizationClient
import com.mobileclaw.interop.probe.client.CompatibilityInspector
import com.mobileclaw.interop.probe.client.DiscoveryClient
import com.mobileclaw.interop.probe.client.InvocationClient
import com.mobileclaw.interop.probe.client.TaskClient
import com.mobileclaw.interop.probe.ui.ProbeNavGraph
import com.mobileclaw.interop.probe.ui.theme.ProbeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProbeTheme {
                val context = LocalContext.current
                val strings = remember { ProbeStrings(applicationContext) }
                val interopClient = remember { AndroidHubInteropClient(applicationContext) }
                val inspector = remember { CompatibilityInspector(strings) }
                val probeLabel = remember {
                    applicationInfo.loadLabel(packageManager).toString()
                }
                val viewModelFactory = remember(strings, interopClient, inspector, probeLabel) {
                    ProbeViewModelFactory(
                        probePackageName = applicationContext.packageName,
                        probeLabel = probeLabel,
                        strings = strings,
                        discoveryClient = DiscoveryClient(
                            interopClient = interopClient,
                            strings = strings,
                            compatibilityInspector = inspector,
                        ),
                        authorizationClient = AuthorizationClient(
                            interopClient = interopClient,
                            strings = strings,
                            compatibilityInspector = inspector,
                        ),
                        invocationClient = InvocationClient(
                            interopClient = interopClient,
                            strings = strings,
                            compatibilityInspector = inspector,
                        ),
                        taskClient = TaskClient(
                            interopClient = interopClient,
                            strings = strings,
                            compatibilityInspector = inspector,
                        ),
                        artifactClient = ArtifactClient(
                            interopClient = interopClient,
                            strings = strings,
                            compatibilityInspector = inspector,
                        ),
                        compatibilityInspector = inspector,
                    )
                }
                val viewModel: ProbeViewModel = viewModel(
                    factory = viewModelFactory,
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ProbeNavGraph(
                    uiState = uiState,
                    summaryText = viewModel.shareableSummary(),
                    onHostPackageChanged = viewModel::onHostPackageChanged,
                    onRequestedVersionChanged = viewModel::onRequestedVersionChanged,
                    onInvocationInputChanged = viewModel::onInvocationInputChanged,
                    onDiscover = viewModel::discoverHost,
                    onRequestAuthorization = viewModel::requestAuthorization,
                    onRefreshGrant = viewModel::refreshGrantStatus,
                    onRevokeGrant = viewModel::revokeGrant,
                    onInvoke = viewModel::invokeCapability,
                    onPollTask = viewModel::pollLatestTask,
                    onLoadArtifact = viewModel::loadLatestArtifact,
                    onRunDriftCheck = viewModel::runDriftDiagnostics,
                    onOpenHost = {
                        interopClient.launchIntentFor(uiState.hostPackageName)?.let { intent ->
                            context.startActivity(intent)
                        }
                    },
                    onShareSummary = {
                        val summary = viewModel.shareableSummary()
                        startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, summary)
                                },
                                getString(R.string.probe_share_summary),
                            ),
                        )
                    },
                )
            }
        }
    }
}
