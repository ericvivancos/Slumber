package com.slumber.mobilehub.ui.app

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slumber.mobilehub.domain.model.DeviceConnectionState
import com.slumber.mobilehub.domain.model.DeviceStatus
import com.slumber.mobilehub.domain.model.EventType
import com.slumber.mobilehub.domain.model.MobileHubSnapshot
import com.slumber.mobilehub.domain.model.QuickAction
import com.slumber.mobilehub.domain.model.RiskLevel
import com.slumber.mobilehub.domain.model.RuleSetting
import com.slumber.mobilehub.domain.model.SignalReading
import com.slumber.mobilehub.domain.model.SignalStatus
import com.slumber.mobilehub.domain.model.SlumberMode
import com.slumber.mobilehub.domain.model.TimelineEvent
import com.slumber.mobilehub.ui.theme.Aurora
import com.slumber.mobilehub.ui.theme.DeepNight
import com.slumber.mobilehub.ui.theme.Ember
import com.slumber.mobilehub.ui.theme.GlowMint
import com.slumber.mobilehub.ui.theme.SignalBlue
import com.slumber.mobilehub.ui.theme.SlumberMobileHubTheme

@Composable
fun SlumberMobileHubApp(
    viewModel: SlumberViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SlumberMobileHubApp(
        uiState = uiState,
        onSelectDestination = viewModel::selectDestination,
        onQuickAction = viewModel::onQuickAction
    )
}

@Composable
private fun SlumberMobileHubApp(
    uiState: SlumberAppUiState,
    onSelectDestination: (AppDestination) -> Unit,
    onQuickAction: (com.slumber.mobilehub.domain.model.QuickActionType) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                    HeroCard(snapshot = uiState.snapshot)
                }

                item {
                    NavigationSection(
                        selectedDestination = uiState.selectedDestination,
                        onSelectDestination = onSelectDestination
                    )
                }

                when (uiState.selectedDestination) {
                    AppDestination.Dashboard -> {
                        item {
                            DeviceSection(devices = uiState.snapshot.devices)
                        }

                        item {
                            SignalsSection(signals = uiState.snapshot.signals)
                        }

                        item {
                            ActionsSection(
                                actions = uiState.snapshot.quickActions,
                                onQuickAction = onQuickAction
                            )
                        }
                    }

                    AppDestination.History -> {
                        item {
                            HistorySection(events = uiState.snapshot.timeline)
                        }
                    }

                    AppDestination.Settings -> {
                        item {
                            SettingsSection(rules = uiState.snapshot.rules)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(snapshot: MobileHubSnapshot) {
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
            StatusPill(text = snapshot.mode.toLabel())

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Slumber Mobile Hub",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = snapshot.summary,
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
                    value = "${snapshot.confidencePercent}%"
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Ultima accion",
                    value = snapshot.lastAction
                )
            }

            MetricTile(
                label = "Riesgo actual",
                value = snapshot.riskLevel.toLabel()
            )
        }
    }
}

@Composable
private fun NavigationSection(
    selectedDestination: AppDestination,
    onSelectDestination: (AppDestination) -> Unit
) {
    SectionCard(title = "Secciones") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppDestination.entries.forEach { destination ->
                val isSelected = destination == selectedDestination
                TextButton(
                    onClick = { onSelectDestination(destination) },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }
                    )
                ) {
                    Text(
                        text = destination.label,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
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
private fun ActionsSection(
    actions: List<QuickAction>,
    onQuickAction: (com.slumber.mobilehub.domain.model.QuickActionType) -> Unit
) {
    SectionCard(title = "Acciones rapidas") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            actions.forEachIndexed { index, action ->
                if (index == 0) {
                    Button(
                        onClick = { onQuickAction(action.type) },
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
                        onClick = { onQuickAction(action.type) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = action.title,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = action.description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySection(events: List<TimelineEvent>) {
    SectionCard(title = "Historial reciente") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            events.forEach { event ->
                EventRow(event = event)
            }
        }
    }
}

@Composable
private fun SettingsSection(rules: List<RuleSetting>) {
    SectionCard(title = "Reglas y umbrales") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rules.forEach { rule ->
                RuleRow(rule = rule)
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
    val color = device.state.toColor()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
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
            text = device.state.toLabel(),
            style = MaterialTheme.typography.labelLarge,
            color = color
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
                color = signal.status.toColor().copy(alpha = 0.45f),
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
private fun EventRow(event: TimelineEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(event.type.toColor())
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = event.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = event.timestamp,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RuleRow(rule: RuleSetting) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = rule.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = rule.value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = rule.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

private fun SlumberMode.toLabel(): String = when (this) {
    SlumberMode.MONITORING -> "Modo vigilancia"
    SlumberMode.CALIBRATING -> "Modo calibracion"
    SlumberMode.PAUSED -> "Modo pausa"
}

private fun RiskLevel.toLabel(): String = when (this) {
    RiskLevel.LOW -> "Bajo"
    RiskLevel.MEDIUM -> "Medio"
    RiskLevel.HIGH -> "Alto"
}

private fun DeviceConnectionState.toLabel(): String = when (this) {
    DeviceConnectionState.ACTIVE -> "Activo"
    DeviceConnectionState.CONNECTED -> "Conectado"
    DeviceConnectionState.DEGRADED -> "Degradado"
    DeviceConnectionState.PENDING -> "Pendiente"
    DeviceConnectionState.DISCONNECTED -> "Desconectado"
}

private fun DeviceConnectionState.toColor(): Color = when (this) {
    DeviceConnectionState.ACTIVE -> SignalBlue
    DeviceConnectionState.CONNECTED -> GlowMint
    DeviceConnectionState.DEGRADED -> Ember
    DeviceConnectionState.PENDING -> Ember
    DeviceConnectionState.DISCONNECTED -> Color(0xFFFF7A7A)
}

private fun SignalStatus.toColor(): Color = when (this) {
    SignalStatus.HEALTHY -> GlowMint
    SignalStatus.WARNING -> Ember
    SignalStatus.UNAVAILABLE -> Color(0xFFFF7A7A)
}

private fun EventType.toColor(): Color = when (this) {
    EventType.COMMAND -> GlowMint
    EventType.SIGNAL -> Ember
    EventType.SYSTEM -> SignalBlue
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SlumberPreview() {
    SlumberMobileHubTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SlumberMobileHubApp(
                uiState = SlumberAppUiState(
                    selectedDestination = AppDestination.Dashboard,
                    snapshot = FakePreviewData.snapshot
                ),
                onSelectDestination = {},
                onQuickAction = {}
            )
        }
    }
}

private object FakePreviewData {
    val snapshot = MobileHubSnapshot(
        mode = SlumberMode.MONITORING,
        summary = "El movil coordina el estado del ecosistema Slumber y prepara la integracion con el PC.",
        confidencePercent = 84,
        lastAction = "Overlay enviado",
        riskLevel = RiskLevel.MEDIUM,
        devices = listOf(
            DeviceStatus(
                type = com.slumber.mobilehub.domain.model.DeviceType.PC_AGENT,
                name = "PC Agent",
                description = "Windows listo para recibir comandos multimedia",
                state = DeviceConnectionState.CONNECTED
            )
        ),
        signals = listOf(
            SignalReading("Audio", "Reproduccion detectada", SignalStatus.HEALTHY),
            SignalReading("Inactividad", "11 min", SignalStatus.WARNING)
        ),
        quickActions = listOf(
            QuickAction(
                type = com.slumber.mobilehub.domain.model.QuickActionType.RUN_SLEEP_CHECK,
                title = "Simular comprobacion de sueno",
                description = "Forzar una evaluacion del estado actual."
            )
        ),
        timeline = listOf(
            TimelineEvent(
                id = "preview-1",
                title = "PC enlazado",
                detail = "El preview usa datos estaticos para renderizar la pantalla.",
                timestamp = "Ahora",
                type = EventType.SYSTEM
            )
        ),
        rules = listOf(
            RuleSetting(
                label = "Umbral de inactividad",
                value = "10 min",
                description = "Base de ejemplo para el preview."
            )
        )
    )
}
