$ErrorActionPreference = 'Stop'
& python (Join-Path (Split-Path $PSScriptRoot -Parent) 'tools/archive_audit.py')
if ($LASTEXITCODE -ne 0) { throw 'Audit chain verification or archive failed; inspect the owned test environment.' }
