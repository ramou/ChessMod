# Transient / one-off maintenance scripts

Scripts under this directory are intentionally **temporary**, issue-scoped tools. They must be reviewed, used deliberately and removed once the associated work is accepted. Keep reusable context exports and release tooling in `scripts/` instead.

## Issue #208: king edge-escape propagation

`propagate_king_edge_escape.py` reads the **committed** `1.21` king fix and regression test, then prepares it for the seven older Minecraft-version branches. It does not propagate the Forge port, audio assets, version bumps, or any other differences.

```sh
python3 scripts/transient/propagate_king_edge_escape.py --help
python3 scripts/transient/propagate_king_edge_escape.py               # preflight only
python3 scripts/transient/propagate_king_edge_escape.py --branch 1.20.1 --apply
# After reviewing 1.20.1, use the same script for the other six branches.
```

`--apply` creates **new, local** `propagate/issue-208-<version>` staging branches with worktrees under ignored `.ai-exports/issue-208-propagation/`. It never checks out, commits to, modifies, or pushes the target version branches. Every staged worktree contains *only* a one-line King.java change plus the new JUnit test. It intentionally stops rather than overwrites existing worktrees or already-modified branches. Run branch-specific Gradle tests and builds, inspect diffs and propagate accepted commits onto each version branch manually. Log results and SHAs on issue #208; do not mark complete until all target branches are verified.

**Historical script retired:** `scripts/propagate_castling_fix.py` was purpose-built for resolved issue #200. Remove it from the default branch as part of the script housekeeping patch; do not reuse it for #208. Delete this directory's one-off issue #208 script after successful propagation, and document its retirement in the corresponding issue/commit.
