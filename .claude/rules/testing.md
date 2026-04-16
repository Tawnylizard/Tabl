# Testing Rules — Tabl

## Coverage Targets

- Unit tests: **≥80% line coverage** for domain + data layers
- Integration tests: cover all Room DB migrations and Worker flows
- E2E (Espresso): cover 3 critical user journeys (see below)

## Test Organization

```
app/src/test/                    # Unit tests (JUnit5 + MockK)
├── data/
│   └── repository/              # MedicationRepository, LogRepository tests
├── domain/
│   └── scheduler/               # NotificationScheduler, calculateNextTrigger tests
└── ui/
    └── viewmodel/               # ViewModel state tests

app/src/androidTest/             # Integration + E2E (Espresso + JUnit4)
├── data/
│   └── local/                   # Room DB migration tests
├── worker/                      # MedicationReminderWorker integration tests
└── e2e/                         # Critical user journeys
```

## Unit Test Rules

- Use **MockK** for mocking — not Mockito
- Use **JUnit5** (`@Test`, `@BeforeEach`) for unit tests
- Test naming: `given_[state]_when_[action]_then_[outcome]`
- Always test: happy path, null inputs, boundary values, error cases
- `calculateNextTrigger()` MUST have tests for: empty daysOfWeek, endDate in past, DST boundary

## Integration Test Rules

- Room DB tests: use `Room.inMemoryDatabaseBuilder()` — never the real DB
- WorkManager tests: use `TestListenableWorkerBuilder` from `work-testing`
- BootReceiver tests: use Robolectric or instrumented test with `ActivityScenario`

## Critical E2E Journeys (Espresso)

1. **Add medication → appears in list**
   - Launch app → tap FAB → fill form → save → verify card visible

2. **Notification action → logged in history**
   - Inject test WorkManager → trigger worker → tap "Принял" → verify TAKEN in DB

3. **Reboot → alarms restored**
   - Setup medications → send BOOT_COMPLETED broadcast → verify WorkManager tasks re-enqueued

## Test Commands

```bash
# Unit tests
./gradlew test

# Lint
./gradlew lint

# All tests including instrumented (requires emulator)
./gradlew connectedAndroidTest

# Coverage report
./gradlew testDebugUnitTestCoverage
```

## What NOT to Test

- Android framework classes (Activity lifecycle, Context) — test only your logic
- Room query syntax — trust Room's compile-time verification
- Hilt injection — trust Hilt's compile-time verification
- Compose rendering details — test ViewModel state, not pixel-perfect UI
