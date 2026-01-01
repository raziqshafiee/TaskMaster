package com.example.taskmaster.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.TaskMasterApplication
import com.example.taskmaster.data.Task
import com.example.taskmaster.viewmodel.TaskViewModel
import com.example.taskmaster.viewmodel.TaskViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as TaskMasterApplication).taskRepository)
    }
    private val settingsRepo by lazy { (application as TaskMasterApplication).settingsRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Observe Dark Mode
        lifecycleScope.launch {
            settingsRepo.isDarkMode.collect { isDark ->
                AppCompatDelegate.setDefaultNightMode(if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Setup List
        val recyclerView: RecyclerView = findViewById(R.id.rvTasks)
        val adapter = TaskAdapter(emptyList(),
            onTaskChecked = { task, isChecked -> viewModel.toggleTask(task, isChecked) },
            onTaskClicked = { task -> showTaskDialog(task) } // Click triggers Edit
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModel.tasks.collect { tasks -> adapter.updateTasks(tasks) }
        }

        // Setup Sort
        val spinner: Spinner = findViewById(R.id.spinnerSort)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Sort...", "Date", "Name"))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { viewModel.setSortOrder(pos) }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Setup Add Button
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener { showTaskDialog(null) }

        // Setup Settings Button
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun showTaskDialog(taskToEdit: Task?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val editTitle = view.findViewById<EditText>(R.id.etTaskTitle)
        val editDue = view.findViewById<EditText>(R.id.etTaskDue)

        if (taskToEdit != null) {
            editTitle.setText(taskToEdit.title)
            editDue.setText(taskToEdit.dueDate)
        }

        editDue.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d -> editDue.setText("$y-${m+1}-$d") },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (taskToEdit == null) "New Task" else "Edit Task")
            .setView(view)
            .setPositiveButton(if (taskToEdit == null) "Add" else "Update", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = editTitle.text.toString().trim()
            val due = editDue.text.toString().trim()
            if (title.isNotEmpty() && due.isNotEmpty()) {
                if (taskToEdit == null) viewModel.addTask(title, due)
                else viewModel.updateTask(taskToEdit.copy(title = title, dueDate = due))
                dialog.dismiss()
            }
        }
    }
}