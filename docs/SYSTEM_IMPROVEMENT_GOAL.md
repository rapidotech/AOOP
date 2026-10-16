# AOOP system improvement goal

## Current authoritative scope

Review the entire MotorPH system and improve it using Astra full mode and
Impeccable for the Swing UI. Make the code consistent and maintainable, improve
the architecture, organize the repository, and optimize it where evidence
supports a change. The user delegates implementation and design decisions.

**Preserve existing functionality.** Login, employee self-service, employee
administration, attendance and leave, salary, payroll views, filters, printing,
and existing persistence entry points must remain available. Do not replace
implemented operations with unconditional unsupported-operation errors.

Work locally in phases. Preserve unrelated edits. Inspect local database schema
read-only when necessary; use isolated data for tests. Do not mutate the live
database, deploy, change statutory rates, or claim live integration from unit
tests. Do not use Graphify unless explicitly requested.

## Phases and acceptance criteria

### Expanded scope: Group 13 MO-IT153 Milestone 2

Complete all five Midgame Moves from the user-supplied guidelines using the
actual Group 13 SIP PDF and audit workbook in Downloads. Implement the two
selected controls: database credential protection and least privilege; BCrypt
password storage and default-password remediation. Make decisions autonomously.

Before security implementation, preserve a complete source/configuration/database
backup, commit a clearly identified baseline, establish an isolated cloned test
environment, and document/exercise rollback. Keep the original `aoop` database
and shared root account unchanged. Record exactly which environment each claim
applies to.

Deliver a control-to-surface mapping; an editable ongoing Implementation Log
with one block per control and actual tests/evidence/status; reproducible testing
and rollback instructions; and an updated annotated system diagram as an image
and PDF. Preserve the supplied PDF/XLSX unchanged. Final explanation should be
simple and cover both the cleanup work and the security implementation.

1. Inventory the architecture, source files, workflows, and persistence contracts.
   Record concrete defects and limitations before changing behavior.
2. Standardize configuration, connection ownership, failure handling, and file
   persistence while retaining compatible public entry points. Verify with
   isolated JDBC doubles and temporary CSV copies.
3. Improve UI consistency, keyboard use, window sizing, and asynchronous login.
   Preserve the MotorPH identity and existing screens. Inspect rendered Swing
   screens with synthetic data.
4. Standardize the build, resources, test runner, and repository layout. Remove
   generated artifacts from version control without deleting user data. Document
   the architecture and reproducible local commands.
5. Review the integrated diff independently, run the applicable regressions and
   build, and record exact remaining limitations and evidence.

## Tracking

Implementation and verification completed on 17 October 2026. The expanded
scope is recorded here because the native goal API cannot edit objective text.

- Standardized Java 17 builds, configuration and connection ownership,
  transactional writes, atomic local records, and shared UI styling across
  all 16 forms; generated output removed from version control.
- Completed the five Milestone 2 moves for the two selected SIP controls in
  a separate, backed-up MySQL clone, with the original database unchanged.
- Verified 75 persistence, 53 security and 63 UI assertions offline; 30 real
  isolated-database checks; six focused review-correction checks; and protected
  temporary-credential retrieval against all 34 enrolled cloned accounts.
- Delivered an editable ten-page Implementation Log, PDF, annotated diagram
  in HTML/SVG/PNG/PDF, and sanitized evidence. Every log page was rendered and
  visually inspected. Private recovery material is excluded from sharing.

The portfolio is at `C:/Users/JAYCEE/Downloads/AOOP_Milestone2_Group13`.
See `security/README.md` for launching, temporary credentials, reproduction,
rollback and the explicit limits of the assurance claims.

Run state: `C:/Users/JAYCEE/.codex/tmp/astra-runs/aoop-20261017-system-improvements/checkpoint.json`.
