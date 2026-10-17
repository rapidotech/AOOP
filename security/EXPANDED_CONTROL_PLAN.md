# Expanded control assessment

User-authorized expansion, 17 October 2026. Baseline: `9ba48e6`.
The supplied SIP selected C1/C2; the audit worksheet proposed the wider set
below. The current goal includes all worthwhile feasible additions. This is
an implementation decision record, not a retroactive change to the original SIP.

| Proposal | Decision and value | Implementation boundary |
| --- | --- | --- |
| C1 protected configuration, restricted grants, secret scanning | Keep; reduces credential exposure and database privileges. Extend regression and commit checks. | Existing DPAPI/TLS clone; custom scanner and Gitleaks. |
| C2 BCrypt and mandatory default reset | Keep; already verified. Preserve regression coverage. | Existing BCrypt12/reset flow and protected enrollment. |
| C3 lockout, progressive delay, authentication events, desktop alert | Implement; unlimited attempts remain a material gap. | Persistent account and source throttling, 5 failures/15 minutes, 1/2/4/8-second retry times, reset-proof coverage, HR activity notification. |
| C4 DAL and object authorization; constructor guards | Implement; UI-only restrictions are insufficient. | Default denial, employee self scope, HR administration, verified authentication capabilities, guards before loading forms. |
| C5 transactional hash-chained audit and CSV coverage | Implement; records must cover actual sensitive writes. | Serialized database audit chain; mutation and event in one transaction; database-backed request/payroll workflow ledgers; controlled legacy import. |
| Network and transport | Keep; existing clone binds loopback and verifies TLS identity. | Recheck clone listener and plaintext denial; do not change the shared original service or host firewall. |
| Repository hygiene | Extend; prevents reintroducing published credential patterns. | Tracked-output exclusions, removed reusable seeds, custom redacted secret scan, commit hook and CI. |
| Build reproducibility | Keep and verify. | Java 17 target, UTF-8, bundled pinned libraries, stop on compile/test failure. |
| Release evidence and dependencies | Extend; scanner success is not functional assurance. | Offline tests plus isolated security suite; hashes and live vulnerability advisories; fail closed on missing advisory feed. |
| Daily off-host audit archive | Local export/checkpoint plus private RustFS Docker object storage implemented and tested. | Versioning, seven-day API retention, retrieval integrity and local scheduler verified. Docker runs on the same PC; independent off-host custody remains unverified. |
| Shared-root rotation, history rewriting, repository visibility | Valuable rollout tasks; do not perform against this shared original environment. | Existing clone credential is rotated. Global root rotation/history rewrite/privacy changes affect collaborators and shared services and need coordinated rollout. |
| Email alerts and expanded Payroll/Admin roles | Keep deferred. | Desktop HR notification is feasible now; email infrastructure and new role responsibilities are absent. Do not invent organization policy. |
| Transparent legacy rehash | Superseded for these accounts by controlled enrollment/reset. | Plaintext-verified rehash can be legitimate, but all34 known default accounts needed unique replacements. Never apply BCrypt to an old SHA digest. |
| Password-column encryption | Defer the desktop-key variant. | A key shipped with the client does not fix shared-client access; it adds key rotation/recovery complexity around already salted password hashes. |
| HikariCP pool | Defer pending measurement. | No connection bottleneck has been established in this desktop workload. Per-operation ownership is already bounded and correct. |
| Old/new audit values | Implement a redacted variant. | Record before/after workflow statuses captured under the row lock. Exclude passwords, hashes, salary figures and full personnel records. |
| HR/Admin read-only audit viewer | Implement with the existing HR role. | A separate paginated business audit view complements authentication alerts. No new Admin role is invented. |

Direct JDBC remains a two-tier trust boundary: application-level authorization
protects normal entry points, but cannot stop a modified desktop client holding
the shared database credential from issuing its own permitted SQL. Database
administrator access can rewrite local logs/checkpoints; a hash chain alone
does not prove independent custody. These limitations must remain in the log.

Acceptance requires runtime evidence for permitted and denied access, lockout
persistence, reset throttling, transactional audit rollback, chain verification,
concurrent appends, audit UPDATE/DELETE denial, and legacy-ledger reconciliation.
Existing application behavior must remain usable for authorized roles.

Design references: [OWASP authentication](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html),
[authorization](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html),
and [logging](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html).
