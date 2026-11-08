# ELO — Elo ratings and persistence

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #82, #83.  
**GameTest mapping:** [scenario ELO](GTF_MAP.md#elo).

## Before you start

Two players and a supported way to inspect ratings. If no interface exists, record BLOCKED with issue #82 rather than inventing an observation. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

A player rating can be observed, persists, and changes only under the documented rating policy.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| ELO-01 | Human | Record two players’ current Elo values using the branch’s supported observation interface; record exactly how values were obtained. | Rating values can be inspected consistently; if no public/admin observation path exists, record BLOCKED and link work item #82. |
| ELO-02 | Human | Play a rated match if the branch implements one, then observe both ratings. | Outcome changes ratings according to the documented policy; if no rated-match flow exists, record GAP rather than PASS. |
| ELO-03 | Human | Disconnect/reconnect and die/respawn as applicable; observe Elo values again. | Rating persists across supported lifecycle operations and is not unexpectedly reset to 1000 (#83). |
| ELO-04 | GTF candidate | If Elo capability exposes test hooks, verify persisted state, cloning and 0-rating edge cases. | State round-trips and does not corrupt/reinitialise valid values. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
