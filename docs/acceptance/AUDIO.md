# AUDIO — Piece moves, captures, sound and subtitles

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #187.  
**GameTest mapping:** [scenario AUDIO](GTF_MAP.md#audio).

## Before you start

Master and Blocks volumes audible, Minecraft subtitles enabled, and a confirmed audible vanilla sound for comparison. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

Sound effects and subtitle cues work during real play for ordinary moves and captures.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| AUDIO-01 | Human | With Blocks/Master volume audible, move a piece on wood and gold boards. | A distinct move sound is audible at the board position and subtitle appears when enabled. |
| AUDIO-02 | Human | Set a capture position and perform a capture; repeat for alternate board types. | Capture effect/subtitle plays without duplicate or missing effects. |
| AUDIO-03 | Human | Compare relative volume at near and far positions and after a fresh game launch. | Sound is perceptible when nearby; no silent events or unexpected global playback. |
| AUDIO-04 | GTF candidate | Verify registered sound-event keys and packaged sounds.json/OGG resources. | All referenced asset paths are resolvable; this cannot certify audible output. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
