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

### Important: For Google Message Notifications

To ensure the app can read Google message notifications correctly, you need to disable a specific Android setting:

**Path**: Phone settings → Notifications → Advanced settings → Suggest actions and replies for notifications

**Action**: Toggle this setting **OFF**

Without disabling this setting, Google's smart reply feature may interfere with the app's ability to read notification content.

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

## Development

The `debug.sh` script provides convenient commands for development and debugging:

```bash
./debug.sh [command]
```

### Available Commands

| Command | Description |
|---------|-------------|
| `build` | Build and install debug APK to connected device |
| `run` | Build, install, launch the app, and show logs |
| `logs` | Show live app logs (filtered to OTPCopy) |
| `logcat` | Show full system logcat |
| `crash` | Show recent crash logs |
| `wireless` | Set up wireless debugging |
| `devices` | List connected devices |
| `clear` | Clear app data |
| `uninstall` | Uninstall the app |

### Examples

```bash
# Full development cycle - build, install, run, and watch logs
./debug.sh run

# Just watch app logs (while app is running)
./debug.sh logs

# Set up wireless debugging (connect via USB first)
./debug.sh wireless

# Check for crash logs
./debug.sh crash
```

### Prerequisites for Debugging

1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging** in Developer Options
3. Connect via USB cable (or use `./debug.sh wireless` for wireless debugging)

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
