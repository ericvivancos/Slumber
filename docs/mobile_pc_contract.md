# Mobile <-> PC Contract

## Purpose

This document defines the first transport-friendly contract between `mobile-hub` and the Windows `pc-agent`.

The goal of this version is not to lock the final protocol, but to establish:

- stable message categories
- minimal payload fields
- a versioned envelope
- a shared vocabulary for future implementation

## Envelope Rules

All messages should carry:

- `schemaVersion`
- an id (`commandId` or `eventId`)
- a timestamp in ISO-8601 UTC
- a typed payload

Version `1` is the initial contract.

## Mobile -> PC

### Command envelope

```json
{
  "schemaVersion": 1,
  "commandId": "cmd-001",
  "type": "SHOW_OVERLAY",
  "issuedAt": "2026-04-20T20:00:00Z",
  "payload": {
    "reason": "Sleep risk increased after long inactivity",
    "riskLevel": "MEDIUM",
    "countdownSeconds": 15
  }
}
```

### Supported command types

- `HEARTBEAT`
- `SHOW_OVERLAY`
- `PAUSE_MEDIA`
- `REFRESH_STATUS`

## PC -> Mobile

### Status envelope

```json
{
  "schemaVersion": 1,
  "eventId": "evt-001",
  "type": "STATUS_SNAPSHOT",
  "emittedAt": "2026-04-20T20:00:05Z",
  "payload": {
    "deviceName": "Eric-PC",
    "audioPlaying": true,
    "isIdle": true,
    "idleSeconds": 662,
    "lastCommandResult": "Overlay acknowledged"
  }
}
```

### Supported event types

- `STATUS_SNAPSHOT`
- `OVERLAY_DISMISSED`
- `MEDIA_PAUSED`
- `ERROR`

## Notes

- JSON is the current reference format because it is easy to inspect and debug on both Android and Windows.
- The Kotlin source of this contract lives in `mobile-hub/app/src/main/java/com/slumber/mobilehub/domain/protocol/PcAgentContract.kt`.
- The future `shared` module can absorb these DTOs once the transport layer is implemented on both ends.
