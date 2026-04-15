package com.mobileclaw.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppScaffold() {
    Surface(modifier = Modifier.fillMaxSize()) {
        MobileClawNavGraph()
    }
}

