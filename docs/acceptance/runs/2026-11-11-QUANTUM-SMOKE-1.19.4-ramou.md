# Retrospective quantum-board smoke-test report — 2026-11-11 / 1.19.4

**Evidence classification: maintainer-reported, limited smoke test; NOT a complete #210 acceptance run.**

- **Reported by:** `ramou` (human), in the ChessMod development conversation on 2026-11-11.
- **Source context:** After Patch 028 (quantum-board backport) compiled and before Patches 029/030 (0.702 release preparation) were applied. The backport was subsequently committed at `b4ba4c44fcfe174d4b1a4e35a336746fcb6ef982` (`1.19.4-0.616` declaration). The **exact working-tree SHA at test time is not recorded**; this commit identifies the backported implementation for reference, not verified binary provenance.
- **Minecraft:** 1.19.4; **Forge runtime build:** not recorded; **Java/launcher/world:** not recorded.
- **ChessMod:** 1.19.4 branch, before version bump to `0.702`; **binary SHA-256:** not recorded.
- **Environment:** An in-game build was exercised. Whether single-player, a dedicated server, or multiple clients were involved was not recorded.

## Reported observations

The maintainer reported that the 1.19.4 backport **built smoothly**, and that basic quantum-board functionality worked, including a **cross-dimensional test**. No detailed reproduction sequence, screenshots, logs or per-step outcomes were supplied. This is useful development evidence, but the precise QBOARD/QLINK/QLIFE step coverage cannot be reconstructed.

| Canonical scenario | Evidence status | Missing for full acceptance |
|---|---|---|
| [QBOARD](../QBOARD.md) | Informal basic gameplay observation only | Record QBOARD-01 through QBOARD-04 outcomes against a specific JAR |
| [QLINK](../QLINK.md) | Informal cross-dimensional interaction observation only | Record QLINK-01 through QLINK-04, including rejection/unlink behaviour |
| [QSYNC](../QSYNC.md) | Not described | Move synchronisation, illegal moves, and multiplayer |
| [QLIFE](../QLIFE.md) | Not described | Save/reload, chunk unload, break/relink behaviour |

**Outcome:** retrospective smoke check, **not a matrix `PASS` or `PARTIAL` claim on the 1.19.4-0.702 tip**. The subsequent committed tip `4d83309ba8c882dc29909143365250acb4f436a8` changes release metadata and packaging, so a version-specific run should test that released artefact before any current-tip status is elevated. Do not silently transfer these observations to that new commit.

**Follow-up:** run the linked canonical steps and create separate, step-scored evidence records for the 0.702 build. Relevant tracking: [#209](https://github.com/ramou/ChessMod/issues/209), [#210](https://github.com/ramou/ChessMod/issues/210), [#207](https://github.com/ramou/ChessMod/issues/207).
