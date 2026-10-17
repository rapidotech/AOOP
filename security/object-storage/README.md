# Local Docker audit archive storage

The prepared Windows PC runs RustFS 1.0.2 in the dedicated
`motorph-m2-archive` Docker Desktop Compose project. Its official image is pinned
to a digest in `compose.yml`. Existing Docker projects are preserved.

- S3 API: `http://127.0.0.1:19000`
- Console: `http://127.0.0.1:19001`
- Private bucket: `motorph-audit-archives`, with versioning and seven-day
  COMPLIANCE retention. Seven days is the chosen lab setting, not an approved
  institutional retention policy.
- Named volume: `motorph-m2-archive_archive-data`, retained when the container is
  restarted/recreated. Container logs rotate at 5 MB with three files.

This is **same-PC storage**. It does not provide independent off-host custody or
protection from loss of this computer. Docker/host administrators can alter the
underlying volume. The S3 API retention tests do not establish administrator-proof
storage. Both endpoints bind loopback; the local S3 transport is HTTP, not TLS.
The existing database's verified TLS configuration is unchanged.

## Setup and use

Start Docker Desktop with its Linux engine, then from the repository root:

```powershell
./scripts/setup-object-storage.ps1
./scripts/archive-audit.ps1
./scripts/get-object-storage-access.ps1 -CopySecret
```

The last command displays the console access key and copies its secret to the
clipboard without printing it. Use it only when you need administrative access.
Normal backups use a separate IAM writer that cannot delete objects, change
retention or bucket policies, or access other prefixes. Anonymous reads are
denied. Compliance retention also denies exact-version deletion and retention
shortening through the root S3 API during the active period.

The bootstrap root and backup writer credentials are separate DPAPI CurrentUser
files under `%USERPROFILE%/.motorph/milestone2/object-storage`. The parent
directory is ACL-restricted to the provisioning user and SYSTEM. The private
`compose.env` supplies the generated root credentials to Docker; Docker
administrators can inspect container environment variables. These files and the
real audit objects are never checked into Git or the public coursework package.

The existing daily task `MotorPH-M2-Audit-Archive` uses the wrapper above. With a
storage profile present, the wrapper uses the pinned private Python environment
to export, upload, retrieve and verify the exact object version and active
retention. A second protected manifest records its hash/version. It fails with a
nonzero result if storage verification fails, preserving the local export.
Docker Desktop and the owned MySQL clone must be available; the Windows user
must be signed in. A manual task invocation was verified; unattended operation
while signed out is not claimed.

Each private local `.s3-receipt.json` identifies the object version and checksum.
Retrieving that specific version, checking SHA-256 and re-running full audit-chain
verification is the recovery check; a bucket listing alone is insufficient.

## Verification and rollback

```powershell
& "$env:USERPROFILE/.motorph/milestone2/object-storage/venv/Scripts/python.exe" tools/test_object_storage.py
python tools/test_integrity_gates.py
```

The first command operates only on this owned bucket, creates tiny retained test
objects, and restarts only the dedicated storage container. Its 18 observed
checks include upload/retrieval, scope/anonymous denial, root/writer deletion
denial, retention shortening denial, corruption rejection, restart persistence,
and visible failure/local-copy preservation when storage is unavailable.
Sanitized results are recorded in the private runtime's `assurance.json`; the
scheduled-task receipt is `scheduler-proof.json`. The selected fixture objects
are subject to the same seven-day retention and are intentionally retained.

To stop the service while preserving its volume, use the exact Compose identity:

```powershell
docker compose -p motorph-m2-archive --env-file "$env:USERPROFILE/.motorph/milestone2/object-storage/compose.env" -f security/object-storage/compose.yml stop storage
```

Backups then retain their local export and report S3 failure until the service
returns. Re-run setup to start the same project and reuse the protected profile.
Do not remove volumes as a routine rollback. To complete off-host assurance,
deploy to an authorized second machine and test its independent permissions,
retention and recovery before changing C5's broader status to Verified.

Official references: [RustFS Docker deployment](https://docs.rustfs.com/en/installation/container/docker),
[IAM](https://docs.rustfs.com/en/security-compliance/iam/policies),
[Object Lock](https://github.com/rustfs/docs.rustfs.com/blob/main/content/en/administration/data/object/object-lock.md).
