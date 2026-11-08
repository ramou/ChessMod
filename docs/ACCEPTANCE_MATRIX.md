# Canonical cross-version feature acceptance matrix — #210

This is the **single canonical matrix** for supported Forge Minecraft lines, maintained on the current `1.20.1` default branch. It is **not** a chess rules/JUnit regression matrix, a feature promise for a particular release, or a release approval. **Every listed feature is a target for every Minecraft version**; an older branch may require a different implementation. Nothing is marked `PASS` until a specific run supplies evidence.

## Reading the matrix

- **Rows** are branch tips, newest Minecraft first. Left row header shows `Minecraft version · declared ChessMod version`; the shortened SHA records the Git snapshot used for this initial *source* inventory, **not** an acceptance test. These values must be refreshed from a new bundle before entering test outcomes.
- **Columns** are features. Click their ID header for the canonical acceptance instructions. Feature groups are ordered by introductory **ChessMod release series**, newest first. The 0.6/0.7 grouping is a **provisional planning classification**, particularly for long-lived features such as French-Canadian localisation, Elo and the wrench; verify historical introductions before treating any threshold as a firm claim.
- `UNTESTED` means no auditable end-to-end acceptance run is recorded, even if a user informally saw something working or unit tests passed.
- `BELOW-0.700` / `BELOW-0.702` means **version lag**, not an impossible feature and not a failed test. It calls for forward-porting or backporting the feature into that older branch, then testing it.
- `GAP-SOURCE` means a static source inventory shows the required implementation absent **despite the nominal release family**. It is *not* the result of an executed gameplay test. Do not infer a gap merely because a particular Java class is missing: `1.16.5` registers the wrench as a regular item and handles its behaviour inside `ChessboardBlock`.
- Record completed outcomes as linked evidence, e.g. `[2026-11-08-PASS-ramou](acceptance/runs/2026-11-08-RULES-1.21-ramou.md)`, `[2026-11-08-FAIL-ramou](...)`, `[2026-11-08-PARTIAL-GTF](...)` or `[2026-11-08-BLOCKED-ramou](...)`. `PASS-GTF` is legitimate only if GTF covers **all mandatory steps** of the feature. Never use `N/A` merely because the Minecraft version is old.
- A feature with a supported-version promise that fails acceptance is `FAIL`, with a linked bug; missing implementations are a *different* backlog/gap. Record no `PASS` without complete mandatory step coverage.
- When the branch tip advances and previous acceptance was for a different SHA/version, **retain the tested baseline as an additional labelled row**, and reset the new tip's cells to `UNTESTED` or `PARTIAL` until covered. Don't rewrite a historical result to pretend it tested newer code. See [recording protocol](ACCEPTANCE_TESTING.md).

**Bundle inventory:** 2026-11-08 (`ChessMod-context-multi-15`, exported at 13:19Z). This version snapshot does not prove GitHub is currently at these SHAs. Minecraft **1.21.1** has been tried as a compatibility runtime for the 1.21 binary, but isn't a dedicated branch; record its runtime version in a run rather than inventing a branch row.

## 0.7 series — linked boards

| Minecraft branch · ChessMod version | [QBOARD](acceptance/QBOARD.md) | [QLINK](acceptance/QLINK.md) | [QSYNC](acceptance/QSYNC.md) | [QLIFE](acceptance/QLIFE.md) |
|:--|:--:|:--:|:--:|:--:|
| **1.21 · 1.21-0.702** (`fe7303061a56`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.20.1 · 1.20.1-0.702** (`778c8d506117`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.19.4 · 1.19.4-0.616** (`a9b4e40464c3`) | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **1.18.2 · 1.18.2-0.616** (`89ee3aa13433`) | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **1.17.1 · 1.17.1-0.616** (`7d322671e54f`) | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **1.16.5 · 1.16.5-0.616** (`9ba9d266fb84`) | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **1.15.2 · 1.15.2-0.616** (`1737d188dd06`) | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **1.14.4 · 1.14.4-0.616** (`04a61d8841e0`) | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |

## 0.7 series — delivery and notifications

| Minecraft branch · ChessMod version | [UPDATE](acceptance/UPDATE.md) | [IMPORT](acceptance/IMPORT.md) |
|:--|:--:|:--:|
| **1.21 · 1.21-0.702** (`fe7303061a56`) | `UNTESTED` | `UNTESTED` |
| **1.20.1 · 1.20.1-0.702** (`778c8d506117`) | `UNTESTED` | `UNTESTED` |
| **1.19.4 · 1.19.4-0.616** (`a9b4e40464c3`) | `BELOW-0.702` | `BELOW-0.702` |
| **1.18.2 · 1.18.2-0.616** (`89ee3aa13433`) | `BELOW-0.702` | `BELOW-0.702` |
| **1.17.1 · 1.17.1-0.616** (`7d322671e54f`) | `BELOW-0.702` | `BELOW-0.702` |
| **1.16.5 · 1.16.5-0.616** (`9ba9d266fb84`) | `BELOW-0.702` | `BELOW-0.702` |
| **1.15.2 · 1.15.2-0.616** (`1737d188dd06`) | `BELOW-0.702` | `BELOW-0.702` |
| **1.14.4 · 1.14.4-0.616** (`04a61d8841e0`) | `BELOW-0.702` | `BELOW-0.702` |

## 0.6 series — boards and controls

| Minecraft branch · ChessMod version | [WOOD](acceptance/WOOD.md) | [RULES](acceptance/RULES.md) | [STUBS](acceptance/STUBS.md) | [WRENCH](acceptance/WRENCH.md) |
|:--|:--:|:--:|:--:|:--:|
| **1.21 · 1.21-0.702** (`fe7303061a56`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.20.1 · 1.20.1-0.702** (`778c8d506117`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.19.4 · 1.19.4-0.616** (`a9b4e40464c3`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.18.2 · 1.18.2-0.616** (`89ee3aa13433`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.17.1 · 1.17.1-0.616** (`7d322671e54f`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.16.5 · 1.16.5-0.616** (`9ba9d266fb84`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.15.2 · 1.15.2-0.616** (`1737d188dd06`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.14.4 · 1.14.4-0.616** (`04a61d8841e0`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |

## 0.6 series — experience and persistence

| Minecraft branch · ChessMod version | [GUI](acceptance/GUI.md) | [AUDIO](acceptance/AUDIO.md) | [SAVE](acceptance/SAVE.md) | [MULTI](acceptance/MULTI.md) |
|:--|:--:|:--:|:--:|:--:|
| **1.21 · 1.21-0.702** (`fe7303061a56`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.20.1 · 1.20.1-0.702** (`778c8d506117`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.19.4 · 1.19.4-0.616** (`a9b4e40464c3`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.18.2 · 1.18.2-0.616** (`89ee3aa13433`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.17.1 · 1.17.1-0.616** (`7d322671e54f`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.16.5 · 1.16.5-0.616** (`9ba9d266fb84`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.15.2 · 1.15.2-0.616** (`1737d188dd06`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **1.14.4 · 1.14.4-0.616** (`04a61d8841e0`) | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |

## 0.6 series — localisation and progression

| Minecraft branch · ChessMod version | [FRCA](acceptance/FRCA.md) | [ELO](acceptance/ELO.md) |
|:--|:--:|:--:|
| **1.21 · 1.21-0.702** (`fe7303061a56`) | `UNTESTED` | `UNTESTED` |
| **1.20.1 · 1.20.1-0.702** (`778c8d506117`) | `UNTESTED` | `UNTESTED` |
| **1.19.4 · 1.19.4-0.616** (`a9b4e40464c3`) | `UNTESTED` | `UNTESTED` |
| **1.18.2 · 1.18.2-0.616** (`89ee3aa13433`) | `UNTESTED` | `UNTESTED` |
| **1.17.1 · 1.17.1-0.616** (`7d322671e54f`) | `UNTESTED` | `UNTESTED` |
| **1.16.5 · 1.16.5-0.616** (`9ba9d266fb84`) | `UNTESTED` | `UNTESTED` |
| **1.15.2 · 1.15.2-0.616** (`1737d188dd06`) | `UNTESTED` | `UNTESTED` |
| **1.14.4 · 1.14.4-0.616** (`04a61d8841e0`) | `UNTESTED` | `UNTESTED` |

## Feature index and tracking

| ID | User-observable feature | Release grouping (provisional) | Key issues |
|---|---|---|---|
| [QBOARD](acceptance/QBOARD.md) | Quantum board placement and presentation | 0.7 / threshold 0.700 | #162 |
| [QLINK](acceptance/QLINK.md) | Ender-pearl linking, unlinking and guardrails | 0.7 / threshold 0.700 | #66, #194 |
| [QSYNC](acceptance/QSYNC.md) | Linked-board play and rule synchronisation | 0.7 / threshold 0.700 | #199, #207 |
| [QLIFE](acceptance/QLIFE.md) | Linked-board lifecycle, save/reload and unload | 0.7 / threshold 0.700 | #172, #183, #186 |
| [UPDATE](acceptance/UPDATE.md) | Forge update discovery | 0.7 / threshold 0.702 | #206 |
| [IMPORT](acceptance/IMPORT.md) | Launcher import and distributable package | 0.7 / threshold 0.702 | #205, #209 |
| [WOOD](acceptance/WOOD.md) | Wood board and free piece placement | 0.6 / threshold 0.600 | #13 |
| [RULES](acceptance/RULES.md) | Rules-based board and turn enforcement | 0.6 / threshold 0.600 | #30, #200, #208 |
| [STUBS](acceptance/STUBS.md) | Chesses, AI and Puzzle board placeholders | 0.6 / threshold 0.600 | #43, #67, #68 |
| [WRENCH](acceptance/WRENCH.md) | Chess wrench rotate and reset | 0.6 / threshold 0.600 | #154, #158 |
| [GUI](acceptance/GUI.md) | 2D GUI: pieces, hints, selected and check overlays | 0.6 / threshold 0.600 | #211, #24 |
| [AUDIO](acceptance/AUDIO.md) | Piece moves, captures, sound and subtitles | 0.6 / threshold 0.600 | #187 |
| [SAVE](acceptance/SAVE.md) | Single-board persistence and chunk lifecycle | 0.6 / threshold 0.600 | #92 |
| [MULTI](acceptance/MULTI.md) | Two-player and dedicated-server behaviour | 0.6 / threshold 0.600 | #202, #207 |
| [FRCA](acceptance/FRCA.md) | French-Canadian localisation | 0.6 / threshold 0.600 | #161, #180 |
| [ELO](acceptance/ELO.md) | Elo ratings and persistence | 0.6 / threshold 0.600 | #82, #83 |

## Evidence and GameTest

[Acceptance protocol](ACCEPTANCE_TESTING.md) · [Run template](acceptance/RUN_TEMPLATE.md) · [GTF implementation map](acceptance/GTF_MAP.md) · [Evidence records](acceptance/runs/README.md)

GameTest suitability is expressed *per step* in each feature file. Its canonical assertions come from the feature specification; branch-specific GTF implementations are linked from the GTF map when actually written and verified. GTF does **not** automatically update this matrix. If a Minecraft version has no working GTF, perform the applicable steps manually.

No claimed success is inferred from source presence, GitHub issue closure, unit tests, or ForgeGradle launch targets. Failures feed narrowly scoped bugs and release decisions such as #209; they do not belong in a new regression-test ledger here.
