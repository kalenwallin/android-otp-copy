package com.otpcopy

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var openSettingsButton: Button
    private lateinit var selectAppsButton: Button
    private lateinit var openNotificationSettingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        openSettingsButton = findViewById(R.id.openSettingsButton)
        selectAppsButton = findViewById(R.id.selectAppsButton)
        openNotificationSettingsButton = findViewById(R.id.openNotificationSettingsButton)

        openSettingsButton.setOnClickListener {
            openNotificationSettings()
        }

        selectAppsButton.setOnClickListener {
            openAppSelection()
        }

        openNotificationSettingsButton.setOnClickListener {
            openSystemNotificationSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        updateNotificationAccessStatus()
    }

    private fun updateNotificationAccessStatus() {
        val isEnabled = isNotificationServiceEnabled()
        statusText.text = if (isEnabled) {
            getString(R.string.notification_access_enabled)
        } else {
            getString(R.string.notification_access_disabled)
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        if (flat != null && flat.isNotEmpty()) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null) {
                    if (pkgName == cn.packageName) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    private fun openAppSelection() {
        val intent = Intent(this, AppSelectionActivity::class.java)
        startActivity(intent)
    }

    private fun openSystemNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_SETTINGS)
        startActivity(intent)
    }
}
