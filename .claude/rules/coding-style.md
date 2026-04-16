# Coding Style — Tabl (Kotlin / Android)

## Language & Framework Conventions

- **Kotlin 2.0+** — use idiomatic Kotlin: data classes, sealed classes, extension functions
- **Jetpack Compose** — stateless composables, hoist state to ViewModel
- **StateFlow / Flow** — all UI state via `StateFlow<UiState>`, never `LiveData`
- **Hilt** — all dependencies injected, no manual `getInstance()` singletons
- **Room** — always use `@Transaction` for multi-table writes
- **Coroutines** — `viewModelScope` for UI, `Dispatchers.IO` for DB/file operations

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes/Objects | PascalCase | `MedicationRepository`, `AlarmScheduler` |
| Functions | camelCase | `scheduleNextNotification()` |
| Variables | camelCase | `medicationId`, `snoozeCount` |
| Constants | SCREAMING_SNAKE | `ACTION_TAKEN`, `CHANNEL_REMINDERS` |
| Composables | PascalCase | `MedicationCard()`, `HomeScreen()` |
| Files | PascalCase matching class | `MedicationRepository.kt` |
| Test files | `[Subject]Test.kt` | `AlarmSchedulerTest.kt` |

## File Organization

```
app/src/main/java/com/app/tabl/
├── data/
│   ├── local/          # Room DAOs, Database, Entities
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models (Medication, Schedule, MedicationLog)
│   └── scheduler/      # NotificationScheduler
├── ui/
│   ├── home/           # HomeScreen + HomeViewModel
│   ├── add/            # AddMedicationScreen + AddMedicationViewModel
│   ├── history/        # HistoryScreen + HistoryViewModel
│   └── settings/       # SettingsScreen
├── worker/             # MedicationReminderWorker
├── receiver/           # NotificationActionReceiver, BootReceiver
└── di/                 # Hilt modules
```

## Compose Rules

- No business logic in composables — only UI rendering and event callbacks
- Use `remember { }` and `derivedStateOf { }` to prevent unnecessary recompositions
- Minimum touch target: 56dp for interactive elements (WCAG)
- Always provide `contentDescription` for icon-only buttons

## Room Rules

- Use `@Transaction` for any operation touching multiple tables
- Define indexes for query-heavy columns (already defined: `medication_id, scheduled_at`)
- Never perform Room operations on the main thread — always `withContext(Dispatchers.IO)`
- Use `Flow<T>` return types for observable queries

## WorkManager Rules

- Use `enqueueUniqueWork(uniqueName, REPLACE, ...)` — prevents duplicate workers
- Tag every work request: `addTag("med_$scheduleId")` for easy cancellation
- Pass only primitive data via `workDataOf()` — no serialized objects
- Always return `Result.success()` from `doWork()` unless retry is needed

## Error Handling

| Situation | Handling |
|-----------|----------|
| Room DB IOException | Retry 3x with exponential backoff, then silent fail |
| SecurityException from PendingIntent | Log warning, show in-app notification prompt |
| Null medication in Worker | Return `Result.success()` silently (deleted/paused) |
| Concurrent alarms same time | Each has unique `requestCode = scheduleId` |

## Known Gotchas

### Android / Kotlin
- `WorkManager.enqueueUniqueWork` with `REPLACE` cancels the pending work and reschedules — use this deliberately when rescheduling alarms.
- `PendingIntent.FLAG_MUTABLE` is required if the Intent extras need to be modified later; use `FLAG_IMMUTABLE` for notification action intents (extras are fixed).
- Room `@TypeConverter` for `Set<Int>` (daysOfWeek stored as JSON string) — register converter in `@Database` annotation, not in DAO.
- DataStore `preferences.first()` in a non-coroutine context throws — always call from `suspend` function or `runBlocking` (only in tests).
- `NotificationChannel` must be created before posting the first notification; recreating with same ID is safe (updates description/name but not importance).
- On Xiaomi (MIUI) and Samsung (OneUI), WorkManager tasks can be killed by Battery Optimization — guide users to exempt the app during onboarding.
