"""Provision only the dedicated localhost MotorPH Docker Compose project."""
import json
import secrets
import subprocess
import sys
import time
import urllib.request
from milestone2_environment import ROOT, private_directory, protect_credential, unprotect_credential
from object_storage import STORE, PROFILE, ENDPOINT, BUCKET, IMAGE, admin, client


def main():
    context=subprocess.check_output(['docker','context','show'],text=True).strip()
    endpoint=subprocess.check_output(['docker','context','inspect',context,'--format','{{.Endpoints.docker.Host}}'],text=True).strip()
    if context!='desktop-linux' or 'dockerDesktopLinuxEngine' not in endpoint:
        raise ValueError('This local profile requires the confirmed Docker Desktop Linux context')
    private_directory(STORE)
    if PROFILE.exists():
        from object_storage import profile
        profile()
    else:
        # Preserve generated identities across a failed provisioning retry.
        for kind in ('root','writer'):
            destination = STORE / (kind+'.credential.xml')
            if not destination.exists():
                protect_credential(destination,'m2-'+kind+'-'+secrets.token_hex(8),secrets.token_urlsafe(36))
        PROFILE.write_text(json.dumps({'kind':'MotorPHOwnedLocalObjectStorage','endpoint':ENDPOINT,
            'bucket':BUCKET,'image':IMAGE,'project':'motorph-m2-archive','version':'1.0.2',
            'retention_days':7,'independent_offhost':False,'python_executable':sys.executable},indent=2))
    credential = unprotect_credential(STORE/'root.credential.xml')
    env = STORE/'compose.env'
    env.write_text('MOTORPH_STORAGE_ROOT_KEY='+credential['user']+'\nMOTORPH_STORAGE_ROOT_SECRET='+credential['password']+'\n')
    compose = ['docker','compose','-p','motorph-m2-archive','--env-file',str(env),'-f',str(ROOT/'security/object-storage/compose.yml')]
    # Dedicated project scope; no Docker-wide start, stop, prune or removal.
    subprocess.run(compose+['config','--quiet'],check=True)
    subprocess.run(compose+['up','-d'],check=True)
    for attempt in range(45):
        try:
            with urllib.request.urlopen(ENDPOINT+'/health',timeout=2) as response:
                if response.status==200:
                    break
        except (OSError,urllib.error.URLError):
            time.sleep(1)
    else:
        raise RuntimeError('Owned object-storage health check timed out')
    s3=client('root')
    buckets=s3.list_buckets()['Buckets']
    if not any(item['Name']==BUCKET for item in buckets):
        s3.create_bucket(Bucket=BUCKET,ObjectLockEnabledForBucket=True)
        (STORE/'bucket-owned.json').write_text(json.dumps({'bucket':BUCKET,'project':'motorph-m2-archive','state':'configuring'}))
    elif not (STORE/'bucket-owned.json').exists():
        raise RuntimeError('An unmarked existing bucket was preserved; investigate ownership')
    s3.put_bucket_versioning(Bucket=BUCKET,VersioningConfiguration={'Status':'Enabled'})
    s3.put_object_lock_configuration(Bucket=BUCKET,ObjectLockConfiguration={'ObjectLockEnabled':'Enabled','Rule':{'DefaultRetention':{'Mode':'COMPLIANCE','Days':7}}})
    (STORE/'bucket-owned.json').write_text(json.dumps({'bucket':BUCKET,'project':'motorph-m2-archive'}))
    arn='arn:aws:s3:::'+BUCKET
    policy={'Version':'2012-10-17','Statement':[
        {'Effect':'Allow','Action':['s3:GetBucketLocation','s3:GetBucketObjectLockConfiguration'],'Resource':[arn]},
        {'Effect':'Allow','Action':['s3:ListBucket'],'Resource':[arn],'Condition':{'StringLike':{'s3:prefix':['audit/*']}}},
        {'Effect':'Allow','Action':['s3:PutObject','s3:GetObject','s3:GetObjectVersion','s3:GetObjectRetention'],'Resource':[arn+'/audit/*']},
        {'Effect':'Deny','Action':['s3:DeleteObject','s3:DeleteObjectVersion','s3:BypassGovernanceRetention','s3:PutObjectRetention','s3:PutObjectLegalHold'],'Resource':[arn+'/*']}
    ]}
    writer=unprotect_credential(STORE/'writer.credential.xml')
    admin('PUT','add-canned-policy',{'name':'motorph-audit-writer'},policy)
    admin('PUT','add-user',{'accessKey':writer['user']},{'secretKey':writer['password'],'status':'enabled'})
    admin('PUT','set-user-or-group-policy',{'policyName':'motorph-audit-writer','userOrGroup':writer['user'],'isGroup':'false'})
    print('Owned RustFS1.0.2 ready: loopback19000 S3 /19001 console; dedicated writer; versioning and seven-day compliance retention. Local Docker, not off-host.')


if __name__=='__main__':
    main()
