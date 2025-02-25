package com.example.plannerwedding

import android.os.Bundle
<<<<<<< HEAD
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TaskCard : Fragment() {

    private var task: Task? = null
    private lateinit var database: DatabaseReference
=======
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Task_Card.newInstance] factory method to
 * create an instance of this fragment.
 */
class Task_Card : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
<<<<<<< HEAD
        val view = inflater.inflate(R.layout.fragment_task__card, container, false)

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("tasks")

        // Bind the task data if available
        task?.let { bindTask(view, it) }

        val completeTaskIcon = view.findViewById<ImageView>(R.id.completeTaskIcon)
        val deleteTaskIcon = view.findViewById<ImageView>(R.id.deleteTaskIcon)
        val taskCardContainer = view.findViewById<LinearLayout>(R.id.taskCardContainer)

        // Task card click listener (Whole card click event)
        taskCardContainer.setOnClickListener {
            task?.let {
                it.completed = !it.completed  // Toggle completion status
                bindTask(view, it)  // Rebind the updated task
                updateTaskStatus(it)
            }
        }

        // Heart icon click listener to toggle completion
        completeTaskIcon.setOnClickListener {
            task?.let {
                it.completed = !it.completed  // Toggle completion status
                bindTask(view, it)  // Rebind the updated task
                updateTaskStatus(it)
            }
        }

        // Delete icon click listener to remove task
        deleteTaskIcon.setOnClickListener {
            task?.let {
                deleteTask(it, view) // Now passing the view to update it
            }
        }

        return view
    }

    // Method to bind task data to the view
    fun bindTask(view: View, task: Task) {
        val title = view.findViewById<TextView>(R.id.taskTitle)
        val deadline = view.findViewById<TextView>(R.id.taskDeadline)
        val status = view.findViewById<TextView>(R.id.taskStatus)
        val completeTaskIcon = view.findViewById<ImageView>(R.id.completeTaskIcon)

        title.text = task.name
        deadline.text = "Deadline: ${task.deadline}"
        status.text = if (task.completed) {
            completeTaskIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.heart_filled))  // Heart filled for completed
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.green)) // Green for completed
            "Completed"
        } else {
            completeTaskIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.heart1))  // Default heart for pending
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.red)) // Red for pending
            "Pending"
        }
    }

    // Update task status in Firebase
    private fun updateTaskStatus(task: Task) {
        val taskId = task.id ?: return
        database.child(task.category).child(taskId).setValue(task)
            .addOnSuccessListener {
                Toast.makeText(context, "Task updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete task from Firebase and UI
    private fun deleteTask(task: Task, view: View) {
        val taskId = task.id ?: return
        database.child(task.category).child(taskId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Task deleted successfully!", Toast.LENGTH_SHORT).show()

                // Remove task from the UI as well
                val taskCardContainer = view.findViewById<LinearLayout>(R.id.taskCardContainer)
                taskCardContainer?.let {
                    val parent = it.parent as? ViewGroup
                    parent?.removeView(it)  // Remove task view from parent container
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
    }

    // Set the task to be displayed in the fragment
    fun setTask(task: Task) {
        this.task = task
        view?.let { bindTask(it, task) }
    }
}
=======
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task__card, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Task_Card.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Task_Card().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
