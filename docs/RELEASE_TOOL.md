# ChessMod branch-aware release manager (issue #205)

Requires **Python 3.9+**, `git`, an authenticated GitHub CLI (`gh`) for online operations, and branch-specific JDKs.

The permanent manager lives at `scripts/release_manager.py`, with its reviewed
registry at `scripts/release_targets.json`. This workflow implements the
**release infrastructure**, not the actual `0.702` release (#209). No command
increments mod versions, changes the release registry, removes checked-in
binaries, or retroactively rewrites Git history.

**Important:** This file describes software ready for maintainer testing. The
multi-JDK Gradle/Forge builds and GitHub publication paths must still be
validated on the maintainer's machine. Do not declare #205 done solely because
the Python unit tests pass.

## Policy and branch model

- Run the manager from the `1.20.1` checkout, where `scripts/` is maintained.
- Read the **committed** version source appropriate to each branch, whether
  `project/build.gradle` or `project/gradle.properties`. `1.18.2` now uses
  `gradle.properties` after its build alignment; never assume all older
  branches share the same version-source format.
- Supply **`--version 0.702`** on release-specific preflights and all
  `verify`, `draft` and `publish` commands. This is the **release version
  suffix**, not a Minecraft-prefixed string. Every selected branch must
  already declare `<minecraft>-0.702` in committed source. The manager never
  changes version files, creates version-bump commits, or overrides a published
  release. If one branch still declares `0.616` or `0.701`, it reports every
  mismatch and refuses to start builds or write anything to GitHub.
- The explicit, reviewed `scripts/release_targets.json` supplies the target
  inventory: branch, Minecraft/loader, Gradle wrapper, launcher JVM, compiler
  JDK, build/test tasks, package metadata and JAR pattern. Default selection
  is **every release-eligible target**; repeat `--target` to restrict.
- Tests and builds use detached Git worktrees at exact, pushed branch source
  commits. No checked-in binary is used as a release asset.
- `0.702` version bumps and approvals are governed by issue #209. Each branch's
  source-version-change commit must reference `Refs #209` (or a matching
  `Fixes`/`Closes` reference). Draft/publish commands require
  `--release-issue 209` and check the live issue, commit messages and declared
  version. Future release issues work the same way.
- Local worktree modifications never enter releases. The source branch tip
  must agree with `origin`; the tool never commits/rebases/force-pushes source.
- `draft --apply` may push annotated Git tags and create **draft-only**
  releases. It never publishes them. `publish --apply` operates on **one**
  explicitly confirmed tag at a time.
- The single, shared update JSON lives on `1.20.1` at
  `project/mod/update.json`. A successful publication is followed by a normal
  metadata commit there, not a rewritten release tag. The expected tag points
  to the tested source commit, intentionally before the metadata commit.

### Draft replacement and historical update feeds

An existing draft/tag is tied to its verified source commit. A subsequent
source change (even metadata only) requires a fresh verification and draft
assets; `draft --apply` will **not** overwrite a tag or draft pointing at a
different SHA. If the release is still unpublished, explicitly inspect and
retire the old draft, remote tag **and local tag** before reusing its version;
never rewrite a published tag. `git ls-remote --tags origin TAG` checks the
remote, while `git tag -l TAG` checks local state.

The publish step updates the canonical feed. It does not automatically fix
URLs embedded in historical JARs. When a previous release points to a legacy
branch-specific `project/mod/update.json`, inspect that branch feed and, when
appropriate, copy the canonical feed there **once** to advertise the migration
release. Verify the JSON version keys against the actual Minecraft versions
(e.g. avoid `1.17.2` keys for `1.17.1`). This is a separate reviewed metadata
commit, not a reason to add permanent mirroring infrastructure.

## JDK selection and preflight

JDKs may be installed in ordinary OS directories, named by `JAVA_HOME` or
`JAVA8_HOME`/`JDK17_HOME`, found via `javac` on PATH, or placed under a custom
parent directory. The tool probes **both** `bin/java -version` and
`bin/javac -version`, checks matching major versions, and refuses JRE-only or
invalid directories. No JDK is downloaded and no global Java settings change.

Examples:

```sh
# Read-only release readiness for version 0.702 across all eight targets:
python3 scripts/release_manager.py preflight --version 0.702 \
  --java-homes-dir "$HOME/jdks"

# JDK/branch inventory without release-version enforcement:
python3 scripts/release_manager.py preflight \
  --java-homes-dir "$HOME/jdks"

# Only check Minecraft 1.21's readiness for 0.702:
python3 scripts/release_manager.py preflight --target 1.21 --version 0.702

# Explicit Java homes, including directories with spaces:
python3 scripts/release_manager.py preflight --version 0.702 \
  --java-home '8=/opt/JDK 8' --java-home '16=/opt/jdk-16' \
  --java-home '17=/opt/jdk-17' --java-home '21=/opt/jdk-21'

# Split Gradle launcher JVM and compiler where the registry allows it:
python3 scripts/release_manager.py preflight --version 0.702 --target 1.16.5 \
  --java-home '1.16.5:launcher=/opt/jdk-8' \
  --java-home '1.16.5:compiler=/opt/another-jdk-8'

# Offline/local-only diagnostic; no GitHub/remote checks:
python3 scripts/release_manager.py preflight --offline --target 1.21 --version 0.702
```

Gradle 4.9 running on Java 21 is **not** equivalent to Java 8 compilation.
The launcher itself must be compatible with the pinned wrapper/plugin.
The tool rejects missing Java requirements for **all selected targets before
any Gradle task or GitHub write**. `verify` also runs a launcher-only `gradlew
--version` probe across every not-already-published target before beginning
the first build.
A downloaded Gradle distribution may populate the normal Gradle cache.


## Existing releases: safe reruns and version conflicts

`preflight --version 0.702` checks the **entire selected branch set** before
any build or GitHub write, including the current pushed source SHAs, declared
versions, remote tags and existing GitHub Releases. Possible outcomes:

| Status | Meaning | Behaviour |
|---|---|---|
| `NEW` | Neither expected release nor tag exists. | Can verify/build, then draft. |
| `TAG_ONLY` | Expected tag already exists at the matching source commit. | Can resume a previously interrupted draft operation; never retag. |
| `DRAFT` | Existing release is a draft at the matching source commit. | Retain the draft; `draft --apply` verifies it against local assets and makes no duplicate. |
| `PUBLISHED_IDENTICAL` | Published tag points at selected branch source commit; published provenance identifies that SHA/version; every release asset matches recorded SHA-256 and `SHA256SUMS`. | `verify` and `draft --apply` **skip** the target. No rebuild, new tag or duplicate release. |

A published release with a conflicting tag SHA, incorrect/missing provenance,
asset mismatch or unexpected content is **not** treated as complete. The
operation stops for manual review. There is deliberately **no force/override
flag**: prefer a new version if the source changed. Never repoint an existing
published tag. If a release exists with a matching SHA but without the required
provenance, its identity cannot be automatically established.

When already published assets are checked, the manager downloads the remote
JAR, launcher ZIP, `PROVENANCE.json` and `SHA256SUMS`, validates their recorded
hashes and internal package structure, and compares with any existing local
verified output. This establishes an **internally consistent release identity**;
it is not a substitute for reproducing the binary bit-for-bit from source or
for actual Minecraft runtime testing.

`publish` still requires a local verified manifest to support its separate
shared-update-feed recovery path; rerunning `publish` on an already published
release verifies the assets and resumes feed bookkeeping rather than
republishing. A JDK remains required for every selected branch even if its
published release will be skipped, so the preflight inventory is consistent.

## Verify committed, pushed releases (no GitHub writes)

After committing a reviewed version bump with `Refs #209` and pushing the
source branch, run:

```sh
python3 scripts/release_manager.py verify --version 0.702 \
  --java-homes-dir "$HOME/jdks" --release-issue 209

# For one target during a shake-down:
python3 scripts/release_manager.py verify --target 1.21 --version 0.702 \
  --release-issue 209
```

`verify` first checks every selected JDK and every Gradle wrapper launcher,
then runs `clean test build` in isolated checkouts. Nonzero executed tests and
zero JUnit failures/errors are mandatory. Missing XML test reports block the
release. It checks the packaged `META-INF/mods.toml`, mod version, JAR filename
and licence presence. The Forge JAR and a **newly generated minimal
Prism/MultiMC import ZIP** go under the ignored
`.ai-exports/releases/<target>/<tag>/` directory, together with:

- `SHA256SUMS`: content hashes of the release JAR and launcher ZIP;
- `PROVENANCE.json`: source SHA, Minecraft/Forge/version, JDK/Gradle, test
  counts, named assets and digests;
- `RELEASE_NOTES.md`: initial draft notes for human review.

A generated launcher ZIP includes the verified JAR, instance configuration,
Minecraft and Forge component versions, and the project's MIT licence. It
does not copy legacy `modpack/` contents or any third-party JAR, including
OptiFine. **Import and test the generated ZIP in PrismLauncher before
publication**; the Python checks do not establish launcher compatibility.

Outputs are immutable from the tool's perspective. To rerun verification,
archive/remove the old target/tag output directory deliberately. Do not edit
files between `verify`, `draft`, and `publish`: the tool recomputes hashes.

## Create and inspect drafts (explicit remote writes)

```sh
python3 scripts/release_manager.py draft --target 1.21 --version 0.702 \
  --release-issue 209 --apply
```

Omit `--target` to prepare the verified set of all eligible targets. Before
any write it validates every selected pending manifest, the source SHAs,
tags, existing drafts and release issue. Already-published identical targets
are skipped without requiring a local output directory. For each pending
target the manager creates an annotated tag at the source SHA,
pushes that tag (not a version branch), then creates the GitHub **draft** with
verified assets. It verifies a previously existing draft's tag and downloads
and hashes every asset for safe idempotent reruns. A tag/draft conflict or
already published release is not overwritten.

Tag convention: `mc<Minecraft-version>-forge-v<mod-version-suffix>`, for example
`mc1.21-forge-v0.702`. Verify the release page, licence, recorded SHA, JUnit
counts, open gameplay issues #199/#207, world/client/server behaviour and all
assets before publication. Some runtime checks require manual evidence.

**Partial draft failure:** a tag push can succeed before draft creation fails.
Do not delete/move the tag. Review the remote tag's SHA and retry the same
draft operation. Existing correct tags are reused. Drafts with missing or
inconsistent assets require manual review rather than automatic overwrites.

## Publish one reviewed draft (explicit human action)

```sh
python3 scripts/release_manager.py publish --target 1.21 --version 0.702 \
  --release-issue 209 --confirm mc1.21-forge-v0.702 --apply
```

The tool requires the exact tag, checks that the tag/source SHA and draft
asset set match local, and **downloads and hashes all draft assets**. Only
then does it mark the accepted GitHub draft public (`--latest=false`). It next
adds/updates that Minecraft version's `latest` and `recommended` promo keys
and changelog entry in the shared update feed at `1.20.1`, preserving other
versions. It commits/pushes that update with an ordinary non-force push. It
does **not** modify download READMEs, auto-publish other drafts, set a
repository-wide Latest release, or modify mod sources or build versions.

**Partial success and recovery:** GitHub publication and the Git update-feed
push cannot be atomic. If publication succeeds but the feed update fails, the
tool prints **PARTIAL SUCCESS**. Inspect the release and Git remote, then
retry the same `publish` command. A published release is never republished;
the tool verifies it and resumes the feed step. A remote feed branch advanced
by a successful metadata commit is explicitly allowed for this recovery path.
If the feed remote tip changes unexpectedly, a normal Git push will reject a
non-fast-forward. No force push, retagging, or automatic rollback.

Because the metadata commit is made in an isolated worktree, your main
`1.20.1` checkout may now trail the remote. Use `git fetch origin` and a
reviewed `git merge --ff-only origin/1.20.1` before further commits. Issue
#206 remains responsible for correcting and validating the Forge update
checker URLs/keys and client behaviour; the tool does not silently alter
`mods.toml` to turn it on.

## Cleanup and explicit exclusions

The binary-removal migration belongs to #209 **after publication and asset
verification**. Use ordinary Git commits and `.gitignore` changes; never
rewrite old Git objects. The manager doesn't install Java, manage historical
releases, download other mods, edit issue bodies, or create release tickets.

Test the tool without Minecraft or GitHub writes:

```sh
python3 -m unittest discover -s scripts/tests -v
```

For the first real release, use a **single target** through version-locked
preflight, verify, draft review and publish; only then expand to all eight.
