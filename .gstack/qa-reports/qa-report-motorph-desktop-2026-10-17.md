# MotorPH native desktop QA

Status: In progress. User-authorized full redesign at baseline d7ee701.

This is an explicit native Swing adaptation of the web-oriented gstack QA
workflow. Browser navigation/DOM/console checks do not apply to this desktop
program. Native Robot clicks, keyboard actions, window captures, executable
assertions and isolated JDBC verification provide the relevant evidence. No
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
check navigation, keyboard use, read-only tables, controls and state messages.
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
