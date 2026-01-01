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

    override suspend fun backupTasksToDevice(): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = gson.toJson(_tasks.value)
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(
                    android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
                    "TaskMaster_Backup_${System.currentTimeMillis()}.json"
                )
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOCUMENTS + "/TaskMaster"
                )
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(
                android.provider.MediaStore.Files.getContentUri("external"),
                contentValues
            )
            uri?.let {
                resolver.openOutputStream(it)
                    ?.use { stream -> stream.write(jsonString.toByteArray()) }
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            return@withContext false
        }
    }
}