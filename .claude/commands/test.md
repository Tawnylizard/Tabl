---
description: Run tests for Tabl Android project. $ARGUMENTS: scope (unit, lint, all, coverage) or empty for unit tests.
---

# /test $ARGUMENTS

## What You Do

Run the appropriate test suite based on `$ARGUMENTS`.

## Test Modes

| Command | What runs |
|---------|-----------|
| `/test` | `./gradlew test` — unit tests only |
| `/test unit` | `./gradlew test` — unit tests only |
| `/test lint` | `./gradlew lint` — lint checks |
| `/test all` | `./gradlew test lint` — unit + lint |
| `/test coverage` | `./gradlew testDebugUnitTestCoverage` — unit tests + coverage report |
| `/test e2e` | `./gradlew connectedAndroidTest` — requires running emulator |

## Process

1. Parse `$ARGUMENTS` to determine mode (default: `unit`)
2. Run the appropriate Gradle command
3. Report results:
   - Tests passed/failed count
   - Coverage % (if coverage mode)
   - Lint warnings/errors (if lint mode)
4. On failure: show failing test names and first error
5. Suggest fixes for common failures

## Coverage Targets (from docs/Refinement.md)

- Domain layer (`scheduler/`): ≥80%
- Data layer (`repository/`, `local/`): ≥80%
- UI ViewModels: ≥70%

## Critical Test Areas

Per `docs/Refinement.md` and `docs/test-scenarios.md`:

- `AlarmCalculatorTest` — `calculateNextTrigger()` edge cases (DST, endDate, empty days)
- `NotificationSchedulerTest` — WorkManager enqueue/cancel
- `HistoryViewModelTest` — compliance % formula: `TAKEN / (TAKEN + MISSED + SKIPPED) × 100`
- `MedicationRepositoryTest` — CRUD with Room in-memory DB
- `BootReceiverTest` — BOOT_COMPLETED broadcast handling

## Common Failures & Fixes

```
"Cannot access database on the main thread"
→ Wrap DB call in withContext(Dispatchers.IO) or use runBlocking in tests

"WorkManager is not initialized"  
→ Initialize WorkManager in test setup: WorkManagerTestInitHelper.initializeTestWorkManager(context)

"Room schema export directory not set"
→ Add exportSchema = false to @Database annotation (or set export path)
```
