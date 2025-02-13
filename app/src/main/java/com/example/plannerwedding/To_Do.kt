package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to__do, container, false)

        progressBar = view.findViewById(R.id.todoProgress)
        totalTasksText = view.findViewById(R.id.totalTasks)
        completedTasksText = view.findViewById(R.id.completedTasks)
        remainingTasksText = view.findViewById(R.id.remainingTasks)
        addTaskIcon = view.findViewById(R.id.addTaskIcon)

        // Initialize Firebase with custom URL
        database = FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("tasks")

        loadTasksFromFirebase()

        addTaskIcon.setOnClickListener {
            val dialog = TaskDialog()
            dialog.show(parentFragmentManager, "TaskDialog")
        }

        return view
    }

    private fun loadTasksFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalTasks = 0
                completedTasks = 0

                // Reset UI and add tasks
                clearAllTaskViews()

                for (taskSnapshot in snapshot.children) {
                    for (categorySnapshot in taskSnapshot.children) {
                        val task = categorySnapshot.getValue(Task::class.java)
                        task?.let {
                            totalTasks++
                            if (it.completed) {
                                completedTasks++
                            }
                            addTaskToCategory(it)
                        }
                    }
                }
                updateProgress()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun clearAllTaskViews() {
        val allContainers = listOf(
            R.id.task_priorityContainer,
            R.id.task_weddingVenueContainer,
            R.id.task_entertainmentContainer,
            R.id.task_foodContainer,
            R.id.task_ceremonyEssentialsTaskContainer,
            R.id.task_favorsGiftsTaskContainer,
            R.id.task_entertainmentTaskContainer,
            R.id.task_photographyContainer,
            R.id.task_hairMakeupContainer,
            R.id.task_brideOutfitContainer,
            R.id.task_groomOutfitContainer,
            R.id.task_transportationContainer,
            R.id.task_otherTasksContainer
        )

        for (containerId in allContainers) {
            val container = view?.findViewById<LinearLayout>(containerId)
            container?.removeAllViews()
        }
    }

    private fun updateProgress() {
        val remainingTasks = totalTasks - completedTasks
        val progress = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

        progressBar.progress = progress
        totalTasksText.text = "Total Tasks: $totalTasks"
        completedTasksText.text = "Completed: $completedTasks"
        remainingTasksText.text = "Remaining: $remainingTasks"
    }

    private fun addTaskToCategory(task: Task) {
        val containerId = when (task.category) {
            "Priority" -> R.id.task_priorityContainer
            "Wedding Venue" -> R.id.task_weddingVenueContainer
            "Entertainment" -> R.id.task_entertainmentContainer
            "Food & Beverages" -> R.id.task_foodContainer
            "Ceremony Essentials" -> R.id.task_ceremonyEssentialsTaskContainer
            "Favors and Gifts" -> R.id.task_favorsGiftsTaskContainer
            "Decorations" -> R.id.task_entertainmentTaskContainer
            "Photography and Videography" -> R.id.task_photographyContainer
            "Hair and Makeup" -> R.id.task_hairMakeupContainer
            "Bride's Outfit" -> R.id.task_brideOutfitContainer
            "Groom's Outfit" -> R.id.task_groomOutfitContainer
            "Transportation Tasks" -> R.id.task_transportationContainer
            else -> R.id.task_otherTasksContainer
        }

        val categoryContainer = view?.findViewById<LinearLayout>(containerId) ?: return
        val taskView = layoutInflater.inflate(R.layout.fragment_task__card, null)

        val title = taskView.findViewById<TextView>(R.id.taskTitle)
        val deadline = taskView.findViewById<TextView>(R.id.taskDeadline)
        val status = taskView.findViewById<TextView>(R.id.taskStatus)
        val heartIcon = taskView.findViewById<ImageView>(R.id.completeTaskIcon)

        title.text = task.name
        deadline.text = "Deadline: ${task.deadline}"
        status.text = if (task.completed) {
            heartIcon.setImageResource(R.drawable.heart_filled)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            "Completed"
        } else {
            heartIcon.setImageResource(R.drawable.heart1)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Pending"
        }

        // Add task to container
        categoryContainer.addView(taskView)

        // Set click listener on the task card to edit the task
        taskView.setOnClickListener {
            // Pass the task to the TaskDialog for editing
            val dialog = TaskDialog()
            val bundle = Bundle()
            bundle.putSerializable("task", task) // Pass the task object
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "TaskDialog")
        }

        // Heart icon listener to mark as completed or pending
        heartIcon.setOnClickListener {
            task.completed = !task.completed
            updateTaskInFirebase(task)

            // Manually update the heart icon and status after the database update
            bindTask(taskView, task)

            // Update the progress bar after marking the task as completed/undone
            updateProgress()  // Recalculate the progress and update the UI
        }

        // Delete icon listener to remove the task
        taskView.findViewById<ImageView>(R.id.deleteTaskIcon).setOnClickListener {
            deleteTask(task, taskView)
        }
    }

    private fun updateTaskInFirebase(task: Task) {
        val taskRef = FirebaseDatabase.getInstance().getReference("tasks")
            .child(task.category)
            .child(task.id ?: return)
        taskRef.setValue(task).addOnSuccessListener {
            // You don't need to reload tasks, manually update the UI
            Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(task: Task, taskView: View) {
        val taskId = task.id ?: return
        database.child(task.category).child(taskId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show()
                // Remove task from the UI as well
                val parent = taskView.parent as? ViewGroup
                parent?.removeView(taskView)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
    }

    // Bind task data to the UI (heart icon, status, etc.)
    private fun bindTask(view: View, task: Task) {
        val title = view.findViewById<TextView>(R.id.taskTitle)
        val deadline = view.findViewById<TextView>(R.id.taskDeadline)
        val status = view.findViewById<TextView>(R.id.taskStatus)
        val completeTaskIcon = view.findViewById<ImageView>(R.id.completeTaskIcon)

        title.text = task.name
        deadline.text = "Deadline: ${task.deadline}"
        status.text = if (task.completed) {
            completeTaskIcon.setImageResource(R.drawable.heart_filled)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            "Completed"
        } else {
            completeTaskIcon.setImageResource(R.drawable.heart1)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Pending"
        }

        // Update the progress bar after each change
        updateProgress()
    }
}
