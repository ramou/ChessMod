# Local Java environments (Bash / Linux / WSL2)

Each supported Minecraft version branch tracks the same small `scripts/env.sh`.
The **single** implementation (`scripts/env_core.sh`) and Java registry
(`scripts/release_targets.json`) are maintained on branch `1.20.1`.

On every invocation, `scripts/env.sh` reads those files from the **committed local**
`1.20.1` Git branch into ignored `scripts/transient/` and sources the implementation.
The implementation refreshes `scripts/transient/app.conf` from this checkout's
ignored `.conf/app.conf` and reads JDK paths from the transient copy.
No network, additional worktree or Git working-tree changes to tracked files are
required. The local `1.20.1` branch must exist and must contain the committed
implementation: `git fetch` alone may only update `origin/1.20.1`, not your
local branch. To inspect the canonical version, run
`git show 1.20.1:scripts/env_core.sh`.

One-time machine setup (once per checkout):

```bash
cp .conf/app.conf.example .conf/app.conf
# Edit .conf/app.conf to point to your installed JDKs.
```

After switching branches (or from the `project/` directory):

```bash
source scripts/env.sh        # from repo root; picks checked-out version
# alternatively, from project/: source ../scripts/env.sh
java -version && javac -version
bash project/gradlew -p project clean build --console=plain
```

Explicit target is useful on detached HEADs or when choosing another version:

```bash
source scripts/env.sh 1.19.4   # Java 17, even without checking out 1.19.4
```

`source` is essential: `bash scripts/env.sh` cannot change the parent shell's
`JAVA_HOME` or `PATH`. Re-sourcing validates both executables and their versions
before changing the environment, and removes the previously selected Java bin
entry. On failure the active Java environment stays unchanged. The example JDK
paths are **not** portable requirements. `.conf/app.conf` is ignored and sourced
as trusted shell code; never copy it from an untrusted archive. The ignored
`scripts/transient/` directory is refreshed each time and must never be committed.

If `1.20.1` is missing, or the implementation hasn't been committed yet, the
bootstrap will report the problem. Apply and **commit the canonical 1.20.1 patch
first**, then apply the per-branch bootstrap patches. `scripts/release_manager.py`
remains independent of local configuration. This environment helper is for
interactive development, not a release/acceptance test.

This helper supports Bash on Linux/WSL2; not CMD/PowerShell. Java/Gradle wrapper
smoke tests must still be run with the installed JDKs.
