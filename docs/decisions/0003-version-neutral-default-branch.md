# ADR-0003: A version-neutral default branch for project governance and tooling

- **Status:** Proposed — no branch switch or source removal authorised
- **Date:** 2026-11-08
- **Decision owner:** ChessMod maintainer
- **Related issues:** #205 (release manager), #206 (update checker), #209 (0.702 releases), #210 (feature acceptance); implementation issue to be created if accepted
- **Affected targets:** Repository default branch, all supported Minecraft/Forge branches, GitHub Releases and the shared update feed

## Context

ChessMod's GitHub default branch is currently `1.20.1`. It serves two roles:

1. A buildable Minecraft 1.20.1 / Forge implementation of ChessMod.
2. The home of repository-wide documentation, ADRs, issue/context export scripts,
   release management, branch inventory and the emerging canonical feature
   acceptance matrix.

Those roles have different lifecycles. Common tooling and feature specifications
must cover **all** supported Minecraft versions, including future loaders; a
version branch should remain focused on building and testing one target.
Maintaining project-wide documentation under a particular Minecraft version
also makes GitHub's default checkout and landing page appear to represent only
that version. The common chess engine and a possible unified adapter layer are
related questions, but neither is a prerequisite for separating governance
from version-specific code.

The existing release manager currently runs from `1.20.1` and publishes
shared update metadata to `1.20.1:project/mod/update.json`. Existing released
JARs may already fetch that exact branch-qualified raw GitHub URL. Changing
GitHub's default branch **does not rewrite URLs that explicitly name
`1.20.1`**. The legacy feed must therefore remain available and correct.

GitHub also uses its default branch as the normal target for new PRs and for
some automatic issue-closing behaviour. Switching defaults is a workflow
migration, not merely a branch rename.

## Proposed decision

Create a permanent, version-neutral branch named **`main`** and, after a
reviewed migration, make it the repository's GitHub default. Treat `main` as
the **canonical home of repository-wide material**, while keeping `1.20.1`
and every other numbered Minecraft branch independently buildable.

### Ownership after migration

| `main` (project-wide) | Minecraft-version branches (target-specific) |
| --- | --- |
| Project overview and supported-version directory | Forge/Minecraft source, resources and Gradle wrapper |
| ADRs, governance, issue and context workflows | Version-specific integration code and configuration |
| Release manager, target registry and release documentation | Chess-domain source snapshot and its runnable unit tests |
| **One** canonical feature acceptance matrix and feature contracts | Version-specific GameTest implementations and integration tests |
| Evidence references, templates and compatibility policies | Release-tagged source and target-specific assets |

Feature requirements are **universal targets**, not optional by Minecraft
version: an older branch lacking a feature represents an implementation gap,
not an intrinsic `N/A`. The matrix records what is tested and what is still
missing. Individual acceptance scenarios describe user-visible behaviour and
may link to per-version GameTest coverage. GameTest results can supply
evidence but do not silently change acceptance statuses. This governance model
is independent of whether the chess-domain source is eventually vendored or a
common adapter framework is adopted (see ADR-0001).

`main` should eventually cease carrying `project/` as a buildable mod tree,
provided all version branches and release processes continue working. That is
**a separate, later migration gate**, not an immediate consequence of creating
`main`. Avoid leaving an ambiguous pseudo-buildable `project/` on the default
branch indefinitely.

### Shared update metadata — compatibility constraint

**Do not change the already-published `updateJSONURL` endpoints in place.**
Initially, leave `1.20.1:project/mod/update.json` as the active shared feed,
with publication tooling still updating it. The release manager can live on
`main` while explicitly targeting that branch for feed updates.

If a future change makes a path on `main` the canonical feed, preserve the
legacy URL with a validated, synchronised JSON copy on `1.20.1` for as long as
released clients rely on it. Publication must verify that every advertised
endpoint contains compatible recommendations and changelogs. A failed mirror
update is a visible partial-success condition with a safe retry path; **never**
silently publish a new version and leave older clients on a stale feed.
Version-specific Git tags and release assets must not depend on the default
branch's source tree.

## Deliberate exclusions

- No changes to Minecraft source, chess rules, supported versions or version
  numbering solely to implement this ADR.
- No forced merges of `main` into numbered branches; selectively transfer only
  source changes genuinely needed by those targets.
- No history rewriting, branch deletion, retagging or republishing binaries.
- No automatic acceptance of ADR-0001 (unified loader API) or of a standalone
  vendored chess engine; decide those independently.
- No new release automation permissions or unattended GitHub publication.

## Alternatives considered

1. **Keep `1.20.1` as default and improve documentation.** Lowest migration
   cost, but continues coupling governance and release metadata to one target.
2. **Use a separate repository for tooling and planning.** Strong separation,
   but splits issues, revision context, release operations and contributor entry
   points without an evident current benefit.
3. **Make a neutral `main` from an orphan history.** Cleaner initial checkout,
   but loses natural ancestry with the existing project and complicates review.
4. **Create `main` from the existing default branch, then remove target-only
   files through normal commits.** Preferred: retains ancestry and immutable
   history while allowing a staged, reviewable transition.

## Migration plan — separate issue and explicit approvals

1. **Inventory and freeze assumptions.** Record current branch tips, active
   release tags, raw update-feed URLs, CI workflows, branch protection,
   README/download links, default-branch-specific scripts and open PR bases.
   Verify that #210's canonical matrix and test files are committed before
   relocating them.
2. **Create `main` without switching defaults.** Branch from the then-current
   `1.20.1` tip to preserve ancestry. Review its public landing page, governance
   files and script paths; move or copy canonical docs/tooling as a scoped patch.
   Do not delete or modify the existing `1.20.1` source branch.
3. **Decouple the tooling.** Run Python tests; prove release preflight and
   detached-source verification operate from `main` against an explicit
   version branch. Retain the existing update-feed branch until the mirror
   strategy is implemented and tested. Check any GitHub actions/CI triggers,
   default branch assumptions and `gh` commands that infer a target.
4. **Change the GitHub default with maintainer approval.** Update landing-page
   documentation, CI/branch rules, PR templates and contributor instructions.
   Check existing open PR targets and web links. Thereafter use explicit
   Minecraft branches for source PRs and build requests.
5. **Remove target-only content from `main`** in a separate, reviewed ordinary
   commit after the previous steps pass. Keep the project README, licence,
   canonical docs, acceptance definitions, tools and registries. Never remove
   the build tree from any numbered version branch as part of this step.
6. **Optional later feed move.** If beneficial, implement a canonical `main`
   feed plus an audited legacy `1.20.1` mirror, test publication/recovery and
   existing installed-client update checks, and only then adopt the new feed
   path for future JARs.

No step in this plan authorises an automatic branch switch, force push,
release, or deletion. Each requires its own reviewed implementation scope.

## Consequences and risks

**Benefits:** The default landing page describes ChessMod as a multi-version
project; one authoritative acceptance matrix and set of ADRs; common tools can
advance without requiring a new 1.20.1 build; easier adoption of new Minecraft
versions/loaders, and eventually a version-independent chess domain.

**Costs:** Release and export scripts must distinguish their *execution branch*
from their *target branch*; CI and PR defaults need explicit routing; version
branches may retain older copies of documentation; migration introduces a
transitional period when `main` still carries the mod source.

**Critical risk:** Accidentally breaking `1.20.1/project/mod/update.json` would
break update checking in existing releases. Explicit branch-name URLs must
remain stable. GitHub's default-branch change also affects the default PR base
and when `Fixes #N`/`Closes #N` commits automatically close issues. Use
`Refs #N` on version-only commits and manually close multi-version issues once
acceptance is met, rather than merging unrelated mod code to `main` just to
trigger closure.

## Validation / exit criteria

Before marking this ADR *Accepted*, agree on the branch name, target-only
content removal policy and legacy feed ownership. A completed migration must
show:

- GitHub default is the reviewed neutral branch, while each supported
  Minecraft branch still builds from its existing source and retains tags.
- The canonical acceptance matrix has **one** authoritative maintained copy,
  with version rows and evidence links that remain valid on GitHub.
- Tooling from `main` can run its own tests, discover all targets and validate
  at least one full verify → draft → publish lifecycle **without changing or
  breaking previously published release identities**. Use a future release
  or a carefully designed rehearsal; do not republish an old tag merely to test.
- Existing installed mods can still fetch the legacy update feed and receive
  correct compatible version recommendations; any new feed mirrors are checked.
- CI, README links, contributor instructions, issue references and PR defaults
  are reviewed; rollback is documented.

**Rollback:** Until the old structure has been retired, restore the GitHub
default branch to `1.20.1` and revert tool-routing changes through reviewed
commits. Keep `main` as an ordinary branch while investigating. Do not rewrite
history or remove the legacy feed to perform a rollback.

## Decision history

- **2026-11-08 — Proposed:** Prepared for maintainer review. No GitHub default
  change, repository migration or source deletion has been performed.
