---
description: Build and install Tabl on connected device or emulator. $ARGUMENTS: device serial (optional).
---

# /run $ARGUMENTS

Build debug APK and install on device/emulator.

## Prerequisites Check

```bash
# Verify device connected
adb devices

# Verify Android SDK available
./gradlew --version
```

## Build + Install

```bash
# Assemble debug build
./gradlew assembleDebug

# Install (specify device if multiple connected)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If `$ARGUMENTS` contains a device serial:
```bash
adb -s $ARGUMENTS install -r app/build/outputs/apk/debug/app-debug.apk
```

## Launch App

```bash
adb shell am start -n com.example.tabl/.ui.MainActivity
```

## Test Notification (quick smoke test)

```bash
# Check WorkManager tasks queued
adb shell dumpsys jobscheduler | grep tabl

# Force trigger notification (debug only)
adb shell am broadcast -a com.example.tabl.DEBUG_TRIGGER_NOTIFICATION
```

## Doze Mode Test

```bash
# Force device into doze
adb shell dumpsys deviceidle force-idle

# Wait ~2 min, verify notification still fires
# Then exit doze
adb shell dumpsys deviceidle unforce
```

## Common Failures

| Error | Fix |
|-------|-----|
| `INSTALL_FAILED_VERSION_DOWNGRADE` | `adb uninstall com.example.tabl` first |
| `device offline` | Reconnect USB, `adb kill-server && adb start-server` |
| No notifications on Xiaomi | Settings → App → Tabl → Battery → No restrictions |
| No notifications on Samsung | Settings → Device Care → Battery → Background usage limits → OFF for Tabl |
