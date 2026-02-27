package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ttelectronics.trackiiapp.ui.theme.TTGreen
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.viewmodel.ReworkReleaseViewModel
import com.ttelectronics.trackiiapp.ui.viewmodel.ReworkReleaseViewModelFactory

@Composable
fun ReworkReleaseScreen(
    lotNumber: String,
    onReleaseSuccess: () -> Unit,
    onContinueRework: () -> Unit,
    onHome: () -> Unit
) {
    val scannerRepository = ServiceLocator.scannerRepository(androidx.compose.ui.platform.LocalContext.current)
    val viewModel: ReworkReleaseViewModel = viewModel(factory = ReworkReleaseViewModelFactory(scannerRepository))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.releaseSuccess) {
        if (uiState.releaseSuccess) {
            onReleaseSuccess()
        }
    }

    TrackIIBackground(glowOffsetX = 20.dp, glowOffsetY = 40.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = TTGreen,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "¿Liberar la orden?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "Si se libera, continuará directamente con la ruta. De lo contrario, registra el retrabajo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TTTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryGlowButton(
                            text = if (uiState.isSubmitting) "Liberando..." else "Sí, liberar",
                            onClick = { viewModel.release(lotNumber) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )
                        SoftActionButton(
                            text = "No, continuar con retrabajo",
                            onClick = onContinueRework,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )
                    }
                }
                if (uiState.isSubmitting) {
                    Spacer(modifier = Modifier.size(16.dp))
                    CircularProgressIndicator()
                }
                uiState.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.releaseSuccess) TTGreen else TTRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
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
