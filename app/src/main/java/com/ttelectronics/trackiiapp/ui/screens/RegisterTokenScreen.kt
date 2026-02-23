package com.ttelectronics.trackiiapp.ui.screens

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
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.components.TrackIITextField
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.RegisterTokenViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.RegisterTokenViewModelFactory

@Composable
fun RegisterTokenScreen(
    onContinue: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val vm: RegisterTokenViewModel = viewModel(
        factory = RegisterTokenViewModelFactory(ServiceLocator.authRepository(context))
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(uiState.isValid) {
        if (uiState.isValid) onContinue(uiState.tokenCode.trim())
    }

    TrackIIBackground(glowOffsetX = (-20).dp, glowOffsetY = 40.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ingresa tu token",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Validaremos el acceso antes de crear tu cuenta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TrackIITextField(
                            label = "Token de acceso",
                            value = uiState.tokenCode,
                            onValueChange = vm::onTokenChange
                        )
                        uiState.errorMessage?.let {
                            Text(text = it, color = TTRed, style = MaterialTheme.typography.bodySmall)
                        }
                        PrimaryGlowButton(
                            text = if (uiState.isLoading) "Validando..." else "Continuar",
                            onClick = vm::validateToken,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SoftActionButton(
                            text = "Volver",
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
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
