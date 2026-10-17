"""Privileged local maintenance: verify the full chain and export a consistent snapshot."""
from pathlib import Path
import argparse
import datetime
import hashlib
import json
from milestone2_environment import RUNTIME, private_directory, sql, unprotect_credential


def verify(snapshot):
    def require(condition, message):
        if not condition:
            raise ValueError(message)
    previous = '0' * 64
    for number, event in enumerate(snapshot['events'], 1):
        require(event['id'] == number, 'Audit sequence gap')
        require(event['previous'] == previous, 'Audit predecessor mismatch')
        fields = [event[k] for k in ['actor', 'action', 'target', 'outcome', 'source', 'details']]
        encoded = '|'.join([previous, str(number), event['time']] + [s.encode('utf-8').hex().upper() for s in fields])
        actual = hashlib.sha256(encoded.encode('utf-8')).hexdigest()
        require(actual == event['hash'], 'Audit entry hash mismatch')
        previous = actual
    require(snapshot['head']['id'] == len(snapshot['events']) and snapshot['head']['hash'] == previous, 'Audit head mismatch')
    return len(snapshot['events'])


def snapshot():
    marker = json.loads((RUNTIME / 'environment.json').read_text())
    if marker['port'] != 3307 or marker['schema'] != 'aoop_m2':
        raise ValueError('Unexpected archive environment')
    credential = unprotect_credential(RUNTIME / 'app.credential.xml')
    statement = """SET TRANSACTION READ ONLY; START TRANSACTION WITH CONSISTENT SNAPSHOT;
SELECT JSON_OBJECT('kind','head','id',LastEventID,'hash',LastHash) FROM AuditChainHead WHERE ChainID=1;
SELECT JSON_OBJECT('kind','event','id',EventID,'time',DATE_FORMAT(OccurredAtUTC,'%Y-%m-%dT%H:%i:%s.%fZ'),
'actor',ActorID,'action',Action,'target',TargetID,'outcome',Outcome,'source',SourceMachine,'details',Details,
'previous',PreviousHash,'hash',EntryHash) FROM AuditLog ORDER BY EventID; COMMIT;"""
    rows = [json.loads(line) for line in sql(credential['user'], credential['password'], statement, database='aoop_m2').splitlines()]
    if not rows or rows[0]['kind'] != 'head':
        raise ValueError('Missing audit snapshot head')
    return {'scope': 'aoop_m2:3307', 'head': rows[0], 'events': rows[1:]}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--verify', type=Path)
    parser.add_argument('--destination', type=Path, help='Explicit authorized external archive directory; no default external transfer')
    parser.add_argument('--object-storage', action='store_true', help='Upload/retrieve/verify against the owned local Docker object store; not off-host proof')
    parser.add_argument('--cloudflare-r2', action='store_true', help='Upload/retrieve/verify the separately configured owned Cloudflare R2 bucket')
    args = parser.parse_args()
    if args.verify:
        print('Archive chain verified: ' + str(verify(json.loads(args.verify.read_text(encoding='utf-8')))) + ' events.')
        return
    data = snapshot()
    count = verify(data)
    directory = RUNTIME / 'audit-archives'
    private_directory(directory)
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%S%fZ')
    archive = directory / ('audit-' + stamp + '.json')
    raw = json.dumps(data, ensure_ascii=False, indent=2).encode('utf-8')
    # Exclusive creation preserves prior archive anchors.
    with archive.open('xb') as stream:
        stream.write(raw)
    digest = hashlib.sha256(raw).hexdigest()
    manifest = {'file': archive.name, 'bytes': len(raw), 'sha256': digest,
                'head': data['head'], 'events': count, 'custody': 'local; not independent off-host proof'}
    archive.with_suffix('.manifest.json').write_text(json.dumps(manifest, indent=2))
    failed = []
    # Attempt both requested destinations even if one is unavailable.
    for selected, module, label, success in [
        (args.cloudflare_r2, 'cloudflare_r2', 'Cloudflare R2',
         'Cloudflare R2 off-host upload/retrieval, full chain and bucket lock verified; administrator-removable retention, not COMPLIANCE Object Lock.'),
        (args.object_storage, 'object_storage', 'Local Docker S3',
         'Local Docker S3 upload/retrieval and active retention verified. Same-PC custody; independent off-host storage remains unproven.')]:
        if selected:
            try:
                from importlib import import_module
                import_module(module).upload_archive(archive, manifest)
                print(success)
            except Exception:
                failed.append(label)
    if args.destination:
        destination = args.destination.resolve()
        if not destination.is_dir():
            raise ValueError('External destination must already exist')
        target = destination / archive.name
        with target.open('xb') as stream:
            stream.write(raw)
        if hashlib.sha256(target.read_bytes()).hexdigest() != digest:
            raise ValueError('External archive checksum mismatch')
        target.with_suffix('.manifest.json').write_text(json.dumps(manifest, indent=2))
    if failed:
        raise RuntimeError('Archive destination verification failed: ' + ', '.join(failed) + '. Verified local export preserved; credential-bearing diagnostics withheld.') from None
    print('Verified audit snapshot exported: ' + str(count) + ' events. See destination-specific receipts for custody and recovery scope.')


if __name__ == '__main__':
    main()
