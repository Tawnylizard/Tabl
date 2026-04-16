---
name: testing-patterns
description: >
  Provides Tabl-specific test patterns and templates for WorkManager, Room, ViewModel,
  and Compose tests. Use when writing or fixing tests. Triggers on "how to test this",
  "write a test for", "test pattern for WorkManager/Room/ViewModel".
version: "1.0"
maturity: production
---

# Testing Patterns — Tabl

## Primary Reference

Read `.claude/rules/testing.md` for coverage targets and rules.
This skill provides copy-paste templates.

## WorkManager Test Template

```kotlin
@HiltAndroidTest
class MedicationReminderWorkerTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var workManager: TestListenableWorkerBuilder<MedicationReminderWorker>

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        hiltRule.inject()
    }

    @Test fun `worker returns success for valid scheduleId`() = runTest {
        val worker = TestListenableWorkerBuilder<MedicationReminderWorker>(context)
            .setInputData(workDataOf("scheduleId" to 1L))
            .build()

        val result = worker.startWork().await()
        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
    }

    @Test fun `worker returns success silently for deleted medication`() = runTest {
        // scheduleId 999 does not exist — should succeed silently, not crash
        val worker = TestListenableWorkerBuilder<MedicationReminderWorker>(context)
            .setInputData(workDataOf("scheduleId" to 999L))
            .build()

        val result = worker.startWork().await()
        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
    }
}
```

## Room DAO Test Template

```kotlin
@RunWith(AndroidJUnit4::class)
class MedicationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MedicationDao

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.medicationDao()
    }

    @After fun teardown() { db.close() }

    @Test fun `insert and retrieve medication`() = runTest {
        val med = Medication(name = "Аспирин", dosage = "100mg", color = 0xFF0000)
        val id = dao.insert(med)
        val retrieved = dao.getById(id)
        assertThat(retrieved?.name).isEqualTo("Аспирин")
    }
}
```

## ViewModel Test Template (StateFlow)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val medicationRepo: MedicationRepository = mockk()
    private lateinit var viewModel: HomeViewModel

    @Before fun setup() {
        coEvery { medicationRepo.getActiveMedications() } returns flowOf(emptyList())
        viewModel = HomeViewModel(medicationRepo)
    }

    @Test fun `initial state is loading then empty`() = runTest {
        val states = mutableListOf<HomeUiState>()
        val job = launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()
        assertThat(states.last()).isInstanceOf(HomeUiState.Empty::class.java)
        job.cancel()
    }
}
```

## calculateNextTrigger Edge Cases (Critical)

```kotlin
@Test fun `returns null when no days of week match`() {
    val schedule = Schedule(daysOfWeek = emptySet(), times = listOf(LocalTime.of(9, 0)))
    assertThat(calculateNextTrigger(schedule, LocalDate.now())).isNull()
}

@Test fun `returns null after endDate`() {
    val schedule = Schedule(
        daysOfWeek = setOf(1,2,3,4,5,6,7),
        times = listOf(LocalTime.of(9, 0)),
        endDate = LocalDate.now().minusDays(1)
    )
    assertThat(calculateNextTrigger(schedule, LocalDate.now())).isNull()
}

@Test fun `DST transition does not duplicate trigger`() {
    // Verify calculateNextTrigger uses ZonedDateTime, not LocalDateTime arithmetic
    val dstDay = LocalDate.of(2025, 3, 30) // Europe/Moscow always UTC+3, use Europe/Paris
    // ... test with ZoneId.of("Europe/Paris") crossing spring-forward
}
```
