"""Run expanded assurance against the marked clone; never targets the original DB."""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import copy
import hashlib
import json
import os
import re
import secrets
import subprocess
from milestone2_environment import MYSQL, RUNTIME, sql, unprotect_credential
from archive_audit import snapshot, verify

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = Path.home() / 'Downloads/AOOP_Milestone2_Group13/evidence'


def denial(credential, statement, codes):
    client = RUNTIME / ('denial-' + secrets.token_hex(8) + '.cnf')
    password = credential['password'].replace('\\', '\\\\').replace('"', '\\"')
    client.write_text('[client]\nuser="' + credential['user'] + '"\npassword="' + password + '"\nhost=127.0.0.1\nport=3307\nssl-mode=REQUIRED\n')
    try:
        result = subprocess.run([str(MYSQL / 'mysql.exe'), '--defaults-file=' + str(client), '--database=aoop_m2'],
                                input=statement.encode(), capture_output=True)
        match = re.search(rb'ERROR (\d+)', result.stderr)
        assert result.returncode and match and int(match[1]) in codes, 'Expected SQL privilege/transaction rejection was not observed'
        return int(match[1])
    finally:
        client.unlink(missing_ok=True)


def main():
    if not __debug__:
        raise RuntimeError('Assurance requires normal Python validation mode; optimization is refused.')
    marker = json.loads((RUNTIME / 'environment.json').read_text())
    assert marker['port'] == 3307 and marker['schema'] == 'aoop_m2'
    admin = unprotect_credential(RUNTIME / 'admin.credential.xml')
    app = unprotect_credential(RUNTIME / 'app.credential.xml')
    assert sql(admin['user'], admin['password'], 'SELECT @@port,DATABASE();', database='aoop_m2').strip() == '3307\taoop_m2'
    outcomes = []
    for query, name in [
        ('UPDATE AuditLog SET Details=Details', 'Audit UPDATE denied'),
        ('DELETE FROM AuditLog WHERE EventID=-1', 'Audit DELETE denied'),
        ('UPDATE AuditChainHead SET LastHash=LastHash WHERE ChainID=1', 'Chain-head UPDATE denied'),
        ("INSERT INTO AuditLog(EventID) VALUES(-1)", 'Direct audit INSERT denied'),
        ('SELECT 1 FROM mysql.user LIMIT 1', 'System account data denied'),
        ('SELECT 1 FROM aoop.users LIMIT 1', 'Original schema access denied'),
        ('CREATE TABLE forbidden_probe(id INT)', 'Schema DDL denied'),
        ("UPDATE users SET role='HR' WHERE username='99003'", 'Role-column mutation denied'),
        ("GRANT SELECT ON aoop_m2.* TO 'motorph_m2_app'@'127.0.0.1'", 'Grant delegation denied'),
    ]:
        code = denial(app, query + ';', {1142, 1143, 1044})
        outcomes.append(name + ': SQL ' + str(code))
    denial(app, "CALL motorph_append_audit('assurance','TEST','isolated','SUCCESS','test','no transaction');", {1644})
    outcomes.append('Audit append requires caller transaction')
    before = snapshot()
    sql(app['user'], app['password'], "SET autocommit=0; CALL motorph_append_audit('assurance','TEST_ROLLBACK','isolated','SUCCESS','test','rolled back probe'); ROLLBACK;", database='aoop_m2')
    assert snapshot()['head'] == before['head']
    outcomes.append('Rolled-back append leaves chain/head unchanged')
    def append(number):
        sql(app['user'], app['password'], "SET autocommit=0; CALL motorph_append_audit('assurance','CONCURRENT_TEST','isolated','SUCCESS','test','worker " + str(number) + "'); COMMIT;", database='aoop_m2')
    with ThreadPoolExecutor(max_workers=4) as pool:
        list(pool.map(append, range(8)))
    after = snapshot()
    assert after['head']['id'] == before['head']['id'] + 8
    verify(after)
    outcomes.append('Eight concurrent committed appends form one complete valid chain')
    for label, change in [
        ('entry edit', lambda x: x['events'][-1].update(details='modified copy')),
        ('tail removal', lambda x: x['events'].pop()),
        ('head rewind', lambda x: x['head'].update(id=0, hash='0' * 64)),
    ]:
        damaged = copy.deepcopy(after)
        change(damaged)
        try:
            verify(damaged)
        except ValueError:
            outcomes.append('Verifier rejects ' + label + ' in an archive copy')
        else:
            raise AssertionError('Tamper fixture passed verification')
    classes = ROOT / 'AOOP - MotorPH from OOP/.build/classes'
    harness = ROOT / 'AOOP - MotorPH from OOP/.build/expanded-assurance'
    harness.mkdir(exist_ok=True)
    libraries = ROOT / 'AOOP - MotorPH from OOP/lib/*'
    compile_result = subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-cp', str(classes) + ';' + str(libraries),
        '-d', str(harness), str(ROOT / 'security/tools/ExpandedSecurityAssurance.java')], capture_output=True)
    if compile_result.returncode:
        (RUNTIME / 'expanded-compile-diagnostic.txt').write_bytes(compile_result.stderr)
        raise RuntimeError('Expanded assurance compilation failed; private diagnostic retained.')
    environment = os.environ.copy()
    profile = json.loads((RUNTIME / 'profile.json').read_text(encoding='utf-8-sig'))
    environment.update(M2_JDBC_URL=profile['jdbc_url'], M2_ADMIN_PASSWORD=admin['password'], M2_APP_PASSWORD=app['password'],
                       COMPUTERNAME='M2-ASSURANCE-' + secrets.token_hex(8))
    result = subprocess.run(['java', '-ea', '-cp', str(harness) + ';' + str(classes) + ';' + str(libraries),
        'oop_motorph.ExpandedSecurityAssurance'], env=environment, capture_output=True, text=True, timeout=150)
    if result.returncode:
        (RUNTIME / 'expanded-assurance-diagnostic.txt').write_text(result.stderr)
        raise RuntimeError('Expanded runtime assurance failed; private diagnostic retained.')
    runtime = json.loads(result.stdout)
    verify(snapshot())
    report = {'scope': 'Real isolated MySQL8.0.42 aoop_m2:3307; disposable fixtures only',
        'sql_checks': len(outcomes), 'sql_outcomes': outcomes, 'runtime': runtime,
        'original_database_writes': 0, 'archive_custody': 'local; external destination not provisioned'}
    (EVIDENCE / 'expanded-controls-integration.json').write_text(json.dumps(report, indent=2))
    print('Expanded assurance passed: ' + str(len(outcomes)) + ' SQL/chain checks; ' + str(runtime['assertions']) + ' runtime checks. Original database untouched.')


if __name__ == '__main__':
    main()
