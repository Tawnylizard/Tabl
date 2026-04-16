---
name: project-context
description: >
  Loads full Tabl project context — architecture, data model, algorithms, open features.
  Use at the start of any implementation session or when you need to understand
  how a component fits into the overall system. Triggers on "what is this project",
  "remind me of the architecture", "what's the data model".
version: "1.0"
maturity: production
---

# Project Context — Tabl

## What This Skill Does

Provides a structured read of the Tabl codebase context so you can answer
"where does this fit?" without re-reading all docs from scratch.

## Files to Read (in order)

1. `CLAUDE.md` — single-page project overview, tech stack, key algorithms
2. `docs/Architecture.md` — full component diagram and layer responsibilities
3. `docs/Specification.md` — data model: Medication, Schedule, MedicationLog entities
4. `.claude/feature-roadmap.json` — current feature status and dependencies

## Output Format

After reading, produce a context summary:

```
## Project: Tabl

**State:** [which features are "next" vs "planned"]
**Stack:** Kotlin + Jetpack Compose + Room + WorkManager + Hilt
**Current focus:** [features with status="next" from roadmap]

### Data Model (key entities)
- Medication: id, name, dosage, color, stockCount, pausedAt
- Schedule: id, medicationId, times[], daysOfWeek, startDate, endDate, intervalDays
- MedicationLog: id, scheduleId, scheduledAt, status, takenAt

### Active Algorithms
1. calculateNextTrigger(schedule, fromDate) → Instant?
2. scheduleReminder(scheduleId) → WorkRequest
3. showNotification(medicationId, scheduleId, logId)
4. handleAction(action, logId) → Unit
5. rescheduleAll() → Unit (called on BOOT_COMPLETED)

### Open TODOs
[from git grep TODO *.kt]
```

## When to Use

- Starting a new feature — run this first to orient yourself
- Reviewing a PR — understand component boundaries before checking code
- Debugging — understand what layer should own the failing behavior
