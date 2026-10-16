"""Focused regression of reviewer findings, using synthetic isolated artifacts."""
from pathlib import Path
import json
import os
import subprocess
import tempfile
from milestone2_environment import ROOT, RUNTIME, sql, run, unprotect_credential


def main():
    checks = []
    temporary = RUNTIME / 'escrow-regression'
    temporary.mkdir(exist_ok=True)
    escrow = temporary / 'fixture.dpapi'
    # Test-created fixture only; do not overwrite an existing operator escrow.
    if escrow.exists():
        escrow = temporary / ('fixture-' + next(tempfile._get_candidate_names()) + '.dpapi')
    environment = os.environ.copy()
    environment['M2_BOOTSTRAP_PATH'] = str(escrow)
    command = ['powershell', '-NoProfile', '-File', str(ROOT / 'scripts/protect-bootstrap-escrow.ps1')]
    environment['M2_BOOTSTRAP_JSON'] = json.dumps([{'username': 'fixture-A', 'password': 'synthetic-fixture-A-only'}])
    run(command, environment=environment)
    first = escrow.read_bytes()
    environment['M2_BOOTSTRAP_JSON'] = json.dumps([{'username': 'fixture-A', 'password': 'synthetic-fixture-A-only'}, {'username': 'fixture-B', 'password': 'synthetic-fixture-B-only'}])
    run(command, environment=environment)
    previous = list(temporary.glob(escrow.name + '.previous-*'))
    if not previous or previous[-1].read_bytes() != first:
        raise RuntimeError('Atomic replacement did not preserve prior ciphertext backup.')
    checks.append('Escrow atomic replacement retains exact prior ciphertext: success')
    decoded = run(['powershell','-NoProfile','-Command',"$ErrorActionPreference='Stop';$s=ConvertTo-SecureString ((Get-Content -LiteralPath $env:M2_BOOTSTRAP_PATH -Raw).Trim());$c=New-Object System.Management.Automation.PSCredential('escrow',$s);$c.GetNetworkCredential().Password"], environment=environment)
    entries = json.loads(decoded.decode('utf-8-sig'))
    if not isinstance(entries,list) or len(entries)!=2 or {e.get('username') for e in entries}!={'fixture-A','fixture-B'}:
        raise RuntimeError('Escrow canonical JSON did not preserve usable entries.')
    checks.append('Windows PowerShell escrow serialization preserves canonical username/password entries: success')
    stable = escrow.read_bytes()
    environment['M2_BOOTSTRAP_JSON'] = '{invalid-json'
    rejected = subprocess.run(command, env=environment, capture_output=True)
    if rejected.returncode == 0 or escrow.read_bytes() != stable:
        raise RuntimeError('Failed escrow update changed prior ciphertext.')
    checks.append('Failed escrow update preserves committed map: denied safely')
    profile = RUNTIME / 'profile.json'
    original = profile.read_bytes()
    launcher = ['powershell', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', str(ROOT / 'scripts/start-milestone2.ps1'), '-SkipBuild', '-VerifyOnly']
    injected = os.environ.copy(); injected['JAVA_TOOL_OPTIONS'] = '-Daoop.db.url=jdbc:mysql://127.0.0.1:3306/aoop'
    rejected = subprocess.run(launcher, env=injected, capture_output=True, timeout=30)
    if rejected.returncode == 0 or b'Inherited JVM options' not in rejected.stderr:
        raise RuntimeError('JVM injection not rejected before launch.')
    checks.append('Inherited JVM database override: denied before connection')
    try:
        bad = json.loads(original); bad['jdbc_url'] = 'jdbc:mysql://127.0.0.1:3306/aoop?sslMode=REQUIRED'
        profile.write_text(json.dumps(bad))
        rejected = subprocess.run(launcher, capture_output=True, timeout=30)
        if rejected.returncode == 0 or b'JDBC target or TLS settings' not in rejected.stderr:
            raise RuntimeError('Mislabeled profile URL not rejected.')
    finally:
        profile.write_bytes(original)
    checks.append('Profile label and actual JDBC URL mismatch: denied before connection')
    admin = unprotect_credential(RUNTIME / 'admin.credential.xml')
    app = unprotect_credential(RUNTIME / 'app.credential.xml')
    p = json.loads(original)
    environment = os.environ.copy(); environment.update(M2_JDBC_URL=p['jdbc_url'], M2_ADMIN_PASSWORD=admin['password'], M2_APP_PASSWORD=app['password'])
    project = ROOT / 'AOOP - MotorPH from OOP'
    classpath = str(project / '.build/security-assurance') + ';' + str(project / '.build/classes') + ';' + str(project / 'lib/*')
    count = sql(admin['user'], admin['password'], "SELECT (SELECT COUNT(*) FROM users WHERE username='99001')+(SELECT COUNT(*) FROM employees WHERE EmployeeID=99001);", database='aoop_m2').strip()
    if count != '0':
        raise RuntimeError('Collision fixture ID occupied; preserving it.')
    sql(admin['user'], admin['password'], "INSERT INTO users(username,password,role,first_name,last_name,must_change_password) VALUES('99001','collision-sentinel-not-a-password','Employee','Collision','Fixture',1);", database='aoop_m2')
    try:
        failed = subprocess.run(['java', '-cp', classpath, 'SecurityAssurance', 'test'], env=environment, capture_output=True, timeout=40)
        intact = sql(admin['user'], admin['password'], "SELECT COUNT(*) FROM users WHERE username='99001' AND password='collision-sentinel-not-a-password';", database='aoop_m2').strip()
        if failed.returncode == 0 or intact != '1':
            raise RuntimeError('Assurance cleanup changed a pre-existing collision fixture.')
    finally:
        sql(admin['user'], admin['password'], "DELETE FROM users WHERE username='99001' AND password='collision-sentinel-not-a-password';", database='aoop_m2')
    checks.append('Occupied test account causes abort and remains untouched: success')
    evidence = Path.home() / 'Downloads/AOOP_Milestone2_Group13/evidence/review-correction-regressions.json'
    evidence.write_text(json.dumps({'checks': checks, 'assertions': len(checks), 'scope': 'Private synthetic escrow and isolated aoop_m2 only', 'original_database_writes': 0}, indent=2))
    print('Reviewer correction regressions passed: ' + str(len(checks)))


if __name__ == '__main__':
    main()
