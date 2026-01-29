# video_generate_v3.ps1 (V3.3 - FIXED: filter_complex_script + colors + paths)
# - PNG icons only
# - Roboto-Regular.ttf in same folder
# - Writes drawtext lines into UTF-8 no BOM textfiles
# - Writes filtergraph into _filtergraph.txt and uses -filter_complex_script

$ErrorActionPreference = "Stop"

$base = $PSScriptRoot
Set-Location $base

$WAV = Join-Path $base "voice_fr.wav"
$SRT = Join-Path $base "subs.srt"
$OUT = Join-Path $base "inscription-elfatoora.mp4"
$RobotoTtf = Join-Path $base "Roboto-Regular.ttf"

if (!(Test-Path $WAV)) { throw "Missing WAV: $WAV" }
if (!(Test-Path $SRT)) { throw "Missing SRT: $SRT" }
if (!(Test-Path $RobotoTtf)) { throw "Missing Roboto-Regular.ttf in videos folder: $RobotoTtf" }

# ---- Duration
$durRaw = & ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$WAV"
if ([string]::IsNullOrWhiteSpace($durRaw)) { throw "ffprobe duration empty" }
$dur = [math]::Ceiling([double]$durRaw)
if ($dur -lt 10) { throw "Audio too short ($dur s)." }

Write-Host "Audio duration raw: $durRaw"
Write-Host "Audio duration sec: $dur"

# ---- Icons
$iconsDir = Resolve-Path (Join-Path $base "..\img\icons") -ErrorAction SilentlyContinue
if (-not $iconsDir) { throw "Icons directory not found: $base\..\img\icons" }

function MustExist([string]$p, [string]$label) {
  if (!(Test-Path $p)) { throw ("Missing {0}: {1}" -f $label, $p) }
}

$P1 = Join-Path $iconsDir "ef-step-1-identity.png"
$P2 = Join-Path $iconsDir "ef-step-2-contacts.png"
$P3 = Join-Path $iconsDir "ef-step-4-docs.png"
$P4 = Join-Path $iconsDir "ef-step-3-signature.png"
$PV = Join-Path $iconsDir "ef-validate.png"

MustExist $P1 "Icon step1 PNG"
MustExist $P2 "Icon step2 PNG"
MustExist $P3 "Icon step3 PNG"
MustExist $P4 "Icon step4 PNG"
MustExist $PV "Icon validate PNG"

Write-Host "Icons found:"
Write-Host " - $P1"
Write-Host " - $P2"
Write-Host " - $P3"
Write-Host " - $P4"
Write-Host " - $PV"

# ---- Timeline
$intro = 8
$outro = 6
$available = $dur - ($intro + $outro)
if ($available -lt 24) {
  $intro = 6
  $outro = 4
  $available = $dur - ($intro + $outro)
}
$stepLen = [math]::Floor($available / 4)
if ($stepLen -lt 7) { $stepLen = 7 }

$t1 = $intro
$t2 = $t1 + $stepLen
$t3 = $t2 + $stepLen
$t4 = $t3 + $stepLen
$t5 = $t4 + $stepLen
if ($t5 -gt ($dur - 1)) { $t5 = $dur - 1 }

Write-Host "Timeline: intro=$intro stepLen=$stepLen t1=$t1 t2=$t2 t3=$t3 t4=$t4 t5=$t5 dur=$dur"

# ---- Theme (use 0xRRGGBB for robustness)
$bg      = "0x0b1f44"
$muted   = "0x3a475a"
$white   = "white"
$primary = "0x0b1f44"

# ---- UTF8 no BOM helper
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

# ---- textfile directory
$txtDir = Join-Path $base "_txt"
if (!(Test-Path $txtDir)) { New-Item -ItemType Directory -Path $txtDir | Out-Null }

function WriteTxt([string]$name, [string]$content) {
  $p = Join-Path $txtDir $name
  [System.IO.File]::WriteAllText($p, $content, $utf8NoBom)
  return $p
}

# Text contents
$intro1 = WriteTxt "intro1.txt" "Mon inscription à El Fatoora"
$intro2 = WriteTxt "intro2.txt" "Les étapes clés pour déposer une demande d’adhésion"
$s1a    = WriteTxt "s1a.txt"    "Étape 1 — Identité & entreprise"
$s1b    = WriteTxt "s1b.txt"    "Qui peut s’inscrire : entreprises, fournisseurs, organismes concernés."
$s2a    = WriteTxt "s2a.txt"    "Étape 2 — Contacts & accès"
$s2b    = WriteTxt "s2b.txt"    "Renseignez vos contacts, emails et accès sécurisé (OTP par email)."
$s3a    = WriteTxt "s3a.txt"    "Étape 3 — Documents"
$s3b    = WriteTxt "s3b.txt"    "Préparez : contrat, déclaration d’adhésion, justificatifs, identifiants."
$s4a    = WriteTxt "s4a.txt"    "Étape 4 — Signature & dépôt"
$s4b    = WriteTxt "s4b.txt"    "Quand s’inscrire : avant l’émission des premières factures via El Fatoora."
$out1   = WriteTxt "out1.txt"   "Dépôt terminé"
$out2   = WriteTxt "out2.txt"   "Votre dossier est transmis à TTN pour validation et suivi."
$out3   = WriteTxt "out3.txt"   "Pensez à conserver les originaux."

# ---- Path normalization for ffmpeg filters (forward slashes + escape drive colon)
function FfPath([string]$p) {
  $q = ($p -replace "\\", "/")
  $q = ($q -replace "^([A-Za-z]):/", '$1\:/')  # D:/ -> D\:/
  return $q
}

$RobotoF = FfPath $RobotoTtf
$intro1f = FfPath $intro1
$intro2f = FfPath $intro2
$s1af    = FfPath $s1a
$s1bf    = FfPath $s1b
$s2af    = FfPath $s2a
$s2bf    = FfPath $s2b
$s3af    = FfPath $s3a
$s3bf    = FfPath $s3b
$s4af    = FfPath $s4a
$s4bf    = FfPath $s4b
$out1f   = FfPath $out1
$out2f   = FfPath $out2
$out3f   = FfPath $out3
$SRTF    = FfPath $SRT

# ---- Optional fade alpha helper (0..1)
function AlphaExpr([double]$start, [double]$end, [double]$fade) {
  $s2 = $start + $fade
  $e2 = $end - $fade
  return "if(lt(t,$start),0,if(lt(t,$s2),(t-$start)/$fade,if(lt(t,$e2),1,if(lt(t,$end),($end-t)/$fade,0))))"
}

$fade = 0.6
$aIntro = AlphaExpr 0 $t1 $fade
$a1     = AlphaExpr $t1 $t2 $fade
$a2     = AlphaExpr $t2 $t3 $fade
$a3     = AlphaExpr $t3 $t4 $fade
$a4     = AlphaExpr $t4 $t5 $fade
$aOut   = AlphaExpr $t5 $dur $fade

# ---- Build filtergraph file
$filterFile = Join-Path $base "_filtergraph.txt"

$graph = @"
[0:v]
drawbox=x=0:y=0:w=iw:h=110:color=$white@1.0:t=fill,
drawtext=fontfile='$RobotoF':text='EL FATOORA':x=40:y=30:fontsize=46:fontcolor=$primary,
drawtext=fontfile='$RobotoF':text='Inscription en ligne - Guide rapide':x=40:y=76:fontsize=24:fontcolor=$muted
[base];

[2:v]scale=140:140[ic1];
[3:v]scale=140:140[ic2];
[4:v]scale=140:140[ic3];
[5:v]scale=140:140[ic4];
[6:v]scale=140:140[icv];

[base]
drawtext=fontfile='$RobotoF':textfile='$intro1f':reload=0:x=80:y=180:fontsize=44:fontcolor=$white:alpha='$aIntro',
drawtext=fontfile='$RobotoF':textfile='$intro2f':reload=0:x=80:y=250:fontsize=26:fontcolor=$white:alpha='$aIntro'
[v0];

[v0][ic1]overlay=80:160:enable='between(t,$t1,$t2)'[v1a];
[v1a]
drawtext=fontfile='$RobotoF':textfile='$s1af':reload=0:x=260:y=180:fontsize=38:fontcolor=$white:alpha='$a1',
drawtext=fontfile='$RobotoF':textfile='$s1bf':reload=0:x=260:y=235:fontsize=24:fontcolor=$white:alpha='$a1'
[v1];

[v1][ic2]overlay=80:160:enable='between(t,$t2,$t3)'[v2a];
[v2a]
drawtext=fontfile='$RobotoF':textfile='$s2af':reload=0:x=260:y=180:fontsize=38:fontcolor=$white:alpha='$a2',
drawtext=fontfile='$RobotoF':textfile='$s2bf':reload=0:x=260:y=235:fontsize=24:fontcolor=$white:alpha='$a2'
[v2];

[v2][ic3]overlay=80:160:enable='between(t,$t3,$t4)'[v3a];
[v3a]
drawtext=fontfile='$RobotoF':textfile='$s3af':reload=0:x=260:y=180:fontsize=38:fontcolor=$white:alpha='$a3',
drawtext=fontfile='$RobotoF':textfile='$s3bf':reload=0:x=260:y=235:fontsize=24:fontcolor=$white:alpha='$a3'
[v3];

[v3][ic4]overlay=80:160:enable='between(t,$t4,$t5)'[v4a];
[v4a]
drawtext=fontfile='$RobotoF':textfile='$s4af':reload=0:x=260:y=180:fontsize=38:fontcolor=$white:alpha='$a4',
drawtext=fontfile='$RobotoF':textfile='$s4bf':reload=0:x=260:y=235:fontsize=24:fontcolor=$white:alpha='$a4'
[v4];

[v4][icv]overlay=80:160:enable='between(t,$t5,$dur)'[v5a];
[v5a]
drawtext=fontfile='$RobotoF':textfile='$out1f':reload=0:x=260:y=180:fontsize=40:fontcolor=$white:alpha='$aOut',
drawtext=fontfile='$RobotoF':textfile='$out2f':reload=0:x=260:y=235:fontsize=24:fontcolor=$white:alpha='$aOut',
drawtext=fontfile='$RobotoF':textfile='$out3f':reload=0:x=260:y=280:fontsize=24:fontcolor=$white:alpha='$aOut'
[vfinal];

[vfinal]subtitles='$SRTF':force_style='FontName=Roboto,FontSize=28,PrimaryColour=&H00FFFFFF,OutlineColour=&H00000000,BorderStyle=1,Outline=2,Shadow=0,MarginV=50'[vout]
"@

[System.IO.File]::WriteAllText($filterFile, $graph, $utf8NoBom)
Write-Host "Filtergraph written: $filterFile"

# ---- Run ffmpeg
$colorInput = "color=c=$bg:s=1280x720:d=$dur:r=30"
if (Test-Path $OUT) { Remove-Item $OUT -Force }

# Optional for diagnosis (creates ffmpeg-*.log)
# $env:FFREPORT = "file=ffmpeg-report.log:level=32"

& ffmpeg -v warning -y `
  -f lavfi -i $colorInput `
  -i "$WAV" `
  -loop 1 -i "$P1" `
  -loop 1 -i "$P2" `
  -loop 1 -i "$P3" `
  -loop 1 -i "$P4" `
  -loop 1 -i "$PV" `
  -filter_complex_script "$filterFile" `
  -map "[vout]" -map 1:a `
  -c:v libx264 -pix_fmt yuv420p -r 30 `
  -c:a aac -b:a 192k `
  -shortest `
  "$OUT"

if (!(Test-Path $OUT)) { throw "Video not generated: $OUT" }

Write-Host "Video generated: $OUT"
exit 0
