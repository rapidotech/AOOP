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
