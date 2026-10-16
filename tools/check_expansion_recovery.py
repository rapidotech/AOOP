"""Restore the pre-expansion clone baseline into a separate owned recovery schema."""
from pathlib import Path
import json
from milestone2_environment import RUNTIME, sql, unprotect_credential


def main():
    marker = json.loads((RUNTIME / 'environment.json').read_text())
    if marker['port'] != 3307 or marker['schema'] != 'aoop_m2':
        raise ValueError('Unexpected environment')
    admin = unprotect_credential(RUNTIME / 'admin.credential.xml')
    identity = sql(admin['user'], admin['password'], 'SELECT @@port,DATABASE();', database='aoop_m2').strip()
    if identity != '3307\taoop_m2':
        raise ValueError('Unexpected recovery server')
    name = 'aoop_m2_expansion_restore_check'
    owned = RUNTIME / 'expansion-recovery-owner.json'
    exists = sql(admin['user'], admin['password'], "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='" + name + "';").strip() == '1'
    if exists and (not owned.exists() or json.loads(owned.read_text()).get('schema') != name):
        raise ValueError('Unowned recovery schema preserved')
    dump = Path.home() / 'Downloads/AOOP_Milestone2_Group13/private-backup/pre-expansion-9ba48e6/aoop_m2-9ba48e6.sql'
    if not dump.is_file() or dump.stat().st_size < 1000:
        raise ValueError('Missing expansion backup')
    if not exists:
        sql(admin['user'], admin['password'], 'CREATE DATABASE ' + name + ' CHARACTER SET utf8mb4;')
        owned.write_text(json.dumps({'schema': name, 'port': 3307}))
    sql(admin['user'], admin['password'], database=name, dump=dump.read_bytes())
    expected = marker['restored_table_counts']
    actual = {table: int(sql(admin['user'], admin['password'], 'SELECT COUNT(*) FROM `' + table + '`;', database=name).strip()) for table in expected}
    if actual != expected:
        raise ValueError('Recovery row-count mismatch')
    bcrypt = sql(admin['user'], admin['password'], "SELECT COUNT(*),SUM(password LIKE '$2b$12$%'),SUM(must_change_password=1) FROM users;", database=name).strip()
    if bcrypt != '34\t34\t34':
        raise ValueError('Recovery credential/reset baseline mismatch')
    evidence = Path.home() / 'Downloads/AOOP_Milestone2_Group13/evidence/expanded-recovery.json'
    evidence.write_text(json.dumps({'baseline': '9ba48e6', 'recovery_schema': name, 'tables': len(actual),
        'row_counts_match': True, 'bcrypt12_pending_accounts': 34, 'original_database_writes': 0,
        'scope': 'Restore of pre-expansion clone backup into a separate owned schema; no active clone replacement'}, indent=2))
    print('Expansion recovery verified:14 tables and34 BCrypt/reset accounts restored in separate owned schema.')


if __name__ == '__main__':
    main()
