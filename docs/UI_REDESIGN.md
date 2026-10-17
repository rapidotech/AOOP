# MotorPH desktop workspace

The application now shares a forest navigation rail, light working surfaces,
an authenticated identity header, consistent actions and flat native controls.
Employee and HR sections remain distinct. Navigation keeps the current window
size; forms reflow into one or two columns and dense tables scroll horizontally.
All original frame entry points and security checks remain in use.

`WorkspaceShell` owns navigation and the page frame. `WorkspaceUI` builds field
groups, sections, actions and tables. `AppTheme` owns colors, typography, native
painters, focus and recoverable error presentation. Forms compose existing
controls and listeners rather than replacing their business workflows. Retired
`.form` files explicitly identify their historical status; do not regenerate
their old fixed layouts over the Java compositions.

Create, view and edit states now have distinct actions. Payroll actions require
both HR authorization and an appropriate selected record/status. Empty lists
explain their next step. Attendance requests preserve their original key until
an edit commits; rejected edits preserve the original record and entered fields.

Normal employee onboarding includes default compensation. Use Edit compensation
for routine maintenance. Add compensation repairs an existing employee that has
no compensation row; it does not create or overwrite a personnel profile. Salary
and statutory-ID changes share one audited transaction. Identifier collisions,
ambiguous records and audit failure reject the whole operation.

## Reproduce verification

```powershell
./scripts/verify.ps1 -Integration
python tools/run_native_ui_qa.py --output .impeccable/review/local
python tools/run_native_ui_qa.py --output .impeccable/review/local-workflows --integration
```

The first native command uses fake JDBC and synthetic records to capture every
screen and exercise component actions/navigation. It never captures the user's
desktop or sends global input. The second requires the prepared, owned
aoop_m2:3307 clone and creates/removes disposable fixtures; no original database
is modified. It deliberately tests failures and transaction rollback.

Native captures use the window's own rendering. Keyboard bindings are checked
programmatically. Browser behavior, physical OS input/focus, physical printing
and performance benchmarks are separate claims. The QA report records exact
scope, evidence, independent visual review and remaining assurance boundaries.

AppTheme.install selects Nimbus centrally at application startup. The native and
UI regression harnesses start from Metal and exercise that production installer,
including sign-in paint contrast and visible bounds at both supported widths.
