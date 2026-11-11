"""Offline integration check of the branch-aware Java helper."""
import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[2]

class EnvHelperTest(unittest.TestCase):
    def test_branch_switch_and_failed_selection(self):
        with tempfile.TemporaryDirectory() as tmp:
            repo = pathlib.Path(tmp) / 'repo'
            repo.mkdir()
            def run(*args):
                return subprocess.run(args, cwd=repo, text=True, capture_output=True)
            for args in [('git','init','-q','-b','1.20.1'),
                         ('git','config','user.name','Test'),
                         ('git','config','user.email','test@example.invalid')]:
                self.assertEqual(run(*args).returncode, 0)
            for rel in ('scripts/env.sh','scripts/env_core.sh','scripts/release_targets.json','.gitignore'):
                dest = repo / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(ROOT / rel, dest)
            (repo / '.conf').mkdir()
            (repo / '.conf/app.conf.example').write_text('# example\n')
            config = []
            for major in (8,16,17,21):
                home = pathlib.Path(tmp) / f'jdk{major}'
                (home / 'bin').mkdir(parents=True)
                for executable, version in [('java',f'openjdk version "{major}.0.1"'),('javac',f'javac {major}.0.1')]:
                    exe = home / 'bin' / executable
                    exe.write_text("#!/bin/sh\necho '%s' >&2\n" % version)
                    exe.chmod(0o755)
                config.append(f'JAVA_HOME_{major}={home}\n')
            (repo / '.conf/app.conf').write_text(''.join(config))
            self.assertEqual(run('git','add','.').returncode, 0)
            self.assertEqual(run('git','commit','-qm','canonical').returncode, 0)
            self.assertEqual(run('git','checkout','-qb','1.19.4').returncode, 0)
            result = run('bash','-c','source scripts/env.sh; echo FIRST:$JAVA_HOME; source scripts/env.sh 1.21; echo SECOND:$JAVA_HOME; source scripts/env.sh invalid; echo LAST:$JAVA_HOME; git status --short')
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertIn('FIRST:'+tmp+'/jdk17',result.stdout)
            self.assertIn('SECOND:'+tmp+'/jdk21',result.stdout)
            self.assertIn('LAST:'+tmp+'/jdk21',result.stdout)
            self.assertIn('unknown target branch',result.stderr)
            self.assertNotIn('transient',result.stdout)

if __name__ == '__main__':
    unittest.main()
