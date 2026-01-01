package com.example.taskmaster.data
import kotlinx.coroutines.flow.StateFlow

interface ITaskRepository {
    val tasks: StateFlow<List<Task>>
    suspend fun saveTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean)
    suspend fun backupTasksToDevice(): Boolean
}