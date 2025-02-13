package com.example.plannerwedding

import Task
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.plannerwedding.TaskDialog
import com.google.firebase.database.*

class To_Do : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var totalTasksText: TextView
    private lateinit var completedTasksText: TextView
    private lateinit var remainingTasksText: TextView
    private lateinit var addTaskIcon: LinearLayout

    private lateinit var database: DatabaseReference

    private var totalTasks = 0
    private var completedTasks = 0
    private var remainingTasks = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to__do, container, false)

        // Initialize Views
        progressBar = view.findViewById(R.id.todoProgress)
        totalTasksText = view.findViewById(R.id.totalTasks)
        completedTasksText = view.findViewById(R.id.completedTasks)
        remainingTasksText = view.findViewById(R.id.remainingTasks)
        addTaskIcon = view.findViewById(R.id.addTaskIcon)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("tasks")

        // Load Tasks from Firebase
        loadTasksFromFirebase()

        // Add Task Button Click
        addTaskIcon.setOnClickListener {
            showTaskDialog()
        }

        return view
    }

    private fun loadTasksFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalTasks = 0
                completedTasks = 0
                remainingTasks = 0

                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    task?.let {
                        totalTasks++
                        if (it.completed) {
                            completedTasks++
                        }
                    }
                }
                remainingTasks = totalTasks - completedTasks

                // Update UI
                updateTaskUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTaskUI() {
        totalTasksText.text = "Total Tasks: $totalTasks"
        completedTasksText.text = "Completed: $completedTasks"
        remainingTasksText.text = "Remaining: $remainingTasks"

        val progress = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
        progressBar.progress = progress
    }

    private fun showTaskDialog() {
        val dialog = TaskDialog()
        dialog.show(parentFragmentManager, "TaskDialog")
    }
}
