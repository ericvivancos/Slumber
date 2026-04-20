# LAN Discovery Flow

## Goal

Allow `mobile-hub` to discover PCs running `Slumber Service` on the same local network and register one of them as the first managed endpoint.

## Windows service

The Windows `pc-agent` now exposes a lightweight LAN service on port `34821`.

Endpoints:

- `GET /identity`
- `GET /health`

`/identity` returns:

- `deviceId`
- `deviceName`
- `host`
- `port`
- `serviceVersion`
- `capabilities`
- `availability`
- current audio and idle metadata

## Android discovery

The Android app scans the current `/24` subnet and requests `http://<host>:34821/identity`.

When the app runs inside the Android emulator, it also probes the host aliases:

- `10.0.2.2` for the standard Android emulator
- `10.0.3.2` for Genymotion-like environments

When a valid Slumber identity is found, the app adds the device to the discovery list.

## Registration

The user can link one discovered device from the `Buscar dispositivos` section.

The linked device is persisted locally in app preferences so it remains available after restart.

## Follow-up

Once this discovery and registration foundation is stable, the next step is to implement:

- live status sync
- real command delivery
- reconnect handling
