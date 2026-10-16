"""Regression for integrity checks remaining active under optimized Python."""
from pathlib import Path
import hashlib
import json
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    with tempfile.TemporaryDirectory(prefix='motorph-integrity-') as temp:
        fixture = Path(temp)
        damaged = fixture / 'damaged-audit.json'
        damaged.write_text(json.dumps({'head': {'id': 1, 'hash': '0' * 64}, 'events': []}))
        result = subprocess.run([sys.executable, '-O', str(ROOT / 'tools/archive_audit.py'), '--verify', str(damaged)], capture_output=True)
        assert result.returncode and b'Audit head mismatch' in result.stderr
        jars = fixture / 'AOOP - MotorPH from OOP/lib'; jars.mkdir(parents=True)
        (jars / 'fixture.jar').write_bytes(b'modified dependency')
        pins = fixture / 'security/dependencies'; pins.mkdir(parents=True)
        (pins / 'inventory.json').write_text(json.dumps([{'file': 'fixture.jar', 'package': 'fixture:fixture', 'version': '1', 'sha256': hashlib.sha256(b'original dependency').hexdigest()}]))
        result = subprocess.run([sys.executable, '-O', str(ROOT / 'scripts/check-dependencies.py'), '--root', str(fixture)], capture_output=True)
        assert result.returncode and b'Dependency checksum mismatch' in result.stderr
    print('Optimized-mode integrity regressions passed: malformed archive and changed dependency rejected.')


if __name__ == '__main__':
    if not __debug__:
        raise RuntimeError('Run the regression driver without optimization; child gates use -O.')
    main()
