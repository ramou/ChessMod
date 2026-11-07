# Issue-driven development and patch protocol

## Lifecycle

1. **Triage and agree:** select GitHub issue(s), intended branch, acceptance
   criteria, constraints, and smallest independently reviewable change. Break
   oversized work into explicit sub-issues/iterations.
2. **Plan:** AI proposes paths affected, design choices, risks, and tests.
   Maintainer agrees or requests revisions before implementation.
3. **Implement:** AI changes only the agreed files. New concerns are recorded,
   not folded into the patch without permission.
4. **Deliver:** AI supplies one ZIP per iteration, with `Patch.md` at its root
   and all added/modified files in **exact repo-relative paths**.
5. **Review:** maintainer checks files side-by-side, runs builds/tests as
   appropriate, and reports defects, observations or scope changes.
6. **Revise:** regenerate a full replacement patch against the stated base, or
   explicitly mark it as an incremental patch that requires prior patches.
7. **Accept and commit:** only after explicit approval, propose a concise commit
   message using `Fixes #N` / `Closes #N` for issues that are genuinely finished.
   Maintainer performs the commit and decides when GitHub issues close.
   **Important:** GitHub's automatic closing keywords act when commits reach
   the repository's *default branch*. A `Fixes #N` commit on a non-default
   Minecraft-version branch may only reference the issue; the maintainer may
   need to close it explicitly after accepting the fix.

GitHub issue creation/edits use the human-operated, ignored root `run.sh`
protocol in [ISSUE_MANAGEMENT.md](ISSUE_MANAGEMENT.md). The AI drafts guarded
`gh` commands; the maintainer reviews the script, invokes `bash run.sh`, reports
the resulting issue number(s), and manually removes the file. The AI never
asserts that merely drafting a script has written to GitHub. Check existing
open/closed issues and live identity before any create/edit operation.

No automatic GitHub writes, commits, cherry-picks or pushes are part of this
workflow. Issue exports are snapshots, not synchronisation.

## ZIP shape

```text
ChessMod-Patch-001.zip
├── Patch.md
├── scripts/
│   └── example.py
└── project/
    └── src/
        └── ...
```

`Patch.md` must identify:

- Target branch and exact **base commit SHA** (plus any required earlier patch).
- Linked issue(s) and objective, with an explicit out-of-scope statement.
- Design decisions and rationale; flag any proposed ADRs.
- Added, modified, and deleted files (separate categories).
- Build/test commands, test results and what **was not** exercised.
- Application instructions, risks, known limitations and rollback guidance.
- Suggested commit message **only when the patch is accepted**; otherwise
  mark it as a suggestion contingent on review.

The ZIP contains **full replacement file contents**, not a textual diff or a
copied `.git/` directory. Deletions exist only as directions in `Patch.md`.
Use forward-slash repo paths and avoid unrelated generated files.

**Separate concept:** generated `-worktree.zip` files are snapshots of a human's
*uncommitted local work*, not accepted AI implementation patches. Their `files/`
folder and Git binary diff files deliberately use a different format. Optional
`-stash-*.zip` snapshots similarly represent shelved work against the stash's
*original* base commit, not changes currently present in the working tree.
Neither is a ready-to-apply software change; consult
[CONTEXT_EXPORT.md](CONTEXT_EXPORT.md) and verify the base SHA first.

## Patch replacement vs incremental patch

A replacement patch is based directly on the indicated Git SHA and supersedes
any earlier unaccepted patch. An incremental patch must name the earlier patch(es)
it builds on and explain its prerequisites. Default to a **replacement** unless
the maintainer specifically wants incremental changes.

## Branch and version discipline

Treat Minecraft-version branches independently until an accepted decision
changes that policy. Record target Minecraft version, loader and build status
on issues where applicable. If the same defect spans branches, plan a separate
validated adaptation for each affected target rather than blindly transplanting
files. Do not convert branches to a unified build system within unrelated fixes.

## No opportunistic refactoring

When new debt is discovered, describe it in the review/issue notes. A new issue
or ADR is the appropriate place to decide whether it warrants work. A defect fix
should not become a cleanup spree, however tempting that may be.
