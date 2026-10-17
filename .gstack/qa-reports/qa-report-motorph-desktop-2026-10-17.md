# MotorPH native desktop QA

Status: DONE_WITH_CONCERNS. Full redesign and targeted fixes completed; independent visual disposition: ship. Baseline d7ee701.

This is an explicit native Swing adaptation of the web-oriented gstack QA
workflow. Browser navigation/DOM/console checks do not apply to this desktop
program. Owned-window Swing rendering, direct component actions, executable assertions
and isolated JDBC verification provide the relevant evidence. No
browser QA is claimed. Java17 regression infrastructure already exists; no
framework bootstrap or CI changes are needed. The baseline tree was clean.

## Baseline findings

ISSUE-001 (high, UX): fixed page geometry exceeds laptop work areas; profile
fields require scrolling through large vacant canvas. Reproduced in baseline
profile.png and profile-compact.png. Fix target: responsive semantic field groups
and a stable navigation shell.

ISSUE-002 (medium, visual): disconnected gray/green navigation and bulky legacy
chrome obscure page hierarchy. Reproduced in baseline records.png. Fix target:
consistent sidebar, headings, action bars and working surfaces.

ISSUE-003 (medium, accessibility/visual): the company label is clipped in the
baseline sign-in render; credentials and brand occupy a narrow fixed composition.
Reproduced in baseline login.png. Fix target: responsive split sign-in and full
organization identification.

Baseline captures: C:/Users/JAYCEE/.codex/tmp/astra-runs/aoop-20261017-ui-overhaul/baseline

## Test plan

Visit every frame and all three security/auth dialogs at desktop/laptop sizes;
check navigation, configured keyboard/focus behavior, read-only tables, controls and state messages.
Run existing asynchronous authentication/reset, worker/session and persistence
regressions. Exercise real native create/edit, salary save, request submit,
attendance approve/reject and payroll verify/approve/undo with owned disposable
rows on aoop_m2:3307. Never mutate the original aoop:3306.

Visual inspection is bounded to two batched rounds. Independent finish review
receives the direction contract and the whole captured viewport matrix.

## First integrated results

Native rendering/component matrix:148checks,0failures,38validself-printPNG.
Direct UI-to-clone workflow test confirmed create/edit employee, salary update,
request submit and attendance approve/reject, then exposed ISSUE-004 below.

ISSUE-004 (high,functional): Payroll approvals disables all actions for HR,
although its constructor and data layer authorize HR. Real native-button Verify
produced no DB transition; repeated independently in the HR fixture render where
all controls are disabled. Evidence:round1/payroll-approval-laptop.png. Fix target:
match the supported HR permission matrix without weakening backend checks.

Capture tooling correction: initial global Robot pass lost foreground focus and
produced an unrelated desktop capture; that entire incompletepass is excluded
from public QA evidence and retained privately only for failure diagnosis. No
further global input or desktop capture is used. Owned-window self-print and
direct Swing component actions prove application rendering/handlers; physical OS
keyboard/mouse/focus and browser behavior are not claimed. A new test-only JDBC
metadata bridge fixes the fixture's missing ResultSetMetaData contract.

## Independent finish findings

The fresh reviewer opened all38validviews and returned disposition:fix.
Structural overhaul/type/palette match the contract; corrections F1-F6 are
recorded in impeccable-finish-review-2026-10-17.md. They cover truthful modes,
HR payroll availability, shared flat controls/disabled contrast, complete laptop
navigation/identity, request-task density and contextual empty/selected states.

ISSUE-005 (high,functional): Add compensation is readonly and incorrectly
rejects existing employees; its DAL creates an incomplete new profile. Both
Add/Edit also perform a later blank-profile update after salary commit. Evidence:
round1/salary-create-laptop.png and salary-edit-laptop.png; confirmed in bounded
source review. The correction locks/requires the existing employee, atomically
inserts missing salary/statutory data with audit, rejects duplicates and removes
the subsequent profile update. Existing onboarding already provides default
compensation, so normal maintenance uses Edit.

ISSUE-006 (high,functional/UX): attendance create/edit mode and heading disagree;
edit can insert instead of preserving the original date key, and a failed edit
can mutate its in-memory original. Evidence:round1/edit-attendance-laptop.png;
confirmed controller path. Correction preserves the original until successful
save and uses the exact original key for edits.


## Final results and fixes

All six scored visual corrections are resolved in the independent final verdict.
The final set contains 42 distinct synthetic captures at desktop/laptop sizes,
including separate request creation and viewing states; two targeted attendance
captures replace the clipped versions. No unrelated desktop capture is included.

- Release gate: 351 regression assertions (Audit UI 32, Payroll UI permission 20,
  Persistence 37, Security 150, UI 112), focused security guard and current-source
  secret scan passed. Four dependency hashes verified; live OSV returned zero
  advisories for the checked dependencies.
- Native component checks: 176 (158 main plus 18 targeted checks); nine navigation
  routes and confirmed logout covered. This proves programmatic Swing actions,
  configured focus behavior and owned-window rendering, not physical input.
- Real UI-to-isolated-database workflows: 38 checks passed, including employee
  create/edit, compensation add/edit, rejection/rollback cases, attendance request
  key preservation, HR approve/reject, payroll transitions and audit integrity.
- Separate release integration: 76 backend runtime and 15 SQL/audit checks passed.

ISSUE-001?003 resolved by the structural redesign and shared control/layout fixes
(cab85dd, 1fd2257). ISSUE-004 resolved by permission/state-aware payroll actions
(3e71d1d). ISSUE-005 resolved by transactional existing-employee compensation
creation with profile preservation (d2c3247; final release includes the reviewed
explicit government-ID ownership checks). ISSUE-006 resolved by detached request
validation and original-key updates (d8708f1). Truthful form actions (a93a49b),
contextual states (eff94ff) and the final attendance clipping correction (a1c1c34)
complete F1?F6. Production source is frozen after the passing final gate.

A single scalar health score is omitted: browser performance, console and physical
OS-input categories have no equivalent measured baseline here. Known scored
findings are resolved; that is not a claim that all defects are eliminated.

## Evidence and limits

See FINAL_VISUAL_VERDICT.md, QA_RESULTS.json, final-release-gate.txt and
native-workflow-results.txt in the accompanying UI evidence archive. Screenshots
are synthetic test fixtures, not proof of the operator's current account data.
The seven before views are owned-window baseline captures. The incomplete initial
Robot pass is excluded. Physical keyboard/mouse/focus, physical printing and
performance benchmarks remain unverified. Existing synchronous reads and the
local direct-JDBC deployment boundary remain documented architecture limitations.
No original-database mutation or credential change is part of this UI redesign.
The original milestone security evidence remains historical; this is its UI/QA
addendum, with separate evidence scopes rather than overwritten implementation
notes. The final archive includes a SHA-256 manifest and a checked ZIP CRC.


## Post-delivery correction: production startup theme

ISSUE-007 (high, visual/functional): the real OOP_MotorPH launcher used Java's
Metal theme, while the QA harness explicitly selected Nimbus. The Nimbus-only
button painter was therefore absent in production, leaving white sign-in text
on a white surface. A user screenshot exposed this missed startup path.

AppTheme.install now selects Nimbus centrally before normal control creation.
The regression and native harness begin with Metal and call the same production
installer; neither substitutes Nimbus itself. The new regression failed before
the fix and passed after it. Button paint contrast and action bounds are checked
at 940- and 800-unit window widths. The sign-in image was visually inspected.

After correction: all 353 offline regression assertions passed (UI now 114).
The fresh full native matrix passed 163 checks, including nine navigation routes,
logout, all captured screens/dialogs, sign-in dispatch and both sign-in sizes.
Earlier release integration remains dated historical evidence, not a new release
run. The separate owned-clone workflow rerun is recorded alongside this addendum.
Physical input, printing and performance remain unverified; this does not claim
that every feature or possible input combination is proven correct.
