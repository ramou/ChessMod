# WRENCH — Chess wrench rotate and reset

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #154, #158.  
**GameTest mapping:** [scenario WRENCH](GTF_MAP.md#wrench).

## Before you start

Creative world with a chess wrench, a placed board, an initial orientation and a recorded starting position. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Wrench rotates a board and provides its documented reset interaction without destroying the board.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| WRENCH-01 | Human | Obtain a chess wrench; interact with a placed board using it four times. | Board orientation changes by quarter-turns and returns to its initial orientation. |
| WRENCH-02 | Human | Make several moves, then hold the wrench and attempt to break/left-click the board (as in the 1.20.1 implementation); document a different branch-specific gesture if needed. | Board resets to the intended starting state without breaking or duplicating the block; verify precise gesture on that branch. |
| WRENCH-03 | Human | Save/reload after rotation and reset; inspect GUI orientation. | Rotation, saved state and GUI agree. |
| WRENCH-04 | GTF candidate | Trigger wrench interactions and assert block FACING/state transitions, reset and preservation of block entity. | Exactly the documented state changes occur; no unrelated state mutation. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
