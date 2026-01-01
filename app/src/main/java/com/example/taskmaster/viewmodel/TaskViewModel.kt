package com.example.taskmaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.Task
import com.example.taskmaster.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _sortOrder = MutableStateFlow(0)

    val tasks = combine(repository.tasks, _sortOrder) { tasks, order ->
        when (order) {
            1 -> tasks.sortedBy { it.dueDate }
            2 -> tasks.sortedBy { it.title }
            else -> tasks
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOrder(order: Int) { _sortOrder.value = order }
    fun addTask(title: String, due: String) = viewModelScope.launch { repository.saveTask(Task(title=title, dueDate=due)) }
    fun updateTask(task: Task) = viewModelScope.launch { repository.updateTask(task) }
    fun toggleTask(task: Task, isChecked: Boolean) = viewModelScope.launch { repository.toggleTaskCompletion(task.id, isChecked) }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskViewModel(repository) as T
    }
}