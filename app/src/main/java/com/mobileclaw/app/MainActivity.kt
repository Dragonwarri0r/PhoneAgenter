package com.mobileclaw.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mobileclaw.app.runtime.ingress.ExternalHandoffCoordinator
import com.mobileclaw.app.runtime.ingress.ExternalHandoffParser
import com.mobileclaw.app.ui.navigation.AppScaffold
import com.mobileclaw.app.ui.theme.MobileClawTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var externalHandoffParser: ExternalHandoffParser
    @Inject lateinit var externalHandoffCoordinator: ExternalHandoffCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            handleExternalIntent(intent)
        }
        enableEdgeToEdge()
        setContent {
            MobileClawTheme {
                AppScaffold()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleExternalIntent(intent)
    }

    private fun handleExternalIntent(intent: Intent?) {
        val result = externalHandoffParser.parse(
            intent = intent,
            callerPackageName = callingActivity?.packageName,
            referrerUri = referrer,
        ) ?: return
        externalHandoffCoordinator.publish(result)
    }
}
