package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField

@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit, onHome: () -> Unit) {
    TrackIIBackground(glowOffsetX = 80.dp, glowOffsetY = (-40).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ttlogo),
                    contentDescription = "TT logo",
                    modifier = Modifier.size(260.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.size(20.dp))
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TrackIITextField(label = "Correo electrónico")
                        TrackIITextField(label = "Contraseña", isPassword = true)
                        PrimaryGlowButton(
                            text = "Iniciar sesión",
                            onClick = onLogin,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SoftActionButton(
                            text = "Crear cuenta",
                            onClick = onRegister,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            )
        }
    }
}
