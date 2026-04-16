---
description: Start implementing a specific feature. $ARGUMENTS: feature ID (e.g. F1) or feature name.
---

# /go $ARGUMENTS

Full implementation kickoff for feature `$ARGUMENTS`.

## Step 1 — Load Context

Read in parallel:
- `CLAUDE.md` — project overview
- `.claude/feature-roadmap.json` — find the feature and verify dependencies are met
- `docs/Specification.md` — relevant entity definitions
- `docs/Pseudocode.md` — relevant algorithms

## Step 2 — Check Dependencies

If any `depends_on` feature has `status != "done"`:
- List what's missing
- Ask: "Feature [X] depends on [Y] which is not done. Proceed anyway? (y/n)"

## Step 3 — Create Plan

Run `/plan $ARGUMENTS` to create `docs/plans/[feature-slug].md`.

## Step 4 — Spawn Implementation Agents

Use the planner agent template from `.claude/agents/planner.md` to break into tasks:

```
⚡ Parallel tasks (no dependencies between layers):
  Task A: Data layer (entities + DAOs + repository)
  Task B: Domain layer (scheduler/business logic changes)
  
Sequential after A+B:
  Task C: Android components (Worker/Receiver)
  Task D: UI layer (Screen + ViewModel)
  Task E: Tests
```

## Step 5 — Update Roadmap

After all tasks complete, update `.claude/feature-roadmap.json`:
- Set `status` to `"done"` for the completed feature
- Check if any `planned` features can now move to `"next"`

## Step 6 — Run Tests

```bash
./gradlew test
./gradlew lint
```

Fix any failures before marking complete.
