# Milestone 2 security implementation

This implements the two Group 13 SIP controls: protected database credentials
with least privilege, and BCrypt storage with default-password remediation.
Effectiveness evidence applies to the isolated local clone.

## Environment and access

Original MySQL: port 3306, schema `aoop`, unchanged accounts/data/service.
Isolated MySQL: 8.0.42, `127.0.0.1:3307`, schema `aoop_m2`, separate storage at
`%USERPROFILE%/.motorph/milestone2`. The application credential is encrypted
with Windows DPAPI CurrentUser and limited by file ACL to that user and SYSTEM.
The launcher decrypts only for the child Java process and restores environment
variables afterward. It validates the actual JDBC URL, rejects inherited JVM
option injection, and checks the active account/database before normal launch.

TLS is required by the server and account. Connector/J uses `VERIFY_IDENTITY`
with a trusted local CA and matching localhost/127.0.0.1 certificate SAN.
The restricted app account has only the required application-table operations.
DDL, system-account data, unrelated schemas, grant delegation, and role-column
updates were denied. This does not establish per-user authorization.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Run
# The original run.bat also selects the protected profile on this prepared machine.
```

Cloned accounts now use unique temporary BCrypt12 credentials and require a
password change before normal access. Retrieve only your authorized test account
from the protected escrow on the provisioning Windows user/machine:

```powershell
powershell -NoProfile -File scripts/get-milestone2-bootstrap.ps1 -Username 10002 -CopyToClipboard
```

Paste the temporary password into MotorPH and complete the mandatory change.
Choose at least 12 Unicode characters, within 72 UTF-8 bytes. Known defaults,
repeated passwords, control characters, and silent truncation are rejected.
The backend locks and revalidates the current credential/reset state; a normal
session is established only after the BCrypt replacement commits.

The private admin credentials, bootstrap escrow, and full backup are not group
sharing artifacts. DPAPI ciphertext is bound to its Windows user/machine.
Another authorized machine needs its own protected profile.

## Verification

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Test
python scripts/check-security.py
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-milestone2.ps1 -SkipBuild -VerifyOnly
python tools/test_milestone2_guards.py
python tools/run_milestone2_assurance.py
```

Offline tests have no real MySQL driver at runtime. The final two commands use
only the marked clone and disposable records. Tests record exact denial codes,
not credential values. A colliding test account aborts and remains untouched.
The initializers are `tools/milestone2_environment.py` and
`tools/milestone2_secure_profile.py`; they require the protected baseline backup
and preserve existing marked state. They do not register a Windows service.

## Migration and recovery

`migrations/001_expand_password_reset.sql` is an administrator-only idempotent
expansion to `users.password VARCHAR(255)` and `must_change_password`. Enrollment
generates a random password for each legacy account and hashes that plaintext
directly. It never wraps a SHA digest and skips supported BCrypt12 rows on retry.
The parent process encrypts and round-trip validates escrow in a temporary
sibling file, atomically replaces it while retaining prior ciphertext, and only
then acknowledges the database commit. Missing acknowledgement rolls back.
Plaintext bundled account seeds were removed; their obsolete importer refuses
to run. `DatabaseHandler.createUser` provides the tested BCrypt account-creation
API after an employee profile exists.

Protected backups are at Downloads
`AOOP_Milestone2_Group13/private-backup/preimplementation-20261017`: full working
tree, Git bundle/index/patch state, settings, complete SQL dump, and hashes.
Baseline commit: `8387a21043f422e186a2a03a932eb8f50ee364c2`.

Recovery was rehearsed: rebuild the extracted baseline code, restore all 14
tables into the separate `aoop_m2_restore_check` schema, and compare row counts.
Use `scripts/stop-milestone2.ps1` to stop only the owned isolated process. Extract
baseline code into a separate folder and restore into a separate recovery schema
before replacing anything. Preserve current code/escrow until checked. Never
restore over the original database or automatically reactivate an exposed secret.

## Evidence and boundaries

The editable log, PDF, diagram, and sanitized evidence are in Downloads
`AOOP_Milestone2_Group13`. The supplied SIP PDF/XLSX remain unchanged. No school
Implementation Log template was supplied; this original log follows the fields
in the user-provided guidelines.

No production effectiveness, internet exposure test, physical printing, lockout,
new audit trail, per-user DAL authorization, or current statutory compliance is
claimed. Original shared-root rotation and Git-history remediation still need
separate coordinated work before real rollout. The old plaintext settings
fallback remains a compatibility API; the active verified profile uses DPAPI.

Official references:

- [BCrypt library API](https://github.com/patrickfav/bcrypt)
- [Windows DPAPI credential export](https://learn.microsoft.com/en-us/powershell/module/Microsoft.PowerShell.Utility/export-clixml?view=powershell-5.1)
- [MySQL TLS](https://dev.mysql.com/doc/refman/8.0/en/using-encrypted-connections.html)
- [MySQL backup behavior](https://dev.mysql.com/doc/refman/8.0/en/mysqldump.html)
