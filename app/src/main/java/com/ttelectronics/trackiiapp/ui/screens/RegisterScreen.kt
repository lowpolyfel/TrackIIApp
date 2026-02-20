package com.ttelectronics.trackiiapp.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.SuccessOverlayDialog
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIIDropdownField
import com.ttelectronics.trackiiapp.ui.components.TrackIIReadOnlyField
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.RegisterViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.RegisterViewModelFactory

@Composable
fun RegisterScreen(
    tokenCode: String,
    onCreateAccount: () -> Unit,
    onBackToLogin: () -> Unit,
    onHome: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "cardFloat")
    val cardLift by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(animation = tween(2400), repeatMode = RepeatMode.Reverse),
        label = "cardLift"
    )
    val context = LocalContext.current
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        ?: "No disponible"
    val vm: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(ServiceLocator.authRepository(context)))
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(tokenCode) {
        vm.setToken(tokenCode)
        vm.preloadDeviceUid(androidId)
        vm.loadLocations()
    }

    uiState.successMessage?.let {
        SuccessOverlayDialog(
            title = "Registro completado",
            message = it,
            show = true
        )
        LaunchedEffect(it) { onCreateAccount() }
    }

    TrackIIBackground(glowOffsetX = (-40).dp, glowOffsetY = 30.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Registro de tableta",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Completa la configuración inicial del dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )
                GlassCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.graphicsLayer { translationY = cardLift }
                    ) {
                        TrackIIReadOnlyField(label = "Android ID", value = uiState.deviceUid, helper = "Detectando Android ID")
                        TrackIITextField(
                            label = "Nombre de la tableta",
                            value = uiState.deviceName,
                            onValueChange = vm::onDeviceNameChange
                        )
                        TrackIITextField(label = "Usuario", value = uiState.username, onValueChange = vm::onUsernameChange)
                        TrackIITextField(
                            label = "Contraseña",
                            isPassword = true,
                            value = uiState.password,
                            onValueChange = vm::onPasswordChange
                        )

                        val options = uiState.locations.map { it.name }
                        val selected = uiState.locations.firstOrNull { it.id == uiState.selectedLocationId }?.name.orEmpty()
                        TrackIIDropdownField(
                            label = "Localidad",
                            options = options,
                            helper = if (uiState.isLoading) "Cargando localidades..." else "Opciones desde API",
                            selectedOption = selected,
                            onOptionSelected = { name ->
                                uiState.locations.firstOrNull { it.name == name }?.id?.let(vm::onLocationChange)
                            }
                        )

                        uiState.errorMessage?.let {
                            Text(text = it, color = TTRed, style = MaterialTheme.typography.bodySmall)
                        }

                        PrimaryGlowButton(
                            text = if (uiState.isLoading) "Registrando..." else "Registrar",
                            onClick = vm::register,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SoftActionButton(
                            text = "Volver",
                            onClick = onBackToLogin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
            }
            FloatingHomeButton(
                onClick = onHome,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
            )
        }
    }
}
