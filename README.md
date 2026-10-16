# MotorPH Payroll System

Java Swing employee, attendance, leave, salary, and payroll management for the
AOOP MotorPH project. Existing employee and HR workflows and NetBeans forms are
retained.

## Build and run

Requires a Java **JDK 17 or newer** with `javac` and `java` on PATH. Dependencies
are already in `AOOP - MotorPH from OOP/lib`; no global Ant or personal library
paths are needed for the PowerShell build.

```powershell
# From the repository root
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Test
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1 -Run
```

Windows users can also run `AOOP - MotorPH from OOP/run.bat`. The launcher stops
if compilation fails. Build output goes into ignored `.build/classes`, including
images and CSV resources. The VS Code build task uses the same script. NetBeans
uses relative library paths and targets Java 17.

## Database configuration

The application uses the existing MySQL schema; startup does not create or
migrate it. Configuration precedence is Java system properties, environment
variables, then the local UTF-8 properties file:

| Setting | Java property | Environment variable |
| --- | --- | --- |
| JDBC URL | `aoop.db.url` | `AOOP_DB_URL` |
| Username | `aoop.db.user` | `AOOP_DB_USER` |
| Password | `aoop.db.password` | `AOOP_DB_PASSWORD` |
| Optional config path | `aoop.db.config` | `AOOP_DB_CONFIG` |

Default local file: `%USERPROFILE%/.motorph/database.properties`, with keys
`url`, `user`, and `password`. Keep credentials out of source control. The URL
must begin with `jdbc:mysql://`; connection/socket waits are bounded. Use your
existing database account and schema. Credential rotation and account privilege
changes are separate database administration operations.

Login can open without a database. Milestone 2 adds BCrypt cost 12 and mandatory
password changes. The prepared isolated profile uses DPAPI credentials,
restricted grants, and verified TLS. See [security setup](security/README.md)
for bootstrap access and the unchanged original database boundary.

## Local request data

Local attendance requests and legacy payroll CSV operations use a separate
writable directory selected by `aoop.data.dir` / `AOOP_DATA_DIR`, with a default
under `%USERPROFILE%/.motorph`. Database time logs and local requests remain
distinct. Bundled historical CSVs are preserved, not automatically imported.
See [architecture](docs/ARCHITECTURE.md) for storage boundaries and limitations.

## Verification

`-Test` compiles and runs offline regression programs using temporary CSV files
and JDBC doubles. It does not modify the configured database.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verify-ui.ps1
```

Visual verification opens short-lived Swing windows with synthetic session data
and an in-process JDBC fixture. It removes real JDBC drivers inside that separate
fixture process, refuses writes, and never opens a database connection. Captures
cover login, employee profile, HR records, and payroll at normal and smaller
window sizes. This is visual evidence, not live database integration proof.

[Improvement goal](docs/SYSTEM_IMPROVEMENT_GOAL.md) |
[Architecture](docs/ARCHITECTURE.md) | [Milestone 2 security](security/README.md)

## Authors

- Juan Carlos Miguel Basa
- Charles Mikael Medel
- Mikko Jerome Bautista
- Frances Balgos
- Clark Anton Juan Diaz
