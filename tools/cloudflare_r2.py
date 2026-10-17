"""Private R2 archive transport; local Docker remains a separate recovery copy."""
import datetime
import hashlib
import json
import re
import os
import urllib.error
import urllib.request
import boto3
from botocore.config import Config
from milestone2_environment import RUNTIME, unprotect_credential

STORE = RUNTIME / 'cloudflare-r2'
PROFILE = STORE / 'profile.json'
BUCKET = 'motorph-audit-archives'
MAX_ARCHIVE_BYTES = 10 * 1024 * 1024
MAX_BUCKET_BYTES = 256 * 1024 * 1024
MAX_DAILY_REQUESTS = 200
MAX_MONTHLY_REQUESTS = 2000


def require_cost_consent():
    policy = json.loads((STORE/'cost-policy.json').read_text(encoding='utf-8'))
    if policy.get('kind') != 'UserAcceptedMeteredFreeTier' or policy.get('allow_metered_provider') is not True:
        raise ValueError('R2 metered free-tier use has not been accepted; transfer refused')


def consume_request_budget(**kwargs):
    """Count attempts before network I/O, including SDK retries; fail closed."""
    require_cost_consent()
    import msvcrt
    lock_path, counter_path = STORE/'request-budget.lock', STORE/'request-budget.json'
    with lock_path.open('a+b') as lock:
        if lock.tell() == 0:
            lock.write(b'0'); lock.flush()
        lock.seek(0)
        try:
            msvcrt.locking(lock.fileno(), msvcrt.LK_NBLCK, 1)
        except OSError:
            raise RuntimeError('R2 request budget is busy; no request sent') from None
        try:
            today = datetime.datetime.now(datetime.timezone.utc).date().isoformat()
            month = today[:7]
            counter = json.loads(counter_path.read_text()) if counter_path.exists() else {}
            monthly = counter.get('monthly',0) if counter.get('month') == month else 0
            daily = counter.get('daily',0) if counter.get('day') == today else 0
            if not isinstance(monthly,int) or not isinstance(daily,int) or min(monthly,daily)<0:
                raise ValueError('R2 request budget is invalid; no request sent')
            if daily >= MAX_DAILY_REQUESTS or monthly >= MAX_MONTHLY_REQUESTS:
                raise ValueError('Conservative R2 request budget exhausted; no request sent')
            temporary = counter_path.with_suffix('.pending.json')
            with temporary.open('w',encoding='utf-8') as output:
                json.dump({'month':month,'monthly':monthly+1,'day':today,'daily':daily+1},output)
                output.flush(); os.fsync(output.fileno())
            temporary.replace(counter_path)
        finally:
            lock.seek(0)
            msvcrt.locking(lock.fileno(),msvcrt.LK_UNLCK,1)


class CloudflareError(RuntimeError):
    def __init__(self, status, codes):
        self.status, self.codes = status, codes
        super().__init__(f'Cloudflare API failed: HTTP {status}; error codes {codes}. Credential-bearing diagnostics withheld.')


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def account_id(value):
    if not isinstance(value, str) or not re.fullmatch('[a-f0-9]{32}', value):
        raise ValueError('Unexpected Cloudflare account identity')
    return value


def profile():
    data = json.loads(PROFILE.read_text(encoding='utf-8'))
    account = account_id(data.get('account_id'))
    if (data.get('kind') != 'MotorPHOwnedCloudflareR2' or data.get('bucket') != BUCKET
            or data.get('endpoint') != f'https://{account}.r2.cloudflarestorage.com'
            or data.get('retention_days') != 7 or data.get('state') != 'configured'):
        raise ValueError('Unexpected R2 archive profile; transfer refused')
    return data


def api(route, *, identity='reader', method='GET', body=None):
    if identity not in ('reader', 'writer', 'bootstrap') or not route.startswith('/accounts'):
        raise ValueError('Unsupported Cloudflare operation')
    credential = unprotect_credential(STORE / (identity + '.credential.xml'))
    data = json.dumps(body).encode('utf-8') if body is not None else None
    request = urllib.request.Request('https://api.cloudflare.com/client/v4' + route,
        data=data, method=method, headers={'Authorization':'Bearer '+credential['password'],
                                         'Content-Type':'application/json'})
    consume_request_budget()
    try:
        with urllib.request.build_opener(NoRedirect).open(request, timeout=30) as response:
            result = json.load(response)
            if not result.get('success'):
                raise CloudflareError(response.status, [e.get('code') for e in result.get('errors', [])])
            return result.get('result')
    except urllib.error.HTTPError as error:
        try:
            result = json.loads(error.read(16384))
        except (ValueError, UnicodeError):
            result = {}
        # Private diagnostic only; never emit provider bodies to shared logs.
        (STORE/'last-api-error.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
        raise CloudflareError(error.code, [e.get('code') for e in result.get('errors', [])]) from None
    except urllib.error.URLError:
        raise RuntimeError('Cloudflare API unavailable; verified local archive retained') from None


def bucket_route(settings):
    return '/accounts/' + account_id(settings['account_id']) + '/r2/buckets/' + BUCKET


def client(identity='writer'):
    settings = profile()
    if identity not in ('writer', 'bootstrap'):
        raise ValueError('Unsupported R2 S3 identity')
    credential = unprotect_credential(STORE / (identity + '.credential.xml'))
    result = boto3.client('s3', endpoint_url=settings['endpoint'], region_name='auto',
        aws_access_key_id=credential['user'],
        aws_secret_access_key=hashlib.sha256(credential['password'].encode()).hexdigest(),
        config=Config(signature_version='s3v4', s3={'addressing_style':'path'},
                      retries={'max_attempts':2}, connect_timeout=5, read_timeout=30))
    result.meta.events.register('before-send.s3',consume_request_budget)
    return result


def require_lock(rules):
    if not isinstance(rules, dict) or not any(
        rule.get('enabled') is True and rule.get('prefix') == 'audit/'
        and rule.get('condition', {}).get('type') == 'Age'
        and rule['condition'].get('maxAgeSeconds', 0) >= 7 * 86400
        for rule in rules.get('rules', [])
    ):
        raise ValueError('R2 audit prefix lacks the required active seven-day bucket lock')


def require_capacity(s3, incoming_bytes):
    total, count = 0, 0
    for page in s3.get_paginator('list_objects_v2').paginate(Bucket=BUCKET):
        for item in page.get('Contents', []):
            total += item['Size']
            count += 1
            if total + incoming_bytes > MAX_BUCKET_BYTES or count >= 10000:
                raise ValueError('Owned R2 bucket exceeds conservative archive budget; transfer refused')
    if total + incoming_bytes > MAX_BUCKET_BYTES:
        raise ValueError('Owned R2 bucket exceeds conservative archive budget; transfer refused')


def upload_archive(archive, manifest):
    from archive_audit import verify
    # Reject damaged data before touching remote credentials or issuing requests.
    if archive.stat().st_size > MAX_ARCHIVE_BYTES:
        raise ValueError('Audit archive exceeds the configured 10 MiB upload limit')
    raw = archive.read_bytes()
    digest = hashlib.sha256(raw).hexdigest()
    if digest != manifest['sha256']:
        raise ValueError('Local archive checksum mismatch')
    count = verify(json.loads(raw))
    require_cost_consent()
    settings = profile()
    route = bucket_route(settings)
    require_lock(api(route + '/lock'))
    s3 = client()
    require_capacity(s3, len(raw) + 8192)
    key = 'audit/' + archive.name
    s3.put_object(Bucket=BUCKET, Key=key, Body=raw, ContentType='application/json',
                  IfNoneMatch='*', Metadata={'sha256':digest})
    returned = s3.get_object(Bucket=BUCKET, Key=key)['Body'].read()
    if returned != raw or verify(json.loads(returned)) != count:
        raise ValueError('R2 retrieval/checksum/chain verification failed')
    # A remotely stored receipt is also protected by the prefix lock.
    receipt = {'kind':'CloudflareR2Archive', 'bucket':BUCKET, 'key':key,
        'sha256':digest, 'events':count, 'retrieved_and_chain_verified':True,
        'retention_days':7, 'retention_type':'Administrator-removable R2 bucket lock',
        'offhost_storage':True, 'independent_administrator_custody_proven':False,
        'boundary':'Cloudflare administrators may remove bucket locks; not S3 COMPLIANCE Object Lock',
        'recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat()}
    manifest_bytes = json.dumps(receipt, indent=2).encode('utf-8')
    s3.put_object(Bucket=BUCKET, Key=key+'.manifest.json', Body=manifest_bytes,
                  ContentType='application/json', IfNoneMatch='*')
    returned_manifest = s3.get_object(Bucket=BUCKET, Key=key+'.manifest.json')['Body'].read()
    if returned_manifest != manifest_bytes:
        raise ValueError('R2 retrieved manifest differs from uploaded manifest')
    require_lock(api(route + '/lock'))
    archive.with_suffix('.r2-receipt.json').write_text(json.dumps(receipt, indent=2), encoding='utf-8')
    return receipt
