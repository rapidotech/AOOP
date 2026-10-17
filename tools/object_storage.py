"""Owned local S3 archive client. Root setup credentials never reach normal export."""
from pathlib import Path
import datetime
import hashlib
import json
import urllib.parse
import urllib.request
import boto3
from botocore.auth import SigV4Auth
from botocore.awsrequest import AWSRequest
from botocore.config import Config
from botocore.credentials import Credentials
from milestone2_environment import RUNTIME, unprotect_credential

STORE = RUNTIME / 'object-storage'
PROFILE = STORE / 'profile.json'
ENDPOINT = 'http://127.0.0.1:19000'
BUCKET = 'motorph-audit-archives'
IMAGE = 'rustfs/rustfs@sha256:7e948dad4748bda48f8985fda7195f8cf2e4259427011f6120acc2c9acf51587'


def profile():
    data = json.loads(PROFILE.read_text(encoding='utf-8'))
    if (data.get('kind') != 'MotorPHOwnedLocalObjectStorage' or data.get('endpoint') != ENDPOINT
            or data.get('bucket') != BUCKET or data.get('image') != IMAGE
            or data.get('independent_offhost') is not False):
        raise ValueError('Unexpected object-storage profile identity')
    return data


def client(kind='writer'):
    profile()
    if kind not in ('writer', 'root'):
        raise ValueError('Unsupported storage identity')
    credential = unprotect_credential(STORE / (kind + '.credential.xml'))
    return boto3.client('s3', endpoint_url=ENDPOINT, aws_access_key_id=credential['user'],
                        aws_secret_access_key=credential['password'], region_name='us-east-1',
                        config=Config(signature_version='s3v4', s3={'addressing_style':'path'},
                                      retries={'max_attempts':2}, connect_timeout=5, read_timeout=30))


def admin(method, route, query=None, body=None):
    """RustFS native JSON admin API, signed in memory; no secret in argv/logs."""
    credential = unprotect_credential(STORE / 'root.credential.xml')
    url = ENDPOINT + '/rustfs/admin/v3/' + route
    if query:
        url += '?' + urllib.parse.urlencode(query)
    raw = json.dumps(body).encode('utf-8') if body is not None else b''
    request = AWSRequest(method=method, url=url, data=raw,
                         headers={'Content-Type':'application/json','x-amz-content-sha256':hashlib.sha256(raw).hexdigest()})
    SigV4Auth(Credentials(credential['user'], credential['password']), 's3', 'us-east-1').add_auth(request)
    try:
        with urllib.request.urlopen(urllib.request.Request(url, data=raw, headers=dict(request.headers), method=method), timeout=30) as response:
            return response.status
    except urllib.error.HTTPError as failure:
        message=failure.read(8192).decode('utf-8',errors='replace')
        for kind in ('root','writer'):
            private=unprotect_credential(STORE/(kind+'.credential.xml'))
            for value in private.values():
                message=message.replace(value,'[redacted]')
        raise RuntimeError(f'Storage admin operation {route} failed: HTTP {failure.code}; {message[:1000]}') from None


def upload_archive(archive, manifest):
    """Verify the retrieved exact version and retention before reporting success."""
    from archive_audit import verify
    settings = profile()
    s3 = client()
    raw = archive.read_bytes()
    digest = hashlib.sha256(raw).hexdigest()
    if digest != manifest['sha256']:
        raise ValueError('Local archive checksum mismatch')
    count = verify(json.loads(raw))
    lock = s3.get_object_lock_configuration(Bucket=BUCKET)
    configuration=lock.get('ObjectLockConfiguration',{})
    if (configuration.get('ObjectLockEnabled') != 'Enabled' or
            configuration.get('Rule',{}).get('DefaultRetention') != {'Mode':'COMPLIANCE','Days':settings['retention_days']}):
        raise ValueError('Storage bucket lacks the configured compliance retention')
    key = 'audit/' + archive.name
    result = s3.put_object(Bucket=BUCKET, Key=key, Body=raw, ContentType='application/json',
                           Metadata={'sha256':digest})
    version = result.get('VersionId')
    if not version or version == 'null':
        raise ValueError('Archive upload did not return a protected version')
    downloaded = s3.get_object(Bucket=BUCKET, Key=key, VersionId=version)['Body'].read()
    if downloaded != raw or verify(json.loads(downloaded)) != count:
        raise ValueError('Storage retrieval/chain verification failed')
    retention = s3.get_object_retention(Bucket=BUCKET, Key=key, VersionId=version)['Retention']
    minimum=datetime.datetime.now(datetime.timezone.utc)+datetime.timedelta(days=settings['retention_days'],minutes=-5)
    if retention.get('Mode') != 'COMPLIANCE' or retention['RetainUntilDate'] < minimum:
        raise ValueError('Uploaded archive lacks active compliance retention')
    receipt = {'kind':'LocalDockerS3Archive', 'endpoint':ENDPOINT, 'bucket':BUCKET, 'key':key,
               'version_id':version, 'sha256':digest, 'events':count,
               'retention_mode':retention['Mode'], 'retain_until_utc':retention['RetainUntilDate'].isoformat(),
               'retention_days':settings['retention_days'], 'retrieved_and_chain_verified':True,
               'independent_offhost':False, 'boundary':'Docker Desktop on the same Windows PC; administrator/disk loss remains a boundary',
               'recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat()}
    proof = s3.put_object(Bucket=BUCKET, Key=key+'.manifest.json', Body=json.dumps(receipt,indent=2).encode(), ContentType='application/json')
    if not proof.get('VersionId'):
        raise ValueError('Archive manifest upload has no version')
    downloaded_manifest=s3.get_object(Bucket=BUCKET,Key=key+'.manifest.json',VersionId=proof['VersionId'])['Body'].read()
    if json.loads(downloaded_manifest).get('sha256')!=digest:
        raise ValueError('Retrieved archive manifest checksum mismatch')
    manifest_retention=s3.get_object_retention(Bucket=BUCKET,Key=key+'.manifest.json',VersionId=proof['VersionId'])['Retention']
    if manifest_retention.get('Mode')!='COMPLIANCE' or manifest_retention['RetainUntilDate']<minimum:
        raise ValueError('Uploaded manifest lacks the configured compliance retention')
    receipt['manifest_version_id'] = proof['VersionId']
    archive.with_suffix('.s3-receipt.json').write_text(json.dumps(receipt,indent=2),encoding='utf-8')
    return receipt
