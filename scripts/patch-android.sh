#!/usr/bin/env bash
# Android project ko alarm ke liye tayyar karta hai (permissions, sounds, theme)
set -e
M=android/app/src/main/AndroidManifest.xml
for p in POST_NOTIFICATIONS SCHEDULE_EXACT_ALARM USE_EXACT_ALARM VIBRATE RECEIVE_BOOT_COMPLETED WAKE_LOCK; do
  if ! grep -q "android.permission.$p" "$M"; then
    sed -i "s#<application#<uses-permission android:name=\"android.permission.$p\" />\n    <application#" "$M"
  fi
done
# portrait only
sed -i 's#<activity#<activity android:screenOrientation="portrait"#' "$M"
# sounds + notification icon
cp -r android-overlay/app/src/main/res/* android/app/src/main/res/
RAW=android/app/src/main/res/raw; mkdir -p $RAW
python3 scripts/make-sounds.py /tmp/gen
for f in /tmp/gen/*.wav; do b=$(basename "$f" .wav); if ! ls assets/sounds/"$b".* >/dev/null 2>&1; then cp "$f" $RAW/; fi; done
if [ -d assets/sounds ]; then for f in assets/sounds/*; do case "$f" in *.mp3|*.wav|*.ogg|*.MP3|*.WAV|*.OGG) cp "$f" "$RAW/$(basename "$f" | tr 'A-Z-' 'a-z_')";; esac; done; fi
mkdir -p android/app/src/main/java; cp -r android-overlay/app/src/main/java/* android/app/src/main/java/
# dark status/navigation bar
S=android/app/src/main/res/values/styles.xml
sed -i '/<style name="AppTheme.NoActionBar"/a\        <item name="android:statusBarColor">#08090C</item>\n        <item name="android:navigationBarColor">#08090C</item>' "$S"
echo "patched OK"
