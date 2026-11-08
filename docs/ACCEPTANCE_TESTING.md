# Feature-based acceptance testing — issue #210

[Canonical feature matrix](ACCEPTANCE_MATRIX.md) · [GTF mapping](acceptance/GTF_MAP.md) · [Run template](acceptance/RUN_TEMPLATE.md)

## Scope and distinction

The matrix answers **which player-facing features work in which supported Minecraft/ChessMod builds**. It is deliberately **not** the chess engine's JUnit regression suite. All features are goals on all supported Minecraft versions, even when implementations differ. Do not label a feature `N/A` because the branch is old. Use `BELOW-<version>` for a feature that hasn't been delivered to that mod version, `GAP-SOURCE` for a missing implementation within a release family that should have it, `UNTESTED` for an unexamined implementation, and `FAIL` for an actually observed acceptance failure. A source-only gap is not proof of a gameplay failure.

## The canonical record

Maintain **one** version-by-feature matrix on the default branch (currently `1.20.1`). Branches contain version-dependent Minecraft/Forge code and may contain individual GTF implementations, but must not each maintain an independent copy of this matrix. If the repository later adds a metadata-only default branch, move the canonical documents through an explicit design issue/ADR; that is **not part of #210**.

The matrix has Minecraft/version rows (newest first) and linked feature columns, grouped by proposed ChessMod introduction family, with an index of features at the bottom. Record source SHA, mod version and game runtime in each run. Add an extra **known-working baseline** row when a branch has advanced beyond its last fully-tested build: keep the old result attached to its original SHA and do not transfer it to the new tip by assumption.

## Manual procedure

1. Choose the branch/head or a previously tested published release. Verify MC runtime, ChessMod version, loader/Forge version, SHA and package checksum. Use a fresh test world or a **copy** of the save for destructive steps.
2. Open the linked `docs/acceptance/<FEATURE>.md`; follow its steps *in order*. The document is canonical; record any branch-specific actions or constraints in the run. Perform every step not covered by an actually verified GTF test manually.
3. Store a run under `docs/acceptance/runs/` using the provided template. Record date, tester, run mode, per-step observations, exact input, logs/screenshots when useful and bugs/blocked conditions. Include test discovery/count and failing-gate evidence for GTF runs.
4. Only after reviewing evidence, update the matching cell with `YYYY-MM-DD-PASS-<tester>`, `FAIL`, `BLOCKED` or `PARTIAL`, linking to the committed run. For a mixed run, use a human tester if human validation completes the final checks; GTF-only `PASS` requires complete feature coverage.
5. If a test fails, open/link an appropriate issue and record the observed defect. If feature code is absent, record the development gap separately. Neither outcome should silently block the entire matrix from tracking other features.
6. When a branch tip moves, leave the old run untouched and mark the **new** version row untested; add a baseline row with old version/SHA where useful. No inferred passing statuses.

## GTF is a mechanism, not the acceptance authority

The **feature file** defines the expected behaviour and is the canonical test specification. A GTF class may implement its server-observable steps differently in each Minecraft/Forge version. Add a mapping to branch-specific GTF implementation path at a pinned SHA, registered test name, the fixture and the exact step IDs covered; see [GTF map](acceptance/GTF_MAP.md). A successful Java unit test or merely having `gameTestServer` configured is **not** GTF evidence.

For GTF-capable branches, run with the branch's own Forge/Gradle toolchain (often `./gradlew runGameTestServer`). Verify actual test discovery, nonzero count, pass/fail output, and a deliberate failing test making the command fail; do not mark PASS from a zero-test run. On pre-GTF branches, or where registration/instrumentation is unavailable, execute the same spec manually. GUI pixels, sound audibility, launcher import and real multi-client behaviour remain human responsibilities.

## Results and release decisions

#210 delivers the matrix and executable human feature specifications and a GTF adoption plan. GameTest implementation can be tracked as separately scoped work. A release decision (e.g. #209) should cite applicable acceptance runs and known gaps, not assume that JUnit green implies in-world feature parity. Setup instructions belong in #202; chess-rule regression tests remain separate.

### Status vocabulary

| Status | Meaning | Evidence requirement |
|---|---|---|
| `UNTESTED` | No full acceptance run for this version/SHA | None; never a PASS |
| `BELOW-0.700` / `BELOW-0.702` | Feature introduced in later ChessMod release family | Source/release-history evidence for threshold, target remains universal |
| `GAP-SOURCE` | Expected feature has no source implementation in this branch | Linked inventory; not a claimed in-game failure |
| `YYYY-MM-DD-PASS-ramou` | Complete acceptance by named human tester | Linked run with all required steps |
| `YYYY-MM-DD-PASS-GTF` | All required steps demonstrably automated and passed | Linked run + test IDs, counts, logs, mapping |
| `YYYY-MM-DD-PARTIAL-GTF` | Only GTF subset passed; full feature not accepted | Linked run with remaining manual steps |
| `YYYY-MM-DD-FAIL-ramou` | Executed acceptance scenario failed | Linked run, defect issue and reproduction |
| `YYYY-MM-DD-BLOCKED-ramou` | Test could not be completed, with reason | Linked run and obstacle |

**GitHub formatting:** do not depend on HTML colour attributes or inline CSS; GitHub sanitises styling. Plain-text status tokens and links remain readable and searchable. GitHub's collapsible `<details>` can be used for longer evidence, but is not necessary for the matrix.
