package com.example.taskmaster

import android.app.Application
import com.example.taskmaster.data.SettingsRepository
import com.example.taskmaster.data.TaskRepository

class TaskMasterApplication : Application() {
    // Lazy initialization of repositories
    val taskRepository by lazy { TaskRepository(this) }
    val settingsRepository by lazy { SettingsRepository(this) }
}