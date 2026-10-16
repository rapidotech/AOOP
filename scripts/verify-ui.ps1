param([string]$OutputDirectory = (Join-Path $env:TEMP 'aoop-ui-verification'))
$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1')
$repositoryRoot = Split-Path $PSScriptRoot -Parent
$projectRoot = Join-Path $repositoryRoot 'AOOP - MotorPH from OOP'
$classes = Join-Path $projectRoot '.build/classes'
$visualClasses = Join-Path $projectRoot '.build/visual'
New-Item -ItemType Directory -Force -Path $visualClasses | Out-Null
$classpath = "$classes;$(Join-Path $projectRoot 'lib/*')"
& javac --release 17 -encoding UTF-8 -cp $classpath -d $visualClasses (Join-Path $repositoryRoot 'tools/VisualSmoke.java') (Join-Path $projectRoot 'test/oop_motorph/UiAuthFixture.java')
if ($LASTEXITCODE -ne 0) { throw 'Visual fixture compilation failed.' }
$fixtureClasspath = "$visualClasses;$classes;$(Join-Path $projectRoot 'lib/jcalendar-1.4.jar');$(Join-Path $projectRoot 'lib/bcrypt-0.10.2.jar');$(Join-Path $projectRoot 'lib/bytes-1.5.0.jar')"
& java -cp $fixtureClasspath oop_motorph.VisualSmoke $OutputDirectory
if ($LASTEXITCODE -ne 0) { throw 'Visual fixture failed.' }
Write-Output "Synthetic UI captures: $OutputDirectory"
