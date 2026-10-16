param([string]$Path = $env:M2_BOOTSTRAP_PATH)
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($Path) -or [string]::IsNullOrWhiteSpace($env:M2_BOOTSTRAP_JSON)) {
    throw 'An escrow destination and enrollment payload are required.'
}
$entries = @($env:M2_BOOTSTRAP_JSON | ConvertFrom-Json)
foreach ($entry in $entries) {
    if ([string]::IsNullOrWhiteSpace($entry.username) -or [string]::IsNullOrWhiteSpace($entry.password)) {
        throw 'Every escrow entry must contain a username and temporary password.'
    }
}
if (Test-Path -LiteralPath $Path) {
    $oldSecure = ConvertTo-SecureString ((Get-Content -LiteralPath $Path -Raw).Trim())
    $oldCredential = New-Object System.Management.Automation.PSCredential('escrow', $oldSecure)
    $oldEntries = @($oldCredential.GetNetworkCredential().Password | ConvertFrom-Json)
    foreach ($oldEntry in $oldEntries) {
        if ($oldEntry.username -notin $entries.username) { throw 'The complete existing escrow map must be preserved before adding entries.' }
    }
}
# Preserve canonical JSON supplied by the parent. Windows PowerShell 5 can
# serialize extended Object[] wrapper metadata when rebuilding an array.
$plaintext = $env:M2_BOOTSTRAP_JSON
$secure = ConvertTo-SecureString $plaintext -AsPlainText -Force
$ciphertext = ConvertFrom-SecureString $secure
$temporary = $Path + '.tmp-' + [Guid]::NewGuid().ToString('N')
try {
    [IO.File]::WriteAllText($temporary, $ciphertext, (New-Object Text.UTF8Encoding($false)))
    $roundTrip = ConvertTo-SecureString ([IO.File]::ReadAllText($temporary))
    $roundTripCredential = New-Object System.Management.Automation.PSCredential('escrow', $roundTrip)
    if ($roundTripCredential.GetNetworkCredential().Password -cne $plaintext) {
        throw 'Protected escrow round trip failed. Existing escrow was preserved.'
    }
    if (Test-Path -LiteralPath $Path) {
        $previous = $Path + '.previous-' + [Guid]::NewGuid().ToString('N')
        [IO.File]::Replace($temporary, $Path, $previous)
    } else {
        [IO.File]::Move($temporary, $Path)
    }
} finally {
    if (Test-Path -LiteralPath $temporary) { Remove-Item -LiteralPath $temporary -Force }
    $plaintext = $null
    $entries = $null
    $oldEntries = $null
}
