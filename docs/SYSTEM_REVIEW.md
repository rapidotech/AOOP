# System review and implementation record

## Scope and decisions

The user requested a whole-system review, code and architecture cleanup,
repository organization, optimization, and Impeccable UI improvements, with
decisions delegated to the agent and existing functionality preserved.

The implementation keeps Java Swing, NetBeans form names, public compatibility
methods, the MotorPH identity, and existing statutory calculation behavior.
Configuration, storage, transaction management, and presentation support now have
separate owners in the code. A framework replacement or wholesale package rename
would add compatibility risk without resolving the observed defects.

## Review coverage

| Area | Findings and implemented response |
| --- | --- |
| Startup/build | Startup moved to the Swing event thread. Build and VS Code task target Java 17, use bundled relative dependencies, copy resources, and stop on compile failure. |
| Configuration | Removed embedded connection secrets. Added properties/environment/user-local settings and bounded connection/socket/query waits. Existing local credentials were migrated outside the repository without changing the database account. |
| Database ownership | Replaced a shared connection with one owned connection per operation. Centralized transactions and safe failures. Employee-plus-salary creation rolls back as one transaction. |
| Authentication/session | Targeted account/employee queries replace full-table login loading. Login is asynchronous, prevents duplicate submissions, preserves password whitespace, supports Enter and mnemonics, and gives recoverable error/loading states. Existing hash format is retained. |
| Employee/profile/salary forms | Shared presentation rules, existing field/navigation/role behavior retained, smaller windows have scroll access, and supervisor debug output removed from normal profile loading. |
| Attendance/leave | Inspected normalized database schema read-only. Preserved DB time-log reads and added separately identified local requests. Existing request/edit/approval APIs persist atomically; approvals select the exact employee/date period. |
| Payroll | Preserved database views, filters, and print routes. Fixed invalid row-column selection. Existing local approval APIs use exact transaction/employee/period keys and update visual state after persistence succeeds. Former no-op Process/Adjust handlers no longer claim payment occurred. |
| CSV/file handling | Added one local store with UTF-8 quoting/multiline round trips, header/width validation, atomic replacement, and explicit I/O failure. Historical resource data is not silently imported. |
| UI consistency | All 16 form classes use shared typography, contrast, keyboard focus, table and error treatment. MotorPH artwork remains. Source notes distinguish database calculations/time logs from local request/approval records. |
| Repository | Removed 390 generated `build`/`dist`/`out` files from the Git index, leaving their local copies intact. Added ignore rules, editor conventions, scripts, tests, tooling, and architecture documentation. |

## Verification evidence

- Original sources compiled successfully before implementation on Java 17.
- The integrated build and executable persistence/UI regressions run through
  `scripts/build.ps1 -Test` using JDBC doubles, injected authentication, and
  temporary record files. The final assertion totals are in the Astra run report.
- Regression coverage includes transaction rollback, owned connections,
  configuration precedence, exact request/payroll approval keys, failed saves
  retaining pending UI state, CSV quoting, failed-file-write preservation,
  responsive login, Enter submission, exact passwords, and sorted-row mapping.
- Read-only smoke against the existing local database passed employee, salary,
  time-log, and payroll reads: 31 / 31 / 4,712 / 217 rows respectively. An existing
  documented account authenticated successfully. Each smoke connection was set
  read-only; account and record contents were withheld.
- Seven synthetic captures cover login, employee profile, HR records, and payroll
  at normal/compact sizes. No employee data or configured database connection is
  used by the screenshot fixture. Two batched UI inspection rounds were used.
- Independent review found overly broad approval targeting and premature visual
  success; these were corrected with exact keys and persistence-before-display.

Evidence paths for this run:

- `%USERPROFILE%/.codex/tmp/astra-runs/aoop-20261017-system-improvements/`
- `%TEMP%/aoop-ui-final/`
- `%TEMP%/aoop-schema-readonly/readonly-smoke.txt`

## Practical limits

No live database CRUD, schema migration, credential rotation, deployment,
physical printing, or current statutory-rate validation was performed. Existing
password hashes remain SHA-256 and need a separately verified migration.

The normalized DB schema cannot store leave approvals or payment status. Those
remain explicitly local records; a new local ledger starts empty. Historical
external CSV records can be used by selecting their directory; they are not
automatically merged or copied. Database time logs cannot be approved as local
requests. The previous Process/Adjust payment handlers had no implementation;
their replacement message accurately reports that no payment was saved.

The local ledger serializes updates inside one JVM. Multiple simultaneous app
processes or shared network storage need a future locking/database-backed design.
Existing large forms retain some synchronous data loading and mixed presentation
and workflow logic. Shared helpers reduce duplication without claiming a complete
rewrite into separate service/repository packages.

The final visual batch retains the original broad desktop composition, with
scrolling at smaller sizes. It is not a replacement responsive layout. Minor
label/disabled-text defects found during the final batch are handled only by
narrow deterministic checks, without starting another visual-polishing loop.
