package com.example.plannerwedding

import Task
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class TaskDialog : DialogFragment() {

    private lateinit var taskNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var deadlineInput: EditText
    private lateinit var completedCheckbox: CheckBox
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var database: DatabaseReference
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_task_dialog, container, false)

        // Initialize Views
        taskNameInput = view.findViewById(R.id.taskName)
        categorySpinner = view.findViewById(R.id.taskCategorySpinner)
        deadlineInput = view.findViewById(R.id.taskDeadline)
        completedCheckbox = view.findViewById(R.id.completedCheckbox)
        doneButton = view.findViewById(R.id.doneTaskButton)
        cancelButton = view.findViewById(R.id.cancelTaskButton)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("tasks")

        // Set up category spinner
        val categories = listOf(
            "Priority", "Wedding Venue", "Entertainment", "Food & Beverages", "Ceremony Essentials",
            "Favors and Gifts", "Decorations", "Photography and Videography", "Hair and Makeup",
            "Bride's Outfit", "Groom's Outfit", "Transportation Tasks", "Other Tasks"
        )

        // Use a custom adapter to display categories
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // Adjust dropdown visibility by setting popup background
        categorySpinner.setPopupBackgroundResource(android.R.color.white)  // Optional: Set background color
        categorySpinner.dropDownVerticalOffset = 10 // Controls dropdown offset position

        // Set click listeners
        doneButton.setOnClickListener { saveTask() }
        cancelButton.setOnClickListener { dismiss() }

        // Set up Calendar Picker
        deadlineInput.isFocusable = false  // Prevent keyboard from appearing
        deadlineInput.setOnClickListener { showDatePicker() }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                deadlineInput.setText(dateFormat.format(calendar.time)) // Set selected date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
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

        val taskId = database.child(category).push().key ?: return
        val task = Task(taskId, taskName, category, deadline, completed)

        database.child(category).child(taskId).setValue(task)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Task added successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show()
            }
    }
}