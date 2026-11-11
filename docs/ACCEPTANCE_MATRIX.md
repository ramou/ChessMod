# Canonical cross-version feature acceptance matrix — #210

This is the **single canonical matrix** for supported Forge Minecraft lines, maintained on the current `1.20.1` default branch. It is **not** a chess rules/JUnit regression matrix, a feature promise for a particular release, or a release approval. **Every listed feature is a target for every Minecraft version**; an older branch may require a different implementation. Nothing is marked `PASS` until a specific run supplies evidence.

## Reading the matrix

- **Rows** identify a **specific published ChessMod release**, newest Minecraft first; click the release/version label to open its GitHub release or legacy CurseForge file. A row stays valid when the corresponding source branch gains later commits. The table never needs a Git SHA refresh merely because someone committed to a branch.
- **Columns** are features; click each ID header for canonical acceptance instructions. Feature groups are ordered by introductory **ChessMod release series**, newest first. The 0.6/0.7 grouping is provisional, particularly for long-lived features such as French-Canadian localisation, Elo and the wrench.
- `UNTESTED` means no auditable end-to-end acceptance run is recorded for that release, even if an informal smoke test or JUnit run succeeded.
- `BELOW-0.700` / `BELOW-0.701` / `BELOW-0.702` means **release-version lag**, not an impossible feature or test failure. That feature is still a porting target.
- `GAP-SOURCE` means the most recent audited source or release inventory found an expected implementation absent; it is **not an executed gameplay failure**. The linked release is the intended acceptance target. Recheck static findings if that release and the audited source differ. For example, `1.16.5` implements the wrench inside `ChessboardBlock`, not a dedicated wrench class.
- Record full outcomes by linking the cell to an **evidence run file**: e.g. `2026-11-08-PASS-ramou`, `2026-11-08-FAIL-ramou`, `2026-11-08-PARTIAL-GTF`. `PASS-GTF` requires coverage of all mandatory steps. Never use `N/A` merely because a Minecraft version is old.
- An observed failure is `FAIL` with a bug link. A missing implementation is a backlog gap, not a test result. Don't infer success from a published release.
- **Update a row only when a new release is published or new evidence is recorded.** When a newer release supersedes one with useful acceptance evidence, retain the older release as an additional labelled row. Never copy its PASS into the new release without testing. Unreleased development builds may be tracked in a clearly separate provisional row, labelled `UNRELEASED`, and should not link to a future/nonexistent release.

**Release inventory:** recorded 2026-11-11 from the supplied Git bundle, shared update feed and release history. The links identify the intended **published binaries**, not mutable branch heads. The three `0.702` rows use the release-manager GitHub tag naming convention; the older `0.616` distributions are on CurseForge. For legacy versions where a direct file link was not verified, the link leads to the version-filtered CurseForge files listing instead. Replace those with exact release-file links when handy, but **do not change a row for routine code commits**. Minecraft 1.21.1 runtime compatibility belongs to #212, not an extra branch row here.

### 1.19.4 evidence boundary

The earlier quantum-board backport was smoke-tested by the maintainer before the `0.702` release-preparation commit. The reported basics included a cross-dimensional quantum-board interaction; see the [retrospective, limited smoke-test record](acceptance/runs/2026-11-11-QUANTUM-SMOKE-1.19.4-ramou.md). That report describes an **unreleased pre-bump build**, not a step-complete acceptance run on the published `1.19.4-0.702` artefact. Accordingly **no cell for the published release is promoted to PARTIAL or PASS solely from that report**. A documented run against the released JAR can populate the appropriate cells.

**Localisation inventory:** Simplified Chinese was introduced on `1.20.1` while the declared mod version was `0.701` (commit `da8c6ef`), and `zh_cn.json` is present on the 1.20.1 and 1.19.4 tips. The 1.21 tip lacks this resource: `GAP-SOURCE` is a static inventory finding, **not** a failed in-game test. This is a feature parity defect to address separately from release/loader compatibility (#212).

## 0.7 series — linked boards

| Minecraft branch · ChessMod version | [QBOARD](acceptance/QBOARD.md) | [QLINK](acceptance/QLINK.md) | [QSYNC](acceptance/QSYNC.md) | [QLIFE](acceptance/QLIFE.md) |
|:--|:--:|:--:|:--:|:--:|
| **[1.21 · 1.21-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.21-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.20.1 · 1.20.1-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.20.1-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.19.4 · 1.19.4-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.19.4-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.18.2 · 1.18.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/3821844)** | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **[1.17.1 · 1.17.1-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.17.1)** | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **[1.16.5 · 1.16.5-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.16.5)** | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **[1.15.2 · 1.15.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.15.2)** | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |
| **[1.14.4 · 1.14.4-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/4658508)** | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` | `BELOW-0.700` |

## 0.7 series — delivery and notifications

| Minecraft branch · ChessMod version | [UPDATE](acceptance/UPDATE.md) | [IMPORT](acceptance/IMPORT.md) |
|:--|:--:|:--:|
| **[1.21 · 1.21-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.21-forge-v0.702)** | `UNTESTED` | `UNTESTED` |
| **[1.20.1 · 1.20.1-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.20.1-forge-v0.702)** | `UNTESTED` | `UNTESTED` |
| **[1.19.4 · 1.19.4-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.19.4-forge-v0.702)** | `UNTESTED` | `UNTESTED` |
| **[1.18.2 · 1.18.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/3821844)** | `BELOW-0.702` | `BELOW-0.702` |
| **[1.17.1 · 1.17.1-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.17.1)** | `BELOW-0.702` | `BELOW-0.702` |
| **[1.16.5 · 1.16.5-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.16.5)** | `BELOW-0.702` | `BELOW-0.702` |
| **[1.15.2 · 1.15.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.15.2)** | `BELOW-0.702` | `BELOW-0.702` |
| **[1.14.4 · 1.14.4-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/4658508)** | `BELOW-0.702` | `BELOW-0.702` |

## 0.7 series — additional localisation

| Minecraft branch · ChessMod version | [ZHCN](acceptance/ZHCN.md) |
|:--|:--:|
| **[1.21 · 1.21-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.21-forge-v0.702)** | `GAP-SOURCE` |
| **[1.20.1 · 1.20.1-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.20.1-forge-v0.702)** | `UNTESTED` |
| **[1.19.4 · 1.19.4-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.19.4-forge-v0.702)** | `UNTESTED` |
| **[1.18.2 · 1.18.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/3821844)** | `BELOW-0.701` |
| **[1.17.1 · 1.17.1-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.17.1)** | `BELOW-0.701` |
| **[1.16.5 · 1.16.5-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.16.5)** | `BELOW-0.701` |
| **[1.15.2 · 1.15.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.15.2)** | `BELOW-0.701` |
| **[1.14.4 · 1.14.4-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/4658508)** | `BELOW-0.701` |

## 0.6 series — boards and controls

| Minecraft branch · ChessMod version | [WOOD](acceptance/WOOD.md) | [RULES](acceptance/RULES.md) | [STUBS](acceptance/STUBS.md) | [WRENCH](acceptance/WRENCH.md) |
|:--|:--:|:--:|:--:|:--:|
| **[1.21 · 1.21-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.21-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.20.1 · 1.20.1-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.20.1-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.19.4 · 1.19.4-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.19.4-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.18.2 · 1.18.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/3821844)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.17.1 · 1.17.1-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.17.1)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.16.5 · 1.16.5-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.16.5)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.15.2 · 1.15.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.15.2)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.14.4 · 1.14.4-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/4658508)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |

## 0.6 series — experience and persistence

| Minecraft branch · ChessMod version | [GUI](acceptance/GUI.md) | [AUDIO](acceptance/AUDIO.md) | [SAVE](acceptance/SAVE.md) | [MULTI](acceptance/MULTI.md) |
|:--|:--:|:--:|:--:|:--:|
| **[1.21 · 1.21-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.21-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.20.1 · 1.20.1-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.20.1-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.19.4 · 1.19.4-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.19.4-forge-v0.702)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.18.2 · 1.18.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/3821844)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.17.1 · 1.17.1-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.17.1)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.16.5 · 1.16.5-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.16.5)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.15.2 · 1.15.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.15.2)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |
| **[1.14.4 · 1.14.4-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/4658508)** | `UNTESTED` | `UNTESTED` | `UNTESTED` | `UNTESTED` |

## 0.6 series — localisation and progression

| Minecraft branch · ChessMod version | [FRCA](acceptance/FRCA.md) | [ELO](acceptance/ELO.md) |
|:--|:--:|:--:|
| **[1.21 · 1.21-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.21-forge-v0.702)** | `UNTESTED` | `UNTESTED` |
| **[1.20.1 · 1.20.1-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.20.1-forge-v0.702)** | `UNTESTED` | `UNTESTED` |
| **[1.19.4 · 1.19.4-0.702](https://github.com/ramou/ChessMod/releases/tag/mc1.19.4-forge-v0.702)** | `UNTESTED` | `UNTESTED` |
| **[1.18.2 · 1.18.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/3821844)** | `UNTESTED` | `UNTESTED` |
| **[1.17.1 · 1.17.1-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.17.1)** | `UNTESTED` | `UNTESTED` |
| **[1.16.5 · 1.16.5-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.16.5)** | `UNTESTED` | `UNTESTED` |
| **[1.15.2 · 1.15.2-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/all?version=1.15.2)** | `UNTESTED` | `UNTESTED` |
| **[1.14.4 · 1.14.4-0.616](https://www.curseforge.com/minecraft/mc-mods/chessmod/files/4658508)** | `UNTESTED` | `UNTESTED` |

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
| [ZHCN](acceptance/ZHCN.md) | Simplified Chinese localisation | 0.7 / observed in 0.701 | #210 |
| [FRCA](acceptance/FRCA.md) | French-Canadian localisation | 0.6 / threshold 0.600 | #161, #180 |
| [ELO](acceptance/ELO.md) | Elo ratings and persistence | 0.6 / threshold 0.600 | #82, #83 |

## Evidence and GameTest

[Acceptance protocol](ACCEPTANCE_TESTING.md) · [Run template](acceptance/RUN_TEMPLATE.md) · [GTF implementation map](acceptance/GTF_MAP.md) · [Evidence records](acceptance/runs/README.md)

GameTest suitability is expressed *per step* in each feature file. Its canonical assertions come from the feature specification; branch-specific GTF implementations are linked from the GTF map when actually written and verified. GTF does **not** automatically update this matrix. If a Minecraft version has no working GTF, perform the applicable steps manually.

No claimed success is inferred from source presence, GitHub issue closure, unit tests, or ForgeGradle launch targets. Failures feed narrowly scoped bugs and release decisions such as #209; they do not belong in a new regression-test ledger here.
