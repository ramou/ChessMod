# QSYNC — Linked-board play and rule synchronisation

**Acceptance specification:** ChessMod 0.7 family / candidate minimum 0.700.  
**Related issues:** #199, #207.  
**GameTest mapping:** [scenario QSYNC](GTF_MAP.md#qsync).

## Before you start

Two linked quantum boards sharing a known starting state; for multiplayer steps arrange two separate clients/players. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Every move has one authoritative outcome, visible from both ends; neither board permits an illegal move.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| QSYNC-01 | Human | Link two boards; play a legal opening move on the first, then a reply using the second. | Both boards show exactly the same positions, captures and side to move without a screen reopen. |
| QSYNC-02 | Human | Try an illegal move or a move that leaves the king in check from either board. | Both reject it and remain identical; no turn advances without the move. |
| QSYNC-03 | Human | Repeat in two-player multiplayer with one player at each end. | Remote clients and server converge after every move; record latency and any stale GUI effects. |
| QSYNC-04 | GTF candidate | Apply a legal and an illegal move to one linked server-side board pair and compare canonical board state. | Single authoritative update; illegal move changes neither board nor turn. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
