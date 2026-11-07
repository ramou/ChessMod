# GitHub issue writes: reviewed `run.sh` protocol

**GitHub issues are authoritative.** The `-issues.zip` is a dated, read-only
snapshot. Creating/updating issues is an explicit human-approved **write**, not
something the AI performs automatically. We use the installed [GitHub CLI]
(https://cli.github.com/manual/) (`gh`) on the maintainer's machine.

## Preflight and issue identity

1. Read the latest `-issues.zip`: `manifest.json`, relevant issue Markdown and
   `issue_catalogue.md`. The manifest includes the **repository-wide** valid
   labels and open/closed milestones even if the issue snapshot was filtered.
   Labels are GitHub **issue labels**, not Git tags. `gh --milestone` takes its
   **title**, while the manifest also gives its milestone number.
2. Before drafting, search existing **open and closed** issues for an identical
   request and plausible duplicates. Read their bodies, comments, states,
   labels and milestones; decide to reuse, modify, reopen, or create. Never
   create a duplicate solely because a title differs slightly. If uncertain,
   show candidates to the maintainer and ask.
3. Verify the target repository explicitly: `ramou/ChessMod`, rather than the
   current Git remote. Check `gh auth status` and repository permissions; never
   embed tokens or secrets in scripts.
4. For an **edit**, select an explicit issue number and URL. Compare its live
   title, state, and `updatedAt`/body against the snapshot or the expected
   preimage. If a mismatch exists, **abort and reconsider** instead of editing
   a merely similar issue. Prefer additive label/milestone edits over replacing
   an entire issue body when a narrower change suffices.
5. Recheck labels/milestones live before writing; the export is not a guarantee
   that names have not changed. Project V2 fields, organisation issue types,
   templates, and assignee eligibility are not included in this catalogue.

## Human-operated script contract

When a change is approved, the AI produces a **single `run.sh` at the repository
root** for review. It is intentionally excluded by the root `.gitignore`:
`/run.sh`. Never include `run.sh` in a commit, patch ZIP, or Git bundle as
committed work. The script should be a standalone Bash script, with
`set -euo pipefail`, explicit `--repo ramou/ChessMod`, no hidden GitHub writes,
no network calls other than reviewed `gh` commands, and no secrets.

1. The maintainer reads **every command and issue body** in `run.sh` and approves
   the proposed issue actions, including labels, milestone and targets.
2. The maintainer invokes `bash run.sh` locally. The AI does **not** run it or
   assert that GitHub has changed merely because the script was generated.
3. The script checks live state again before any mutation, prints what it did,
   and retrieves the created issue number from the returned URL using
   `gh issue view "$url" --json number --jq '.number'` (or prints the number
   after a guarded edit). Print issue URL and number for the human to share.
4. The maintainer **manually deletes** `run.sh` afterwards. Do not include a
   self-deletion step, `trap` on `run.sh`, or any automatic cleanup of that file.
   (Temporary body files created with `mktemp` may be cleaned automatically.)
5. Refresh the issue snapshot before the next substantial planning pass; the
   previous ZIP remains a historical snapshot, not a synchronised state.

The normal order is **prepare → review → run → report IDs → manually remove**.
A single script may make several explicitly scoped changes, but should print
an ID/URL and stop on any error. Avoid mixing unrelated issue updates.

## Guarded creation pattern

This is a **pattern**, not a ready-to-run issue script. A concrete `run.sh`
embeds the actual reviewed title/body and checks both the exported issues and
live candidates before calling `gh issue create`. Keep the issue body in a
quoted heredoc to avoid shell expansion and pass it with `--body-file`.

```bash
#!/usr/bin/env bash
set -euo pipefail
repo='ramou/ChessMod'
title='REPLACE WITH APPROVED ISSUE TITLE'
body_file="$(mktemp)"
candidates_file="$(mktemp)"
trap 'rm -f "$body_file" "$candidates_file"' EXIT

cat > "$body_file" <<'ISSUE_BODY'
REPLACE WITH APPROVED MARKDOWN DESCRIPTION AND ACCEPTANCE CRITERIA
ISSUE_BODY

# Ask GitHub about open AND closed similar titles; still review the snapshot
# separately for semantic duplicates that this search can miss.
gh issue list --repo "$repo" --state all --search "\"$title\" in:title" \
  --json number,title,state,url --limit 200 > "$candidates_file"
if ! python3 - "$title" "$candidates_file" <<'CHECK'
import json, sys
title = sys.argv[1].casefold()
with open(sys.argv[2], encoding='utf-8') as source:
    matches = json.load(source)
for item in matches:
    exact = item['title'].casefold() == title
    kind = 'EXISTING ISSUE' if exact else 'POSSIBLE DUPLICATE'
    print(f"{kind}: #{item['number']} {item['state']} {item['title']} {item['url']}")
sys.exit(1 if matches else 0)
CHECK
then
  echo 'Issue already exists or may be related: inspect and revise run.sh.' >&2
  exit 1
fi

# Approved labels and milestone can be added here after live validation.
url="$(gh issue create --repo "$repo" --title "$title" --body-file "$body_file")"
number="$(gh issue view "$url" --repo "$repo" --json number --jq '.number')"
printf 'Created issue #%s: %s\n' "$number" "$url"
```


A generated script must **abort by default** on exact or plausible duplicate
matches unless the maintainer explicitly approves proceeding. Existing closed
issues count: consider reopening instead. If `gh issue create` reports an error,
do not blindly rerun it—creation might have partially succeeded. Recheck live
state first.

## Guarded modification pattern

For updates, use `gh issue view NUMBER --repo ... --json
number,title,state,updatedAt,url,body,labels,milestone` to verify identity and
freshness. In `run.sh`, assert the **expected issue number and exact prior title**
(and, if available, the snapshot's `updatedAt`) before running `gh issue edit`.
Use `--add-label` / `--remove-label` for metadata edits; `--body-file` replaces
the complete body and requires explicit full-body review. Print the issue number
and resulting URL using `gh issue view` after the command.

If an intended change is approximate rather than an exact match to an issue,
propose the candidate and its differences to the maintainer first. Do not guess
an issue number from wording or modify the first search result.

## Boundaries

- Neither `export_issues.py` nor `build_ai_context.py` writes to GitHub.
- The AI may draft `run.sh`; only the human executes it. No automatic closure,
  repo mutation, force push, issue creation or deletion is implied by a patch.
- Re-running scripts should not silently create duplicates. If uncertain about
  previous success, **inspect GitHub before retrying**.
- Commit issue references (`Fixes #N`) are separate from issue writes; GitHub
  auto-close rules depend on the default branch.
