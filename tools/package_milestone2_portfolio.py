"""Package only the public Milestone 2 deliverables; never private recovery data."""
from pathlib import Path
import hashlib
import json
import zipfile

OUTPUT = Path.home() / 'Downloads/AOOP_Milestone2_Group13'
ARCHIVE = OUTPUT.parent / 'AOOP_Milestone2_Group13_Portfolio.zip'
ROOT = Path(__file__).resolve().parents[1]


def main():
    (OUTPUT / 'START_HERE.md').write_text('''# MotorPH Group 13 Milestone 2

Open MotorPH_Group13_Milestone2_Implementation_Log.pdf to read the complete
ten-page portfolio. Edit the matching DOCX to continue your implementation log.
It covers all five Midgame Moves: control mapping, readiness and rollback,
implementation, assurance tests, and the updated system diagram.

The control diagram is available as an interactive HTML file, PNG, SVG and PDF.
The evidence folder contains sanitized results and five password-reset UI images.
The supplied Milestone 1 SIP and audit workbook were preserved unchanged.

Both selected controls were implemented and verified in an isolated MySQL clone:
protected credentials/least privilege, and BCrypt12 with mandatory password reset.
The original database and shared root account remain unchanged. Deferred controls
and test limitations are identified in the log; this is local assurance evidence.

On the prepared computer, run the repository's run.bat to open MotorPH with the
protected profile. See SECURITY_RUNBOOK.md for private temporary-password retrieval,
testing and recovery commands. Existing cloned accounts require a password change.
The application source and verification tools are in the AOOP repository.

The private-backup folder, Windows-bound credentials and temporary-password escrow
are deliberately excluded from this archive. Keep the private recovery material
on the provisioning computer; it is not a group-sharing deliverable.
''', encoding='utf-8')
    (OUTPUT / 'SECURITY_RUNBOOK.md').write_bytes((ROOT / 'security/README.md').read_bytes())
    primary = [OUTPUT / name for name in [
        'START_HERE.md', 'SECURITY_RUNBOOK.md',
        'MotorPH_Group13_Milestone2_Implementation_Log.docx',
        'MotorPH_Group13_Milestone2_Implementation_Log.pdf',
        'MotorPH_Updated_Control_Diagram.html',
        'MotorPH_Updated_Control_Diagram.png',
        'MotorPH_Updated_Control_Diagram.svg',
        'MotorPH_Updated_Control_Diagram.pdf',
        'MotorPH_Updated_Control_Diagram.visual-check.json',
    ]]
    evidence = [p for p in (OUTPUT / 'evidence').rglob('*') if p.is_file()
                and (p.suffix in {'.json', '.txt'} or
                     (p.parent.name == 'ui' and p.suffix == '.png'))
                and 'render' not in p.parts]
    files = sorted(set(primary + evidence))
    assert all(p.is_file() for p in files), 'Missing deliverable'
    manifest = [{'file': str(p.relative_to(OUTPUT)).replace('\\', '/'),
                 'bytes': p.stat().st_size,
                 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in files]
    manifest_path = OUTPUT / 'portfolio-manifest.json'
    manifest_path.write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    with zipfile.ZipFile(ARCHIVE, 'w', zipfile.ZIP_DEFLATED) as archive:
        for p in files + [manifest_path]:
            assert 'private-backup' not in p.parts
            archive.write(p, p.relative_to(OUTPUT))
    with zipfile.ZipFile(ARCHIVE) as archive:
        assert archive.testzip() is None
        assert not any('private-backup' in name for name in archive.namelist())
        assert len(archive.namelist()) == len(files) + 1
    print(f'Public portfolio verified: {len(files) + 1} files; {ARCHIVE.stat().st_size} bytes.')


if __name__ == '__main__':
    main()
