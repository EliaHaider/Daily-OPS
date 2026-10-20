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
python3 scripts/make-sounds.py android/app/src/main/res/raw
# dark status/navigation bar
S=android/app/src/main/res/values/styles.xml
sed -i '/<style name="AppTheme.NoActionBar"/a\        <item name="android:statusBarColor">#08090C</item>\n        <item name="android:navigationBarColor">#08090C</item>' "$S"
echo "patched OK"
