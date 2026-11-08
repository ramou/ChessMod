# QLIFE — Linked-board lifecycle, save/reload and unload

**Acceptance specification:** ChessMod 0.7 family / candidate minimum 0.700.  
**Related issues:** #172, #183, #186.  
**GameTest mapping:** [scenario QLIFE](GTF_MAP.md#qlife).

## Before you start

A backup of a world with two linked quantum boards and at least one recorded move. Use a disposable copy for breaking boards. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Links remain valid or fail safely through save/load, chunk unload, dimension changes and block breaking.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| QLIFE-01 | Human | Link two boards, play a move, save/quit, then reload. | State and link survive as designed, with identical boards and a coherent next turn. |
| QLIFE-02 | Human | Unload one board chunk (travel or change dimension) and then return. | No crash, duplication, loss of state or silent divergent board state. |
| QLIFE-03 | Human | Break one linked board, revisit the other, and attempt to relink a new board. | The remaining board safely becomes independent and can be linked again. |
| QLIFE-04 | GTF candidate | Save/reload the linked block entities and simulate invalid or absent linkage references. | Serialized state preserves valid pairs; missing counterpart fails safely and cleans link metadata. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
