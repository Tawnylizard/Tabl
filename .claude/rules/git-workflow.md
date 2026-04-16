# Git Workflow

## Commit Format

```
type(scope): description
```

## Types

- `feat` — new feature
- `fix` — bug fix
- `refactor` — code restructuring (no behavior change)
- `test` — adding or updating tests
- `docs` — documentation changes
- `chore` — build, CI, config changes

## Scopes (by module)

- `alarm` — notification scheduling, WorkManager, MedicationReminderWorker
- `ui` — Compose screens and components
- `data` — Room DB, repositories, migrations
- `history` — history screen, compliance calculation
- `settings` — DataStore preferences, SettingsRepository
- `ci` — GitHub Actions workflows

## Rules

- Commit after each logical change — never batch unrelated changes
- Use imperative mood: "add", "fix", "remove" (not "added", "fixed")
- Keep description under 72 characters
- Reference issue number if applicable: `fix(alarm): handle null medication on boot #12`

## Examples

```
feat(alarm): add snooze repeat Worker with configurable delay
fix(data): handle Room IOException with 3x exponential backoff
refactor(ui): hoist MedicationList state to HomeViewModel
test(alarm): add calculateNextTrigger edge cases for DST
docs: update Architecture.md notification flow diagram
chore(ci): add Android emulator instrumented test job
```
