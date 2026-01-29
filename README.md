# android-otp-copy

Android app that automatically reads Gmail notifications and copies OTPs (One-Time Passwords) to the clipboard.

## Features

- 🔔 Reads incoming Gmail notifications in real-time
- 🔍 Automatically detects OTP codes using multiple pattern matching algorithms
- 📋 Copies detected OTPs to clipboard instantly
- 🔒 Privacy-focused: No data is stored or transmitted
- ⚡ Works in the background with minimal battery usage

## Supported OTP Patterns

The app recognizes various OTP formats commonly used by services:
- Simple numeric codes (4-8 digits): `123456`
- "OTP: 123456" or "OTP is 123456"
- "Your code is 123456"
- "123456 is your OTP"
- "Use code 123456"

## How to Use

1. Install the app on your Android device (API 24+)
2. Open the app
3. Tap "Open Settings" to grant notification access permission
4. Enable "OTP Copy" in the notification access settings
5. Done! The app will now automatically copy OTPs from Gmail notifications

## Building the App

### Prerequisites
- Android Studio (latest version recommended)
- JDK 8 or higher
- Android SDK with API level 34

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/kalenwallin/android-otp-copy.git
   cd android-otp-copy
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build and run:
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio

5. Or build from command line:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`

## Permissions

The app requires the following permission:
- **Notification Access**: To read notifications and extract OTP codes

## Privacy

This app:
- ✅ Only processes notifications locally on your device
- ✅ Does not store any notification data
- ✅ Does not transmit any data to external servers
- ✅ Only copies OTP codes to clipboard when detected

## Technical Details

### Architecture
- **MainActivity**: Handles permission checking and settings navigation
- **OtpNotificationListener**: NotificationListenerService that monitors notifications
- **OTP Detection**: Uses multiple regex patterns to identify OTP codes

### Minimum Requirements
- Android 7.0 (API 24) or higher
- Notification access permission

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the MIT License.
