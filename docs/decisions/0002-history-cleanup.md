# ADR-0002: Preserve or prune historical release binaries

- **Status:** Proposed — NOT authorised for application or force-push
- **Date:** 2026-11-07
- **Decision owner:** ChessMod maintainer
- **Related issue:** Create only if investigation is approved
- **Affected branches:** All eight Minecraft version branches (1.14.4–1.21)

## Problem and measurements

The archived eight-branch context bundle supplied on 2026-11-07 is approximately
18.90 MiB. Historical build and distribution binaries dominate packed storage,
particularly `modpack/*.zip` and compiled JARs. An earlier disposable pruning
experiment yielded a **6.08 MiB** candidate (~68% smaller) while preserving
all eight branch-tip file contents. That result used its own removal scope;
the more narrowly defined experiment below may produce a different size.

The single largest historical artefact, `modpack/ChessMod_0.605.zip` (~5.2 MiB),
was introduced in `d00a105` and removed from the 1.14.4 branch in `3a14bac`.
**It is absent from every supplied current branch tip.** Its embedded files
include `OptiFine_1.14.4_HD_U_F5.jar` (~5.2 MiB uncompressed),
`MixinBootstrap-1.0.3.jar`, `OptiForge-MC1.14.4-0.1.1.jar` and a shader ZIP.
OptiFine redistribution permissions should be checked separately; historical
inclusion is not proof that the current distributable contains it, nor does a
history rewrite guarantee deletion of every externally held copy.

Gradle wrapper JARs are **tooling dependencies**, not old ChessMod release
binaries. Source, tests, images, PSD assets and the Gradle wrapper should stay.

## Proposed default — no history rewrite (safest)

1. Stop committing new distributable ZIPs and mod JARs where practicable;
   publish binaries via GitHub Releases with tags, checksums, and provenance,
   following the separately documented, human-approved
   [release procedure](../RELEASING.md).
2. Keep existing Git history untouched and document historical release locations.
3. Optionally remove current checked-in release binaries in a normal reviewed
   commit *only if* distribution/build behaviour and public links are addressed.
4. Accept the extra ~13 MiB of history if history continuity matters more.

This maintains commit hashes, forks, existing issue/PR links, tags, and the ability
to check out historic commits. It does **not** eliminate old binaries from the
remote's reachable history or reduce full bundle size immediately.

## Alternative: disposable history rewrite preserving branch-tip contents

An experimental alternative uses `git-filter-repo` to strip historical
**release artefact paths only**:

- `modpack/*.zip`
- `project/mod/*.jar`
- `forge-*/mod/*.jar`

Current branch-tip release files are then restored in new tip commits **per
branch**. This changes commit SHAs while making each branch's newest checkout
byte-for-byte identical to its old tip, including current release binaries.
It does not rewrite Gradle wrapper JARs and does not prune design assets.
Branch-specific files matter: 1.21 currently includes a 1.20.1-named build;
that naming is preserved, not silently corrected.

A reproducible, **non-publishing experiment** is provided at
[`../../scripts/experiment_history_cleanup.py`](../../scripts/experiment_history_cleanup.py):

```bash
# Full context bundle (not a delta), created by build_ai_context.py
python scripts/experiment_history_cleanup.py \
  --bundle .ai-exports/ChessMod-context-multi-8-<timestamp>-<sha>.bundle \
  --dry-run

# Requires git-filter-repo installed, in addition to git and Python 3.10+
python scripts/experiment_history_cleanup.py \
  --bundle .ai-exports/ChessMod-context-multi-8-<timestamp>-<sha>.bundle
```

The script only **reads** the input bundle, creates an isolated temporary Git
clone, filters that clone, restores current releases to each branch and compares
the entire branch-tip file-mode/blob-ID map with the original. It emits only a
candidate `.bundle` and JSON verification report in ignored `.ai-exports/`.
It has **no push or remote mutation command**. The disposable clone is deleted
afterward. The `--force` flag applies *only* to `git-filter-repo` inside the
temporary clone; it never applies to your working repository. The experiment
requires the external `git-filter-repo` utility; our normal context exporters
remain standard-library-only. **The rewrite execution itself has not yet been
regression-tested in this environment**, where `git-filter-repo` was absent.

Before considering publication: independently clone and build every candidate
branch, test the game, inspect tags/release links, verify CI and issue references,
record all old/new SHA mappings, retain and verify an immutable backup, assess
all collaborators/forks/open PRs, and plan an explicit GitHub migration with
maintainer approval. Don't use `git push --force` as part of the experiment.

## Alternative: replace old blobs with textual tombstones

Replace historical release contents with small notice files identifying the
original distribution and where to obtain/rebuild it. This could retain *a
narrative* of what existed at each point, but still rewrites affected commits
and all descendant hashes, breaks historical checkout/reproduction, may place
misleading text at `.jar`/`.zip` paths, and doesn't repair GitHub references.
A separate `REMOVED_RELEASES.md` mapping original commit/path/blob SHA to
external download or build instructions would be cleaner than placeholder
binaries. Keeping a frozen, read-only archive of the original Git repository
preserves provenance, but leaves old objects obtainable from that archive.

## Other alternatives

| Approach | Benefit | Cost / limitation |
| --- | --- | --- |
| **Do nothing to history** (preferred for now) | No broken links or clone migrations | Full bundle remains large |
| Move future releases to GitHub Releases | Smaller growth; clear distribution provenance | Does not purge old objects |
| Filter historical release blobs and restore tips | Substantially smaller candidate bundle | New commit SHAs; forks/PRs/tags coordination |
| Text tombstones + rewrite | Retains explanatory breadcrumbs | Same broken SHAs; historical binaries unreproducible |
| New source-only repository, archive old one | Clean start without altering old repository | Forks and issue history split across repos |
| Git LFS migration | Externalises binary storage | Also rewrites affected Git history; introduces hosting dependency |

Neither layered reconstruction, Git replace/graft references, nor tombstones can
make modified historical commits retain their **original SHA identity**. Existing
clones and external links may still reference old hashes regardless of where the
new repository places equivalent source files.

## Risks and decision gate

A rewrite can disrupt external clones, forks, issue/PR commit references,
comments, checked-out tags, signed commits, GitHub release source archives and
CI/deployment integrations. Tags and extra refs require separate verification;
**matching the latest eight trees is necessary but insufficient**. Published
copies cannot reliably be recalled, and old objects may remain reachable on
hosting infrastructure or in forks.

**Current recommendation:** Retain Git history, stop committing new release
binaries, and revisit this only if size or redistribution warrants a disruptive
migration. Do not implement history rewrites, remote pushes, or placeholder
changes without a separate accepted decision and scoped issue.

## Decision history

- 2026-11-07: Proposal and safe experiment documented; no rewrite accepted.
