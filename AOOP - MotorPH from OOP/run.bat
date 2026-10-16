@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "..\scripts\build.ps1" -Run
if errorlevel 1 exit /b 1
pause
