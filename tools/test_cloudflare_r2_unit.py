"""Offline R2 safety regressions. These fixtures do not establish live R2 assurance."""
from pathlib import Path
from io import BytesIO
import hashlib
import json
import sys
import tempfile
import unittest
from unittest.mock import Mock, patch
import archive_audit
import cloudflare_r2 as r2


class R2SafetyTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix='motorph-r2-offline-')
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        store_patch=patch.object(r2,'STORE',self.directory)
        store_patch.start()
        self.addCleanup(store_patch.stop)
        (self.directory/'cost-policy.json').write_text(json.dumps({'kind':'UserAcceptedMeteredFreeTier','allow_metered_provider':True}))
        self.archive = self.directory/'audit-fixture.json'
        self.raw = json.dumps({'head':{'id':0,'hash':'0'*64},'events':[]}).encode()
        self.archive.write_bytes(self.raw)
        self.manifest = {'sha256':hashlib.sha256(self.raw).hexdigest()}
        self.settings = {'kind':'MotorPHOwnedCloudflareR2','account_id':'a'*32,
            'endpoint':'https://'+'a'*32+'.r2.cloudflarestorage.com',
            'bucket':r2.BUCKET,'retention_days':7,'state':'configured'}
        self.lock = {'rules':[{'enabled':True,'prefix':'audit/',
            'condition':{'type':'Age','maxAgeSeconds':604800}}]}

    def read_profile(self, **changes):
        path=self.directory/'profile.json'
        path.write_text(json.dumps({**self.settings, **changes}))
        with patch.object(r2,'PROFILE',path):
            return r2.profile()

    def test_valid_owned_profile(self):
        self.assertEqual(self.read_profile()['bucket'],r2.BUCKET)

    def test_endpoint_cannot_redirect_credentials_to_other_host(self):
        with self.assertRaises(ValueError):self.read_profile(endpoint='https://example.org')

    def test_profile_cannot_select_other_bucket(self):
        with self.assertRaises(ValueError):self.read_profile(bucket='unrelated-bucket')

    def test_partial_setup_cannot_be_used_for_transfer(self):
        with self.assertRaises(ValueError):self.read_profile(state='preparing')

    def test_account_identity_cannot_inject_url_path(self):
        with self.assertRaises(ValueError):self.read_profile(account_id='a'*32+'/other')

    def test_wrong_retention_profile_is_rejected(self):
        with self.assertRaises(ValueError):self.read_profile(retention_days=1)

    def test_missing_disabled_short_or_wrong_prefix_lock_rejected(self):
        bad=[{}, {'rules':[]}, {'rules':[{**self.lock['rules'][0],'enabled':False}]},
            {'rules':[{**self.lock['rules'][0],'prefix':'other/'}]},
            {'rules':[{'enabled':True,'prefix':'audit/','condition':{'type':'Age','maxAgeSeconds':1}}]}]
        for rules in bad:
            with self.subTest(rules=rules),self.assertRaises(ValueError):r2.require_lock(rules)
        r2.require_lock(self.lock)

    def test_checksum_mismatch_never_opens_remote_credentials(self):
        with patch.object(r2,'profile') as profile,self.assertRaises(ValueError):
            r2.upload_archive(self.archive,{'sha256':'bad'})
        profile.assert_not_called()

    def test_damaged_chain_never_opens_remote_credentials(self):
        self.archive.write_text(json.dumps({'head':{'id':1,'hash':'0'*64},'events':[]}))
        digest=hashlib.sha256(self.archive.read_bytes()).hexdigest()
        with patch.object(r2,'profile') as profile,self.assertRaises(ValueError):
            r2.upload_archive(self.archive,{'sha256':digest})
        profile.assert_not_called()

    def test_single_archive_limit_rejects_before_remote_access(self):
        with patch.object(r2,'MAX_ARCHIVE_BYTES',1),patch.object(r2,'profile') as profile,self.assertRaises(ValueError):
            r2.upload_archive(self.archive,self.manifest)
        profile.assert_not_called()

    def test_capacity_counts_later_pages_and_incoming_bytes(self):
        s3=Mock()
        s3.get_paginator.return_value.paginate.return_value=[{'Contents':[{'Size':5}]},{'Contents':[{'Size':5}]}]
        with patch.object(r2,'MAX_BUCKET_BYTES',15),self.assertRaises(ValueError):r2.require_capacity(s3,6)

    def test_retrieval_mismatch_does_not_write_success_receipt(self):
        s3=Mock()
        s3.get_paginator.return_value.paginate.return_value=[]
        s3.get_object.return_value={'Body':BytesIO(b'corrupt returned data')}
        with patch.object(r2,'profile',return_value=self.settings),patch.object(r2,'api',return_value=self.lock),patch.object(r2,'client',return_value=s3),self.assertRaises(ValueError):
            r2.upload_archive(self.archive,self.manifest)
        self.assertFalse(self.archive.with_suffix('.r2-receipt.json').exists())

    def test_success_verifies_both_payloads_and_records_exact_scope(self):
        objects={}
        s3=Mock()
        s3.get_paginator.return_value.paginate.return_value=[]
        def put(**kwargs):
            self.assertEqual(kwargs['IfNoneMatch'],'*')
            objects[kwargs['Key']]=kwargs['Body']
        s3.put_object.side_effect=put
        s3.get_object.side_effect=lambda **kwargs:{'Body':BytesIO(objects[kwargs['Key']])}
        with patch.object(r2,'profile',return_value=self.settings),patch.object(r2,'api',return_value=self.lock),patch.object(r2,'client',return_value=s3):
            receipt=r2.upload_archive(self.archive,self.manifest)
        self.assertTrue(receipt['offhost_storage'])
        self.assertFalse(receipt['independent_administrator_custody_proven'])
        self.assertEqual(len(objects),2)

    def test_remote_failure_preserves_new_local_snapshot_and_manifest(self):
        with patch.object(archive_audit,'RUNTIME',self.directory),patch.object(archive_audit,'snapshot',return_value=json.loads(self.raw)),patch.object(archive_audit,'private_directory',side_effect=lambda p:p.mkdir()),patch.object(r2,'upload_archive',side_effect=RuntimeError('simulated remote outage')),patch.object(sys,'argv',['archive_audit.py','--cloudflare-r2']),self.assertRaises(RuntimeError):
            archive_audit.main()
        exported=list((self.directory/'audit-archives').glob('*.json'))
        self.assertEqual(len(exported),2)
        self.assertTrue(any(p.name.endswith('.manifest.json') for p in exported))

    def test_failed_cloud_destination_still_attempts_local_docker(self):
        import object_storage
        with patch.object(archive_audit,'RUNTIME',self.directory),patch.object(archive_audit,'snapshot',return_value=json.loads(self.raw)),patch.object(archive_audit,'private_directory',side_effect=lambda p:p.mkdir()),patch.object(r2,'upload_archive',side_effect=RuntimeError('simulated remote outage')),patch.object(object_storage,'upload_archive') as local,patch.object(sys,'argv',['archive_audit.py','--cloudflare-r2','--object-storage']),self.assertRaises(RuntimeError):
            archive_audit.main()
        local.assert_called_once()

    def test_request_budget_stops_before_exceeding_daily_allowance(self):
        with patch.object(r2,'MAX_DAILY_REQUESTS',2):
            r2.consume_request_budget()
            r2.consume_request_budget()
            with self.assertRaises(ValueError):r2.consume_request_budget()
        self.assertEqual(json.loads((self.directory/'request-budget.json').read_text())['daily'],2)

    def test_corrupt_budget_is_not_silently_reset(self):
        (self.directory/'request-budget.json').write_text('not valid JSON')
        with self.assertRaises(ValueError):r2.consume_request_budget()

    def test_unaccepted_metered_use_is_blocked(self):
        (self.directory/'cost-policy.json').write_text(json.dumps({'allow_metered_provider':False}))
        with self.assertRaises(ValueError):r2.consume_request_budget()

    def test_monthly_request_limit_is_enforced_on_a_new_daily_budget(self):
        import datetime
        today=datetime.datetime.now(datetime.timezone.utc).date().isoformat()
        (self.directory/'request-budget.json').write_text(json.dumps({'month':today[:7],'monthly':2000,'day':today,'daily':0}))
        with self.assertRaises(ValueError):r2.consume_request_budget()


if __name__=='__main__':
    unittest.main(verbosity=2)
