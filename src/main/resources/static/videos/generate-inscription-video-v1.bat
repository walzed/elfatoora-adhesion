@echo off
setlocal

cd /d %~dp0

echo === STEP 1: Generate WAV (FR)
powershell -ExecutionPolicy Bypass -File "tts_male.ps1"
if errorlevel 1 exit /b 1

echo === STEP 2: Generate Video MP4 (FFmpeg)
powershell -ExecutionPolicy Bypass -File "video_generate.ps1"
if errorlevel 1 exit /b 1

echo ✅ DONE: inscription-elfatoora.mp4
pause
