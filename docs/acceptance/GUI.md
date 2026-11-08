# GUI — 2D GUI: pieces, hints, selected and check overlays

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #211, #24.  
**GameTest mapping:** [scenario GUI](GTF_MAP.md#gui).

## Before you start

A board with a known position and screenshot capture available. Prepare a check/checkmate state in a copied test world. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

GUI orientation, selected-square overlay, legal-target hints and check indications remain legible.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| GUI-01 | Human | Open rules-board GUI, select a movable piece and examine candidate-square overlays. | Selected square/piece and possible destinations are visually distinct and correct. |
| GUI-02 | Human | Leave the GUI open, then reopen it after several seconds; capture screenshots if any appearance changes. | No disappearing overlay or unexpected progressive sharpening; document visual inconsistencies (#211). |
| GUI-03 | Human | Rotate board and approach from different directions; reopen GUI. | Board and controls remain interpretable from supported viewpoints. |
| GUI-04 | Human | Set a check/checkmate state and view the GUI. | Correct side and squares show appropriate check/terminal indication. |
| GUI-05 | GTF candidate | Verify board state to GUI data mapping and selection events when test hooks exist. | Selection targets match canonical positions; GTF cannot certify pixels or visual clarity. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
