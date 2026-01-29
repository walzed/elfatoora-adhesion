# tts_male.ps1
# Génère voice_fr.wav à partir de script_voix_off.txt (dans le même dossier que ce .ps1)
# Priorité: voix FR masculine si dispo -> sinon voix FR (Hortense) -> sinon première voix installée.

Add-Type -AssemblyName System.Speech

$base = $PSScriptRoot
$scriptPath = Join-Path $base "script_voix_off.txt"
$out = Join-Path $base "voice_fr.wav"

Write-Host "=== TTS base dir: $base"
Write-Host "Input text:  $scriptPath"
Write-Host "Output wav:  $out"

if (!(Test-Path $scriptPath)) {
  Write-Error "❌ script_voix_off.txt introuvable: $scriptPath"
  exit 1
}

# Lire le texte (RAW pour conserver les retours à la ligne)
$text = Get-Content -Path $scriptPath -Raw -Encoding UTF8
if ([string]::IsNullOrWhiteSpace($text)) {
  Write-Error "❌ Le fichier script_voix_off.txt est vide."
  exit 1
}

$sp = New-Object System.Speech.Synthesis.SpeechSynthesizer

Write-Host "Voices installed:"
$voices = $sp.GetInstalledVoices()
$voices | ForEach-Object {
  $vi = $_.VoiceInfo
  Write-Host (" - " + $vi.Name + " | " + $vi.Culture + " | " + $vi.Gender)
}

# --- Sélection de la voix ---
# 1) Voix FR masculine si disponible
$frMale = $voices |
  Where-Object { $_.VoiceInfo.Culture.Name -like "fr-*" -and $_.VoiceInfo.Gender.ToString() -eq "Male" } |
  Select-Object -First 1

# 2) Sinon voix FR (Hortense, etc.)
$frAny = $voices |
  Where-Object { $_.VoiceInfo.Culture.Name -like "fr-*" } |
  Select-Object -First 1

# 3) Sinon première voix
$any = $voices | Select-Object -First 1

$selected = $null
if ($frMale) {
  $selected = $frMale.VoiceInfo.Name
} elseif ($frAny) {
  $selected = $frAny.VoiceInfo.Name
} elseif ($any) {
  $selected = $any.VoiceInfo.Name
}

if ([string]::IsNullOrWhiteSpace($selected)) {
  Write-Error "❌ Aucune voix installée détectée."
  exit 1
}

$sp.SelectVoice($selected)
Write-Host "✅ Selected voice: $selected"

# Réglages
$sp.Rate = 0   # -10..10
$sp.Volume = 100

# Supprimer ancien wav si présent
if (Test-Path $out) { Remove-Item $out -Force }

# Générer wav
$sp.SetOutputToWaveFile($out)
$sp.Speak($text)
$sp.SetOutputToDefaultAudioDevice()
$sp.Dispose()

if (!(Test-Path $out)) {
  Write-Error "❌ WAV non généré: $out"
  exit 1
}

Write-Host "✅ Generated wav: $out"
exit 0
