package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

private const val WELCOME_JOURNEY_DURATION_MS = 2500L

@Composable
fun WelcomeScreen(
    onStart: () -> Unit,
    userName: String = "Usuario"
) {
    val displayName = userName.ifBlank { "Usuario" }
    val transition = rememberInfiniteTransition(label = "arrowWave")
    val arrowOne by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOne"
    )
    val arrowTwo by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowTwo"
    )
    val arrowThree by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowThree"
    )
    var leafAlpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        delay(WELCOME_JOURNEY_DURATION_MS)
        leafAlpha = 0f
    }

    TrackIIBackground(glowOffsetX = (-10).dp, glowOffsetY = (-20).dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStart
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Bienvenido, $displayName",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(24.dp))
                Box(modifier = Modifier.size(360.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_trackii),
                        contentDescription = "TrackII logo",
                        modifier = Modifier.size(360.dp),
                        contentScale = ContentScale.Fit
                    )
                    this@Column.AnimatedVisibility(
                        visible = leafAlpha > 0f,
                        exit = fadeOut(animationSpec = tween(durationMillis = 260))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logofondo_a),
                            contentDescription = "Hoja del logo",
                            modifier = Modifier.size(360.dp).graphicsLayer { alpha = leafAlpha },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.size(20.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Contenedor de flechas
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ArrowHint(alpha = arrowOne)
                        ArrowHint(alpha = arrowTwo)
                        ArrowHint(alpha = arrowThree)
                    }
                    Text(
                        text = "Puedes dar click en cualquier parte de la pantalla",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TTTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArrowHint(alpha: Float) {
    Icon(
        imageVector = Icons.Rounded.KeyboardArrowUp,
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                this.alpha = alpha
                translationY = (1f - alpha) * 24f
            },
        tint = TTTextSecondary
    )
}
