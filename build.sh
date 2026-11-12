#!/bin/bash
# Compila la APK sin Gradle usando el toolchain local (ecj + build-tools).
set -e
export PATH=/home/appuser/.local/lib/python3.12/site-packages/jdk4py/java-runtime/bin:$PATH

TC=/workspace/tc
JRE=/home/appuser/.local/lib/python3.12/site-packages/jdk4py/java-runtime
BT=$TC/bt/android-14
PLATFORM=$TC/plat/android-34/android.jar
PROJ=/workspace/TorreDelGrimorio
OUT=$PROJ/build
SRC=$PROJ/app/src/main

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

echo "== aapt2: compilar recursos =="
mkdir -p "$OUT/flat"
find "$SRC/res" -type f | while read -r f; do
  "$BT/aapt2" compile -o "$OUT/flat" "$f"
done

echo "== aapt2: link =="
"$BT/aapt2" link \
  -I "$PLATFORM" \
  --manifest "$SRC/AndroidManifest.xml" \
  --java "$OUT/gen" \
  --min-sdk-version 24 \
  --target-sdk-version 34 \
  --version-code 1 \
  --version-name 1.0 \
  -o "$OUT/base.apk" \
  "$OUT/flat"/*

echo "== ecj: compilar java =="
java -jar "$TC/ecj.jar" \
  -source 1.8 -target 1.8 -encoding UTF-8 \
  -nowarn \
  -cp "$PLATFORM:$TC/billing/classes.jar" \
  -d "$OUT/classes" \
  $(find "$SRC/java" "$OUT/gen" -name '*.java')

echo "== d8: dex (incluye billing) =="
java -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 \
  --release \
  --lib "$PLATFORM" \
  --output "$OUT/dex" \
  $(find "$OUT/classes" -name '*.class') \
  "$TC/billing/classes.jar"

echo "== empaquetar dex =="
cd "$OUT/dex"
zip -q "$OUT/base.apk" classes.dex

echo "== zipalign =="
"$BT/zipalign" -f 4 "$OUT/base.apk" "$OUT/aligned.apk"

echo "== firmar =="
if [ ! -f "$PROJ/debug.keystore" ]; then
  "$JRE/bin/keytool" -genkeypair -keystore "$PROJ/debug.keystore" \
    -alias grimorio -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass grimorio -keypass grimorio \
    -dname "CN=Torre del Grimorio, OU=Games, O=Grimorio, C=ES"
fi
java -jar "$BT/lib/apksigner.jar" sign \
  --ks "$PROJ/debug.keystore" \
  --ks-key-alias grimorio \
  --ks-pass pass:grimorio \
  --key-pass pass:grimorio \
  --out "$OUT/TorreDelGrimorio.apk" \
  "$OUT/aligned.apk"

java -jar "$BT/lib/apksigner.jar" verify "$OUT/TorreDelGrimorio.apk"
echo "== OK: $OUT/TorreDelGrimorio.apk =="
ls -la "$OUT/TorreDelGrimorio.apk"
