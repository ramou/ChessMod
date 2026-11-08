# Publishing ChessMod with GitHub Releases

**Status: documented procedure; no release migration has been executed by this
chore.** Publishing a release, pushing a tag, removing checked-in binaries or
changing public download links requires separate maintainer approval. See
[ADR-0002](decisions/0002-history-cleanup.md) for the distinct question of
whether old Git history should ever be rewritten.

The permanent branch-aware release manager is specified in
[RELEASE_TOOL.md](RELEASE_TOOL.md) (issue #205). Release preparation uses an
explicit requested version such as `--version 0.702`: it validates the
committed version across every selected Minecraft branch (all eligible
branches by default), reports already-published identical releases as no-ops,
and blocks conflicting tags/assets. It never auto-bumps or overwrites a
published release. It is deliberately separate
from the human-approved 0.702 release and binary-removal work (issue #209).
The legacy `gh` commands below are illustrative, not an instruction to
bypass its Java, test, provenance or release-issue safeguards.

## Model: branch, commit, tag, release and asset

- A **branch** (for example `1.20.1`) is the ongoing Minecraft-version line.
  Its head moves as development continues.
- A **commit SHA** identifies one precise source state. Release only after the
  target branch's changes have been accepted, committed and pushed.
- A **Git tag** names that commit. GitHub Releases attach to **tags**, not to
  movable branches. Prefer an *annotated* tag at the exact intended SHA, push
  it, and keep published release tags unchanged.
- A **GitHub Release** adds a title, notes, pre-release/draft state and
  downloadable **assets** to that tag. Assets are uploaded files, not Git
  objects: upload the compiled JAR and modpack ZIP without committing either.
- GitHub also offers automatically generated **source code** ZIP/tar.gz links
  at the tag. These are not Minecraft-installable mod binaries.

**The tag does not prove that uploaded assets were built from that commit.**
The maintainer must validate provenance: record the full SHA, Minecraft
version, loader, build toolchain, and file checksums in release notes.

Git tags are repository-wide. Proposed naming convention:
`mc1.20.1-forge-v0.701` (Minecraft version, loader, ChessMod version). This
avoids conflicts if a second loader later ships the same ChessMod version.
Versions shown here illustrate the format; do not assume that 0.701 is ready
for publication. Choose consistent asset names that identify version/loader.
Existing historical releases/tags, if any, should be inventoried before
adopting the convention; do not move them or rename them casually.

A repository has **one** `Latest` release designation, not separate latest
releases per Minecraft branch. Decide which release to mark latest; use
`--latest=false` for other supported versions, and keep the compatibility
matrix in release notes or the public README. Use **pre-release** for testing
builds, **draft** for review, and published releases for user downloads.

## What the maintainer will actually do

1. Identify the supported Minecraft version, loader, ChessMod version and
   intended release commit. Confirm the commit is on the expected branch and
   has been pushed. No uncommitted changes may influence the release build
   without being explicitly recorded and approved.
2. Build and test the mod **from the exact committed source**. Validate the
   JAR in Minecraft and the modpack in its target launcher; automated tests
   alone are not an installation test. Check the contents and licences of all
   distributed dependencies. **Do not bundle OptiFine or other third-party
   binaries without appropriate redistribution permission.**
3. Name the release files unambiguously; collect SHA-256 checksums and
   record the full commit SHA, game version, loader, build/runtime versions,
   noteworthy fixes, known issues and installation instructions.
4. Create and push an annotated Git tag at that SHA (once); create a **draft
   release** linked to the existing tag; upload the reviewed assets and notes.
   Do not allow the CLI to silently create a tag from the default branch.
5. Inspect the draft release on GitHub: correct tag/SHA, notes, downloads,
   filenames, hashes and absence of unlicensed bundled dependencies. Publish
   only after human approval. If the repository enforces immutable releases,
   asset changes must be complete **before** publishing.
6. Download the published asset and verify its checksum; confirm installation
   instructions and links. Only then update the public README/download links,
   optionally remove currently tracked binaries in a **separate normal
   commit**, and adjust `.gitignore` to keep future generated releases out.
   Removing files in a new commit **does not reduce existing Git history**.

A release can contain several assets, for example a stand-alone mod JAR and
an importable modpack ZIP, with different labels. GitHub release assets have a
2 GiB per-file limit, more than sufficient for the current ChessMod files.

## Web interface (for an initial/manual release)

1. Open the repository on GitHub → **Releases** → **Draft a new release**.
2. Choose an existing tag, or create a tag **against the intended branch**.
   An exact pre-created tag is safer than relying on the default branch.
3. Enter title and notes, attach binary files in the upload area, and save
   as a draft. Check the tag and assets before clicking **Publish release**.
4. Share the release page or the per-asset links with users. The auto-generated
   source ZIP is not a substitute for the JAR/modpack assets.

## Git + `gh` CLI example (illustrative, not `run.sh`)

These commands demonstrate the mechanics; replace example values and paths
with the **reviewed** release. Never run commands copied from this document
as a blind release script. The remote and `gh` authentication must point to
`ramou/ChessMod`. In a real, AI-authored publication plan, use an ignored root
`run.sh`, have the maintainer review and invoke `bash run.sh`, then **manually
remove** the file, as with [issue writes](ISSUE_MANAGEMENT.md).

```bash
# Confirm correct repo, existing tags/releases and authentication.
gh auth status
gh repo view ramou/ChessMod
gh release list --repo ramou/ChessMod
git fetch origin 1.20.1 --tags

tag='mc1.20.1-forge-v0.701'
commit="$(git rev-parse 'origin/1.20.1^{commit}')"
printf 'Selected commit: %s\n' "$commit"
git show --no-patch --format=fuller "$commit"

# Once reviewed: create + push an annotated, immutable-in-practice tag.
# Abort if a tag of this name already exists: investigate rather than move it.
if git rev-parse -q --verify "refs/tags/$tag" >/dev/null; then
  echo "Tag already exists; inspect before proceeding" >&2
  exit 1
fi
git tag -a "$tag" "$commit" -m "ChessMod 0.701 / MC 1.20.1 / Forge"
git push origin "refs/tags/$tag"

# Record the hashes for the actual files you built and verified.
sha256sum ./dist/chessmod-1.20.1-0.701.jar \
          ./dist/ChessMod-1.20.1-0.701.zip > ./dist/SHA256SUMS

# Draft first; --verify-tag refuses an absent remote tag.
gh release create "$tag" \
  ./dist/chessmod-1.20.1-0.701.jar \
  ./dist/ChessMod-1.20.1-0.701.zip \
  ./dist/SHA256SUMS \
  --repo ramou/ChessMod --verify-tag --draft --latest=false \
  --title 'ChessMod 0.701 for Minecraft 1.20.1 (Forge)' \
  --notes-file ./dist/RELEASE_NOTES.md

# Review online; publish *only* following explicit acceptance.
gh release view "$tag" --repo ramou/ChessMod --web
# gh release edit "$tag" --repo ramou/ChessMod --draft=false
```

`gh release create` can also create a tag if it does not exist, using
`--target <branch-or-full-SHA>` to override the default branch. We **prefer
explicit tags plus `--verify-tag`** because the tag and its SHA are independently
reviewable before release creation. For future releases, do not overwrite an
existing tag or silently replace published assets; issue a new version/tag.

The example shows `./dist` as a local staging directory, **not** the current
ChessMod build output directory. Adjust paths after inspecting the actual
branch's build system; avoid assuming every branch produces the same outputs.
Review the script for side effects: tag pushes and publication are GitHub
writes and are **not** authorised by this document alone.

## Migration checklist (separate, explicitly approved work)

- [ ] Inventory current branch-tip distributables, any existing GitHub
      tags/releases, and all public download links.
- [ ] Decide support targets, release version/tag convention, and which build
      is the repository-wide `Latest` release.
- [ ] Establish reproducible build/test steps and accepted release assets for
      the chosen branch; verify permissions for bundled dependencies.
- [ ] Create a draft release and validate it end-to-end, then publish.
- [ ] Update root README and other distribution links to the release page or
      appropriate assets; distinguish JAR from importable modpack.
- [ ] Separately remove tracked distribution binaries (if approved), prevent
      accidental recommits, and verify that the existing historical links still
      behave as expected. No force-push or history rewrite.
- [ ] Consider CI-based release automation only after manual publishing is
      well understood and explicitly authorised.

## References

- [GitHub: About releases](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases)
- [GitHub: Managing releases](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)
- [GitHub CLI: `gh release create`](https://cli.github.com/manual/gh_release_create)
- [GitHub CLI: `gh release edit`](https://cli.github.com/manual/gh_release_edit)
