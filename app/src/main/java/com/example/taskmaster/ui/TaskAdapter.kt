package com.example.taskmaster.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.data.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TaskAdapter(
    private var taskList: List<Task>,
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTaskTitle)
        val dueDate: TextView = view.findViewById(R.id.tvTaskDue)
        val checkBox: CheckBox = view.findViewById(R.id.cbTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.title.text = task.title

        if (task.isCompleted) holder.dueDate.text = "Completed"
        else holder.dueDate.text = getFriendlyDate(task.dueDate)

        holder.itemView.setOnClickListener { onTaskClicked(task) }

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.isCompleted
        toggleStrikeThrough(holder.title, task.isCompleted)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            toggleStrikeThrough(holder.title, isChecked)
            holder.dueDate.text = if (isChecked) "Completed" else getFriendlyDate(task.dueDate)
            onTaskChecked(task, isChecked)
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        taskList = newTasks
        notifyDataSetChanged()
    }

    private fun toggleStrikeThrough(tv: TextView, isDone: Boolean) {
        if (isDone) tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    private fun getFriendlyDate(date: String): String {
        return try {
            val d = LocalDate.parse(date, DateTimeFormatter.ofPattern("y-M-d"))
            when (ChronoUnit.DAYS.between(LocalDate.now(), d)) {
                0L -> "Today"
                1L -> "Tomorrow"
                else -> d.format(DateTimeFormatter.ofPattern("MMM dd"))
            }
        } catch (e: Exception) { date }
    }

    override fun getItemCount() = taskList.size
}