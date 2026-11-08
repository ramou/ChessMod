# QBOARD — Quantum board placement and presentation

**Acceptance specification:** ChessMod 0.7 family / candidate minimum 0.700.  
**Related issues:** #162.  
**GameTest mapping:** [scenario QBOARD](GTF_MAP.md#qboard).

## Before you start

Creative test world with two quantum boards, an unobstructed area and a way to observe the linked-state cue. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

A quantum board is obtainable, placeable, visible, interactive, and indicates linked state.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| QBOARD-01 | Human | Obtain a quantum chessboard; place it on a clear, flat surface; inspect the board from several directions. | It places once, renders pieces and model properly, and opens its rules-based GUI. |
| QBOARD-02 | Human | Place another unlinked quantum board nearby and compare appearance and status. | Unlinked presentation and interaction are consistent; no phantom link is indicated. |
| QBOARD-03 | GTF candidate | Spawn/inspect the quantum block entity and block-state fields on an isolated test structure. | Registered type and unlinked default state are correct and server-readable. |
| QBOARD-04 | Human | After linking by F-LINK, compare visible linked-state cue and board interaction. | Linked cue reflects actual state and remains readable; any flicker or missing texture is recorded. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
