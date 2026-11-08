# SAVE — Single-board persistence and chunk lifecycle

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #92.  
**GameTest mapping:** [scenario SAVE](GTF_MAP.md#save).

## Before you start

Disposable world or backed-up save with a placed chessboard. Record the exact board position and side to move. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Non-quantum boards keep board state, orientation and turn through save/reload and chunk cycles.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| SAVE-01 | Human | Play four moves, note exact position and side to move, then save and exit. | Reopening world and GUI restores exact position and turn. |
| SAVE-02 | Human | Leave the chunk or dimension, return and inspect block and GUI. | Board is unchanged; no phantom move or reset. |
| SAVE-03 | Human | Break/place boards under the documented rules and inspect expected reset or state preservation. | Reset and drops follow user-facing contract without crash. |
| SAVE-04 | GTF candidate | Save and load block entity NBT from a fixture in a game-test environment. | Position and move history round-trip; malformed state fails safely or is reported. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
