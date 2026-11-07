#!/usr/bin/env python3
"""Prepare reviewed, uncommitted castling-fix changes on other Minecraft branches.

Run --help. Dry-run is the default. Requires an accepted source commit on 1.20.1
and a normal checkout with all target branch refs available locally. Nothing is
pushed or committed; --apply creates local review worktrees under .ai-exports/.
"""
import argparse
import os
import subprocess
import sys
import tempfile
from pathlib import Path

SOURCE_FILES = (
    "project/src/main/java/chessmod/common/dom/model/chess/board/Board.java",
    "project/src/main/java/chessmod/common/dom/model/chess/piece/King.java",
)
TEST_FILE = "project/src/test/java/chess/TestCastlingSafety.java"
PATCH_FILES = (*SOURCE_FILES, TEST_FILE)
DEFAULT_BRANCHES = (
    "1.14.4", "1.15.2", "1.16.5", "1.17.1", "1.18.2", "1.19.4", "1.21"
)


def git(repo, *args, input_bytes=None, env=None):
    result = subprocess.run(
        ["git", "-C", str(repo), *args], input=input_bytes,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=env, check=False,
    )
    if result.returncode:
        raise RuntimeError(f"git {' '.join(args)} failed: {result.stderr.decode(errors='replace').strip()}")
    return result.stdout


def exists_in_ref(repo, ref, path):
    result = subprocess.run(
        ["git", "-C", str(repo), "cat-file", "-e", f"{ref}:{path}"],
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False,
    )
    return result.returncode == 0


def main():
    parser = argparse.ArgumentParser(
        description="Prepare the approved castling fix on other Minecraft branches without commits or pushes.",
        epilog="Default mode is a no-write preflight. Use --apply after reviewing its plan. Worktrees are left for human review and commits.",
    )
    parser.add_argument("--repo", type=Path, default=Path.cwd(), help="Git repository (default: current directory)")
    parser.add_argument("--source-commit", required=True, help="Accepted 1.20.1 commit containing the castling fix")
    parser.add_argument("--branch", action="append", dest="branches", help="Target version branch; repeat for several (default: all seven other branches)")
    parser.add_argument("--workspace", type=Path, help="New review-worktree directory (default: .ai-exports/castling-propagation)")
    parser.add_argument("--apply", action="store_true", help="Create review worktrees and apply changes (never commits or pushes)")
    args = parser.parse_args()

    try:
        repo = Path(os.fsdecode(git(args.repo, "rev-parse", "--show-toplevel")).strip()).resolve()
        source = git(repo, "rev-parse", "--verify", f"{args.source_commit}^{{commit}}").decode().strip()
        parents = git(repo, "rev-list", "--parents", "-n", "1", source).decode().split()
        if len(parents) != 2:
            raise ValueError("Source must be a single-parent commit (not a merge or root commit)")
        parent = parents[1]
        changed = set(git(repo, "diff", "--name-only", parent, source, "--", *PATCH_FILES).decode().splitlines())
        if changed != set(PATCH_FILES):
            raise ValueError(f"Source commit must modify both engine files and add regression tests: found {sorted(changed)}")
        if exists_in_ref(repo, parent, TEST_FILE):
            raise ValueError("Regression-test file already existed in source parent; expected a newly added test")
        patch = git(repo, "diff", "--binary", parent, source, "--", *PATCH_FILES)
        if not patch:
            raise ValueError("Empty castling patch")

        branches = args.branches or list(DEFAULT_BRANCHES)
        if len(branches) != len(set(branches)) or any(b not in DEFAULT_BRANCHES for b in branches):
            raise ValueError(f"Select distinct supported branches from {', '.join(DEFAULT_BRANCHES)}")
        workspace = (args.workspace or repo / ".ai-exports" / "castling-propagation").resolve()
        if workspace == repo:
            raise ValueError("Workspace cannot be the repository root")
        if repo in workspace.parents:
            # In-repo worktrees must live in an ignored directory.
            git(repo, "check-ignore", "-q", str(workspace))
        plans = []
        for branch in branches:
            local_ref = f"refs/heads/{branch}"
            remote_ref = f"refs/remotes/origin/{branch}"
            try:
                git(repo, "show-ref", "--verify", "--quiet", local_ref)
                ref, create_local = local_ref, False
            except RuntimeError:
                git(repo, "show-ref", "--verify", "--quiet", remote_ref)
                ref, create_local = remote_ref, True
            for path in SOURCE_FILES:
                if git(repo, "show", f"{ref}:{path}") != git(repo, "show", f"{parent}:{path}"):
                    raise ValueError(f"{branch}: {path} differs from the original source; adapt manually")
            if exists_in_ref(repo, ref, TEST_FILE):
                raise ValueError(f"{branch}: {TEST_FILE} already exists; adapt manually")
            # Verify each target without checking out files or changing its real index.
            with tempfile.TemporaryDirectory(prefix="chessmod-index-") as tmp:
                alternate = os.environ.copy()
                alternate["GIT_INDEX_FILE"] = str(Path(tmp) / "index")
                git(repo, "read-tree", ref, env=alternate)
                git(repo, "apply", "--cached", "--check", "-", input_bytes=patch, env=alternate)
            dest = workspace / branch
            if dest.exists():
                raise ValueError(f"Refusing existing worktree destination: {dest}")
            plans.append((branch, ref, create_local, dest))

        print(f"Source: {source}\nParent: {parent}\nFiles: {', '.join(PATCH_FILES)}")
        for branch, ref, _, dest in plans:
            print(f"Verified {branch} ({git(repo, 'rev-parse', '--short', ref).decode().strip()}): {dest}")
            if branch == "1.21":
                print("  WARNING: 1.21 lacks JUnit dependencies/test configuration; test compilation needs separate attention.")
        if not args.apply:
            print("Dry run complete; no worktrees, commits or branch changes. Rerun with --apply after review.")
            return 0

        workspace.mkdir(parents=True, exist_ok=True)
        for branch, ref, create_local, dest in plans:
            if create_local:
                git(repo, "worktree", "add", "--track", "-b", branch, str(dest), ref)
            else:
                git(repo, "worktree", "add", str(dest), branch)
            git(dest, "apply", "--check", "-", input_bytes=patch)
            git(dest, "apply", "-", input_bytes=patch)
            print(f"APPLIED {branch}: review {dest}, test, then commit manually")
        print("No commits or pushes were performed. Do not delete review worktrees before accepting their changes.")
        return 0
    except (RuntimeError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
