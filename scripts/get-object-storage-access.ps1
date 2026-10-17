param([switch]$CopySecret)
$ErrorActionPreference='Stop'
$path=Join-Path $env:USERPROFILE '.motorph/milestone2/object-storage/root.credential.xml'
$credential=Import-Clixml -LiteralPath $path
Write-Output 'Console: http://127.0.0.1:19001'
Write-Output ('Access key: '+$credential.UserName)
if($CopySecret){Set-Clipboard -Value $credential.GetNetworkCredential().Password;Write-Output 'Secret key copied to your clipboard. Keep it private.'}
else{Write-Output 'Use -CopySecret to copy the private secret key; it is never printed.'}
