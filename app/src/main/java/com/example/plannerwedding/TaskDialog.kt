package com.example.plannerwedding

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TaskDialog : DialogFragment() {

    private lateinit var taskNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var deadlineInput: EditText
    private lateinit var completedCheckbox: CheckBox
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var db: FirebaseFirestore
    private val calendar = Calendar.getInstance()

    private var task: Task? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_task_dialog, container, false)

        taskNameInput = view.findViewById(R.id.taskName)
        categorySpinner = view.findViewById(R.id.taskCategorySpinner)
        deadlineInput = view.findViewById(R.id.taskDeadline)
        completedCheckbox = view.findViewById(R.id.completedCheckbox)
        doneButton = view.findViewById(R.id.doneTaskButton)
        cancelButton = view.findViewById(R.id.cancelTaskButton)

        db = FirebaseFirestore.getInstance()
        val categories = listOf(
            "Priority", "Wedding Venue", "Entertainment", "Food & Beverages", "Ceremony Essentials",
            "Favors and Gifts", "Decorations", "Photography and Videography", "Hair and Makeup",
            "Bride's Outfit", "Groom's Outfit", "Transportation Tasks", "Other Tasks"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        deadlineInput.isFocusable = false
        deadlineInput.setOnClickListener { showDatePicker() }

        doneButton.setOnClickListener { saveTask() }
        cancelButton.setOnClickListener { dismiss() }

        task = arguments?.getSerializable("task") as Task?
        task?.let { populateTaskDetails(it) }

        return view
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                deadlineInput.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun populateTaskDetails(task: Task) {
        taskNameInput.setText(task.name)
        categorySpinner.setSelection((categorySpinner.adapter as ArrayAdapter<String>).getPosition(task.category))
        deadlineInput.setText(task.deadline)
        completedCheckbox.isChecked = task.completed
    }

    private fun saveTask() {
        val taskName = taskNameInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString() ?: ""
        val deadline = deadlineInput.text.toString().trim()
        val completed = completedCheckbox.isChecked

        if (taskName.isEmpty()) {
            Toast.makeText(requireContext(), "Task name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val taskId = task?.id ?: db.collection("Users").document(userId).collection("Tasks").document().id

        val updatedTask = Task(taskId, taskName, category, deadline, completed)

        val taskRef = db.collection("Users").document(userId).collection("Tasks").document(taskId)
        taskRef.set(updatedTask)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Task saved successfully", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save task", Toast.LENGTH_SHORT).show()
            }
    }
}
