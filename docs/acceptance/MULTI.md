# MULTI — Two-player and dedicated-server behaviour

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #202, #207.  
**GameTest mapping:** [scenario MULTI](GTF_MAP.md#multi).

## Before you start

Compatible Forge dedicated server and two distinct clients with the same ChessMod JAR. Follow issue #202 for installation instructions. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Authoritative rules and visible board state remain consistent across distinct clients and a server.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| MULTI-01 | Human | On a dedicated Forge server with two clients, place a rules board and join as both players. | No dedicated-server classloading error; both clients see identical board. |
| MULTI-02 | Human | Alternate moves from the two clients and attempt an illegal move. | Only accepted moves propagate; neither client can advance turn without valid move. |
| MULTI-03 | Human | Disconnect one client, make a move from the other, then reconnect. | Reconnecting client receives correct authoritative board state. |
| MULTI-04 | GTF candidate | Drive server-side packet/board mutation, asserting one canonical update and rejection of invalid moves. | State invariants hold; GTF does not substitute for real two-client latency testing. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
