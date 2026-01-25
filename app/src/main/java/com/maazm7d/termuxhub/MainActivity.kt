package com.maazm7d.termuxhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.view.WindowCompat
import com.maazm7d.termuxhub.navigation.TermuxHubAppNav
import com.maazm7d.termuxhub.ui.theme.TermuxHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AppContent(initialIntent = intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setContent {
            AppContent(initialIntent = intent)
        }
    }

    @Composable
    private fun AppContent(initialIntent: Intent?) {
        val darkTheme = isSystemInDarkTheme()
        val deepLinkToolId = rememberSaveable { mutableStateOf<String?>(null) }
        
        LaunchedEffect(initialIntent) {
            processDeepLink(initialIntent)?.let { toolId ->
                deepLinkToolId.value = toolId
            }
        }
        
        LaunchedEffect(intent) {
            processDeepLink(intent)?.let { toolId ->
                deepLinkToolId.value = toolId
            }
        }

        SideEffect {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
            }
        }

        TermuxHubTheme(darkTheme = darkTheme) {
            TermuxHubAppNav(
                deepLinkToolId = deepLinkToolId.value
            )
        }
    }
    
    private fun processDeepLink(intent: Intent?): String? {
        return intent?.data?.let { data ->
            if (data.scheme == "https" && 
                data.host == "maazm7d.github.io" &&
                data.path?.startsWith("/termuxhub/tool") == true) {
                
                data.fragment?.takeIf { it.isNotBlank() }
            } else {
                null
            }
        }
    }
}
