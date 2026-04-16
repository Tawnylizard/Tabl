# Completion — Tabl

## Deployment Plan

### Release Build

```bash
# 1. Обновить versionCode и versionName в build.gradle.kts
# 2. Собрать release APK / AAB
./gradlew bundleRelease

# 3. Подписать через keystore
# (keystore создаётся один раз, хранится безопасно)
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore tabl.keystore app-release.aab tabl-key

# 4. Загрузить AAB в Google Play Console
# Путь: app/build/outputs/bundle/release/app-release.aab
```

### Pre-Release Checklist

- [ ] Все unit тесты проходят (`./gradlew test`)
- [ ] Lint без ошибок (`./gradlew lint`)
- [ ] Протестировано на Android 8.0, 10, 12, 13, 14
- [ ] Протестировано на Xiaomi (MIUI) и Samsung (OneUI)
- [ ] Проверено поведение после перезагрузки
- [ ] Проверен Doze mode (adb shell dumpsys deviceidle force-idle)
- [ ] APK/AAB размер ≤ 15 МБ
- [ ] ProGuard/R8 не ломает Room entities
- [ ] SCHEDULE_EXACT_ALARM request flow работает

### Rollback

Если критический баг после релиза:
1. Google Play Console → Release → Halt rollout
2. Откатиться на предыдущий versionCode через "Re-publish"

---

## CI/CD (GitHub Actions)

```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run Unit Tests
        run: ./gradlew test
      - name: Run Lint
        run: ./gradlew lint
      - name: Build Debug APK
        run: ./gradlew assembleDebug

  instrumented:
    runs-on: macos-latest  # для Android emulator
    steps:
      - uses: actions/checkout@v4
      - name: Run Instrumented Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          script: ./gradlew connectedAndroidTest
```

---

## Monitoring

Поскольку приложение полностью offline без backend, мониторинг минимален:

| Метрика | Инструмент | Что смотреть |
|---------|-----------|--------------|
| Краши | Firebase Crashlytics (опц.) | Crash-free rate ≥ 99.5% |
| ANR | Google Play Console | ANR rate < 0.47% |
| Отзывы | Google Play Console | Оценка ≥ 4.5 |
| Размер APK | Play Console → Android vitals | ≤ 15 МБ |

> Примечание: Crashlytics опционален — если добавлять, то только crash stack traces без PII.

---

## Logging Strategy

```kotlin
// Только в Debug build, никаких PII
if (BuildConfig.DEBUG) {
    Log.d("AlarmScheduler", "Scheduled alarm for scheduleId=$scheduleId at $triggerTime")
}

// В Release — только через Crashlytics non-fatal для критических путей
// Crashlytics.recordException(e) — без имён лекарств в сообщении
```

---

## Handoff Checklists

### Для разработчика

- [ ] Android Studio Iguana 2023.2.1+
- [ ] JDK 17
- [ ] Клонировать репозиторий
- [ ] `./gradlew assembleDebug` — должен собраться без ошибок
- [ ] Запустить на эмуляторе API 34
- [ ] Прочитать `docs/Architecture.md` и `CLAUDE.md`

### Для QA

- [ ] Устройства для тестирования: Pixel (stock Android), Xiaomi, Samsung
- [ ] Тест-кейсы в `docs/Refinement.md` секция "Test Cases"
- [ ] Проверить будильник при: Doze mode, перезагрузке, смене времени
- [ ] Проверить onboarding для SCHEDULE_EXACT_ALARM permission

### Для Google Play

- [ ] App Bundle (AAB) подписан release keystore
- [ ] Target SDK ≥ 34 (требование Google Play 2024+)
- [ ] Privacy Policy (даже для offline apps Google требует)
- [ ] Screenshots: телефон + 10" планшет
- [ ] Feature graphic 1024×500
