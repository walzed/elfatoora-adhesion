@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM -------------------------------------------------------
REM Go to folder where this .bat is located
REM -------------------------------------------------------
cd /d "%~dp0"

REM Files in this folder:
set "VIDEOS_DIR=%cd%"
set "PS1=%VIDEOS_DIR%\tts_male.ps1"
set "SCRIPT_TXT=%VIDEOS_DIR%\script_voix_off.txt"
set "WAV=%VIDEOS_DIR%\voice_fr_male.wav"
set "SRT=%VIDEOS_DIR%\inscription-elfatoora.srt"
set "OUT=%VIDEOS_DIR%\inscription-elfatoora.mp4"

echo === Working dir: %VIDEOS_DIR%

REM -------------------------------------------------------
REM 1) Generate male voice wav (Windows TTS)
REM -------------------------------------------------------
if not exist "%PS1%" (
  echo ❌ ERROR: %PS1% not found
  pause
  exit /b 1
)

powershell -ExecutionPolicy Bypass -File "%PS1%"
if errorlevel 1 (
  echo ❌ ERROR: TTS generation failed
  pause
  exit /b 1
)

if not exist "%WAV%" (
  echo ❌ ERROR: %WAV% not generated
  pause
  exit /b 1
)

REM -------------------------------------------------------
REM 2) Ensure ffmpeg exists
REM -------------------------------------------------------
where ffmpeg >nul 2>&1
if errorlevel 1 (
  echo ❌ ERROR: ffmpeg not found in PATH.
  echo    Install it, or add it to PATH. (See steps below)
  pause
  exit /b 1
)

REM -------------------------------------------------------
REM 3) Build video (use filter_complex from a .txt file to avoid BAT escaping issues)
REM -------------------------------------------------------
set "FILTERTXT=%VIDEOS_DIR%\ffmpeg_filter.txt"

REM IMPORTANT: no & in this file to avoid cmd splitting
(
  echo drawtext=font='Arial':fontsize=48:fontcolor=#27306d:x=90:y=110:text='Inscription El Fatoora':enable='between(t,0,7.8)',
  echo drawtext=font='Arial':fontsize=30:fontcolor=#1b2430:x=90:y=190:text='Etapes cles pour demarrer':enable='between(t,0,7.8)',
  echo drawtext=font='Arial':fontsize=40:fontcolor=#27306d:x=90:y=120:text='Etape 1':enable='between(t,7.8,16)',
  echo drawtext=font='Arial':fontsize=28:fontcolor=#1b2430:x=90:y=190:text='Identite et entreprise':enable='between(t,7.8,16)',
  echo drawtext=font='Arial':fontsize=24:fontcolor=#3a475a:x=90:y=240:text='Renseignez les informations de votre organisation.':enable='between(t,7.8,16)',
  echo drawtext=font='Arial':fontsize=40:fontcolor=#27306d:x=90:y=120:text='Etape 2':enable='between(t,16,24.5)',
  echo drawtext=font='Arial':fontsize=28:fontcolor=#1b2430:x=90:y=190:text='Contacts et acces':enable='between(t,16,24.5)',
  echo drawtext=font='Arial':fontsize=24:fontcolor=#3a475a:x=90:y=240:text='Emails, notifications, securisation par OTP.':enable='between(t,16,24.5)',
  echo drawtext=font='Arial':fontsize=40:fontcolor=#27306d:x=90:y=120:text='Etape 3':enable='between(t,24.5,34.5)',
  echo drawtext=font='Arial':fontsize=28:fontcolor=#1b2430:x=90:y=190:text='Pieces jointes':enable='between(t,24.5,34.5)',
  echo drawtext=font='Arial':fontsize=24:fontcolor=#3a475a:x=90:y=240:text='Contrat, declaration d adhesion, justificatifs.':enable='between(t,24.5,34.5)',
  echo drawtext=font='Arial':fontsize=40:fontcolor=#27306d:x=90:y=120:text='Etape 4':enable='between(t,34.5,45.5)',
  echo drawtext=font='Arial':fontsize=28:fontcolor=#1b2430:x=90:y=190:text='Signature et engagement':enable='between(t,34.5,45.5)',
  echo drawtext=font='Arial':fontsize=24:fontcolor=#3a475a:x=90:y=240:text='Soumission du dossier a Tunisie TradeNet.':enable='between(t,34.5,45.5)',
  echo drawtext=font='Arial':fontsize=42:fontcolor=#27306d:x=90:y=120:text='Pret a demarrer ?':enable='between(t,45.5,52)',
  echo drawtext=font='Arial':fontsize=26:fontcolor=#1b2430:x=90:y=190:text='Commencez votre demande d adhesion sur El Fatoora.':enable='between(t,45.5,52)',
  echo subtitles='%SRT%':force_style='FontName=Arial,FontSize=28,PrimaryColour=FFFFFF,OutlineColour=000000,BorderStyle=3,Outline=1,Shadow=0,BackColour=66000000,MarginV=50'
) > "%FILTERTXT%"

REM Create the mp4
ffmpeg -y ^
  -f lavfi -i color=c=white:s=1280x720:d=52 ^
  -i "%WAV%" ^
  -vf "script=%FILTERTXT%" ^
  -c:v libx264 -pix_fmt yuv420p -r 25 ^
  -c:a aac -b:a 192k ^
  -shortest "%OUT%"

if errorlevel 1 (
  echo ❌ ERROR: ffmpeg failed
  pause
  exit /b 1
)

echo.
echo ✅ Video generated: %OUT%
pause
endlocal
