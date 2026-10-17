$ErrorActionPreference = 'Stop'
$archiveScript = Join-Path (Split-Path $PSScriptRoot -Parent) 'tools/archive_audit.py'
$storageProfile = Join-Path $env:USERPROFILE '.motorph/milestone2/object-storage/profile.json'
if (Test-Path -LiteralPath $storageProfile) {
    $profile = Get-Content -LiteralPath $storageProfile -Raw | ConvertFrom-Json
    if ($profile.kind -ne 'MotorPHOwnedLocalObjectStorage' -or $profile.endpoint -ne 'http://127.0.0.1:19000') {
        throw 'Unexpected local object-storage profile; archive transfer refused.'
    }
    & $profile.python_executable $archiveScript --object-storage
} else {
    & python $archiveScript
}
if ($LASTEXITCODE -ne 0) { throw 'Audit chain verification or archive failed; inspect the owned test environment.' }
