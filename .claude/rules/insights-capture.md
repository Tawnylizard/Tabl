# Insights Capture Protocol

## 🔍 Error-First Lookup (CRITICAL — do this BEFORE debugging)

**IMPORTANT:** When you encounter ANY error, ALWAYS do this before starting to debug:

```bash
# Step 1: Check if index exists
if [ -f "myinsights/1nsights.md" ]; then
  # Step 2: Grep for the error signature in the index
  grep -i "ERROR_STRING_OR_CODE" myinsights/1nsights.md
fi
```

**Pattern:**
1. User reports a problem or an error occurs
2. Extract the key error string (error code, exception name, or unique message fragment)
3. `grep` the error string against `myinsights/1nsights.md` Error Signatures column
4. **If match found** → read ONLY the linked detail file → suggest documented solution FIRST
5. **If match found AND solution works** → increment hit counter in both index and detail file
6. **If no match** → debug normally → after resolution, suggest capturing with `/myinsights`

**Example lookup flow:**
```
Error: java.lang.IllegalStateException: Cannot access database on the main thread
→ grep "main thread" myinsights/1nsights.md
→ Match: INS-001 | `main thread`, `database` | Room query on main thread... | INS-001-room-main-thread.md
→ cat myinsights/INS-001-room-main-thread.md
→ Apply documented solution
→ Increment hit counter
```

## When to Suggest Capturing an Insight

Proactively suggest `/myinsights` when ANY of these occur:

1. **Error → Fix cycle**: A non-trivial bug was debugged and resolved
   - Especially: errors that took >3 attempts to fix
   - Especially: misleading error messages (e.g. WorkManager silently not firing)

2. **Android-specific surprise**: A platform behavior was unexpected
   - Battery optimization killing WorkManager on Xiaomi/Samsung
   - Notification channel importance cannot be changed after creation
   - FLAG_IMMUTABLE vs FLAG_MUTABLE confusion

3. **Dependency issue**: A library/package caused problems
   - Room migration version mismatch
   - Hilt component scope issues
   - WorkManager constraints behavior

4. **Architecture decision under pressure**: A design choice was made
   during debugging that should be documented

5. **Workaround applied**: A temporary fix was applied that needs
   future attention (suggest status: 🟡 Workaround)

## How to Suggest

After resolving a tricky issue, say:
```
💡 This looks like a valuable insight. Want me to capture it?
   Run `/myinsights [brief title]` or say "да, запиши"
```

## When NOT to Suggest

- Trivial typos or syntax errors
- Well-known Android patterns (standard RecyclerView, basic Compose)
- Issues already documented in `myinsights/` (check index first!)
- User explicitly said they don't want to capture

## Lifecycle Awareness

When reviewing insights during lookup, check the status:
- `🟢 Active` — trusted solution, apply directly
- `🟡 Workaround` — temporary fix, may need better solution. Apply but flag to user.
- `🔴 Obsolete` — should be in archive. If found in main folder, suggest `/myinsights archive INS-NNN`

When a workaround gets a proper fix, suggest:
```
💡 INS-NNN was a workaround. Now we have a proper fix — update it?
   Run `/myinsights status INS-NNN active` and I'll update the solution.
```
