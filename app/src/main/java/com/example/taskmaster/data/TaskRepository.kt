package com.example.taskmaster.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class TaskRepository(private val context: Context) : ITaskRepository {
    private val gson = Gson()
    private val internalFile = File(context.filesDir, "tasks.json")
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    override val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init { loadTasks() }

    private fun loadTasks() {
        if (!internalFile.exists()) return
        try {
            val json = internalFile.readText()
            if (json.isNotBlank()) {
                val type = object : TypeToken<List<Task>>() {}.type
                _tasks.value = gson.fromJson(json, type) ?: emptyList()
            }
        } catch (e: Exception) { Log.e("Repo", "Load Error", e) }
    }

    private suspend fun persist() = withContext(Dispatchers.IO) {
        internalFile.writeText(gson.toJson(_tasks.value))
    }

    override suspend fun saveTask(task: Task) {
        val list = _tasks.value.toMutableList()
        list.add(task)
        _tasks.value = list
        persist()
    }

    override suspend fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
        persist()
    }

    override suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        _tasks.value = _tasks.value.map { if (it.id == taskId) it.copy(isCompleted = isCompleted) else it }
        persist()
    }

    override suspend fun backupTasksToDevice(): Boolean = false // Role C will fix this
}