#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
PROJECT_DIR="$PWD"
BUILD_DIR="$PROJECT_DIR/build"
SDK_DIR="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$SDK_DIR" ]]; then echo 'Set ANDROID_SDK_ROOT or ANDROID_HOME.' >&2; exit 1; fi
TOOLS="$SDK_DIR/build-tools/35.0.0"
ANDROID_JAR="$SDK_DIR/platforms/android-35/android.jar"
if [[ ! -f "$ANDROID_JAR" || ! -x "$TOOLS/aapt" ]]; then echo 'Install Android SDK platform 35 and build-tools 35.0.0.' >&2; exit 1; fi
mkdir -p "$BUILD_DIR/classes" "$BUILD_DIR/dex" "$BUILD_DIR/tests"
mapfile -t JAVA_SOURCES < <(find src -name '*.java' -type f | sort)
javac -encoding UTF-8 -source 8 -target 8 -bootclasspath "$ANDROID_JAR" -d "$BUILD_DIR/classes" "${JAVA_SOURCES[@]}"
VALIDATOR=$(find src -name LinkValidator.java -type f)
javac -encoding UTF-8 -source 8 -target 8 -d "$BUILD_DIR/tests" "$VALIDATOR" tests/LinkValidatorTest.java
java -cp "$BUILD_DIR/tests" LinkValidatorTest
mapfile -t CLASS_FILES < <(find "$BUILD_DIR/classes" -name '*.class' -type f | sort)
java -cp "$TOOLS/lib/d8.jar" com.android.tools.r8.D8 --min-api 23 --lib "$ANDROID_JAR" --output "$BUILD_DIR/dex" "${CLASS_FILES[@]}"
"$TOOLS/aapt" package -f -M AndroidManifest.xml -S res -I "$ANDROID_JAR" -F "$BUILD_DIR/unsigned.apk"
cp "$BUILD_DIR/dex/classes.dex" "$BUILD_DIR/classes.dex"
(cd "$BUILD_DIR" && "$TOOLS/aapt" add unsigned.apk classes.dex)
"$TOOLS/zipalign" -f 4 "$BUILD_DIR/unsigned.apk" "$BUILD_DIR/aligned.apk"
SIGNING_KEY="${NFC_KEYSTORE:-$BUILD_DIR/local.keystore}"
SIGNING_ALIAS="${NFC_KEY_ALIAS:-app}"
export NFC_STORE_PASSWORD="${NFC_STORE_PASSWORD:-android}"
export NFC_KEY_PASSWORD="${NFC_KEY_PASSWORD:-$NFC_STORE_PASSWORD}"
if [[ ! -f "$SIGNING_KEY" ]]; then
  keytool -genkeypair -keystore "$SIGNING_KEY" -alias "$SIGNING_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 -storepass:env NFC_STORE_PASSWORD -keypass:env NFC_KEY_PASSWORD -dname 'CN=Campus Run NFC, OU=Local Build, O=Pkqa, C=CN'
fi
java -jar "$TOOLS/lib/apksigner.jar" sign --ks "$SIGNING_KEY" --ks-key-alias "$SIGNING_ALIAS" --ks-pass env:NFC_STORE_PASSWORD --key-pass env:NFC_KEY_PASSWORD --v4-signing-enabled false --out "$BUILD_DIR/campus-run-nfc.apk" "$BUILD_DIR/aligned.apk"
java -jar "$TOOLS/lib/apksigner.jar" verify --verbose "$BUILD_DIR/campus-run-nfc.apk"
echo "APK: $BUILD_DIR/campus-run-nfc.apk"
