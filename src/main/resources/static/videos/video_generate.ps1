# video_generate.ps1
# Génère inscription-elfatoora.mp4 avec voice_fr.wav + sous-titres SRT (Windows-safe)

$base = $PSScriptRoot
Set-Location $base

$WAV = Join-Path $base "voice_fr.wav"
$SRT = Join-Path $base "subs.srt"
$OUT = Join-Path $base "inscription-elfatoora.mp4"

if (!(Test-Path $WAV)) { Write-Error "❌ Missing $WAV"; exit 1 }
if (!(Test-Path $SRT)) { Write-Error "❌ Missing $SRT"; exit 1 }

# Durée audio en secondes (ffprobe)
$dur = & ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$WAV"
$dur = [math]::Ceiling([double]$dur)
Write-Host "Audio duration: $dur sec"

# IMPORTANT: subtitles filter sur Windows
# - Utiliser chemin RELATIF (subs.srt) en se plaçant dans $base
# - Et dans force_style : utiliser les couleurs ASS (format &H00RRGGBB) etc.

# Facultatif: vérifier si Roboto existe côté Windows
# Sinon FFmpeg fallback sur Arial
$fontName = "Roboto"

& ffmpeg -y `
  -f lavfi -i "color=c=#0b1f44:s=1280x720:d=$dur:r=30" `
  -i "$WAV" `
  -vf "drawbox=x=0:y=0:w=iw:h=120:color=white@1.0:t=fill,
       drawtext=font='$fontName':text='EL FATOORA | Inscription':x=40:y=42:fontsize=44:fontcolor=#0b1f44,
       drawtext=font='$fontName':text='Guide rapide':x=40:y=88:fontsize=24:fontcolor=#3a475a,
       subtitles=subs.srt:force_style='FontName=$fontName,FontSize=28,PrimaryColour=&H00FFFFFF,OutlineColour=&H00000000,BorderStyle=1,Outline=2,Shadow=0,MarginV=48'" `
  -c:v libx264 -pix_fmt yuv420p -r 30 `
  -c:a aac -b:a 192k `
  -shortest `
  "$OUT"

if (!(Test-Path $OUT)) {
  Write-Error "❌ Video not generated: $OUT"
  exit 1
}

Write-Host "✅ Video generated: $OUT"
exit 0
