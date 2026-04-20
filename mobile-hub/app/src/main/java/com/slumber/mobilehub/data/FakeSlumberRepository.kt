package com.slumber.mobilehub.data

import android.content.Context
import com.slumber.mobilehub.domain.model.DeviceConnectionState
import com.slumber.mobilehub.domain.model.DeviceDiscoveryState
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
import com.slumber.mobilehub.domain.model.SlumberServiceEndpoint
import com.slumber.mobilehub.domain.model.TimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class FakeSlumberRepository(
    context: Context,
    private val discoveryService: LanDiscoveryService = LanDiscoveryService()
) : SlumberRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val state = MutableStateFlow(buildInitialSnapshot())

    override val snapshot: StateFlow<MobileHubSnapshot> = state

    init {
        val linkedDevice = loadLinkedDevice()
        if (linkedDevice != null) {
            state.value = withLinkedDevice(state.value, linkedDevice)
        }
    }

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

            QuickActionType.CONNECT_PC,
            QuickActionType.DISCOVER_DEVICES -> current.copy(
                lastAction = "Busqueda de dispositivos preparada"
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

    override suspend fun refreshDiscovery() {
        val current = state.value
        state.value = current.copy(
            lastAction = "Escaneando la red local",
            discovery = current.discovery.copy(
                isScanning = true,
                statusMessage = "Buscando servicios Slumber en la LAN..."
            )
        )

        val scanResult = discoveryService.discoverServices()
        val linked = loadLinkedDevice()
        val devices = scanResult.devices.map { candidate ->
            candidate.copy(isLinked = linked?.id == candidate.id)
        }

        val statusMessage = if (devices.isEmpty()) {
            "No se han encontrado servicios Slumber en esta red."
        } else {
            "Se detectaron ${devices.size} dispositivo(s) disponibles."
        }

        state.value = withLinkedDevice(
            state.value.copy(
                lastAction = "Escaneo LAN completado",
                discovery = state.value.discovery.copy(
                    isScanning = false,
                    statusMessage = statusMessage,
                    discoveredDevices = devices
                ),
                timeline = listOf(
                    TimelineEvent(
                        id = "evt-scan-${System.currentTimeMillis()}",
                        title = "Busqueda LAN finalizada",
                        detail = statusMessage,
                        timestamp = "Ahora",
                        type = EventType.SYSTEM
                    )
                ) + state.value.timeline
            ),
            linked
        )
    }

    override fun linkDevice(device: SlumberServiceEndpoint) {
        saveLinkedDevice(device)
        state.value = withLinkedDevice(
            state.value.copy(
                lastAction = "PC vinculado: ${device.deviceName}",
                timeline = listOf(
                    TimelineEvent(
                        id = "evt-link-${device.id}",
                        title = "Dispositivo vinculado",
                        detail = "El hub usara ${device.deviceName} como primer endpoint gestionado.",
                        timestamp = "Ahora",
                        type = EventType.SYSTEM
                    )
                ) + state.value.timeline
            ),
            device
        )
    }

    private fun withLinkedDevice(
        snapshot: MobileHubSnapshot,
        linkedDevice: SlumberServiceEndpoint?
    ): MobileHubSnapshot {
        val discoveryState = snapshot.discovery.copy(
            linkedDevice = linkedDevice,
            discoveredDevices = snapshot.discovery.discoveredDevices.map { device ->
                device.copy(isLinked = linkedDevice?.id == device.id)
            },
            statusMessage = linkedDevice?.let {
                "Dispositivo vinculado: ${it.deviceName}"
            } ?: snapshot.discovery.statusMessage
        )

        val updatedDevices = snapshot.devices.map { device ->
            if (device.type == DeviceType.PC_AGENT) {
                if (linkedDevice != null) {
                    device.copy(
                        name = linkedDevice.deviceName,
                        description = "${linkedDevice.host}:${linkedDevice.port} listo para control remoto",
                        state = DeviceConnectionState.CONNECTED
                    )
                } else {
                    device.copy(
                        description = "Ningun PC vinculado todavia",
                        state = DeviceConnectionState.PENDING
                    )
                }
            } else {
                device
            }
        }

        return snapshot.copy(
            devices = updatedDevices,
            discovery = discoveryState
        )
    }

    private fun saveLinkedDevice(device: SlumberServiceEndpoint) {
        prefs.edit()
            .putString(KEY_LINKED_DEVICE, JSONObject().apply {
                put("id", device.id)
                put("deviceName", device.deviceName)
                put("host", device.host)
                put("port", device.port)
                put("serviceVersion", device.serviceVersion)
                put("availability", device.availability)
                put("capabilities", device.capabilities.joinToString(","))
            }.toString())
            .apply()
    }

    private fun loadLinkedDevice(): SlumberServiceEndpoint? {
        val raw = prefs.getString(KEY_LINKED_DEVICE, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            val capabilities = json.optString("capabilities")
                .split(',')
                .filter { it.isNotBlank() }

            SlumberServiceEndpoint(
                id = json.getString("id"),
                deviceName = json.getString("deviceName"),
                host = json.getString("host"),
                port = json.getInt("port"),
                serviceVersion = json.optString("serviceVersion", "unknown"),
                capabilities = capabilities,
                availability = json.optString("availability", "available"),
                isLinked = true
            )
        }.getOrNull()
    }

    private fun buildInitialSnapshot(): MobileHubSnapshot {
        return MobileHubSnapshot(
            mode = SlumberMode.MONITORING,
            summary = "El movil actua como centro de control: descubre PCs en la misma red, los vincula y prepara el control remoto de Slumber.",
            confidencePercent = 84,
            lastAction = "Hub inicializado",
            riskLevel = RiskLevel.MEDIUM,
            devices = listOf(
                DeviceStatus(
                    type = DeviceType.PC_AGENT,
                    name = "PC Agent",
                    description = "Ningun PC vinculado todavia",
                    state = DeviceConnectionState.PENDING
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
                SignalReading("Audio", "Esperando PC vinculado", SignalStatus.UNAVAILABLE),
                SignalReading("Inactividad", "Sin datos remotos", SignalStatus.UNAVAILABLE),
                SignalReading("Pulso", "Sin reloj", SignalStatus.UNAVAILABLE),
                SignalReading("Riesgo", "Medio", SignalStatus.WARNING)
            ),
            quickActions = listOf(
                QuickAction(
                    type = QuickActionType.DISCOVER_DEVICES,
                    title = "Buscar dispositivos Slumber",
                    description = "Escanea la LAN para detectar PCs con el servicio activo."
                ),
                QuickAction(
                    type = QuickActionType.RUN_SLEEP_CHECK,
                    title = "Simular comprobacion de sueno",
                    description = "Fuerza una evaluacion y prepara un comando de overlay."
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
                    title = "Base LAN preparada",
                    detail = "La app queda lista para descubrir dispositivos Slumber en la red local.",
                    timestamp = "Hace 1 min",
                    type = EventType.SYSTEM
                ),
                TimelineEvent(
                    id = "evt-002",
                    title = "Contrato Mobile <-> PC definido",
                    detail = "El protocolo inicial ya esta documentado para el siguiente paso.",
                    timestamp = "Hace 4 min",
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
            ),
            discovery = DeviceDiscoveryState(
                isScanning = false,
                statusMessage = "Todavia no se ha realizado ninguna busqueda en la red local.",
                linkedDevice = null,
                discoveredDevices = emptyList()
            )
        )
    }

    private companion object {
        const val PREFS_NAME = "slumber_mobile_hub"
        const val KEY_LINKED_DEVICE = "linked_device"
    }
}
