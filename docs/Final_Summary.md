# Tabl — Executive Summary

## Overview

Tabl — бесплатное нативное Android-приложение для напоминания о приёме лекарств. Ключевое отличие: **настойчивый будильник**, который открывается поверх локскрина и не умолкает до подтверждения приёма. Никаких трекеров здоровья, регистрации, интернета или платных функций.

## Problem & Solution

**Problem:** Люди пропускают лекарства потому что стандартные push-уведомления легко смахнуть. Лучшие решения (Medisafe) стали платными, другие (MyTherapy) перегружены ненужными функциями.

**Solution:** Минималистичное Android-приложение с AlarmManager + FullScreenIntent — будильник требует осознанного действия: нажать "Принял", "Снуз" или "Пропустить". Работает полностью offline, хранит данные локально в Room DB.

## Target Users

- **Хронические пациенты** (диабет, гипертония) — 2-5 таблеток/день
- **Пожилые 65+** — простой интерфейс, крупные кнопки
- **Курсовые пациенты** — антибиотики, витамины 7-30 дней

## Key Features (MVP v1.0)

1. **Настойчивый будильник** — AlarmManager + FLAG_INSISTENT, поверх локскрина
2. **Экран подтверждения** — Принял / Снуз / Пропустить
3. **Управление лекарствами** — название, доза, расписание, курс
4. **История приёмов** — лог с процентом соблюдения
5. **Счётчик таблеток** — напоминание о пополнении запасов

## Technical Approach

- **Архитектура:** Clean Architecture + MVVM, Single-module Android app
- **Стек:** Kotlin 2.0, Jetpack Compose, Room DB, AlarmManager, Hilt, Coroutines
- **Хранение:** 100% локально, SQLite через Room, нет сервера
- **Min SDK:** API 26 (Android 8.0) — охват 97%+ устройств
- **Размер APK:** цель ≤ 15 МБ

## Competitive Differentiation

```
Tabl = Надёжность будильника Pillo
     + Простота MedTimer  
     + 0 трекеров здоровья
     + 0 рублей
```

## Success Metrics

| Метрика | Цель |
|---------|------|
| Retention D7 | > 60% |
| Retention D30 | > 35% |
| Play Store оценка | ≥ 4.5 |
| Crash-free rate | ≥ 99.5% |

## Timeline

| Фаза | Содержание | Срок |
|------|-----------|------|
| MVP v1.0 | Все Must Have функции | 6-8 недель |
| v1.1 | Виджет, интервальный приём, тёмная тема | +4 недели |
| v2.0 | Бэкап, экспорт, опциональная синхронизация | +8 недель |

## Immediate Next Steps

1. Инициализировать Android проект (Kotlin + Compose + Room + Hilt)
2. Реализовать Room schema и AlarmScheduler
3. Построить AlarmActivity с полноэкранным Intent
4. Собрать HomeScreen со списком лекарств

## Documentation Package

- `PRD.md` — Product Requirements
- `Solution_Strategy.md` — Анализ проблемы (SCQA + TRIZ)
- `Specification.md` — User Stories + Data Model
- `Pseudocode.md` — Алгоритмы и State Machine
- `Architecture.md` — Системный дизайн + DB Schema
- `Refinement.md` — Edge Cases + BDD Test Scenarios
- `Completion.md` — CI/CD + Deploy + Checklist
- `Research_Findings.md` — Анализ рынка и конкурентов
