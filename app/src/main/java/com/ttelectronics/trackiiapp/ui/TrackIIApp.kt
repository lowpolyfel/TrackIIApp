package com.ttelectronics.trackiiapp.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.ui.theme.TTAccent
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTBlueLight
import com.ttelectronics.trackiiapp.ui.theme.TTBlueTint
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary

private object TrackIIRoute {
    const val Login = "login"
    const val Register = "register"
    const val Tasks = "tasks"
}

@Composable
fun TrackIIApp() {
    val navController = rememberNavController()
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TrackIIRoute.Login,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TrackIIRoute.Login) {
                LoginScreen(
                    onLogin = { navController.navigate(TrackIIRoute.Tasks) },
                    onRegister = { navController.navigate(TrackIIRoute.Register) }
                )
            }
            composable(TrackIIRoute.Register) {
                RegisterScreen(
                    onCreateAccount = { navController.navigate(TrackIIRoute.Tasks) },
                    onBackToLogin = { navController.popBackStack(TrackIIRoute.Login, inclusive = false) }
                )
            }
            composable(TrackIIRoute.Tasks) {
                TaskSelectionScreen(navController)
            }
        }
    }
}

@Composable
private fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    FuturisticBackground {
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
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )
            FuturisticCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Bienvenido a TrackII",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Accede a tus operaciones con un flujo ágil y moderno.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TTTextSecondary
                    )
                    FuturisticTextField(label = "Correo electrónico")
                    FuturisticTextField(label = "Contraseña", isPassword = true)
                    Spacer(modifier = Modifier.height(4.dp))
                    GlowButton(text = "Iniciar sesión", onClick = onLogin)
                    OutlinedButton(
                        onClick = onRegister,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TTBlueDark)
                    ) {
                        Text(text = "Crear cuenta")
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterScreen(onCreateAccount: () -> Unit, onBackToLogin: () -> Unit) {
    FuturisticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Registro",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            FuturisticCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Crea tu cuenta de acceso",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FuturisticTextField(label = "Nombre completo")
                    FuturisticTextField(label = "Correo electrónico")
                    FuturisticTextField(label = "Contraseña", isPassword = true)
                    FuturisticTextField(label = "Confirmar contraseña", isPassword = true)
                    GlowButton(text = "Registrar", onClick = onCreateAccount)
                    OutlinedButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TTBlueDark)
                    ) {
                        Text(text = "Ya tengo cuenta")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSelectionScreen(navController: NavHostController) {
    FuturisticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona una tarea",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Flujos inteligentes diseñados para TT Electronics.",
                style = MaterialTheme.typography.bodyMedium,
                color = TTTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskCard(title = "Seguimiento de hojas viajeras")
                TaskCard(title = "Cancelar Orden")
                TaskCard(title = "Retrabajo")
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Necesitas cambiar de cuenta?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TTTextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { navController.navigate(TrackIIRoute.Login) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "Volver al inicio")
                }
            }
        }
    }
}

@Composable
private fun FuturisticBackground(content: @Composable () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            TTBlueTint,
            Color.White,
            TTBlueLight.copy(alpha = 0.35f)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun FuturisticCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

@Composable
private fun FuturisticTextField(label: String, isPassword: Boolean = false) {
    var value by remember { mutableStateOf("") }
    OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        label = { Text(text = label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
private fun GlowButton(text: String, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(TTAccent.copy(alpha = glowAlpha))
        )
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TTBlue)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TaskCard(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(TTBlue, TTBlueDark)))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
