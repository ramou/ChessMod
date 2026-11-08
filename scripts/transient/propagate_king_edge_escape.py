#!/usr/bin/env python3
"""Prepare issue #208's king edge-escape fix on older ChessMod branches.

Read-only by default. --apply creates review worktrees on NEW LOCAL staging
branches; never edits version-branch tips, commits, pushes, or uses a shell.
Run from any checkout that contains local 1.21 and target branch refs.
"""
from __future__ import annotations

import argparse
import os
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

ENGINE = "project/src/main/java/chessmod/common/dom/model/chess/piece/King.java"
TEST = "project/src/test/java/chess/TestKingEdgeEscape.java"
OLD = b"if(getPosition().toX(-2).x == t.x || getPosition().toX(2).x == t.x) { //Castle"
NEW = b"if(getPosition().y == t.y && Math.abs(getPosition().x - t.x) == 2) { //Castle"
OLD_LEFT = b"final Piece leftRook = b.pieceAt(Point.create(getPosition(), -4, 0));"
OLD_RIGHT = b"final Piece rightRook = b.pieceAt(Point.create(getPosition(), 3, 0));"
NEW_LEFT = b"final Point leftRookPoint = Point.create(getPosition(), -4, 0);"
NEW_RIGHT = b"final Point rightRookPoint = Point.create(getPosition(), 3, 0);"
NEW_LEFT_LOOKUP = b"final Piece leftRook = leftRookPoint instanceof Point.InvalidPoint ? null : b.pieceAt(leftRookPoint);"
NEW_RIGHT_LOOKUP = b"final Piece rightRook = rightRookPoint instanceof Point.InvalidPoint ? null : b.pieceAt(rightRookPoint);"
TARGETS = ("1.14.4", "1.15.2", "1.16.5", "1.17.1", "1.18.2", "1.19.4", "1.20.1")


@dataclass(frozen=True)
class Plan:
    version: str
    ref: str
    sha: str
    branch: str
    destination: Path
    engine: bytes


def git(repo: Path, *args: str, data: bytes | None = None) -> bytes:
    result = subprocess.run(
        ("git", "-C", str(repo), *args), input=data, stdout=subprocess.PIPE,
        stderr=subprocess.PIPE, check=False,
    )
    if result.returncode:
        detail = result.stderr.decode("utf-8", errors="replace").strip()
        raise RuntimeError(f"git {' '.join(args)}: {detail}")
    return result.stdout


def has_ref(repo: Path, ref: str) -> bool:
    return subprocess.run(("git", "-C", str(repo), "show-ref", "--verify", "--quiet", ref),
                          stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode == 0


def exists(repo: Path, ref: str, path: str) -> bool:
    return subprocess.run(("git", "-C", str(repo), "cat-file", "-e", f"{ref}:{path}"),
                          stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode == 0


def read_at(repo: Path, ref: str, path: str) -> bytes:
    return git(repo, "show", f"{ref}:{path}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path.cwd(), help="repository root or child")
    parser.add_argument("--source-ref", default="refs/heads/1.21", help="accepted, committed 1.21 source (default: local 1.21 branch)")
    parser.add_argument("--branch", action="append", choices=TARGETS, help="target version; repeat; default: all seven older branches")
    parser.add_argument("--workspace", type=Path, help="review-worktree directory; default: .ai-exports/issue-208-propagation")
    parser.add_argument("--apply", action="store_true", help="create review worktrees on new staging branches; no commits/pushes")
    args = parser.parse_args()
    try:
        repo = Path(os.fsdecode(git(args.repo.resolve(), "rev-parse", "--show-toplevel")).strip()).resolve()
        source = git(repo, "rev-parse", "--verify", f"{args.source_ref}^{{commit}}").decode().strip()
        source_code = read_at(repo, source, ENGINE)
        if (source_code.count(NEW) != 1 or OLD in source_code
                or source_code.count(NEW_LEFT) != 1 or source_code.count(NEW_RIGHT) != 1
                or source_code.count(NEW_LEFT_LOOKUP) != 1 or source_code.count(NEW_RIGHT_LOOKUP) != 1):
            raise ValueError("Source 1.21 King.java does not contain the accepted one-line fix exactly once")
        source_test = read_at(repo, source, TEST)
        if b"blackKingAtB1CanEscapeKnightCheckToA1" not in source_test or source_test.count(b"@Test") < 5:
            raise ValueError("Source 1.21 must have the b1 -> a1 regression and at least four other edge/castling tests")
        versions = args.branch or list(TARGETS)
        if len(set(versions)) != len(versions):
            raise ValueError("Duplicate --branch argument")
        ws = (args.workspace.resolve() if args.workspace else repo / ".ai-exports" / "issue-208-propagation")
        if ws == repo or ws == repo / ".git" or (repo / ".git") in ws.parents:
            raise ValueError("Invalid workspace path")
        if repo in ws.parents:
            git(repo, "check-ignore", "-q", str(ws))  # Never create tracked workspace contents.
        plans = []
        for version in versions:
            local = f"refs/heads/{version}"
            remote = f"refs/remotes/origin/{version}"
            if has_ref(repo, local):
                ref = local
            elif has_ref(repo, remote):
                ref = remote
            else:
                raise ValueError(f"Missing local or origin ref for {version}")
            old_code = read_at(repo, ref, ENGINE)
            if (old_code.count(OLD) != 1 or NEW in old_code
                    or old_code.count(OLD_LEFT) != 1 or old_code.count(OLD_RIGHT) != 1
                    or NEW_LEFT in old_code or NEW_RIGHT in old_code):
                raise ValueError(f"{version}: King.java differs from the known vulnerable implementation: manual adaptation required")
            if exists(repo, ref, TEST):
                raise ValueError(f"{version}: test file already exists; review manually (no overwrite)")
            stage = f"propagate/issue-208-{version}"
            if has_ref(repo, f"refs/heads/{stage}"):
                raise ValueError(f"Staging branch {stage} already exists; refusing overwrite")
            dest = ws / version
            if dest.exists():
                raise ValueError(f"Destination {dest} exists; refusing overwrite")
            sha = git(repo, "rev-parse", ref).decode().strip()
            # Preserve each branch's own CRLF/LF convention and all unrelated source.
            eol = b"\r\n" if b"\r\n" in old_code else b"\n"
            updated = old_code.replace(OLD, NEW, 1)
            updated = updated.replace(OLD_LEFT, NEW_LEFT + eol + b"\t\t\t" + NEW_LEFT_LOOKUP, 1)
            updated = updated.replace(OLD_RIGHT, NEW_RIGHT + eol + b"\t\t\t" + NEW_RIGHT_LOOKUP, 1)
            plans.append(Plan(version, ref, sha, stage, dest, updated))
        print(f"Source 1.21: {source[:12]} | regression tests: {source_test.count(b'@Test')}")
        for p in plans:
            print(f"  {p.version} @ {p.sha[:12]} -> {p.branch} ({p.destination})")
        if not args.apply:
            print("PREFLIGHT OK. Read-only. Re-run with --apply to create review worktrees.")
            return 0
        ws.mkdir(parents=True, exist_ok=True)
        for p in plans:
            git(repo, "worktree", "add", "-b", p.branch, str(p.destination), p.ref)
            target = p.destination / ENGINE
            target.write_bytes(p.engine)
            test = p.destination / TEST
            test.parent.mkdir(parents=True, exist_ok=True)
            test.write_bytes(source_test)
            status = git(p.destination, "status", "--short").decode().strip()
            print(f"\nPREPARED {p.version} at {p.destination}\n{status}")
        print("\nReview each worktree and run its tests/build. Commit and integrate manually; no target branch was moved or pushed.")
        return 0
    except (RuntimeError, ValueError, OSError) as err:
        print(f"ERROR: {err}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
