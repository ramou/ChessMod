# Producing an AI context snapshot

Scripts in [`scripts/`](../scripts/) use Python 3.10+ standard library and
`argparse` (`--help`). The context builder additionally requires `git` on PATH.
Issue export uses GitHub's **read-only REST API**; public issues need no token,
but setting `GH_TOKEN` (or `GITHUB_TOKEN`) avoids the low anonymous rate limit.
Never put tokens on the command line or in exported files.

## Everyday commands (from repository root)

```sh
# Default: a Git bundle, plus a separate -worktree.zip if the tree is dirty
# or stashes exist. The bundle itself always contains committed history only.
python scripts/build_ai_context.py

# Bundle plus all GitHub issues (open and closed, with comments):
python scripts/build_ai_context.py --with-issues

# Small, targeted handoff (two versions and only selected tickets):
python scripts/build_ai_context.py --branch 1.20.1 --branch 1.21 \
  --with-issues --issue-number 13 --issue-number 84

# Alternative issue-only view:
python scripts/export_issues.py --state open --label bug

# Only the local working state, without a Git bundle:
python scripts/export_worktree.py

# Explicitly export one shelved change, or every stash, as separate ZIPs:
python scripts/build_ai_context.py --stash-ref 'stash@{0}'
python scripts/build_ai_context.py --all-stash-contents

# Same controls are available in standalone mode:
python scripts/export_worktree.py --stash-ref 'stash@{0}'

# Intentionally omit uncommitted work (not recommended for a handoff):
python scripts/build_ai_context.py --allow-dirty

# Include an explicit clean-state manifest; omit untracked file contents:
python scripts/build_ai_context.py --worktree always --no-untracked

# Preview options and selected refs:
python scripts/build_ai_context.py --dry-run
python scripts/export_issues.py --help
python scripts/build_ai_context.py --help
```

The context builder understands `--all-branches`, repeated `--branch`,
`--worktree auto|always|skip`, `--no-untracked`, `--no-stashes`,
`--stash-ref` (repeatable), `--all-stash-contents`, and
issue filters (`--issue-state`, `--issue-label`, `--issue-since`,
`--issue-milestone`, `--issue-number`, `--issue-limit`, `--issue-no-comments`).
Run `--help` for complete descriptions. Issue labels use AND filtering when
repeated; timestamps are UTC. A limited export is explicitly marked as partial.

## Outputs

By default outputs go to `/.ai-exports/`, which the root `.gitignore` ignores.
Example paired snapshot (actual timestamp/SHA will differ):

```text
.ai-exports/
├── ChessMod-context-1.20.1-20261107T120000Z-a6bfb9574da3.bundle
├── ChessMod-context-1.20.1-20261107T120000Z-a6bfb9574da3-issues.zip
├── ChessMod-context-1.20.1-20261107T120000Z-a6bfb9574da3-worktree.zip  # if needed
└── ChessMod-context-1.20.1-20261107T120000Z-a6bfb9574da3-stash-0-<sha>.zip  # opt-in
```

The issue ZIP includes `manifest.json` with selected issue metadata, comments,
filter conditions, export timestamp and (in paired exports) source Git SHA, plus
`issues/000013.md` etc. Markdown is for reading; JSON is for precise state,
labels, milestones, timestamps, and machine processing. A snapshot is **not**
a live view or a full state-transition audit log.

Git bundles contain reachable *committed* Git history for selected refs.
They do **not** include the working directory, Git index or stashes.

By default, when the working directory has changed or the stash list is nonempty,
`build_ai_context.py` also writes a paired **`-worktree.zip`**. This is a
separate working-state snapshot, **not an ordinary accepted implementation
patch**. Its `Patch.md` identifies the exact working-directory `HEAD` and branch;
`manifest.json` records status for each changed path, deleted/renamed paths,
file SHA-256 and permissions, the portable bundle refs, paired bundle filename
and SHA-256, whether the base is in the bundle, and stash references/commit
IDs/subjects. The exporter evaluates the CURRENT worktree's `.gitignore`
(and Git's other active exclude rules) using `git check-ignore --no-index`,
so it excludes paths that have since become ignored, **even if they are tracked**.
Ignore filtering also applies to both Git diff payloads, not merely the copied
working files. The manifest records how many changed paths it excluded, without
listing their names. `files/` contains the
exact modified/added working files in their repository-relative paths.
`staged.diff` and `unstaged.diff` retain the index-versus-working-tree split,
including Git binary patches. Files omitted with `--no-untracked` remain listed
in the manifest, without their contents. A clean tree can force a snapshot with
`--worktree always`; `--worktree skip --allow-dirty` or the older
`--allow-dirty` explicitly omits uncommitted content.

**Stash contents are opt-in.** By default only the stash inventory appears in
the worktree manifest; `--no-stashes` removes even that. To export actual saved
work, specify `--stash-ref 'stash@{0}'` (repeatable) or explicitly request every
stash with `--all-stash-contents`. Each gets its own paired `-stash-N-<sha>.zip`,
containing `Patch.md`, `manifest.json`, `files/`, `staged.diff`, and
`unstaged.diff`. The stash's first parent identifies the ORIGINAL commit at
which it was shelved, its second parent preserves the staged state, and its
optional third parent contains stashed untracked/ignored files. The exporter
reads those Git objects without checking out or modifying the stash. It applies
the CURRENT working tree's ignore rules to stash contents, including files
originally captured with `git stash -a`, and never exports matching paths.
A stash archive records its own original base SHA, which must be reachable from
a selected bundle ref; a newer unrelated branch tip cannot substitute for it.
These archives are snapshots, **not** a one-command recreation of `git stash`.

Untracked, nonignored working files are captured by default, so **review ZIPs
for credentials, personal information, and oversized or unrelated artefacts**.
Each worktree or stash ZIP has a 64 MiB uncompressed limit by default, changed
with `--max-worktree-mb` (`export_worktree.py` uses `--max-mb`). Repeated stash
exports can produce multiple archives; request specific stashes to limit size.
The ZIP is not an automatic restoration of Git's index or stashes.

When exporting uncommitted changes, `HEAD` must be reachable from at least one
selected bundle tip. This avoids giving an AI a working-state snapshot whose
base isn't actually in its accompanying bundle. Add the checked-out branch
to `--branch`, or use `--all-branches` if necessary.

Local branches and fetched remote-tracking refs are eligible, but remote refs
that have not been fetched locally cannot be included. Use `git fetch` first if
necessary. The script creates temporary *portable branch refs* inside the
bundle so even a remote-only selection can be cloned normally; it does not
change any refs in your working repository. Paired issue manifests record the
portable branch names and their SHAs in `source_refs`. Use
`git bundle list-heads FILE.bundle` to inspect the result.

Generated outputs are not imported back into Git. The scripts refuse to
overwrite a same-named snapshot and have configurable size limits (bundle:
256 MiB; uncompressed issues: 64 MiB; uncompressed worktree: 64 MiB, by
default). Narrow the export or raise the explicit limit when necessary. Failed
paired exports remove all newly produced files, avoiding misleading partial
handoffs.

## Handoff checklist

Send the relevant `.bundle`, matching `-issues.zip` if requested,
`-worktree.zip` if produced, and any explicitly requested `-stash-*.zip` files.
The worktree ZIP is required to reconstruct the current working directory when
your changes haven't been committed; stash ZIPs describe separately shelved
work and must not be mistaken for current changes. Specify the issue
and target branch. The next session should inspect the bundle heads and
working-state manifest, overlay the changes only after review, then read
[AI_HANDOFF.md](AI_HANDOFF.md) before proposing implementation.
