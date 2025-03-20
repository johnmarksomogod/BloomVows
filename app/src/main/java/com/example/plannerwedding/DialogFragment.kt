package com.example.plannerwedding

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.*

class EditSectionDialogFragment(
    private val sectionTitle: String,
    private val sectionValue: String
) : DialogFragment() {

    private lateinit var sectionInputEditText: TextInputEditText
    private lateinit var sectionTitleTextView: TextView
    private var onSaveListener: ((String) -> Unit)? = null
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)

        sectionInputEditText = view.findViewById(R.id.sectionInput)
        sectionTitleTextView = view.findViewById(R.id.sectionTitle)

        // Set the section title and input field value
        sectionTitleTextView.text = sectionTitle
        sectionInputEditText.setText(sectionValue)

        // If the section title is "Wedding Date", show the date picker when the user clicks the field
        if (sectionTitle == "Wedding Date") {
            sectionInputEditText.isFocusable = false
            sectionInputEditText.setOnClickListener {
                showDatePicker()
            }
        }

        // Cancel button functionality
        val cancelButton = view.findViewById<TextView>(R.id.UserCancelButton)
        cancelButton.setOnClickListener { dismiss() }

        // Save button functionality
        val saveButton = view.findViewById<TextView>(R.id.UserSaveButton)
        saveButton.setOnClickListener { saveData() }

        return view
    }

    // Allow setting the save listener from the parent fragment
    fun setOnSaveListener(listener: (String) -> Unit) {
        this.onSaveListener = listener
    }

    private fun saveData() {
        val newValue = sectionInputEditText.text.toString().trim()
        // Notify the parent fragment about the change
        onSaveListener?.invoke(newValue)
        dismiss() // Dismiss the dialog after saving
    }

    // Show DatePicker dialog when "Wedding Date" field is clicked
    private fun showDatePicker() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                sectionInputEditText.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the minimum date to today's date to prevent selecting past dates
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    // Override onStart() to adjust the dialog size
    override fun onStart() {
        super.onStart()

        // Adjust the width to match parent (full screen width)
        // Adjust the height to wrap content
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set width to match parent (full width)
            ViewGroup.LayoutParams.WRAP_CONTENT    // Set height to wrap content (adjustable)
        )
    }
}