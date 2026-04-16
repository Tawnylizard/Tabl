# Security Rules — Tabl

## Android Intent Security

- All `PendingIntent` objects **MUST** use `FLAG_IMMUTABLE` flag (required API 31+)
- `NotificationActionReceiver` **MUST** have `android:exported="false"` in AndroidManifest
- `BootReceiver`: `android:exported="true"` only with `android.permission.RECEIVE_BOOT_COMPLETED` declared
- PendingIntent `requestCode` = `scheduleId` (unique per schedule, prevents collision)
- No implicit Intents for internal BroadcastReceiver communication

## Data Privacy

- Room DB stored in app-private storage (`/data/data/com.app.tabl/`) — inaccessible without root
- **NEVER** log medication names, doses, or any PII — only log `medicationId`/`scheduleId`
- Debug-only logging: wrap all `Log.*` calls with `if (BuildConfig.DEBUG)`
- If Crashlytics added: record only crash stack traces, **no** medication data in messages

## Input Validation

- Medication name: trim whitespace, reject empty/blank strings
- Medication name: maximum 100 characters (enforced at UI input field level)
- Stock count: reject negative values, validate range 0..Int.MAX_VALUE
- Snooze period: validate against allowed values (10 min, 30 min)
- Date fields: validate `startDate < endDate` before saving to Room

## Build Security

- ProGuard/R8 minification **required** in release build (`minifyEnabled = true`)
- Keystore stored **outside** the repository — never committed to git
- No hardcoded credentials or API keys (app is 100% offline)
- `.gitignore` must exclude: `*.keystore`, `*.jks`, `keystore.properties`

## Notification Security

- Notification channel `CHANNEL_REMINDERS`: created once at app init, not recreatable
- Low-stock notification: `IMPORTANCE_DEFAULT` (not `IMPORTANCE_HIGH`) — non-intrusive
- Reminder notification: `PRIORITY_HIGH` for heads-up only
- `autoCancel = true` on all notifications

## Permissions

Required permissions and justification:
```xml
POST_NOTIFICATIONS       — user-triggered, request at first launch (API 33+)
RECEIVE_BOOT_COMPLETED   — restore WorkManager tasks after reboot
WAKE_LOCK                — required by WorkManager for reliable background execution
```

If `POST_NOTIFICATIONS` permission is denied:
- Show in-app banner on every HomeScreen open
- Do not block app functionality — reminder scheduling still runs
- Re-request permission on next app launch
