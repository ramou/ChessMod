# Acceptance run — <date> / <feature ID> / <branch and mod version>

Copy to `docs/acceptance/runs/YYYY-MM-DD-<ID>-<MC>-<tester>.md` once a run has happened; commit with the matrix update. **Do not fill in a PASS before conducting the test.**

- Feature/spec revision: `<ID>`, link to `../<ID>.md` at tested source revision
- Date (UTC or local with zone):
- Tester (`ramou`, etc.) / method (`HUMAN`, `GTF`, or `MIXED`):
- Minecraft runtime version / branch / loader / Forge version:
- ChessMod packaged mod version / full source commit SHA:
- Java (if relevant), OS, launcher, single-player / integrated / dedicated / multiplayer:
- Source of binary (release tag/asset/SHA-256 or local build):
- World/save fixture and preconditions (use a **copy** for destructive cases):
- GameTest implementation ref and exact test IDs (if used):
- Command, number discovered/passed/failed and failing-test gate evidence (if used):

| Step | PASS/FAIL/BLOCKED | Human or GTF | Observation / proof link |
|---|---|---|---|
| `<ID>-01` |  |  |  |

- Overall result: `PASS` / `FAIL` / `BLOCKED` / `PARTIAL` (only PASS if all mandatory steps are covered)
- Bug/issue links, failing observations and reproducible steps:
- Screenshots, logs, video and artefact links (do not include secrets):
- Notes on skipped or unavailable functionality:
- Decision / follow-up owner:
