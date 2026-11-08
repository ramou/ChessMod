# FRCA — French-Canadian localisation

**Acceptance specification:** ChessMod 0.6 family / candidate minimum 0.600.  
**Related issues:** #161, #180.  
**GameTest mapping:** [scenario FRCA](GTF_MAP.md#frca).

## Before you start

Minecraft language menu with French (Canada) and English available. Use a world with the board types being assessed. Record the exact Minecraft, Forge and ChessMod versions, source commit, tester and world in the run record.

## User-observable acceptance contract

User-facing labels, tooltips, board messages and sound subtitles use the fr_CA locale where translated.

Run against the **actual version in the matrix row**. Features are universal targets, not impossible or `N/A` on old Minecraft versions. If this branch lacks the feature, record a version gap or implementation gap; do not pretend the test passed. Version-family assignments are provisional until confirmed from releases/history.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| FRCA-01 | Human | Switch Minecraft language to Français (Canada) and reopen the game/mod UI. | Board names, wrench label, core descriptions and sound subtitles use French strings. |
| FRCA-02 | Human | Perform quantum linking where supported; otherwise inspect localisation keys relevant to branch. | Available messages display translated text, not raw keys or English fallbacks where expected. |
| FRCA-03 | Human | Change back to English and compare critical messages. | No language-switch crash or broken formatting; placeholders interpolate correctly. |
| FRCA-04 | GTF candidate | Check language JSON syntax, required keys and format placeholders in packaged resources. | Key sets and format arguments remain consistent; actual displayed translation remains a human check. |

**Execution rule:** A GTF candidate is still a **manual step** on a branch with no verified GTF implementation. A GameTest run covers only the specific steps it asserts; any remaining required human steps must be completed before recording full-feature `PASS`. Link a [run record](RUN_TEMPLATE.md) from the matrix cell, with the full commit, versions, environment and evidence.
