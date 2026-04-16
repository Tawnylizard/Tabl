---
description: Show the next recommended feature to implement based on roadmap and dependencies.
---

# /next

Read `.claude/feature-roadmap.json` and apply the feature-navigator skill logic:

1. Find all features with `status == "next"`
2. Check their `depends_on` — skip any whose dependencies are not `done`
3. Recommend the highest-priority unblocked feature

## Output

```
## Next Feature: [F-ID] [Title]

**Priority:** [must/should/could]
**Depends on:** [list or "none"]
**Stories:** [US-XX list]

### What to do
1. Run `/plan [feature-name]` to create an implementation plan
2. Use the `planner` agent to break it into tasks
3. Start with the Data layer (see feature-navigator skill for file list)

### Blocked features (waiting on dependencies)
- [F-ID] [Title] — waiting on [F-ID]
```
