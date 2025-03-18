package com.example.plannerwedding

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.util.*

class TimelineDialogFragment(private val onEventAdded: (String, String) -> Unit) : DialogFragment() {

    private lateinit var eventTitle: EditText
    private lateinit var eventTime: TextView
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private var lastSelectedTimeInMinutes: Int? = null

    private var currentTitle: String? = null
    private var currentTime: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timeline_dialog, container, false)

        eventTitle = view.findViewById(R.id.eventTitle)
        eventTime = view.findViewById(R.id.eventTime)
        doneButton = view.findViewById(R.id.timelineDoneButton)
        cancelButton = view.findViewById(R.id.timelineCancelButton)

        // Populate the fields if we are editing an event
        if (currentTitle != null && currentTime != null) {
            eventTitle.setText(currentTitle)
            eventTime.text = currentTime
        }

        // Time Picker functionality for AM/PM selection
        eventTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    // Convert to 12-hour AM/PM format
                    val isPM = selectedHour >= 12
                    val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                    val formattedTime = String.format(
                        "%02d:%02d %s",
                        formattedHour, selectedMinute, if (isPM) "PM" else "AM"
                    )

                    val selectedTimeInMinutes = (selectedHour * 60) + selectedMinute
                    eventTime.text = formattedTime
                    lastSelectedTimeInMinutes = selectedTimeInMinutes
                },
                hour, minute, false // false for 12-hour format
            )
            timePickerDialog.show()
        }

        doneButton.setOnClickListener {
            val title = eventTitle.text.toString().trim()
            val time = eventTime.text.toString()

            if (title.isNotEmpty() && time.isNotEmpty() && time != "Select Time") {
                onEventAdded(title, time)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a title and select a time.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dismiss() // Simply dismiss the dialog without making any changes
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // Set the event data when editing
    fun setEventData(title: String, time: String) {
        currentTitle = title
        currentTime = time
    }
}