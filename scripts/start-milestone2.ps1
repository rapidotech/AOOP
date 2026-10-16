param([switch]$SkipBuild, [switch]$VerifyOnly)
$ErrorActionPreference = 'Stop'
foreach ($injectionName in @('JAVA_TOOL_OPTIONS', '_JAVA_OPTIONS', 'JDK_JAVA_OPTIONS')) {
    if (![string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($injectionName, 'Process'))) {
        throw 'Inherited JVM options are not accepted by this isolated launcher. Clear them before launching.'
    }
}
$runtimePath = Join-Path $env:USERPROFILE '.motorph/milestone2'
$profilePath = Join-Path $runtimePath 'profile.json'
if (!(Test-Path -LiteralPath $profilePath)) { throw 'Milestone 2 profile is not provisioned. Follow security/README.md.' }
$profile = Get-Content -LiteralPath $profilePath -Raw | ConvertFrom-Json
if ($profile.host -ne '127.0.0.1' -or $profile.port -ne 3307 -or $profile.schema -ne 'aoop_m2') {
    throw 'Unexpected environment identity. Refusing to launch against another database.'
}
$trustStoreUri = ([Uri](Join-Path $runtimePath 'truststore.p12')).AbsoluteUri
$expectedUrl = 'jdbc:mysql://127.0.0.1:3307/aoop_m2?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreUrl=' + $trustStoreUri + '&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&fallbackToSystemTrustStore=false'
if ($profile.jdbc_url -cne $expectedUrl) { throw 'The JDBC target or TLS settings differ from the owned isolated profile.' }
$listener = Get-NetTCPConnection -State Listen -LocalPort 3307 -ErrorAction SilentlyContinue
if (!$listener) {
    $mysqlBinary = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe'
    $myIni = Join-Path $runtimePath 'my.ini'
    Start-Process -FilePath $mysqlBinary -ArgumentList ('"--defaults-file=' + $myIni + '"') -WindowStyle Hidden
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        Start-Sleep -Milliseconds 500
        $listener = Get-NetTCPConnection -State Listen -LocalPort 3307 -ErrorAction SilentlyContinue
        if ($listener) { break }
    }
}
if (!$listener) { throw 'Isolated MySQL did not start. Inspect its private error log.' }
$mysqlProcess = Get-CimInstance Win32_Process -Filter ('ProcessId=' + $listener[0].OwningProcess)
if ($mysqlProcess.Name -ne 'mysqld.exe' -or $mysqlProcess.CommandLine -notlike ('*' + $runtimePath + '*')) {
    throw 'Port 3307 is owned by another process. Existing processes were left untouched.'
}
if (!$SkipBuild) { & (Join-Path $PSScriptRoot 'build.ps1') }
$credential = Import-Clixml -LiteralPath (Join-Path $runtimePath 'app.credential.xml')
if ($credential.UserName -ne 'motorph_m2_app') { throw 'Unexpected application credential identity.' }
$previous = @{}
foreach ($name in @('AOOP_DB_URL', 'AOOP_DB_USER', 'AOOP_DB_PASSWORD', 'AOOP_DATA_DIR')) {
    $previous[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}
try {
    $env:AOOP_DB_URL = $profile.jdbc_url
    $env:AOOP_DB_USER = $credential.UserName
    $env:AOOP_DB_PASSWORD = $credential.GetNetworkCredential().Password
    $env:AOOP_DATA_DIR = Join-Path $runtimePath 'local-records'
    $project = Join-Path (Split-Path $PSScriptRoot -Parent) 'AOOP - MotorPH from OOP'
    # The same active target/account preflight runs before both a normal launch and verification.
        $probeClasses = Join-Path $project '.build/security-assurance'
        New-Item -ItemType Directory -Force -Path $probeClasses | Out-Null
        & javac --release 17 -encoding UTF-8 -cp "$project\.build\classes;$project\lib\*" -d $probeClasses (Join-Path (Split-Path $PSScriptRoot -Parent) 'security/tools/SecurityAssurance.java')
        if ($LASTEXITCODE -ne 0) { throw 'Profile probe compilation failed.' }
        & java -cp "$probeClasses;$project\.build\classes;$project\lib\*" SecurityAssurance profile
    if ($LASTEXITCODE -ne 0) { throw 'Active protected profile verification failed.' }
    if (!$VerifyOnly) {
        & java -cp "$project\.build\classes;$project\lib\*" oop_motorph.OOP_MotorPH
    }
    if ($LASTEXITCODE -ne 0) { throw 'MotorPH exited with an error.' }
} finally {
    foreach ($name in $previous.Keys) { [Environment]::SetEnvironmentVariable($name, $previous[$name], 'Process') }
    $credential = $null
}
