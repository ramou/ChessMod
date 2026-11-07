#!/usr/bin/env python3
"""Run a disposable Git history-cleanup experiment. NEVER modifies/pushes source."""

from __future__ import annotations

import argparse
from datetime import datetime, timezone
import fnmatch
import json
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parent.parent
# These paths are distributable outputs, NOT source or Gradle wrapper dependencies.
# Preserve current branch-tip copies by restoring them on new tip commits.
RELEASE_PATTERNS = ("modpack/*.zip", "project/mod/*.jar", "forge-*/mod/*.jar")


class ExperimentError(Exception):
    pass


def git(*args: str, cwd: Path | None = None, capture: bool = True) -> str:
    command = ["git", *( ["-C", str(cwd)] if cwd else []), *args]
    completed = subprocess.run(command, capture_output=capture, check=False)
    if completed.returncode:
        message = completed.stderr.decode(errors="replace") if capture else ""
        raise ExperimentError(f"git {' '.join(args[:3])} failed: {message.strip()}")
    return completed.stdout.decode() if capture else ""


def branch_refs(bundle: Path) -> dict[str, str]:
    heads = {}
    for entry in git("bundle", "list-heads", str(bundle)).splitlines():
        parts = entry.split()
        if len(parts) == 2 and parts[1].startswith("refs/heads/"):
            name = parts[1][len("refs/heads/"):]
            if name in heads:
                raise ExperimentError(f"Duplicate branch {name}")
            heads[name] = parts[0]
    if not heads:
        raise ExperimentError("The bundle has no exported refs/heads branches")
    return heads


def git_tree(repo: Path, ref: str) -> dict[str, tuple[str, str]]:
    raw = subprocess.run(["git", "-C", str(repo), "ls-tree", "-rz", ref],
                         capture_output=True, check=True).stdout
    result = {}
    for entry in raw.split(b"\0"):
        if not entry:
            continue
        header, name = entry.split(b"\t", 1)
        mode, kind, oid = header.decode("ascii").split()
        if kind != "blob":
            raise ExperimentError(f"Unsupported tree entry type: {kind}")
        result[name.decode("utf-8", "surrogateescape")] = (mode, oid)
    return result


def selected(path: str) -> bool:
    return any(fnmatch.fnmatchcase(path, pattern) for pattern in RELEASE_PATTERNS)


def data_for_release(repo: Path, branch: str, mapping: dict[str, tuple[str, str]]) -> dict[str, tuple[str, str, bytes]]:
    result = {}
    for path, (mode, oid) in mapping.items():
        if not selected(path):
            continue
        if mode not in ("100644", "100755") or Path(path).is_absolute() or ".." in Path(path).parts:
            raise ExperimentError(f"Unsafe path or unsupported mode in {branch}: {path}")
        result[path] = (mode, oid, subprocess.check_output(
            ["git", "-C", str(repo), "cat-file", "blob", oid]))
    return result


def restore_files(repo: Path, branch: str, files: dict[str, tuple[str, str, bytes]]) -> None:
    git("switch", "--force", branch, cwd=repo)
    for name, (mode, oid, content) in files.items():
        path = repo / name
        path.parent.mkdir(parents=True, exist_ok=True)
        if path.is_symlink():
            raise ExperimentError(f"Refusing symlink at expected release path: {name}")
        path.write_bytes(content)
        path.chmod(0o755 if mode == "100755" else 0o644)
        git("add", "--", name, cwd=repo)
        if git("hash-object", "--", name, cwd=repo).strip() != oid:
            raise ExperimentError(f"Restored bytes differ: {name}")
    if subprocess.run(["git", "-C", str(repo), "diff", "--cached", "--quiet"]).returncode == 1:
        git("-c", "user.name=ChessMod history experiment",
            "-c", "user.email=history-experiment@example.invalid",
            "commit", "--quiet", "-m",
            f"Restore current {branch} release artifacts after experimental history pruning",
            cwd=repo)


def run(bundle: Path, output: Path, max_bundle_mb: int) -> tuple[Path, Path]:
    refs = branch_refs(bundle)
    if shutil.which("git-filter-repo") is None and subprocess.run(
            ["git", "filter-repo", "--version"], capture_output=True).returncode:
        raise ExperimentError("git-filter-repo is required to run the experiment; install it first")
    output.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    target_bundle = output / f"ChessMod-history-experiment-{stamp}.bundle"
    target_report = output / f"ChessMod-history-experiment-{stamp}.json"
    if target_bundle.exists() or target_report.exists():
        raise ExperimentError("Refusing to overwrite existing experimental artefacts")
    with tempfile.TemporaryDirectory(prefix="chessmod-history-experiment-") as scratch:
        scratch_path = Path(scratch)
        repo = scratch_path / "clone"
        git("clone", "--quiet", "--no-local", str(bundle), str(repo))
        original_checkout = git("symbolic-ref", "--quiet", "--short", "HEAD", cwd=repo).strip()
        # Full bundle refs may arrive as remote-tracking rather than local branches.
        for name, sha in refs.items():
            git("update-ref", f"refs/heads/{name}", sha, cwd=repo)
        before = {name: git_tree(repo, sha) for name, sha in refs.items()}
        release_files = {name: data_for_release(repo, name, mapping)
                         for name, mapping in before.items()}
        before_count = int(git("rev-list", "--all", "--count", cwd=repo).strip())
        # The ONLY force flag here acts on an isolated temporary clone.
        # No history changes, pushes, or ref updates touch the user's repository.
        command = ["filter-repo", "--force", "--invert-paths"]
        for pattern in RELEASE_PATTERNS:
            command += ["--path-glob", pattern]
        git(*command, cwd=repo)
        for name in sorted(refs):
            if not git("rev-parse", "--verify", f"refs/heads/{name}", cwd=repo).strip():
                raise ExperimentError(f"Missing rewritten branch {name}")
            restore_files(repo, name, release_files[name])
            after = git_tree(repo, name)
            if before[name] != after:
                missing = sorted(before[name].keys() ^ after.keys())
                different = sorted(k for k in before[name].keys() & after.keys()
                                   if before[name][k] != after[k])
                raise ExperimentError(
                    f"HEAD tree mismatch after rewrite for {name}: missing={missing[:5]} "
                    f"changed={different[:5]}")
        # Preserve the source bundle's default checkout branch as HEAD.
        if original_checkout not in refs:
            raise ExperimentError(f"Default checkout branch {original_checkout} absent from bundle")
        git("switch", "--force", original_checkout, cwd=repo)
        # Create a self-contained bundle and verify in this isolated repo.
        candidate = scratch_path / "candidate.bundle"
        git("bundle", "create", str(candidate), "--all", cwd=repo)
        git("bundle", "verify", str(candidate), cwd=repo)
        if candidate.stat().st_size > max_bundle_mb * 1024 * 1024:
            raise ExperimentError(f"Candidate bundle exceeds {max_bundle_mb} MiB limit")
        report = {
            "status": "EXPERIMENT_ONLY_NOT_APPROVED_FOR_PUBLICATION",
            "original_bundle": str(bundle),
            "original_bundle_bytes": bundle.stat().st_size,
            "candidate_bundle_bytes": candidate.stat().st_size,
            "release_patterns": RELEASE_PATTERNS,
            "commits_before": before_count,
            "commits_after": int(git("rev-list", "--all", "--count", cwd=repo).strip()),
            "all_branch_tip_trees_identical": True,
            "default_checkout_branch": original_checkout,
            "branches": [{
                "branch": name, "before": sha,
                "after": git("rev-parse", name, cwd=repo).strip(),
                "preserved_release_files": sorted(release_files[name]),
            } for name, sha in sorted(refs.items())],
            "warning": "Commit SHAs differ; external clones, PRs, forks and GitHub links are not repaired.",
        }
        # Output is only copied after ALL branch trees have passed byte-level checks.
        shutil.copy2(candidate, target_bundle)
        try:
            target_report.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
        except Exception:
            target_bundle.unlink(missing_ok=True)
            raise
    return target_bundle, target_report


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description="Experimentally prune historical release binaries in a TEMP clone of a Git bundle; NEVER push.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""Example:
  python scripts/experiment_history_cleanup.py \\
      --bundle .ai-exports/ChessMod-context-multi-8-....bundle

Requires git and git-filter-repo installed. Only writes final experimental bundle
and report to --output-dir (default: .ai-exports/). Safe to discard results.
Preserves complete file contents at every selected branch HEAD; rewrites hashes.
This is a decision experiment, NOT a procedure for force-pushing GitHub.
""",
    )
    parser.add_argument("--bundle", type=Path, required=True, help="Input full Git bundle (read-only)")
    parser.add_argument("--output-dir", type=Path, default=ROOT / ".ai-exports", help="Directory for candidate bundle and report")
    parser.add_argument("--max-bundle-mb", type=int, default=256, metavar="N", help="Maximum candidate bundle MiB (default 256)")
    parser.add_argument("--dry-run", action="store_true", help="Show selected branches and rewrite patterns; do not clone/filter/write")
    args = parser.parse_args(argv)
    if args.max_bundle_mb <= 0:
        parser.error("--max-bundle-mb must be positive")
    bundle = args.bundle.expanduser().resolve()
    if not bundle.is_file():
        parser.error(f"Input bundle does not exist: {bundle}")
    try:
        refs = branch_refs(bundle)
        print("Candidate branch tips:")
        for name, sha in sorted(refs.items()):
            print(f"  {name}: {sha[:12]}")
        print("History removal patterns:", ", ".join(RELEASE_PATTERNS))
        print("Current branch tip release files will be restored; Gradle wrapper jars are excluded.")
        if args.dry_run:
            return 0
        candidate, report = run(bundle, args.output_dir.expanduser().resolve(), args.max_bundle_mb)
    except (ExperimentError, OSError, subprocess.SubprocessError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1
    print("Candidate experimental bundle:", candidate)
    print("Verification report:", report)
    return 0


if __name__ == "__main__":
    sys.exit(main())
