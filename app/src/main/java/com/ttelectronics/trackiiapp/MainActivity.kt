package com.ttelectronics.trackiiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Importante
import com.ttelectronics.trackiiapp.ui.TrackIIApp
import com.ttelectronics.trackiiapp.ui.theme.TrackIIAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Instalar la pantalla de inicio ANTES del super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TrackIIAppTheme {
                TrackIIApp()
            }
        }
    }
}