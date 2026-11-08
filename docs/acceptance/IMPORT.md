# IMPORT — Launcher import and distributable package

**Acceptance specification:** ChessMod 0.7 family / candidate minimum 0.702.  
**Related issues:** #205, #209.  
**GameTest mapping:** [scenario IMPORT](GTF_MAP.md#import).

## Before you start

A release JAR and ZIP with SHA-256 checksums and a fresh PrismLauncher/MultiMC instance without an older ChessMod copy. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

The published JAR and generated launcher ZIP install for the designated Minecraft/Forge versions.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| IMPORT-01 | Human | Import the published release ZIP into a fresh PrismLauncher/MultiMC instance. | Launcher recognises intended Minecraft and Forge targets without downloading an obsolete ChessMod JAR. |
| IMPORT-02 | Human | Start instance, load a world, and open a board. | ChessMod appears in Mods, assets load, and basic board interaction works. |
| IMPORT-03 | Human | Separately install the standalone JAR into a matching Forge instance. | No duplicate mod; startup succeeds and version shown matches published release. |
| IMPORT-04 | GTF candidate | Check manifest, JAR contents and release provenance/checksums. | Metadata matches the intended branch/source SHA; no unlicensed third-party JAR is bundled. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
