"""Prove the custom rule detects a low-entropy credential without exposing it."""
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    with tempfile.TemporaryDirectory(prefix='motorph-rule-test-') as temp:
        fixture = Path(temp) / 'DatabaseConfig.java'
        marker = 'test-only-short-value'
        fixture.write_text('class DatabaseConfig { static final String PASSWORD = "' + marker + '"; }')
        command = [shutil.which('gitleaks') or 'gitleaks', 'dir', temp, '--config', str(ROOT / '.gitleaks.toml'),
                   '--redact', '--no-banner', '--no-color', '--report-format', 'json', '--report-path', str(Path(temp) / 'report.json')]
        result = subprocess.run(command, capture_output=True, text=True)
        assert result.returncode == 1, 'Custom rule missed a credential fixture'
        report = (Path(temp) / 'report.json').read_text()
        assert 'motorph-embedded-db-credential' in report
        assert marker not in report, 'Report exposed the credential fixture'
        (Path(temp) / 'report.json').unlink()
        fixture.write_text('class DatabaseConfig { String password = System.getenv("AOOP_DB_PASSWORD"); }')
        assert subprocess.run(command, capture_output=True).returncode == 0, 'External configuration rejected'
    print('Repository control regressions passed: positive detection, output redaction and safe configuration.')


if __name__ == '__main__':
    if not __debug__:
        raise RuntimeError('Run the regression driver without optimization')
    main()
