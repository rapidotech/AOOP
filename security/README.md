# Milestone 2 security implementation

This implements the two originally selected Group13 SIP controls plus the
worthwhile additional audit proposals authorized in the expanded goal:
persistent authentication throttling, DAL/object authorization, transactional
audit logging and database-backed workflow ledgers. Supporting repository,
dependency, transport and release checks are included. Effectiveness evidence
applies to the isolated clone and supported desktop client.

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
python tools/run_expanded_assurance.py
python tools/test_integrity_gates.py
python tools/test_repository_controls.py
python tools/archive_audit.py
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify.ps1 -Integration
```

Offline tests have no real MySQL driver at runtime. Database assurance, guard
and archive commands require the marked clone; mutation tests use disposable
records. Tests record exact denial codes,
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

No production effectiveness, internet exposure test, physical printing or current
statutory compliance is claimed. Original shared-root rotation and Git-history/
repository-visibility remediation need coordinated rollout. The plaintext settings
fallback remains a compatibility API; the active verified profile uses DPAPI.

## Expanded controls

Apply `tools/prepare_expanded_controls.py` only after the protected source/clone
backup exists. Migration002 adds counters, source throttles, workflow ledgers and
the audit procedure/tables without changing original3306. The pre-expansion
recovery baseline is private-backup/pre-expansion-9ba48e6: source archive, complete
clone dump, protected settings/escrow and TLS configuration. Recover into a separate
schema/directory and validate before replacing the owned clone.

Five failed credential proofs cause a15-minute account/source lock. Earlier
failures enforce1/2/4/8-second retry deadlines, including reset proof. Failed
attempt state and audit commit before the generic rejection. Login/reset timers
remain asynchronous. HR's security activity panel shows the latest100 failure/
lockout events with UTC times; it is not an all-time count or email notification.

Only successful committed authentication can issue the single-use capability
accepted by `EmpUserSession.establish`. The DAL rechecks database role/reset/
credential state. Employees read only their own records and create/edit their
own Pending requests. HR manages records, salaries, account enrollment, workflow
approvals, imports and security activity. Every restricted constructor checks
before initialization. Bulk credential dumping is denied.

HR can open the read-only Audit trail viewer from Security activity. It uses
100-row keyset pages, UTC metadata and plain-text details; newest/older navigation
does not expose credentials or full sensitive records. Workflow events include
only allowlisted status-before/status-after values in the same transaction.

Audit append is a DEFINER routine that locks a private chain head and never
commits on its own. The application has EXECUTE/SELECT but no direct audit/head
DML. Business changes and their event share one transaction. Whole-chain/head
verification uses a consistent snapshot. Event payloads omit credentials, hashes
and payroll/personnel record bodies. Source-machine labels are telemetry.

Attendance requests and payroll workflow records now live in `AttendanceRequests`
and `PayrollLedger`, preserving the previous20/22-field compatibility shapes.
Normal CSVHandler writes are audited database operations. Status transitions are
enforced under row locks; ordinary edit/save cannot bypass approvals. The old
file writers and obsolete raw-SQL migration generator are retired. Normal payroll
UI processing remains available; statutory calculations were not redesigned.

`DatabaseHandler.importLegacyLedgers` is an authenticated-HR maintenance API.
It validates all rows, imports in one audited transaction, skips only exact
duplicates and aborts conflicting records. Preserve source CSVs and compare
row/field counts. No external local ledgers existed on this computer; disposable
fixtures exercised exact reimport/conflict behavior. Bundled normalized CSV seeds
are not silently reimported as new workflow requests.

`archive_audit.py` verifies and exports a consistent snapshot plus SHA256 manifest
to the private runtime directory. `--verify <file>` checks an existing archive.
`--destination <existing authorized directory>` explicitly copies/checksums an
export; R2 uses the separate verified transport described below. The installed task
`MotorPH-M2-Audit-Archive` runs daily23:00 while the provisioning Windows user is
signed in. Database availability is required; failures produce a nonzero task
result. Remove only this owned task with `Unregister-ScheduledTask` if retiring
the lab. Local export does not establish independent off-host custody.

The prepared PC now also runs a private, digest-pinned RustFS1.0.2 Docker service
on loopback19000/19001. A separate scoped writer uploads versioned audit snapshots,
retrieves/checks their full chain and SHA256, and verifies seven-day COMPLIANCE
retention. The daily wrapper uses it when the protected profile exists. Eighteen
real S3 checks and an actual scheduled invocation passed. This remains same-PC
custody; see [setup, evidence and limits](object-storage/README.md).

Cloudflare R2 now also stores private off-host copies, verified by 20 live checks
and an actual successful scheduled invocation. The bucket-scoped writer cannot
change configuration or access another bucket. Seven-day bucket locks block
deletion/overwrite while active; a read-only identity checks their configuration.
The eight-day expiry rule is configured but its future execution is not yet
observed. R2 locks are administrator-removable, unlike the local service's tested
COMPLIANCE API semantics. See the [R2 runbook](object-storage/CLOUDFLARE_R2.md)
for recovery, token expiry, rollback and metered free-tier safeguards. The
bootstrap credential remains on the PC; independently administered custody and
a hard zero-cost account guarantee are not established.

`scripts/verify.ps1 -Integration` is the complete prepared-machine gate. CI's
source/build checks do not replace its real database tests. Gitleaks with the
custom low-entropy rule is installed in the local commit hook; install it on
another machine and run `git config core.hooksPath .githooks`. Python optimization
cannot bypass archive/dependency integrity validation. OSV feed unavailability
or returned advisories fail the dependency gate. Current JAR hashes are in
security/dependencies/inventory.json. Connector/J9.7.0 replaces vulnerable8.0.33.

The expanded assessment is `security/EXPANDED_CONTROL_PLAN.md`. C5's tested
transactional audit and local/off-host copies are verified within their stated
scope. Independent administration is outside that proof. A modified desktop client holding the shared JDBC
credential can still issue SQL permitted by that account; a trusted server/API
is needed to enforce human authorization outside this supported client. A DBA
can rewrite local database history and local anchors. Email and expanded roles
remain deferred. Semgrep/Dependency-Check were not successfully rerun in this
Windows environment; live OSV and Gitleaks are recorded with their actual scopes.

Official references:

- [BCrypt library API](https://github.com/patrickfav/bcrypt)
- [Windows DPAPI credential export](https://learn.microsoft.com/en-us/powershell/module/Microsoft.PowerShell.Utility/export-clixml?view=powershell-5.1)
- [MySQL TLS](https://dev.mysql.com/doc/refman/8.0/en/using-encrypted-connections.html)
- [MySQL backup behavior](https://dev.mysql.com/doc/refman/8.0/en/mysqldump.html)
