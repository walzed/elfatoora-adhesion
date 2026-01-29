@echo off
setlocal

cd /d %~dp0

echo === STEP 1: WAV (FR)
powershell -ExecutionPolicy Bypass -File "tts_male.ps1"
if errorlevel 1 exit /b 1

echo === STEP 2: Video V2 Premium
powershell -ExecutionPolicy Bypass -File "video_generate_v2.ps1"
if errorlevel 1 exit /b 1

echo ✅ DONE: inscription-elfatoora.mp4
pause
