# Cloudflare R2 off-host audit backups

The owned private `motorph-audit-archives` R2 bucket now holds verified copies
outside the payroll PC. This supplements the existing local files and RustFS
Docker service; it does not replace them or move the MySQL database.

## Implemented and observed

- Standard storage; public `r2.dev` access disabled; no public custom domain.
- Seven-day bucket lock for `audit/`. While active, it blocks object deletion
  and overwriting. Cloudflare administrators can remove the rule. This is not
  S3 COMPLIANCE Object Lock or proof of independently administered custody.
- Eight-day expiry configured for that prefix. Its configuration was verified;
  actual deletion eight days later has not yet been observed.
- A bucket-scoped object writer and a separate read-only configuration identity.
  Both generated tokens expire after 90 days. Their token values are protected
  using Windows DPAPI CurrentUser under `.motorph/milestone2/cloudflare-r2`.
- The bootstrap token is used for provisioning and controlled assurance only.
  It remains available on the provisioning PC, including the user's original
  credential file. A compromised administrator on that PC may therefore have
  cloud administration capability; no protection against that is claimed.
- Uploads require an intact audit chain, matching checksum and active lock.
  Downloaded audit bytes and the protected manifest are compared before a local
  `.r2-receipt.json` is written. R2 object version IDs are not claimed.
- Live assurance passed 20 checks, including actual off-host recovery and eight
  rejected operations. R2 rejected anonymous S3 access with HTTP400
  `InvalidArgument` / missing Authorization; locked-object operations returned
  `ObjectLockedByBucketPolicy`; other access/configuration denials retain their
  actual codes in the receipt. These differ from the local RustFS responses.
- An actual Windows scheduled invocation returned 0 and produced verified Docker
  and R2 receipts for the same archive checksum. The task remains daily23:00
  while the provisioning user is signed in. MySQL availability is required;
  Docker and Internet/R2 availability are required for their respective copies.

## Free-tier safeguards and limits

The user accepted metered R2 use with safeguards after the billing limitation
was explained. **This is not a provider-enforced $0 spending cap.** Other account
activity, provider pricing changes or use outside this integration are not
controlled by these local limits. No zero-dollar invoice has been certified.

The implementation rejects archives over 10 MiB or transfers that would exceed
256 MiB of stored objects in the owned bucket. A Windows file lock protects a
persistent request counter: at most 200 attempts per UTC day and 2,000 per UTC
month. Requests are counted before sending, including SDK retries. A corrupt or
exhausted counter fails closed. Those limits apply to this integration, not to
other software or dashboard activity. Only Standard storage is configured.
No Worker, public website, analytics catalog, R2 SQL or paid add-on was enabled.

Cloudflare's documented Standard free allowance is 10 GB-month, one million
Class A operations and ten million Class B operations monthly. The conservative
application limits are intended to stay well below those allowances. Exceeding
Cloudflare's allowances remains billable. Budget alerts do not stop charges.

## Commands and recovery

```powershell
./scripts/setup-cloudflare-r2.ps1
& "$env:USERPROFILE/.motorph/milestone2/object-storage/venv/Scripts/python.exe" tools/test_cloudflare_r2_unit.py
& "$env:USERPROFILE/.motorph/milestone2/object-storage/venv/Scripts/python.exe" tools/test_cloudflare_r2.py --enable-schedule
./scripts/archive-audit.ps1
```

Setup requires the privately imported, authorized bootstrap token and an active
R2 subscription. It preserves existing unmarked buckets. Assurance creates a
tiny retained fixture and a temporary separate bucket to test scope. Only that
new temporary bucket is removed. The real archive bucket is never emptied.

The scheduled wrapper enables R2 only after live assurance writes `enabled.json`;
the marker binds the profile and assurance hashes. Both destinations are attempted
even if one fails. Any failed requested destination produces a nonzero result;
the local export and any successfully verified remote copy remain available.

To stop future R2 uploads while preserving every copy, rename the owned private
`cloudflare-r2/enabled.json` marker to `enabled.disabled.json`. The existing local
Docker route continues. This does not cancel the Cloudflare subscription or
delete retained cloud objects. Do not reset the cost counter to bypass a limit.

For recovery, use the scoped client to download the key recorded in the private
receipt, compare SHA-256 and run `tools/archive_audit.py --verify <download>`.
The live assurance performs this real retrieval and full-chain check. It does
not restore or overwrite the original payroll database.

Sanitized proof: `cloudflare-r2-assurance.json` and
`cloudflare-r2-scheduler.json` in the group portfolio. No token values, protected
credential files, actual audit objects or private profiles belong in Git or ZIP.

Official references: [pricing](https://developers.cloudflare.com/r2/pricing/),
[bucket locks](https://developers.cloudflare.com/r2/buckets/bucket-locks/),
[token scopes](https://developers.cloudflare.com/r2/api/tokens/),
[lifecycle rules](https://developers.cloudflare.com/r2/buckets/object-lifecycles/).
