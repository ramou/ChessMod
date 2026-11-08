# RULES — Rules-based board and turn enforcement

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #30, #200, #208.  
**GameTest mapping:** [scenario RULES](GTF_MAP.md#rules).

## Before you start

Gold/rules board in a disposable world. For special positions, use a saved fixture or a documented legal move sequence; if no fixture can be prepared, record the affected step as BLOCKED. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Gold/rules board enforces standard chess legality, including special moves and terminal states.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| RULES-01 | Human | Place a gold board; open GUI; try a legal move, followed by the same colour again. | Legal move succeeds, illegal turn is rejected without changing the position. |
| RULES-02 | Human | For en passant, play 1.e4 a6 2.e5 d5 3.exd6 e.p. on a fresh board; for castling-through-check and promotion, use an annotated saved-board fixture and record its setup. | Only legal castling is allowed; en passant and promotion follow chess rules. |
| RULES-03 | Human | From prepared check/checkmate/stalemate positions, inspect hints and attempted moves. | Check, terminal states and legal move restrictions match the expected chess position. |
| RULES-04 | GTF candidate | Drive a rules-board block entity through legal/illegal server-side move messages. | Server refuses invalid moves, does not advance turns on rejection and emits one canonical state change. |
| RULES-05 | Human | Reproduce king-on-b1 in check by knight c3 with a1 safe; attempt b1-a1. | King escape is accepted, not mistaken for castling (#208). |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
