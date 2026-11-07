#!/usr/bin/env python3
"""Produce a standalone Git bundle and optional GitHub issue snapshot for AI handoff."""

from __future__ import annotations

import argparse
from datetime import datetime, timezone
from pathlib import Path
import re
import subprocess
import sys
import tempfile

import export_worktree

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_OUTPUT_DIR = ROOT / ".ai-exports"


class ContextError(Exception):
    pass


def git(*arguments: str, capture: bool = True) -> str:
    command = ["git", "-C", str(ROOT), *arguments]
    result = subprocess.run(command, text=True, capture_output=capture, check=False)
    if result.returncode:
        raise ContextError(f"git {' '.join(arguments)} failed: {result.stderr.strip()}")
    return result.stdout.strip() if capture else ""


def slug(value: str, limit: int = 54) -> str:
    return re.sub(r"[^a-zA-Z0-9._-]+", "-", value).strip("-.")[:limit] or "branch"


def branch_refs(branches: list[str], all_branches: bool) -> list[str]:
    all_refs = git("for-each-ref", "--format=%(refname)", "refs/heads", "refs/remotes").splitlines()
    all_refs = [ref for ref in all_refs if not ref.endswith("/HEAD")]
    if all_branches:
        if not all_refs:
            raise ContextError("No branches found")
        return sorted(set(all_refs))
    if not branches:
        current = git("symbolic-ref", "--quiet", "--short", "HEAD")
        branches = [current]
    resolved = []
    for branch in branches:
        candidates = (branch, f"refs/heads/{branch}", f"refs/remotes/{branch}",
                      f"refs/remotes/origin/{branch}")
        found = next((ref for ref in candidates if ref in all_refs), None)
        if not found:
            raise ContextError(f"Unknown local/remote branch {branch!r}. Available: "
                               + ", ".join(all_refs))
        if found not in resolved:
            resolved.append(found)
    return resolved



def portable_branches(refs: list[str]) -> list[tuple[str, str, str]]:
    """Map selected local/remote refs to unique, clone-friendly heads in a bundle."""
    result: list[tuple[str, str, str]] = []
    names: dict[str, str] = {}
    # Prefer local branch names if a local and remote ref share a name.
    ordered = sorted(refs, key=lambda r: (not r.startswith("refs/heads/"), r))
    for ref in ordered:
        sha = git("rev-parse", f"{ref}^{{commit}}")
        if ref.startswith("refs/heads/"):
            candidate = ref.removeprefix("refs/heads/")
            remote = "local"
        else:
            parts = ref.removeprefix("refs/remotes/").split("/", 1)
            remote, candidate = parts
        if candidate in names:
            if names[candidate] == sha:
                continue  # Duplicate branch tip, no need for two identical heads.
            candidate = f"{remote}/{candidate}"
        if candidate in names and names[candidate] != sha:
            raise ContextError(f"Cannot assign unique bundle branch name for {ref}")
        names[candidate] = sha
        result.append((candidate, sha, ref))
    return result


def create_portable_bundle(path: Path, heads: list[tuple[str, str, str]]) -> None:
    """Make a cloneable bundle without altering the user's local Git refs.

    A remote-tracking ref alone is not cloneable as a branch from a Git bundle.
    A temporary bare repo with object alternates lets us publish selected tips
    as ordinary refs/heads/* and a HEAD, without rewriting history or the repo.
    Git bundle packs the full reachable objects into a self-contained file.
    """
    with tempfile.TemporaryDirectory(prefix="chessmod-context-") as temp:
        bare = Path(temp) / "bundle.git"
        object_path = git("rev-parse", "--path-format=absolute", "--git-path", "objects")
        commands = [["git", "init", "--bare", "--quiet", str(bare)]]
        for command in commands:
            subprocess.run(command, check=True, capture_output=True, text=True)
        (bare / "objects" / "info" / "alternates").write_text(object_path + "\n")
        for name, sha, _ in heads:
            subprocess.run(["git", f"--git-dir={bare}", "update-ref",
                            f"refs/heads/{name}", sha], check=True, capture_output=True, text=True)
        subprocess.run(["git", f"--git-dir={bare}", "symbolic-ref", "HEAD",
                        f"refs/heads/{heads[0][0]}"], check=True, capture_output=True, text=True)
        subprocess.run(["git", f"--git-dir={bare}", "bundle", "create",
                        str(path), "--branches", "HEAD"], check=True,
                       capture_output=True, text=True)


def make_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Export committed Git history, optional GitHub issues, and separate uncommitted working state.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""Examples:
  python scripts/build_ai_context.py
  python scripts/build_ai_context.py --with-issues
  python scripts/build_ai_context.py --branch 1.20.1 --branch 1.21 --with-issues --issue-label bug
  python scripts/build_ai_context.py --all-branches --with-issues --issue-state open
  python scripts/build_ai_context.py --with-issues --issue-number 13 --issue-number 84
  python scripts/build_ai_context.py --worktree always --with-issues
  python scripts/build_ai_context.py --stash-ref 'stash@{0}'
  python scripts/build_ai_context.py --all-stash-contents
  python scripts/build_ai_context.py --allow-dirty
  python scripts/build_ai_context.py --dry-run

All bundles contain COMPLETE committed history for selected refs, while a separate
-worktree.zip preserves uncommitted changes against HEAD (and inventories stashes).
Git ignore rules from the current working tree apply even to tracked files and
selected stash contents. Stash contents are exported only when explicitly selected,
in separate -stash-*.zip archives based on each stash's original base commit.
Review exports for private data before sharing. Issues are read-only snapshots.
""",
    )
    selection = parser.add_mutually_exclusive_group()
    selection.add_argument("--branch", action="append", metavar="NAME", help="Include selected branch; repeat (default: current branch)")
    selection.add_argument("--all-branches", action="store_true", help="Include all local and remote-tracking branch refs")
    parser.add_argument("--with-issues", action="store_true", help="Also export GitHub issues as a paired ZIP")
    parser.add_argument("--repo", default="ramou/ChessMod", metavar="OWNER/REPO", help="GitHub repo used with --with-issues")
    parser.add_argument("--issue-state", choices=("all", "open", "closed"), default="all", help="Issue state (default: all)")
    parser.add_argument("--issue-label", action="append", default=[], metavar="NAME", help="Require label; repeat for AND filtering")
    parser.add_argument("--issue-milestone", metavar="NUMBER|any|none", help="Filter issue milestone")
    parser.add_argument("--issue-since", metavar="ISO_DATE", help="Only issues updated since this date/time")
    parser.add_argument("--issue-number", type=int, action="append", metavar="N", help="Select issue by number; repeat")
    parser.add_argument("--issue-limit", type=int, metavar="N", help="Limit number of issues; partial export")
    parser.add_argument("--issue-no-comments", action="store_true", help="Do not fetch comments")
    parser.add_argument("--issue-max-mb", type=int, default=64, metavar="N", help="Max issue ZIP uncompressed MiB (default: 64)")
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIR, metavar="DIR", help="Output directory (default: .ai-exports/)")
    parser.add_argument("--max-bundle-mb", type=int, default=256, metavar="N", help="Max bundle file MiB (default: 256)")
    parser.add_argument("--worktree", choices=("auto", "always", "skip"), default="auto",
                        help="Separate working-state ZIP: auto if dirty/stashes (default), always, or skip")
    parser.add_argument("--no-untracked", action="store_true", help="Record names, but omit untracked file contents from working-state ZIP")
    parser.add_argument("--no-stashes", action="store_true", help="Do not include stash inventory in working-state ZIP")
    parser.add_argument("--stash-ref", action="append", default=[], metavar="REF",
                        help="Export stash contents in a separate ZIP; repeat for multiple refs")
    parser.add_argument("--all-stash-contents", action="store_true",
                        help="Explicitly export a separate ZIP for every stash")
    parser.add_argument("--max-worktree-mb", type=int, default=64, metavar="N", help="Max uncompressed working-state ZIP MiB (default: 64)")
    parser.add_argument("--allow-dirty", action="store_true", help="Legacy alias for --worktree skip (uncommitted content excluded)")
    parser.add_argument("--dry-run", action="store_true", help="Print selected refs and options without writing anything")
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = make_parser()
    args = parser.parse_args(argv)
    if args.max_bundle_mb <= 0 or args.issue_max_mb <= 0 or args.max_worktree_mb <= 0:
        parser.error("size limits must be positive")
    if args.issue_limit is not None and args.issue_limit <= 0:
        parser.error("--issue-limit must be positive")
    if args.issue_number and any(number <= 0 for number in args.issue_number):
        parser.error("--issue-number values must be positive")
    if not args.with_issues and (args.issue_label or args.issue_number or args.issue_since or
                                  args.issue_milestone or args.issue_limit is not None or
                                  args.issue_no_comments or args.issue_state != "all"):
        parser.error("--issue-* filters require --with-issues")
    if args.allow_dirty and args.worktree != "auto":
        parser.error("--allow-dirty cannot be combined with --worktree (use --worktree skip)")
    if args.stash_ref and args.all_stash_contents:
        parser.error("Use --stash-ref or --all-stash-contents, not both")
    if args.no_stashes and (args.stash_ref or args.all_stash_contents):
        parser.error("--no-stashes cannot be combined with stash contents")
    if (args.allow_dirty or args.worktree == "skip") and (args.stash_ref or args.all_stash_contents):
        parser.error("Stash contents require --worktree auto or always, not skip")
    try:
        refs = branch_refs(args.branch or [], args.all_branches)
        state = export_worktree.inspect(ROOT)
        dirty = bool(state["entries"])
        mode = "skip" if args.allow_dirty else args.worktree
        stash_exports = export_worktree.selected_stashes(state["stashes"], args.stash_ref,
                                                         args.all_stash_contents)
        needs_worktree = (mode == "always" or
                          (mode == "auto" and (dirty or (state["stashes"] and not args.no_stashes))))
        if dirty and not needs_worktree:
            print("WARNING: uncommitted changes excluded from Git bundle and working-state ZIP", file=sys.stderr)
        heads = portable_branches(refs)
        if needs_worktree and not export_worktree.head_reachable_from(ROOT, state["head_commit"], heads):
            raise ContextError("Current HEAD is absent from selected bundle history; include the "
                               "checked-out branch with --branch or use --all-branches")
        for stash in stash_exports:
            parent = export_worktree.git_text(ROOT, "rev-parse", stash["commit"] + "^1")
            if not export_worktree.head_reachable_from(ROOT, parent, heads):
                raise ContextError(f"Stash {stash['reference']} has base {parent[:12]} absent "
                                   "from selected bundle history; include its branch")
        commit = heads[0][1]
        branch_id = slug(heads[0][0]) if len(heads) == 1 else f"multi-{len(heads)}"
        label = f"ChessMod-context-{branch_id}-{datetime.now(timezone.utc):%Y%m%dT%H%M%SZ}-{commit[:12]}"
        output_dir = args.output_dir.expanduser().resolve()
        bundle = output_dir / f"{label}.bundle"
        issues = output_dir / f"{label}-issues.zip"
        worktree = output_dir / f"{label}-worktree.zip"
        stash_paths = [(stash, output_dir / export_worktree.stash_filename(label, stash))
                       for stash in stash_exports]
        if (bundle.exists() or (args.with_issues and issues.exists()) or
                (needs_worktree and worktree.exists()) or any(p.exists() for _, p in stash_paths)):
            raise ContextError("Output already exists; refusing to overwrite a prior snapshot")
        print("Selected refs (source -> portable bundle branch):")
        for name, sha, ref in heads:
            print(f"  {ref} -> {name}: {sha[:12]}")
        print(f"Bundle: {bundle}")
        if args.with_issues:
            print(f"Issues: {issues} ({args.repo}, state={args.issue_state})")
        if needs_worktree:
            print(f"Uncommitted state: {worktree} ({len(state['entries'])} paths, {len(state['stashes']) if not args.no_stashes else 0} stashes)")
        for stash, target in stash_paths:
            print(f"Saved stash {stash['reference']}: {target}")
        if args.dry_run:
            return 0
        output_dir.mkdir(parents=True, exist_ok=True)
        try:
            create_portable_bundle(bundle, heads)
            git("bundle", "verify", str(bundle))
            bundle_size = bundle.stat().st_size
            if bundle_size > args.max_bundle_mb * 1024 * 1024:
                raise ContextError(f"Bundle {bundle_size / 1048576:.1f} MiB exceeds "
                                   f"--max-bundle-mb {args.max_bundle_mb}")
            if needs_worktree:
                export_worktree.export(ROOT, worktree, state,
                                       include_untracked=not args.no_untracked,
                                       include_stashes=not args.no_stashes,
                                       max_mb=args.max_worktree_mb,
                                       bundle_heads=heads, bundle_path=bundle)
            for stash, target in stash_paths:
                export_worktree.export_stash(ROOT, target, stash, max_mb=args.max_worktree_mb,
                                             bundle_heads=heads, bundle_path=bundle)
            if args.with_issues:
                command = [sys.executable, str(ROOT / "scripts" / "export_issues.py"),
                           "--repo", args.repo, "--state", args.issue_state,
                           "--source-commit", commit, "--output", str(issues),
                           "--max-mb", str(args.issue_max_mb)]
                for name, sha, original in heads:
                    command.extend(("--source-ref", f"{name}={sha}"))
                for arg_name, option in (("issue_label", "--label"),
                                         ("issue_number", "--number")):
                    for item in getattr(args, arg_name) or []:
                        command.extend((option, str(item)))
                for arg_name, option in (("issue_since", "--since"),
                                         ("issue_milestone", "--milestone"),
                                         ("issue_limit", "--limit")):
                    item = getattr(args, arg_name)
                    if item is not None:
                        command.extend((option, str(item)))
                if args.issue_no_comments:
                    command.append("--no-comments")
                subprocess.run(command, check=True)
        except (ContextError, export_worktree.SnapshotError, subprocess.CalledProcessError, OSError) as exc:
            bundle.unlink(missing_ok=True)
            issues.unlink(missing_ok=True)
            worktree.unlink(missing_ok=True)
            for _, stash_path in stash_paths:
                stash_path.unlink(missing_ok=True)
            raise ContextError(f"Context export failed; incomplete pair removed: {exc}") from exc
        print(f"Ready: {bundle}")
        if args.with_issues:
            print(f"Ready: {issues}")
        if needs_worktree:
            print(f"Ready: {worktree}")
        for _, stash_path in stash_paths:
            print(f"Ready: {stash_path}")
        return 0
    except (ContextError, export_worktree.SnapshotError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
