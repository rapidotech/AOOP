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
| Password security | `AuthenticationService`, `PasswordService`, `PasswordResetDialog` | BCrypt12 verification, restricted reset state, and transactional credential replacement. |
| Database operations | `DatabaseHandler` | Prepared queries, employee/salary transactions, targeted authentication, and database views. |
| Connection configuration | `DatabaseConfig`, `PersistenceException` | External settings, per-operation connection ownership, bounded network waits, and safe failure messages. |
| Compatibility facade | `CSVHandler` | Retain existing call sites while delegating database and local-record operations. |
| Local record persistence | `LocalRecordStore` | Separate writable request/payroll records, quoted UTF-8 CSV, atomic replacement, and explicit I/O failures. |
| Maintenance utilities | `DataMigration`, `CheckTableSchema` | Existing manual utilities; normal startup does not run migrations. |

## Storage boundaries

The inspected local MySQL schema has normalized employees, compensation,
government IDs, attendance time logs, users, and payroll calculations. Attendance
contains dates/times/hours but has no leave request, approval, or leave-balance
columns. Payroll has calculated amounts but no payment-status column.

Keep database records and local requests distinct. Database reads retain their
existing source. Local requests use an explicit separate API; the UI identifies
their source. Bundled historical CSVs are not silently imported into a live
database or a new user store. Tests operate on temporary files and JDBC doubles.

Database operations open and close their own connections. A singleton
configuration object does not own a shared mutable connection. Multi-statement
employee/salary operations use transactions and propagate failures, so a caller
can only report success after persistence returns.

Login runs database work in a Swing worker and publishes results on the event
dispatch thread. Milestone 2 adds BCrypt12-only authentication and a restricted
reset state. Cloned legacy accounts receive unique temporary credentials from
protected enrollment. A reset rechecks proof under a row lock and commits before
a normal session is created. The original database remains unchanged. Normal
launch uses the protected isolated profile; see `security/README.md` for recovery.

## Repository layout

```text
scripts/                         reproducible build and visual verification
tools/                           isolated offline screenshot fixture
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

This is an incremental architecture cleanup. Existing forms still contain
workflow logic and some synchronous reads. `CSVHandler` remains a compatibility
facade rather than a new domain layer. Historical statutory formulas have not
been certified or updated. A local database schema inspection and isolated tests
do not establish production behavior or compliance.
