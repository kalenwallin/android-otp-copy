package com.otpcopy

import android.graphics.drawable.Drawable

/**
 * Data class representing an installed app for the selection list.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)
