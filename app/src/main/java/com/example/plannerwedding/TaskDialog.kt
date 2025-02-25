package com.example.plannerwedding

<<<<<<< HEAD
=======
import Task
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
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

<<<<<<< HEAD
    // Task object that will be passed from the calling fragment
    private var task: Task? = null

=======
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
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

<<<<<<< HEAD
        // Initialize Firebase with the custom URL
        database = FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("tasks")

        // Retrieve the task data passed from the calling fragment
        task = arguments?.getSerializable("task") as Task?

        // Populate the views with the task data if it's not null
        task?.let { populateTaskDetails(it) }
=======
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("tasks")
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00

        // Set up category spinner
        val categories = listOf(
            "Priority", "Wedding Venue", "Entertainment", "Food & Beverages", "Ceremony Essentials",
            "Favors and Gifts", "Decorations", "Photography and Videography", "Hair and Makeup",
            "Bride's Outfit", "Groom's Outfit", "Transportation Tasks", "Other Tasks"
        )
<<<<<<< HEAD
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // Set up Calendar Picker for deadline
        deadlineInput.isFocusable = false
        deadlineInput.setOnClickListener { showDatePicker() }

        // Set up click listeners for done and cancel buttons
        doneButton.setOnClickListener { saveTask() }
        cancelButton.setOnClickListener { dismiss() }

        return view
    }

    // Override onStart() to adjust the dialog size
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set width to match parent (full width)
            ViewGroup.LayoutParams.WRAP_CONTENT    // Set height to wrap content (adjustable)
        )
=======

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
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
<<<<<<< HEAD
                deadlineInput.setText(dateFormat.format(calendar.time))
=======
                deadlineInput.setText(dateFormat.format(calendar.time)) // Set selected date
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

<<<<<<< HEAD
        // Set min date to today, preventing dates in the past
        datePicker.datePicker.minDate = System.currentTimeMillis() // Disable past dates
        datePicker.show()
    }

    private fun populateTaskDetails(task: Task) {
        // Set the task's existing values
        taskNameInput.setText(task.name)
        categorySpinner.setSelection((categorySpinner.adapter as ArrayAdapter<String>).getPosition(task.category))
        deadlineInput.setText(task.deadline)
        completedCheckbox.isChecked = task.completed
    }

=======
        datePicker.show()
    }

>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
    private fun saveTask() {
        val taskName = taskNameInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString() ?: ""
        val deadline = deadlineInput.text.toString().trim()
        val completed = completedCheckbox.isChecked

        if (taskName.isEmpty()) {
            Toast.makeText(requireContext(), "Task name is required", Toast.LENGTH_SHORT).show()
            return
        }

<<<<<<< HEAD
        // If this is a new task, generate a task ID
        val taskId = task?.id ?: database.child(category).push().key ?: return

        val updatedTask = Task(taskId, taskName, category, deadline, completed)

        // Check if it's an existing task or a new task
        if (task != null) {
            // Update existing task in Firebase
            updateTask(updatedTask)
        } else {
            // Add new task to Firebase
            addNewTask(updatedTask)
        }
    }

    private fun updateTask(task: Task) {
        task.id?.let {
            database.child(task.category).child(it).setValue(task)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addNewTask(task: Task) {
        task.id?.let {
            database.child(task.category).child(it).setValue(task)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Task added successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
=======
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
>>>>>>> d47a662892ae575003ab89ada2af2446e000ae00
