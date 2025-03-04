package com.example.plannerwedding

import android.content.DialogInterface
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

class TaskCard : Fragment() {

    private var task: Task? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task__card, container, false)

        db = FirebaseFirestore.getInstance()

        task?.let { bindTask(view, it) }

        val completeTaskIcon = view.findViewById<ImageView>(R.id.completeTaskIcon)
        val deleteTaskIcon = view.findViewById<ImageView>(R.id.deleteTaskIcon)
        val taskCardContainer = view.findViewById<LinearLayout>(R.id.taskCardContainer)

        // When the task card itself is clicked
        taskCardContainer.setOnClickListener {
            task?.let {
                it.completed = !it.completed
                showCompleteConfirmationDialog(view, it) // Show confirmation before marking as complete
            }
        }

        // When the "complete" icon (heart icon) is clicked
        completeTaskIcon.setOnClickListener {
            task?.let {
                it.completed = !it.completed
                showCompleteConfirmationDialog(view, it) // Show confirmation before marking as complete
            }
        }

        // When the "delete" icon is clicked
        deleteTaskIcon.setOnClickListener {
            task?.let {
                showDeleteConfirmationDialog(view, it) // Show confirmation before deleting
            }
        }

        return view
    }

    // Confirmation dialog for marking the task as completed or pending
    private fun showCompleteConfirmationDialog(view: View, task: Task) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setMessage("Are you sure you want to mark this task as ${if (task.completed) "Pending" else "Completed"}?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                bindTask(view, task)
                updateTaskStatus(task) // Update task status in Firestore
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        val alert = alertDialogBuilder.create()
        alert.show()

        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))
    }

    // Confirmation dialog for deleting the task
    private fun showDeleteConfirmationDialog(view: View, task: Task) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setMessage("Are you sure you want to delete this task?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                deleteTask(task, view) // Proceed with deleting the task
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        val alert = alertDialogBuilder.create()
        alert.show()

        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.parseColor("#EDABAD"))
        negativeButton.setTextColor(Color.parseColor("#EDABAD"))
    }

    // Bind task data to UI elements (update task view)
    fun bindTask(view: View, task: Task) {
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
    }

    // Update the task status in Firestore
    private fun updateTaskStatus(task: Task) {
        val taskId = task.id ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("Users").document(userId)
            .collection("Tasks").document(taskId)
            .set(task)
            .addOnSuccessListener {
                Toast.makeText(context, "Task updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete the task from Firestore and remove it from the UI
    private fun deleteTask(task: Task, view: View) {
        val taskId = task.id ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("Users").document(userId)
            .collection("Tasks").document(taskId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Task deleted successfully!", Toast.LENGTH_SHORT).show()
                val taskCardContainer = view.findViewById<LinearLayout>(R.id.taskCardContainer)
                taskCardContainer?.let {
                    val parent = it.parent as? ViewGroup
                    parent?.removeView(it)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
    }

    // Set the task object to this fragment
    fun setTask(task: Task) {
        this.task = task
        view?.let { bindTask(it, task) }
    }
}
