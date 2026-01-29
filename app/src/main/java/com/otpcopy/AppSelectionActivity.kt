package com.otpcopy

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var appPreferences: AppPreferences
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.app_selection_title)

        appPreferences = AppPreferences(this)
        
        recyclerView = findViewById(R.id.appRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter(emptyList(), appPreferences)
        recyclerView.adapter = adapter

        loadInstalledApps()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadInstalledApps() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                getInstalledApps()
            }
            adapter.updateApps(apps)
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return installedApps
            .filter { appInfo ->
                // Exclude our own app, include all other apps that can send notifications
                appInfo.packageName != packageName
            }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = try {
                        pm.getApplicationIcon(appInfo.packageName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    private inner class AppListAdapter(
        private var apps: List<AppInfo>,
        private val prefs: AppPreferences
    ) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

        fun updateApps(newApps: List<AppInfo>) {
            apps = newApps
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.bind(app)
        }

        override fun getItemCount(): Int = apps.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
            private val appName: TextView = itemView.findViewById(R.id.appName)
            private val appPackage: TextView = itemView.findViewById(R.id.appPackage)
            private val enabledCheckBox: CheckBox = itemView.findViewById(R.id.enabledCheckBox)

            fun bind(app: AppInfo) {
                appName.text = app.appName
                appPackage.text = app.packageName
                
                if (app.icon != null) {
                    appIcon.setImageDrawable(app.icon)
                } else {
                    appIcon.setImageResource(android.R.drawable.sym_def_app_icon)
                }

                // Checkbox is checked when app is NOT ignored (i.e., enabled for OTP detection)
                enabledCheckBox.setOnCheckedChangeListener(null)
                enabledCheckBox.isChecked = !prefs.isAppIgnored(app.packageName)
                
                enabledCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    prefs.setAppIgnored(app.packageName, !isChecked)
                }

                // Make the checkbox not clickable directly, only through the item click
                enabledCheckBox.isClickable = false
                
                itemView.setOnClickListener {
                    enabledCheckBox.isChecked = !enabledCheckBox.isChecked
                }
            }
        }
    }
}
