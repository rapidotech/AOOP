"""Scan current version-controlled files with Gitleaks, excluding local/private output."""
from pathlib import Path
import argparse
import json
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--report', type=Path)
    args = parser.parse_args()
    scanner = shutil.which('gitleaks')
    if not scanner:
        raise SystemExit('Gitleaks is required; install it before running the release gate.')
    names = subprocess.check_output(['git', 'ls-files', '-z', '--cached', '--others', '--exclude-standard'], cwd=ROOT).split(b'\0')
    with tempfile.TemporaryDirectory(prefix='motorph-source-scan-') as temp:
        scan = Path(temp)
        for name in set(names):
            if not name:
                continue
            relative = Path(name.decode('utf-8'))
            source = ROOT / relative
            if not source.is_file():
                continue
            destination = scan / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, destination)
        report = scan / 'redacted-gitleaks.json'
        result = subprocess.run([scanner, 'dir', str(scan), '--config', str(ROOT / '.gitleaks.toml'),
            '--redact', '--no-banner', '--no-color', '--report-format', 'json', '--report-path', str(report)], capture_output=True, text=True)
        if args.report:
            args.report.parent.mkdir(parents=True, exist_ok=True)
            args.report.write_text(report.read_text() if report.exists() else json.dumps({'error': 'Scanner did not produce a report'}))
        if result.returncode:
            raise SystemExit('Gitleaks failed or found secrets; inspect the redacted report. Raw diagnostics withheld.')
    print('Gitleaks current-source scan passed with custom low-entropy credential rule; Git history excluded.')


if __name__ == '__main__':
    main()
