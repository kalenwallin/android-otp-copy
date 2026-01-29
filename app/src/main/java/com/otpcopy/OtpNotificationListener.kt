package com.otpcopy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Toast
import java.util.regex.Pattern

class OtpNotificationListener : NotificationListenerService() {

    companion object {
        // Gmail package name for filtering notifications
        private const val GMAIL_PACKAGE = "com.google.android.gm"

        // Common OTP patterns - ordered from most specific to least specific
        private val OTP_PATTERNS = listOf(
            // Matches "OTP: 123456" or "OTP is 123456"
            Pattern.compile("(?:OTP|otp|code|verification code|verify)\\s*(?:is|:)?\\s*(\\d{4,8})", Pattern.CASE_INSENSITIVE),
            // Matches "Your code is 123456"
            Pattern.compile("(?:Your|your)\\s+(?:code|OTP|verification code)\\s+(?:is|:)\\s*(\\d{4,8})", Pattern.CASE_INSENSITIVE),
            // Matches patterns like "123456 is your OTP"
            Pattern.compile("(\\d{4,8})\\s+(?:is|is your|is the)\\s+(?:OTP|code|verification code)", Pattern.CASE_INSENSITIVE),
            // Matches "Use code 123456"
            Pattern.compile("(?:use|enter)\\s+(?:code|OTP)?\\s*(\\d{4,8})", Pattern.CASE_INSENSITIVE),
            // Matches 4-8 digit numbers as last resort (moved to end to reduce false positives)
            Pattern.compile("\\b(\\d{4,8})\\b")
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        if (sbn == null) return

        // Only process notifications from Gmail
        if (sbn.packageName != GMAIL_PACKAGE) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Get notification title and text
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        // Combine all text for OTP extraction
        val fullText = "$title $text $bigText"

        // Try to extract OTP
        val otp = extractOtp(fullText)
        if (otp != null && otp.isNotEmpty()) {
            copyToClipboard(otp)
            showToast(getString(R.string.otp_copied, otp))
        }
    }

    private fun extractOtp(text: String): String? {
        if (text.isEmpty()) return null

        // Try each pattern
        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                // Get the captured group (the OTP digits)
                val otp = matcher.group(1) ?: continue
                // Validate OTP length (4-8 digits)
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
        // Show toast on main thread
        android.os.Handler(mainLooper).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Optional: Handle notification removal
    }
}
