---
description: Pre-release checklist and release build for Tabl. $ARGUMENTS: env (debug, release) — default: release.
---

# /deploy $ARGUMENTS

## Pre-Release Checklist

Run through all items from `docs/Completion.md` before building:

### Code Quality
- [ ] All unit tests pass: `./gradlew test`
- [ ] Lint clean: `./gradlew lint`
- [ ] No TODO/FIXME comments in release-critical paths

### Android-Specific
- [ ] Tested on Android 8.0 (API 26) — minimum supported
- [ ] Tested on Android 12 (API 31) — FLAG_IMMUTABLE required
- [ ] Tested on Android 14 (API 34) — latest stable
- [ ] Tested on Xiaomi (MIUI) — Battery Optimization behavior
- [ ] Tested on Samsung (OneUI) — Background restrictions
- [ ] WorkManager tasks fire after reboot (BOOT_COMPLETED)
- [ ] Doze mode: `adb shell dumpsys deviceidle force-idle` → notification fires
- [ ] POST_NOTIFICATIONS permission request flow works (API 33+)

### Build
- [ ] `versionCode` incremented in `app/build.gradle.kts`
- [ ] `versionName` updated
- [ ] ProGuard/R8 enabled (`minifyEnabled = true`)
- [ ] Room entities not broken by R8 (check `@Keep` annotations)
- [ ] APK/AAB size ≤ 15 MB

## Build Commands

### Debug build (for testing)
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release AAB (for Google Play)
```bash
./gradlew bundleRelease
# Sign with keystore (keystore NOT in repo):
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore tabl.keystore app-release.aab tabl-key
# Output: app/build/outputs/bundle/release/app-release.aab
```

## Rollback

If critical bug after release:
1. Google Play Console → Release → Halt rollout
2. Re-publish previous `versionCode` via "Re-publish"

## Post-Deploy Monitoring

| Metric | Tool | Target |
|--------|------|--------|
| Crashes | Firebase Crashlytics / Play Console | Crash-free ≥ 99.5% |
| ANR | Google Play Console → Android vitals | ANR rate < 0.47% |
| Ratings | Google Play Console | ≥ 4.5 stars |
| APK size | Play Console → Android vitals | ≤ 15 MB |
