package com.example.plannerwedding

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class EditSectionDialog(
    private val sectionTitle: String,
    private val currentValue: String
) : DialogFragment() {

    private var onSaveListener: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_section_dialog, container, false)

        // Set the title
        val titleTextView = view.findViewById<TextView>(R.id.sectionTitle)
        titleTextView.text = sectionTitle

        // Set the current value in the input field
        val inputEditText = view.findViewById<TextInputEditText>(R.id.sectionInput)
        inputEditText.setText(currentValue)

        // Special handling for date field
        if (sectionTitle == "Wedding Date") {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Try to parse the current date value
            try {
                if (currentValue != "No Date" && currentValue != "No date set") {
                    calendar.time = dateFormat.parse(currentValue) ?: calendar.time
                }
            } catch (e: Exception) {
                // If parsing fails, use current date
            }

            inputEditText.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _, year, month, day ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        inputEditText.setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()
            }
        }

        // Set up cancel button
        val cancelButton = view.findViewById<TextView>(R.id.UserCancelButton)
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Set up save button
        val saveButton = view.findViewById<TextView>(R.id.UserSaveButton)
        saveButton.setOnClickListener {
            val newValue = inputEditText.text.toString().trim()
            if (newValue.isNotEmpty()) {
                onSaveListener?.invoke(newValue)
                dismiss()
            }
        }

        return view
    }

    fun setOnSaveListener(listener: (String) -> Unit) {
        onSaveListener = listener
    }
}