#!/usr/bin/env python3
"""
SessionStart hook — outputs Tabl sprint context in < 10 seconds.
Reads: .claude/feature-roadmap.json, git log, source tree TODOs.
"""
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).parent.parent.parent

def run(cmd, timeout=5):
    try:
        r = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout, cwd=ROOT)
        return r.stdout.strip()
    except Exception:
        return ""

def load_roadmap():
    p = ROOT / ".claude" / "feature-roadmap.json"
    if not p.exists():
        return [], []
    data = json.loads(p.read_text())
    features = data.get("features", [])
    current = [f for f in features if f["status"] == "next"]
    planned = [f for f in features if f["status"] == "planned"][:3]
    return current, planned

def recent_commits():
    out = run(["git", "log", "--oneline", "-5"])
    return out.splitlines() if out else []

def scan_todos():
    out = run(["git", "grep", "-n", "-i", "TODO\\|FIXME\\|HACK", "--", "*.kt"], timeout=6)
    lines = out.splitlines() if out else []
    return lines[:10]

def main():
    current, planned = load_roadmap()
    commits = recent_commits()
    todos = scan_todos()

    lines = ["=== Tabl Sprint Context ==="]

    if current:
        lines.append("\nIn Progress:")
        for f in current:
            lines.append(f"  [{f['id']}] {f['title']} ({f['priority'].upper()})")

    if planned:
        lines.append("\nUp Next:")
        for f in planned:
            dep = f", needs {','.join(f['depends_on'])}" if f['depends_on'] else ""
            lines.append(f"  [{f['id']}] {f['title']}{dep}")

    if commits:
        lines.append("\nRecent commits:")
        for c in commits:
            lines.append(f"  {c}")

    if todos:
        lines.append(f"\nTODOs/FIXMEs ({len(todos)} found):")
        for t in todos[:5]:
            lines.append(f"  {t}")
        if len(todos) > 5:
            lines.append(f"  ... and {len(todos)-5} more")

    lines.append("")
    print("\n".join(lines))

if __name__ == "__main__":
    main()
