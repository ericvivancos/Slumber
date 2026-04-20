package com.slumber.mobilehub.data

import com.slumber.mobilehub.domain.model.DeviceConnectionState
import com.slumber.mobilehub.domain.model.DeviceStatus
import com.slumber.mobilehub.domain.model.DeviceType
import com.slumber.mobilehub.domain.model.EventType
import com.slumber.mobilehub.domain.model.MobileHubSnapshot
import com.slumber.mobilehub.domain.model.QuickAction
import com.slumber.mobilehub.domain.model.QuickActionType
import com.slumber.mobilehub.domain.model.RiskLevel
import com.slumber.mobilehub.domain.model.RuleSetting
import com.slumber.mobilehub.domain.model.SignalReading
import com.slumber.mobilehub.domain.model.SignalStatus
import com.slumber.mobilehub.domain.model.SlumberMode
import com.slumber.mobilehub.domain.model.TimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSlumberRepository : SlumberRepository {
    private val state = MutableStateFlow(buildInitialSnapshot())

    override val snapshot: StateFlow<MobileHubSnapshot> = state

    override fun triggerAction(action: QuickActionType) {
        val current = state.value
        val updated = when (action) {
            QuickActionType.RUN_SLEEP_CHECK -> current.copy(
                confidencePercent = 91,
                lastAction = "Comprobacion manual enviada al PC",
                riskLevel = RiskLevel.HIGH,
                timeline = listOf(
                    TimelineEvent(
                        id = "evt-manual-check",
                        title = "Chequeo manual lanzado",
                        detail = "El hub ha solicitado al PC que muestre un overlay inmediato.",
                        timestamp = "Ahora",
                        type = EventType.COMMAND
                    )
                ) + current.timeline
            )

            QuickActionType.CONNECT_PC -> current.copy(
                lastAction = "Handshake con PC preparado",
                timeline = listOf(
                    TimelineEvent(
                        id = "evt-connect-pc",
                        title = "Contrato Mobile <-> PC listo",
                        detail = "La aplicacion queda preparada para implementar el transporte real.",
                        timestamp = "Ahora",
                        type = EventType.SYSTEM
                    )
                ) + current.timeline
            )

            QuickActionType.OPEN_SETTINGS -> current.copy(
                lastAction = "Revision de reglas sugerida"
            )

            QuickActionType.VIEW_HISTORY -> current.copy(
                lastAction = "Historial actualizado"
            )
        }

        state.value = updated
    }

    private fun buildInitialSnapshot(): MobileHubSnapshot {
        return MobileHubSnapshot(
            mode = SlumberMode.MONITORING,
            summary = "El movil actua como centro de control: consolida senales, estima riesgo y decide cuando intervenir sobre el PC.",
            confidencePercent = 84,
            lastAction = "Overlay enviado",
            riskLevel = RiskLevel.MEDIUM,
            devices = listOf(
                DeviceStatus(
                    type = DeviceType.PC_AGENT,
                    name = "PC Agent",
                    description = "Windows listo para recibir comandos multimedia",
                    state = DeviceConnectionState.CONNECTED
                ),
                DeviceStatus(
                    type = DeviceType.WATCH_AGENT,
                    name = "Watch Agent",
                    description = "Pulso y movimiento pendientes de enlazar",
                    state = DeviceConnectionState.PENDING
                ),
                DeviceStatus(
                    type = DeviceType.MOBILE_HUB,
                    name = "Mobile Hub",
                    description = "Motor de reglas local en preparacion",
                    state = DeviceConnectionState.ACTIVE
                )
            ),
            signals = listOf(
                SignalReading("Audio", "Reproduccion detectada", SignalStatus.HEALTHY),
                SignalReading("Inactividad", "11 min", SignalStatus.WARNING),
                SignalReading("Pulso", "Sin reloj", SignalStatus.UNAVAILABLE),
                SignalReading("Riesgo", "Medio", SignalStatus.WARNING)
            ),
            quickActions = listOf(
                QuickAction(
                    type = QuickActionType.RUN_SLEEP_CHECK,
                    title = "Simular comprobacion de sueno",
                    description = "Fuerza una evaluacion y prepara un comando de overlay."
                ),
                QuickAction(
                    type = QuickActionType.CONNECT_PC,
                    title = "Vincular PC Agent",
                    description = "Prepara el punto de entrada para el handshake con Windows."
                ),
                QuickAction(
                    type = QuickActionType.OPEN_SETTINGS,
                    title = "Configurar reglas",
                    description = "Revisa umbrales, cooldown y prioridad de senales."
                ),
                QuickAction(
                    type = QuickActionType.VIEW_HISTORY,
                    title = "Abrir historial de eventos",
                    description = "Consulta las ultimas decisiones del sistema."
                )
            ),
            timeline = listOf(
                TimelineEvent(
                    id = "evt-003",
                    title = "Riesgo elevado a medio",
                    detail = "La reproduccion continua y la inactividad supera el umbral base.",
                    timestamp = "Hace 2 min",
                    type = EventType.SIGNAL
                ),
                TimelineEvent(
                    id = "evt-002",
                    title = "PC Agent reporta audio activo",
                    detail = "El dispositivo Windows confirma reproduccion en curso.",
                    timestamp = "Hace 5 min",
                    type = EventType.SYSTEM
                ),
                TimelineEvent(
                    id = "evt-001",
                    title = "Sesion iniciada en Mobile Hub",
                    detail = "Se cargaron reglas locales y estado inicial del orquestador.",
                    timestamp = "Hace 8 min",
                    type = EventType.SYSTEM
                )
            ),
            rules = listOf(
                RuleSetting(
                    label = "Umbral de inactividad",
                    value = "10 min",
                    description = "Tiempo base antes de pedir confirmacion al usuario."
                ),
                RuleSetting(
                    label = "Cuenta atras overlay",
                    value = "15 s",
                    description = "Margen antes de pausar el contenido si no hay respuesta."
                ),
                RuleSetting(
                    label = "Cooldown",
                    value = "5 min",
                    description = "Evita repetir avisos seguidos tras una cancelacion."
                )
            )
        )
    }
}
