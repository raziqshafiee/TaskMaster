package com.example.taskmaster.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmaster.R
import com.example.taskmaster.TaskMasterApplication
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val app = application as TaskMasterApplication
        val switchDark = findViewById<Switch>(R.id.switchDarkMode)
        val btnBackup = findViewById<Button>(R.id.btnBackup)

        // Observe Dark Mode
        lifecycleScope.launch {
            app.settingsRepository.isDarkMode.collect { isDark ->
                switchDark.isChecked = isDark
            }
        }

        // Toggle Dark Mode
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { app.settingsRepository.setDarkMode(isChecked) }
        }

        // Placeholder for Role C's backup logic
        btnBackup.setOnClickListener {
            // Role C will implement the logic here later
            Toast.makeText(this, "Backup feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}