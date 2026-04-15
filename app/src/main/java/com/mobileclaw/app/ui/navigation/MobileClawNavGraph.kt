package com.mobileclaw.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobileclaw.app.ui.agentworkspace.AgentWorkspaceScreen
import com.mobileclaw.app.ui.agentworkspace.AgentWorkspaceViewModel

object MobileClawRoute {
    const val AGENT_WORKSPACE = "agent_workspace"
}

@Composable
fun MobileClawNavGraph(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MobileClawRoute.AGENT_WORKSPACE,
        modifier = modifier,
    ) {
        composable(MobileClawRoute.AGENT_WORKSPACE) {
            val viewModel: AgentWorkspaceViewModel = hiltViewModel()
            AgentWorkspaceScreen(viewModel = viewModel)
        }
    }
}

