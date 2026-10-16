"""Apply additive expanded-control migration to the marked isolated clone only."""
from pathlib import Path
import json
from milestone2_environment import RUNTIME, sql, unprotect_credential

ROOT = Path(__file__).resolve().parents[1]


def main():
    marker = json.loads((RUNTIME / 'environment.json').read_text())
    if marker['port'] != 3307 or marker['schema'] != 'aoop_m2':
        raise ValueError('Unexpected environment')
    admin = unprotect_credential(RUNTIME / 'admin.credential.xml')
    app = unprotect_credential(RUNTIME / 'app.credential.xml')
    if app['user'] != 'motorph_m2_app':
        raise ValueError('Unexpected application account')
    identity = sql(admin['user'], admin['password'], 'SELECT @@port,DATABASE();', database='aoop_m2').strip()
    if identity != '3307\taoop_m2':
        raise ValueError('Refusing a different database')
    backup = Path.home() / 'Downloads/AOOP_Milestone2_Group13/private-backup/pre-expansion-9ba48e6/aoop_m2-9ba48e6.sql'
    if not backup.is_file() or backup.stat().st_size <= 1000:
        raise ValueError('Expansion baseline required')
    migration = ROOT / 'security/migrations/002_authorization_lockout_audit_ledgers.sql'
    sql(admin['user'], admin['password'], database='aoop_m2', dump=migration.read_bytes())
    account = "'motorph_m2_app'@'127.0.0.1'"
    grants = [
        f'GRANT SELECT,INSERT,UPDATE ON aoop_m2.AuthThrottle TO {account}',
        f'GRANT SELECT,INSERT,UPDATE,DELETE ON aoop_m2.AttendanceRequests TO {account}',
        f'GRANT SELECT,INSERT,UPDATE,DELETE ON aoop_m2.PayrollLedger TO {account}',
        f'GRANT UPDATE(password,must_change_password,failed_attempts,locked_until,next_login_at) ON aoop_m2.users TO {account}',
        f'GRANT EXECUTE ON PROCEDURE aoop_m2.motorph_append_audit TO {account}',
    ]
    sql(admin['user'], admin['password'], ';'.join(grants) + ';', database='aoop_m2')
    output = Path.home() / 'Downloads/AOOP_Milestone2_Group13/evidence/expanded-migration.json'
    output.write_text(json.dumps({'database': 'aoop_m2', 'port': 3307, 'migration': migration.name,
        'identity_verified': True, 'grants': sql(admin['user'], admin['password'], 'SHOW GRANTS FOR ' + account + ';').splitlines(),
        'original_database_writes': 0, 'rollback_baseline': str(backup)}, indent=2))
    print('Expanded migration applied to marked clone; audit DML remains unavailable to application account.')


if __name__ == '__main__':
    main()
