@echo off
setlocal enabledelayedexpansion

REM =====================================================
REM generate-inscription-video-v3.bat (V3.3)
REM - Generate video using video_generate_v3.ps1
REM - Windows safe (ASCII only)
REM =====================================================

set "BASE=%~dp0"
cd /d "%BASE%"

echo === Working dir: %CD%

where ffmpeg >nul 2>&1
if errorlevel 1 (
  echo ERROR: ffmpeg not found in PATH
  pause
  exit /b 1
)

where ffprobe >nul 2>&1
if errorlevel 1 (
  echo ERROR: ffprobe not found in PATH
  pause
  exit /b 1
)

if not exist "voice_fr.wav" (
  echo ERROR: voice_fr.wav not found in %CD%
  pause
  exit /b 1
)

if not exist "subs.srt" (
  echo ERROR: subs.srt not found in %CD%
  pause
  exit /b 1
)

if not exist "Roboto-Regular.ttf" (
  echo ERROR: Roboto-Regular.ttf not found in %CD%
  pause
  exit /b 1
)

if not exist "video_generate_v3.ps1" (
  echo ERROR: video_generate_v3.ps1 not found in %CD%
  pause
  exit /b 1
)

echo === STEP: Generate Video MP4 (V3.3)
powershell -NoProfile -ExecutionPolicy Bypass -File "%CD%\video_generate_v3.ps1"
if errorlevel 1 (
  echo.
  echo ERROR: Video generation failed (see logs above)
  pause
  exit /b 1
)

echo.
echo DONE: inscription-elfatoora.mp4 generated in:
echo   %CD%\inscription-elfatoora.mp4
pause
exit /b 0
