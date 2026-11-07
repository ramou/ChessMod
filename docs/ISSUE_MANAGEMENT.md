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
3. The script checks live state again before **each** mutation and reports an
   outcome for **each requested issue**: `CREATED`, `UPDATED`, `SKIPPED` (verified
   existing issue), or `BLOCKED`/`FAILED` (with the reason). Print the actual
   issue number and URL wherever known; retrieve created numbers with
   `gh issue view "$url" --json number --jq '.number'`.
4. The maintainer **manually deletes** `run.sh` afterwards. Do not include a
   self-deletion step, `trap` on `run.sh`, or any automatic cleanup of that file.
   (Temporary body files created with `mktemp` may be cleaned automatically.)
5. Refresh the issue snapshot before the next substantial planning pass; the
   previous ZIP remains a historical snapshot, not a synchronised state.

The normal order is **prepare → review → run → report IDs → manually remove**.
A single script may make several explicitly scoped changes. Process them
**independently**: an already-existing issue must not prevent unrelated issues
from being created. Never mix unrelated issue *edits* into a batch merely for
convenience.

## Multi-issue scripts: idempotence and partial success

1. Before writing **each** issue, perform its own live duplicate/identity and
   metadata checks, including open and closed issues. Do not rely solely on a
   preflight performed at the start of the script or on the exported snapshot.
2. An **unambiguous exact match** is `SKIPPED` (successful no-op): print its
   number, URL and current state; do not recreate, edit, or reopen it without a
   separately approved action. This applies on reruns after partial success.
3. A **plausible but uncertain duplicate**, competing exact matches, a changed
   edit preimage, or an unavailable prerequisite is `BLOCKED` for that issue:
   print candidate IDs/URLs and require human review. **Do not** guess or write
   that issue. Continue with independent requests if doing so is safe.
4. A failed `gh` API call, including a failed duplicate search, must **never**
   be interpreted as "no match". Record `FAILED`, with enough information to
   retry safely; continue only if other requests are independent.
5. Creation/editing must retrieve and report the **real** issue number and URL.
   If a write might have succeeded but its confirmation failed, re-query GitHub
   before retrying: don't risk a duplicate write.
6. Finish with a per-issue summary (`CREATED`, `UPDATED`, `SKIPPED`, `BLOCKED`,
   `FAILED`) and an overall nonzero exit if any issue remains blocked/failed.
   A rerun should safely skip completed requests and attempt outstanding ones.

With `set -euo pipefail`, use **explicit command-result checks** and controlled
per-issue handling; don't let an expected skip abort the entire script. In
particular, invoking a Bash function from an `if` can suppress `errexit` inside
that function, so check failures explicitly rather than relying on `set -e`.

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
decision="$(python3 - "$title" "$candidates_file" <<'CHECK'
import json, sys
title = ' '.join(sys.argv[1].casefold().split())
with open(sys.argv[2], encoding='utf-8') as source:
    matches = json.load(source)
exact = [m for m in matches if ' '.join(m['title'].casefold().split()) == title]
for item in matches:
    print(f"CANDIDATE: #{item['number']} {item['state']} {item['title']} {item['url']}", file=sys.stderr)
if len(exact) == 1:
    item = exact[0]
    print(f"skip:{item['number']}:{item['url']}:{item['state']}")
elif matches:
    print('block')  # Uncertain or competing matches require review.
else:
    print('create')
CHECK
)"
case "$decision" in
  skip:*) echo "SKIPPED existing issue ${decision#skip:}"; exit 0 ;;
  block) echo 'BLOCKED: inspect candidate issues before writing.' >&2; exit 2 ;;
  create) ;;
  *) echo 'FAILED: unexpected duplicate-check result.' >&2; exit 3 ;;
esac

# Approved labels and milestone can be added here after live validation.
url="$(gh issue create --repo "$repo" --title "$title" --body-file "$body_file")"
number="$(gh issue view "$url" --repo "$repo" --json number --jq '.number')"
printf 'Created issue #%s: %s\n' "$number" "$url"
```


This is a **single-issue** reference pattern: its `exit` statements are not a
model for multi-issue execution. In a multi-issue `run.sh`, wrap each issue in
an independently checked operation that records its outcome and returns control
to the batch. An exact match is a successful skip; plausible duplicates are
blocked **for that issue** pending review. Closed matches still count; reopening
requires explicit approval. If `gh issue create` fails, do not blindly rerun
it—creation might have partially succeeded. Recheck live state first.

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
