# MotorPH architecture

MotorPH remains a Java Swing desktop application with the existing NetBeans
forms and public class names. Keeping those entry points avoids breaking the
form designer, launchers, and coursework references.

## Responsibilities

| Layer | Code | Responsibility |
| --- | --- | --- |
| Startup | `OOP_MotorPH` | Start Swing on its event dispatch thread and install shared presentation defaults. |
| Presentation | `frm_*.java`, matching `.form` files | Existing employee and HR workflows; generated layouts remain paired with their forms. |
| Presentation support | `AppTheme` | Shared colors, typography, focus, table defaults, reachable window content, and recoverable data errors. |
| Session and models | `EmpUserSession`, `EmpDetails`, `EmpAttLeave`, `EmpSalaryDetails`, `PayrollProcessor` | Existing session and domain data contracts. |
| Authentication | `AuthenticationService`, `PasswordService`, `PasswordResetDialog` | BCrypt12, persisted account/source throttles, restricted reset, committed failure audits and credential replacement. |
| Authorization | `AuthorizationService`, `EmpUserSession` | Single-use verified capabilities, immutable transaction identity, current-account validation, HR/self scope. |
| Database operations | `DatabaseHandler` | Prepared queries, employee/salary transactions, targeted authentication, and database views. |
| Connection configuration | `DatabaseConfig`, `PersistenceException` | External settings, per-operation connection ownership, bounded network waits, and safe failure messages. |
| Compatibility facade | `CSVHandler`, `DatabaseLedger` | Existing row-shaped APIs now use scoped database workflow ledgers and audited state transitions. |
| Audit | `AuditService`, private append procedure | Append-last business transactions, serialized chain, complete snapshot verification, HR activity. |
| Legacy files | `LocalRecordStore` | Read/parse historical files; writes are retired. Explicit HR import validates and reconciles into DB ledgers. |
| Maintenance | `security/migrations`, `tools/` | Guarded clone provisioning, controlled enrollment, testing, archive/export and recovery. Obsolete raw-SQL DataMigration entry points refuse execution. |

## Storage boundaries

The inspected local MySQL schema has normalized employees, compensation,
government IDs, attendance time logs, users, and payroll calculations. Attendance
contains dates/times/hours but has no leave request, approval, or leave-balance
columns. Payroll has calculated amounts but no payment-status column.

Migration002 adds `AttendanceRequests` and `PayrollLedger` without rewriting
normalized time logs or calculated payroll. Employee/period/transaction keys and
approval state are normalized; JSON arrays preserve the legacy20/22-field shapes.
Existing adapters read/write these tables transactionally. Pending requests and
payroll verification/approval transitions are checked under row locks; ordinary
edits cannot change approval state. Explicit historical import skips exact
duplicates, rejects conflicts and preserves source files. Bundled seeds are not
silently imported. Offline tests use JDBC doubles; runtime tests use the owned
clone and disposable records.

Database operations open and close their own connections. A singleton
configuration object does not own a shared mutable connection. Multi-statement
employee/salary operations use transactions and propagate failures, so a caller
can only report success after persistence returns.

Login runs database work in a Swing worker and publishes results on the event
dispatch thread. Milestone 2 adds BCrypt12-only authentication and a restricted
reset state. Cloned legacy accounts receive unique temporary credentials from
protected enrollment. A reset rechecks proof under a row lock and commits before
a normal session capability is issued. Rejected proofs commit persistent
throttle state and audit before returning a generic retry response. Each DAL
operation rechecks the authenticated database identity and uses a captured actor
for the same-transaction audit event. The original database remains unchanged. Normal
launch uses the protected isolated profile; see `security/README.md` for recovery.

## Repository layout

```text
scripts/                         reproducible build and visual verification
tools/                           isolated assurance, migration, export and portfolio tools
security/                        control plans, migrations, harnesses and dependency pins
.githooks/                       local staged secret gate
.github/workflows/               source/build security gates (remote execution separate)
docs/                            goal, architecture, review and evidence notes
AOOP - MotorPH from OOP/
  src/oop_motorph/                application source, forms and bundled resources
  lib/                           existing runtime dependencies
  test/oop_motorph/               executable offline regression tests
  nbproject/                     NetBeans build configuration
  .build/                        ignored current build/test output
```

Previously tracked `build/`, `dist/`, and root `out/` outputs are removed from the
index and ignored. Existing local copies are preserved. `.idea/`, local NetBeans
settings, and credential files are ignored for future changes; source, forms,
libraries, and data resources remain available.

## Known boundaries

The prepared clone uses loopback/TLS and Windows-bound DPAPI configuration.
The definer audit routine serializes a private chain head; the client has no
direct audit/head DML. Daily verified local exports preserve prior snapshots,
but independent off-host custody is not provisioned. Database administrators can
rewrite local history and anchors. Source-machine names are telemetry.

Application authorization protects the supported client; its shared direct JDBC
account cannot attest the human behind arbitrary SQL from a modified client.
An independently authenticated server/API is needed for that stronger boundary.
Original shared-root rotation, repository privacy and Git-history rewriting are
coordinated rollout tasks, not implied by local tests.

This is an incremental architecture cleanup. Existing forms still contain
workflow logic and some synchronous reads. `CSVHandler` remains a compatibility
facade rather than a new domain layer. Historical statutory formulas have not
been certified or updated. A local database schema inspection and isolated tests
do not establish production behavior or compliance.
