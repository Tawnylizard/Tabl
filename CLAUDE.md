# Project: Tabl

## Overview

Tabl — бесплатное нативное Android-приложение для напоминания о приёме лекарств. Ключевое отличие: **настойчивый будильник**, который не умолкает до подтверждения приёма. Нет трекеров здоровья, нет регистрации, нет интернета, нет рекламы.

**Целевые пользователи:** хронические пациенты (диабет, гипертония), пожилые 65+, курсовые пациенты (антибиотики, витамины).

## Problem & Solution

Стандартные push-уведомления легко игнорируются — пользователи смахивают их машинально. Лучшие решения (Medisafe) стали платными, другие (MyTherapy) перегружены трекерами здоровья.

**Решение:** AlarmManager + FullScreenIntent — будильник требует осознанного действия: нажать "Принял", "Снуз" или "Пропустить". Работает полностью offline, 100% локально через Room DB.

## Architecture

Single-module Android app, Clean Architecture + MVVM.

```
UI Layer (Compose)       → HomeScreen, AddMedicationScreen, AlarmActivity, HistoryScreen
Domain Layer             → MedicationViewModel, AlarmViewModel, HistoryViewModel, AlarmScheduler
Data Layer               → MedicationRepository, LogRepository, SettingsRepository
Local Storage            → Room DB (SQLite), DataStore Preferences
Android System           → WorkManager, NotificationManager, AlarmManager
```

**Key Android Components:**
- `MedicationReminderWorker` — CoroutineWorker, показывает push-уведомление
- `NotificationActionReceiver` — BroadcastReceiver, Принял/Снуз/Пропустить
- `BootReceiver` — BOOT_COMPLETED, пересоздаёт WorkManager задачи
- `AlarmActivity` — полноэкранный экран будильника поверх локскрина

## Tech Stack

| Слой | Технология | Версия |
|------|-----------|--------|
| Язык | Kotlin | 2.0+ |
| UI | Jetpack Compose + Material 3 | 1.7+ |
| Архитектура | MVVM + StateFlow | — |
| БД | Room | 2.6+ |
| DI | Hilt | 2.51+ |
| Async | Kotlin Coroutines + Flow | 1.8+ |
| Настройки | DataStore Preferences | 1.1+ |
| Будильники | AlarmManager | Android API |
| Фон | WorkManager | 2.9+ |
| Тестирование | JUnit5, MockK, Espresso | — |
| Min SDK | API 26 (Android 8.0) | 97%+ охват |

## Key Algorithms

```kotlin
// 1. Планирование будильника
fun scheduleNextNotification(medicationId: Long, scheduleId: Long)
  // WorkManager.enqueueUniqueWork("reminder_$scheduleId", REPLACE, ...)

// 2. Вычисление следующего срабатывания O(7) = O(1)
fun calculateNextTrigger(schedule: Schedule): Long?
  // Проверяет daysOfWeek, startDate/endDate, возвращает timestamp или null

// 3. Показ push-уведомления
fun doWork(medicationId, scheduleId, scheduledTime): Result
  // Создаёт лог со статусом MISSED, строит NotificationCompat с 3 кнопками

// 4. Обработка действия пользователя
fun onUserAction(action: UserAction, logId: Long, ...)
  // TAKEN → log TAKEN + decrementStock; SNOOZED → snooze Worker; SKIPPED → log SKIPPED

// 5. Восстановление после перезагрузки
fun onBootCompleted()
  // findAllActive → для каждого schedule → findMissed → scheduleNext
```

## Security Rules

⚠️ **Intent Security:**
- Все PendingIntent: флаг `FLAG_IMMUTABLE` (обязательно API 31+)
- `NotificationActionReceiver`: `android:exported="false"` в AndroidManifest
- `BootReceiver`: `android:exported="true"` только с `android.permission.RECEIVE_BOOT_COMPLETED`
- PendingIntent requestCode = scheduleId (уникален, нет collision)

⚠️ **Data Security:**
- Room DB в app-private storage (недоступен без root)
- Никаких сетевых запросов (нет attack surface)
- Debug logs: только medicationId, никогда имена лекарств или PII
- Crashlytics (если используется): только crash stack traces, без PII

⚠️ **Build Security:**
- ProGuard/R8 минификация в release build
- Keystore хранится вне репозитория
- Нет hardcoded credentials (всё offline, нет API ключей)

## Parallel Execution Strategy

- Используй `Task` tool для независимых подзадач
- Запускай tests, linting, type-checking параллельно
- Для сложных фич: запускай специализированных агентов параллельно

## Swarm Agents

| Сценарий | Агенты | Параллелизм |
|----------|--------|-------------|
| Новая фича | planner + 2-3 impl агентов | Да |
| Рефакторинг | code-reviewer + refactor агенты | Да |
| Баг-фикс | 1 агент | Нет |
| Валидация | 5 validator агентов | Да |

## Git Workflow

- Коммит после каждого логического изменения
- Формат: `type(scope): description`
- Types: feat, fix, refactor, test, docs, chore

## Available Agents

| Агент | Триггер | Описание |
|-------|---------|----------|
| `@planner` | планирование, декомпозиция | Разбивает задачи по Pseudocode.md, планирует реализацию |
| `@code-reviewer` | ревью, проверка кода | Edge cases из Refinement.md, Android best practices |
| `@architect` | архитектура, структура | Architecture.md consistency, MVVM patterns |

## Available Skills

| Скилл | Описание |
|-------|----------|
| `sparc-prd-mini` | SPARC планирование фич (orchestrator) |
| `explore` | Сократовские вопросы → Product Brief |
| `goap-research-ed25519` | GOAP + OODA исследование |
| `problem-solver-enhanced` | 9 модулей + TRIZ |
| `requirements-validator` | Валидация требований INVEST+SMART |
| `brutal-honesty-review` | Безжалостный review кода |
| `project-context` | Контекст проекта, домен |
| `coding-standards` | Стандарты Kotlin/Android |
| `testing-patterns` | Паттерны JUnit5/MockK/Espresso |
| `feature-navigator` | Навигация по roadmap |

## Quick Commands

| Команда | Описание |
|---------|----------|
| `/start` | Bootstrap проекта из документации |
| `/feature [name]` | 4-фазный lifecycle: PLAN → VALIDATE → IMPLEMENT → REVIEW |
| `/next` | Sprint progress + top 3 next tasks |
| `/plan [name]` | Lightweight implementation plan |
| `/test [scope]` | Запуск тестов |
| `/deploy` | Pre-release checklist + сборка |
| `/go [feature]` | Авто-выбор pipeline и реализация |
| `/run mvp` | Bootstrap + реализация всех MVP фич |
| `/docs` | Генерация документации RU/EN |
| `/myinsights [title]` | Захват dev insights |
| `/review` | Ревью изменений |

## 🔍 Development Insights (живая база знаний)

Index: [myinsights/1nsights.md](myinsights/1nsights.md) — check here FIRST before debugging.
⚠️ On error → grep the error string in the index → read only the matched detail file.
Capture new findings: `/myinsights [title]`

## 🔄 Feature Development Lifecycle

New features use the 4-phase lifecycle: `/feature [name]`
1. **PLAN** — sparc-prd-mini (with Gate + external skills) → `docs/features/<n>/sparc/`
2. **VALIDATE** — requirements-validator swarm → score ≥70
3. **IMPLEMENT** — parallel agents from validated docs
4. **REVIEW** — brutal-honesty-review swarm → fix all criticals

Available lifecycle skills in `.claude/skills/`:
- `sparc-prd-mini` (orchestrator, delegates to explore, goap-research-ed25519, problem-solver-enhanced)
- `explore` (Socratic questioning → Product Brief)
- `goap-research-ed25519` (GOAP A* + OODA → Research Findings)
- `problem-solver-enhanced` (9 modules + TRIZ → Solution Strategy)
- `requirements-validator`
- `brutal-honesty-review`

## 📋 Feature Roadmap

Roadmap: [.claude/feature-roadmap.json](.claude/feature-roadmap.json) — single source of truth for feature status.
Sprint progress and next steps are injected automatically at session start.
Quick check: `/next` | Full overview: ask "what should I work on?"
Mark done: `/next [feature-id]` | Update all: `/next update`

## 📝 Implementation Plans

Plans: [docs/plans/](docs/plans/) — lightweight implementation plans.
Create: `/plan <feature-name>` | List: `/plan list` | Mark done: `/plan done <slug>`
For full feature lifecycle (10 docs, 4 phases): `/feature <n>`

## 🚀 Automation Commands

- `/go [feature]` — auto-select pipeline (/plan, /feature) and implement
- `/run` or `/run mvp` — bootstrap + implement all MVP features in a loop
- `/run all` — bootstrap + implement ALL features
- `/docs` — generate bilingual documentation (RU/EN) in /README/
- `/docs update` — update existing documentation

## Resources

- [PRD.md](docs/PRD.md) — product requirements
- [Architecture.md](docs/Architecture.md) — system architecture
- [Specification.md](docs/Specification.md) — data model, user stories
- [Pseudocode.md](docs/Pseudocode.md) — core algorithms
- [Refinement.md](docs/Refinement.md) — edge cases, testing strategy
- [Completion.md](docs/Completion.md) — deployment, CI/CD
- [validation-report.md](docs/validation-report.md) — requirements validation
- [test-scenarios.md](docs/test-scenarios.md) — BDD test scenarios
