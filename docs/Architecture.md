# Architecture — Tabl

## Architecture Style

**Clean Architecture + MVVM** — стандарт для Android с Jetpack Compose.  
**Offline-first** — нет сетевых зависимостей.  
**Single-module** — простое приложение, монолит оправдан.

---

## High-Level Diagram

```mermaid
graph TB
    subgraph UI["UI Layer (Compose)"]
        A[HomeScreen] 
        B[AddMedicationScreen]
        D[HistoryScreen]
        E[SettingsScreen]
    end

    subgraph Domain["Domain Layer"]
        F[MedicationViewModel]
        H[HistoryViewModel]
        I[AlarmScheduler]
    end

    subgraph Data["Data Layer"]
        J[MedicationRepository]
        K[LogRepository]
        L[SettingsRepository]
    end

    subgraph Local["Local Storage"]
        M[(Room DB)]
        N[DataStore Prefs]
    end

    subgraph System["Android System"]
        O[WorkManager]
        P[NotificationManager]
    end

    A --> F
    B --> F
    D --> H
    E --> L
    F --> J
    F --> I
    H --> K
    I --> O
    J --> M
    K --> M
    L --> N
    F --> I
    F --> J
```

---

## Component Breakdown

### UI Layer

| Экран | Описание |
|-------|----------|
| `HomeScreen` | Список лекарств с карточками. FAB для добавления. |
| `AddMedicationScreen` | Форма: название, доза, цвет, запас. Вложенный `ScheduleForm`. |
| `HistoryScreen` | Лог приёмов с фильтрацией и процентом соблюдения. |
| `SettingsScreen` | Звук, снуз, тема, язык. |

### Domain Layer

| Компонент | Описание |
|-----------|----------|
| `NotificationScheduler` | Управляет регистрацией/отменой задач WorkManager. |
| `MedicationViewModel` | Состояние списка лекарств, CRUD операции. |
| `HistoryViewModel` | Агрегация логов, расчёт процента соблюдения. |

### Data Layer

| Компонент | Описание |
|-----------|----------|
| `MedicationRepository` | CRUD для Medication и Schedule. |
| `LogRepository` | Запись событий приёма, запросы истории. |
| `SettingsRepository` | Чтение/запись настроек через DataStore. |

### Android Components

| Компонент | Тип | Назначение |
|-----------|-----|-----------|
| `MedicationReminderWorker` | CoroutineWorker | Показывает push-уведомление в назначенное время |
| `NotificationActionReceiver` | BroadcastReceiver | Принимает действия из уведомления (Принял/Снуз/Пропустить) |
| `BootReceiver` | BroadcastReceiver | BOOT_COMPLETED — пересоздаёт WorkManager задачи |

---

## Technology Stack

| Слой | Технология | Версия | Обоснование |
|------|-----------|--------|-------------|
| Язык | Kotlin | 2.0+ | Официальный язык Android |
| UI | Jetpack Compose | 1.7+ | Современный декларативный UI, Material 3 |
| Навигация | Navigation Compose | 2.8+ | Официальная библиотека |
| Архитектура | MVVM + StateFlow | — | Официальный Android паттерн |
| БД | Room | 2.6+ | Типобезопасный SQLite ORM |
| DI | Hilt | 2.51+ | Стандарт Android DI |
| Async | Kotlin Coroutines + Flow | 1.8+ | Реактивные потоки данных |
| Настройки | DataStore Preferences | 1.1+ | Замена SharedPreferences |
| Фон | WorkManager | 2.9+ | Планирование, устойчивое к перезагрузке |
| Тестирование | JUnit5, MockK, Espresso | — | Unit + Integration тесты |
| Min SDK | API 26 | Android 8.0 | Охват 97%+ устройств |
| Target SDK | API 35 | Android 15 | Актуальная версия |

---

## Database Schema

```sql
CREATE TABLE medications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    dose TEXT,
    color_index INTEGER NOT NULL DEFAULT 0,
    stock_count INTEGER,
    stock_threshold INTEGER,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at INTEGER NOT NULL
);

CREATE TABLE schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medication_id INTEGER NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    time_hour INTEGER NOT NULL,       -- 0-23
    time_minute INTEGER NOT NULL,     -- 0-59
    days_of_week TEXT NOT NULL,       -- JSON: [1,2,3] или [] = каждый день
    start_date TEXT,                  -- ISO date "2026-04-16" или null
    end_date TEXT                     -- ISO date или null
);

CREATE TABLE medication_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medication_id INTEGER NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    schedule_id INTEGER NOT NULL,
    scheduled_at INTEGER NOT NULL,    -- timestamp запланированного приёма
    action_at INTEGER,                -- timestamp действия пользователя
    status TEXT NOT NULL,             -- 'TAKEN'|'MISSED'|'SNOOZED'|'SKIPPED'
    snooze_count INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_logs_medication_date 
    ON medication_logs(medication_id, scheduled_at);
```

---

## Notification Architecture Detail

```
Scheduled time arrives
        ↓
WorkManager fires MedicationReminderWorker
        ↓
MedicationReminderWorker.doWork():
  1. Build NotificationCompat:
       - priority = PRIORITY_HIGH  (heads-up — всплывает поверх экрана)
       - addAction("Принял",      NotificationActionReceiver, ACTION_TAKEN)
       - addAction("Снуз",        NotificationActionReceiver, ACTION_SNOOZE)
       - addAction("Пропустить",  NotificationActionReceiver, ACTION_SKIP)
       - contentIntent = MainActivity PendingIntent (тап открывает приложение)
  2. notificationManager.notify(scheduleId, notification)
        ↓
User sees heads-up notification (звук + вибрация через Notification Channel)
        ↓
NotificationActionReceiver.onReceive(action):
  - ACTION_TAKEN   → log TAKEN, cancel notification, scheduleNext()
  - ACTION_SNOOZE  → log SNOOZED, cancel notification, enqueue snooze Worker
  - ACTION_SKIP    → log SKIPPED, cancel notification, scheduleNext()
  - [no action]    → after 30 min repeat notification fires (max 2 times)
```

---

## Permissions Required

```xml
<!-- Уведомления (API 33+) — запрос у пользователя при первом запуске -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Восстановление после перезагрузки -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- WorkManager требует для надёжного запуска в фоне -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

```

> WorkManager использует внутренний scheduling без `SCHEDULE_EXACT_ALARM` — меньше разрешений, меньше вероятность блокировки Xiaomi/Samsung Battery Optimization.

---

## Security Architecture

- Нет сетевых запросов → нет attack surface для MITM
- Нет аккаунта → нет credentials для кражи
- Room DB хранится в app-private storage (недоступен без root)
- Нет sensitive данных в логах (только medicationId, без названий)

---

## Scalability Considerations

Приложение offline-first, нет серверной части — масштабирование не требуется.  
Room DB с SQLite эффективно обрабатывает тысячи записей истории.  
WorkManager с OneTimeWorkRequest: каждое расписание — одна задача, практически без ограничений на количество.
