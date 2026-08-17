# KometTV APK

Minimal Android WebView app for BMK Komet's digital signage.  
Opens `https://tv.bmkkomet.se/display/main` in full-screen kiosk mode.

## Features

- Full-screen immersive mode (no status bar, no navigation bar)
- JavaScript and HTML5 video support
- Keep screen awake (never sleeps)
- Auto-retry on network failure (every 5 seconds)
- "Reconnecting..." overlay when offline
- Back button blocked (kiosk mode)
- Remote-control compatible (D-pad navigation)
- Periodic page reload every 6 hours (memory safety)
- TV Leanback launcher support

## Requirements

- Android Studio (Arctic Fox or later)
- JDK 11+
- Android SDK 34

## Build

### Option 1: Android Studio
1. Open this folder in Android Studio
2. Wait for Gradle sync
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Command Line
```bash
# Make gradlew executable (generate it from Android Studio or use gradle wrapper)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Install on Thomson Go Cast 152

### Via ADB (recommended)
```bash
# Connect dongle to same network, enable developer mode on the TV
# Find the TV's IP in Settings → Network

# Connect via ADB
adb connect <TV_IP>:5555

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n se.bmkkomet.tv/.MainActivity
```

### Via USB
1. Copy APK to a USB drive
2. Insert into Thomson dongle
3. Use a file manager app to open and install the APK
4. Enable "Install from unknown sources" when prompted

## After Installation

- Launch "Komet TV" from the TV's app list
- The app will open `https://tv.bmkkomet.se/display/main` full-screen
- If the network drops, it shows "Reconnecting..." and retries automatically
- The back button is disabled — use Home to exit

## Auto-start on Boot (optional, device-dependent)

For the Thomson Go Cast 152, you can try:
1. Install a "Boot Manager" or "AutoStart" app from Play Store
2. Or use ADB to set KometTV as a launcher (advanced)

This is device-specific and should be tested on the actual hardware.

## Project Structure

```
komet-tv-apk/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/se/bmkkomet/tv/
│       │   └── MainActivity.java
│       └── res/
│           ├── drawable/
│           ├── mipmap-*/
│           └── values/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradle/wrapper/
```
