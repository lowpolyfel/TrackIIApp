package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField

@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "logoFloat")
    val scale by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    TrackIIBackground(glowOffsetX = 80.dp, glowOffsetY = (-40).dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_texto_sin_fondo_b),
                    contentDescription = "TrackII logo",
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Spacer(
                    modifier = Modifier
                        .height(120.dp)
                        .width(2.dp)
                        .alpha(0.35f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.ttlogo),
                    contentDescription = "TT logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }
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
    }
}
