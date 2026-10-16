# تطبيق Lights (أندرويد) — خطوات البناء بـ Android Studio

## الخطوة 1: فك الضغط في مسار إنجليزي
1. فك ملف `LightsApp.zip` في `C:\Lights` (يعني يبقى عندك `C:\Lights\LightsApp`).
2. **مهم:** لازم المسار يكون بحروف إنجليزية بس. متفكهوش في المستندات أو OneDrive.

## الخطوة 2: افتح المشروع
1. افتح Android Studio ← **Open** (أو File ← Open).
2. اختار المجلد `C:\Lights\LightsApp` واضغط OK.
3. لو سأل Trust Project اضغط **Trust**.
4. استنى. أول مرة بتاخد من 5 لـ 15 دقيقة، لأنه بينزّل Gradle والمكتبات. شوف الشريط اللي تحت لحد ما يخلص كلمة "Gradle sync".

### لو ظهرت رسالة أثناء الـ Sync
- **"Install missing SDK / Android SDK Platform 34"** ← اضغط على الرابط الأزرق (Install) ووافق على الرخصة (Accept) واستنى.
- **"Upgrade Android Gradle Plugin"** ← اضغط **Upgrade** (آمن).
- **خطأ في JDK** ← File ← Settings ← Build, Execution, Deployment ← Build Tools ← Gradle ← **Gradle JDK** اختار `jbr-17` أو `Embedded JDK`، وبعدها Sync Project (أيقونة الفيل فوق).

## الخطوة 3: ابني الـ APK
1. من القائمة فوق: **Build ← Build Bundle(s) / APK(s) ← Build APK(s)**.
2. استنى لحد ما تظهر رسالة تحت على اليمين: "APK(s) generated successfully". اضغط **locate**.
3. الملف هيبقى اسمه `app-debug.apk` في:
   `C:\Lights\LightsApp\app\build\outputs\apk\debug\`

## الخطوة 4: ثبّته على الموبايل
1. انقل `app-debug.apk` للموبايل (كابل USB، أو Google Drive، أو واتساب لنفسك).
2. افتح الملف من الموبايل. لو طلب، اسمح بـ **Install unknown apps** للتطبيق اللي فتحت منه.
3. لو ظهرت رسالة Google Play Protect اضغط **Install anyway**.

## الخطوة 5: أول تشغيل
1. وافق على **الإشعارات** (Allow) لما يسأل.
2. اضغط أيقونة **الترس ⚙** فوق على اليمين.
3. تأكد إن عنوان الجهاز `192.168.1.37` (أو الرقم الجديد لو اتغيّر)، اضغط **Test connection** المفروض يكتب Connected، وبعدها **Save**.
4. المفروض يظهر "Live" بنقطة خضراء ويتحكم في النورين.

## الخطوة 6: اختبر المنبه على الموبايل
1. من الإعدادات اضغط **Ring this phone in 10 seconds**، اقفل الشاشة، وانتظر. المفروض الموبايل يرن بشاشة كبيرة فيها Snooze و Dismiss.
2. اضغط على منبه من المنبهات (Timers)، فعّل **Also ring this phone**، اختار وقت بعد دقيقتين، واضغط Save. المفروض الأنوار تنور (من اللوحة) والموبايل يرن في نفس الوقت.
3. اعمل Restart للموبايل وتأكد إن المنبه لسه شغال.

## لو المنبه مرنّش
- ادخل **Battery settings** من الإعدادات واسمح للتطبيق بالعمل في الخلفية (Unrestricted / No restrictions).
- في شاومي وهواوي وسامسونج: فعّل Autostart واسمح بالتشغيل في الخلفية للتطبيق.
- في **Full-screen alarm settings** اسمح بالشاشة الكاملة.
- تأكد إن صوت **المنبه** (Alarm volume) مش على صفر.

## ملحوظات
- التطبيق بيتحكم لما الموبايل على **نفس واي فاي البيت** بس.
- منبه الأنوار نفسه بيشتغل جوه اللوحة، حتى لو الموبايل مقفول.
- رنة الموبايل بتشتغل حتى لو الواي فاي واقع.
- لو غيّرت الـ IP بتاع اللوحة، غيّره من الترس ⚙.
