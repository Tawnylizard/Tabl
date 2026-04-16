---
name: coding-standards
description: >
  Enforces Tabl Kotlin/Android coding conventions. Checks new code against
  project rules before it's written. Use when writing new files, naming things,
  or deciding how to structure a component. Triggers on "how should I name this",
  "is this the right pattern", "coding conventions".
version: "1.0"
maturity: production
---

# Coding Standards — Tabl

## Primary Reference

Always read `.claude/rules/coding-style.md` before giving any coding advice.
That file is authoritative — this skill is an entry point to it.

## Quick Checklist

Before writing any Kotlin file, verify:

### Naming
| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `MedicationRepository` |
| Functions | camelCase | `calculateNextTrigger` |
| Constants | SCREAMING_SNAKE | `MAX_SNOOZE_COUNT` |
| Composables | PascalCase + noun | `MedicationCard` |
| ViewModels | Screen + ViewModel | `HomeViewModel` |
| DAOs | Entity + Dao | `MedicationDao` |

### File Placement
```
app/src/main/java/com/example/tabl/
├── data/
│   ├── local/entity/      ← Room entities
│   ├── local/dao/         ← Room DAOs
│   └── repository/        ← Repository impls
├── domain/
│   ├── model/             ← domain data classes
│   └── scheduler/         ← NotificationScheduler
├── ui/
│   └── [screen]/          ← Screen.kt + ViewModel.kt co-located
├── worker/                ← MedicationReminderWorker
├── receiver/              ← BootReceiver, NotificationActionReceiver
└── di/                    ← Hilt modules
```

### Must-Have Patterns
- All PendingIntents: `FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT`
- WorkManager: `enqueueUniqueWork("reminder_$scheduleId", REPLACE, ...)`
- Room queries: use `Dispatchers.IO` — never on main thread
- Logs: wrapped in `if (BuildConfig.DEBUG)` — no PII ever
- State: `StateFlow` not `LiveData` in ViewModels

## Known Gotchas

Read `.claude/rules/coding-style.md` section "Known Gotchas" for the full list.
Key ones:
- WorkManager `enqueue()` ≠ `enqueueUniqueWork()` — duplicates silently
- `Set<Int>` needs `@TypeConverter` in Room
- `NotificationChannel` must be created before first notification
- Xiaomi/Samsung kill background processes — document in release notes
