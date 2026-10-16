$ErrorActionPreference = 'Stop'
$runtimePath = Join-Path $env:USERPROFILE '.motorph/milestone2'
$state = Get-Content -LiteralPath (Join-Path $runtimePath 'environment.json') -Raw | ConvertFrom-Json
if ($state.port -ne 3307 -or $state.schema -ne 'aoop_m2') { throw 'Unexpected isolated environment identity.' }
$listener = Get-NetTCPConnection -State Listen -LocalPort 3307 -ErrorAction SilentlyContinue
if (!$listener) { Write-Output 'Isolated instance is already stopped.'; exit 0 }
$process = Get-CimInstance Win32_Process -Filter ('ProcessId=' + $listener[0].OwningProcess)
if ($process.Name -ne 'mysqld.exe' -or $process.CommandLine -notlike ('*' + $runtimePath + '*')) {
    throw 'Port 3307 is not the owned isolated instance. No process was stopped.'
}
$credential = Import-Clixml -LiteralPath (Join-Path $runtimePath 'admin.credential.xml')
$clientFile = Join-Path $runtimePath ('shutdown-' + [Guid]::NewGuid().ToString('N') + '.cnf')
try {
    $content = "[client]`nuser=root`npassword=" + $credential.GetNetworkCredential().Password + "`nhost=127.0.0.1`nport=3307`nssl-mode=REQUIRED`n"
    [IO.File]::WriteAllText($clientFile, $content, (New-Object Text.UTF8Encoding($false)))
    & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqladmin.exe' ('--defaults-file=' + $clientFile) shutdown
    if ($LASTEXITCODE -ne 0) { throw 'Isolated shutdown failed.' }
    Write-Output 'Isolated MySQL stopped gracefully. The original service was left running.'
} finally {
    if (Test-Path -LiteralPath $clientFile) { Remove-Item -LiteralPath $clientFile -Force }
    $credential = $null
    $content = $null
}
