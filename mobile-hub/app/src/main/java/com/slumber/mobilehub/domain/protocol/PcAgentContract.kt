package com.slumber.mobilehub.domain.protocol

enum class PcCommandType {
    HEARTBEAT,
    SHOW_OVERLAY,
    PAUSE_MEDIA,
    REFRESH_STATUS
}

enum class PcEventType {
    STATUS_SNAPSHOT,
    OVERLAY_DISMISSED,
    MEDIA_PAUSED,
    ERROR
}

data class PcCommandEnvelope(
    val schemaVersion: Int,
    val commandId: String,
    val type: PcCommandType,
    val issuedAt: String,
    val payload: PcCommandPayload
)

data class PcCommandPayload(
    val reason: String,
    val riskLevel: String,
    val countdownSeconds: Int? = null
)

data class PcStatusEnvelope(
    val schemaVersion: Int,
    val eventId: String,
    val type: PcEventType,
    val emittedAt: String,
    val payload: PcStatusPayload
)

data class PcStatusPayload(
    val deviceName: String,
    val audioPlaying: Boolean,
    val isIdle: Boolean,
    val idleSeconds: Int,
    val lastCommandResult: String
)

object PcAgentContractExamples {
    val sampleCommand = PcCommandEnvelope(
        schemaVersion = 1,
        commandId = "cmd-001",
        type = PcCommandType.SHOW_OVERLAY,
        issuedAt = "2026-04-20T20:00:00Z",
        payload = PcCommandPayload(
            reason = "Sleep risk increased after long inactivity",
            riskLevel = "MEDIUM",
            countdownSeconds = 15
        )
    )

    val sampleStatus = PcStatusEnvelope(
        schemaVersion = 1,
        eventId = "evt-001",
        type = PcEventType.STATUS_SNAPSHOT,
        emittedAt = "2026-04-20T20:00:05Z",
        payload = PcStatusPayload(
            deviceName = "Eric-PC",
            audioPlaying = true,
            isIdle = true,
            idleSeconds = 662,
            lastCommandResult = "Overlay acknowledged"
        )
    )
}
