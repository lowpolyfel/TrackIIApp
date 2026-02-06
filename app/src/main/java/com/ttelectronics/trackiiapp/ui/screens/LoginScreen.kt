package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    TrackIIBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_trackii),
                contentDescription = "TrackII logo",
                modifier = Modifier.size(140.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.size(20.dp))
            Text(
                text = "TrackII",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Accede con un flujo limpio y futurista.",
                style = MaterialTheme.typography.bodyMedium,
                color = TTTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TrackIITextField(label = "Correo electrónico")
                    TrackIITextField(label = "Contraseña", isPassword = true)
                    PrimaryGlowButton(text = "Iniciar sesión", onClick = onLogin)
                    SoftActionButton(text = "Crear cuenta", onClick = onRegister)
                }
            }
        }
    }
}
