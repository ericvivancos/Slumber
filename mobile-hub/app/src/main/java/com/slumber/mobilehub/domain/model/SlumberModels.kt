package com.slumber.mobilehub.domain.model

enum class SlumberMode {
    MONITORING,
    CALIBRATING,
    PAUSED
}

enum class DeviceType {
    MOBILE_HUB,
    PC_AGENT,
    WATCH_AGENT
}

enum class DeviceConnectionState {
    ACTIVE,
    CONNECTED,
    DEGRADED,
    PENDING,
    DISCONNECTED
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

enum class SignalStatus {
    HEALTHY,
    WARNING,
    UNAVAILABLE
}

enum class EventType {
    COMMAND,
    SIGNAL,
    SYSTEM
}

enum class QuickActionType {
    RUN_SLEEP_CHECK,
    CONNECT_PC,
    OPEN_SETTINGS,
    VIEW_HISTORY,
    DISCOVER_DEVICES
}

data class DeviceStatus(
    val type: DeviceType,
    val name: String,
    val description: String,
    val state: DeviceConnectionState
)

data class SignalReading(
    val label: String,
    val value: String,
    val status: SignalStatus
)

data class QuickAction(
    val type: QuickActionType,
    val title: String,
    val description: String
)

data class TimelineEvent(
    val id: String,
    val title: String,
    val detail: String,
    val timestamp: String,
    val type: EventType
)

data class RuleSetting(
    val label: String,
    val value: String,
    val description: String
)

data class SlumberServiceEndpoint(
    val id: String,
    val deviceName: String,
    val host: String,
    val port: Int,
    val serviceVersion: String,
    val capabilities: List<String>,
    val availability: String,
    val isLinked: Boolean
)

data class DeviceDiscoveryState(
    val isScanning: Boolean,
    val statusMessage: String,
    val linkedDevice: SlumberServiceEndpoint?,
    val discoveredDevices: List<SlumberServiceEndpoint>
)

data class MobileHubSnapshot(
    val mode: SlumberMode,
    val summary: String,
    val confidencePercent: Int,
    val lastAction: String,
    val riskLevel: RiskLevel,
    val devices: List<DeviceStatus>,
    val signals: List<SignalReading>,
    val quickActions: List<QuickAction>,
    val timeline: List<TimelineEvent>,
    val rules: List<RuleSetting>,
    val discovery: DeviceDiscoveryState
)
