# Architecture inventory (initial; branch 1.20.1)

This is an orientation document, **not** an approved refactor plan. Check
other branches before generalising these observations.

## Repository layout

- `project/`: ForgeGradle Java mod, `build.gradle`, wrappers, Java sources,
  resources and tests. Build entry point is `project/gradlew`.
- `project/src/main/java/chessmod/common/dom/model/chess/`: chess rules and
  board/piece domain objects (`Board`, `Move`, piece types, etc.).
- `project/src/main/java/chessmod/block/` and `blockentity/`: in-world blocks,
  game state attachment/persistence and interactions.
- `project/src/main/java/chessmod/common/network/`: network messages.
- `project/src/main/java/chessmod/client/`: rendering and GUIs.
- `project/src/main/java/chessmod/setup/`: mod registration/setup.
- `project/src/test/java/chess/TestBoard.java`: existing chess-engine tests.
- `modpack/`, `project/mod/`: distributables already checked into the repo.

## Initial seams worth investigating

The domain model appears substantially separable from loader-specific code;
`Board.java` refers to `ChessMod.LOGGER`, so full independence is not currently
achieved. A logger abstraction or injected callback is one **possible** fix,
not an approved change.

Minecraft integration includes substantial version-sensitive areas (entity and
block registration, data serialisation, networking, rendering and Gradle/
loader configuration). A unified modding API might concentrate that work in
adapters. It also risks becoming harder to maintain than versioned branches;
we should measure actual duplication and migration costs first.

## Unanswered questions

- Which version branches build and run today? Which tests execute on each?
- How do common model semantics and persistence formats vary by branch?
- How much Forge-specific code would a loader-independent boundary need?
- Would resource packaging and Gradle composition work across older targets?
- Which loaders/versions are worth supporting based on users and maintenance?

Capture answers as evidence in issues and/or ADRs. Do **not** infer production
compatibility from matching Java filenames.

## Repository history and artefact policy

Historical release ZIP/JAR bloat is documented in [ADR-0002](decisions/0002-history-cleanup.md).
Its status is **Proposed**; current history and distributables remain unchanged.
