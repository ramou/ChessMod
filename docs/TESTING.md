# Testing and validation baseline

This document describes our **verification contract**, not a claim of successful
builds. Each patch must distinguish checks performed by the AI environment
from checks performed by the maintainer.

## Initial known inventory

- The Java project lives in `project/`; CI in `.circleci/config.yml` contains
  Gradle dependency resolution and a `./gradlew test` step for Java 17.
- `project/src/test/java/chess/TestBoard.java` contains existing JUnit tests.
- Minecraft/Forge execution requires matching Java, Gradle and Minecraft
  dependencies; do not assume they are installed in an AI environment.
- We have not established a green baseline across the supported versions.

## Suggested verification sequence

1. Run fast, dependency-free checks for scripts and documentation when relevant:
   `python -m unittest discover -s scripts/tests -v`.
2. For Java changes, identify target branch and toolchain, then run in
   `project/` the relevant `./gradlew test` and/or `./gradlew build` when feasible.
3. Test the changed behaviour in Minecraft (client, server, multiplayer as
   applicable) on the maintainer's machine when automated execution cannot
   establish game behaviour.
4. Include error output, environment details and reproducible steps in review
   feedback, then iterate.

If a command was not executed, say **not executed**, not **passed**. A passing
unit test does not establish that a mod loads in the game.

## Proposed future improvements (not approved work)

- Identify truly standalone chess rules and expand regression tests for bugs.
- Add input/serialization fixtures that survive branch or loader migration.
- Track a branch-by-branch build matrix with toolchain and known failures.
- Consider local test harness scripts for repeatable maintainer validation.

Each requires its own agreed issue/scope before implementation.
