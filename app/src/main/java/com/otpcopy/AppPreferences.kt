package com.otpcopy

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing app filtering preferences.
 * Stores which apps should be ignored when detecting OTPs.
 */
class AppPreferences(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "otp_copy_prefs"
        private const val KEY_IGNORED_APPS = "ignored_apps"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Get the set of ignored app package names.
     */
    fun getIgnoredApps(): Set<String> {
        return prefs.getStringSet(KEY_IGNORED_APPS, emptySet()) ?: emptySet()
    }
    
    /**
     * Check if a specific app is ignored.
     */
    fun isAppIgnored(packageName: String): Boolean {
        return getIgnoredApps().contains(packageName)
    }
    
    /**
     * Add an app to the ignored list.
     */
    fun addIgnoredApp(packageName: String) {
        val currentSet = getIgnoredApps().toMutableSet()
        currentSet.add(packageName)
        // Create a new HashSet to avoid SharedPreferences StringSet caching issues
        prefs.edit().putStringSet(KEY_IGNORED_APPS, HashSet(currentSet)).apply()
    }
    
    /**
     * Remove an app from the ignored list.
     */
    fun removeIgnoredApp(packageName: String) {
        val currentSet = getIgnoredApps().toMutableSet()
        currentSet.remove(packageName)
        // Create a new HashSet to avoid SharedPreferences StringSet caching issues
        prefs.edit().putStringSet(KEY_IGNORED_APPS, HashSet(currentSet)).apply()
    }
    
    /**
     * Set whether an app should be ignored.
     */
    fun setAppIgnored(packageName: String, ignored: Boolean) {
        if (ignored) {
            addIgnoredApp(packageName)
        } else {
            removeIgnoredApp(packageName)
        }
    }
}
