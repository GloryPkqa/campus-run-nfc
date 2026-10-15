param(
    [string]$AndroidSdk = $(if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } elseif ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }),
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$KeyStorePath = '',
    [string]$KeyAlias = 'app'
)
$ErrorActionPreference = 'Stop'
$projectPath = $PSScriptRoot
$buildPath = Join-Path $projectPath 'build'
$toolsPath = Join-Path $AndroidSdk 'build-tools\35.0.0'
$androidJar = Join-Path $AndroidSdk 'platforms\android-35\android.jar'
if (-not $JavaHome) { $JavaHome = Split-Path (Split-Path (Get-Command javac.exe).Source) }
$javaBin = Join-Path $JavaHome 'bin'
if (-not (Test-Path "$javaBin\javac.exe")) { throw 'Set JAVA_HOME or pass -JavaHome pointing to JDK 17 or newer.' }
if (-not (Test-Path $androidJar)) { throw 'Install Android SDK platform 35 and pass -AndroidSdk.' }
function Check([string]$step) { if ($LASTEXITCODE -ne 0) { throw "$step failed" } }
New-Item -ItemType Directory -Path "$buildPath\classes","$buildPath\dex","$buildPath\tests" -Force | Out-Null
$sources = (Get-ChildItem "$projectPath\src" -Recurse -Filter *.java).FullName
& "$javaBin\javac.exe" -encoding UTF-8 -source 8 -target 8 -bootclasspath $androidJar -d "$buildPath\classes" $sources
Check 'Java compilation'
$validator = (Get-ChildItem "$projectPath\src" -Recurse -Filter LinkValidator.java).FullName
& "$javaBin\javac.exe" -encoding UTF-8 -source 8 -target 8 -d "$buildPath\tests" $validator "$projectPath\tests\LinkValidatorTest.java"
Check 'Test compilation'
& "$javaBin\java.exe" -cp "$buildPath\tests" LinkValidatorTest
Check 'Link validation tests'
$classes = (Get-ChildItem "$buildPath\classes" -Recurse -Filter *.class).FullName
& "$javaBin\java.exe" -cp "$toolsPath\lib\d8.jar" com.android.tools.r8.D8 --min-api 23 --lib $androidJar --output "$buildPath\dex" $classes
Check 'DEX conversion'
& "$toolsPath\aapt.exe" package -f -M "$projectPath\AndroidManifest.xml" -S "$projectPath\res" -I $androidJar -F "$buildPath\unsigned.apk"
Check 'APK packaging'
Copy-Item "$buildPath\dex\classes.dex" "$buildPath\classes.dex"
Push-Location $buildPath
try { & "$toolsPath\aapt.exe" add unsigned.apk classes.dex; Check 'DEX packaging' } finally { Pop-Location }
& "$toolsPath\zipalign.exe" -f 4 "$buildPath\unsigned.apk" "$buildPath\aligned.apk"
Check 'Alignment'
if (-not $KeyStorePath) { $KeyStorePath = Join-Path $buildPath 'local.keystore' }
if (-not $env:NFC_STORE_PASSWORD) { $env:NFC_STORE_PASSWORD = 'android' }
if (-not $env:NFC_KEY_PASSWORD) { $env:NFC_KEY_PASSWORD = $env:NFC_STORE_PASSWORD }
if (-not (Test-Path -LiteralPath $KeyStorePath)) {
    & "$javaBin\keytool.exe" -genkeypair -keystore $KeyStorePath -alias $KeyAlias -keyalg RSA -keysize 2048 -validity 10000 -storepass:env NFC_STORE_PASSWORD -keypass:env NFC_KEY_PASSWORD -dname 'CN=Campus Run NFC, OU=Local Build, O=Pkqa, C=CN'
    Check 'Local signing key generation'
}
$apkPath = Join-Path $buildPath 'campus-run-nfc.apk'
& "$javaBin\java.exe" -jar "$toolsPath\lib\apksigner.jar" sign --ks $KeyStorePath --ks-key-alias $KeyAlias --ks-pass env:NFC_STORE_PASSWORD --key-pass env:NFC_KEY_PASSWORD --v4-signing-enabled false --out $apkPath "$buildPath\aligned.apk"
Check 'Signing'
& "$javaBin\java.exe" -jar "$toolsPath\lib\apksigner.jar" verify --verbose $apkPath
Check 'Signature verification'
Write-Output "APK: $apkPath"
