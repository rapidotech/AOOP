"""Package only the public Milestone 2 deliverables; never private recovery data."""
from pathlib import Path
import hashlib
import json
import zipfile
import shutil

OUTPUT = Path.home() / 'Downloads/AOOP_Milestone2_Group13'
ARCHIVE = OUTPUT.parent / 'AOOP_Milestone2_Group13_Portfolio.zip'
ROOT = Path(__file__).resolve().parents[1]


def main():
    reviewer=OUTPUT.parent/'MotorPH_Group13_MS2_Validation_Reviewer.pdf'
    if reviewer.is_file():
        shutil.copyfile(reviewer,OUTPUT/reviewer.name)
    current_log=OUTPUT/'outputs/implementation-log/MotorPH_Group13_Implementation_Log.xlsx'
    (OUTPUT / 'START_HERE.md').write_text('''# MotorPH Group 13 Milestone 2

Open MotorPH_Group13_Milestone2_Implementation_Log.pdf to read the expanded
portfolio. Edit the matching DOCX to continue your implementation log.
It covers all five Midgame Moves: control mapping, readiness and rollback,
implementation, assurance tests, and the updated system diagram.

MotorPH_Expanded_Control_Diagram is the current diagram, in interactive HTML,
PNG, SVG and PDF. The earlier C1/C2 diagram is retained as historical evidence.
The evidence folder contains sanitized results and UI captures with provenance.
The supplied Milestone 1 SIP and audit workbook were preserved unchanged.

UI overhaul addendum: evidence/ui-overhaul-qa-report.txt and
evidence/ui-overhaul-visual-verdict.txt document the rebuilt Java desktop UI.
The 42 final synthetic captures and current results are under evidence/ui/overhaul.
The implementation log retains the earlier security implementation chronology;
this addendum records the later UI work and additional workflow corrections.

All worthwhile feasible core controls were implemented in an isolated MySQL clone:
protected credentials/least privilege, BCrypt12/mandatory reset, persistent lockout,
scoped authorization, transactional audit logging and DB workflow ledgers.
CONTROL_ASSESSMENT.md records every proposed option and its decision.
The revised audit workbook is under outputs/all-controls; original source remains intact.
The original database and shared root account remain unchanged. Deferred controls
and test limitations are identified in the log. R2 now also has actual off-host
recovery evidence; it does not establish independently administered custody.

On the prepared computer, run the repository's run.bat to open MotorPH with the
protected profile. See SECURITY_RUNBOOK.md for private temporary-password retrieval,
testing and recovery commands. Existing cloned accounts require a password change.
The application source and verification tools are in the AOOP repository.

The private-backup folder, Windows-bound credentials and temporary-password escrow
are deliberately excluded from this archive. Keep the private recovery material
on the provisioning computer; it is not a group-sharing deliverable.
''', encoding='utf-8')
    (OUTPUT / 'SECURITY_RUNBOOK.md').write_bytes((ROOT / 'security/README.md').read_bytes())
    (OUTPUT / 'LOCAL_OBJECT_STORAGE.md').write_bytes((ROOT / 'security/object-storage/README.md').read_bytes())
    (OUTPUT / 'CLOUDFLARE_R2.md').write_bytes((ROOT / 'security/object-storage/CLOUDFLARE_R2.md').read_bytes())
    (OUTPUT / 'COMPLETION_AUDIT.md').write_bytes((ROOT / 'docs/COMPLETION_AUDIT.md').read_bytes())
    if current_log.is_file():
        start=OUTPUT/'START_HERE.md'
        start.write_text('Current school-template Implementation Log: outputs/implementation-log/MotorPH_Group13_Implementation_Log.xlsx. Use this workbook as the current log. The DOCX/PDF retain earlier chronology and later Docker/R2 addenda.\n\nCloudflare R2 now provides actual off-host copies; evidence/cloudflare-r2-*.json records live recovery and scheduled uploads. Local Docker proof remains under evidence/local-object-storage-*.json. Independently administered custody and a provider hard $0 spending cap are not claimed. See CLOUDFLARE_R2.md for safeguards and limits.\n\n'+start.read_text(encoding='utf-8'),encoding='utf-8')
    (OUTPUT / 'CONTROL_ASSESSMENT.md').write_bytes((ROOT / 'security/EXPANDED_CONTROL_PLAN.md').read_bytes())
    primary = [OUTPUT / name for name in [
        'START_HERE.md', 'SECURITY_RUNBOOK.md', 'LOCAL_OBJECT_STORAGE.md', 'CLOUDFLARE_R2.md', 'CONTROL_ASSESSMENT.md', 'COMPLETION_AUDIT.md',
        'MotorPH_Group13_Milestone2_Implementation_Log.docx',
        'MotorPH_Group13_Milestone2_Implementation_Log.pdf',
        'MotorPH_Updated_Control_Diagram.html',
        'MotorPH_Updated_Control_Diagram.png',
        'MotorPH_Updated_Control_Diagram.svg',
        'MotorPH_Updated_Control_Diagram.pdf',
        'MotorPH_Updated_Control_Diagram.visual-check.json',
        'MotorPH_Expanded_Control_Diagram.html',
        'MotorPH_Expanded_Control_Diagram.png',
        'MotorPH_Expanded_Control_Diagram.svg',
        'MotorPH_Expanded_Control_Diagram.pdf',
        'MotorPH_Expanded_Control_Diagram.visual-check.json',
        'outputs/all-controls/MotorPH_Group13_Updated_Control_Audit.xlsx',
    ]]
    evidence = [p for p in (OUTPUT / 'evidence').rglob('*') if p.is_file()
                and (p.suffix in {'.json', '.txt'} or
                     ('ui' in p.relative_to(OUTPUT / 'evidence').parts and p.suffix == '.png'))
                and 'render' not in p.parts]
    if current_log.is_file():
        primary.append(current_log)
    if reviewer.is_file():
        primary.append(OUTPUT/reviewer.name)
    files = sorted(set(primary + evidence))
    if not all(p.is_file() for p in files):
        raise ValueError('Missing deliverable')
    manifest = [{'file': str(p.relative_to(OUTPUT)).replace('\\', '/'),
                 'bytes': p.stat().st_size,
                 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in files]
    manifest_path = OUTPUT / 'portfolio-manifest.json'
    manifest_path.write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    with zipfile.ZipFile(ARCHIVE, 'w', zipfile.ZIP_DEFLATED) as archive:
        for p in files + [manifest_path]:
            if ('private-backup' in p.parts or p.name.endswith(('.r2-receipt.json','.s3-receipt.json','.credential.xml'))
                    or p.name == 'compose.env' or p.name.startswith('audit-20')):
                raise ValueError('Private recovery material cannot enter the public package')
            archive.write(p, p.relative_to(OUTPUT))
    with zipfile.ZipFile(ARCHIVE) as archive:
        if archive.testzip() is not None or any('private-backup' in name for name in archive.namelist()) or len(archive.namelist()) != len(files) + 1:
            raise ValueError('Public archive integrity/exclusion check failed')
    print(f'Public portfolio verified: {len(files) + 1} files; {ARCHIVE.stat().st_size} bytes.')


if __name__ == '__main__':
    main()
