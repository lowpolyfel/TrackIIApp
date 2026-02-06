package com.ttelectronics.trackiiapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTBlueLight
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

@Composable
fun TrackIIBackground(content: @Composable () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            TTBlueTint,
            Color.White,
            TTBlueLight.copy(alpha = 0.45f)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        DecorativeGlow()
        content()
    }
}

@Composable
private fun BoxScope.DecorativeGlow() {
    val transition = rememberInfiniteTransition(label = "glow")
    val blurAlpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blurAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .align(Alignment.TopCenter)
            .background(
                Brush.radialGradient(
                    listOf(TTAccent.copy(alpha = blurAlpha), Color.Transparent)
                )
            )
            .blur(60.dp)
    )
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "glassShift")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glassShift"
    )
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.9f),
            TTBlueTint.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.95f)
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(600f * shift + 200f, 600f),
        tileMode = TileMode.Mirror
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(shadowElevation = 18f, shape = RoundedCornerShape(28.dp), clip = false),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f))
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(24.dp)
        ) {
            content()
        }
    }
}

@Composable
fun TrackIITextField(label: String, isPassword: Boolean = false) {
    var value by remember { mutableStateOf("") }
    val transformation = if (isPassword) {
        androidx.compose.ui.text.input.PasswordVisualTransformation()
    } else {
        androidx.compose.ui.text.input.VisualTransformation.None
    }
    TextField(
        value = value,
        onValueChange = { value = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(text = label) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TTBlueTint,
            unfocusedContainerColor = TTBlueTint,
            disabledContainerColor = TTBlueTint,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(18.dp),
        visualTransformation = transformation
    )
}

@Composable
fun PrimaryGlowButton(text: String, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "buttonGlow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val gradientShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    val gradient = Brush.linearGradient(
        colors = listOf(TTBlue, TTAccent, TTBlueDark),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(400f * gradientShift + 200f, 0f),
        tileMode = TileMode.Mirror
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(TTAccent.copy(alpha = glowAlpha))
        )
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SoftActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = TTBlueLight),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text = text, color = TTBlueDark, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TaskCard(title: String, icon: ImageVector, animationDelayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "taskPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = animationDelayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "taskScale"
    )
    val glow by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = animationDelayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "taskGlow"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(TTBlueTint)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.radialGradient(
                                listOf(TTAccent.copy(alpha = glow), Color.Transparent)
                            )
                        )
                )
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    shape = CircleShape,
                    color = TTBlueTint
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(TTBlue, TTBlueDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Flujo inteligente de alta precisión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TTTextSecondary
                )
            }
        }
    }
}
