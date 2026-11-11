# ZHCN — Simplified Chinese localisation

**Acceptance specification:** ChessMod 0.7 family / first observed in `1.20.1-0.701` (Git commit `da8c6ef`, 2024-09-25).  
**Related issue:** #210.  
**GameTest mapping:** [scenario ZHCN](GTF_MAP.md#zhcn).

## Before you start

Use Minecraft's 简体中文 (Simplified Chinese) language option and a clean world with accessible ChessMod boards and a wrench. Prepare a quantum board pair where available. Record the Minecraft/Forge/ChessMod versions and link the published release (or identify an unreleased test build). A source SHA is optional for a published release. A resource file existing in Git does not establish that the game displays every string correctly.

## User-observable acceptance contract

Minecraft presents the ChessMod names, tooltips, messages and subtitles supported by its `zh_cn` translations without raw keys, broken placeholder substitution or UI regressions. The same feature is a target on every supported Minecraft branch; a missing resource remains a source gap or version lag, not `N/A`.

## Steps

| Step | Method | Action | Expected observation |
|---|---|---|---|
| ZHCN-01 | Human | Switch to 简体中文. Open the creative inventory and inspect ChessMod board and wrench item names, descriptions and tooltips. | Available translated strings appear in Chinese without untranslated resource keys, truncation or rendering problems. |
| ZHCN-02 | Human | Place a board, make a piece move and capture, and observe the sound subtitles with subtitles enabled. | ChessMod subtitles use appropriate Chinese entries and remain readable. |
| ZHCN-03 | Human | Link two quantum boards in a supported build, including boards in separate dimensions; inspect the notifications. | The quantum linking messages display correctly, including substituted coordinates and dimension IDs; the action itself still follows QLINK acceptance. |
| ZHCN-04 | Human | Switch back to English and repeat representative interactions. | English returns without stale Chinese labels, literal format placeholders or crashes. |
| ZHCN-05 | GTF candidate | Inspect the packaged `assets/chessmod/lang/zh_cn.json` and compare keys and format placeholders with `en_us.json`. | File parses, required keys are present and format substitutions are compatible; translation fluency and on-screen display remain human checks. |

**Execution rule:** A GTF candidate must be performed manually until a verified branch-specific implementation is available. A complete `PASS` requires every applicable step, including human visual/audible checks. If a branch has not yet delivered the resource, record its version/source gap and do not invent a PASS.
