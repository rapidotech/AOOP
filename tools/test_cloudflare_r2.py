"""Live assurance of only the owned R2 bucket and disposable validation objects."""
import argparse
import datetime
import hashlib
import json
import secrets
import subprocess
import sys
import tempfile
import boto3
from botocore import UNSIGNED
from botocore.config import Config
from botocore.exceptions import ClientError
from archive_audit import verify
from milestone2_environment import ROOT, RUNTIME
from cloudflare_r2 import (STORE, PROFILE, BUCKET, profile, client, api, bucket_route,
    require_lock, upload_archive, consume_request_budget, MAX_BUCKET_BYTES,
    MAX_DAILY_REQUESTS, MAX_MONTHLY_REQUESTS, CloudflareError)


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--enable-schedule',action='store_true')
    args=parser.parse_args()
    passed=[]; denials=[]
    def check(condition,label):
        if not condition:raise RuntimeError(label)
        passed.append(label)
    def denied(action,label,anonymous=False):
        try:action()
        except ClientError as error:
            code=error.response['Error']['Code']
            status=error.response['ResponseMetadata']['HTTPStatusCode']
            missing_auth=anonymous and status==400 and code=='InvalidArgument' and error.response['Error'].get('Message')=='Authorization'
            check(missing_auth or (status in (400,403,409) and (code=='AccessDenied' or 'lock' in code.lower())),label)
            denials.append({'scenario':label,'code':code,'http_status':status})
            return
        except CloudflareError as error:
            check(error.status in (401,403),label)
            denials.append({'scenario':label,'codes':error.codes,'http_status':error.status})
            return
        raise RuntimeError(label+' unexpectedly succeeded')
    settings=profile(); route=bucket_route(settings)
    owned=json.loads((STORE/'bucket-owned.json').read_text())
    check(owned=={'account_id':settings['account_id'],'bucket':BUCKET},'Named bucket has the matching local ownership marker')
    bucket=api(route)
    check(bucket.get('storage_class',bucket.get('storageClass'))=='Standard','Owned bucket uses Standard storage')
    managed=api(route+'/domains/managed')
    check(managed.get('enabled') is False,'Public managed r2.dev domain is disabled')
    custom=api(route+'/domains/custom')
    check(not any(item.get('enabled',True) for item in custom.get('domains',[])),'No public custom domain is enabled')
    locks=api(route+'/lock'); require_lock(locks)
    check(True,'Active audit-prefix seven-day bucket lock confirmed via read-only identity')
    lifecycle=api(route+'/lifecycle')
    check(any(rule.get('enabled') is True and rule.get('conditions',{}).get('prefix')=='audit/' and rule.get('deleteObjectsTransition',{}).get('condition')=={'type':'Age','maxAge':8*86400} for rule in lifecycle.get('rules',[])),'Eight-day expiry rule configured; actual future expiration is not yet observed')
    subprocess.run([sys.executable,str(ROOT/'tools/archive_audit.py'),'--cloudflare-r2'],check=True)
    path=max((RUNTIME/'audit-archives').glob('*.r2-receipt.json'),key=lambda p:p.stat().st_mtime_ns)
    receipt=json.loads(path.read_text(encoding='utf-8'))
    s3=client(); root=client('bootstrap')
    returned=s3.get_object(Bucket=BUCKET,Key=receipt['key'])['Body'].read()
    check(hashlib.sha256(returned).hexdigest()==receipt['sha256'] and verify(json.loads(returned))==receipt['events'],'Actual off-host audit download matches checksum and full chain')
    manifest=s3.get_object(Bucket=BUCKET,Key=receipt['key']+'.manifest.json')['Body'].read()
    check(json.loads(manifest)==receipt,'Downloaded protected manifest matches the local verification receipt')
    anonymous=boto3.client('s3',endpoint_url=settings['endpoint'],region_name='auto',config=Config(signature_version=UNSIGNED,retries={'max_attempts':0}))
    anonymous.meta.events.register('before-send.s3',consume_request_budget)
    denied(lambda:anonymous.get_object(Bucket=BUCKET,Key=receipt['key']),'Anonymous S3 read of an existing audit object is rejected',anonymous=True)
    # Denial mutations target a new disposable fixture, not the actual recovered backup.
    key='audit/validation/'+secrets.token_hex(12)+'.txt'
    s3.put_object(Bucket=BUCKET,Key=key,Body=b'Owned retention-denial fixture')
    denied(lambda:s3.delete_object(Bucket=BUCKET,Key=key),'Backup writer cannot delete an object under the active lock')
    denied(lambda:s3.put_object(Bucket=BUCKET,Key=key,Body=b'Changed fixture'),'Backup writer cannot overwrite an object under the active lock')
    denied(lambda:root.delete_object(Bucket=BUCKET,Key=key),'Bootstrap S3 identity cannot delete a currently locked object')
    denied(lambda:api(route+'/lock',method='PUT',body=locks),'Read-only configuration identity cannot change bucket locks')
    denied(lambda:api(route+'/lock',identity='writer',method='PUT',body=locks),'Object-only writer cannot change configuration through the REST API')
    other='motorph-audit-scope-'+secrets.token_hex(6)
    account_route='/accounts/'+settings['account_id']+'/r2/buckets'
    api(account_route,identity='bootstrap',method='POST',body={'name':other,'storageClass':'Standard'})
    fixture_created=False
    try:
        root.put_object(Bucket=other,Key='owned.txt',Body=b'Owned bucket-scope fixture')
        fixture_created=True
        denied(lambda:s3.get_object(Bucket=other,Key='owned.txt'),'Scoped writer cannot read an existing object in a separate owned test bucket')
        denied(lambda:s3.put_object(Bucket=other,Key='denied.txt',Body=b'Owned denied fixture'),'Scoped writer cannot write into the separate owned test bucket')
    finally:
        if fixture_created:root.delete_object(Bucket=other,Key='owned.txt')
        # Only the newly created test bucket is removed; real archive bucket is retained.
        api(account_route+'/'+other,identity='bootstrap',method='DELETE')
    before=(STORE/'request-budget.json').read_bytes()
    with tempfile.TemporaryDirectory(prefix='corrupt-',dir=STORE) as temporary:
        damaged=__import__('pathlib').Path(temporary)/'damaged.json'
        damaged.write_text(json.dumps({'head':{'id':1,'hash':'0'*64},'events':[]}))
        try:upload_archive(damaged,{'sha256':hashlib.sha256(damaged.read_bytes()).hexdigest()})
        except ValueError:check((STORE/'request-budget.json').read_bytes()==before,'Damaged chain rejected before any remote request')
        else:raise RuntimeError('Damaged chain upload unexpectedly succeeded')
    recovered=s3.get_object(Bucket=BUCKET,Key=receipt['key'])['Body'].read()
    check(recovered==returned and verify(json.loads(recovered))==receipt['events'],'Actual audit backup remains recoverable after denial scenarios')
    objects=[item for page in s3.get_paginator('list_objects_v2').paginate(Bucket=BUCKET) for item in page.get('Contents',[])]
    total=sum(item['Size'] for item in objects)
    check(total<MAX_BUCKET_BYTES,'Owned bucket storage is below the conservative 256 MiB ceiling')
    budget=json.loads((STORE/'request-budget.json').read_text())
    check(budget['daily']<MAX_DAILY_REQUESTS and budget['monthly']<MAX_MONTHLY_REQUESTS,'Recorded integration request counts remain below conservative daily/monthly ceilings')
    report={'recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
        'scope':'Live Cloudflare R2 owned private bucket; actual aoop_m2 audit snapshot plus disposable denial fixtures',
        'assertions':len(passed),'outcomes':passed,'denials':denials,'retained_events':receipt['events'],
        'stored_bytes':total,'objects':len(objects),'request_budget':budget,'offhost_storage_verified':True,
        'independent_administrator_custody_proven':False,'future_lifecycle_expiration_observed':False,
        'cost':'Configured within the metered free allowance; no provider hard-zero-cost guarantee',
        'boundary':'Cloudflare administrators can remove locks; bootstrap token remains on the provisioning PC; not S3 COMPLIANCE or independently administered custody'}
    (STORE/'assurance.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    if args.enable_schedule:
        (STORE/'enabled.json').write_text(json.dumps({'kind':'MotorPHVerifiedR2Schedule',
            'profile_sha256':hashlib.sha256(PROFILE.read_bytes()).hexdigest(),
            'assurance_sha256':hashlib.sha256((STORE/'assurance.json').read_bytes()).hexdigest()},indent=2),encoding='utf-8')
    print('Live R2 assurance passed: '+str(len(passed))+' checks; real off-host recovery verified. Administrator-removable bucket lock and metered free-tier boundaries retained.')


if __name__=='__main__':main()
