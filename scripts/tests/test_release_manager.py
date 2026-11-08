"""No network, GitHub writes, Gradle installations, or Minecraft dependencies."""
import importlib.util
import json
from pathlib import Path
import subprocess
import shutil
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

SCRIPT = Path(__file__).resolve().parents[1] / "release_manager.py"
spec = importlib.util.spec_from_file_location("release_manager", SCRIPT)
rm = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = rm
spec.loader.exec_module(rm)


def fake_jdk(root, major):
    root.mkdir(parents=True)
    (root / "bin").mkdir()
    version = f"1.{major}.0_401" if major == 8 else f"{major}.0.12"
    java = root / "bin" / "java"
    javac = root / "bin" / "javac"
    java.write_text(f'#!/bin/sh\necho "openjdk version \\"{version}\\"" >&2\n')
    javac.write_text(f'#!/bin/sh\necho "javac {version}"\n')
    java.chmod(0o755); javac.chmod(0o755)
    return root


class RegistryTests(unittest.TestCase):
    def test_registry_eight_targets_and_default_all(self):
        r = rm.load_registry(Path(__file__).resolve().parents[1] / "release_targets.json")
        self.assertEqual(len(rm.select_targets(r,[])),8)
        self.assertEqual([t['id'] for t in rm.select_targets(r,['1.21'])],['1.21'])
        with self.assertRaises(rm.ReleaseError):
            rm.select_targets(r,['5.0'])

    def test_refuse_duplicate_and_malformed_registry(self):
        with tempfile.TemporaryDirectory() as td:
            registry=json.loads((Path(__file__).resolve().parents[1] / "release_targets.json").read_text())
            registry['targets'].append(dict(registry['targets'][0]))
            p=Path(td)/'a.json'; p.write_text(json.dumps(registry))
            with self.assertRaisesRegex(rm.ReleaseError,'Duplicate'):
                rm.load_registry(p)

    def test_version_and_mod_metadata_parser(self):
        x='modLoader="javafml"\n[[mods]] #mandatory\nmodId="chessmod"\nversion="${mod_version}"\n[[dependencies.chessmod]]\nversionRange="[47,)"\n'
        self.assertEqual(rm.parse_mod_metadata(x)['version'], '${mod_version}')
        self.assertEqual(rm.tag_for({'minecraft':'1.21','loader':'forge'},'1.21-0.702'),
                         'mc1.21-forge-v0.702')
        with self.assertRaises(rm.ReleaseError): rm.parse_mod_metadata('[[mods]]\nmodId="chessmod"')

    def test_sha_and_version_order(self):
        self.assertTrue(rm.version_num('1.20.1-0.702') > rm.version_num('1.20.1-0.701'))
        self.assertTrue(rm.version_num('1.20.1-0.702') == rm.version_num('1.21-0.702'))


class JavaTests(unittest.TestCase):
    def test_major_versions(self):
        self.assertEqual(rm.parse_java_major('java version "1.8.0_372"'),8)
        self.assertEqual(rm.parse_java_major('openjdk version "21.0.7"'),21)
        self.assertEqual(rm.parse_java_major('javac 16.0.2'),16)
        self.assertIsNone(rm.parse_java_major('no executable here'))

    @unittest.skipIf(sys.platform == 'win32', 'POSIX fake JDK sh scripts')
    def test_probe_realistic_fake_jdk_and_missing_compiler(self):
        with tempfile.TemporaryDirectory() as td:
            root=fake_jdk(Path(td)/'jdk 8 with spaces',8)
            result=rm.probe_java(root)
            self.assertEqual(result.major,8)
            self.assertEqual(result.path,root.resolve())
            (root/'bin'/'javac').unlink()
            self.assertIsNone(rm.probe_java(root))

    @unittest.skipIf(sys.platform == 'win32', 'POSIX fake JDK sh scripts')
    def test_multi_root_default_all_and_subset_gate(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td)/'JDKs with spaces'
            fake_jdk(root/'jdk 8',8)
            fake_jdk(root/'jdk 16',16)
            fake_jdk(root/'jdk 17',17)
            fake_jdk(root/'jdk 21',21)
            registry=rm.load_registry(Path(__file__).resolve().parents[1]/'release_targets.json')
            all_targets=rm.select_targets(registry,[])
            # Isolate discovery from this machine's real JDKs. In production,
            # discovery intentionally falls back to system installations.
            candidates = [root / f'jdk {major}' for major in (8,16,17,21)]
            with patch.object(rm, 'java_candidates', return_value=candidates):
                assignments,errors=rm.discover_java(all_targets,[],[str(root)])
                self.assertFalse(errors)
                self.assertEqual(len(assignments),8)
                self.assertEqual(assignments['1.17.1']['launcher'].major,16)
                self.assertEqual(assignments['1.21']['compiler'].major,21)
                # Removing the only discoverable JDK 16 blocks all-target
                # preflight, but not a selected 1.21-only run.
                for filename in (root/'jdk 16'/'bin').iterdir(): filename.unlink()
                assignments,errors=rm.discover_java(all_targets,[],[str(root)])
                self.assertTrue(any('1.17.1' in e and '16' in e for e in errors))
                assignments,errors=rm.discover_java(rm.select_targets(registry,['1.21']),[],[str(root)])
                self.assertFalse(errors)

    @unittest.skipIf(sys.platform == 'win32', 'POSIX fake JDK sh scripts')
    def test_discovery_uses_alternate_java_when_primary_disappears(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td)
            primary=fake_jdk(root/'primary 16',16)
            alternate=fake_jdk(root/'alternate 16',16)
            target={'id':'1.17.1','gradle_jvm':16,'compiler_jdk':16}
            with patch.object(rm, 'java_candidates', return_value=[primary,alternate]):
                assignments,errors=rm.discover_java([target],[],[str(root)])
                self.assertFalse(errors)
                self.assertEqual(assignments['1.17.1']['launcher'].path,primary.resolve())
                for filename in (primary/'bin').iterdir(): filename.unlink()
                assignments,errors=rm.discover_java([target],[],[str(root)])
                self.assertFalse(errors)
                self.assertEqual(assignments['1.17.1']['launcher'].path,alternate.resolve())
                self.assertEqual(assignments['1.17.1']['compiler'].path,alternate.resolve())

    @unittest.skipIf(sys.platform == 'win32', 'POSIX fake JDK sh scripts')
    def test_override_rejects_mismatch(self):
        with tempfile.TemporaryDirectory() as td:
            path=fake_jdk(Path(td)/'jdk 17',17)
            target={'id':'1.21','gradle_jvm':21,'compiler_jdk':21}
            _,errors=rm.discover_java([target],[f'21={path}'],[])
            self.assertEqual(len(errors),2)
            self.assertTrue(all('21' in x for x in errors))

    def test_gradle_env_does_not_change_global_environment(self):
        java=rm.JavaHome(Path('/fake path/jdk 17'),17,'17.0.1')
        with patch.dict('os.environ',{'JAVA_HOME':'/existing','PATH':'/bin'}):
            env=rm.gradle_env({'launcher':java,'compiler':java})
            self.assertEqual(env['JAVA_HOME'],str(java.path))
            self.assertEqual(__import__('os').environ['JAVA_HOME'],'/existing')


class ValidationTests(unittest.TestCase):
    def fake_cfg(self):
        return {'id':'1.21','branch':'1.21','minecraft':'1.21','loader':'forge',
                'version':'1.21-0.702','forge_version':'51.0.26','gradle':'8.9',
                'tag':'mc1.21-forge-v0.702','source_sha':'1'*40}

    def test_jar_metadata_and_launcher_zip_without_third_party(self):
        with tempfile.TemporaryDirectory() as td:
            base=Path(td)
            cfg=self.fake_cfg()
            jar=base/'chessmod-1.21-0.702.jar'
            with zipfile.ZipFile(jar,'w') as z:
                z.writestr('META-INF/mods.toml','[[mods]] #mandatory\nmodId="chessmod"\nversion="1.21-0.702"')
            (base/'LICENSE').write_text('MIT')
            rm.inspect_jar(jar,cfg)
            pack=base/'ChessMod-1.21-0.702.zip'
            rm.make_launcher_zip(pack,jar,cfg,base)
            with zipfile.ZipFile(pack) as z:
                self.assertEqual(len([n for n in z.namelist() if n.endswith('.jar')]),1)
                self.assertTrue(any(n.endswith('mmc-pack.json') for n in z.namelist()))
            with zipfile.ZipFile(jar,'w') as z:
                z.writestr('META-INF/mods.toml','[[mods]]\nmodId="chessmod"\nversion="1.21-0.700"')
            with self.assertRaisesRegex(rm.ReleaseError,'Packaged'):
                rm.inspect_jar(jar,cfg)

    def test_nonzero_test_reports_gate(self):
        with tempfile.TemporaryDirectory() as td:
            test=Path(td)/'project'/'build'/'test-results'/'test'
            test.mkdir(parents=True)
            self.assertEqual(rm.tests_from_xml(Path(td)),(0,0,0))
            (test/'TEST-Board.xml').write_text('<testsuite tests="3" failures="1" errors="0"/>')
            self.assertEqual(rm.tests_from_xml(Path(td)),(3,1,1))
            (test/'TEST-Board.xml').write_text('<testsuite tests="5" failures="0" errors="0"/>')
            self.assertEqual(rm.tests_from_xml(Path(td)),(5,0,1))

    def test_manifest_tamper_and_missing_report(self):
        with tempfile.TemporaryDirectory() as td:
            out=Path(td); cfg=self.fake_cfg()
            file=out/'chessmod-1.21-0.702.jar'; file.write_bytes(b'dummy')
            java=rm.JavaHome(Path('/test/jdk'),21,'21')
            rm.write_manifest(out,cfg,{'launcher':java,'compiler':java},[file],{'executed':4,'failures':0,'reports':1})
            rm.validate_local_manifest(out,cfg)
            file.write_bytes(b'altered')
            with self.assertRaisesRegex(rm.ReleaseError,'Artifact modified'):
                rm.validate_local_manifest(out,cfg)
            file.write_bytes(b'dummy')
            data=json.loads((out/'PROVENANCE.json').read_text())
            data['tests']['executed']=0
            (out/'PROVENANCE.json').write_text(json.dumps(data))
            with self.assertRaisesRegex(rm.ReleaseError,'nonzero tests'):
                rm.validate_local_manifest(out,cfg)

    def test_draft_requires_apply_and_publish_confirm(self):
        with patch.object(rm,'discover_java',side_effect=AssertionError('must not proceed')):
            with self.assertRaisesRegex(rm.ReleaseError,'explicit --apply'):
                rm.main(['draft', '--version', '0.702'])
            with self.assertRaisesRegex(rm.ReleaseError,'exact-tag'):
                rm.main(['publish','--version','0.702','--apply','--target','1.21'])

    def test_remote_tag_mismatch_blocks(self):
        cfg=self.fake_cfg()
        with patch.object(rm,'remote_tag',return_value='2'*40):
            with self.assertRaisesRegex(rm.ReleaseError,'Existing tag'):
                rm.check_remote_tag(cfg,'origin')

    def test_publish_must_be_manually_confirmed(self):
        cfg=self.fake_cfg()
        with self.assertRaisesRegex(rm.ReleaseError,'Explicit --confirm'):
            rm.publish_one(cfg,'/nothing',{},'ramou/ChessMod','origin','wrong-tag',209)


class PublicationGuardTests(unittest.TestCase):
    def cfg(self):
        return {'id':'1.21','branch':'1.21','minecraft':'1.21','loader':'forge',
                'version':'1.21-0.702','forge_version':'51.0.26','gradle':'8.9',
                'tag':'mc1.21-forge-v0.702','source_sha':'1'*40}

    def test_release_issue_requires_related_version_and_reference(self):
        from subprocess import CompletedProcess
        payload={'number':209,'state':'OPEN','title':'Release ChessMod 0.702','body':'Eight Forge branches'}
        with patch.object(rm,'gh',return_value=CompletedProcess([],0,json.dumps(payload),'')):
            with patch.object(rm,'git',return_value='feat: bump to 0.702\n\nRefs #209'):
                rm.release_issue_guard(209,'ramou/ChessMod',[self.cfg()])
            with patch.object(rm,'git',return_value='chore: unrelated change'):
                with self.assertRaisesRegex(rm.ReleaseError,'source version-change history'):
                    rm.release_issue_guard(209,'ramou/ChessMod',[self.cfg()])
            payload['title']='Release ChessMod 0.701'
            with patch.object(rm,'gh',return_value=CompletedProcess([],0,json.dumps(payload),'')):
                with self.assertRaisesRegex(rm.ReleaseError,'does not appear to authorise'):
                    rm.release_issue_guard(209,'ramou/ChessMod',[self.cfg()])

    def test_published_release_can_resume_after_shared_branch_moves(self):
        from subprocess import CompletedProcess
        cfg=self.cfg()
        with tempfile.TemporaryDirectory() as td:
            p=Path(td)/'1.21'/cfg['tag'];p.mkdir(parents=True)
            (p/'PROVENANCE.json').write_text(json.dumps({'source_sha':cfg['source_sha']}))
            with patch.object(rm,'parse_branch_config',return_value={
                'version':cfg['version'],'forge_version':'51.0.26','gradle':'8.9'}), \
                 patch.object(rm,'remote_head',return_value='2'*40), \
                 patch.object(rm,'release_view',return_value={'isDraft':False}), \
                 patch.object(rm,'command',return_value=CompletedProcess([],0,'','')):
                got=rm.pinned_publish_config(cfg,td,'origin',cfg['tag'])
                self.assertEqual(got['source_sha'],cfg['source_sha'])
            with patch.object(rm,'parse_branch_config',return_value={
                'version':cfg['version'],'forge_version':'51.0.26','gradle':'8.9'}), \
                 patch.object(rm,'remote_head',return_value='2'*40), \
                 patch.object(rm,'release_view',return_value={'isDraft':True}):
                with self.assertRaisesRegex(rm.ReleaseError,'moved since verification'):
                    rm.pinned_publish_config(cfg,td,'origin',cfg['tag'])

    def test_remote_asset_verification_refuses_hash_mismatch(self):
        from subprocess import CompletedProcess
        with tempfile.TemporaryDirectory() as td:
            p=Path(td); original=p/'file.jar'; original.write_bytes(b'correct')
            manifest={'assets':{'file.jar':{'sha256':rm.sha256(original),'bytes':original.stat().st_size}}}
            (p/'SHA256SUMS').write_text('test')
            (p/'PROVENANCE.json').write_text('test')
            def fake_gh(*args,**kw):
                name=args[args.index('--pattern')+1]
                dest=Path(args[args.index('--dir')+1]) / name
                dest.write_bytes(b'wrong')
                return CompletedProcess([],0,'','')
            with patch.object(rm,'gh',side_effect=fake_gh):
                with self.assertRaisesRegex(rm.ReleaseError,'asset mismatch'):
                    rm.remote_assets_match('a-tag','ramou/ChessMod',p,manifest)

    def test_preflight_fails_java_before_any_online_check(self):
        targets=[{'id':'target','gradle_jvm':90,'compiler_jdk':90}]
        with patch.object(rm,'select_targets',return_value=targets), \
             patch.object(rm,'load_registry',return_value={'targets':targets}), \
             patch.object(rm,'discover_java',return_value=({},['target requires unavailable JDK 90'])), \
             patch.object(rm,'repo_guard',side_effect=AssertionError('must not reach remote')):
            with self.assertRaisesRegex(rm.ReleaseError,'preflight FAILED'):
                rm.main(['preflight'])

    def test_gh_errors_not_mistaken_for_missing_release(self):
        from subprocess import CompletedProcess
        with patch.object(rm,'gh',return_value=CompletedProcess([],1,'','HTTP 403: forbidden')):
            with self.assertRaisesRegex(rm.ReleaseError,'Failed to inspect'):
                rm.release_view('a-tag','ramou/ChessMod')
        with patch.object(rm,'gh',return_value=CompletedProcess([],1,'','release not found')):
            self.assertIsNone(rm.release_view('a-tag','ramou/ChessMod'))


class RequestedReleaseVersionTests(unittest.TestCase):
    def test_version_suffix_validation(self):
        self.assertEqual(rm.release_version("0.702"), "0.702")
        self.assertIsNone(rm.release_version(None))
        for value in ("", "1.21-0.702", "v0.702", "0.0702", "../0.702", "0.702;echo"):
            with self.subTest(value=value), self.assertRaisesRegex(rm.ReleaseError, "Invalid --version"):
                rm.release_version(value)

    def test_entire_version_set_is_checked_not_first_mismatch(self):
        cfgs = {
            "1.14.4":{"id":"1.14.4","minecraft":"1.14.4","version":"1.14.4-0.616"},
            "1.20.1":{"id":"1.20.1","minecraft":"1.20.1","version":"1.20.1-0.701"},
            "1.21":{"id":"1.21","minecraft":"1.21","version":"1.21-0.702"},
        }
        with self.assertRaises(rm.ReleaseError) as caught:
            rm.require_requested_versions(cfgs, "0.702")
        self.assertIn("1.14.4-0.616", str(caught.exception))
        self.assertIn("1.20.1-0.701", str(caught.exception))
        self.assertNotIn("declared 1.21-0.702", str(caught.exception))
        self.assertIsNone(rm.require_requested_versions(cfgs, None))

    def test_verify_requires_explicit_version_before_jdk_probe(self):
        with patch.object(rm, "discover_java", side_effect=AssertionError("must not discover JDKs")):
            with self.assertRaisesRegex(rm.ReleaseError, "requires --version"):
                rm.main(["verify"])

    def test_mismatch_blocks_release_lookups_and_builds_across_targets(self):
        targets = [{"id":"1.14.4", "branch":"1.14.4", "gradle_jvm":8,"compiler_jdk":8},
                   {"id":"1.21", "branch":"1.21", "gradle_jvm":21,"compiler_jdk":21}]
        cfgs = {"1.14.4":{"id":"1.14.4", "minecraft":"1.14.4", "version":"1.14.4-0.616", "tag":"x", "source_sha":"1"*40},
                "1.21":{"id":"1.21", "minecraft":"1.21", "version":"1.21-0.701", "tag":"y", "source_sha":"2"*40}}
        java = {t["id"]:{"launcher":rm.JavaHome(Path("/fake"),t["gradle_jvm"],"fake"),
                          "compiler":rm.JavaHome(Path("/fake"),t["compiler_jdk"],"fake")}
                for t in targets}
        with patch.object(rm,"load_registry",return_value={"repository":"ramou/ChessMod"}), \
             patch.object(rm,"select_targets",return_value=targets), \
             patch.object(rm,"discover_java",return_value=(java,[])), \
             patch.object(rm,"repo_guard"), patch.object(rm,"github_guard"), \
             patch.object(rm,"inspect_target",side_effect=lambda t,r:dict(cfgs[t["id"]])), \
             patch.object(rm,"classify_remote_release",side_effect=AssertionError("should stop before remote release lookup")), \
             patch.object(rm,"probe_gradle",side_effect=AssertionError("must not build")):
            with self.assertRaises(rm.ReleaseError) as caught:
                rm.main(["verify", "--version", "0.702", "--release-issue", "209"])
        self.assertIn("1.14.4",str(caught.exception))
        self.assertIn("1.21",str(caught.exception))

    def test_draft_prechecks_all_manifests_before_remote_writes(self):
        targets = [{"id":"1.14.4", "branch":"1.14.4", "gradle_jvm":8,"compiler_jdk":8},
                   {"id":"1.21", "branch":"1.21", "gradle_jvm":21,"compiler_jdk":21}]
        cfgs = {
            "1.14.4":{"id":"1.14.4", "branch":"1.14.4", "minecraft":"1.14.4", "version":"1.14.4-0.702", "tag":"mc1.14.4-forge-v0.702", "source_sha":"1"*40},
            "1.21":{"id":"1.21", "branch":"1.21", "minecraft":"1.21", "version":"1.21-0.702", "tag":"mc1.21-forge-v0.702", "source_sha":"2"*40},
        }
        java = {t["id"]:{"launcher":rm.JavaHome(Path("/fake"),t["gradle_jvm"],"fake"),
                          "compiler":rm.JavaHome(Path("/fake"),t["compiler_jdk"],"fake")}
                for t in targets}
        with tempfile.TemporaryDirectory() as output:
            with patch.object(rm,"load_registry",return_value={"repository":"ramou/ChessMod"}), \
                 patch.object(rm,"select_targets",return_value=targets), \
                 patch.object(rm,"discover_java",return_value=(java,[])), \
                 patch.object(rm,"repo_guard"), patch.object(rm,"github_guard"), \
                 patch.object(rm,"inspect_target",side_effect=lambda t,r:dict(cfgs[t["id"]])), \
                 patch.object(rm,"classify_remote_release",return_value="NEW"), \
                 patch.object(rm,"release_issue_guard"), \
                 patch.object(rm,"draft_one",side_effect=AssertionError("must not write any draft")):
                with self.assertRaisesRegex(rm.ReleaseError,"Missing verified release record"):
                    rm.main(["draft","--apply","--version","0.702","--release-issue","209",
                             "--output-dir",output])

    def test_identical_published_branch_skips_build(self):
        target = {"id":"1.21","branch":"1.21","gradle_jvm":21,"compiler_jdk":21,"gradle":"8.9"}
        java = rm.JavaHome(Path("/fake"),21,"fake")
        cfg = {"id":"1.21","minecraft":"1.21","version":"1.21-0.702", "tag":"mc1.21-forge-v0.702","source_sha":"1"*40}
        with patch.object(rm,"load_registry",return_value={"repository":"ramou/ChessMod"}), \
             patch.object(rm,"select_targets",return_value=[target]), \
             patch.object(rm,"discover_java",return_value=({"1.21":{"launcher":java,"compiler":java}},[])), \
             patch.object(rm,"repo_guard"), patch.object(rm,"github_guard"), \
             patch.object(rm,"inspect_target",return_value=cfg), \
             patch.object(rm,"classify_remote_release",return_value="PUBLISHED_IDENTICAL"), \
             patch.object(rm,"probe_gradle",side_effect=AssertionError("must not launch Gradle")), \
             patch.object(rm,"verify_targets",side_effect=AssertionError("must not rebuild")):
            rm.main(["verify","--target","1.21","--version","0.702","--release-issue","209"])


class PublishedReleaseIdentityTests(unittest.TestCase):
    def cfg(self):
        return {"id":"1.21", "branch":"1.21", "minecraft":"1.21", "loader":"forge",
                "version":"1.21-0.702", "forge_version":"51.0.26", "gradle":"8.9",
                "source_sha":"a"*40, "tag":"mc1.21-forge-v0.702", "launcher_zip":True}

    def setup_published(self, folder, cfg):
        folder.mkdir(exist_ok=True,parents=True)
        jar = folder / "chessmod-1.21-0.702.jar"
        with zipfile.ZipFile(jar, 'w') as z:
            z.writestr('META-INF/mods.toml', '[[mods]]\nmodId="chessmod"\nversion="1.21-0.702"\n')
        pack = folder / "ChessMod-1.21-0.702.zip"
        (folder / "LICENSE").write_text("MIT")
        rm.make_launcher_zip(pack,jar,cfg,folder)
        jdk=rm.JavaHome(Path('/fake'),21,'21')
        rm.write_manifest(folder,cfg,{"launcher":jdk,"compiler":jdk},
                          [jar,pack],{"executed":27,"failures":0,"reports":3},209)
        names = sorted(p.name for p in folder.iterdir() if p.name not in ('RELEASE_NOTES.md', 'LICENSE'))
        view={"tagName":cfg["tag"],"isDraft":False,"isPrerelease":False,
              "assets":[{"name":n} for n in names]}
        return view

    def downloader(self, folder):
        def f(*args, **kwargs):
            self.assertEqual(args[:2], ("release","download"))
            name = args[args.index("--pattern")+1]
            target = Path(args[args.index("--dir")+1]) / name
            shutil.copyfile(folder / name,target)
            return subprocess.CompletedProcess(args,0,'','')
        return f

    def test_published_matching_source_and_all_assets_is_idempotent(self):
        cfg=self.cfg()
        with tempfile.TemporaryDirectory() as td:
            folder=Path(td)/"remote";view=self.setup_published(folder,cfg)
            with patch.object(rm,"check_remote_tag",return_value=cfg["source_sha"]), \
                 patch.object(rm,"release_view",return_value=view), \
                 patch.object(rm,"gh",side_effect=self.downloader(folder)):
                self.assertEqual(rm.classify_remote_release(cfg,"origin","ramou/ChessMod"),
                                 "PUBLISHED_IDENTICAL")

    def test_published_asset_tamper_is_hard_failure(self):
        cfg=self.cfg()
        with tempfile.TemporaryDirectory() as td:
            folder=Path(td)/"remote";view=self.setup_published(folder,cfg)
            (folder/"chessmod-1.21-0.702.jar").write_bytes(b"different asset bytes")
            with patch.object(rm,"gh",side_effect=self.downloader(folder)):
                with self.assertRaisesRegex(rm.ReleaseError,"checksum conflict"):
                    rm.published_release_matches_head(cfg,view,"ramou/ChessMod")

    def test_published_provenance_source_tamper_is_hard_failure(self):
        cfg=self.cfg()
        with tempfile.TemporaryDirectory() as td:
            folder=Path(td)/"remote";view=self.setup_published(folder,cfg)
            provenance=folder/"PROVENANCE.json"
            data=json.loads(provenance.read_text());data["source_sha"]='b'*40
            provenance.write_text(json.dumps(data))
            with patch.object(rm,"gh",side_effect=self.downloader(folder)):
                with self.assertRaisesRegex(rm.ReleaseError,"provenance conflict: source_sha"):
                    rm.published_release_matches_head(cfg,view,"ramou/ChessMod")

    def test_missing_remote_provenance_prevents_noop(self):
        cfg=self.cfg()
        with tempfile.TemporaryDirectory() as td:
            folder=Path(td)/"remote";view=self.setup_published(folder,cfg)
            view['assets']=[a for a in view['assets'] if a['name']!='PROVENANCE.json']
            with self.assertRaisesRegex(rm.ReleaseError,"lacks verifiable provenance"):
                rm.published_release_matches_head(cfg,view,"ramou/ChessMod")

    def test_remote_tag_points_to_other_source_blocks_before_asset_check(self):
        cfg=self.cfg()
        with patch.object(rm,"check_remote_tag",side_effect=rm.ReleaseError('Existing tag points to different source')), \
             patch.object(rm,"release_view",side_effect=AssertionError("must stop before assets")):
            with self.assertRaisesRegex(rm.ReleaseError,"different source"):
                rm.classify_remote_release(cfg,"origin","ramou/ChessMod")

    def test_existing_local_manifest_must_match_published_assets(self):
        cfg=self.cfg()
        with tempfile.TemporaryDirectory() as td:
            base=Path(td)
            remote=base/"remote";view=self.setup_published(remote,cfg)
            local=base/"local";self.setup_published(local,cfg)
            localjar=local/"chessmod-1.21-0.702.jar"
            with zipfile.ZipFile(localjar,'a') as z:
                z.writestr('different.txt','rebuilt differently')
            # Local hashes were not changed in manifest: definitely not an
            # immutable verified artifact and must not qualify as a no-op.
            with patch.object(rm,"gh",side_effect=self.downloader(remote)):
                with self.assertRaisesRegex(rm.ReleaseError,"Artifact modified"):
                    rm.published_release_matches_head(cfg,view,"ramou/ChessMod", local_output=local)

    def test_provenance_release_issue_conflict_stops_noop(self):
        cfg=self.cfg()
        with tempfile.TemporaryDirectory() as td:
            remote=Path(td)/"remote";view=self.setup_published(remote,cfg)
            with patch.object(rm,"gh",side_effect=self.downloader(remote)):
                with self.assertRaisesRegex(rm.ReleaseError,"another release issue"):
                    rm.published_release_matches_head(cfg,view,"ramou/ChessMod",release_issue=999)

    def test_draft_remains_pending_not_identical_published(self):
        cfg=self.cfg()
        view={"tagName":cfg["tag"],"isDraft":True,"isPrerelease":False,"assets":[]}
        with patch.object(rm,"check_remote_tag",return_value=cfg["source_sha"]), \
             patch.object(rm,"release_view",return_value=view), \
             patch.object(rm,"gh",side_effect=AssertionError("no published asset audit for draft")):
            self.assertEqual(rm.classify_remote_release(cfg,"origin","ramou/ChessMod"),"DRAFT")




class RemoteTagDiscoveryTests(unittest.TestCase):
    """Exercise real git ls-remote output, including annotated peeled refs."""

    @unittest.skipUnless(shutil.which("git"), "Git executable required")
    def test_annotated_tag_is_discovered_and_retry_classifies_tag_only(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            bare = root / "origin.git"
            work = root / "checkout"

            def run_git(*args, cwd=root):
                p = subprocess.run(["git", *args], cwd=cwd, capture_output=True,
                                   text=True, check=True)
                return p.stdout.strip()

            run_git("init", "--quiet", "--bare", str(bare))
            run_git("init", "--quiet", str(work))
            run_git("config", "user.name", "ChessMod Test", cwd=work)
            run_git("config", "user.email", "chessmod-test@example.invalid", cwd=work)
            (work / "test.txt").write_text("source commit\n", encoding="utf8")
            run_git("add", "test.txt", cwd=work)
            run_git("commit", "--quiet", "-m", "Test source", cwd=work)
            commit = run_git("rev-parse", "HEAD", cwd=work)
            run_git("remote", "add", "origin", str(bare), cwd=work)
            run_git("push", "--quiet", "origin", "HEAD:refs/heads/1.21", cwd=work)

            tag = "mc1.21-forge-v0.702"
            run_git("tag", "-a", tag, "-m", "Annotated release tag", cwd=work)
            run_git("push", "--quiet", "origin", f"refs/tags/{tag}", cwd=work)
            raw = run_git("ls-remote", "--tags", "origin",
                          f"refs/tags/{tag}", f"refs/tags/{tag}^{{}}", cwd=work)
            self.assertEqual(len(raw.splitlines()), 2)
            tag_object = raw.splitlines()[0].split()[0]
            self.assertNotEqual(tag_object, commit)
            self.assertIn(f"{commit}\trefs/tags/{tag}^{{}}", raw)

            lightweight = "mc1.21-forge-v0.703"
            run_git("tag", lightweight, cwd=work)
            run_git("push", "--quiet", "origin", f"refs/tags/{lightweight}", cwd=work)

            def local_git(*args, **kwargs):
                return run_git(*args, cwd=work)

            cfg = {"tag": tag, "source_sha": commit}
            with patch.object(rm, "git", side_effect=local_git):
                self.assertEqual(rm.remote_tag(tag, "origin"), commit)
                self.assertEqual(rm.remote_tag(lightweight, "origin"), commit)
                self.assertIsNone(rm.remote_tag("mc1.21-forge-v0.999", "origin"))
                with patch.object(rm, "release_view", return_value=None):
                    self.assertEqual(
                        rm.classify_remote_release(cfg, "origin", "ramou/ChessMod"),
                        "TAG_ONLY")
                with self.assertRaisesRegex(rm.ReleaseError, "Existing tag"):
                    rm.check_remote_tag({"tag": tag, "source_sha": "0" * 40}, "origin")

    def test_invalid_remote_tag_listing_fails_closed(self):
        tag = "mc1.21-forge-v0.702"
        direct = f"refs/tags/{tag}"
        peeled = f"{direct}^{{}}"
        for raw in (
            f"not-a-sha\t{direct}",
            f"{'a' * 40}\trefs/tags/another-tag",
            f"{'a' * 40}\t{direct}\n{'b' * 40}\t{direct}",
            f"{'a' * 40}\t{peeled}",
            f"{'a' * 40}\t{direct}\textra",
        ):
            with self.subTest(raw=raw), patch.object(rm, "git", return_value=raw):
                with self.assertRaises(rm.ReleaseError):
                    rm.remote_tag(tag, "origin")
if __name__=='__main__':
    unittest.main()


class ForgeFeedFormatTests(unittest.TestCase):
    def setUp(self):
        self.cfg = {"minecraft":"1.21", "version":"1.21-0.702",
                    "tag":"mc1.21-forge-v0.702"}
        self.expected = ("ChessMod 1.21-0.702 — "
                         "https://github.com/ramou/ChessMod/releases/tag/mc1.21-forge-v0.702")

    def test_nested_changelog_and_promotion(self):
        old = {"homepage":"https://old.invalid/", "promos":{
                   "1.20.1-latest":"1.20.1-0.701"},
               "1.20.1":{"1.20.1-0.701":"previous"}}
        new = rm.updated_forge_feed(old,self.cfg,"ramou/ChessMod")
        self.assertEqual(new["1.21"]["1.21-0.702"],self.expected)
        self.assertEqual(new["promos"]["1.21-latest"],"1.21-0.702")
        self.assertEqual(new["promos"]["1.21-recommended"],"1.21-0.702")
        self.assertEqual(new["1.20.1"],old["1.20.1"])
        self.assertEqual(old["homepage"],"https://old.invalid/")  # pure function
        self.assertNotIn("1.21-0.702",new)
        self.assertEqual(rm.updated_forge_feed(new,self.cfg,"ramou/ChessMod"),new)

    def test_migrates_original_top_level_bug(self):
        previous = {"homepage":"https://github.com/ramou/ChessMod/releases",
                    "promos":{"1.21-latest":"1.21-0.702",
                              "1.21-recommended":"1.21-0.702"},
                    "1.21-0.702":self.expected}
        new = rm.updated_forge_feed(previous,self.cfg,"ramou/ChessMod")
        self.assertNotIn("1.21-0.702",new)
        self.assertEqual(new["1.21"]["1.21-0.702"],self.expected)

    def test_conflict_and_downgrade_fail_closed(self):
        with self.assertRaisesRegex(rm.ReleaseError,"Conflicting"):
            rm.updated_forge_feed({"1.21-0.702":"wrong"}, self.cfg,"ramou/ChessMod")
        with self.assertRaisesRegex(rm.ReleaseError,"Conflicting"):
            rm.updated_forge_feed({"1.21":{"1.21-0.702":"wrong"}},self.cfg,"ramou/ChessMod")
        with self.assertRaisesRegex(rm.ReleaseError,"downgrade"):
            rm.updated_forge_feed({"promos":{"1.21-latest":"1.21-0.703"}},self.cfg,"ramou/ChessMod")

