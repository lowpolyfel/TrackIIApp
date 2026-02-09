package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
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
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun WelcomeScreen(onStart: () -> Unit, userName: String) {
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
    val textOffset by animateDpAsState(
        targetValue = if (showHint) 0.dp else 180.dp,
        animationSpec = tween(durationMillis = 900),
        label = "textOffset"
    )

    TrackIIBackground(glowOffsetX = (-10).dp, glowOffsetY = (-20).dp) {
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
                text = "Bienvenido, $userName",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(24.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_trackii),
                contentDescription = "TrackII logo",
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
                    text = "Hola usuario",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(24.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo_trackii),
                    contentDescription = "TrackII logo",
                    modifier = Modifier
                        .size(360.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.size(20.dp))
                AnimatedVisibility(
                    visible = showHint,
                    enter = slideInVertically(initialOffsetY = { it })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .graphicsLayer { translationY = textOffset.toPx() }
                        )
                    }
                    Text(
                        text = "Puedes dar click en cualquier parte de la pantalla",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TTTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .graphicsLayer { translationY = textOffset.toPx() }
                    )
                }
            }
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
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
