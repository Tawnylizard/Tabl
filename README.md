# Tabl — Medication Reminder

Free native Android app for tracking medication schedules and intake history. No accounts, no backend, all data stays on device.

## Features

- Add medications with name, dose, color, stock count
- Flexible schedules: specific days, intervals, time ranges, start/end dates
- Push notifications with action buttons (Taken / Snooze / Skip) — works on lock screen
- Intake history with compliance percentage
- Pill counter with low-stock alerts
- Dark theme / system theme

## Tech Stack

Kotlin · Jetpack Compose · Room · WorkManager · Hilt · Material 3 · DataStore

**Min SDK:** Android 8.0 (API 26)  
**Target SDK:** Android 14 (API 34)

## Getting Started

```bash
# Build
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

See [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) for full setup and architecture.

## Project Status

Toolkit ready, implementation in progress.

| Feature | Status |
|---------|--------|
| F1 Medication CRUD | Next |
| F2 Schedule configuration | Next |
| F3 Push notifications | Next |
| F4 Notification actions | Planned |
| F5 Intake history | Planned |

Run `/next` in Claude Code to see what to implement next.

## Documentation

| Doc | Contents |
|-----|----------|
| [Architecture](docs/Architecture.md) | Component diagram, layer boundaries |
| [Specification](docs/Specification.md) | Data model entities |
| [Pseudocode](docs/Pseudocode.md) | Core algorithms |
| [PRD](docs/PRD.md) | Features, user stories, NFRs |
| [Refinement](docs/Refinement.md) | Edge cases, security hardening |
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | Dev setup, commands, patterns |

## License

MIT
