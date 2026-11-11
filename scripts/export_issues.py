#!/usr/bin/env python3
"""Export GitHub issues as reviewable Markdown plus a JSON snapshot. Python stdlib only."""

from __future__ import annotations

import argparse
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
from urllib.error import HTTPError, URLError
from urllib.parse import quote, urlencode, urlsplit
from urllib.request import Request, urlopen
import zipfile

ROOT = Path(__file__).resolve().parent.parent
API_ROOT = "https://api.github.com"
API_VERSION = "2022-11-28"
DEFAULT_REPO = "ramou/ChessMod"


def timestamp() -> str:
    return datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")


def slug(value: str, limit: int = 48) -> str:
    return re.sub(r"[^a-zA-Z0-9._-]+", "-", value).strip("-.")[:limit] or "selection"


def parse_since(value: str) -> str:
    """Normalise an ISO date/datetime for GitHub's since parameter."""
    try:
        date_value = value.replace("Z", "+00:00")
        parsed = datetime.fromisoformat(date_value)
        if parsed.tzinfo is None:
            parsed = parsed.replace(tzinfo=timezone.utc)
        return parsed.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    except ValueError as exc:
        raise argparse.ArgumentTypeError("expected ISO date or datetime, e.g. 2026-11-01") from exc


def positive_int(value: str) -> int:
    try:
        number = int(value)
    except ValueError as exc:
        raise argparse.ArgumentTypeError("expected a positive integer") from exc
    if number <= 0:
        raise argparse.ArgumentTypeError("must be greater than zero")
    return number


class ExportError(Exception):
    pass


class GitHubClient:
    def __init__(self, token: str | None = None, timeout: int = 30):
        self.token = token
        self.timeout = timeout

    def page(self, url: str) -> tuple[object, str | None]:
        target = urlsplit(url)
        if target.scheme != "https" or target.netloc != "api.github.com":
            raise ExportError("Refusing a GitHub pagination URL outside https://api.github.com")
        headers = {
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": API_VERSION,
            "User-Agent": "ChessMod-issues-export/1.0",
        }
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        try:
            with urlopen(Request(url, headers=headers), timeout=self.timeout) as response:
                data = json.load(response)
                link_header = response.headers.get("Link", "")
        except HTTPError as exc:
            remainder = exc.headers.get("X-RateLimit-Remaining", "unknown")
            raise ExportError(
                f"GitHub API returned HTTP {exc.code} ({exc.reason}); "
                f"rate-limit remaining: {remainder}. Check repo access/token."
            ) from exc
        except (URLError, TimeoutError, ValueError) as exc:
            raise ExportError(f"GitHub request failed: {exc}") from exc
        next_match = re.search(r'<([^>]+)>;\s*rel="next"', link_header)
        return data, next_match.group(1) if next_match else None

    def get(self, path: str, params: dict | None = None) -> object:
        url = f"{API_ROOT}{path}"
        if params:
            url += "?" + urlencode(params)
        data, next_url = self.page(url)
        if next_url:
            raise ExportError("Unexpected paginated response from a single-item endpoint")
        return data

    def list(self, path: str, params: dict | None = None, limit: int | None = None) -> list[dict]:
        url = f"{API_ROOT}{path}"
        params = {**(params or {}), "per_page": 100}
        url += "?" + urlencode(params)
        items: list[dict] = []
        while url:
            page, url = self.page(url)
            if not isinstance(page, list):
                raise ExportError("Expected an array from GitHub's list endpoint")
            items.extend(page)
            if limit is not None and len(items) >= limit:
                return items[:limit]
        return items



class GhClient:
    """Read GitHub REST endpoints with the locally authenticated gh CLI."""

    def __init__(self, timeout: int = 120):
        self.timeout = timeout
        if not shutil.which("gh"):
            raise ExportError("GitHub CLI (gh) not found on PATH; install/authenticate gh or use --transport rest")

    def _request(self, path: str, params: dict | None = None) -> object:
        # Use query parameters in the URL, not -f/-F: those switches cause gh
        # api to choose POST rather than GET unless --method GET is specified.
        endpoint = path + ("?" + urlencode(params) if params else "")
        command = ["gh", "api", "--method", "GET", "--header",
                   f"X-GitHub-Api-Version: {API_VERSION}"]
        command.append(endpoint)
        try:
            response = subprocess.run(command, capture_output=True, text=True,
                                      timeout=self.timeout, check=False)
        except FileNotFoundError as exc:
            raise ExportError("GitHub CLI (gh) not found on PATH") from exc
        except subprocess.TimeoutExpired as exc:
            raise ExportError(f"GitHub CLI request timed out after {self.timeout}s") from exc
        if response.returncode:
            detail = response.stderr.strip() or "unknown error"
            raise ExportError(f"gh api failed ({response.returncode}): {detail}. "
                              "Check 'gh auth status', access permissions and API rate limits.")
        try:
            return json.loads(response.stdout)
        except ValueError as exc:
            raise ExportError("gh api returned invalid JSON") from exc

    def get(self, path: str, params: dict | None = None) -> object:
        return self._request(path, params)

    def list(self, path: str, params: dict | None = None, limit: int | None = None) -> list[dict]:
        # Explicit page requests work with older gh versions that lack --slurp.
        # They also let us stop promptly when a caller specifies a limit.
        if limit is not None and limit <= 0:
            return []
        query = {**(params or {}), "per_page": 100}
        page_number = 1
        items: list[dict] = []
        while True:
            page = self._request(path, {**query, "page": page_number})
            if not isinstance(page, list) or any(not isinstance(item, dict) for item in page):
                raise ExportError("Expected an array of objects from gh api list endpoint")
            items.extend(page)
            if limit is not None and len(items) >= limit:
                return items[:limit]
            if len(page) < 100:
                return items
            page_number += 1


def compact_issue(issue: dict, comments: list[dict]) -> dict:
    def actor(value: dict | None) -> str | None:
        return value.get("login") if isinstance(value, dict) else None

    def milestone(value: dict | None) -> dict | None:
        if not value:
            return None
        return {key: value.get(key) for key in
                ("number", "title", "state", "description", "due_on", "closed_at")}

    return {
        "number": issue["number"],
        "kind": "pull_request" if "pull_request" in issue else "issue",
        "title": issue.get("title", ""),
        "body": issue.get("body") or "",
        "url": issue.get("html_url"),
        "state": issue.get("state"),
        "state_reason": issue.get("state_reason"),
        "created_at": issue.get("created_at"),
        "updated_at": issue.get("updated_at"),
        "closed_at": issue.get("closed_at"),
        "closed_by": actor(issue.get("closed_by")),
        "author": actor(issue.get("user")),
        "author_association": issue.get("author_association"),
        "assignees": [actor(a) for a in issue.get("assignees", [])],
        "labels": [{"name": x.get("name"), "color": x.get("color"),
                    "description": x.get("description")} for x in issue.get("labels", [])],
        "milestone": milestone(issue.get("milestone")),
        "locked": issue.get("locked", False),
        "comments_count_reported": issue.get("comments", 0),
        "comments": [{"id": c.get("id"), "author": actor(c.get("user")),
                      "created_at": c.get("created_at"), "updated_at": c.get("updated_at"),
                      "url": c.get("html_url"), "body": c.get("body") or ""}
                     for c in comments],
    }




def collect_catalogue(client: GitHubClient, repo: str) -> dict:
    """Capture repository-wide issue creation choices, independent of filters.

    GitHub uses *labels* for issue categorisation (not Git tags). Milestones
    are addressed by title with gh CLI, but include numbers for stable review.
    Organization issue types and Projects V2 are not universally available and
    are deliberately not assumed to be covered by this catalogue.
    """
    owner, repository = repo.split("/")
    base = f"/repos/{quote(owner)}/{quote(repository)}"
    repository_info = client.get(base)
    if not isinstance(repository_info, dict):
        raise ExportError("Expected an object from GitHub repository endpoint")
    labels = client.list(f"{base}/labels")
    milestones = client.list(f"{base}/milestones", {"state": "all"})
    return {
        "repository": {
            "full_name": repository_info.get("full_name", repo),
            "url": repository_info.get("html_url"),
            "description": repository_info.get("description"),
            "default_branch": repository_info.get("default_branch"),
            "has_issues": repository_info.get("has_issues"),
            "archived": repository_info.get("archived"),
            "disabled": repository_info.get("disabled"),
        },
        "labels": sorted(({
            "name": label.get("name"), "description": label.get("description"),
            "color": label.get("color"), "default": label.get("default"),
            "url": label.get("url"),
        } for label in labels), key=lambda x: (x["name"] or "").casefold()),
        "milestones": sorted(({
            "number": milestone.get("number"), "title": milestone.get("title"),
            "state": milestone.get("state"), "description": milestone.get("description"),
            "due_on": milestone.get("due_on"), "open_issues": milestone.get("open_issues"),
            "closed_issues": milestone.get("closed_issues"),
            "html_url": milestone.get("html_url"),
        } for milestone in milestones), key=lambda x: x["number"] or 0),
        "not_included": [
            "Git tags (not issue labels)",
            "GitHub Projects V2 and project-specific fields",
            "Organization issue types and forms",
            "Repository write permissions and user-specific assignee eligibility",
        ],
    }


def render_catalogue(catalogue: dict) -> str:
    def cell(value: object) -> str:
        if value is None or value == "":
            return "—"
        return str(value).replace("|", "\\|").replace("\n", " ")

    repo = catalogue["repository"]
    lines = ["# GitHub issue creation catalogue", "",
             "Snapshot of repository-wide options, independent of issue filters.",
             "Recheck GitHub before making any write: labels and milestones may change.", "",
             f"- Repository: {cell(repo['full_name'])}",
             f"- Default branch: {cell(repo['default_branch'])}",
             f"- Issues enabled: {cell(repo['has_issues'])}",
             f"- Archived: {cell(repo['archived'])}", "",
             f"## Labels ({len(catalogue['labels'])})", "",
             "| Label | Description | Colour |", "| --- | --- | --- |"]
    for label in catalogue["labels"]:
        lines.append(f"| {cell(label['name'])} | {cell(label['description'])} | {cell(label['color'])} |")
    lines.extend(["", f"## Milestones ({len(catalogue['milestones'])})", "",
                  "| # | Milestone title (`gh --milestone` argument) | State | Due | Description |",
                  "| --- | --- | --- | --- | --- |"])
    for milestone in catalogue["milestones"]:
        lines.append("| " + " | ".join(cell(milestone[k]) for k in
                     ("number", "title", "state", "due_on", "description")) + " |")
    lines.extend(["", "Not captured: " + "; ".join(catalogue["not_included"]) + ".", ""])
    return "\n".join(lines)


def render_markdown(issue: dict, includes_comments: bool) -> str:
    def printable(value: object) -> str:
        return str(value) if value is not None else "—"

    labels = ", ".join(label["name"] or "" for label in issue["labels"]) or "(none)"
    assignees = ", ".join(issue["assignees"]) or "(none)"
    lines = [
        f"# #{issue['number']}: {issue['title']}", "",
        f"- URL: {printable(issue['url'])}",
        f"- State: {printable(issue['state'])}",
        f"- State reason: {printable(issue['state_reason'])}",
        f"- Labels: {labels}",
        f"- Milestone: {issue['milestone']['title'] if issue['milestone'] else '(none)'}",
        f"- Author: {printable(issue['author'])}",
        f"- Assignees: {assignees}",
        f"- Created: {printable(issue['created_at'])}",
        f"- Updated: {printable(issue['updated_at'])}",
        f"- Closed: {printable(issue['closed_at'])}",
        "", "## Description", "", issue["body"] or "(no description)", "",
    ]
    if includes_comments:
        lines += [f"## Comments ({len(issue['comments'])})", ""]
        if not issue["comments"]:
            lines += ["(no comments)", ""]
        for comment in issue["comments"]:
            lines += [f"### {printable(comment['author'])} — {printable(comment['created_at'])}",
                      "", f"Source: {printable(comment['url'])}", "",
                      comment["body"] or "(empty comment)", ""]
    return "\n".join(lines).rstrip() + "\n"


def matches(issue: dict, args: argparse.Namespace) -> bool:
    if args.state != "all" and issue.get("state") != args.state:
        return False
    actual_labels = {v.get("name", "").casefold() for v in issue.get("labels", [])}
    if not all(label.casefold() in actual_labels for label in args.label):
        return False
    if args.milestone is not None:
        milestone = issue.get("milestone")
        if args.milestone == "none":
            if milestone:
                return False
        elif args.milestone == "any":
            if not milestone:
                return False
        elif not milestone or str(milestone.get("number")) != args.milestone:
            return False
    if args.since and (not issue.get("updated_at") or issue["updated_at"] < args.since):
        return False
    return True


def collect(client: GitHubClient, args: argparse.Namespace) -> list[dict]:
    owner, repository = args.repo.split("/")
    base = f"/repos/{quote(owner)}/{quote(repository)}/issues"
    selected: list[dict]
    if args.number:
        selected = []
        for number in sorted(set(args.number)):
            fetched = client.get(f"{base}/{number}")
            if not isinstance(fetched, dict):
                raise ExportError(f"Expected an issue object for #{number}")
            selected.append(fetched)
    else:
        params = {"state": args.state, "sort": "updated", "direction": "desc"}
        if args.label:
            params["labels"] = ",".join(args.label)
        if args.since:
            params["since"] = args.since
        if args.milestone and args.milestone not in ("none", "any"):
            params["milestone"] = args.milestone
        selected = client.list(base, params)

    # GitHub's issue listing includes pull requests: exclude them unless asked.
    selected = [issue for issue in selected
                if (args.include_prs or "pull_request" not in issue) and matches(issue, args)]
    if args.limit is not None:
        selected = selected[:args.limit]
    result = []
    for issue in sorted(selected, key=lambda i: i["number"]):
        number = issue["number"]
        comments = []
        if not args.no_comments and issue.get("comments", 0):
            comments = client.list(f"{base}/{number}/comments")
        result.append(compact_issue(issue, comments))
        if not args.quiet:
            print(f"Fetched #{number}: {issue.get('title', '')}", file=sys.stderr)
    return result


def export_to_zip(args: argparse.Namespace, client: GitHubClient | None = None) -> Path:
    client = client or (GhClient() if args.transport == "gh" else
                        GitHubClient(os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN")))
    issues = collect(client, args)
    catalogue = None if args.no_catalogue else collect_catalogue(client, args.repo)
    label_segment = "-" + "-".join(slug(label, 20) for label in args.label) if args.label else ""
    suffix = f"{args.state}{label_segment}-{timestamp()}"
    if args.source_commit:
        suffix += "-" + slug(args.source_commit[:12])
    output = args.output or args.output_dir / f"{slug(args.repo.replace('/', '-'))}-issues-{suffix}.zip"
    output = Path(output).expanduser().resolve()
    if output.exists():
        raise ExportError(f"Won't overwrite existing export: {output}")
    output.parent.mkdir(parents=True, exist_ok=True)
    manifest = {
        "schema_version": 2,
        "kind": "github_issue_snapshot",
        "repository": args.repo,
        "exported_at": datetime.now(timezone.utc).isoformat(),
        "source_commit": args.source_commit,
        "source_refs": args.source_ref,
        "source": "GitHub REST API: issues and issue comments (read-only)",
        "filters": {"state": args.state, "labels_all": args.label,
                    "milestone": args.milestone, "since": args.since,
                    "numbers": args.number, "include_prs": args.include_prs,
                    "limit": args.limit},
        "comments_included": not args.no_comments,
        "limit_requested": args.limit is not None,
        "may_be_partial_due_to_limit": args.limit is not None,
        "issue_count": len(issues),
        "catalogue_included": catalogue is not None,
        "issue_creation_catalogue": catalogue,
        "issues": issues,
    }
    documents = [("manifest.json", json.dumps(manifest, indent=2, ensure_ascii=False) + "\n")]
    if catalogue is not None:
        documents.append(("issue_catalogue.md", render_catalogue(catalogue)))
    documents.extend((f"issues/{issue['number']:06d}.md", render_markdown(issue, not args.no_comments))
                     for issue in issues)
    # Cap uncompressed output to avoid accidental huge exports; fail without partial ZIP.
    uncompressed_size = sum(len(content.encode("utf-8")) for _, content in documents)
    if uncompressed_size > args.max_mb * 1024 * 1024:
        raise ExportError(f"Export would be {uncompressed_size / 1048576:.1f} MiB uncompressed "
                          f"(limit {args.max_mb} MiB); narrow filters or raise --max-mb")
    try:
        with zipfile.ZipFile(output, "x", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
            for name, content in documents:
                archive.writestr(name, content)
    except Exception:
        output.unlink(missing_ok=True)
        raise
    return output


def make_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Export ChessMod GitHub issues to Markdown files and a JSON manifest inside one ZIP.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""Examples:
  python scripts/export_issues.py
  python scripts/export_issues.py --state open --label bug --since 2026-11-01
  python scripts/export_issues.py --number 13 --number 84
  python scripts/export_issues.py --no-comments --limit 10
  python scripts/export_issues.py --number 13  # still includes ALL available labels/milestones
  python scripts/export_issues.py --no-catalogue  # smaller snapshot

Uses locally authenticated gh CLI by default (run gh auth login first).
Use --transport rest for direct REST API access with GH_TOKEN/GITHUB_TOKEN,
or anonymously for public repositories (subject to lower rate limits).
Snapshots are read-only and do not record the full issue-state change history.
A repository-wide label/milestone catalogue is included by default, independently
of issue filters; --no-catalogue omits it. Projects and organisation issue types
are not captured. Verify choices live before running a proposed gh write script.
Defaults to all open and closed issues, including all their comments, but not PRs.
""",
    )
    parser.add_argument("--transport", choices=("gh", "rest"), default="gh",
                        help="GitHub API transport (default: authenticated gh CLI; rest: direct urllib)")
    parser.add_argument("--repo", default=DEFAULT_REPO, metavar="OWNER/REPO", help=f"GitHub repository (default: {DEFAULT_REPO})")
    parser.add_argument("--state", choices=("all", "open", "closed"), default="all", help="Issue state to include (default: all)")
    parser.add_argument("--label", action="append", default=[], metavar="NAME", help="Require this label; repeat for AND filtering")
    parser.add_argument("--milestone", metavar="NUMBER|any|none", help="Limit by milestone number or presence/absence")
    parser.add_argument("--since", type=parse_since, metavar="ISO_DATE", help="Only issues updated since this UTC date/time")
    parser.add_argument("--number", type=positive_int, action="append", metavar="N", help="Export a specific issue; repeat to select several")
    parser.add_argument("--limit", type=positive_int, metavar="N", help="Maximum selected issues (explicitly partial export)")
    parser.add_argument("--no-comments", action="store_true", help="Skip fetching issue comments (faster, less context)")
    parser.add_argument("--include-prs", action="store_true", help="Also export pull requests returned by the issues API")
    parser.add_argument("--no-catalogue", action="store_true", help="Omit repository-wide issue label/milestone catalogue (included by default)")
    parser.add_argument("--output-dir", type=Path, default=ROOT / ".ai-exports", metavar="DIR", help="Directory for timestamped ZIP (default: .ai-exports/)")
    parser.add_argument("--output", type=Path, metavar="ZIP", help="Exact output filename (used by the context coordinator)")
    parser.add_argument("--source-commit", metavar="SHA", help="Optional corresponding Git commit recorded in manifest")
    parser.add_argument("--source-ref", action="append", default=[], metavar="BRANCH=SHA", help="Associated portable bundle branch mapping (context coordinator)")
    parser.add_argument("--max-mb", type=positive_int, default=64, metavar="N", help="Max uncompressed ZIP content size in MiB (default: 64)")
    parser.add_argument("--quiet", action="store_true", help="Suppress per-issue progress messages")
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = make_parser()
    args = parser.parse_args(argv)
    if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", args.repo):
        parser.error("--repo must be OWNER/REPO")
    if args.milestone not in (None, "any", "none") and not args.milestone.isdigit():
        parser.error("--milestone must be an integer, 'any', or 'none'")
    try:
        output = export_to_zip(args)
    except (ExportError, OSError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1
    print(f"Created {output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
