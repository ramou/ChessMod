# 1.18.2 development and Patch A acceptance

This branch is a **ForgeGradle 5.1 / Gradle 7.4 / Java 17** project.
Patch A deliberately keeps Forge `40.1.0` and Parchment `2022.05.22-1.18.2`;
it does not migrate to ForgeGradle 6 or change runtime gameplay.

## First-time or repaired IDE import

1. From the repository root, run `source scripts/env.sh 1.18.2` in a Bash-compatible
   terminal if the branch-aware environment helper (#213) is installed.
   Otherwise set `JAVA_HOME` to a JDK 17 manually.
2. In `project/`, run `./gradlew --version`, then `./gradlew clean build`
   and `./gradlew genIntellijRuns` if that task is available in this ForgeGradle build.
3. In IntelliJ IDEA, **open `project/` as a Gradle project**, not the repository
   root as an existing IntelliJ project. Configure **Gradle JVM = JDK 17**
   and let Gradle sync.
4. Prefer Gradle's `runClient` task from the `project/` build when checking whether
   generated IDE run configurations are actually necessary.
5. If `.idea` metadata interferes with import, test in a fresh clone/worktree
   or temporarily move the existing top-level `.idea` directory outside the
   repository. The tracked historical `.idea` files are not changed by Patch A.
   Do not commit IDE-generated workspace files.

## Patch A acceptance checks

From `project/` (with Java 17 active):

```sh
./gradlew --version
./gradlew clean test build
./gradlew runClient
```

Check `build/resources/main/META-INF/mods.toml` after `processResources`:
all `${...}` placeholders must be expanded, with version `1.18.2-0.616`,
Forge range `[40,)`, Minecraft range `[1.18.2,1.19)`, and the
update JSON URL pointing to canonical `1.20.1`.

In-game, select Simplified Chinese and confirm that the five existing
board names, wrench, sounds, and advancement text render properly.
Quantum board text is intentionally excluded until Patch B.

This patch does **not** update version/release history or claim a completed
IntelliJ acceptance check; the maintainer performs those checks before Patch B.
