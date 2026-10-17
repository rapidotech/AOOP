$ErrorActionPreference = 'Stop'
$archiveScript = Join-Path (Split-Path $PSScriptRoot -Parent) 'tools/archive_audit.py'
$storageProfile = Join-Path $env:USERPROFILE '.motorph/milestone2/object-storage/profile.json'
$archiveArguments=@()
$archivePython='python'
if (Test-Path -LiteralPath $storageProfile) {
    $profile = Get-Content -LiteralPath $storageProfile -Raw | ConvertFrom-Json
    if ($profile.kind -ne 'MotorPHOwnedLocalObjectStorage' -or $profile.endpoint -ne 'http://127.0.0.1:19000') {
        throw 'Unexpected local object-storage profile; archive transfer refused.'
    }
    $archivePython=$profile.python_executable
    $archiveArguments+='--object-storage'
}
$r2Directory=Join-Path $env:USERPROFILE '.motorph/milestone2/cloudflare-r2'
$r2Marker=Join-Path $r2Directory 'enabled.json'
if(Test-Path -LiteralPath $r2Marker) {
    $r2ProfilePath=Join-Path $r2Directory 'profile.json'
    $r2AssurancePath=Join-Path $r2Directory 'assurance.json'
    $marker=Get-Content -LiteralPath $r2Marker -Raw | ConvertFrom-Json
    $r2Profile=Get-Content -LiteralPath $r2ProfilePath -Raw | ConvertFrom-Json
    if($marker.kind -ne 'MotorPHVerifiedR2Schedule' -or
       (Get-FileHash -LiteralPath $r2ProfilePath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $marker.profile_sha256 -or
       (Get-FileHash -LiteralPath $r2AssurancePath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $marker.assurance_sha256 -or
       $r2Profile.kind -ne 'MotorPHOwnedCloudflareR2' -or
       $r2Profile.account_id -notmatch '^[a-f0-9]{32}$' -or
       $r2Profile.endpoint -ne ('https://'+$r2Profile.account_id+'.r2.cloudflarestorage.com') -or
       $r2Profile.bucket -ne 'motorph-audit-archives') {
        throw 'R2 profile or assurance differs from the verified setup; transfer refused.'
    }
    $archivePython=$r2Profile.python_executable
    $archiveArguments+='--cloudflare-r2'
}
& $archivePython $archiveScript @archiveArguments
if ($LASTEXITCODE -ne 0) { throw 'Audit chain verification or archive failed; inspect the owned test environment.' }
