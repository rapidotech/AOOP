param([switch]$Integration)
$ErrorActionPreference = 'Stop'
$repository = Split-Path $PSScriptRoot -Parent
Push-Location $repository
try {
    & (Join-Path $PSScriptRoot 'build.ps1') -Test
    if ($LASTEXITCODE -ne 0) { throw 'Build/regression gate failed.' }
    & python (Join-Path $PSScriptRoot 'check-security.py')
    if ($LASTEXITCODE -ne 0) { throw 'Focused security gate failed.' }
    & python (Join-Path $PSScriptRoot 'check-repository.py')
    if ($LASTEXITCODE -ne 0) { throw 'Secret scanner gate failed.' }
    & python (Join-Path $PSScriptRoot 'check-dependencies.py')
    if ($LASTEXITCODE -ne 0) { throw 'Dependency/advisory gate failed.' }
    if ($Integration) {
        & python (Join-Path $repository 'tools/run_expanded_assurance.py')
        if ($LASTEXITCODE -ne 0) { throw 'Isolated runtime assurance gate failed.' }
        Write-Output 'Complete local release gate passed, including the owned isolated database.'
    } else {
        Write-Output 'Source/build gate passed. Runtime database assurance is a separate required release check: rerun with -Integration on the prepared clone.'
    }
} finally { Pop-Location }
