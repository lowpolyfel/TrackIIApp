package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.components.TopAccountButton
import com.ttelectronics.trackiiapp.ui.navigation.TaskType
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTBlueLight
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTYellow
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun TaskSelectionScreen(
    onTaskSelected: (TaskType) -> Unit,
    onHome: () -> Unit,
    onAccount: () -> Unit,
    onLogout: () -> Unit,
    username: String,
    locationName: String,
    deviceName: String
) {
    // --- TIMER DE INACTIVIDAD (1 Minuto) ---
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastInteractionTime) {
        delay(60_000L) // 60 segundos
        onHome() // Regresa al lobby
    }

    // ESTADOS PARA EL MENÚ SECRETO
    var secretClickCount by remember { mutableIntStateOf(0) }
    var showAdminButtons by remember { mutableStateOf(false) }

    // DATO MOCKADO
    // TODO: En el ViewModel, asegurarse que este conteo traiga SOLO las órdenes donde fecha = HOY
    val currentDayOrdersCount = 45

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TTBlueDark) // El fondo base ahora es el azul oscuro
            // Detecta toques en la pantalla para reiniciar el timer de inactividad
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        lastInteractionTime = System.currentTimeMillis()
                    }
                }
            }
    ) {

        // =========================================================
        // PANEL SUPERIOR: (Información, Localidad y Título)
        // =========================================================
        TopSolidBanner(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f), // Peso extra para la letra gigante
            locationName = locationName,
            dailyOrdersCount = currentDayOrdersCount,
            showAdminButtons = showAdminButtons,
            onAccount = onAccount,
            onLogout = onLogout,
            onSecretClick = {
                if (showAdminButtons) {
                    showAdminButtons = false
                    secretClickCount = 0
                } else {
                    secretClickCount++
                    if (secretClickCount >= 5) {
                        showAdminButtons = true
                        secretClickCount = 0
                    }
                }
            }
        )

        // =========================================================
        // PANEL INFERIOR: (GlassCard con Olas y Botones)
        // =========================================================
        BottomGlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.3f),
            onTaskSelected = onTaskSelected
        )
    }
}

// =======================================================================
// COMPONENTES DE PANELES PRINCIPALES
// =======================================================================

@Composable
private fun TopSolidBanner(
    modifier: Modifier = Modifier,
    locationName: String,
    dailyOrdersCount: Int,
    showAdminButtons: Boolean,
    onAccount: () -> Unit,
    onLogout: () -> Unit,
    onSecretClick: () -> Unit
) {
    Box(
        modifier = modifier.background(TTBlueDark) // Completamente sólido
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {

            // --- HEADER: MENÚ OCULTO IZQUIERDA / TÍTULO DERECHA ---
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Izquierda: Menú Oculto (Los botones aparecen aquí al hacer los 5 clicks)
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // LOGO DESACTIVADO TEMPORALMENTE (Se mantiene en código por si acaso)
                    /*
                    Image(
                        painter = painterResource(id = R.drawable.ttlogo),
                        contentDescription = "Logo TT",
                        modifier = Modifier.height(48.dp)
                    )
                    */

                    AnimatedVisibility(visible = showAdminButtons) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TopAccountButton(onClick = onAccount)
                            FloatingActionButton(
                                onClick = onLogout,
                                modifier = Modifier.size(48.dp),
                                containerColor = TTBlueLight,
                                contentColor = TTBlueDark
                            ) {
                                Icon(imageVector = Icons.Rounded.Logout, contentDescription = "Cerrar sesión")
                            }
                        }
                    }
                }

                // Derecha: Título anclado
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.align(Alignment.TopEnd)) {
                    Text(
                        text = "Selecciona una tarea",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Text(
                        text = "Elige el flujo que necesitas ejecutar.",
                        style = MaterialTheme.typography.titleMedium,
                        color = TTBlueTint.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- FOOTER DEL BANNER: LOCALIDAD Y ÓRDENES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Localidad (TEXTO MASIVO Y AHORA EL BOTÓN SECRETO)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onSecretClick
                        )
                ) {
                    Text(
                        text = "LOCALIDAD ACTUAL",
                        style = MaterialTheme.typography.titleMedium,
                        color = TTBlueTint.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                        fontSize = 68.sp, // TAMAÑO MUY MUY GRANDE
                        color = Color.White,
                        lineHeight = 68.sp,
                        // Offset negativo para "pegarlo" hacia arriba y quitar el espacio
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }

                // Órdenes del día (Asegurarse de pasar solo las del día actual)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ÓRDENES DEL DÍA",
                        style = MaterialTheme.typography.titleMedium,
                        color = TTBlueTint.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dailyOrdersCount",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        fontSize = 52.sp,
                        color = Color.White,
                        lineHeight = 52.sp,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomGlassPanel(
    modifier: Modifier = Modifier,
    onTaskSelected: (TaskType) -> Unit
) {
    // El GlassCard rediseñado
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        backgroundColors = listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.90f)),
        borderColors = listOf(Color.White, Color.White.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // CAPA 1: Las olas como marca de agua en el fondo del GlassCard
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawGlassWaves(TTBlue, TTBlueLight)
            }

            // CAPA 2: Los Botones
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // LOGO INFERIOR DESACTIVADO TEMPORALMENTE
                /*
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ttlogo),
                        contentDescription = "Logo TT Oculto",
                        modifier = Modifier.height(50.dp).alpha(0f)
                    )
                }
                */

                Spacer(modifier = Modifier.weight(0.5f))

                // 1. BOTÓN PRINCIPAL (Verde Gigante Centrado)
                CircularTaskButton(
                    title = "Avanzar producto",
                    icon = Icons.Rounded.Description,
                    color = TTGreen,
                    buttonSize = 250.dp,
                    iconSize = 100.dp,
                    titleStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    titleColor = TTBlueDark,
                    onClick = { onTaskSelected(TaskType.ProductAdvance) }
                )

                Spacer(modifier = Modifier.weight(1f))

                // 2. BOTONES SECUNDARIOS (Tarjetas cuadradas)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryTaskButton(
                        modifier = Modifier.weight(1f),
                        title = "Seguimiento\nde orden",
                        icon = Icons.Rounded.Assignment, // Ícono de portapapeles/tabla
                        color = TTBlue,
                        onClick = { onTaskSelected(TaskType.TravelSheet) }
                    )

                    SecondaryTaskButton(
                        modifier = Modifier.weight(1f),
                        title = "Cancelar\norden",
                        icon = Icons.Rounded.HighlightOff,
                        color = TTRed,
                        onClick = { onTaskSelected(TaskType.CancelOrder) }
                    )

                    SecondaryTaskButton(
                        modifier = Modifier.weight(1f),
                        title = "Retrabajo",
                        icon = Icons.Rounded.Build,
                        color = TTYellow,
                        onClick = { onTaskSelected(TaskType.Rework) }
                    )
                }
            }
        }
    }
}

// =======================================================================
// COMPONENTES AUXILIARES Y BOTONES
// =======================================================================

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(36.dp),
    backgroundColors: List<Color> = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.40f)),
    borderColors: List<Color> = listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.3f)),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = backgroundColors,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = borderColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
    ) {
        content()
    }
}

private fun DrawScope.drawGlassWaves(ttBlue: Color, ttBlueLight: Color) {
    val width = size.width
    val height = size.height

    val path1 = Path().apply {
        moveTo(0f, height * 0.6f)
        cubicTo(
            width * 0.25f, height * 0.8f,
            width * 0.75f, height * 0.4f,
            width, height * 0.7f
        )
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(path = path1, color = ttBlue.copy(alpha = 0.05f))

    val path2 = Path().apply {
        moveTo(0f, height * 0.8f)
        cubicTo(
            width * 0.3f, height * 1.0f,
            width * 0.7f, height * 0.6f,
            width, height * 0.85f
        )
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(
        path = path2,
        brush = Brush.verticalGradient(listOf(ttBlueLight.copy(alpha = 0.1f), Color.Transparent))
    )
}

@Composable
private fun CircularTaskButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    buttonSize: Dp,
    iconSize: Dp,
    titleStyle: androidx.compose.ui.text.TextStyle,
    titleColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(buttonSize * 1.2f)
        ) {
            // Halo animado (Atrás)
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = pulseAlpha))
            )

            // Botón sólido verde (Sombra primero, clip después)
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(color)
                    .border(3.dp, Color.White, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = titleStyle,
            color = titleColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SecondaryTaskButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TTBlueDark,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}