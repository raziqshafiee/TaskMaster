package com.example.taskmaster.data

data class Task(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val dueDate: String,
    val isCompleted: Boolean = false
)