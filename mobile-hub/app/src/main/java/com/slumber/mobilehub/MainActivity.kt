package com.slumber.mobilehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slumber.mobilehub.ui.theme.Aurora
import com.slumber.mobilehub.ui.theme.DeepNight
import com.slumber.mobilehub.ui.theme.GlowMint
import com.slumber.mobilehub.ui.theme.SlumberMobileHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SlumberMobileHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SlumberApp()
                }
            }
        }
    }
}

@Composable
fun SlumberApp(modifier: Modifier = Modifier) {
    val uiState = rememberDashboardState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DeepNight, Aurora)
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    HeroCard(
                        mode = uiState.mode,
                        summary = uiState.summary,
                        confidence = uiState.confidence
                    )
                }

                item {
                    DeviceSection(devices = uiState.devices)
                }

                item {
                    SignalsSection(signals = uiState.signals)
                }

                item {
                    ActionsSection(actions = uiState.actions)
                }
            }
        }
    }
}

@Composable
private fun HeroCard(mode: String, summary: String, confidence: String) {
    Card(
        modifier = Modifier.border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.24f), GlowMint.copy(alpha = 0.4f))
            ),
            shape = RoundedCornerShape(28.dp)
        ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusPill(text = mode)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Slumber Mobile Hub",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Confianza",
                    value = confidence
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Ultima accion",
                    value = "Overlay enviado"
                )
            }
        }
    }
}

@Composable
private fun DeviceSection(devices: List<DeviceStatus>) {
    SectionCard(title = "Dispositivos") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            devices.forEach { device ->
                DeviceRow(device = device)
            }
        }
    }
}

@Composable
private fun SignalsSection(signals: List<SignalReading>) {
    SectionCard(title = "Senales activas") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            signals.forEach { signal ->
                SignalChip(signal = signal)
            }
        }
    }
}

@Composable
private fun ActionsSection(actions: List<QuickAction>) {
    SectionCard(title = "Acciones rapidas") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            actions.forEachIndexed { index, action ->
                if (index == 0) {
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    TextButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = action.title,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(device.color)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = device.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = device.status,
            style = MaterialTheme.typography.labelLarge,
            color = device.color
        )
    }
}

@Composable
private fun SignalChip(signal: SignalReading) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = signal.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = signal.value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatusPill(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(CircleShape)
            .background(GlowMint.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        color = GlowMint,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun rememberDashboardState(): DashboardUiState {
    return DashboardUiState(
        mode = "Modo vigilancia",
        summary = "El movil actua como centro de control: recibe senales, estima somnolencia y decide cuando avisar o pausar en el PC.",
        confidence = "84%",
        devices = listOf(
            DeviceStatus(
                name = "PC Agent",
                description = "Windows listo para recibir comandos multimedia",
                status = "Conectado",
                color = GlowMint
            ),
            DeviceStatus(
                name = "Watch Agent",
                description = "Pulso y movimiento pendientes de enlazar",
                status = "Pendiente",
                color = MaterialTheme.colorScheme.tertiary
            ),
            DeviceStatus(
                name = "Mobile Hub",
                description = "Motor de reglas local en preparacion",
                status = "Activo",
                color = MaterialTheme.colorScheme.primary
            )
        ),
        signals = listOf(
            SignalReading("Audio", "Reproduccion detectada"),
            SignalReading("Inactividad", "11 min"),
            SignalReading("Pulso", "Sin reloj"),
            SignalReading("Riesgo", "Medio")
        ),
        actions = listOf(
            QuickAction("Simular comprobacion de sueno"),
            QuickAction("Vincular PC Agent"),
            QuickAction("Configurar reglas"),
            QuickAction("Abrir historial de eventos")
        )
    )
}

data class DashboardUiState(
    val mode: String,
    val summary: String,
    val confidence: String,
    val devices: List<DeviceStatus>,
    val signals: List<SignalReading>,
    val actions: List<QuickAction>
)

data class DeviceStatus(
    val name: String,
    val description: String,
    val status: String,
    val color: Color
)

data class SignalReading(
    val label: String,
    val value: String
)

data class QuickAction(
    val title: String
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SlumberPreview() {
    SlumberMobileHubTheme {
        SlumberApp()
    }
}
