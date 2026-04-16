# Tabl — Development Guide

## Overview

Tabl is a free native Android medication reminder app. No backend, no accounts, all data local.
See `CLAUDE.md` for a single-page project summary. See `docs/` for full SPARC documentation.

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Android Studio | Hedgehog+ (2023.1) | [developer.android.com](https://developer.android.com/studio) |
| JDK | 17 | bundled with Android Studio |
| Android SDK | API 26–34 | via SDK Manager |
| Kotlin | 1.9+ | via Gradle |

## Project Structure

```
app/src/main/java/com/example/tabl/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         ← Room database, version tracking
│   │   ├── entity/                ← Medication, Schedule, MedicationLog
│   │   └── dao/                   ← DAOs per entity
│   └── repository/                ← MedicationRepository, etc.
├── domain/
│   ├── model/                     ← domain data classes (non-Room)
│   └── scheduler/
│       └── NotificationScheduler.kt  ← WorkManager enqueue logic
├── ui/
│   ├── MainActivity.kt
│   ├── theme/                     ← Material 3 theme
│   ├── home/                      ← HomeScreen + HomeViewModel
│   ├── medication/                ← MedicationFormScreen + ViewModel
│   ├── history/                   ← HistoryScreen + HistoryViewModel
│   └── settings/                  ← SettingsScreen + ViewModel
├── worker/
│   └── MedicationReminderWorker.kt
├── receiver/
│   ├── BootReceiver.kt
│   └── NotificationActionReceiver.kt
└── di/
    └── AppModule.kt               ← Hilt module
```

## Quick Start

```bash
# 1. Clone
git clone <repo-url> && cd Tabl

# 2. Open in Android Studio (or build from CLI)
./gradlew assembleDebug

# 3. Install on device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or use `/start` command in Claude Code for full scaffold generation.

## Key Commands (Claude Code)

| Command | What it does |
|---------|-------------|
| `/start` | Scaffold full Android project from docs |
| `/next` | Show next recommended feature to implement |
| `/go F1` | Full implementation kickoff for feature F1 |
| `/plan feature-name` | Create lightweight implementation plan |
| `/test` | Run unit tests (`./gradlew test`) |
| `/test lint` | Run lint (`./gradlew lint`) |
| `/run` | Build + install on connected device |
| `/deploy` | Pre-release checklist + release build |
| `/docs arch` | Show architecture documentation |
| `/myinsights` | Capture a development insight |
| `/feature US-01` | Full feature lifecycle: plan → validate → implement → review |

## Architecture Summary

```
UI (Compose) → ViewModel → Repository → Room DB
                         → NotificationScheduler → WorkManager
                                                  → MedicationReminderWorker
                                                  → NotificationCompat (heads-up)
                                                  → NotificationActionReceiver
                                                        → logAction + scheduleNext
```

**Layer rules:**
- UI only calls ViewModel — never Repository or Room directly
- ViewModel only calls Repository/Scheduler — no Room imports
- Worker only calls Repository — no ViewModel references
- All DI via Hilt (`@HiltViewModel`, `@HiltWorker`, `@AndroidEntryPoint`)

## Data Model

```kotlin
// 3 Room entities
Medication(id, name, dosage, color, stockCount, stockThreshold, pausedAt)
Schedule(id, medicationId, times, daysOfWeek, startDate, endDate, intervalDays)
MedicationLog(id, scheduleId, scheduledAt, status, takenAt, note)

// MedicationLog.status values
enum class LogStatus { SCHEDULED, TAKEN, MISSED, SKIPPED, SNOOZED }
```

## Notification Flow

1. `NotificationScheduler.scheduleReminder(scheduleId)` → enqueues `MedicationReminderWorker`
2. Worker fires → queries DB → calls `NotificationManager.notify()` with 3 action buttons
3. User taps action → `NotificationActionReceiver.onReceive()` → updates log status
4. Receiver calls `NotificationScheduler.scheduleNext()` for next occurrence
5. On reboot → `BootReceiver` calls `NotificationScheduler.rescheduleAll()`

## Testing

```bash
# Unit tests
./gradlew test

# Lint
./gradlew lint

# Coverage report
./gradlew testDebugUnitTest jacocoTestReport
# open app/build/reports/jacoco/index.html

# Full check before PR
./gradlew check
```

Coverage targets: ≥80% for `calculateNextTrigger`, `NotificationScheduler`, `MedicationRepository`.

## Security Checklist

Before every PR:
- [ ] All `PendingIntent` use `FLAG_IMMUTABLE`
- [ ] `NotificationActionReceiver` has `android:exported="false"` in manifest
- [ ] No `Log.*` calls outside `if (BuildConfig.DEBUG)` blocks
- [ ] No medication names or doses in log messages
- [ ] Input validation: name not blank, stock count ≥ 0

## Known Device Issues

| Device | Issue | Workaround |
|--------|-------|------------|
| Xiaomi (MIUI) | Kills background workers | User must disable Battery Optimization for Tabl |
| Samsung (OneUI) | Background app restrictions | User must allow "Background activity" for Tabl |
| All Android 13+ | Notification permission | Request `POST_NOTIFICATIONS` at first launch |

## Feature Roadmap

See `.claude/feature-roadmap.json` for current status. Priority order:

1. **F1** Medication CRUD — _next_
2. **F2** Schedule configuration — _next_ (needs F1)
3. **F3** Push notifications — _next_ (needs F2)
4. **F4** Action buttons in notification — _planned_ (needs F3)
5. **F5** Intake history + compliance % — _planned_ (needs F4)

Run `/next` to get the current recommended feature.

## Contributing

Follow `.claude/rules/git-workflow.md` for commit format:
```
type(scope): description

Types: feat, fix, refactor, test, docs, chore
Scopes: alarm, ui, data, history, settings, ci
```

Run `/feature <story-id>` to start any new user story through the full lifecycle.
