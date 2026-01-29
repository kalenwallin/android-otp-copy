package com.otpcopy

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
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
    private lateinit var searchEditText: EditText
    private lateinit var selectAllButton: Button
    private lateinit var clearAllButton: Button
    private lateinit var appPreferences: AppPreferences
    private lateinit var adapter: AppListAdapter
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.app_selection_title)

        appPreferences = AppPreferences(this)
        
        recyclerView = findViewById(R.id.appRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        searchEditText = findViewById(R.id.searchEditText)
        selectAllButton = findViewById(R.id.selectAllButton)
        clearAllButton = findViewById(R.id.clearAllButton)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter(emptyList(), appPreferences)
        recyclerView.adapter = adapter

        setupSearch()
        setupButtons()
        loadInstalledApps()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
    }

    private fun setupButtons() {
        selectAllButton.setOnClickListener {
            selectAllApps()
        }
        clearAllButton.setOnClickListener {
            clearAllApps()
        }
    }

    private fun selectAllApps() {
        // Enable OTP detection for all apps (remove from ignored list)
        allApps.forEach { app ->
            appPreferences.setAppIgnored(app.packageName, false)
        }
        adapter.notifyDataSetChanged()
    }

    private fun clearAllApps() {
        // Disable OTP detection for all apps (add to ignored list)
        allApps.forEach { app ->
            appPreferences.setAppIgnored(app.packageName, true)
        }
        adapter.notifyDataSetChanged()
    }

    private fun filterApps(query: String) {
        val filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
        adapter.updateApps(filteredApps)
    }

    private fun loadInstalledApps() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                getInstalledApps()
            }
            allApps = apps
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
