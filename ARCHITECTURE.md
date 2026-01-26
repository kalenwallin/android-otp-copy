# Architecture and Implementation Details

## Overview
This document provides detailed information about the architecture and implementation of the OTP Copy Android application.

## Application Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────┐
│                  Android System                  │
│  ┌────────────────────────────────────────────┐ │
│  │         Notification System                 │ │
│  └───────────────┬────────────────────────────┘ │
└──────────────────┼──────────────────────────────┘
                   │ Notification Events
                   ▼
┌─────────────────────────────────────────────────┐
│           OtpNotificationListener               │
│      (NotificationListenerService)              │
│                                                  │
│  • onNotificationPosted()                       │
│  • extractOtp() - Pattern Matching              │
│  • copyToClipboard()                            │
│  • showToast()                                  │
└────────────┬────────────────────────────────────┘
             │
             │ Copies OTP
             ▼
┌─────────────────────────────────────────────────┐
│         Android Clipboard Manager               │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│              MainActivity                        │
│                                                  │
│  • Check notification access permission         │
│  • Display permission status                    │
│  • Open system settings                         │
└─────────────────────────────────────────────────┘
```

## Core Components

### 1. MainActivity (Entry Point)

**Purpose**: Provide user interface for permission management

**Key Responsibilities**:
- Check if notification access is granted
- Display current permission status
- Provide button to open system notification settings
- Update UI when user returns to app

**Key Methods**:
```kotlin
- onCreate(): Initialize UI components
- onResume(): Update permission status when app resumes
- isNotificationServiceEnabled(): Check if permission is granted
- openNotificationSettings(): Launch system settings
- updateNotificationAccessStatus(): Update UI based on permission
```

**User Flow**:
1. User opens app
2. App checks notification permission status
3. If disabled, user clicks "Open Settings"
4. User enables permission in system settings
5. User returns to app (onResume called)
6. Status updates to "Enabled ✓"

### 2. OtpNotificationListener (Core Service)

**Purpose**: Monitor notifications and extract OTP codes

**Key Responsibilities**:
- Listen to all incoming notifications
- Extract text from notifications (title, text, bigText)
- Match text against OTP patterns
- Copy detected OTPs to clipboard
- Show confirmation toast

**Key Methods**:
```kotlin
- onNotificationPosted(sbn): Called when any notification arrives
- extractOtp(text): Extract OTP from notification text
- copyToClipboard(text): Copy OTP to system clipboard
- showToast(message): Display confirmation to user
```

**Lifecycle**:
1. Service starts when permission is granted
2. Runs continuously in background
3. Triggered on each notification
4. Processes notification → Extracts OTP → Copies → Shows toast
5. Returns to idle state

### 3. OTP Pattern Matching System

**Pattern Priority** (Most specific to least specific):

1. **Context-based patterns** (Highest priority)
   - "OTP: 123456"
   - "Your code is 123456"
   - "Use code 123456"
   
2. **Reverse patterns**
   - "123456 is your OTP"

3. **Generic pattern** (Lowest priority, fallback)
   - Any 4-8 digit number

**Why this order?**
- More specific patterns reduce false positives
- Generic pattern catches edge cases
- First match wins, so order matters

## Data Flow

### Notification Processing Pipeline

```
Notification Arrives
    ↓
onNotificationPosted() triggered
    ↓
Extract notification.extras
    ↓
Get title, text, bigText
    ↓
Combine into fullText
    ↓
Call extractOtp(fullText)
    ↓
Try Pattern 1 → No match
    ↓
Try Pattern 2 → No match
    ↓
Try Pattern 3 → Match found! "123456"
    ↓
Validate: length 4-8? ✓ digits only? ✓
    ↓
copyToClipboard("123456")
    ↓
showToast("OTP copied to clipboard: 123456")
    ↓
Done - return to idle
```

## Security & Privacy Design

### Privacy Principles

1. **Local Processing Only**
   - All OTP extraction happens on device
   - No network requests
   - No external dependencies for extraction

2. **No Data Persistence**
   - Notifications are processed in memory
   - No database or file storage
   - No logging of notification content

3. **Minimal Permissions**
   - Only notification access required
   - No internet permission
   - No storage permission

4. **User Transparency**
   - Toast shows what was copied
   - User always knows when OTP is detected
   - Clear permission request flow

### Security Considerations

**What we protect against**:
- ✅ Data leakage (no network, no storage)
- ✅ Excessive permissions (minimal permissions)
- ✅ Background abuse (lightweight, event-driven)

**Limitations**:
- ⚠️ App with notification access has broad read capabilities
- ⚠️ User must trust the app not to misuse access
- ⚠️ OTPs copied to clipboard can be read by other apps

## Performance Characteristics

### Resource Usage

**Memory**:
- Small footprint (~10-20 MB)
- No data caching
- Immediate garbage collection after processing

**CPU**:
- Event-driven (only runs on notification)
- Quick regex matching (<1ms typical)
- Minimal overhead

**Battery**:
- No periodic polling
- No wake locks
- Background service uses minimal power

### Optimization Techniques

1. **Early Returns**
   ```kotlin
   if (sbn == null) return
   if (notification == null) return
   if (text.isEmpty()) return null
   ```

2. **Efficient Pattern Matching**
   - Stop at first match
   - Most specific patterns first
   - Single pass through text

3. **Lazy Initialization**
   - Patterns compiled once (companion object)
   - Views initialized in onCreate

## Error Handling

### Null Safety

All nullable values are safely handled:
```kotlin
val notification = sbn.notification ?: return
val title = extras.getCharSequence("android.title")?.toString() ?: ""
val otp = matcher.group(1) ?: continue
```

### Permission Handling

MainActivity checks permission state:
- Displays current status
- Provides easy access to settings
- Updates on resume

### Edge Cases

1. **Empty notifications**: Handled with isEmpty() checks
2. **Missing extras**: Null coalescing to empty strings
3. **Invalid patterns**: Continue to next pattern
4. **Non-OTP numbers**: Validated with length and digit checks

## Testing Strategy

### Manual Testing

1. **Permission Flow**
   - Install app
   - Check initial state
   - Enable permission
   - Verify status updates

2. **OTP Detection**
   - Send test notifications with various OTP formats
   - Verify clipboard content
   - Check toast messages

3. **Background Operation**
   - Close app
   - Send notification
   - Verify OTP still copied

### Test Notification Commands

```bash
# Test various OTP formats
adb shell "cmd notification post -t 'Bank' 'OTP' 'Your OTP is 123456'"
adb shell "cmd notification post -t 'Service' 'Code' 'OTP: 654321'"
adb shell "cmd notification post -t 'App' 'Verify' 'Use code 789012'"
```

## Build Configuration

### Gradle Structure

**Project-level** (`build.gradle`):
- Plugin versions
- Common configurations

**App-level** (`app/build.gradle`):
- Application ID: `com.otpcopy`
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Dependencies

### Build Variants

- **Debug**: Development builds with debugging enabled
- **Release**: Production builds with ProGuard (if configured)

## Future Enhancement Possibilities

### Potential Features

1. **App Filtering**
   - Allow users to select which apps to monitor
   - Block specific apps from triggering

2. **Pattern Customization**
   - User-defined regex patterns
   - Language-specific patterns

3. **History**
   - Optional OTP history (with encryption)
   - Auto-delete after time period

4. **Advanced Notifications**
   - Persistent notification when service is active
   - Quick actions (disable temporarily)

5. **Analytics**
   - Local statistics (no upload)
   - Detection success rate

### Technical Debt

- Add unit tests for pattern matching
- Add UI tests for MainActivity
- Implement ProGuard rules for release
- Add localization support

## Dependencies

### Core Dependencies

```gradle
androidx.core:core-ktx:1.12.0           // Kotlin extensions
androidx.appcompat:appcompat:1.6.1      // AppCompat support
com.google.android.material:material:1.11.0  // Material Design
androidx.constraintlayout:constraintlayout:2.1.4  // Layout
```

### Why These Dependencies?

- **core-ktx**: Kotlin idiomatic extensions for Android
- **appcompat**: Backward compatibility for older Android versions
- **material**: Modern UI components following Material Design
- **constraintlayout**: Flexible layout manager for complex UIs

## Deployment Checklist

Before releasing to production:

- [ ] Test on multiple Android versions (7.0 - 14)
- [ ] Test on different device manufacturers (Samsung, Google, etc.)
- [ ] Verify battery usage is minimal
- [ ] Check memory leaks with profiler
- [ ] Test with real banking/service notifications
- [ ] Review all permissions
- [ ] Update version code and version name
- [ ] Generate signed APK/AAB
- [ ] Test release build
- [ ] Prepare store listing (if publishing)

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-26
