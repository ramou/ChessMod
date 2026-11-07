#!/usr/bin/env python3
"""Snapshot Git working state and optionally stash contents without changing the repository."""

from __future__ import annotations

import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import stat
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_OUTPUT_DIR = ROOT / ".ai-exports"


class SnapshotError(Exception):
    """A working-state snapshot could not be made safely or completely."""


def run_git(root: Path, *args: str) -> bytes:
    proc = subprocess.run(["git", "-C", str(root), *args], capture_output=True)
    if proc.returncode:
        raise SnapshotError(f"git {' '.join(args)} failed: {proc.stderr.decode(errors='replace').strip()}")
    return proc.stdout


def git_text(root: Path, *args: str) -> str:
    return run_git(root, *args).decode("utf-8", errors="replace").strip()


def safe_path(raw: str) -> str:
    path = PurePosixPath(raw)
    if (not raw or path.is_absolute() or ".." in path.parts or
            path.as_posix() != raw or "\\" in raw or "\x00" in raw):
        raise SnapshotError(f"Unsafe or nonportable Git path: {raw!r}")
    # Python ZIP encodes names as UTF-8; fail instead of silently corrupting names.
    try:
        raw.encode("utf-8")
    except UnicodeEncodeError as exc:
        raise SnapshotError(f"Git path cannot be encoded in ZIP: {raw!r}") from exc
    return raw


def get_entries(root: Path) -> list[dict]:
    """Parse porcelain v1 -z, including rename/copy source fields."""
    chunks = run_git(root, "-c", "core.quotepath=false", "status", "--porcelain=v1", "-z",
                     "--untracked-files=all").split(b"\0")
    if chunks[-1:] == [b""]:
        chunks.pop()
    entries = []
    i = 0
    while i < len(chunks):
        value = chunks[i]
        if len(value) < 4 or value[2:3] != b" ":
            raise SnapshotError("Malformed git status --porcelain -z output")
        xy = value[:2].decode("ascii")
        if xy in ("DD", "AU", "UD", "UA", "DU", "AA", "UU"):
            raise SnapshotError("Unmerged paths exist; resolve conflicts before exporting")
        path = safe_path(value[3:].decode("utf-8", errors="surrogateescape"))
        item = {"path": path, "index_status": xy[0], "worktree_status": xy[1],
                "untracked": xy == "??"}
        i += 1
        if "R" in xy or "C" in xy:
            if i >= len(chunks):
                raise SnapshotError("Malformed rename/copy entry in git status")
            item["original_path"] = safe_path(chunks[i].decode("utf-8", errors="surrogateescape"))
            i += 1
        entries.append(item)
    return entries


def ignored_paths(root: Path, paths: list[str]) -> set[str]:
    """Ask Git to apply the CURRENT worktree ignore rules, even for tracked paths.

    `git status` already hides ignored untracked files, but it will still list
    tracked files newly matched by .gitignore. `--no-index` is necessary here.
    NUL-delimited input/output preserves valid spaces and newlines in filenames.
    """
    if not paths:
        return set()
    data = b"\0".join(path.encode("utf-8", "surrogateescape") for path in paths) + b"\0"
    result = subprocess.run(["git", "-C", str(root), "check-ignore", "--no-index", "-z", "--stdin"],
                            input=data, capture_output=True)
    if result.returncode not in (0, 1):
        raise SnapshotError("git check-ignore failed: " + result.stderr.decode(errors="replace"))
    return {chunk.decode("utf-8", "surrogateescape")
            for chunk in result.stdout.split(b"\0") if chunk}


def filter_entries(root: Path, entries: list[dict]) -> tuple[list[dict], int]:
    candidates = [x["path"] for x in entries]
    candidates += [x["original_path"] for x in entries if x.get("original_path")]
    ignored = ignored_paths(root, candidates)
    filtered = []
    for item in entries:
        if item["path"] in ignored:
            continue
        clean = dict(item)
        if clean.get("original_path") in ignored:
            clean.pop("original_path")
        filtered.append(clean)
    return filtered, len(entries) - len(filtered)


def filtered_diff(root: Path, left: str | None, right: str | None,
                  paths: list[str], *, cached: bool = False) -> bytes:
    """Generate Git binary patches only for allowed paths; never leak ignored paths.

    Turning rename detection off prevents cross-boundary renames from revealing
    ignored source/destination names. Git's literal pathspec preserves unusual
    filename characters without interpreting them as wildcards.
    """
    if not paths:
        return b""
    command = ["diff", "--binary", "--no-renames", "--no-ext-diff",
               "--no-textconv", "--full-index"]
    if cached:
        command.append("--cached")
    if left is not None:
        command.append(left)
    if right is not None:
        command.append(right)
    # One invocation per path avoids an OS ARG_MAX limit for large working trees.
    return b"".join(run_git(root, *command, "--", ":(literal)" + path)
                    for path in sorted(set(paths)))


def get_stashes(root: Path) -> list[dict]:
    # git's stash format is stable; the NUL field delimiter avoids ambiguous spaces.
    lines = run_git(root, "stash", "list", "--format=%gd%x00%H%x00%gs%x00%aI").splitlines()
    stashes = []
    for line in lines:
        fields = line.decode("utf-8", errors="replace").split("\0", 3)
        if len(fields) != 4:
            raise SnapshotError("Malformed git stash list output")
        stashes.append(dict(reference=fields[0], commit=fields[1], subject=fields[2],
                            authored_at=fields[3]))
    return stashes


def inspect(root: Path) -> dict:
    entries, ignored_count = filter_entries(root, get_entries(root))
    return {"head_commit": git_text(root, "rev-parse", "HEAD"),
            "head_branch": git_text(root, "branch", "--show-current") or None,
            "entries": entries, "ignored_changed_path_count": ignored_count,
            "stashes": get_stashes(root)}


def head_reachable_from(root: Path, head: str, bundle_heads: list[tuple[str, str, str]]) -> bool:
    """A selected bundle tip must contain HEAD, or a working patch cannot be reconstructed."""
    for _, sha, _ in bundle_heads:
        proc = subprocess.run(["git", "-C", str(root), "merge-base", "--is-ancestor", head, sha],
                              capture_output=True)
        if proc.returncode == 0:
            return True
        if proc.returncode != 1:
            raise SnapshotError(proc.stderr.decode(errors="replace"))
    return False


def zip_entry(archive: zipfile.ZipFile, name: str, data: bytes, mode: int = 0o644,
              kind: int = stat.S_IFREG) -> None:
    item = zipfile.ZipInfo(name)
    item.create_system = 3
    item.external_attr = ((kind | mode) << 16)
    item.compress_type = zipfile.ZIP_DEFLATED
    archive.writestr(item, data)


def make_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Export a ZIP of uncommitted Git files/diffs against HEAD and a stash inventory.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""Examples:
  python scripts/export_worktree.py
  python scripts/export_worktree.py --no-untracked --no-stashes
  python scripts/export_worktree.py --stash-ref 'stash@{0}'
  python scripts/export_worktree.py --all-stash-contents --max-mb 128

The CURRENT worktree .gitignore rules filter working and stash files, even
tracked ones. By default stash references are inventoried but not exported.
Stash contents require --stash-ref or --all-stash-contents. Review archives
for credentials/private data before sharing. Pair with build_ai_context.py
for a matching Git bundle and issue snapshot.
""",
    )
    destinations = parser.add_mutually_exclusive_group()
    destinations.add_argument("--output", type=Path, help="Exact output ZIP (never overwritten)")
    destinations.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIR,
                              help="Directory for automatically named ZIPs (default: .ai-exports/)")
    parser.add_argument("--no-untracked", action="store_true", help="List but do not include untracked file contents")
    parser.add_argument("--no-stashes", action="store_true", help="Omit stash inventory (incompatible with stash contents)")
    parser.add_argument("--stash-ref", action="append", default=[], metavar="REF",
                        help="Export a separate snapshot of this stash; repeat for several")
    parser.add_argument("--all-stash-contents", action="store_true",
                        help="Explicitly export separate ZIPs for every listed stash")
    parser.add_argument("--max-mb", type=int, default=64, metavar="N",
                        help="Maximum uncompressed ZIP payload in MiB (default: 64)")
    parser.add_argument("--dry-run", action="store_true", help="Show what would be captured without writing")
    return parser


def export(root: Path, destination: Path, state: dict, *, include_untracked: bool = True,
           include_stashes: bool = True, max_mb: int = 64,
           bundle_heads: list[tuple[str, str, str]] | None = None,
           bundle_path: Path | None = None) -> Path:
    if max_mb <= 0:
        raise SnapshotError("--max-mb must be positive")
    if destination.exists():
        raise SnapshotError(f"Refusing to overwrite existing snapshot: {destination}")
    max_bytes = max_mb * 1024 * 1024
    files: dict[str, tuple[bytes, int, int]] = {}
    entries = []
    deletions = set()
    total = 0

    def charge(size: int) -> None:
        nonlocal total
        total += size
        if total > max_bytes:
            raise SnapshotError(f"Snapshot would exceed --max-mb {max_mb} (uncompressed)")

    for item in state["entries"]:
        entry = dict(item)
        path = item["path"]
        if item.get("original_path") and "R" in (item["index_status"] + item["worktree_status"]):
            deletions.add(item["original_path"])
        if item["untracked"] and not include_untracked:
            entry["action"] = "excluded-untracked"
            entries.append(entry)
            continue
        target = root / path
        if not target.is_symlink() and not target.is_file():
            if target.exists():
                raise SnapshotError(f"Unsupported working-tree file type: {path}")
            entry["action"] = "delete"
            deletions.add(path)
        else:
            is_link = target.is_symlink()
            info = target.lstat()
            if is_link:
                data = os.readlink(target).encode("utf-8", errors="surrogateescape")
                kind = stat.S_IFLNK
            else:
                if info.st_size > max_bytes - total:
                    raise SnapshotError(f"File too large for --max-mb {max_mb}: {path}")
                data = target.read_bytes()
                kind = stat.S_IFREG
            charge(len(data))
            entry.update(action="upsert", bytes=len(data), sha256=hashlib.sha256(data).hexdigest(),
                         mode=format(stat.S_IMODE(info.st_mode), "04o"), symlink=is_link)
            files[path] = (data, stat.S_IMODE(info.st_mode), kind)
        entries.append(entry)

    allowed_paths = [x["path"] for x in entries if not x["untracked"]]
    allowed_paths += [x["original_path"] for x in entries if x.get("original_path")]
    staged = filtered_diff(root, None, None, allowed_paths, cached=True)
    unstaged = filtered_diff(root, None, None, allowed_paths)
    charge(len(staged) + len(unstaged))
    bundle_digest = None
    if bundle_path is not None:
        bundle_hash = hashlib.sha256()
        with bundle_path.open("rb") as bundled:
            for chunk in iter(lambda: bundled.read(1024 * 1024), b""):
                bundle_hash.update(chunk)
        bundle_digest = bundle_hash.hexdigest()
    manifest = {
        "schema_version": 1,
        "snapshot_type": "uncommitted-working-tree",
        "created_at_utc": datetime.now(timezone.utc).isoformat(),
        "repository_base_commit": state["head_commit"],
        "repository_head_branch": state["head_branch"],
        "paired_bundle_name": bundle_path.name if bundle_path is not None else None,
        "paired_bundle_sha256": bundle_digest,
        "bundle_heads": [dict(branch=name, commit=sha, source_ref=ref)
                         for name, sha, ref in (bundle_heads or [])],
        "bundle_contains_base": None if bundle_heads is None else head_reachable_from(root, state["head_commit"], bundle_heads),
        "untracked_contents_included": include_untracked,
        "stash_contents_included": False,
        "gitignore_policy": "Current worktree rules via git check-ignore --no-index; ignored paths excluded, including tracked paths and diff headers",
        "ignored_changed_path_count": state.get("ignored_changed_path_count", 0),
        "stashes": state["stashes"] if include_stashes else [],
        "stash_inventory_included": include_stashes,
        "entries": entries,
        "deleted_paths": sorted(deletions),
        "notes": "files/ is a complete WORKING-TREE snapshot of changed paths; staged.diff and unstaged.diff preserve Git index distinctions. Stash contents are NOT included. Ignored files are excluded.",
    }
    counts = {"upsert": sum(x.get("action") == "upsert" for x in entries),
              "delete": sum(x.get("action") == "delete" for x in entries),
              "excluded_untracked": sum(x.get("action") == "excluded-untracked" for x in entries)}
    lines = ["# ChessMod — uncommitted working-state snapshot", "",
             "**This is NOT a commit or a normal implementation patch.**", "",
             f"- Base Git HEAD: `{state['head_commit']}`",
             f"- Checked-out branch: `{state['head_branch'] or '(detached)'}`",
             f"- Working files captured: {counts['upsert']}",
             f"- Deleted paths: {len(deletions)}",
             f"- Untracked paths omitted: {counts['excluded_untracked']}",
             f"- Recorded stashes (metadata only): {len(manifest['stashes'])}",
             f"- Changed paths omitted by current Git ignore rules: {manifest['ignored_changed_path_count']}",
             "", "## Files and reconstruction", "",
             "- `files/` contains exact working-file contents at export time, relative to repo root.",
             "- `staged.diff` and `unstaged.diff` contain separate Git `--binary` diffs.",
             "- `manifest.json` records index/worktree statuses, file SHA-256, modes, renames and stashes.",
             "- Apply only after reviewing. This snapshot does not automatically restore index state or stash contents.",
             "- Files outside this snapshot may exist in the original checkout; ignored files are not captured.",
             "", "## Deletions", ""]
    lines += [f"- `{path}`" for path in sorted(deletions)] or ["None."]
    lines += ["", "## Change inventory", ""]
    lines += [f"- `{e['path']}` — {e['index_status']}{e['worktree_status']} ({e['action']})"
              for e in entries] or ["No working-tree changes."]
    patch_md = "\n".join(lines) + "\n"
    manifest_bytes = (json.dumps(manifest, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    charge(len(manifest_bytes) + len(patch_md.encode("utf-8")))
    destination.parent.mkdir(parents=True, exist_ok=True)
    try:
        with zipfile.ZipFile(destination, "x", allowZip64=True) as archive:
            zip_entry(archive, "Patch.md", patch_md.encode("utf-8"))
            zip_entry(archive, "manifest.json", manifest_bytes)
            zip_entry(archive, "staged.diff", staged)
            zip_entry(archive, "unstaged.diff", unstaged)
            for path in sorted(files):
                data, mode, kind = files[path]
                zip_entry(archive, f"files/{path}", data, mode, kind)
    except Exception:
        destination.unlink(missing_ok=True)
        raise
    return destination


def _tree_map(root: Path, tree: str) -> dict[str, tuple[str, str, str]]:
    result = {}
    for item in run_git(root, "ls-tree", "-rz", "--full-tree", tree).split(b"\0"):
        if not item:
            continue
        meta, sep, raw_path = item.partition(b"\t")
        fields = meta.decode("ascii").split()
        if not sep or len(fields) != 3:
            raise SnapshotError("Malformed ls-tree output for stash")
        path = safe_path(raw_path.decode("utf-8", "surrogateescape"))
        result[path] = tuple(fields)
    return result


def _changed_between(root: Path, before: str, after: str) -> set[str]:
    return {safe_path(item.decode("utf-8", "surrogateescape"))
            for item in run_git(root, "diff", "--name-only", "--no-renames", "-z",
                                before, after).split(b"\0") if item}


def selected_stashes(stashes: list[dict], references: list[str], all_contents: bool) -> list[dict]:
    """Resolve user-requested stash names to immutable commit IDs at inspection time."""
    if all_contents and references:
        raise SnapshotError("Use either --all-stash-contents or --stash-ref, not both")
    if all_contents:
        return stashes
    index = {item["reference"]: item for item in stashes}
    for item in stashes:
        index[item["commit"]] = item
    result = []
    for reference in references:
        if reference not in index:
            raise SnapshotError(f"Unknown stash {reference!r}; run git stash list")
        item = index[reference]
        if item not in result:
            result.append(item)
    return result


def stash_filename(prefix: str, stash: dict) -> str:
    number = re.sub(r"[^0-9]+", "", stash["reference"]) or "unknown"
    return f"{prefix}-stash-{number}-{stash['commit'][:12]}.zip"


def export_stash(root: Path, destination: Path, stash: dict, *, max_mb: int = 64,
                 bundle_heads: list[tuple[str, str, str]] | None = None,
                 bundle_path: Path | None = None) -> Path:
    """Non-mutating snapshot of a stash's two diff layers and optional untracked tree.

    A stash commit stores the tracked worktree tree; parent one is its original
    base, parent two its staged index, and optional parent three untracked files.
    Nothing is checked out, applied, committed, or added to the user's index.
    """
    if max_mb <= 0:
        raise SnapshotError("--max-mb must be positive")
    if destination.exists():
        raise SnapshotError(f"Refusing to overwrite existing stash snapshot: {destination}")
    sha = stash["commit"]
    parents = git_text(root, "rev-list", "--parents", "-n", "1", sha).split()
    if len(parents) not in (3, 4) or parents[0] != sha:
        raise SnapshotError(f"Not a recognised Git stash commit: {sha}")
    base, index = parents[1:3]
    extra = parents[3] if len(parents) == 4 else None
    if bundle_heads is not None and not head_reachable_from(root, base, bundle_heads):
        raise SnapshotError(f"Stash {stash['reference']} base {base[:12]} is absent from the bundle")
    base_map, index_map, final_map = (_tree_map(root, ref) for ref in (base, index, sha))
    extra_map = _tree_map(root, extra) if extra else {}
    staged_paths = _changed_between(root, base, index)
    unstaged_paths = _changed_between(root, index, sha)
    changed = staged_paths | unstaged_paths | set(extra_map)
    ignored = ignored_paths(root, list(changed))
    allowed = sorted(changed - ignored)
    staged = filtered_diff(root, base, index, sorted(staged_paths - ignored))
    unstaged = filtered_diff(root, index, sha, sorted(unstaged_paths - ignored))

    files = {}
    entries = []
    deletions = []
    limit = max_mb * 1048576
    consumed = len(staged) + len(unstaged)
    if consumed > limit:
        raise SnapshotError("Stash diffs exceed --max-mb")
    for path in allowed:
        source = extra_map if path in extra_map else final_map
        info = source.get(path)
        entry = {"path": path, "staged": path in staged_paths,
                 "unstaged": path in unstaged_paths, "untracked": path in extra_map}
        if info is None:
            entry["action"] = "delete"
            deletions.append(path)
        else:
            mode, kind, oid = info
            if kind != "blob" or mode not in ("100644", "100755", "120000"):
                raise SnapshotError(f"Unsupported stash object at {path}: {mode} {kind}")
            # Reject oversized blobs BEFORE loading them from Git into memory.
            blob_size = int(git_text(root, "cat-file", "-s", oid))
            if consumed + blob_size > limit:
                raise SnapshotError(f"Stash snapshot would exceed --max-mb {max_mb}: {path}")
            data = run_git(root, "cat-file", "blob", oid)
            consumed += len(data)
            if consumed > limit:
                raise SnapshotError(f"Stash snapshot would exceed --max-mb {max_mb}")
            mode_bits = 0o755 if mode == "100755" else 0o644
            file_kind = stat.S_IFLNK if mode == "120000" else stat.S_IFREG
            entry.update(action="upsert", bytes=len(data), sha256=hashlib.sha256(data).hexdigest(),
                         mode=mode, symlink=(mode == "120000"))
            files[path] = (data, mode_bits, file_kind)
        entries.append(entry)
    digest = None
    if bundle_path is not None:
        sha_hasher = hashlib.sha256()
        with bundle_path.open("rb") as stream:
            for block in iter(lambda: stream.read(1048576), b""):
                sha_hasher.update(block)
        digest = sha_hasher.hexdigest()
    manifest = {
        "schema_version": 1, "snapshot_type": "git-stash", "created_at_utc": datetime.now(timezone.utc).isoformat(),
        "stash_reference_at_export": stash["reference"], "stash_commit": sha,
        "stash_subject": stash["subject"], "stash_authored_at": stash["authored_at"],
        "repository_base_commit": base, "stash_index_commit": index,
        "stash_untracked_commit": extra, "gitignore_basis_commit": git_text(root, "rev-parse", "HEAD"),
        "gitignore_policy": "Current worktree rules via git check-ignore --no-index, including tracked files",
        "ignored_changed_path_count": len(ignored), "paired_bundle_name": bundle_path.name if bundle_path else None,
        "paired_bundle_sha256": digest, "bundle_contains_base": None if bundle_heads is None else True,
        "entries": entries, "deleted_paths": deletions,
        "notes": "Stash content captured from Git objects, not applied. It reflects the original stash base, not current HEAD. Review before restoring.",
    }
    patch_lines = ["# ChessMod — saved Git stash snapshot", "",
                   "**Not a commit and not a normal implementation patch.**", "",
                   f"- Stash at export: `{stash['reference']}` (`{sha}`)",
                   f"- Stash base commit: `{base}`", f"- Stash index commit: `{index}`",
                   f"- Changed paths excluded by current ignore rules: {len(ignored)}", "",
                   "`files/` holds the stash's final file content for changed paths,",
                   "`staged.diff` and `unstaged.diff` preserve its two tracked diff layers.",
                   "Untracked files saved by `git stash -u/-a` are in `files/`.",
                   "This archive does not automatically recreate a stash or restore index state.",
                   "", "## Deletions", ""]
    patch_lines += [f"- `{p}`" for p in deletions] or ["None."]
    patch_lines += ["", "## Files", ""]
    patch_lines += [f"- `{item['path']}` — {item['action']}" for item in entries] or ["None."]
    patch_md = "\n".join(patch_lines) + "\n"
    metadata = (json.dumps(manifest, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    consumed += len(patch_md.encode()) + len(metadata)
    if consumed > limit:
        raise SnapshotError(f"Stash snapshot would exceed --max-mb {max_mb}")
    destination.parent.mkdir(parents=True, exist_ok=True)
    try:
        with zipfile.ZipFile(destination, "x", allowZip64=True) as archive:
            zip_entry(archive, "Patch.md", patch_md.encode())
            zip_entry(archive, "manifest.json", metadata)
            zip_entry(archive, "staged.diff", staged)
            zip_entry(archive, "unstaged.diff", unstaged)
            for path in sorted(files):
                data, mode, kind = files[path]
                zip_entry(archive, f"files/{path}", data, mode, kind)
    except Exception:
        destination.unlink(missing_ok=True)
        raise
    return destination


def main(argv: list[str] | None = None) -> int:
    parser = make_parser()
    args = parser.parse_args(argv)
    if args.max_mb <= 0:
        parser.error("--max-mb must be positive")
    if args.no_stashes and (args.stash_ref or args.all_stash_contents):
        parser.error("--no-stashes cannot be combined with stash content export")
    if args.stash_ref and args.all_stash_contents:
        parser.error("Use --stash-ref or --all-stash-contents, not both")
    try:
        state = inspect(ROOT)
        branch = re.sub(r"[^a-zA-Z0-9._-]+", "-", state["head_branch"] or "detached")
        label = f"ChessMod-worktree-{branch}-{datetime.now(timezone.utc):%Y%m%dT%H%M%SZ}-{state['head_commit'][:12]}"
        output = (args.output or args.output_dir / f"{label}.zip").expanduser().resolve()
        selected = selected_stashes(state["stashes"], args.stash_ref, args.all_stash_contents)
        stash_outputs = [(stash, output.parent / stash_filename(output.stem, stash)) for stash in selected]
        if len({output, *(p for _, p in stash_outputs)}) != 1 + len(stash_outputs):
            raise SnapshotError("Multiple output snapshots would have the same filename")
        if any(p.exists() for p in [output, *(p for _, p in stash_outputs)]):
            raise SnapshotError("One or more output archives already exist; refusing to overwrite")
        print(f"HEAD {state['head_commit']} ({state['head_branch'] or 'detached'})")
        print(f"Working paths: {len(state['entries'])}; stash entries: {len(state['stashes'])}")
        print(f"Snapshot: {output}")
        for stash, path in stash_outputs:
            print(f"Stash {stash['reference']} -> {path}")
        if args.dry_run:
            return 0
        created = []
        try:
            export(ROOT, output, state, include_untracked=not args.no_untracked,
                   include_stashes=not args.no_stashes, max_mb=args.max_mb)
            created.append(output)
            for stash, path in stash_outputs:
                export_stash(ROOT, path, stash, max_mb=args.max_mb)
                created.append(path)
        except Exception:
            for path in created:
                path.unlink(missing_ok=True)
            raise
        for path in created:
            print(f"Ready: {path}")
        return 0
    except (SnapshotError, OSError, UnicodeError, ValueError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
