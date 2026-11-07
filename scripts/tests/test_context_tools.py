"""Offline regression tests for the ChessMod context tools; no GitHub API calls."""

from __future__ import annotations

from contextlib import redirect_stderr, redirect_stdout
from io import StringIO
import json
import hashlib
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

SCRIPTS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS))
import build_ai_context as builder  # noqa: E402
import export_worktree as worktree  # noqa: E402
import export_issues as exporter  # noqa: E402
import experiment_history_cleanup as history  # noqa: E402


def sample_issue(number=13, *, state="open", labels=("bug",), comments=1, pr=False):
    issue = {
        "number": number, "title": "Castling regression", "body": "Reproduction steps.\n",
        "html_url": f"https://github.com/ramou/ChessMod/issues/{number}",
        "state": state, "state_reason": None, "created_at": "2026-09-01T00:00:00Z",
        "updated_at": "2026-11-01T12:00:00Z", "closed_at": None,
        "user": {"login": "ramou"}, "assignees": [],
        "labels": [{"name": name, "color": "123456", "description": None} for name in labels],
        "comments": comments, "locked": False, "milestone": None,
    }
    if pr:
        issue["pull_request"] = {"url": "ignored"}
    return issue


def sample_comment():
    return {
        "id": 17, "user": {"login": "contributor"},
        "created_at": "2026-11-02T12:00:00Z", "updated_at": "2026-11-02T12:00:00Z",
        "html_url": "https://github.com/ramou/ChessMod/issues/13#issuecomment-17",
        "body": "Observed on 1.20.1.",
    }


class FakeClient:
    def __init__(self):
        self.calls = []
        self.issues = [sample_issue(13), sample_issue(14, pr=True),
                       sample_issue(84, state="closed", labels=("feature",), comments=0)]

    def list(self, path, params=None, limit=None):
        self.calls.append((path, params))
        if path.endswith("/comments"):
            return [sample_comment()]
        if path.endswith("/labels"):
            return [{"name": "bug", "description": "A defect", "color": "ff0000",
                     "default": True, "url": "https://api.github.com/repos/ramou/ChessMod/labels/bug"},
                    {"name": "enhancement", "description": "New feature", "color": "22ff11"}]
        if path.endswith("/milestones"):
            return [{"number": 4, "title": "Version 1.0", "state": "open",
                     "description": "Future release", "due_on": None, "open_issues": 12,
                     "closed_issues": 3},
                    {"number": 2, "title": "Historical", "state": "closed"}]
        return self.issues

    def get(self, path, params=None):
        self.calls.append((path, params))
        if path == "/repos/ramou/ChessMod":
            return {"full_name": "ramou/ChessMod", "html_url": "https://github.com/ramou/ChessMod",
                    "default_branch": "1.20.1", "has_issues": True, "archived": False}
        return next(i for i in self.issues if path.endswith("/" + str(i["number"])))


class IssueExportTests(unittest.TestCase):
    def test_default_is_all_states_excludes_prs_includes_comments(self):
        with tempfile.TemporaryDirectory() as tmp:
            args = exporter.make_parser().parse_args(["--output-dir", tmp, "--source-commit", "abcdef1234"])
            client = FakeClient()
            with redirect_stderr(StringIO()):
                path = exporter.export_to_zip(args, client)
            with zipfile.ZipFile(path) as archive:
                names = archive.namelist()
                self.assertEqual(names, ["manifest.json", "issue_catalogue.md", "issues/000013.md", "issues/000084.md"])
                manifest = json.loads(archive.read("manifest.json"))
                self.assertEqual(manifest["source_commit"], "abcdef1234")
                self.assertEqual(manifest["issue_count"], 2)
                self.assertEqual(manifest["schema_version"], 2)
                self.assertEqual(manifest["issue_creation_catalogue"]["repository"]["default_branch"], "1.20.1")
                self.assertEqual([m["number"] for m in manifest["issue_creation_catalogue"]["milestones"]], [2, 4])
                self.assertIn("Version 1.0", archive.read("issue_catalogue.md").decode())
                self.assertEqual(manifest["issues"][0]["comments"][0]["author"], "contributor")
                markdown = archive.read("issues/000013.md").decode("utf-8")
                self.assertIn("Observed on 1.20.1", markdown)
                self.assertIn("- Labels: bug", markdown)

    def test_filtered_export_keeps_full_catalogue_and_no_catalogue_is_opt_in(self):
        with tempfile.TemporaryDirectory() as tmp:
            client = FakeClient()
            args = exporter.make_parser().parse_args([
                "--output-dir", tmp, "--number", "13", "--state", "open",
                "--label", "bug", "--no-comments",
            ])
            with redirect_stderr(StringIO()):
                output = exporter.export_to_zip(args, client)
            with zipfile.ZipFile(output) as archive:
                manifest = json.loads(archive.read("manifest.json"))
                self.assertEqual(manifest["issue_count"], 1)
                self.assertEqual(len(manifest["issue_creation_catalogue"]["labels"]), 2)
                self.assertEqual(len(manifest["issue_creation_catalogue"]["milestones"]), 2)
                self.assertIn(("/repos/ramou/ChessMod/milestones", {"state": "all"}), client.calls)

            args = exporter.make_parser().parse_args([
                "--output", str(Path(tmp) / "no-catalogue.zip"), "--no-catalogue",
            ])
            client = FakeClient()
            with redirect_stderr(StringIO()):
                output = exporter.export_to_zip(args, client)
            with zipfile.ZipFile(output) as archive:
                manifest = json.loads(archive.read("manifest.json"))
                self.assertIsNone(manifest["issue_creation_catalogue"])
                self.assertFalse(manifest["catalogue_included"])
                self.assertNotIn("issue_catalogue.md", archive.namelist())
            self.assertFalse(any(path.endswith("/milestones") for path, _ in client.calls))

    def test_filters_by_label_state_since_and_numbers(self):
        args = exporter.make_parser().parse_args([
            "--number", "13", "--number", "84", "--state", "open",
            "--label", "bug", "--since", "2026-10-01", "--no-comments",
        ])
        result = exporter.collect(FakeClient(), args)
        self.assertEqual([i["number"] for i in result], [13])
        self.assertFalse(result[0]["comments"])
        self.assertEqual(args.since, "2026-10-01T00:00:00Z")

    def test_size_guard_leaves_no_partial_zip(self):
        with tempfile.TemporaryDirectory() as tmp:
            args = exporter.make_parser().parse_args(["--output-dir", tmp, "--max-mb", "1"])
            client = FakeClient()
            client.issues = [sample_issue()]
            client.issues[0]["body"] = "A" * (1024 * 1024)
            with redirect_stderr(StringIO()), self.assertRaises(exporter.ExportError):
                exporter.export_to_zip(args, client)
            self.assertEqual(list(Path(tmp).glob("*.zip")), [])

    def test_refuse_overwrite(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "fixed.zip"
            args = exporter.make_parser().parse_args(["--output", str(path)])
            with redirect_stderr(StringIO()):
                exporter.export_to_zip(args, FakeClient())
            with self.assertRaisesRegex(exporter.ExportError, "overwrite"):
                exporter.export_to_zip(args, FakeClient())

    def test_github_pagination(self):
        client = exporter.GitHubClient()
        calls = []

        def page(url):
            calls.append(url)
            if len(calls) == 1:
                return ([sample_issue(13)], "https://api.github.com/repos/ramou/ChessMod/issues?page=2")
            return ([sample_issue(84)], None)

        client.page = page
        result = client.list("/repos/ramou/ChessMod/issues")
        self.assertEqual([i["number"] for i in result], [13, 84])
        self.assertEqual(len(calls), 2)
        with self.assertRaisesRegex(exporter.ExportError, "outside"):
            exporter.GitHubClient().page("https://example.com/a")


class ContextBundleTests(unittest.TestCase):
    def setUp(self):
        self.dir = tempfile.TemporaryDirectory()
        self.root = Path(self.dir.name) / "repo"
        self.root.mkdir()
        self.run_git("init", "-q")
        self.run_git("config", "user.name", "Test")
        self.run_git("config", "user.email", "test@example.invalid")
        (self.root / "README.md").write_text("ChessMod fixture\n")
        self.run_git("add", ".")
        self.run_git("commit", "-qm", "Initial")
        self.run_git("branch", "1.21")
        self.run_git("update-ref", "refs/remotes/origin/legacy-1.8", "HEAD")
        self.branch = subprocess.check_output(
            ["git", "-C", str(self.root), "symbolic-ref", "--short", "HEAD"], text=True
        ).strip()
        self.old_root = builder.ROOT
        builder.ROOT = self.root

    def tearDown(self):
        builder.ROOT = self.old_root
        self.dir.cleanup()

    def run_git(self, *args):
        subprocess.run(["git", "-C", str(self.root), *args], check=True,
                       capture_output=True, text=True)

    def test_selected_branches_and_standalone_bundle(self):
        out = self.root / ".ai-exports"
        with redirect_stdout(StringIO()):
            result = builder.main(["--branch", self.branch, "--branch", "1.21",
                                   "--output-dir", str(out)])
        self.assertEqual(result, 0)
        bundles = list(out.glob("*.bundle"))
        self.assertEqual(len(bundles), 1)
        self.run_git("bundle", "verify", str(bundles[0]))
        heads = subprocess.check_output(["git", "-C", str(self.root), "bundle", "list-heads",
                                         str(bundles[0])], text=True)
        self.assertIn(f"refs/heads/{self.branch}", heads)
        self.assertIn("refs/heads/1.21", heads)
        self.assertIn(" HEAD", heads)
        clone = Path(self.dir.name) / "clone"
        subprocess.run(["git", "clone", "--quiet", str(bundles[0]), str(clone)],
                       check=True, capture_output=True, text=True)
        self.assertTrue((clone / "README.md").exists())

    def test_remote_only_branch_is_cloneable(self):
        out = self.root / ".ai-exports"
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--branch", "origin/legacy-1.8",
                                           "--output-dir", str(out)]), 0)
        bundle = next(out.glob("*.bundle"))
        heads = subprocess.check_output(["git", "bundle", "list-heads", str(bundle)], text=True)
        self.assertIn("refs/heads/legacy-1.8", heads)
        clone = Path(self.dir.name) / "cloned-remote"
        subprocess.run(["git", "clone", "--quiet", str(bundle), str(clone)],
                       check=True, capture_output=True, text=True)
        branch = subprocess.check_output(["git", "-C", str(clone),
                                          "branch", "--show-current"], text=True).strip()
        self.assertEqual(branch, "legacy-1.8")

    def test_paired_export_passes_selected_refs(self):
        real_run = subprocess.run
        issued_commands = []
        def fake_run(command, *args, **kwargs):
            if len(command) > 1 and str(command[1]).endswith("export_issues.py"):
                issued_commands.append(command)
                output = Path(command[command.index("--output") + 1])
                with zipfile.ZipFile(output, "x") as archive:
                    archive.writestr("manifest.json", "{}")
                return subprocess.CompletedProcess(command, 0)
            return real_run(command, *args, **kwargs)

        out = self.root / ".ai-exports"
        with patch.object(builder.subprocess, "run", side_effect=fake_run):
            with redirect_stdout(StringIO()):
                self.assertEqual(builder.main(["--branch", "1.21", "--with-issues",
                                               "--issue-number", "13", "--issue-label", "bug",
                                               "--output-dir", str(out)]), 0)
        self.assertEqual(len(list(out.glob("*.bundle"))), 1)
        self.assertEqual(len(list(out.glob("*.zip"))), 1)
        self.assertIn("--source-ref", issued_commands[0])
        self.assertIn("--number", issued_commands[0])
        self.assertIn("--label", issued_commands[0])

    def test_issue_no_catalogue_option_forwarding(self):
        real_run = subprocess.run
        captured = []
        def fake_run(command, *args, **kwargs):
            if len(command) > 1 and str(command[1]).endswith("export_issues.py"):
                captured.append(command)
                output = Path(command[command.index("--output") + 1])
                with zipfile.ZipFile(output, "x") as archive:
                    archive.writestr("manifest.json", "{}")
                return subprocess.CompletedProcess(command, 0)
            return real_run(command, *args, **kwargs)
        with patch.object(builder.subprocess, "run", side_effect=fake_run):
            with redirect_stdout(StringIO()):
                self.assertEqual(builder.main(["--with-issues", "--issue-no-catalogue",
                                               "--output-dir", str(self.root / "exports")]), 0)
        self.assertIn("--no-catalogue", captured[0])

    def test_dirty_worktree_automatically_exports_separate_snapshot(self):
        (self.root / "uncommitted.txt").write_text("not in a bundle\n")
        out = self.root / "exports"
        with redirect_stdout(StringIO()), redirect_stderr(StringIO()):
            result = builder.main(["--output-dir", str(out)])
        self.assertEqual(result, 0)
        snapshots = list(out.glob("*-worktree.zip"))
        self.assertEqual(len(snapshots), 1)
        with zipfile.ZipFile(snapshots[0]) as z:
            self.assertEqual(z.read("files/uncommitted.txt"), b"not in a bundle\n")
            manifest = json.loads(z.read("manifest.json"))
            self.assertEqual(manifest["repository_base_commit"],
                             subprocess.check_output(["git", "-C", str(self.root),
                                                      "rev-parse", "HEAD"], text=True).strip())
            self.assertTrue(manifest["bundle_contains_base"])
            self.assertEqual(manifest["paired_bundle_name"], next(out.glob("*.bundle")).name)
            self.assertEqual(manifest["paired_bundle_sha256"],
                             hashlib.sha256(next(out.glob("*.bundle")).read_bytes()).hexdigest())
        with redirect_stdout(StringIO()), redirect_stderr(StringIO()):
            result = builder.main(["--worktree", "skip", "--output-dir", str(out / "more")])
        self.assertEqual(result, 0)
        self.assertFalse(list((out / "more").glob("*-worktree.zip")))
        with redirect_stdout(StringIO()), redirect_stderr(StringIO()):
            result = builder.main(["--allow-dirty", "--output-dir", str(out / "skip")])
        self.assertEqual(result, 0)
        self.assertFalse(list((out / "skip").glob("*-worktree.zip")))

    def test_worktree_snapshot_has_staged_unstaged_and_deletions(self):
        (self.root / "delete-me.txt").write_text("short lived\n")
        self.run_git("add", "delete-me.txt")
        self.run_git("commit", "-qm", "A deletion candidate")
        (self.root / "README.md").write_text("staged revision\n")
        self.run_git("add", "README.md")
        (self.root / "README.md").write_text("working revision\n")
        (self.root / "added file.txt").write_text("some data\n")
        (self.root / "delete-me.txt").unlink()
        out = self.root / "snapshots"
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--output-dir", str(out)]), 0)
        path = next(out.glob("*-worktree.zip"))
        with zipfile.ZipFile(path) as z:
            names = z.namelist()
            self.assertIn("files/README.md", names)
            self.assertIn("files/added file.txt", names)
            self.assertNotIn("files/delete-me.txt", names)
            self.assertEqual(z.read("files/README.md"), b"working revision\n")
            self.assertIn(b"staged revision", z.read("staged.diff"))
            self.assertIn(b"working revision", z.read("unstaged.diff"))
            manifest = json.loads(z.read("manifest.json"))
            self.assertIn("delete-me.txt", manifest["deleted_paths"])
            self.assertTrue(manifest["bundle_contains_base"])

    def test_stash_metadata_only_and_excluded_untracked(self):
        (self.root / "README.md").write_text("private shelf\n")
        self.run_git("stash", "push", "-m", "Hold for later")
        (self.root / "untracked-secret.txt").write_text("TOP SECRET\n")
        out = self.root / "snapshots"
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--no-untracked", "--output-dir", str(out)]), 0)
        with zipfile.ZipFile(next(out.glob("*-worktree.zip"))) as z:
            self.assertNotIn("files/untracked-secret.txt", z.namelist())
            manifest = json.loads(z.read("manifest.json"))
            self.assertEqual(manifest["stashes"][0]["reference"], "stash@{0}")
            self.assertEqual(manifest["stashes"][0]["subject"], "On " + self.branch + ": Hold for later")
            self.assertIn("untracked-secret.txt", [e["path"] for e in manifest["entries"]])
            self.assertFalse(manifest["stash_contents_included"])
            self.assertNotIn(b"TOP SECRET", z.read("manifest.json"))

    def test_worktree_rejected_if_head_not_in_bundle(self):
        self.run_git("checkout", "-qb", "work-only")
        (self.root / "README.md").write_text("A new committed version\n")
        self.run_git("add", "README.md")
        self.run_git("commit", "-qm", "Work only")
        (self.root / "README.md").write_text("Local change\n")
        out = self.root / "out"
        with redirect_stdout(StringIO()), redirect_stderr(StringIO()):
            self.assertEqual(builder.main(["--branch", "1.21", "--output-dir", str(out)]), 1)
        self.assertFalse(out.exists())

    def test_stash_only_creates_snapshot_for_clean_tree(self):
        (self.root / "README.md").write_text("shelved\n")
        self.run_git("stash", "push", "-m", "Paused work")
        self.assertFalse(worktree.inspect(self.root)["entries"])
        out = self.root / ".ai-exports"
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--output-dir", str(out)]), 0)
        with zipfile.ZipFile(next(out.glob("*-worktree.zip"))) as z:
            manifest = json.loads(z.read("manifest.json"))
            self.assertEqual(len(manifest["entries"]), 0)
            self.assertEqual(len(manifest["stashes"]), 1)

    def test_rename_and_symlink_metadata(self):
        (self.root / "old.txt").write_text("content\n")
        self.run_git("add", "old.txt")
        self.run_git("commit", "-qm", "Add old path")
        self.run_git("mv", "old.txt", "new.txt")
        if hasattr(Path, "symlink_to"):
            try:
                (self.root / "link.txt").symlink_to("new.txt")
            except (OSError, NotImplementedError):
                pass
        state = worktree.inspect(self.root)
        path = Path(self.dir.name) / "renamed.zip"
        worktree.export(self.root, path, state)
        with zipfile.ZipFile(path) as z:
            manifest = json.loads(z.read("manifest.json"))
            self.assertIn("old.txt", manifest["deleted_paths"])
            self.assertIn("files/new.txt", z.namelist())
            if "files/link.txt" in z.namelist():
                link = next(i for i in manifest["entries"] if i["path"] == "link.txt")
                self.assertTrue(link["symlink"])
                self.assertEqual(z.read("files/link.txt"), b"new.txt")

    def test_worktree_size_limit_and_conflicts(self):
        (self.root / "large.txt").write_bytes(b"x" * (1024 * 1024 + 1))
        state = worktree.inspect(self.root)
        with self.assertRaisesRegex(worktree.SnapshotError, "too large"):
            worktree.export(self.root, Path(self.dir.name) / "large.zip", state, max_mb=1)
        self.assertFalse((Path(self.dir.name) / "large.zip").exists())

    def test_current_gitignore_excludes_untracked_and_tracked_even_from_diffs(self):
        # Deliberately modify tracked data that a new, uncommitted ignore rule covers.
        # `git status` normally STILL reports tracked ignored files, so merely
        # using --untracked-files=all would not satisfy the handoff policy.
        (self.root / "private.txt").write_text("OLD SECRET\n")
        (self.root / "public.txt").write_text("original\n")
        self.run_git("add", "private.txt", "public.txt")
        self.run_git("commit", "-qm", "Add fixture")
        (self.root / ".gitignore").write_text("private.txt\n*.vault\n")
        (self.root / "private.txt").write_text("NEW SECRET DO NOT EXPORT\n")
        self.run_git("add", "private.txt")
        (self.root / "public.txt").write_text("safe revision\n")
        (self.root / "untracked.vault").write_text("DO NOT EXPORT\n")
        (self.root / "untracked.txt").write_text("safe untracked\n")
        out = self.root / "snapshots"
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--output-dir", str(out)]), 0)
        with zipfile.ZipFile(next(out.glob("*-worktree.zip"))) as z:
            names = z.namelist()
            self.assertIn("files/.gitignore", names)
            self.assertIn("files/public.txt", names)
            self.assertIn("files/untracked.txt", names)
            self.assertNotIn("files/private.txt", names)
            self.assertNotIn("files/untracked.vault", names)
            manifest = json.loads(z.read("manifest.json"))
            self.assertGreaterEqual(manifest["ignored_changed_path_count"], 1)
            self.assertNotIn("private.txt", [e["path"] for e in manifest["entries"]])
            self.assertNotIn(b"NEW SECRET", z.read("staged.diff"))
            self.assertNotIn(b"private.txt", z.read("staged.diff"))
            self.assertNotIn(b"private.txt", z.read("unstaged.diff"))

    def test_stash_content_export_index_worktree_and_untracked_filtered(self):
        (self.root / "ordinary.txt").write_text("original\n")
        (self.root / "sensitive.txt").write_text("INITIAL SECRET\n")
        self.run_git("add", "ordinary.txt", "sensitive.txt")
        self.run_git("commit", "-qm", "Add fixture")
        (self.root / "ordinary.txt").write_text("staged version\n")
        self.run_git("add", "ordinary.txt")
        (self.root / "ordinary.txt").write_text("unstaged version\n")
        (self.root / "sensitive.txt").write_text("STASHED SECRET DO NOT EXPORT\n")
        (self.root / "untracked-okay.txt").write_text("extra content\n")
        (self.root / "private.vault").write_text("IGNORED STASH ITEM\n")
        # Capture untracked and ignored files within the stash, then put NEW
        # ignore rules into the real working tree for both tracked and extra.
        self.run_git("stash", "push", "-a", "-m", "Paused complex change")
        (self.root / ".gitignore").write_text("sensitive.txt\n*.vault\n")
        out = self.root / "snapshots"
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--stash-ref", "stash@{0}",
                                           "--output-dir", str(out)]), 0)
        stashed = list(out.glob("*-stash-*.zip"))
        self.assertEqual(len(stashed), 1)
        with zipfile.ZipFile(stashed[0]) as z:
            names = z.namelist()
            self.assertIn("files/ordinary.txt", names)
            self.assertIn("files/untracked-okay.txt", names)
            self.assertNotIn("files/sensitive.txt", names)
            self.assertNotIn("files/private.vault", names)
            self.assertIn(b"staged version", z.read("staged.diff"))
            self.assertIn(b"unstaged version", z.read("unstaged.diff"))
            self.assertNotIn(b"STASHED SECRET", z.read("unstaged.diff"))
            self.assertNotIn(b"sensitive.txt", z.read("unstaged.diff"))
            manifest = json.loads(z.read("manifest.json"))
            self.assertEqual(manifest["snapshot_type"], "git-stash")
            self.assertEqual(manifest["stash_reference_at_export"], "stash@{0}")
            self.assertGreaterEqual(manifest["ignored_changed_path_count"], 2)
            self.assertTrue(manifest["bundle_contains_base"])
        # Neither the index nor the stash inventory should be modified.
        self.assertEqual(len(worktree.inspect(self.root)["stashes"]), 1)
        self.assertEqual((self.root / "ordinary.txt").read_text(), "original\n")

    def test_stash_content_opt_in_and_bad_selection(self):
        (self.root / "README.md").write_text("saved\n")
        self.run_git("stash", "push", "-m", "Later")
        out = self.root / "out"
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--output-dir", str(out)]), 0)
        self.assertFalse(list(out.glob("*-stash-*.zip")))
        with redirect_stdout(StringIO()), redirect_stderr(StringIO()):
            self.assertEqual(builder.main(["--stash-ref", "stash@{99}",
                                           "--output-dir", str(out / "bad")]), 1)
        self.assertFalse((out / "bad").exists())
        with redirect_stdout(StringIO()):
            self.assertEqual(builder.main(["--all-stash-contents",
                                           "--output-dir", str(out / "all")]), 0)
        self.assertEqual(len(list((out / "all").glob("*-stash-*.zip"))), 1)

    def test_stash_base_must_be_reachable_from_bundle(self):
        self.run_git("checkout", "-qb", "unbundled")
        (self.root / "README.md").write_text("later commit\n")
        self.run_git("add", "README.md")
        self.run_git("commit", "-qm", "Extra")
        (self.root / "README.md").write_text("shelved work\n")
        self.run_git("stash", "push", "-m", "Orphan branch stash")
        with redirect_stdout(StringIO()), redirect_stderr(StringIO()):
            result = builder.main(["--branch", "1.21", "--stash-ref", "stash@{0}",
                                   "--output-dir", str(self.root / "snapshots")])
        self.assertEqual(result, 1)
        self.assertFalse((self.root / "snapshots").exists())

    def test_bad_branch_rejected(self):
        with redirect_stdout(StringIO()), redirect_stderr(StringIO()):
            self.assertEqual(builder.main(["--branch", "no-such-branch"]), 1)


class HistoryExperimentTests(unittest.TestCase):
    def test_release_globs_do_not_remove_gradle_wrapper(self):
        self.assertTrue(history.selected("modpack/ChessMod_0.605.zip"))
        self.assertTrue(history.selected("project/mod/chessmod-1.20.1.jar"))
        self.assertTrue(history.selected("forge-1.14.4-28.2.0-mdk/mod/ChessMod-0.56.jar"))
        self.assertFalse(history.selected("project/gradle/wrapper/gradle-wrapper.jar"))
        self.assertFalse(history.selected("psd/chessboard.psd"))
        self.assertFalse(history.selected("project/src/main/java/Board.java"))

    def test_dry_run_against_temporary_git_bundle(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo = Path(tmp) / "source"
            repo.mkdir()
            def git(*args):
                return subprocess.run(["git", "-C", str(repo), *args],
                                      check=True, capture_output=True, text=True)
            git("init", "-q")
            git("config", "user.name", "Test")
            git("config", "user.email", "test@example.invalid")
            (repo / "README.md").write_text("sample repo\n")
            git("add", ".")
            git("commit", "-qm", "Initial")
            git("branch", "1.21")
            bundle = Path(tmp) / "snapshot.bundle"
            git("bundle", "create", str(bundle), "--all")
            with redirect_stdout(StringIO()) as stdout:
                self.assertEqual(history.main(["--bundle", str(bundle), "--dry-run"]), 0)
            self.assertIn("1.21", stdout.getvalue())
            self.assertFalse(list(Path(tmp).glob("ChessMod-history-*")))


if __name__ == "__main__":
    unittest.main()
