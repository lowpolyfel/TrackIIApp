package com.ttelectronics.trackiiapp.ui.screens

import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ttelectronics.trackiiapp.R
import com.ttelectronics.trackiiapp.core.ServiceLocator
import com.ttelectronics.trackiiapp.ui.components.FloatingHomeButton
import com.ttelectronics.trackiiapp.ui.components.GlassCard
import com.ttelectronics.trackiiapp.ui.components.PrimaryGlowButton
import com.ttelectronics.trackiiapp.ui.components.SoftActionButton
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.viewmodel.LoginViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.LoginViewModelFactory

@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit, onHome: () -> Unit) {
    val context = LocalContext.current
    val vm: LoginViewModel = viewModel(factory = LoginViewModelFactory(ServiceLocator.authRepository(context)))
    val uiState by vm.uiState.collectAsState()

    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        ?: ""

    LaunchedEffect(androidId) {
        vm.setDeviceUid(androidId)
    }

    if (uiState.isSuccess) onLogin()

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
                        TrackIITextField(
                            label = "Usuario",
                            value = uiState.username,
                            onValueChange = vm::onUsernameChange
                        )
                        TrackIITextField(
                            label = "Contraseña",
                            isPassword = true,
                            value = uiState.password,
                            onValueChange = vm::onPasswordChange
                        )
                        uiState.errorMessage?.let {
                            Text(text = it, color = TTRed, style = MaterialTheme.typography.bodySmall)
                        }
                        PrimaryGlowButton(
                            text = if (uiState.isLoading) "Validando..." else "Iniciar sesión",
                            onClick = vm::login,
                            enabled = !uiState.isLoading,
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
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }
}
