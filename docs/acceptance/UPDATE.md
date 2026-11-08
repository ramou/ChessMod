# UPDATE — Forge update discovery

**Acceptance specification:** ChessMod 0.7 family / candidate minimum 0.702.  
**Related issues:** #206.  
**GameTest mapping:** [scenario UPDATE](GTF_MAP.md#update).

## Before you start

A published release and a controlled, non-production feed fixture for testing a newer recommended version. Do not spoof the live feed. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Compatible builds find an update feed and correctly report current/newer releases without self-installing.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| UPDATE-01 | Human | Open Mods screen with network available and an update feed advertising the installed version. | Forge reports up-to-date; download URL resolves to the correct release page. |
| UPDATE-02 | Human | Using a controlled test feed or safe pre-release test version, advertise a newer compatible mod. | Forge indicates an update and displays the appropriate change notes and destination; no automatic JAR overwrite. |
| UPDATE-03 | GTF candidate | Inspect built META-INF/mods.toml and feed JSON. | Configured updateJSONURL resolves; recommendation key matches running Minecraft version; changelog structure is valid. |
| UPDATE-04 | Human | Repeat with no network or a temporary feed failure. | Game still loads; warning/error is non-fatal and documented. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
