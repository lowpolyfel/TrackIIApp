package com.ttelectronics.trackiiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttelectronics.trackiiapp.ui.components.TrackIIBackground
import com.ttelectronics.trackiiapp.ui.theme.TTBlue
import com.ttelectronics.trackiiapp.ui.theme.TTBlueDark
import com.ttelectronics.trackiiapp.ui.theme.TTRed
import com.ttelectronics.trackiiapp.ui.theme.TTTextSecondary
import com.ttelectronics.trackiiapp.ui.theme.TTYellow // <-- Agregamos el color amarillo

@Composable
fun ScrapLocationCheckScreen(
    lotNumber: String,
    partNumber: String,
    scrapAmount: Int,
    qtyIn: Int,
    onThisArea: () -> Unit,
    onPreviousArea: () -> Unit,
    onBack: () -> Unit
) {
    TrackIIBackground(glowOffsetX = 40.dp, glowOffsetY = (-30).dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER BOTÓN VOLVER (Fondo blanco sólido, flecha oscura)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver", tint = TTBlueDark)
                }
            }

            // CONTENIDO PRINCIPAL
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // TARJETA DE ALERTA DE DIFERENCIA
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = TTRed,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Se detectó una diferencia",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = TTBlueDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "El sistema esperaba más piezas según el paso anterior.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TTTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Piezas Recibidas", color = TTTextSecondary, fontSize = 14.sp)
                                Text("$qtyIn", fontWeight = FontWeight.Black, fontSize = 28.sp, color = TTBlueDark)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Diferencia (Scrap)", color = TTTextSecondary, fontSize = 14.sp)
                                Text("-$scrapAmount", fontWeight = FontWeight.Black, fontSize = 28.sp, color = TTRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // PREGUNTA CLAVE (Ahora en Amarillo para que resalte al máximo)
                Text(
                    text = "¿Dónde se detectó el scrap?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = TTBlueDark,
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÓN: EN ESTA ÁREA (Blanco)
                Button(
                    onClick = onThisArea,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TTBlueDark)
                    Text(
                        text = "Ocurrió en ESTA área",
                        color = TTBlueDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN: EN ÁREA ANTERIOR (Azul)
                Button(
                    onClick = onPreviousArea,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TTBlue)
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = Color.White)
                    Text(
                        text = "Viene de un área ANTERIOR",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            // Espaciador inferior para equilibrar visualmente
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}