package com.ttelectronics.trackiiapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ttelectronics.trackiiapp.ui.navigation.TrackIINavHost

@Composable
fun TrackIIApp() {
    Scaffold { innerPadding ->
        TrackIINavHost(modifier = Modifier.padding(innerPadding))
    }
}
