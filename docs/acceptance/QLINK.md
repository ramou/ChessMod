# QLINK — Ender-pearl linking, unlinking and guardrails

**Acceptance specification:** ChessMod 0.7 family / candidate minimum 0.700.  
**Related issues:** #66, #194.  
**GameTest mapping:** [scenario QLINK](GTF_MAP.md#qlink).

## Before you start

Two placed quantum boards and at least two ender pearls in a disposable world. Record both coordinates and dimensions. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Two quantum boards link through the documented ender-pearl workflow and reject invalid pairings safely.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| QLINK-01 | Human | Place two quantum boards; use an ender pearl on the first then the second. | Both report a completed link and the second shares the intended board state. |
| QLINK-02 | Human | Attempt self-linking, re-linking an already linked pair, and pairing incompatible or unavailable boards. | The operation is rejected with a comprehensible message; no item/state corruption or crash. |
| QLINK-03 | Human | Use the documented unlink action, if implemented; try linking again. | The two boards become independent and can be relinked without stale association; otherwise record FAIL or BLOCKED with reason. |
| QLINK-04 | GTF candidate | Arrange two quantum block entities in a test structure, run the server-side link operation and validate IDs and state. | Only valid pairings persist and rejected operations preserve both entities. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
