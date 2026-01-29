# video_generate_v2.ps1
# Génère une vidéo premium (slides) inscription-elfatoora.mp4
# - Background + header
# - 4 slides + outro
# - icones svg
# - transitions fade
# - sous-titres burn-in

$base = $PSScriptRoot
cd $base

$WAV = Join-Path $base "voice_fr.wav"
$SRT = Join-Path $base "subs.srt"
$OUT = Join-Path $base "inscription-elfatoora.mp4"

# Chemins des icons (adapte si tes icons sont ailleurs)
$ICON1 = Join-Path $base "..\..\..\..\..\src\main\resources\static\img\icons\ef-step-1-identity.svg"
$ICON2 = Join-Path $base "..\..\..\..\..\src\main\resources\static\img\icons\ef-step-2-contacts.svg"
$ICON3 = Join-Path $base "..\..\..\..\..\src\main\resources\static\img\icons\ef-step-4-docs.svg"
$ICON4 = Join-Path $base "..\..\..\..\..\src\main\resources\static\img\icons\ef-step-3-signature.svg"
$ICONV = Join-Path $base "..\..\..\..\..\src\main\resources\static\img\icons\ef-validate.svg"

# --- Si tu n'as pas ces chemins, copie/colle tes icons dans /static/videos/icons/ ---
# et remplace ci-dessus par:
# $ICON1 = Join-Path $base "icons\ef-step-1-identity.svg"
# etc.

if (!(Test-Path $WAV)) { Write-Error "❌ Missing $WAV"; exit 1 }
if (!(Test-Path $SRT)) { Write-Error "❌ Missing $SRT"; exit 1 }

# Durée audio (secondes)
$dur = & ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$WAV"
$dur = [math]::Ceiling([double]$dur)

Write-Host "Audio duration: $dur sec"

# --- Montage: 5 scènes ---
# scene 0 (intro)      0-10s
# scene 1 (step1)     10-25s
# scene 2 (step2)     25-40s
# scene 3 (step3)     40-55s
# scene 4 (step4/out) 55-end

# Ajuste automatiquement la scène finale selon durée audio
$intro=10
$step=15
$t1=$intro
$t2=$intro+$step
$t3=$intro+2*$step
$t4=$intro+3*$step
# finale = jusqu'à fin audio
Write-Host "Timeline: intro=$intro step=$step t1=$t1 t2=$t2 t3=$t3 t4=$t4 dur=$dur"

# Styles
$bg="#0b1f44"
$headerBg="white@1.0"
$primary="#0b1f44"
$muted="#3a475a"
$white="#ffffff"
$accent="#1f5bd6"

# FFmpeg command
# Note: on force Roboto si dispo, sinon fallback automatiquement
$vf = @"
color=c=$bg:s=1280x720:d=$dur,
drawbox=x=0:y=0:w=iw:h=110:color=$headerBg:t=fill,
drawtext=font='Roboto':text='EL FATOORA':x=40:y=30:fontsize=46:fontcolor=$primary,
drawtext=font='Roboto':text='Inscription en ligne - Guide rapide':x=40:y=76:fontsize=24:fontcolor=$muted,

# Slide Title (change depending on time)
drawtext=font='Roboto':text='Mon inscription à El Fatoora':x=80:y=170:fontsize=42:fontcolor=$white:enable='between(t,0,$intro)',
drawtext=font='Roboto':text='4 étapes clés à suivre':x=80:y=230:fontsize=28:fontcolor=$white:enable='between(t,0,$intro)',

# Step 1
drawtext=font='Roboto':text='Étape 1 — Identité & entreprise':x=260:y=170:fontsize=36:fontcolor=$white:enable='between(t,$t1,$t2)',
drawtext=font='Roboto':text='Renseignez les informations du demandeur et de l’entreprise.':x=260:y=225:fontsize=24:fontcolor=$white:enable='between(t,$t1,$t2)',

# Step 2
drawtext=font='Roboto':text='Étape 2 — Contacts & accès':x=260:y=170:fontsize=36:fontcolor=$white:enable='between(t,$t2,$t3)',
drawtext=font='Roboto':text='Définissez emails, notifications et accès sécurisé (OTP).':x=260:y=225:fontsize=24:fontcolor=$white:enable='between(t,$t2,$t3)',

# Step 3
drawtext=font='Roboto':text='Étape 3 — Pièces jointes':x=260:y=170:fontsize=36:fontcolor=$white:enable='between(t,$t3,$t4)',
drawtext=font='Roboto':text='Téléversez contrat, déclaration et justificatifs scannés.':x=260:y=225:fontsize=24:fontcolor=$white:enable='between(t,$t3,$t4)',

# Step 4 / Outro
drawtext=font='Roboto':text='Étape 4 — Signature & dépôt':x=260:y=170:fontsize=36:fontcolor=$white:enable='gte(t,$t4)',
drawtext=font='Roboto':text='Signature électronique puis dépôt du dossier pour validation TTN.':x=260:y=225:fontsize=24:fontcolor=$white:enable='gte(t,$t4)',
drawtext=font='Roboto':text='Important : conservez les originaux papier.':x=260:y=275:fontsize=24:fontcolor=$white:enable='gte(t,$t4)',

# Fade transitions
fade=t=in:st=0:d=0.8,
fade=t=out:st=$($intro-0.8):d=0.8:enable='between(t,0,$intro)',

fade=t=in:st=$t1:d=0.6:enable='between(t,$t1,$t2)',
fade=t=in:st=$t2:d=0.6:enable='between(t,$t2,$t3)',
fade=t=in:st=$t3:d=0.6:enable='between(t,$t3,$t4)',
fade=t=in:st=$t4:d=0.6:enable='gte(t,$t4)',

subtitles='$SRT':force_style='FontName=Roboto,FontSize=28,PrimaryColour=&H00FFFFFF,OutlineColour=&H00000000,BorderStyle=1,Outline=2,Shadow=0,MarginV=50'
"@

# On ajoute les icônes via overlay (SVG -> image2 via 'movie')
# NOTE: ffmpeg support SVG selon build. Si problème, convertir SVG en PNG (je te le ferai).
$vfIcons = @"
movie='$ICON1',scale=140:140[ic1];
movie='$ICON2',scale=140:140[ic2];
movie='$ICON3',scale=140:140[ic3];
movie='$ICON4',scale=140:140[ic4];
movie='$ICONV',scale=140:140[icv];

[0:v][ic1]overlay=80:160:enable='between(t,$t1,$t2)'[v1];
[v1][ic2]overlay=80:160:enable='between(t,$t2,$t3)'[v2];
[v2][ic3]overlay=80:160:enable='between(t,$t3,$t4)'[v3];
[v3][ic4]overlay=80:160:enable='gte(t,$t4)'[v4]
"@

& ffmpeg -y `
  -f lavfi -i "color=c=$bg:s=1280x720:d=$dur" `
  -i "$WAV" `
  -filter_complex "$vfIcons" `
  -vf "$vf" `
  -c:v libx264 -pix_fmt yuv420p -r 30 `
  -c:a aac -b:a 192k -shortest `
  "$OUT"

if (!(Test-Path $OUT)) {
  Write-Error "❌ Video not generated"
  exit 1
}

Write-Host "✅ Video generated: $OUT"
exit 0
