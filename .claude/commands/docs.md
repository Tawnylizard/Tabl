---
description: Show key documentation for Tabl. $ARGUMENTS: topic (arch, spec, algo, prd, refinement, all).
---

# /docs $ARGUMENTS

Quick access to Tabl documentation.

## Topics

| Argument | File | Contents |
|----------|------|----------|
| `arch` | `docs/Architecture.md` | Component diagram, layer boundaries, notification flow |
| `spec` | `docs/Specification.md` | Data model: Medication, Schedule, MedicationLog entities |
| `algo` | `docs/Pseudocode.md` | 6 core algorithms with function signatures |
| `prd` | `docs/PRD.md` | Features (F1-F10), user stories, NFRs |
| `refinement` | `docs/Refinement.md` | Edge cases matrix, security hardening |
| `validation` | `docs/validation-report.md` | Requirements validation scores |
| `scenarios` | `docs/test-scenarios.md` | BDD scenarios |
| `roadmap` | `.claude/feature-roadmap.json` | Feature statuses and dependencies |
| `all` | all above | Full documentation dump |

## Usage

```
/docs arch      → show Architecture.md
/docs algo      → show Pseudocode.md algorithms
/docs prd       → show PRD features and stories
/docs all       → show all documentation files
```

If no argument provided, show this index.
