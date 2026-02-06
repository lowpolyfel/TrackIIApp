package com.ttelectronics.trackiiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ttelectronics.trackiiapp.ui.TrackIIApp
import com.ttelectronics.trackiiapp.ui.theme.TrackIIAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackIIAppTheme {
                TrackIIApp()
            }
        }
    }
}
