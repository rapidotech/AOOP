param(
    [switch]$Run,
    [switch]$Test
)
$ErrorActionPreference = 'Stop'
$projectRoot = Join-Path (Split-Path $PSScriptRoot -Parent) 'AOOP - MotorPH from OOP'
$outputRoot = Join-Path $projectRoot '.build'
$classes = Join-Path $outputRoot 'classes'
$javac = (Get-Command javac -ErrorAction Stop).Source
$java = (Get-Command java -ErrorAction Stop).Source
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src/oop_motorph') -Filter '*.java' | ForEach-Object FullName)
& $javac --release 17 -encoding UTF-8 -d $classes -cp (Join-Path $projectRoot 'lib/*') $sourceFiles
if ($LASTEXITCODE -ne 0) { throw "Compilation failed; application was not launched." }
foreach ($resourceDirectory in @('img', 'resources')) {
    Copy-Item -LiteralPath (Join-Path $projectRoot "src/oop_motorph/$resourceDirectory") -Destination (Join-Path $classes 'oop_motorph') -Recurse -Force
}
$classpath = "$classes;$(Join-Path $projectRoot 'lib/*')"
Write-Output "Build passed (Java 17 target): $classes"
if ($Test) {
    $testSources = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'test') -Filter '*.java' -Recurse | ForEach-Object FullName)
    $testFiles = @($testSources | Where-Object { [System.IO.Path]::GetFileName($_) -like '*Test.java' })
    if ($testFiles.Count -eq 0) { throw 'No regression tests found.' }
    $testClasses = Join-Path $outputRoot 'test-classes'
    New-Item -ItemType Directory -Force -Path $testClasses | Out-Null
    & $javac --release 17 -encoding UTF-8 -d $testClasses -cp $classpath $testSources
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
    # Keep the real MySQL driver off the regression runtime classpath.
    # Persistence tests register their own JDBC double; UI tests inject auth.
    $testRuntime = "$testClasses;$classes;$(Join-Path $projectRoot 'lib/jcalendar-1.4.jar');$(Join-Path $projectRoot 'lib/bcrypt-0.10.2.jar');$(Join-Path $projectRoot 'lib/bytes-1.5.0.jar')"
    foreach ($testFile in $testFiles) {
        $testName = [System.IO.Path]::GetFileNameWithoutExtension($testFile)
        & $java -ea -cp $testRuntime "oop_motorph.$testName"
        if ($LASTEXITCODE -ne 0) { throw "Regression test failed: $testName" }
    }
}
if ($Run) {
    $secureProfile = Join-Path $env:USERPROFILE '.motorph/milestone2/profile.json'
    if (Test-Path -LiteralPath $secureProfile) {
        & (Join-Path $PSScriptRoot 'start-milestone2.ps1') -SkipBuild
    } else {
        & $java -cp $classpath oop_motorph.OOP_MotorPH
    }
    if ($LASTEXITCODE -ne 0) { throw 'Application exited with an error.' }
}
