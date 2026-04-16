---
description: Bootstrap the Tabl Android project from documentation. Generates project skeleton, all source files, Gradle configs, Room DB schema, and basic tests. $ARGUMENTS: optional flags --skip-tests, --dry-run.
---

# /start $ARGUMENTS

## Purpose

One-command project generation from SPARC documentation → working Android project with `./gradlew assembleDebug`.

## Prerequisites

- Documentation in `docs/` directory (SPARC output)
- Android Studio Iguana 2023.2.1+ or CLI tools
- JDK 17
- Git initialized

## Process

### Phase 1: Foundation (sequential — everything depends on this)

1. **Read all project docs** to build full context:
   - `docs/Architecture.md` → module structure, tech stack, component breakdown
   - `docs/Specification.md` → data model (Medication, Schedule, MedicationLog), user stories
   - `docs/Pseudocode.md` → 6 core algorithms, state machine
   - `docs/PRD.md` → features F1-F7, NFRs
   - `docs/Refinement.md` → edge cases, error handling, testing strategy
   - `docs/Completion.md` → CI/CD, deployment checklist

2. **Generate root configs:**
   - `build.gradle.kts` (project-level, with Kotlin 2.0, Hilt, AGP)
   - `app/build.gradle.kts` (app-level, all dependencies from Architecture.md tech stack)
   - `gradle/libs.versions.toml` (version catalog)
   - `settings.gradle.kts`
   - `gradle.properties`
   - `.gitignore` (exclude keystore, build/, .gradle/)
   - `local.properties.example`

3. **Git commit:** `chore: project root configuration`

### Phase 2: Source Files (parallel via Task tool ⚡)

Launch 3 parallel tasks:

#### Task A: Data Layer ⚡

Read and use as source:
- `docs/Specification.md` → data model → Room entities + DAOs
- `docs/Architecture.md` → DB schema SQL → Room schema verification

Generate:
- `data/local/AppDatabase.kt` (Room database, version 1)
- `data/local/entity/MedicationEntity.kt`
- `data/local/entity/ScheduleEntity.kt`
- `data/local/entity/MedicationLogEntity.kt`
- `data/local/dao/MedicationDao.kt` (CRUD + Flow queries)
- `data/local/dao/ScheduleDao.kt`
- `data/local/dao/MedicationLogDao.kt` (logs by medication + date range)
- `data/local/converter/DaysOfWeekConverter.kt` (Set<Int> ↔ JSON)
- `data/repository/MedicationRepository.kt`
- `data/repository/LogRepository.kt`
- `data/repository/SettingsRepository.kt` (DataStore)
- `domain/model/Medication.kt`, `Schedule.kt`, `MedicationLog.kt`

**Commits:** `feat(data): Room DB schema and DAOs`, `feat(data): repository implementations`

#### Task B: Domain + Worker Layer ⚡

Read and use as source:
- `docs/Pseudocode.md` → Algorithms 1-6 → Kotlin implementations
- `docs/Architecture.md` → notification flow → Worker + Receiver

Generate:
- `domain/scheduler/NotificationScheduler.kt` (Algorithm 1: scheduleNextNotification)
- `domain/scheduler/AlarmCalculator.kt` (Algorithm 2: calculateNextTrigger)
- `worker/MedicationReminderWorker.kt` (Algorithm 3: doWork with NotificationCompat)
- `receiver/NotificationActionReceiver.kt` (Algorithm 4: onUserAction)
- `receiver/BootReceiver.kt` (Algorithm 6: onBootCompleted)
- `domain/service/StockService.kt` (Algorithm 5: checkStockThreshold)
- `di/AppModule.kt` (Hilt: Room, WorkManager, NotificationManager)
- `di/RepositoryModule.kt`
- `AndroidManifest.xml` (permissions, receivers with exported flags)

**Commits:** `feat(alarm): notification scheduler and WorkManager worker`, `feat(receiver): boot receiver and action handler`

#### Task C: UI Layer ⚡

Read and use as source:
- `docs/PRD.md` → F1-F7 features → screens and components
- `docs/Specification.md` → user stories US-01–US-10 → UI flows

Generate:
- `ui/theme/Theme.kt`, `Color.kt`, `Type.kt` (Material 3)
- `ui/navigation/NavGraph.kt`
- `ui/home/HomeScreen.kt` + `HomeViewModel.kt`
- `ui/home/components/MedicationCard.kt`
- `ui/add/AddMedicationScreen.kt` + `AddMedicationViewModel.kt`
- `ui/add/components/ScheduleForm.kt`
- `ui/history/HistoryScreen.kt` + `HistoryViewModel.kt` (compliance % calculation)
- `ui/settings/SettingsScreen.kt`
- `MainActivity.kt`
- `TabApplication.kt` (Hilt @HiltAndroidApp)

**Commits:** `feat(ui): home screen and medication list`, `feat(ui): add medication form and schedule`, `feat(ui): history screen with compliance`

### Phase 3: Integration (sequential)

1. **Verify cross-module imports** (domain models used correctly in UI and data layers)
2. **Build debug APK:** `./gradlew assembleDebug`
3. *No Docker/database migration — Room schema auto-created on first launch*
4. **Run unit tests:** `./gradlew test`
5. **Run lint:** `./gradlew lint`
6. **Git commit:** `chore: verify build and tests pass`

### Phase 4: Finalize

1. Generate/update `README.md` with quick start instructions
2. Final git tag: `git tag v0.1.0-scaffold`
3. Report summary: files generated, build status, what needs manual attention (keystore, Play Store setup)

## Output

After /start completes:
```
app/src/main/java/com/app/tabl/
├── data/local/          # Room entities, DAOs, AppDatabase
├── data/repository/     # Repository implementations
├── domain/model/        # Domain models
├── domain/scheduler/    # NotificationScheduler, AlarmCalculator
├── di/                  # Hilt modules
├── receiver/            # BootReceiver, NotificationActionReceiver
├── ui/                  # All screens + ViewModels
├── worker/              # MedicationReminderWorker
└── MainActivity.kt, TabApplication.kt

AndroidManifest.xml      # Permissions, receivers, activities
build.gradle.kts         # All dependencies
gradle/libs.versions.toml
```

## Flags

- `--skip-tests` — skip test file generation (faster, not recommended)
- `--dry-run` — show plan without executing

## Estimated Time

- With parallel tasks: ~15-20 minutes
- Files generated: ~40-50 files
- Commits: ~8-10 commits

## Error Recovery

If a task fails mid-generation:
- All completed phases are committed to git
- Re-run `/start` — it detects existing files and skips completed phases
- Or fix the issue manually and continue from where it stopped

## Swarm Agents Used

| Phase | Agents | Parallelism |
|-------|--------|-------------|
| Phase 1 | Main | Sequential |
| Phase 2 | 3 Task tools (data, domain, UI) | ⚡ Parallel |
| Phase 3 | Main | Sequential |
| Phase 4 | Main | Sequential |
