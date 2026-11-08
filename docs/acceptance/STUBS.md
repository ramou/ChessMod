# STUBS — Chesses, AI and Puzzle board placeholders

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #43, #67, #68.  
**GameTest mapping:** [scenario STUBS](GTF_MAP.md#stubs).

## Before you start

Creative world with Chesses, AI and Puzzle board items and a clear place to set down each. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Placeholder board items exist and behave according to their documented *current* scope; no claim of implemented AI or puzzles.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| STUBS-01 | Human | Obtain and place one Chesses, AI and Puzzle board each. | All are distinct items/blocks, render correctly and are accessible without crashes. |
| STUBS-02 | Human | Interact with each board and observe messages, model and GUI. | Behaviour matches the documented placeholder scope; no non-existent AI or puzzle gameplay is advertised. |
| STUBS-03 | Human | Break and re-place the boards, then reload the world. | Board types retain identity and basic state; no duplication or crash. |
| STUBS-04 | GTF candidate | Create each registered block and block entity in a game-test structure. | Registry and block entity mappings are correct and loadable. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
