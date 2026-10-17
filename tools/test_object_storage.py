"""Real local Docker S3 assurance using the owned bucket and disposable objects."""
from pathlib import Path
import datetime
import hashlib
import json
import secrets
import subprocess
import sys
import tempfile
import time
import urllib.request
import boto3
from botocore import UNSIGNED
from botocore.config import Config
from botocore.exceptions import ClientError
from archive_audit import verify
from milestone2_environment import ROOT, RUNTIME
from object_storage import BUCKET, ENDPOINT, STORE, client, upload_archive


def main():
    passed=[];denials=[]
    def check(condition,label):
        if not condition:
            raise RuntimeError(label)
        passed.append(label)
    def denied(operation,label):
        try:
            operation()
        except ClientError as error:
            code=error.response['Error']['Code'];message=error.response['Error'].get('Message','').lower()
            retention_rejection=code=='InvalidRequest' and any(word in message for word in ('retention','compliance','locked'))
            check(code=='AccessDenied' or retention_rejection,label)
            denials.append({'scenario':label,'s3_error_code':code,'http_status':error.response['ResponseMetadata']['HTTPStatusCode']})
            return
        raise RuntimeError(label+' unexpectedly succeeded')
    context=subprocess.check_output(['docker','context','show'],text=True).strip()
    check(context=='desktop-linux','Confirmed Docker Desktop Linux context')
    inspected=json.loads(subprocess.check_output(['docker','inspect','motorph-m2-archive-storage-1'],text=True))[0]
    check(inspected['Config']['Labels'].get('com.docker.compose.project')=='motorph-m2-archive','Owned dedicated Compose project')
    check(all(item['HostIp']=='127.0.0.1' for bindings in inspected['NetworkSettings']['Ports'].values() if bindings for item in bindings),'S3 and console ports bind loopback only')
    s3=client();root=client('root')
    check(root.get_bucket_versioning(Bucket=BUCKET)['Status']=='Enabled','Bucket versioning enabled')
    retention=root.get_object_lock_configuration(Bucket=BUCKET)['ObjectLockConfiguration']['Rule']['DefaultRetention']
    check(retention=={'Mode':'COMPLIANCE','Days':7},'Seven-day COMPLIANCE default retention configured')
    subprocess.run([sys.executable,str(ROOT/'tools/archive_audit.py'),'--object-storage'],check=True)
    path=max((RUNTIME/'audit-archives').glob('*.s3-receipt.json'),key=lambda p:p.stat().st_mtime_ns)
    receipt=json.loads(path.read_text(encoding='utf-8'));key=receipt['key'];version=receipt['version_id']
    raw=s3.get_object(Bucket=BUCKET,Key=key,VersionId=version)['Body'].read()
    check(hashlib.sha256(raw).hexdigest()==receipt['sha256'] and verify(json.loads(raw))==receipt['events'],'Exact uploaded version downloaded and full audit chain verified')
    anonymous=boto3.client('s3',endpoint_url=ENDPOINT,region_name='us-east-1',config=Config(signature_version=UNSIGNED))
    denied(lambda:anonymous.get_object(Bucket=BUCKET,Key=key),'Anonymous archive read denied')
    denied(lambda:s3.delete_object(Bucket=BUCKET,Key=key,VersionId=version),'Backup writer cannot delete retained archive version')
    denied(lambda:s3.put_bucket_policy(Bucket=BUCKET,Policy=json.dumps({'Version':'2012-10-17','Statement':[]})),'Backup writer cannot change bucket policy')
    denied(lambda:s3.put_object(Bucket=BUCKET,Key='outside-scope/denied.txt',Body=b'owned-test'),'Backup writer cannot upload outside audit prefix')
    other='validation/owned-'+secrets.token_hex(8)+'.txt'
    root.put_object(Bucket=BUCKET,Key=other,Body=b'Owned scope-denial fixture; retained seven days')
    denied(lambda:s3.get_object(Bucket=BUCKET,Key=other),'Backup writer cannot read outside audit prefix')
    denied(lambda:root.delete_object(Bucket=BUCKET,Key=key,VersionId=version),'S3 root cannot delete an active COMPLIANCE-protected version')
    denied(lambda:root.put_object_retention(Bucket=BUCKET,Key=key,VersionId=version,Retention={'Mode':'COMPLIANCE','RetainUntilDate':datetime.datetime.now(datetime.timezone.utc)+datetime.timedelta(days=1)}),'S3 root cannot shorten active compliance retention')
    with tempfile.TemporaryDirectory(prefix='integrity-',dir=STORE) as temporary:
        broken=Path(temporary)/'audit-corrupt.json'
        broken.write_text(json.dumps({'head':{'id':1,'hash':'0'*64},'events':[]}))
        try:
            upload_archive(broken,{'sha256':hashlib.sha256(broken.read_bytes()).hexdigest()})
        except ValueError:
            check(True,'Corrupt local audit chain rejected before upload')
        else:
            raise RuntimeError('Corrupt audit upload unexpectedly succeeded')
    before=hashlib.sha256(raw).hexdigest()
    subprocess.run(['docker','restart','motorph-m2-archive-storage-1'],check=True,capture_output=True)
    for attempt in range(40):
        try:
            with urllib.request.urlopen(ENDPOINT+'/health',timeout=2) as health:
                if health.status==200: break
        except OSError:
            time.sleep(1)
    restored=client().get_object(Bucket=BUCKET,Key=key,VersionId=version)['Body'].read()
    check(hashlib.sha256(restored).hexdigest()==before,'Dedicated container restart preserves exact archive version and writer access')
    check(verify(json.loads(restored))==receipt['events'],'Restored archive chain remains valid after container restart')
    local=RUNTIME/'audit-archives'
    before_count=len([p for p in local.glob('audit-*.json') if p.name.count('.')==1])
    subprocess.run(['docker','stop','motorph-m2-archive-storage-1'],check=True,capture_output=True)
    try:
        failed=subprocess.run(['powershell','-NoProfile','-File',str(ROOT/'scripts/archive-audit.ps1')],capture_output=True,timeout=60)
        check(failed.returncode!=0,'Unavailable S3 makes the archive wrapper fail visibly')
        after_count=len([p for p in local.glob('audit-*.json') if p.name.count('.')==1])
        check(after_count>before_count,'Unavailable S3 preserves the newly verified local archive')
    finally:
        subprocess.run(['docker','start','motorph-m2-archive-storage-1'],check=True,capture_output=True)
        for attempt in range(40):
            try:
                with urllib.request.urlopen(ENDPOINT+'/health',timeout=2) as health:
                    if health.status==200: break
            except OSError:
                time.sleep(1)
        else:
            raise RuntimeError('Owned storage failed to recover after availability test')
    report={'recorded_at_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
            'scope':'Real RustFS1.0.2 in owned Docker Desktop project on this same Windows PC',
            'assertions':len(passed),'outcomes':passed,'denials':denials,'retained_events':receipt['events'],
            'independent_offhost_custody':False,'boundary':'Host administrator and loss of this PC/volume are outside the retention guarantee',
            'fixture_note':'One tiny scope-denial object is intentionally retained for seven days; no protected archive deletion attempted after denial.'}
    (STORE/'assurance.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print('Local object-storage assurance passed: '+str(len(passed))+' checks; same-PC custody only.')


if __name__=='__main__':
    main()
