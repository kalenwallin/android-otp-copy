# Specification: Convex Integration for OTP Copy

**Issue**: android-otp-copy-088  
**Type**: Feature  
**Priority**: P1  
**Created**: 2026-02-03

## Overview

This specification outlines the integration of Convex as a backend to persist OTP codes captured by the Android OTP Copy application. When an OTP is copied to the clipboard, it will also be saved to a Convex `otpItem` table.

## Project Structure Changes

### Current Structure
```
android-otp-copy/
├── app/
│   └── src/main/java/com/otpcopy/
├── build.gradle
├── settings.gradle
└── ...
```

### New Structure
```
android-otp-copy/
├── Android/                    # Android app (moved from root)
│   ├── app/
│   │   └── src/main/java/com/otpcopy/
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradle/
├── Convex/                     # Convex backend
│   ├── convex/
│   │   ├── schema.ts           # Database schema
│   │   ├── otpItems.ts         # OTP mutations and queries
│   │   └── _generated/
│   ├── package.json
│   ├── pnpm-lock.yaml
│   └── convex.json
├── ARCHITECTURE.md
├── README.md
└── ...
```

---

## Convex Backend Implementation

### 1. Initialize Convex Project

```bash
cd Convex
pnpm create convex@latest .
```

### 2. Database Schema (`Convex/convex/schema.ts`)

```typescript
import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  otpItems: defineTable({
    otp: v.string(),                    // The OTP code (4-8 digits)
    sourceApp: v.optional(v.string()),  // Package name of source app
    notificationTitle: v.optional(v.string()),
    notificationText: v.optional(v.string()),
    deviceId: v.optional(v.string()),   // Unique device identifier
    copiedAt: v.number(),               // Timestamp when OTP was copied
  })
    .index("by_copiedAt", ["copiedAt"])
    .index("by_deviceId", ["deviceId"]),
});
```

### 3. OTP Mutations (`Convex/convex/otpItems.ts`)

```typescript
import { mutation, query } from "./_generated/server";
import { v } from "convex/values";

// Save a new OTP item
export const save = mutation({
  args: {
    otp: v.string(),
    sourceApp: v.optional(v.string()),
    notificationTitle: v.optional(v.string()),
    notificationText: v.optional(v.string()),
    deviceId: v.optional(v.string()),
    copiedAt: v.number(),
  },
  handler: async (ctx, args) => {
    const otpItemId = await ctx.db.insert("otpItems", {
      otp: args.otp,
      sourceApp: args.sourceApp,
      notificationTitle: args.notificationTitle,
      notificationText: args.notificationText,
      deviceId: args.deviceId,
      copiedAt: args.copiedAt,
    });
    return otpItemId;
  },
});

// List recent OTP items
export const list = query({
  args: {
    limit: v.optional(v.number()),
  },
  handler: async (ctx, args) => {
    const limit = args.limit ?? 50;
    const otpItems = await ctx.db
      .query("otpItems")
      .order("desc")
      .take(limit);
    return otpItems;
  },
});

// List OTP items by device
export const listByDevice = query({
  args: {
    deviceId: v.string(),
    limit: v.optional(v.number()),
  },
  handler: async (ctx, args) => {
    const limit = args.limit ?? 50;
    const otpItems = await ctx.db
      .query("otpItems")
      .withIndex("by_deviceId", (q) => q.eq("deviceId", args.deviceId))
      .order("desc")
      .take(limit);
    return otpItems;
  },
});

// Delete an OTP item
export const remove = mutation({
  args: {
    id: v.id("otpItems"),
  },
  handler: async (ctx, args) => {
    await ctx.db.delete(args.id);
  },
});
```

### 4. HTTP Endpoint for Android (`Convex/convex/http.ts`)

The Android app will use HTTP to communicate with Convex:

```typescript
import { httpRouter } from "convex/server";
import { httpAction } from "./_generated/server";
import { api } from "./_generated/api";

const http = httpRouter();

http.route({
  path: "/otp",
  method: "POST",
  handler: httpAction(async (ctx, request) => {
    const body = await request.json();
    
    // Validate required fields
    if (!body.otp || typeof body.otp !== "string") {
      return new Response(JSON.stringify({ error: "Invalid OTP" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }

    // Save the OTP item
    const id = await ctx.runMutation(api.otpItems.save, {
      otp: body.otp,
      sourceApp: body.sourceApp,
      notificationTitle: body.notificationTitle,
      notificationText: body.notificationText,
      deviceId: body.deviceId,
      copiedAt: body.copiedAt ?? Date.now(),
    });

    return new Response(JSON.stringify({ success: true, id }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  }),
});

http.route({
  path: "/otp",
  method: "GET",
  handler: httpAction(async (ctx, request) => {
    const url = new URL(request.url);
    const deviceId = url.searchParams.get("deviceId");
    const limit = parseInt(url.searchParams.get("limit") ?? "50");

    let items;
    if (deviceId) {
      items = await ctx.runQuery(api.otpItems.listByDevice, { deviceId, limit });
    } else {
      items = await ctx.runQuery(api.otpItems.list, { limit });
    }

    return new Response(JSON.stringify(items), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  }),
});

export default http;
```

### 5. Package Configuration (`Convex/package.json`)

```json
{
  "name": "otp-copy-convex",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "convex dev",
    "deploy": "convex deploy"
  },
  "dependencies": {
    "convex": "^1.17.0"
  },
  "devDependencies": {
    "typescript": "^5.0.0"
  }
}
```

---

## Android Implementation Changes

### 1. New Dependencies (`Android/app/build.gradle`)

Add the following dependencies for HTTP networking:

```gradle
dependencies {
    // Existing dependencies...
    
    // OkHttp for HTTP requests
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Gson for JSON serialization
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Kotlin coroutines for async operations
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

### 2. Internet Permission (`Android/app/src/main/AndroidManifest.xml`)

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 3. Configuration Class (`Android/app/src/main/java/com/otpcopy/ConvexConfig.kt`)

```kotlin
package com.otpcopy

object ConvexConfig {
    // Replace with your Convex deployment URL after running `npx convex deploy`
    const val CONVEX_HTTP_URL = "https://YOUR_DEPLOYMENT.convex.site"
    
    // Endpoint paths
    const val OTP_ENDPOINT = "/otp"
}
```

### 4. Convex API Client (`Android/app/src/main/java/com/otpcopy/ConvexApiClient.kt`)

```kotlin
package com.otpcopy

import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ConvexApiClient(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    
    data class OtpSaveRequest(
        val otp: String,
        val sourceApp: String?,
        val notificationTitle: String?,
        val notificationText: String?,
        val deviceId: String,
        val copiedAt: Long
    )
    
    data class OtpSaveResponse(
        val success: Boolean,
        val id: String?
    )
    
    suspend fun saveOtp(
        otp: String,
        sourceApp: String? = null,
        notificationTitle: String? = null,
        notificationText: String? = null
    ): Result<OtpSaveResponse> = withContext(Dispatchers.IO) {
        try {
            val request = OtpSaveRequest(
                otp = otp,
                sourceApp = sourceApp,
                notificationTitle = notificationTitle,
                notificationText = notificationText,
                deviceId = deviceId,
                copiedAt = System.currentTimeMillis()
            )
            
            val requestBody = gson.toJson(request).toRequestBody(jsonMediaType)
            
            val httpRequest = Request.Builder()
                .url("${ConvexConfig.CONVEX_HTTP_URL}${ConvexConfig.OTP_ENDPOINT}")
                .post(requestBody)
                .build()
            
            val response = client.newCall(httpRequest).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val saveResponse = gson.fromJson(responseBody, OtpSaveResponse::class.java)
                Result.success(saveResponse)
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 5. Modified OtpNotificationListener (`Android/app/src/main/java/com/otpcopy/OtpNotificationListener.kt`)

Update the `OtpNotificationListener` to save OTPs to Convex:

```kotlin
package com.otpcopy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class OtpNotificationListener : NotificationListenerService() {

    private lateinit var appPreferences: AppPreferences
    private lateinit var convexApiClient: ConvexApiClient
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "OtpNotificationListener"
        
        // Gmail package name for filtering notifications
        private const val GMAIL_PACKAGE = "com.google.android.gm"

        // Common OTP patterns - ordered from most specific to least specific
        private val OTP_PATTERNS = listOf(
            Pattern.compile("(?:OTP|otp|code|verification code|verify)\\s*(?:is|:)?\\s*(\\d{4,8})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:Your|your)\\s+(?:code|OTP|verification code)\\s+(?:is|:)\\s*(\\d{4,8})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{4,8})\\s+(?:is|is your|is the)\\s+(?:OTP|code|verification code|one-time password|one time password)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:use|enter)\\s+(?:code|OTP)?\\s*(\\d{4,8})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(\\d{4,8})\\b")
        )
    }

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(this)
        convexApiClient = ConvexApiClient(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        if (sbn == null) return

        val sourcePackage = sbn.packageName
        if (sourcePackage != null && appPreferences.isAppIgnored(sourcePackage)) {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        val fullText = "$title $text $bigText"

        val otp = extractOtp(fullText)
        if (otp != null && otp.isNotEmpty()) {
            copyToClipboard(otp)
            showToast(getString(R.string.otp_copied, otp))
            
            // Save OTP to Convex
            saveOtpToConvex(
                otp = otp,
                sourceApp = sourcePackage,
                notificationTitle = title,
                notificationText = text.ifEmpty { bigText }
            )
        }
    }

    private fun extractOtp(text: String): String? {
        if (text.isEmpty()) return null

        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val otp = matcher.group(1) ?: continue
                if (otp.length in 4..8 && otp.matches(Regex("\\d+"))) {
                    return otp
                }
            }
        }

        return null
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OTP", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showToast(message: String) {
        android.os.Handler(mainLooper).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun saveOtpToConvex(
        otp: String,
        sourceApp: String?,
        notificationTitle: String?,
        notificationText: String?
    ) {
        serviceScope.launch {
            val result = convexApiClient.saveOtp(
                otp = otp,
                sourceApp = sourceApp,
                notificationTitle = notificationTitle,
                notificationText = notificationText
            )
            
            result.onSuccess {
                Log.d(TAG, "OTP saved to Convex: ${it.id}")
            }.onFailure { error ->
                Log.e(TAG, "Failed to save OTP to Convex", error)
            }
        }
    }
}
```

### 6. Settings Screen Update (`Android/app/src/main/java/com/otpcopy/SettingsActivity.kt`)

Add a setting for enabling/disabling cloud sync:

```kotlin
// Add to existing SettingsActivity or create new preference
class ConvexSyncPreference {
    companion object {
        private const val PREF_CONVEX_SYNC_ENABLED = "convex_sync_enabled"
        
        fun isEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_CONVEX_SYNC_ENABLED, true) // Enabled by default
        }
        
        fun setEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_CONVEX_SYNC_ENABLED, enabled).apply()
        }
    }
}
```

---

## Implementation Steps

### Phase 1: Project Restructure

1. Create new directory structure:
   ```bash
   mkdir -p Android Convex
   ```

2. Move Android files to `Android/` folder:
   ```bash
   mv app Android/
   mv build.gradle Android/
   mv settings.gradle Android/
   mv gradle Android/
   mv gradlew Android/
   mv gradlew.bat Android/  # if exists
   mv gradle.properties Android/
   mv local.properties Android/  # if exists
   ```

3. Update any path references in Android build files if needed

### Phase 2: Convex Backend Setup

1. Initialize Convex project:
   ```bash
   cd Convex
   pnpm init
   pnpm add convex
   pnpm add -D typescript
   npx convex init
   ```

2. Create schema and functions as specified above

3. Deploy Convex:
   ```bash
   npx convex deploy
   ```

4. Note the deployment URL for Android configuration

### Phase 3: Android Integration

1. Add dependencies to `Android/app/build.gradle`

2. Add Internet permission to `AndroidManifest.xml`

3. Create `ConvexConfig.kt` with the deployment URL

4. Create `ConvexApiClient.kt`

5. Update `OtpNotificationListener.kt` to call Convex API

6. Build and test the Android app

---

## Manual Testing Instructions

### Prerequisites

1. Android device or emulator with notification access permission granted to the app
2. Access to Convex dashboard (https://dashboard.convex.dev)
3. A way to trigger OTP notifications (e.g., test email, SMS, or app that sends OTP)

### Test Cases

#### Test 1: Verify Project Structure

1. Confirm directory structure:
   ```bash
   ls -la  # Should show Android/ and Convex/ folders
   ls -la Android/  # Should show app/, build.gradle, etc.
   ls -la Convex/   # Should show convex/, package.json, etc.
   ```

2. Verify Android project builds:
   ```bash
   cd Android
   ./gradlew assembleDebug
   ```

3. Verify Convex deploys:
   ```bash
   cd Convex
   pnpm run deploy
   ```

**Expected**: Both projects build/deploy successfully without errors.

---

#### Test 2: Basic OTP Capture and Sync

1. Install the Android app on a test device
2. Grant notification access permission
3. Trigger an OTP notification (e.g., request a verification code from a service)
4. Verify:
   - [ ] Toast shows "OTP copied: XXXXXX"
   - [ ] OTP is in clipboard (paste to verify)
5. Check Convex dashboard → Data → `otpItems` table
   - [ ] New entry exists with correct OTP value
   - [ ] `sourceApp` shows the correct package name
   - [ ] `copiedAt` timestamp is accurate
   - [ ] `deviceId` is populated

**Expected**: OTP appears in clipboard AND in Convex database within ~2 seconds.

---

#### Test 3: Multiple OTPs

1. Trigger 3 different OTP notifications in sequence
2. Wait 5 seconds between each
3. Check Convex dashboard

**Expected**: 
- All 3 OTPs appear in `otpItems` table
- Each has distinct `copiedAt` timestamps
- Entries are ordered by time (most recent first)

---

#### Test 4: Offline Behavior

1. Enable airplane mode on the device
2. Trigger an OTP notification
3. Verify:
   - [ ] Toast still shows "OTP copied"
   - [ ] OTP is copied to clipboard
4. Check Convex dashboard

**Expected**: 
- Local clipboard copy works offline
- OTP does NOT appear in Convex (expected - no retry mechanism in v1)
- No crash or error toast shown to user

---

#### Test 5: Query OTP History

1. Using curl or Postman, query the HTTP endpoint:
   ```bash
   curl "https://YOUR_DEPLOYMENT.convex.site/otp?limit=10"
   ```

2. Query by device ID:
   ```bash
   curl "https://YOUR_DEPLOYMENT.convex.site/otp?deviceId=YOUR_DEVICE_ID&limit=10"
   ```

**Expected**: JSON array of OTP items returned, matching what's in the dashboard.

---

#### Test 6: App Ignore List

1. Add an app to the ignore list in OTP Copy settings
2. Trigger an OTP notification from that ignored app
3. Check clipboard and Convex dashboard

**Expected**: 
- OTP is NOT copied to clipboard
- OTP is NOT saved to Convex

---

#### Test 7: Edge Cases

1. **Very long OTP (8 digits)**:
   - Trigger a notification with an 8-digit code
   - Verify it's captured and synced

2. **Short OTP (4 digits)**:
   - Trigger a notification with a 4-digit code
   - Verify it's captured and synced

3. **Rapid succession**:
   - Trigger 5 OTPs within 5 seconds
   - Verify all are captured (may have some drops in rapid succession)

---

### Verification Checklist

| # | Test | Status |
|---|------|--------|
| 1 | Android project builds from Android/ folder | ☐ |
| 2 | Convex project deploys from Convex/ folder | ☐ |
| 3 | OTP copied to clipboard | ☐ |
| 4 | OTP saved to Convex database | ☐ |
| 5 | Source app name captured | ☐ |
| 6 | Notification title captured | ☐ |
| 7 | Device ID captured | ☐ |
| 8 | Timestamp accurate | ☐ |
| 9 | HTTP GET endpoint returns OTPs | ☐ |
| 10 | Offline OTP copy works | ☐ |
| 11 | Ignored apps work correctly | ☐ |

---

## Security Considerations

1. **Authentication**: The v1 HTTP endpoint has no authentication. For production:
   - Add API key validation
   - Or use Convex's built-in auth

2. **Data Privacy**: OTPs are sensitive. Consider:
   - Auto-delete OTPs older than 24 hours
   - Add encryption at rest
   - Limit access by device ID

3. **Rate Limiting**: Consider adding rate limiting to prevent abuse

---

## Future Enhancements

1. **Offline Queue**: Queue failed syncs and retry when online
2. **OTP History UI**: Show synced OTPs in the Android app
3. **Cross-Device Sync**: View OTPs from other devices
4. **Auto-Cleanup**: Scheduled function to delete old OTPs
5. **Authentication**: Add user authentication for multi-user support

---

## Dependencies Summary

### Convex (pnpm)
- `convex`: ^1.17.0
- `typescript`: ^5.0.0 (dev)

### Android (Gradle)
- `com.squareup.okhttp3:okhttp`: 4.12.0
- `com.google.code.gson:gson`: 2.10.1
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`: 1.7.3
