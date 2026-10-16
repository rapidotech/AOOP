param([Parameter(Mandatory=$true)][string]$Username, [switch]$CopyToClipboard)
$ErrorActionPreference = 'Stop'
$path = Join-Path $env:USERPROFILE '.motorph/milestone2/bootstrap-passwords.dpapi'
$protected = Get-Content -LiteralPath $path -Raw
$secure = ConvertTo-SecureString $protected.Trim()
$credential = New-Object System.Management.Automation.PSCredential('bootstrap', $secure)
$accounts = $credential.GetNetworkCredential().Password | ConvertFrom-Json
$entry = $accounts | Where-Object username -EQ $Username
if (!$entry) { throw 'No pending bootstrap credential exists for that username.' }
if ($CopyToClipboard) {
    Set-Clipboard -Value $entry.password
    Write-Output 'Temporary credential copied. Complete the mandatory password change in MotorPH.'
} else {
    # This intentionally displays a credential only when the local operator requests it.
    Write-Output $entry.password
}
