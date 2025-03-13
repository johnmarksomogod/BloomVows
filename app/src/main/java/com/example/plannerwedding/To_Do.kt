package com.example.plannerwedding

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class To_Do : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var totalTasksText: TextView
    private lateinit var completedTasksText: TextView
    private lateinit var remainingTasksText: TextView
    private lateinit var addTaskIcon: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var totalTasks = 0
    private var completedTasks = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to__do, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        progressBar = view.findViewById(R.id.todoProgress)
        totalTasksText = view.findViewById(R.id.totalTasks)
        completedTasksText = view.findViewById(R.id.completedTasks)
        remainingTasksText = view.findViewById(R.id.remainingTasks)
        addTaskIcon = view.findViewById(R.id.addTaskIcon)

        loadTasksFromFirestore()

        addTaskIcon.setOnClickListener {
            val dialog = TaskDialog()
            dialog.show(parentFragmentManager, "TaskDialog")
        }

        return view
    }

    private fun loadTasksFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId)
            .collection("Tasks")
            .get()
            .addOnSuccessListener { snapshot ->
                totalTasks = 0
                completedTasks = 0

                clearAllTaskViews()

                if (snapshot.isEmpty) {
                    updateProgress()
                }

                for (document in snapshot.documents) {
                    val task = document.toObject(Task::class.java)
                    task?.let {
                        totalTasks++
                        if (it.completed) {
                            completedTasks++
                        }
                        addTaskToCategory(it)
                    }
                }
                updateProgress()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
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

        categoryContainer.addView(taskView)

        taskView.setOnClickListener {
            val dialog = TaskDialog()
            val bundle = Bundle()
            bundle.putSerializable("task", task)
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "TaskDialog")
        }

        heartIcon.setOnClickListener {
            showCompletionConfirmationDialog(task, taskView)
        }

        taskView.findViewById<ImageView>(R.id.deleteTaskIcon).setOnClickListener {
            showDeleteConfirmationDialog(task, taskView)
        }
    }

    private fun showCompletionConfirmationDialog(task: Task, taskView: View) {
        val message = if (task.completed)
            "Are you sure you want to mark this task as incomplete?"
        else
            "Are you sure you want to mark this task as completed?"

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Confirm Status Change")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                task.completed = !task.completed
                updateTaskInFirestore(task)

            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

        val alert = alertDialogBuilder.create()
        alert.show()

        // Change the text color of the buttons
        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))

        // Set dialog message text color to black
        alert.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
    }

    private fun showDeleteConfirmationDialog(task: Task, taskView: View) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                deleteTask(task, taskView)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

        val alert = alertDialogBuilder.create()
        alert.show()

        // Change the text color of the buttons
        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))

        // Set dialog message text color to black
        alert.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
    }

    private fun updateTaskInFirestore(task: Task) {
        val userId = auth.currentUser?.uid ?: return
        val taskRef = db.collection("Users").document(userId)
            .collection("Tasks").document(task.id ?: return)

        taskRef.set(task).addOnSuccessListener {
            Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(task: Task, taskView: View) {
        val taskId = task.id ?: return
        val userId = auth.currentUser?.uid ?: return
        db.collection("Users").document(userId)
            .collection("Tasks").document(taskId).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show()
                (taskView.parent as? LinearLayout)?.removeView(taskView)
                updateProgress()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
    }
}
