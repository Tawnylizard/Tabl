---
description: Create or manage a lightweight implementation plan. Saves to docs/plans/ and auto-commits on session end. $ARGUMENTS: plan name OR subcommand (list, done <slug>)
---

# /plan $ARGUMENTS

## What You Do

Create lightweight implementation plans for tasks that don't require full SPARC lifecycle.
Plans live in `docs/plans/` and are auto-committed by the Stop hook.

## Subcommands

- `/plan <feature-name>` — create a new plan (default)
- `/plan list` — list all active plans
- `/plan done <slug>` — mark plan as complete

## Create Flow (default)

1. Parse `$ARGUMENTS` as the plan name/slug
2. Create `docs/plans/<slug>.md` with template:

```markdown
# Plan: <feature-name>

**Date:** YYYY-MM-DD
**Status:** 🔵 In Progress
**Estimated:** X hours

## Goal
[What needs to be done and why]

## Tasks
- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

## Files to Touch
- `path/to/file.kt` — what changes
- `path/to/other.kt` — what changes

## Dependencies
- Requires: [other plan or feature]
- Blocks: [what this unblocks]

## Risks
- [potential issue] → [mitigation]

## Notes
[Any decisions, constraints, or context]
```

3. Fill in Goal from `$ARGUMENTS` context
4. Suggest Tasks based on scope
5. Notify: `📝 Plan created: docs/plans/<slug>.md`

## List Flow (`/plan list`)

Read all files in `docs/plans/`, display:

```
Active Plans:
  🔵 snooze-limit          docs/plans/snooze-limit.md
  🔵 dark-theme            docs/plans/dark-theme.md

Completed:
  ✅ add-medication-form   docs/plans/add-medication-form.md
```

## Done Flow (`/plan done <slug>`)

1. Read `docs/plans/<slug>.md`
2. Update `**Status:** ✅ Complete`
3. Add `**Completed:** YYYY-MM-DD`
4. Notify: `✅ Plan marked complete: <slug>`

## Tips

- For simple tasks (1-3 files): use `/plan`
- For full features (new screens, new flows): use `/feature`
- Plans are auto-committed on session end via Stop hook
