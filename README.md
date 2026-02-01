# android-otp-copy

Android app that automatically reads notifications and copies OTPs (One-Time Passwords) to the clipboard.

## Features

- 🔔 Reads incoming notifications in real-time
- 🔍 Automatically detects OTP codes using a simple pattern matching algorithm (4-8 digits): `123456`
- 📋 Copies detected OTPs to clipboard instantly
- 🔒 Privacy-focused: No data is stored or transmitted
- ⚡ Works in the background with minimal battery usage

## How to Use

1. Install the app on your Android device (API 24+)
2. Open the app
3. Tap "Open Settings" to grant notification access permission
4. Enable "OTP Copy" in the notification access settings
5. Done! The app will now automatically copy OTPs from Gmail notifications

## Building the App

### Prerequisites
- JDK 8 or higher
- Android SDK with API level 34

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/kalenwallin/android-otp-copy.git
   cd android-otp-copy
   ```

2. Build from command line:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`

## Permissions

The app requires the following permission:
- **Notification Access**: To read notifications and extract OTP codes

## Technical Details

### Architecture
- **MainActivity**: Handles permission checking and settings navigation
- **OtpNotificationListener**: NotificationListenerService that monitors notifications
- **OTP Detection**: Uses regex patterns to identify OTP codes

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the MIT License.
