#!/usr/bin/env python3
"""ChessMod release manager (#205). Standard-library only. No unattended publishing.

Subcommands:
  preflight (default): read-only inventory / JDK discovery / version + remote checks.
  verify:               all-target Java+Gradle preflight, then isolated builds.
  draft --apply:        verified artifacts -> annotated tag + GitHub draft only.
  publish --apply:      one reviewed draft -> publish -> shared update-feed commit.

See docs/RELEASE_TOOL.md for contracts, recovery and examples.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
import zipfile
from dataclasses import dataclass
from datetime import datetime, timezone


ROOT = Path(__file__).resolve().parents[1]
REGISTRY = Path(__file__).with_name("release_targets.json")
SHA_RE = re.compile(r"[0-9a-f]{40}\Z")
NAME_RE = re.compile(r"[A-Za-z0-9_.-]+\Z")
JAVA_RE = re.compile(r'(?:openjdk|java|javac)(?: version)?\s+"?(1\.\d+|\d+)(?:[._+\-\s"]|$)', re.I)


class ReleaseError(Exception):
    pass


def require(condition, message):
    if not condition:
        raise ReleaseError(message)


def command(args, *, cwd=ROOT, env=None, check=True, capture=True):
    proc = subprocess.run([str(a) for a in args], cwd=cwd, env=env,
                          text=True, stdout=subprocess.PIPE if capture else None,
                          stderr=subprocess.PIPE if capture else None,
                          errors="replace")
    if check and proc.returncode:
        msg = ((proc.stdout or "") + "\n" + (proc.stderr or "")).strip()
        raise ReleaseError(f"Command failed ({proc.returncode}): {' '.join(map(str, args))}\n{msg[-2500:]}")
    return proc


def git(*args, check=True):
    return command(["git", *args], check=check).stdout.strip()


def gh(*args, check=True):
    return command(["gh", *args], check=check)


def utc_now():
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat()


def sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as src:
        for chunk in iter(lambda: src.read(2**20), b""):
            digest.update(chunk)
    return digest.hexdigest()


def load_registry(path=REGISTRY):
    data = json.loads(Path(path).read_text(encoding="utf8"))
    require(data.get("schema") == 1 and isinstance(data.get("targets"), list), "Unsupported release registry")
    require(re.fullmatch(r"[\w.-]+/[\w.-]+", data.get("repository", "")), "Invalid GitHub repository")
    defaults = data.get("defaults", {})
    require(isinstance(defaults, dict), "Registry defaults must be an object")
    required_paths = ("project_dir", "mod_metadata", "wrapper_properties", "jar_glob", "licence_path")
    for field in required_paths:
        require(isinstance(defaults.get(field),str) and defaults[field] and
                not Path(defaults[field]).is_absolute() and ".." not in Path(defaults[field]).parts,
                f"Missing or unsafe registry path: {field}")
    for field in ("test_task", "build_task"):
        require(re.fullmatch(r"[A-Za-z][\w-]*", defaults.get(field, "")), f"Unsafe Gradle task: {field}")
    ids = set(); branches = set()
    for i, raw in enumerate(data["targets"]):
        target = {**defaults, **raw}
        data["targets"][i] = target
        for field in ("id", "branch", "minecraft", "loader", "gradle"):
            require(isinstance(target.get(field), str) and NAME_RE.fullmatch(target[field]), f"Invalid target {field}: {target}")
        for field in ("gradle_jvm", "compiler_jdk"):
            require(type(target.get(field)) is int and target[field] >= 8, f"Invalid Java requirement: {target}")
        require(target["id"] not in ids and target["branch"] not in branches, "Duplicate target/branch")
        require(target["loader"] == "forge" or target.get("release_eligible") is False,
                "Unsupported loader must be explicitly ineligible until its adapter is implemented")
        ids.add(target["id"]); branches.add(target["branch"])
    require(data.get("update_feed_branch") in branches, "Update-feed branch must be configured")
    require(data.get("update_feed_path") == "project/mod/update.json", "Unexpected update-feed path")
    return data


def select_targets(registry, names):
    eligible = [t for t in registry["targets"] if t.get("release_eligible") is True]
    if not names:
        return eligible
    wanted = set(names)
    unknown = wanted - {t["id"] for t in eligible}
    require(not unknown, f"Unknown or ineligible targets: {', '.join(sorted(unknown))}")
    return [t for t in eligible if t["id"] in wanted]


def git_show(commit, path):
    require(SHA_RE.fullmatch(commit), "Invalid Git commit identifier")
    return git("show", f"{commit}:{path}")


def parse_properties(data):
    result = {}
    for line in data.splitlines():
        line = line.strip()
        if not line or line[0] in "#!":
            continue
        m = re.match(r"([^:=\s]+)\s*[=:]\s*(.*)", line)
        if m:
            result[m.group(1)] = m.group(2).strip()
    return result


def parse_branch_config(commit, target):
    props = parse_properties(git_show(commit, "project/gradle.properties")) if exists(commit, "project/gradle.properties") else {}
    build = git_show(commit, "project/build.gradle")
    wrapper = git_show(commit, target["wrapper_properties"])
    gradle_match = re.search(r"gradle-([0-9]+(?:\.[0-9]+){1,2})-(?:bin|all)\.zip", wrapper)
    require(gradle_match and gradle_match.group(1) == target["gradle"], f"{target['id']}: Gradle registry mismatch")
    version = props.get("mod_version")
    if not version:
        m = re.search(r"(?m)^\s*version\s*=\s*['\"]([^'\"]+)['\"]", build)
        require(m, f"{target['id']}: Cannot resolve committed Gradle mod version")
        version = m.group(1)
    require(re.fullmatch(re.escape(target["minecraft"]) + r"-[0-9]+(?:\.[0-9]+)*", version),
            f"{target['id']}: Invalid/foreign mod version {version}")
    if "minecraft_version" in props:
        require(props["minecraft_version"] == target["minecraft"], f"{target['id']}: Minecraft property mismatch")
    if "JavaLanguageVersion.of(" in build:
        j = re.search(r"JavaLanguageVersion\.of\((\d+)\)", build)
        require(j and int(j.group(1)) == target["compiler_jdk"], f"{target['id']}: Java toolchain differs from registry")
    else:
        j = re.search(r"sourceCompatibility\s*=.*?['\"]1\.(\d+)['\"]", build)
        require(j and int(j.group(1)) == target["compiler_jdk"], f"{target['id']}: Java source compatibility differs from registry")
    forge = props.get("forge_version")
    if not forge:
        m = re.search(r"net\.minecraftforge:forge:" + re.escape(target["minecraft"]) + r"-([0-9.]+)", build)
        require(m, f"{target['id']}: Unable to identify Forge version")
        forge = m.group(1)
    raw_toml = git_show(commit, target["mod_metadata"])
    metadata = parse_mod_metadata(raw_toml)
    declared = metadata["version"].replace("${mod_version}", version)
    mod_id = metadata["modId"].replace("${mod_id}", props.get("mod_id", ""))
    require(declared == version and mod_id == "chessmod", f"{target['id']}: mods.toml disagrees with Gradle version/modId")
    require(exists(commit, target["licence_path"]), f"{target['id']}: LICENSE missing")
    license_text = git_show(commit, target["licence_path"])
    require("MIT" in license_text or "Permission is hereby granted" in license_text,
            f"{target['id']}: Licence requires explicit review")
    return {"version": version, "forge_version": forge, "gradle": gradle_match.group(1),
            "licence_path": target["licence_path"],
            "java_launcher": target["gradle_jvm"], "java_compiler": target["compiler_jdk"],
            "source_sha": commit, "metadata": metadata}


def parse_mod_metadata(text):
    # Only the first [[mods]] block is authoritative for the mod's version.
    m = re.search(r"(?m)^\s*\[\[mods\]\](?:\s*#.*)?\s*$", text)
    require(m, "No [[mods]] in mods.toml")
    part = re.split(r"(?m)^\s*\[\[", text[m.end():], maxsplit=1)[0]
    result = {}
    for key in ("modId", "version", "license"):
        x = re.search(r"(?m)^\s*" + re.escape(key) + r"\s*=\s*['\"]([^'\"]+)['\"]", part)
        if x:
            result[key] = x.group(1)
    require("modId" in result and "version" in result, "Missing modId/version in mods.toml")
    return result


def exists(commit, path):
    return command(["git", "cat-file", "-e", f"{commit}:{path}"], check=False).returncode == 0


def remote_head(branch, remote):
    out = git("ls-remote", "--heads", remote, f"refs/heads/{branch}")
    lines = out.splitlines()
    require(len(lines) == 1 and lines[0].split()[1] == f"refs/heads/{branch}",
            f"Remote branch missing/ambiguous: {branch}")
    sha = lines[0].split()[0]
    require(SHA_RE.fullmatch(sha), "Invalid remote commit")
    return sha


def local_head(branch):
    return git("rev-parse", "--verify", f"refs/heads/{branch}^{{commit}}")


def remote_tag(tag, remote):
    """Return the commit SHA of a remote tag (annotated or lightweight).

    `git ls-remote` outputs SHA followed by ref name. An annotated tag has
    both the tag-object ref and a peeled `^{}` commit ref. Never compare its
    tag-object SHA to the source commit, and never silently accept bad output.
    """
    direct = f"refs/tags/{tag}"
    peeled = f"{direct}^{{}}"
    out = git("ls-remote", "--tags", remote, direct, peeled)
    refs = {}
    for line in out.splitlines():
        if not line.strip():
            continue
        fields = line.split()
        require(len(fields) == 2, f"Malformed remote tag listing for {tag}: {line!r}")
        sha, ref = fields  # ls-remote reports SHA first, then ref.
        require(SHA_RE.fullmatch(sha) and ref in (direct, peeled),
                f"Unexpected remote tag listing for {tag}: {line!r}")
        require(ref not in refs, f"Duplicate remote ref for {tag}: {ref}")
        refs[ref] = sha
    require(peeled not in refs or direct in refs,
            f"Remote tag {tag} has a peeled commit but no tag-object ref")
    return refs.get(peeled) or refs.get(direct)


def tag_for(target, version):
    suffix = version[len(target["minecraft"]) + 1:]
    return f"mc{target['minecraft']}-{target['loader']}-v{suffix}"


def release_version(value):
    """A requested suffix (0.702), NEVER an implicit source modification."""
    if value is None:
        return None
    require(isinstance(value, str) and re.fullmatch(r"(?:0|[1-9][0-9]*)(?:\.(?:0|[1-9][0-9]*))*", value),
            "Invalid --version; supply a numeric mod-version suffix such as 0.702")
    return value


def require_requested_versions(cfgs, requested):
    """Check the entire selected set before Gradle or any remote write."""
    if requested is None:
        return
    mismatches = [f"{cfg['id']}: declared {cfg['version']}, expected {cfg['minecraft']}-{requested}"
                  for cfg in cfgs.values()
                  if cfg['version'] != f"{cfg['minecraft']}-{requested}"]
    require(not mismatches,
            "Requested release version mismatch; commit/push reviewed version bumps first.\n  " +
            "\n  ".join(mismatches))


def repo_guard(registry, remote):
    url = git("remote", "get-url", remote)
    expected = registry["repository"]
    # Refuse a remote pointing to a fork or a local directory.
    require(url.endswith("/" + expected + ".git") or url.endswith("/" + expected) or
            url.endswith(":" + expected + ".git") or url.endswith(":" + expected),
            f"Remote {remote} points to {url}, expected {expected}")


def inspect_target(target, remote):
    remote_sha = remote_head(target["branch"], remote)
    local_sha = local_head(target["branch"])
    require(remote_sha == local_sha,
            f"{target['id']}: local branch {local_sha[:12]} differs from remote {remote_sha[:12]}; push/fetch and review before release")
    cfg = parse_branch_config(local_sha, target)
    cfg.update({"id":target["id"], "branch":target["branch"], "loader":target["loader"],
                "minecraft":target["minecraft"], "tag":tag_for(target,cfg["version"])})
    return cfg


@dataclass(frozen=True)
class JavaHome:
    path: Path
    major: int
    version: str


def java_exe(home, tool):
    suffix = ".exe" if os.name == "nt" else ""
    return home / "bin" / (tool + suffix)


def parse_java_major(text):
    m = JAVA_RE.search(text.strip())
    if not m:
        return None
    v = m.group(1)
    return int(v.split(".")[1] if v.startswith("1.") else v.split(".")[0])


def probe_java(path):
    home = Path(path).expanduser().resolve()
    if home.name == "bin" and (home / "java").is_file():
        home = home.parent
    java, javac = java_exe(home, "java"), java_exe(home, "javac")
    if not java.is_file() or not javac.is_file():
        return None
    try:
        j = command([java, "-version"], check=False)
        c = command([javac, "-version"], check=False)
    except OSError:
        return None
    if j.returncode or c.returncode:
        return None
    jinfo = (j.stderr or "") + (j.stdout or "")
    cinfo = (c.stderr or "") + (c.stdout or "")
    major = parse_java_major(jinfo)
    if major is None or parse_java_major(cinfo) != major:
        return None
    return JavaHome(home, major, jinfo.splitlines()[0])


def java_candidates(extra_dirs, explicit_paths):
    candidates = []
    candidates.extend(explicit_paths)
    for root in extra_dirs:
        p = Path(root).expanduser()
        candidates.append(p)
        if p.is_dir():
            candidates.extend(sorted(q for q in p.iterdir() if q.is_dir()))
    for name, val in os.environ.items():
        if name in {"JAVA_HOME", "JDK_HOME"} or re.fullmatch(r"(?:JAVA|JDK)(?:_HOME)?_?(?:8|16|17|21)(?:_HOME)?", name):
            if val: candidates.append(Path(val))
    javac = shutil.which("javac")
    if javac: candidates.append(Path(javac).resolve().parent.parent)
    sysroots = [Path("/usr/lib/jvm"), Path("/opt/java"), Path("/opt/jdks"),
                Path("/Library/Java/JavaVirtualMachines")]
    if os.name == "nt":
        sysroots.extend(Path(os.environ.get("ProgramFiles", "C:/Program Files")) / root
                        for root in ("Java", "Eclipse Adoptium", "Microsoft", "Zulu"))
    for root in sysroots:
        if not root.is_dir(): continue
        candidates.extend(sorted(q for q in root.iterdir() if q.is_dir()))
        if root.name == "JavaVirtualMachines":
            candidates.extend(q / "Contents" / "Home" for q in root.iterdir() if q.is_dir())
    unique = []
    seen = set()
    for c in candidates:
        try: resolved = Path(c).expanduser().resolve()
        except OSError: continue
        if resolved not in seen:
            unique.append(resolved); seen.add(resolved)
    return unique


def discover_java(targets, explicit, extra_dirs, *, probe=probe_java):
    """Find exact-major launcher and compiler JDKs before ANY Gradle work."""
    overrides = {}
    for pair in explicit:
        match = re.fullmatch(r"(\d+|[\w.\-]+:(?:launcher|compiler))=(.+)", pair)
        require(match, f"Expected --java-home MAJOR=PATH or TARGET:launcher=PATH: {pair}")
        key, path = match.groups()
        require(key not in overrides, f"Repeated Java override: {key}")
        overrides[key] = Path(path)
    candidates = java_candidates(extra_dirs, list(overrides.values()))
    found = {}
    for path in candidates:
        home = probe(path)
        if home is not None:
            found[home.path] = home
    homes = list(found.values())
    assignments, failures = {}, []
    for target in targets:
        pair = {}
        for role, field in (("launcher", "gradle_jvm"), ("compiler", "compiler_jdk")):
            required = target[field]
            specific = overrides.get(f"{target['id']}:{role}")
            override = specific or overrides.get(str(required))
            if override:
                resolved = Path(override).expanduser().resolve()
                match = found.get(resolved)
                if not match or match.major != required:
                    failures.append(f"{target['id']} {role}: requires JDK {required}, override {resolved} is missing/incompatible")
                    continue
                pair[role] = match
            else:
                matching = [h for h in homes if h.major == required]
                if not matching:
                    failures.append(f"{target['id']} {role}: requires JDK {required} (use --java-home {required}=PATH or --java-homes-dir DIR)")
                    continue
                pair[role] = matching[0]
        if len(pair) == 2:
            assignments[target["id"]] = pair
    return assignments, failures


def gradle_env(javas):
    env = os.environ.copy()
    home = str(javas["launcher"].path)
    env["JAVA_HOME"] = home
    env["PATH"] = str(javas["launcher"].path / "bin") + os.pathsep + env.get("PATH", "")
    return env


def gradle_command(worktree, javas, *tasks):
    project = worktree / "project"
    script = ["cmd.exe", "/c", "gradlew.bat"] if os.name == "nt" else ["bash", "gradlew"]
    # Gradle can locate the toolchain JDK without modifying user-wide Java settings.
    paths = ",".join(sorted({str(javas["launcher"].path), str(javas["compiler"].path)}))
    args = [*script, *tasks, "--console=plain", "--no-daemon",
            f"-Dorg.gradle.java.installations.paths={paths}"]
    return args, project, gradle_env(javas)


def probe_gradle(target, javas, sha):
    """Check wrapper launcher startup before ANY verify/build task begins."""
    with tempfile.TemporaryDirectory(prefix=f"chessmod-gradle-{target['id']}-") as td:
        path = Path(td) / "source"
        git("worktree", "add", "--quiet", "--detach", str(path), sha)
        try:
            args, cwd, env = gradle_command(path, javas, "--version")
            result = command(args, cwd=cwd, env=env)
            match = re.search(r"(?m)^Gradle\s+([0-9.]+)", result.stdout)
            require(match and match.group(1) == target["gradle"],
                    f"{target['id']}: Gradle wrapper started but reported unexpected version")
        finally:
            command(["git", "worktree", "remove", "--force", str(path)], check=False)


def tests_from_xml(worktree):
    reports = sorted((worktree / "project/build/test-results/test").glob("TEST-*.xml"))
    total = fail = 0
    for report in reports:
        root = ET.parse(report).getroot()
        suites = [root] if root.tag == "testsuite" else root.findall("testsuite")
        for suite in suites:
            total += int(suite.get("tests", "0"))
            fail += int(suite.get("failures", "0")) + int(suite.get("errors", "0"))
    return total, fail, len(reports)


def inspect_jar(jar, cfg):
    require(zipfile.is_zipfile(jar), f"Invalid JAR: {jar}")
    with zipfile.ZipFile(jar) as z:
        try: text = z.read("META-INF/mods.toml").decode("utf8")
        except KeyError: raise ReleaseError(f"No META-INF/mods.toml in {jar}")
        metadata = parse_mod_metadata(text)
        require(metadata["version"] == cfg["version"] and metadata["modId"] == "chessmod",
                f"Packaged mod metadata mismatch: {metadata} != {cfg['version']}")
        require(not any(n.lower().endswith(("optifine.jar", ".exe")) for n in z.namelist()),
                "Unexpected executable/third-party asset in JAR")
    require(cfg["version"].lower() in jar.name.lower(), f"JAR filename {jar.name} doesn't include version {cfg['version']}")


def make_launcher_zip(destination, jar, cfg, source):
    """Minimal Prism/MultiMC importable instance; never copy legacy binary packs."""
    label = "ChessMod-" + cfg["version"]
    pack = {"formatVersion":1, "components":[
        {"uid":"net.minecraft", "version":cfg["minecraft"], "important":True},
        {"uid":"net.minecraftforge", "version":cfg["forge_version"],
         "cachedRequires":[{"uid":"net.minecraft", "equals":cfg["minecraft"]}]}
    ]}
    config = f"InstanceType=OneSix\nname={label}\niconKey=default\n"
    with zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as zipout:
        zipout.write(jar, f"{label}/.minecraft/mods/{jar.name}")
        zipout.writestr(f"{label}/mmc-pack.json", json.dumps(pack, indent=2) + "\n")
        zipout.writestr(f"{label}/instance.cfg", config)
        zipout.write(source / cfg.get("licence_path", "LICENSE"), f"{label}/LICENSE")
    with zipfile.ZipFile(destination) as z:
        names = z.namelist()
        require(len([n for n in names if n.endswith(".jar")]) == 1, "Unexpected JAR in launcher ZIP")
        packed = json.loads(z.read(f"{label}/mmc-pack.json"))
        require(packed["components"][0]["version"] == cfg["minecraft"], "Minecraft launcher version mismatch")


def write_manifest(out, cfg, javas, names, tests, release_issue=None):
    files = {p.name: {"sha256":sha256(p), "bytes":p.stat().st_size} for p in names}
    manifest = {"schema":1, "created":utc_now(), "repository":"ramou/ChessMod",
                "release_issue":release_issue,
                "target":cfg["id"], "branch":cfg["branch"], "minecraft":cfg["minecraft"],
                "loader":cfg["loader"], "mod_version":cfg["version"], "forge":cfg["forge_version"],
                "gradle":cfg["gradle"], "tag":cfg["tag"], "source_sha":cfg["source_sha"],
                "tests":tests, "runtime_smoke_test":"NOT AUTOMATED; MAINTAINER REVIEW REQUIRED",
                "java":{"launcher":{"home":str(javas["launcher"].path),"major":javas["launcher"].major},
                        "compiler":{"home":str(javas["compiler"].path),"major":javas["compiler"].major}},
                "assets":files}
    path = out / "PROVENANCE.json"
    path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf8")
    checksums = out / "SHA256SUMS"
    checksums.write_text("".join(f"{hash['sha256']}  {name}\n" for name,hash in sorted(files.items())), encoding="utf8")
    notes = out / "RELEASE_NOTES.md"
    notes.write_text(f"ChessMod {cfg['version']} for Minecraft {cfg['minecraft']} (Forge {cfg['forge_version']})\n\n"
                     f"Source commit: `{cfg['source_sha']}`\n"
                     f"JUnit: {tests['executed']} tests, {tests['failures']} failures.\n"
                     "Installation: place the JAR in the matching Forge mods folder, or import the optional Prism/MultiMC ZIP.\n\n"
                     "Before publication, review gameplay smoke tests, open gameplay issues, and asset licensing.\n"
                     "See PROVENANCE.json and SHA256SUMS for exact build inputs and hashes.\n", encoding="utf8")
    return manifest


def validate_local_manifest(out, cfg):
    path = out / "PROVENANCE.json"
    require(path.is_file(), f"Missing verified release record {path}; run verify first")
    data = json.loads(path.read_text(encoding="utf8"))
    for key,expected in (("schema",1),("repository","ramou/ChessMod"),("target",cfg["id"]),
                         ("branch",cfg["branch"]),("source_sha",cfg["source_sha"]),
                         ("mod_version",cfg["version"]),("tag",cfg["tag"])):
        require(data.get(key) == expected, f"Verified release manifest mismatch: {key}")
    require(int(data.get("tests",{}).get("executed",0)) > 0 and
            int(data["tests"].get("failures",-1)) == 0, "No successful, nonzero tests in manifest")
    assets = data.get("assets",{})
    require(any(name.lower().endswith(".jar") for name in assets) and
            all(NAME_RE.fullmatch(name) for name in assets), "Missing/unsafe release assets")
    for name,item in assets.items():
        p = out / name
        require(p.is_file() and sha256(p) == item["sha256"] and p.stat().st_size == item["bytes"],
                f"Artifact modified or missing: {p}")
    for extra in ("PROVENANCE.json", "SHA256SUMS", "RELEASE_NOTES.md"):
        require((out / extra).is_file(), f"Required release record missing: {extra}")
    recorded = (out / "SHA256SUMS").read_text(encoding="utf8")
    require(recorded == "".join(f"{v['sha256']}  {k}\n" for k,v in sorted(assets.items())),
            "SHA256SUMS does not agree with provenance")
    return data


def release_dir(base, cfg):
    return Path(base).resolve() / cfg["id"] / cfg["tag"]


def verify_targets(targets, cfgs, assignments, base, release_issue=None):
    """Call only AFTER all JDK and Gradle launcher preflights passed."""
    for target in targets:
        cfg = cfgs[target["id"]]
        java = assignments[target["id"]]
        dest = release_dir(base, cfg)
        require(not dest.exists() or not any(dest.iterdir()),
                f"Output already exists: {dest}. Never overwrite verified artefacts; archive/delete it explicitly first")
        dest.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="chessmod-release-") as td:
            wt = Path(td) / "repo"
            git("worktree", "add", "--quiet", "--detach", str(wt), cfg["source_sha"])
            try:
                args,cwd,env = gradle_command(wt,java,"clean",target["test_task"],target["build_task"])
                print(f"BUILD {target['id']}: {cfg['source_sha'][:12]} ({cfg['version']})", flush=True)
                # Stream full Gradle diagnostics to user's terminal instead of swallowing test logs.
                command(args,cwd=cwd,env=env,capture=False)
                executed,failed,reports = tests_from_xml(wt)
                require(reports > 0 and executed > 0 and failed == 0,
                        f"{target['id']}: Tests not executed, missing reports, or failures: {executed}/{failed} ({reports} reports)")
                jars = [p for p in wt.glob(target["jar_glob"])
                        if p.is_file() and not p.name.endswith(("-sources.jar","-javadoc.jar","-dev.jar"))]
                require(len(jars)==1,f"{target['id']}: expected exactly one release JAR, found {[p.name for p in jars]}")
                inspect_jar(jars[0],cfg)
                outjar = dest / jars[0].name
                shutil.copy2(jars[0],outjar)
                files = [outjar]
                if target.get("launcher_zip"):
                    zipout = dest / f"ChessMod-{cfg['version']}.zip"
                    make_launcher_zip(zipout,outjar,cfg,wt)
                    files.append(zipout)
                write_manifest(dest,cfg,java,files,{"executed":executed,"failures":failed,"reports":reports},release_issue)
                print(f"VERIFIED {target['id']}: {executed} tests, {', '.join(p.name for p in files)}",flush=True)
            finally:
                command(["git","worktree","remove","--force",str(wt)],check=False)


def github_guard(repository):
    require(shutil.which("gh"), "GitHub CLI not found; install/login before online release preflight")
    auth = gh("auth","status",check=False)
    require(auth.returncode == 0,"GitHub CLI not authenticated: gh auth login")
    repo = gh("repo","view",repository,"--json","nameWithOwner")
    actual = json.loads(repo.stdout).get("nameWithOwner", "")
    require(actual.lower() == repository.lower(), f"GitHub CLI repository mismatch: {actual}")


def release_issue_guard(number, repository, cfgs):
    """Require an explicit release issue and source version-upgrade commit before writing."""
    require(type(number) is int and number > 0, "Draft/publish requires --release-issue NUMBER")
    detail = gh("issue", "view", str(number), "--repo", repository,
                "--json", "number,title,body,state")
    item = json.loads(detail.stdout)
    require(item.get("number") == number, "Release issue number mismatch")
    require(item.get("state") in ("OPEN", "CLOSED"), "Release issue has unexpected state")
    combined = item.get("title", "") + "\n" + item.get("body", "")
    for cfg in cfgs:
        release_number = cfg["version"].removeprefix(cfg["minecraft"] + "-")
        require(re.search(r"(?<![0-9.])" + re.escape(release_number) + r"(?![0-9.])", combined),
                f"#{number} does not appear to authorise mod version {cfg['version']}")
        logs = git("log", "-n", "80", "--format=%B", cfg["source_sha"], "--",
                   "project/gradle.properties", "project/build.gradle")
        require(re.search(r"\b(?:refs|fixes|closes)\s+#" + str(number) + r"\b", logs, re.I),
                f"{cfg['id']}: source version-change history does not reference issue #{number}")


def release_view(tag, repository):
    result = gh("release","view",tag,"--repo",repository,"--json","tagName,isDraft,isPrerelease,assets,url",check=False)
    if result.returncode:
        message = (result.stderr or "").lower()
        require("release not found" in message or "no release found" in message or "http 404" in message,
                f"Failed to inspect GitHub release {tag}: {result.stderr}")
        return None
    return json.loads(result.stdout)


def remote_assets_match(tag, repository, path, manifest):
    """Download each GitHub asset and compare exact bytes; never trust filenames alone."""
    expect = {**manifest["assets"]}
    for n in ("PROVENANCE.json", "SHA256SUMS"):
        p = path / n
        expect[n] = {"sha256":sha256(p),"bytes":p.stat().st_size}
    with tempfile.TemporaryDirectory(prefix="chessmod-release-audit-") as td:
        for name, recorded in expect.items():
            gh("release","download",tag,"--repo",repository,"--pattern",name,"--dir",td)
            p = Path(td) / name
            require(p.is_file() and sha256(p) == recorded["sha256"] and p.stat().st_size == recorded["bytes"],
                    f"Remote release asset mismatch {tag}: {name}")
    return expect


def published_release_matches_head(cfg, view, repository, local_output=None, release_issue=None):
    """Proof for an existing-release no-op: tag/source, provenance and ALL asset bytes.

    A remote provenance file alone is NOT enough. Recompute hashes of every
    downloaded release asset, compare against the published SHA256SUMS, and
    optionally cross-check any local verified output. Missing proof is a hard
    failure, never a permission to overwrite a published release.
    """
    tag = cfg["tag"]
    require(view.get("isDraft") is False and view.get("isPrerelease") is False,
            f"Existing release {tag} is not an ordinary published release")
    require(view.get("tagName") == tag, f"Published release tag mismatch: {tag}")
    names = [a.get("name") for a in view.get("assets", [])]
    require(all(isinstance(n, str) and NAME_RE.fullmatch(n) and n not in (".", "..")
                for n in names) and len(set(names)) == len(names),
            f"Unsafe or duplicate published asset names: {tag}")
    require({"PROVENANCE.json", "SHA256SUMS"}.issubset(names),
            f"Published release {tag} lacks verifiable provenance/checksums; manual review required")
    with tempfile.TemporaryDirectory(prefix="chessmod-published-audit-") as td:
        out = Path(td)
        for name in names:
            gh("release", "download", tag, "--repo", repository,
               "--pattern", name, "--dir", str(out))
            require((out / name).is_file(), f"Published release asset missing: {tag}/{name}")
        try:
            payload = json.loads((out / "PROVENANCE.json").read_text(encoding="utf8"))
        except (ValueError, UnicodeError) as exc:
            raise ReleaseError(f"Invalid published provenance for {tag}: {exc}") from exc
        identity = {"schema":1, "repository":repository, "target":cfg["id"],
                    "branch":cfg["branch"], "minecraft":cfg["minecraft"],
                    "loader":cfg["loader"], "mod_version":cfg["version"],
                    "tag":tag, "source_sha":cfg["source_sha"],
                    "forge":cfg["forge_version"], "gradle":cfg["gradle"]}
        for field, expected in identity.items():
            require(payload.get(field) == expected,
                    f"Published release {tag} provenance conflict: {field}")
        if release_issue is not None:
            require(payload.get("release_issue") == release_issue,
                    f"Published release {tag} belongs to another release issue")
        tests = payload.get("tests", {})
        require(isinstance(tests, dict) and type(tests.get("executed")) is int and
                tests["executed"] > 0 and tests.get("failures") == 0,
                f"Published release {tag} has invalid test provenance")
        assets = payload.get("assets")
        require(isinstance(assets, dict) and assets,
                f"Published release {tag} has no recorded assets")
        require(set(assets) | {"PROVENANCE.json", "SHA256SUMS"} == set(names),
                f"Published release {tag} asset set differs from provenance")
        require(sum(n.lower().endswith(".jar") for n in assets) == 1,
                f"Published release {tag} needs exactly one mod JAR")
        if cfg.get("launcher_zip"):
            require(sum(n.lower().endswith(".zip") for n in assets) == 1,
                    f"Published release {tag} missing launcher ZIP")
        checksums = []
        for name, info in sorted(assets.items()):
            require(NAME_RE.fullmatch(name) and isinstance(info, dict) and
                    isinstance(info.get("sha256"), str) and
                    re.fullmatch(r"[0-9a-f]{64}", info["sha256"]) and
                    type(info.get("bytes")) is int and info["bytes"] >= 0,
                    f"Published release {tag} contains invalid digest metadata: {name}")
            path = out / name
            require(path.stat().st_size == info["bytes"] and sha256(path) == info["sha256"],
                    f"Published release {tag} asset checksum conflict: {name}")
            checksums.append(f"{info['sha256']}  {name}\n")
        require((out / "SHA256SUMS").read_text(encoding="utf8") == "".join(checksums),
                f"Published release {tag} SHA256SUMS does not match assets")
        jar_name = next(n for n in assets if n.lower().endswith(".jar"))
        inspect_jar(out / jar_name, cfg)
        if cfg.get("launcher_zip"):
            pack_name = next(n for n in assets if n.lower().endswith(".zip"))
            require(zipfile.is_zipfile(out / pack_name),
                    f"Published launcher archive is invalid: {tag}/{pack_name}")
            with zipfile.ZipFile(out / pack_name) as launcher:
                jars = [n for n in launcher.namelist() if n.lower().endswith(".jar")]
                require(len(jars) == 1 and Path(jars[0]).name == jar_name and
                        hashlib.sha256(launcher.read(jars[0])).hexdigest() == assets[jar_name]["sha256"],
                        f"Published launcher archive JAR differs from release JAR: {tag}")
        # A completed local verify is optional for a remote no-op, but if it
        # exists, it must not contradict the immutable published assets.
        if local_output is not None and Path(local_output).exists():
            local = validate_local_manifest(Path(local_output), cfg)
            require(local["assets"] == assets,
                    f"Published release {tag} differs from locally verified assets")
    return True


def classify_remote_release(cfg, remote, repository, *, local_output=None, release_issue=None):
    """Read-only, fail-closed check performed for ALL selected targets first."""
    tag_sha = check_remote_tag(cfg, remote)
    view = release_view(cfg["tag"], repository)
    if view:
        require(tag_sha, f"Existing GitHub release {cfg['tag']} has no matching remote tag")
        require(view.get("tagName") == cfg["tag"], "GitHub release tag mismatch")
        require(view.get("isPrerelease") is False,
                f"Prerelease {cfg['tag']} requires an explicit reviewed policy")
        if view.get("isDraft") is True:
            return "DRAFT"
        require(view.get("isDraft") is False, "GitHub release draft status unknown")
        published_release_matches_head(cfg, view, repository, local_output, release_issue)
        return "PUBLISHED_IDENTICAL"
    return "TAG_ONLY" if tag_sha else "NEW"


def check_remote_tag(cfg, remote):
    sha = remote_tag(cfg["tag"],remote)
    if sha:
        require(sha == cfg["source_sha"], f"Existing tag {cfg['tag']} points to {sha}, not source {cfg['source_sha']}")
    return sha


def draft_one(cfg, base, repository, remote):
    dest = release_dir(base,cfg)
    manifest = validate_local_manifest(dest,cfg)
    tag = cfg["tag"]
    existing_tag = check_remote_tag(cfg,remote)
    draft = release_view(tag,repository)
    if draft:
        require(draft.get("isDraft") is True, f"Release {tag} is already published; will not modify")
        require(draft.get("isPrerelease") is False, "Unexpected prerelease draft; explicit prerelease policy required")
        require(existing_tag, f"Draft exists without remote tag: {tag}")
        require(draft.get("tagName") == tag, "Draft tag mismatch")
        remote_assets_match(tag,repository,dest,manifest)
        expected_names = set(manifest["assets"]) | {"PROVENANCE.json","SHA256SUMS"}
        observed_names = {a["name"] for a in draft.get("assets",[])}
        require(expected_names == observed_names, f"Draft assets unexpected: {observed_names ^ expected_names}")
        print(f"DRAFT ALREADY VERIFIED: {tag} (no writes)")
        return
    if not existing_tag:
        local_tag = git("rev-parse", "--verify", f"refs/tags/{tag}^{{}}", check=False)
        if local_tag:
            require(local_tag == cfg["source_sha"], f"Local tag collision at {tag}: {local_tag}")
        else:
            git("tag","-a",tag,cfg["source_sha"],"-m",f"ChessMod {cfg['version']} / Minecraft {cfg['minecraft']} / Forge")
        require(remote_head(cfg["branch"], remote) == cfg["source_sha"],
                f"{cfg['id']}: remote branch moved during draft preparation")
        git("push",remote,f"refs/tags/{tag}")
        observed = remote_tag(tag, remote)
        require(observed == cfg["source_sha"],
                f"Tag push succeeded, but remote {tag} resolves to {observed or 'no tag'}; "
                f"expected {cfg['source_sha']}. Do not delete or force-update the tag.")
    # This command can be safely retried after tag push; never overwrite an existing draft.
    assets=[str(dest/n) for n in sorted(manifest["assets"])]+[str(dest/"PROVENANCE.json"),str(dest/"SHA256SUMS")]
    gh("release","create",tag,*assets,"--repo",repository,"--verify-tag","--draft", "--latest=false",
       "--title",f"ChessMod {cfg['version']} for Minecraft {cfg['minecraft']} (Forge)",
       "--notes-file",str(dest/"RELEASE_NOTES.md"))
    draft=release_view(tag,repository)
    require(draft and draft.get("isDraft") is True,"Draft creation returned success but verification failed")
    print(f"DRAFT CREATED: {tag} — review it before publication")


def updated_forge_feed(data, cfg, repository):
    """Return a copy using Forge's required per-Minecraft-version changelog buckets.

    Promo keys contain the MC version while the changelog is nested below it.
    Remove the legacy top-level full-version entry written by earlier versions of
    the release manager, but reject unexpected content rather than discard it.
    """
    result = json.loads(json.dumps(data))
    require(isinstance(result, dict), "Forge update feed must be a JSON object")
    promos = result.setdefault("promos", {})
    require(isinstance(promos, dict), "Forge update feed promos must be a JSON object")
    name = cfg["minecraft"]
    for key in (f"{name}-latest", f"{name}-recommended"):
        old = promos.get(key)
        if old and old != cfg["version"]:
            require(version_num(old) <= version_num(cfg["version"]),
                    f"Refusing to downgrade {key}: {old} -> {cfg['version']}")
        promos[key] = cfg["version"]
    result["homepage"] = f"https://github.com/{repository}/releases"
    changelog = f"ChessMod {cfg['version']} — https://github.com/{repository}/releases/tag/{cfg['tag']}"
    bucket = result.setdefault(name, {})
    require(isinstance(bucket, dict), f"Invalid Forge changelog for Minecraft {name}")
    previous = bucket.get(cfg["version"])
    require(previous is None or previous == changelog,
            f"Conflicting Forge changelog for {cfg['version']}")
    bucket[cfg["version"]] = changelog
    # Migrate the 0.702-era writer's erroneous root-level entry, if present.
    if cfg["version"] in result:
        require(result[cfg["version"]] == changelog,
                f"Conflicting legacy Forge changelog for {cfg['version']}")
        del result[cfg["version"]]
    return result


def release_version_feed(registry, cfg, remote, repository, release_issue):
    """Post-publication bookkeeping on the canonical shared feed branch.

    Creates a normal commit in an isolated worktree and pushes WITHOUT force.
    A failed push leaves the published release intact; retry publish to resume.
    """
    branch = registry["update_feed_branch"]
    remote_sha = remote_head(branch, remote)
    local_ref = f"refs/remotes/{remote}/{branch}"
    if git("rev-parse","--verify",local_ref,check=False) != remote_sha:
        git("fetch",remote,f"refs/heads/{branch}:{local_ref}")
    with tempfile.TemporaryDirectory(prefix="chessmod-update-feed-") as td:
        wt=Path(td)/"repo"
        git("worktree","add","--quiet","--detach",str(wt),remote_sha)
        try:
            path=wt/registry["update_feed_path"]
            require(path.is_file(),f"Canonical update feed missing: {path}")
            data=updated_forge_feed(
                json.loads(path.read_text(encoding="utf8")),cfg,repository)
            result=json.dumps(data,indent=2,ensure_ascii=False)+"\n"
            if path.read_text(encoding="utf8")==result:
                print(f"UPDATE FEED ALREADY CURRENT: {cfg['version']}")
                return
            path.write_text(result,encoding="utf8")
            command(["git","add",str(path.relative_to(wt))],cwd=wt)
            command(["git","commit","-m",f"chore(release): publish update feed for {cfg['version']}",
                     "-m",f"Refs #205, Refs #{release_issue}"],cwd=wt)
            command(["git","push",remote,f"HEAD:refs/heads/{branch}"],cwd=wt)
            print(f"UPDATE FEED PUSHED: {branch} now advertises {cfg['version']}")
        finally:
            command(["git","worktree","remove","--force",str(wt)],check=False)


def version_num(full_version):
    try:
        return tuple(int(x) for x in full_version.rsplit("-",1)[-1].split("."))
    except (ValueError,IndexError):
        raise ReleaseError(f"Cannot compare release version: {full_version}")


def pinned_publish_config(target, base, remote, confirm):
    """Release identity comes from the verified exact SHA, never a moving branch tip.

    A previously published release can resume its bookkeeping after the shared
    update-feed branch advances; a not-yet-published draft cannot.
    """
    require(confirm and NAME_RE.fullmatch(confirm), "Invalid or missing --confirm tag")
    record = Path(base).resolve() / target["id"] / confirm / "PROVENANCE.json"
    require(record.is_file(), f"Missing local verified release manifest: {record}")
    payload = json.loads(record.read_text(encoding="utf8"))
    sha = payload.get("source_sha", "")
    require(SHA_RE.fullmatch(sha), "Untrusted source SHA in release manifest")
    cfg = parse_branch_config(sha, target)
    cfg.update({"id":target["id"], "branch":target["branch"], "loader":target["loader"],
                "minecraft":target["minecraft"], "source_sha":sha,
                "tag":tag_for(target,cfg["version"])})
    require(cfg["tag"] == confirm, "Confirmed tag does not match version at verified source SHA")
    branch_sha = remote_head(target["branch"],remote)
    view = release_view(confirm, "ramou/ChessMod")
    if view and view.get("isDraft") is False:
        require(command(["git","merge-base","--is-ancestor",sha,branch_sha],check=False).returncode == 0,
                "Published source commit is no longer an ancestor of remote branch")
    else:
        require(sha == branch_sha,
                f"Remote {target['branch']} moved since verification; draft publication blocked")
    return cfg


def publish_one(cfg, base, registry, repository, remote, confirm, release_issue):
    require(confirm==cfg["tag"], f"Explicit --confirm must equal {cfg['tag']}")
    dest=release_dir(base,cfg)
    manifest=validate_local_manifest(dest,cfg)
    require(check_remote_tag(cfg,remote), "No remote tag to publish")
    draft=release_view(cfg["tag"],repository)
    require(draft and draft["tagName"]==cfg["tag"],"No matching GitHub release")
    require(draft.get("isPrerelease") is False, "Prerelease publication requires a separate reviewed policy")
    current_remote_sha = remote_head(cfg["branch"],remote)
    if draft.get("isDraft"):
        require(current_remote_sha==cfg["source_sha"],"Remote branch moved before publication")
    else:
        require(command(["git","merge-base","--is-ancestor",cfg["source_sha"],current_remote_sha],check=False).returncode==0,
                "Published tag source is no longer an ancestor of branch")
    expect=set(manifest["assets"]) | {"PROVENANCE.json","SHA256SUMS"}
    require({a["name"] for a in draft.get("assets",[])}==expect,
            "Release asset set differs from verified manifest")
    remote_assets_match(cfg["tag"],repository,dest,manifest)
    if draft.get("isDraft"):
        print("PUBLISHING VERIFIED DRAFT (explicit user approval supplied)",flush=True)
        gh("release","edit",cfg["tag"],"--repo",repository,"--draft=false","--latest=false")
        print(f"PUBLISHED {cfg['tag']} — now updating shared feed",flush=True)
    else:
        print(f"ALREADY PUBLISHED {cfg['tag']} — checking/resuming feed metadata",flush=True)
    require(not release_view(cfg["tag"],repository).get("isDraft"), "Publication status unverified")
    try:
        release_version_feed(registry,cfg,remote,repository,release_issue)
    except Exception as exc:
        raise ReleaseError(f"PARTIAL SUCCESS: {cfg['tag']} is PUBLISHED, but update-feed commit/push failed. "
                           f"Inspect remote before rerunning the same publish command. Cause: {exc}") from exc


def parser():
    p=argparse.ArgumentParser(description="ChessMod branch-aware, human-gated release manager (#205)")
    p.add_argument("action",nargs="?",choices=("preflight","verify","draft","publish"),default="preflight")
    p.add_argument("--target",action="append",default=[],help="Minecraft version; repeat; default all eligible")
    p.add_argument("--version",help="Required release suffix, e.g. 0.702; validate every selected branch, never auto-bump")
    p.add_argument("--java-homes-dir",action="append",default=[],metavar="DIR",help="Directory of JDK installations")
    p.add_argument("--java-home",action="append",default=[],metavar="MAJOR=PATH",help="JDK override or TARGET:launcher/compiler=PATH")
    p.add_argument("--remote",default="origin")
    p.add_argument("--output-dir",default=str(ROOT/".ai-exports"/"releases"))
    p.add_argument("--apply",action="store_true",help="Required for remote-write draft/publish operations")
    p.add_argument("--confirm",help="Exact tag required when publishing")
    p.add_argument("--release-issue",type=int,help="Reviewed release issue (required for verify/draft/publish)")
    p.add_argument("--offline",action="store_true",help="Local-only Java/config preflight (no remote check); preflight only")
    p.add_argument("--registry",default=str(REGISTRY),help=argparse.SUPPRESS)
    return p


def main(argv=None):
    args = parser().parse_args(argv)
    requested = release_version(args.version)
    registry = load_registry(args.registry)
    targets = select_targets(registry, args.target)
    require(targets, "No eligible release targets")
    require(args.action == "preflight" or requested is not None,
            "Release verify/draft/publish requires --version X.Y (e.g. --version 0.702)")
    require(args.action not in ("draft", "publish") or args.apply,
            "Remote writes require explicit --apply")
    require(args.action != "publish" or (len(targets) == 1 and args.confirm),
            "Publish exactly one --target and supply --confirm <exact-tag>")
    require(args.action not in ("verify", "draft", "publish") or args.release_issue,
            "Verify/draft/publish requires --release-issue NUMBER")
    require(not args.offline or args.action == "preflight", "--offline is allowed only for preflight")
    require(not args.confirm or args.action == "publish", "--confirm is for publication only")
    require(args.remote == "origin", "Changing the release remote requires a reviewed registry change")

    # Java inventory must succeed for the WHOLE selected set before any builds.
    assignments, failures = discover_java(targets, args.java_home, args.java_homes_dir)
    print(f"Selected targets: {', '.join(t['id'] for t in targets)}")
    print("Java preflight (exact major versions):")
    for t in targets:
        a = assignments.get(t["id"])
        if a:
            print(f"  {t['id']:7} Gradle JDK {a['launcher'].major}: {a['launcher'].path}; compiler JDK {a['compiler'].major}: {a['compiler'].path}")
        else:
            print(f"  {t['id']:7} MISSING/INVALID")
    if failures:
        for e in failures:
            print("  ERROR:", e, file=sys.stderr)
        raise ReleaseError("Java preflight FAILED for selected targets; no build or GitHub operations attempted")

    if args.offline:
        cfgs = {}
        for target in targets:
            sha = local_head(target["branch"])
            cfg = parse_branch_config(sha, target)
            cfg["id"] = target["id"]
            cfgs[target["id"]] = cfg
            print(f"LOCAL: {target['id']} {cfg['version']} at {sha[:12]} (no remote checks)")
        require_requested_versions(cfgs, requested)
        print("LOCAL VERSION PREFLIGHT OK (no builds or remote operations)")
        return

    repo_guard(registry, args.remote)
    github_guard(registry["repository"])
    cfgs = {}
    # Source+version check for ALL targets, before querying release states or
    # invoking any Gradle task / writing tags or GitHub releases.
    for target in targets:
        cfg = (pinned_publish_config(target, args.output_dir, args.remote, args.confirm)
               if args.action == "publish" else inspect_target(target, args.remote))
        cfg["launcher_zip"] = target.get("launcher_zip", False)
        cfgs[target["id"]] = cfg
        print(f"SOURCE: {target['id']} {cfg['version']} {cfg['source_sha'][:12]} tag={cfg['tag']}")
    require_requested_versions(cfgs, requested)

    if args.action == "preflight" and requested is None:
        print("JDK/BRANCH INVENTORY OK; specify --version 0.702 to check release readiness")
        return

    # Inspect every existing tag/release BEFORE the first verify build or
    # draft/publish write. A matching published release is provably identical
    # only after remote provenance and ALL uploaded asset bytes are validated.
    states = {}
    for target in targets:
        cfg = cfgs[target["id"]]
        local = release_dir(args.output_dir, cfg)
        state = classify_remote_release(cfg, args.remote, registry["repository"],
                                        local_output=local, release_issue=args.release_issue)
        states[target["id"]] = state
        print(f"RELEASE: {target['id']} {cfg['tag']} => {state}")

    if args.action == "preflight":
        print("VERSION-LOCKED PREFLIGHT OK (no build, tag, GitHub or release writes)")
        return
    if args.action in ("draft", "publish"):
        release_issue_guard(args.release_issue, registry["repository"], list(cfgs.values()))
    if args.action == "verify":
        pending = [t for t in targets if states[t["id"]] != "PUBLISHED_IDENTICAL"]
        for t in pending:
            print(f"Gradle launcher probe: {t['id']} (Gradle {t['gradle']})", flush=True)
            probe_gradle(t, assignments[t["id"]], cfgs[t["id"]]["source_sha"])
        if pending:
            verify_targets(pending, cfgs, assignments, args.output_dir, args.release_issue)
        for t in targets:
            if states[t["id"]] == "PUBLISHED_IDENTICAL":
                print(f"ALREADY RELEASED: {t['id']} {cfgs[t['id']]['tag']} (identical tag, source SHA and verified assets; no build)")
        print("ALL SELECTED TARGETS VERIFIED OR ALREADY RELEASED. No GitHub writes were made.")
    elif args.action == "draft":
        pending = [t for t in targets if states[t["id"]] != "PUBLISHED_IDENTICAL"]
        # Validate ALL local manifests and ALL existing drafts BEFORE first
        # tag push or draft creation; partial releases must fail closed.
        records = {}
        for t in pending:
            cfg = cfgs[t["id"]]
            dest = release_dir(args.output_dir, cfg)
            record = validate_local_manifest(dest, cfg)
            require(record.get("release_issue") == args.release_issue,
                    f"{t['id']}: verify manifest belongs to another release issue")
            records[t["id"]] = record
            if states[t["id"]] == "DRAFT":
                view = release_view(cfg["tag"], registry["repository"])
                require(view and view.get("isDraft") is True, "Draft state changed during preflight")
                expected = set(record["assets"]) | {"PROVENANCE.json", "SHA256SUMS"}
                require({a.get("name") for a in view.get("assets", [])} == expected,
                        f"{cfg['tag']}: existing draft has unexpected assets")
                remote_assets_match(cfg["tag"], registry["repository"], dest, record)
        for t in pending:
            draft_one(cfgs[t["id"]], args.output_dir, registry["repository"], args.remote)
        for t in targets:
            if states[t["id"]] == "PUBLISHED_IDENTICAL":
                print(f"ALREADY PUBLISHED: {cfgs[t['id']]['tag']} (verified; draft is a no-op)")
    elif args.action == "publish":
        cfg = cfgs[targets[0]["id"]]
        record = validate_local_manifest(release_dir(args.output_dir, cfg), cfg)
        require(record.get("release_issue") == args.release_issue,
                "Verified manifest does not match supplied release issue")
        publish_one(cfg, args.output_dir, registry, registry["repository"],
                    args.remote, args.confirm, args.release_issue)


if __name__=="__main__":
    try:
        main()
    except (ReleaseError, OSError, ValueError, ET.ParseError, subprocess.SubprocessError, json.JSONDecodeError) as exc:
        print(f"RELEASE BLOCKED: {exc}",file=sys.stderr)
        sys.exit(1)
