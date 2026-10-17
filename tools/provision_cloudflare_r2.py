"""Provision only the named MotorPH bucket using a privately imported bootstrap token."""
import datetime
import json
import sys
import uuid
from milestone2_environment import private_directory, protect_credential, unprotect_credential
from cloudflare_r2 import STORE, PROFILE, BUCKET, account_id, api, require_lock


def main():
    private_directory(STORE)
    if not (STORE/'bootstrap.credential.xml').exists():
        raise ValueError('Import the explicitly authorized bootstrap token privately first')
    accounts = api('/accounts', identity='bootstrap')
    if len(accounts) != 1:
        raise ValueError('Expected exactly one authorized Cloudflare account; choose explicitly')
    account = account_id(accounts[0]['id'])
    verified = api(f'/accounts/{account}/tokens/verify', identity='bootstrap')
    if verified.get('status') != 'active':
        raise ValueError('Bootstrap token is not active')
    bootstrap = unprotect_credential(STORE/'bootstrap.credential.xml')
    if bootstrap['user'] != verified['id']:
        protect_credential(STORE/'bootstrap.credential.xml',verified['id'],bootstrap['password'])
    route = f'/accounts/{account}/r2/buckets'
    # Activation/ownership checks precede all writes. HTTP10042 requires dashboard checkout.
    listing = api(route, identity='bootstrap')
    existing = listing.get('buckets', [])
    owned_path = STORE/'bucket-owned.json'
    if any(item.get('name') == BUCKET for item in existing):
        if not owned_path.exists() or json.loads(owned_path.read_text()) != {'account_id':account,'bucket':BUCKET}:
            raise ValueError('Existing unmarked R2 bucket preserved; ownership must be reviewed')
    else:
        api(route, identity='bootstrap', method='POST', body={'name':BUCKET,'storageClass':'Standard'})
        owned_path.write_text(json.dumps({'account_id':account,'bucket':BUCKET}), encoding='utf-8')
    route += '/' + BUCKET
    api(route+'/domains/managed', identity='bootstrap', method='PUT', body={'enabled':False})
    api(route+'/lock', identity='bootstrap', method='PUT', body={'rules':[
        {'id':'motorph-audit-retention','enabled':True,'prefix':'audit/',
         'condition':{'type':'Age','maxAgeSeconds':7*86400}}]})
    api(route+'/lifecycle', identity='bootstrap', method='PUT', body={'rules':[
        {'id':'motorph-audit-expiry','enabled':True,'conditions':{'prefix':'audit/'},
         'deleteObjectsTransition':{'condition':{'type':'Age','maxAge':8*86400}}}]})
    groups = api(f'/accounts/{account}/tokens/permission_groups', identity='bootstrap')
    # Preserve credentials on retry. Token values are shown only once by Cloudflare.
    for kind, group_name, resources in [
        ('writer','Workers R2 Storage Bucket Item Write',
         {f'com.cloudflare.edge.r2.bucket.{account}_default_{BUCKET}':'*'}),
        ('reader','Workers R2 Storage Read',{f'com.cloudflare.api.account.{account}':'*'})]:
        destination = STORE/(kind+'.credential.xml')
        if destination.exists():
            continue
        group = next(g for g in groups if g['name'] == group_name)
        created = api(f'/accounts/{account}/tokens', identity='bootstrap', method='POST', body={
            'name':'motorph-audit-'+kind+'-'+uuid.uuid4().hex[:8],
            'expires_on':(datetime.datetime.now(datetime.timezone.utc)+datetime.timedelta(days=90)).strftime('%Y-%m-%dT%H:%M:%SZ'),
            'policies':[{'effect':'allow',
                         'permission_groups':[{'id':group['id']}], 'resources':resources}]})
        protect_credential(destination, created['id'], created['value'])
        (STORE/(kind+'-scope.json')).write_text(json.dumps({k:v for k,v in created.items() if k != 'value'},indent=2),encoding='utf-8')
    settings={'kind':'MotorPHOwnedCloudflareR2','account_id':account,'bucket':BUCKET,
        'endpoint':f'https://{account}.r2.cloudflarestorage.com','retention_days':7,
        'state':'configured','python_executable':sys.executable}
    PROFILE.write_text(json.dumps(settings,indent=2),encoding='utf-8')
    require_lock(api(route+'/lock'))
    print('Private R2 bucket configured with scoped writer and seven-day bucket lock. Not enabled for the scheduler until live assurance passes.')


if __name__ == '__main__':
    main()
