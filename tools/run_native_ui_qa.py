"""Native Swing QA: offline screenshot matrix or real owned-clone workflows."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import secrets
import subprocess
from milestone2_environment import ROOT, RUNTIME, sql, unprotect_credential


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--integration', action='store_true')
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    project = ROOT / 'AOOP - MotorPH from OOP'
    classes = project / '.build/native-qa'
    classes.mkdir(parents=True, exist_ok=True)
    production = project / '.build/classes'
    libraries = project / 'lib'
    sources = [ROOT / 'security/tools/NativeWorkflowAssurance.java'] if args.integration else [ROOT / 'tools/NativeUiQa.java', project / 'test/oop_motorph/UiAuthFixture.java']
    compile_result = subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-cp', str(production) + ';' + str(libraries / '*'), '-d', str(classes), *map(str, sources)], capture_output=True)
    if compile_result.returncode:
        (args.output / 'compile-errors.txt').write_bytes(compile_result.stderr)
        raise RuntimeError('Native QA compilation failed; inspect compile-errors.txt')
    environment = os.environ.copy()
    if args.integration:
        marker = json.loads((RUNTIME / 'environment.json').read_text(encoding='utf-8'))
        profile = json.loads((RUNTIME / 'profile.json').read_text(encoding='utf-8-sig'))
        if marker['port'] != 3307 or marker['schema'] != 'aoop_m2' or not profile['jdbc_url'].startswith('jdbc:mysql://127.0.0.1:3307/aoop_m2?'):
            raise ValueError('Owned isolated clone required')
        admin = unprotect_credential(RUNTIME / 'admin.credential.xml')
        app = unprotect_credential(RUNTIME / 'app.credential.xml')
        if sql(admin['user'], admin['password'], 'SELECT @@port,DATABASE();', database='aoop_m2').strip() != '3307\taoop_m2':
            raise ValueError('Actual clone identity mismatch')
        environment.update(M2_JDBC_URL=profile['jdbc_url'], M2_ADMIN_PASSWORD=admin['password'], M2_APP_PASSWORD=app['password'], COMPUTERNAME='M2-UI-QA-' + secrets.token_hex(8))
        classpath = ';'.join([str(classes), str(production), str(libraries / '*')])
        command = ['java', '-ea', '-cp', classpath, 'oop_motorph.NativeWorkflowAssurance']
    else:
        classpath = ';'.join([str(classes), str(production), *[str(libraries / name) for name in ('bcrypt-0.10.2.jar', 'bytes-1.5.0.jar', 'jcalendar-1.4.jar')]])
        command = ['java', '-ea', '-cp', classpath, 'oop_motorph.NativeUiQa', str(args.output.resolve())]
    try:
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', env=environment, timeout=240)
    except subprocess.TimeoutExpired as error:
        # The child is owned by this invocation; timeout terminates only this test JVM.
        (RUNTIME / 'native-ui-timeout-private.txt').write_text(str(error), encoding='utf-8')
        raise RuntimeError('Native QA timed out; private diagnostics retained') from None
    if result.returncode:
        destination = RUNTIME / 'native-ui-private-diagnostic.txt' if args.integration else args.output / 'native-ui-diagnostic.txt'
        destination.write_text(result.stdout + '\n' + result.stderr, encoding='utf-8')
        raise RuntimeError('Native QA failed; inspect its scoped diagnostic')
    public = '\n'.join(line for line in result.stdout.splitlines() if line.startswith(('PASS ', 'Native ')))
    (args.output / ('native-workflow-results.txt' if args.integration else 'native-run.txt')).write_text(public, encoding='utf-8')
    if not args.integration:
        images = [{'file': p.name, 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in sorted(args.output.glob('*.png'))]
        (args.output / 'capture-provenance.json').write_text(json.dumps({'kind': 'Native Swing window self-print captures; direct component actions; no global desktop input', 'data': 'Synthetic UiAuthFixture with metadata bridge; real BCrypt-issued capability over fake JDBC; no real database driver/socket', 'application_baseline': 'd7ee701', 'images': images}, indent=2), encoding='utf-8')
    print(public)


if __name__ == '__main__':
    main()
