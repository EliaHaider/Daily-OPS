# Daily Ops — APK banane ka tareeqa (Roman Urdu)

Is folder mein poora app ready hai. Bas APK build karna baqi hai. Yeh kaam GitHub par free mein ho jata hai
(Android Studio install karne ki zaroorat nahi). Computer/laptop par karna sabse aasan hai.

## APK banane ke steps

1. https://github.com par free account banao (agar nahi hai).
2. Upar "+" > **New repository** > naam `daily-ops` > **Create repository**.
3. "uploading an existing file" par click karo, is folder ki **saari files aur folders** drag-drop kar do
   (`www`, `assets`, `android-overlay`, `scripts`, `.github`, `package.json`, `capacitor.config.json`, `.gitignore`).
   Dhyan rahe `.github` folder bhi upload ho (hidden folder hota hai). Phir **Commit changes**.
4. Repository mein **Actions** tab kholo > "Build APK" > **Run workflow**.
   (Pehli baar "I understand my workflows, enable them" aaye to enable kar do.)
5. 4-6 minute baad green tick aayega. Us run par click karo, neeche **Artifacts** mein `DailyOps-APK` download karo.
6. Zip kholo, andar `app-debug.apk` milegi. Phone mein bhej kar install karo
   ("Install unknown apps" allow karna padega).

## Install ke baad (zaroori, warna alarm late/band ho sakte hain)

1. App kholo, **Notification** aur **Alarms & reminders** ki permission "Allow" karo.
2. Phone Settings > Apps > Daily Ops > Battery > **Unrestricted / No restrictions**.
3. Xiaomi / Oppo / Vivo / Infinix / Tecno phones par "Autostart" bhi **on** karo.
4. App ki Settings mein **Test alarm (5 sec)** dabao aur screen lock kar ke check karo.

## Naya time ya naya feature

`www/index.html` mein badlao karo, GitHub par dobara upload karo, workflow dobara chalao — naya APK ban jayega.

## Update (v4)

- Alarm/notification sounds ab build ke waqt khud ban jati hain (`scripts/make-sounds.py`), purane silent channels hata diye gaye.
- Pani ke reminder ab jagne ke time se fixed slots par aate hain (app kholne se reset nahi hote), 7 din aage tak.
- Naya **Exercise** tab: hafte ke 7 din ka split, set tracker, rest timer, streak, aur roz ka reminder.
- Naya APK banane ke baad purani app ke upar install kar do; data bacha rahega.

## Update (v5)

- Pani tab (graph, pattern, suggestion), naya Kaam section (aadat / zaroori / normal, har ek par reminder), Settings mein Awaaz (app / phone default / apni mp3).
- Apni awaaz `assets/sounds/` mein rakho (naam README.txt mein).

## Update (v6)

- Alarm/reminder ab native Android alarm se chalte hain (app band hone par bhi), alarm 30 sec tak bajta hai aur volume/power button se band hota hai.
- Logo transparent version se bana hai.

## Update (v7)

- `signing/debug.keystore`: har build isi fixed key se sign hota hai, isliye naya APK purani app ke upar install hota hai aur data bacha rehta hai. Is file ko kabhi mat badlo/delete karo.
- Data apne aap `Documents/DailyOps/dailyops-backup.json` mein save hota hai; naye install par khud restore hone ki koshish karta hai.
- Kaam aur aadatein default khali hain.
