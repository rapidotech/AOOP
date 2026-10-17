$ErrorActionPreference='Stop'
$repoRoot=Split-Path $PSScriptRoot -Parent
$runtime=Join-Path $env:USERPROFILE '.motorph/milestone2/object-storage'
$storagePython=Join-Path $runtime 'venv/Scripts/python.exe'
if(!(Test-Path -LiteralPath $storagePython)) {
    & python -m venv (Join-Path $runtime 'venv')
    if($LASTEXITCODE -ne 0){throw 'Storage Python environment creation failed.'}
}
& $storagePython -m pip install --disable-pip-version-check -r (Join-Path $repoRoot 'security/object-storage/requirements.txt')
if($LASTEXITCODE -ne 0){throw 'Storage dependency installation failed.'}
& $storagePython (Join-Path $repoRoot 'tools/provision_cloudflare_r2.py')
if($LASTEXITCODE -ne 0){throw 'R2 setup did not complete; local Docker backups remain configured.'}
