"""Create the voluntary study reviewer from inspected local implementation evidence."""
from pathlib import Path
import shutil
from xml.sax.saxutils import escape
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'output/pdf/MotorPH_Group13_MS2_Validation_Reviewer.pdf'

PAGES = [
    ('MotorPH Milestone 2 Validation Reviewer', [
        ('Group 13 study guide', 'Use this to understand the implementation and locate its evidence. The required submission is your shared Implementation Log. This reviewer is optional study material, not a required presentation, a script, or a substitute for doing and understanding the work.'),
        ('What Milestone 2 demonstrates', 'The security plan was connected to real system components, implemented in a safe test copy, and checked using expected versus observed behavior. Evidence belongs to the isolated local MotorPH environment and the supported application, not to an independently assessed production deployment.'),
        ('The two selected controls', '<b>C1:</b> Protect database credentials and give the application only the database privileges it needs. <b>C2:</b> Replace weak/shared passwords with salted BCrypt hashes and require temporary-password changes. These are the two originally selected SIP controls. C3 to C5 were implemented later as supporting improvements; keep that scope change clear.'),
        ('What the group needs to submit', 'One shared, complete Implementation Log link, submitted by every member. Its header needs Project Title, Group No. or Name, System Link, and Updated Diagram Link. For each selected control, include surface mapping, goal, exact location, tools and versions, implementation status, tests, observations or evidence, and post-test status.'),
        ('Submission gaps found on review', 'The inspected log contains the title/group, control records, tests and diagrams, but no clearly labeled System Link or Updated Diagram Link. The public repository URL exists, but its remote main revision differs from the local implemented revision. A valid repository address alone does not prove the mentor can obtain the current implementation.'),
        ('Resolve before calling the submission ready', 'Add the two labeled links to the log. Share a current, authorized code/build copy and the current expanded diagram; confirm mentor access using a signed-out browser or a separate account. Upload the log to your group shared location and test its link. Each member should submit that same shared log link. Uploading/publishing and mentor access were not performed by this reviewer.'),
        ('Files to recognize', 'Implementation Log: <b>MotorPH_Group13_Milestone2_Implementation_Log.pdf</b> and its editable DOCX. Current diagram: <b>MotorPH_Expanded_Control_Diagram</b> in PDF/PNG/HTML. Source repository address: <link href="https://github.com/rapidotech/AOOP" color="#16704c">github.com/rapidotech/AOOP</link>. These are desktop source files, not a hosted payroll website.'),
    ]),
    ('Control 1 Database credential protection', [
        ('Explain it simply', 'The application no longer depends on an embedded reusable database password. A protected local profile provides a separate application account, and that account is restricted to the database operations needed by MotorPH. This reduces exposure if configuration or application access is misused.'),
        ('Where it was applied', '<b>Code surface:</b> DatabaseConfig and connection setup validate configuration. <b>Configuration surface:</b> scripts/start-milestone2.ps1 loads the Windows-protected profile and checks the target. <b>Database surface:</b> a separate MySQL application account has restricted grants. <b>Transport:</b> TLS is required with server identity verification.'),
        ('Tools and environment', 'Java 17; Windows DPAPI CurrentUser and file permissions; MySQL 8.0.42; Connector/J 9.7.0; TLS VERIFY_IDENTITY; repository secret checks. DPAPI protects the stored credential for the prepared Windows user. TLS protects the database connection and checks the server identity. Database grants limit what that account can do.'),
        ('Valid scenario', '<b>Action:</b> Launch with the prepared protected profile and use allowed employee, salary, attendance or payroll operations. <b>Expected:</b> connect to the owned clone and permit required work. <b>Observed:</b> the profile identity/TLS probe, required reads and disposable employee/salary operations passed.'),
        ('Invalid scenarios', '<b>Action:</b> use missing/corrupt configuration, a changed target URL or injected JVM options. <b>Expected/observed:</b> startup rejects the unsafe input. <b>Action:</b> try schema changes, grant delegation, role-column edits, system-account access or a plaintext connection. <b>Expected/observed:</b> restricted privileges or required TLS deny the operation.'),
        ('Evidence to locate', 'evidence/C1-protected-config.json; C1-profile-and-rotation.json; C1-C2-isolated-integration.json; review-correction-regressions.json; readiness-and-restore.json. The original C1/C2 integration receipt contains 30 checks across both controls. Read the actual outcome, not just a green status.'),
        ('What to say about its status', 'Verified in the dedicated local clone for the observed scenarios. The original database on port 3306 was preserved; changes and mutation tests used aoop_m2 on port 3307. Account rotation in the clone does not mean the original shared root password or Git history was cleaned everywhere.'),
        ('Important limitation', 'The authorized application can decrypt its credential while running. DPAPI does not remove the need for a database password. A modified desktop client with the shared application credential can still issue SQL allowed by that account. Restricted grants reduce the impact but do not create complete per-person database isolation.'),
    ]),
    ('Control 2 Password storage and required reset', [
        ('Explain it simply', 'Passwords are checked against salted BCrypt hashes instead of reusable weak/default values. Enrolled accounts receive unique temporary passwords and must choose a new password before getting a normal application session.'),
        ('Where it was applied', '<b>Code surface:</b> PasswordService creates/verifies hashes; AuthenticationService checks credentials and reset state. <b>UI surface:</b> frm_Login and PasswordResetDialog handle sign-in and the required change. <b>Database surface:</b> the users table stores the supported hash and reset state; the reset commits transactionally.'),
        ('Tools and password rules', 'BCrypt library 0.10.2 with cost 12. New passwords require at least 12 Unicode characters and no more than 72 UTF-8 bytes. Known defaults, repeated passwords and control characters are rejected. Input is rejected rather than silently truncated. The current supported hash policy checks for cost 12.'),
        ('Why a salt and cost matter', 'A salt is random input that makes identical passwords produce different stored hashes. Cost controls the work needed for hashing, making repeated guesses more expensive. Hashing verifies a password without recovering the original; it is different from reversible encryption. Neither salts nor cost make weak passwords unguessable.'),
        ('Valid scenario', '<b>Action:</b> sign in using an authorized temporary credential, complete the password change, then sign in again with the replacement. <b>Expected:</b> temporary proof allows reset only; successful commit clears the reset requirement and permits normal access. <b>Observed:</b> the reset committed a valid BCrypt hash; the replacement worked and the old temporary value failed.'),
        ('Invalid scenarios', '<b>Wrong password:</b> authentication fails. <b>Cancel or failed reset:</b> no normal session is released. <b>Short, known-default, repeated or oversized replacement:</b> validation rejects it. <b>Retry after an incomplete operation:</b> the reset requirement remains until the password change successfully commits.'),
        ('Evidence to locate', 'evidence/C1-C2-isolated-integration.json; C2-controlled-enrollment.json; C2-escrow-atomic-maintenance.json; review-correction-regressions.json; ui/ui-proof.json. Controlled enrollment initially processed 34 clone accounts. Temporary-password recovery material remains private and is excluded from the public portfolio.'),
        ('What to say about its status', 'Verified for the documented clone enrollment, authentication, policy and reset scenarios. Mandatory reset is not required on every login after a completed change. Another Windows user or computer needs its own protected profile; the local DPAPI files are not portable group credentials.'),
        ('A distinction the mentor may check', 'C2 protects password storage and the reset gate. The later C3 throttle controls repeated guesses. Having BCrypt alone does not establish account lockout, access authorization, or a complete audit trail.'),
    ]),
    ('Supporting controls and evidence', [
        ('C3 Slowing repeated login failures', 'Persistent account/source counters enforce increasing retry delays of 1, 2, 4 and 8 seconds. The fifth failure imposes a 15-minute lock. Authentication events are stored and surfaced to HR. The backend checks the restriction; disabling a button is not the only enforcement. No email alert delivery is claimed.'),
        ('C4 Checking who may access records', 'Employee actions are limited to the authenticated employee scope; HR actions use the existing HR role. Form constructors and data-access operations enforce access checks. Hiding a menu item alone would be insufficient. These controls apply to normal supported-client entry points and retain the shared-JDBC limitation described under C1.'),
        ('C5 Recording sensitive changes', 'Sensitive data changes and their audit event commit in the same transaction. If audit insertion fails, the data change rolls back. A serialized hash chain helps detect changes to the recorded sequence. Attendance requests and payroll workflow ledgers are database-backed. HR has a read-only audit viewer.'),
        ('What the audit chain cannot prove', 'A database administrator can rewrite local data and checkpoints. A local hash chain is not proof of independent custody. Local audit export/checkpoint tooling was exercised; an independently controlled off-host archive destination has not been established. Do not describe the audit trail as completely tamper-proof.'),
        ('The safe environment and rollback', 'A labeled source/configuration/database backup and baseline were retained before implementation. Mutation tests used disposable records in aoop_m2 on 127.0.0.1:3307. The original aoop instance remained on 3306. Recovery records cover restoring the owned test copy and baseline. Never distribute private-backup, database passwords or temporary-password escrow.'),
        ('How to interpret the test counts', 'The later startup-theme correction passed 353 offline regression assertions, 163 native UI checks and 38 real UI-to-clone checks. The earlier complete release gate separately recorded 76 backend runtime and 15 SQL/audit checks. These are different suites and dates/scopes; they are not a single count of unique features. Removing the company footer afterward was build-checked, not another full-suite run.'),
        ('An issue worth understanding', 'The actual launcher initially used Metal while QA forced Nimbus, making Sign in invisible. The shared theme installer now selects Nimbus; the new test starts from Metal and checks the production installer and button paint. Other QA fixes preserved employee profiles during compensation saves, request keys during edits and HR payroll action states.'),
        ('What remains outside the proof', 'Physical keyboard/mouse/focus testing, physical printing, performance benchmarks, remote CI and full production deployment are not established by these local checks. A screenshot proves a visible state; it does not alone prove database authorization, transaction behavior or every possible feature/input combination.'),
    ]),
    ('Practice questions and personal preparation', [
        ('Why were these two controls chosen', 'C1 reduces exposed credentials and excessive database privilege. C2 addresses weak/default passwords and unsafe password storage. Connect each answer to the original SIP and its recorded risk, then point to the relevant implementation and test evidence.'),
        ('How do you know a control works', 'Describe one allowed action and one rejected action, the expected result, the observed result and the evidence filename. For C1, compare allowed application work with denied privileged SQL. For C2, compare successful replacement-password login with a wrong or superseded password and an incomplete reset.'),
        ('What changed from the original plan', 'The SIP selected C1/C2. Later work added C3/C4/C5 and supporting checks after assessing the broader audit proposals. Explain that scope expansion honestly. The UI redesign improves usability, but attractive screens are not evidence that a security control is effective.'),
        ('Where can you show the implementation', 'On the prepared computer, run run.bat. Use only your authorized account and protected credential retrieval procedure. Identify login/reset, the employee/HR access boundary, HR security activity and audit views. For exact backend and denied-operation evidence, open the recorded test receipts or the relevant test/tool source. Run mutation tests only against the owned clone.'),
        ('How do you explain a failed test', 'State what was expected, what actually happened, the cause found, the fix and the retest result. The startup-theme bug is a concrete example of why test setup must match the real launch path. Do not hide a failure or say every feature is guaranteed correct.'),
        ('What was your own contribution', 'Each member should identify only work they actually performed: implementation, configuration, running a test, checking a result, reviewing the diagram, or updating the log. Know the file or evidence you handled, one decision you understand and one problem you helped resolve. Do not claim another member\'s work or tool-assisted implementation as personal work you did not perform; follow your course disclosure rules.'),
        ('Check your understanding without memorizing a script', 'Can you explain hashing versus encryption, salt versus cost, authentication versus authorization, and expected versus observed results? Can you locate one C1 and one C2 receipt? Can you explain why the original database was preserved and name one remaining limitation? Revisit the relevant page for any answer you cannot explain.'),
        ('Before the mentor session', 'Confirm the shared log and diagram open for the mentor, the shared system copy includes the implementation, and the prepared app environment is available. Attend together when possible; arrange another schedule with the mentor early if needed. Each member must complete validation. No slides, formal presentation or separate demonstration plan is required by the supplied instructions.'),
    ]),
]


def main():
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle('ReviewerTitle', fontName='Helvetica-Bold', fontSize=21,
                              leading=25, textColor=colors.black, spaceAfter=15))
    styles.add(ParagraphStyle('ReviewerHeading', fontName='Helvetica-Bold', fontSize=11,
                              leading=14, spaceBefore=10, spaceAfter=4, keepWithNext=True))
    styles.add(ParagraphStyle('ReviewerBody', fontName='Helvetica', fontSize=10,
                              leading=14, spaceAfter=4, alignment=TA_LEFT))
    document = SimpleDocTemplate(str(OUTPUT), pagesize=A4, rightMargin=47, leftMargin=47,
                                 topMargin=43, bottomMargin=45,
                                 title='MotorPH Milestone 2 Validation Reviewer', author='Group 13')
    story = []
    for index, (title, sections) in enumerate(PAGES):
        if index:
            story.append(PageBreak())
        story.append(Paragraph(escape(title), styles['ReviewerTitle']))
        for heading, body in sections:
            story.append(Paragraph(escape(heading), styles['ReviewerHeading']))
            story.append(Paragraph(body, styles['ReviewerBody']))
    def footer(canvas, doc):
        canvas.saveState()
        canvas.setStrokeColor(colors.HexColor('#d6e0d7'))
        canvas.line(47, 34, A4[0]-47, 34)
        canvas.setFont('Helvetica', 8)
        canvas.setFillColor(colors.HexColor('#596a61'))
        canvas.drawString(47, 23, 'Group 13 | Optional study guide | Reviewed 17 October 2026')
        canvas.drawRightString(A4[0]-47, 23, str(doc.page))
        canvas.restoreState()
    document.build(story, onFirstPage=footer, onLaterPages=footer)
    public = Path.home() / 'Downloads/MotorPH_Group13_MS2_Validation_Reviewer.pdf'
    shutil.copyfile(OUTPUT, public)
    print(public)


if __name__ == '__main__':
    main()
