# WOOD — Wood board and free piece placement

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #13.  
**GameTest mapping:** [scenario WOOD](GTF_MAP.md#wood).

## Before you start

Creative world with a wood chessboard, sufficient space and the 2D GUI available. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Wood board provides unrestricted manual setup/play without enforcing chess turn legality.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| WOOD-01 | Human | Craft or obtain a wood board, place it and open the 2D GUI. | Board model and GUI load and display a starting position. |
| WOOD-02 | Human | Move a piece of either colour, including a deliberately non-standard move or turn order. | Free-play behaviour is intentionally permissive and the resulting position is visible in-world and in the GUI. |
| WOOD-03 | Human | Close and reopen the GUI and revisit the world board. | Positions agree with the changes made. |
| WOOD-04 | GTF candidate | Place a wood board, inspect the block entity, and process a free-placement action. | Block entity and board state exist; action follows free-play semantics. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
