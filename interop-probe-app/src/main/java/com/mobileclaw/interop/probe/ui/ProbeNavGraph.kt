package com.mobileclaw.interop.probe.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobileclaw.interop.probe.ProbeUiState
import com.mobileclaw.interop.probe.R

private data class ProbeRoute(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit,
)

@Composable
fun ProbeNavGraph(
    uiState: ProbeUiState,
    summaryText: String,
    onHostPackageChanged: (String) -> Unit,
    onRequestedVersionChanged: (String) -> Unit,
    onInvocationInputChanged: (String) -> Unit,
    onDiscover: () -> Unit,
    onRequestAuthorization: () -> Unit,
    onRefreshGrant: () -> Unit,
    onRevokeGrant: () -> Unit,
    onInvoke: () -> Unit,
    onPollTask: () -> Unit,
    onLoadArtifact: () -> Unit,
    onRunDriftCheck: () -> Unit,
    onOpenHost: () -> Unit,
    onShareSummary: () -> Unit,
) {
    val navController = rememberNavController()
    val routes = listOf(
        ProbeRoute("home", R.string.probe_nav_home) { Icon(Icons.Outlined.Home, contentDescription = null) },
        ProbeRoute("discovery", R.string.probe_nav_discovery) { Icon(Icons.Outlined.Api, contentDescription = null) },
        ProbeRoute("authorization", R.string.probe_nav_authorization) { Icon(Icons.Outlined.Key, contentDescription = null) },
        ProbeRoute("invocation", R.string.probe_nav_invocation) { Icon(Icons.Outlined.PlayArrow, contentDescription = null) },
        ProbeRoute("task", R.string.probe_nav_task) { Icon(Icons.Outlined.TaskAlt, contentDescription = null) },
        ProbeRoute("summary", R.string.probe_nav_summary) { Icon(Icons.AutoMirrored.Outlined.FactCheck, contentDescription = null) },
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                routes.forEach { route ->
                    val selected = currentDestination?.hierarchy?.any { it.route == route.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(route.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = route.icon,
                        label = { Text(stringResource(route.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                ProbeHomeScreen(
                    uiState = uiState,
                    onHostPackageChanged = onHostPackageChanged,
                    onRequestedVersionChanged = onRequestedVersionChanged,
                    onInvocationInputChanged = onInvocationInputChanged,
                    onDiscover = onDiscover,
                )
            }
            composable("discovery") {
                DiscoveryScreen(uiState = uiState)
            }
            composable("authorization") {
                AuthorizationScreen(
                    uiState = uiState,
                    onRequestAuthorization = onRequestAuthorization,
                    onRefreshGrant = onRefreshGrant,
                    onRevokeGrant = onRevokeGrant,
                    onOpenHost = onOpenHost,
                )
            }
            composable("invocation") {
                InvocationScreen(
                    uiState = uiState,
                    onInvocationInputChanged = onInvocationInputChanged,
                    onInvoke = onInvoke,
                )
            }
            composable("task") {
                TaskScreen(
                    uiState = uiState,
                    onPollTask = onPollTask,
                    onLoadArtifact = onLoadArtifact,
                    onRunDriftCheck = onRunDriftCheck,
                )
            }
            composable("summary") {
                ProbeSummaryScreen(
                    uiState = uiState,
                    summaryText = summaryText,
                    onShareSummary = onShareSummary,
                )
            }
        }
    }
}
