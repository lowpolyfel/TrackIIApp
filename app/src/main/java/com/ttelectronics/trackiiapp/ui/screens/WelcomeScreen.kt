package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    var showHint by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showHint = true }
    val transition = rememberInfiniteTransition(label = "arrowWave")
    val arrowOne by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOne"
    )
    val arrowTwo by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowTwo"
    )
    val arrowThree by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowThree"
    )

    TrackIIBackground(glowOffsetX = (-10).dp, glowOffsetY = (-20).dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hola usuario",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(24.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_trackii),
                contentDescription = "TrackII logo",
                modifier = Modifier
                    .size(220.dp)
                    .clickable(onClick = onStart),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.size(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArrowHint(alpha = arrowOne)
                ArrowHint(alpha = arrowTwo)
                ArrowHint(alpha = arrowThree)
            }
            AnimatedVisibility(
                visible = showHint,
                enter = slideInVertically(initialOffsetY = { it / 2 })
            ) {
                Text(
                    text = "Da click en el logo para empezar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ArrowHint(alpha: Float) {
    Text(
        text = ">>>",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .graphicsLayer {
                rotationZ = -90f
                this.alpha = alpha
            },
        color = TTTextSecondary
    )
}
