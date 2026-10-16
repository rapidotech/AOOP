$ErrorActionPreference = 'Stop'
$taskName = 'MotorPH-M2-Audit-Archive'
$description = 'MotorPH owned isolated clone: verify and archive local audit chain while provisioning user is signed in.'
$existing = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
if ($existing -and $existing.Description -ne $description) { throw 'A different task already owns this name; it was preserved.' }
$script = Join-Path $PSScriptRoot 'archive-audit.ps1'
$engine = Join-Path $env:SystemRoot 'System32/WindowsPowerShell/v1.0/powershell.exe'
$action = New-ScheduledTaskAction -Execute $engine -Argument ('-NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File "' + $script + '"')
$trigger = New-ScheduledTaskTrigger -Daily -At '23:00'
$principal = New-ScheduledTaskPrincipal -UserId ([System.Security.Principal.WindowsIdentity]::GetCurrent().Name) -LogonType Interactive -RunLevel Limited
$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -ExecutionTimeLimit (New-TimeSpan -Minutes 5)
Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Principal $principal -Settings $settings -Description $description -Force | Out-Null
Write-Output 'Owned daily local audit archive task installed. Runs while this Windows user is signed in; external archival remains separate.'
