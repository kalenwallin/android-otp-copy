# User Guide - OTP Copy Android App

## Overview
OTP Copy is an Android application that automatically detects One-Time Passwords (OTPs) in Gmail notifications and copies them to your clipboard for easy pasting.

## Installation

1. Download and install the APK on your Android device (Android 7.0 or higher)
2. Launch the app from your app drawer

## First Time Setup

### Step 1: Grant Notification Access
When you first open the app, you'll see a screen with the notification access status.

1. Tap the **"Open Settings"** button
2. You'll be taken to the Android system settings for Notification Access
3. Find "OTP Copy" in the list
4. Toggle it **ON**
5. Confirm any security warnings that appear
6. Press back to return to the app

### Step 2: Verify Setup
- The app will now show "Notification Access: Enabled ✓"
- You're all set! The app will now work automatically in the background

## How It Works

Once enabled, the app:

1. **Monitors Gmail Notifications**: Listens to incoming notifications from the Gmail app
2. **Detects OTPs**: Uses smart pattern recognition to identify OTP codes
3. **Copies to Clipboard**: Automatically copies detected OTPs to your clipboard
4. **Shows Confirmation**: Displays a toast message showing the copied OTP

## Supported OTP Formats

The app recognizes various OTP formats:

- ✅ Simple numbers: `Your OTP is 123456`
- ✅ With label: `OTP: 654321`
- ✅ Code format: `Use code 789012`
- ✅ Verification: `Verification code is 345678`
- ✅ Standalone: Just `123456` in the notification

## Privacy & Security

### What the app DOES:
- ✅ Read notifications locally on your device
- ✅ Extract only numeric OTP codes (4-8 digits)
- ✅ Copy OTPs to clipboard
- ✅ Show you what was copied

### What the app DOES NOT do:
- ❌ Store any notification data
- ❌ Send data to external servers
- ❌ Access the internet
- ❌ Read your messages or emails directly
- ❌ Share your OTPs with anyone

## Common Use Cases

### Gmail OTP Emails
When you receive an OTP via email in Gmail, the app will:
1. Detect the OTP in the Gmail notification
2. Copy it to clipboard
3. Show a toast: "OTP copied to clipboard: 123456"
4. You can now paste it into your app or website

### 2FA Authentication via Email
When you receive a 2FA code from Google, Facebook, or other services via email:
1. The Gmail notification appears
2. OTP is automatically copied
3. Switch to the app requesting the code
4. Paste the code

### One-Time Verification via Email
For one-time verification codes sent to your email:
1. Request the code on the website
2. Gmail notification arrives on your phone
3. Code is automatically copied
4. Return to the browser and paste

## Troubleshooting

### The app is not copying OTPs

**Problem**: Notification access is not enabled
- **Solution**: Open the app and tap "Open Settings" to enable notification access

**Problem**: OTP format is not recognized
- **Solution**: The app supports 4-8 digit OTPs. If your OTP has a different format, it may not be detected

**Problem**: App is not running
- **Solution**: The app works in the background. You don't need to keep it open. However, if you've force-stopped it or cleared it from recent apps, it may need to be reopened once.

### Permission keeps getting disabled

Some Android manufacturers (especially Xiaomi, Oppo, Vivo) have aggressive battery optimization:

1. Go to Settings → Apps → OTP Copy
2. Battery → Unrestricted battery usage
3. Permissions → Enable Notification Access (if disabled)
4. Remove from battery optimization
5. Allow autostart (if available)

### OTP is not in my language

The app currently supports English OTP patterns. If you receive OTPs in another language, they may not be detected. However, numeric codes are universal and should work regardless of language.

## Tips for Best Results

1. **Keep the app installed**: Don't uninstall after setup
2. **Don't force stop**: Avoid force-stopping the app in system settings
3. **Check permissions**: Periodically verify notification access is still enabled
4. **Update regularly**: Keep the app updated for improved OTP detection

## Frequently Asked Questions

**Q: Does this work with SMS OTPs?**
A: No, the app only monitors Gmail notifications. OTPs sent via SMS are not detected.

**Q: Will this drain my battery?**
A: No, the app uses very minimal resources. It only activates when a Gmail notification arrives.

**Q: Does this work with other email apps?**
A: No, the app is specifically designed to work only with the Gmail app (com.google.android.gm).

**Q: What happens to the old clipboard content?**
A: When an OTP is copied, it replaces whatever was previously in your clipboard.

**Q: Is this app open source?**
A: Yes! Check out the source code on GitHub.

## Support

If you encounter any issues or have suggestions, please:
- Open an issue on the GitHub repository
- Contact the developer

## Permissions Explained

### Notification Access (Required)
- **Why needed**: To read notification content and extract OTP codes
- **How used**: Only to detect and copy numeric OTP codes
- **Privacy**: All processing happens locally on your device

---

**Version**: 1.0  
**Minimum Android Version**: 7.0 (API 24)  
**Target Android Version**: 14 (API 34)
