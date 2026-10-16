"""Verify pinned JARs and query live OSV advisories; unavailable feeds fail closed."""
from pathlib import Path
import argparse
import datetime
import hashlib
import json
import urllib.request

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--report', type=Path)
    parser.add_argument('--root', type=Path, default=ROOT)
    args = parser.parse_args()
    pins = json.loads((args.root / 'security/dependencies/inventory.json').read_text())
    actual = {p.name for p in (args.root / 'AOOP - MotorPH from OOP/lib').glob('*.jar')}
    if actual != {p['file'] for p in pins}:
        raise ValueError('Unexpected or missing bundled dependency')
    for pin in pins:
        jar = args.root / 'AOOP - MotorPH from OOP/lib' / pin['file']
        if hashlib.sha256(jar.read_bytes()).hexdigest() != pin['sha256']:
            raise ValueError('Dependency checksum mismatch: ' + pin['file'])
    queries = [{'package': {'ecosystem': 'Maven', 'name': p['package']}, 'version': p['version']} for p in pins]
    request = urllib.request.Request('https://api.osv.dev/v1/querybatch',
        data=json.dumps({'queries': queries}).encode(), headers={'Content-Type': 'application/json'})
    response = json.load(urllib.request.urlopen(request, timeout=40))
    results = response.get('results')
    if not isinstance(results, list) or len(results) != len(queries):
        raise ValueError('Incomplete advisory response')
    # A next-page token means this answer must not be treated as complete.
    if any(r.get('next_page_token') for r in results):
        raise ValueError('Paginated advisory results require review')
    report = {'checked_at_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
        'source': 'https://api.osv.dev/v1/querybatch', 'hashes_verified': len(pins),
        'queries': queries, 'results': results,
        'scope': 'Live OSV package/version advisory coverage; not a complete exploit or vulnerability assessment.'}
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2))
    count = sum(len(r.get('vulns', [])) for r in results)
    print(f'Dependency checksums verified: {len(pins)}; OSV advisories returned: {count}.')
    if count:
        raise SystemExit('Dependency advisory found; review and remediate before release.')


if __name__ == '__main__':
    main()
