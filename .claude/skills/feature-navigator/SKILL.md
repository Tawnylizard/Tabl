---
name: feature-navigator
description: >
  Maps Tabl feature IDs to implementation locations and dependencies.
  Use when picking the next feature to implement or understanding what's blocked.
  Triggers on "what should I work on next", "which feature", "F1 F2 F3", "roadmap".
version: "1.0"
maturity: production
---

# Feature Navigator — Tabl

## How to Use

1. Read `.claude/feature-roadmap.json` for current status
2. Apply dependency rules below
3. Output: recommended next feature + files to create

## Dependency Chain

```
F1 Medication CRUD  ──►  F2 Schedules  ──►  F3 Notifications  ──►  F4 Action Buttons
                                                                          │
                                                                     F5 History ──► F9 Widget
                                                                     F6 Counter      F10 Export
                         F7 Settings ◄──────────────────────────────┘
F8 Dark Theme ◄── F1
```

## Feature → Files Mapping

| Feature | Key Files to Create |
|---------|---------------------|
| F1 — Medication CRUD | `data/local/entity/Medication.kt`, `data/local/dao/MedicationDao.kt`, `data/repository/MedicationRepository.kt`, `ui/home/HomeScreen.kt`, `ui/home/HomeViewModel.kt`, `ui/medication/MedicationFormScreen.kt` |
| F2 — Schedules | `data/local/entity/Schedule.kt`, `data/local/dao/ScheduleDao.kt`, `domain/scheduler/NotificationScheduler.kt`, `ui/medication/ScheduleFormScreen.kt` |
| F3 — Push Notifications | `worker/MedicationReminderWorker.kt`, `receiver/BootReceiver.kt`, `receiver/NotificationActionReceiver.kt`, `data/local/entity/MedicationLog.kt` |
| F4 — Action Buttons | extend `NotificationActionReceiver`, `data/local/dao/MedicationLogDao.kt` |
| F5 — History | `ui/history/HistoryScreen.kt`, `ui/history/HistoryViewModel.kt`, `data/repository/MedicationLogRepository.kt` |
| F6 — Tablet Counter | extend `Medication` entity + `MedicationRepository`, `ui/home/StockBadge.kt` |
| F7 — Settings | `ui/settings/SettingsScreen.kt`, `data/local/datastore/AppPreferences.kt` |
| F8 — Dark Theme | `ui/theme/Theme.kt` update, `ui/settings/SettingsScreen.kt` theme picker |

## Recommendation Logic

```
next_features = roadmap.features WHERE status == "next"
blocked = [f for f in next_features if any dep.status != "done" for dep in f.depends_on]
ready = [f for f in next_features if f not in blocked]
recommend = ready[0] by priority order: must > should > could
```

## When Starting a Feature

Run `/plan [feature-name]` → creates `docs/plans/[feature].md`
Then use the `planner` agent to break it into tasks.
