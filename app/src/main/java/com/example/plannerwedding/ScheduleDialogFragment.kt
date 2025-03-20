package com.example.plannerwedding

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ScheduleDialogFragment : DialogFragment() {

    private lateinit var scheduleNameEditText: EditText
    private lateinit var scheduleDateTextView: EditText
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private val calendar = Calendar.getInstance()

    var onScheduleAddedListener: OnScheduleAddedListener? = null

    interface OnScheduleAddedListener {
        fun onScheduleAdded(scheduleItem: ScheduleItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_dialog, container, false)

        scheduleNameEditText = view.findViewById(R.id.scheduleName)
        scheduleDateTextView = view.findViewById(R.id.scheduleDate)
        doneButton = view.findViewById(R.id.doneScheduleButton)
        cancelButton = view.findViewById(R.id.cancelScheduleButton)

        scheduleDateTextView.isFocusable = false
        scheduleDateTextView.setOnClickListener { showDatePickerDialog() }
        scheduleDateTextView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (scheduleDateTextView.right - scheduleDateTextView.compoundPaddingEnd)) {
                    showDatePickerDialog()
                    return@setOnTouchListener true
                }
            }
            false
        }

        doneButton.setOnClickListener {
            val scheduleName = scheduleNameEditText.text.toString().trim()
            val scheduleDate = scheduleDateTextView.text.toString().trim()

            if (scheduleName.isNotEmpty() && scheduleDate.isNotEmpty()) {
                (activity as? MainActivity)?.showLoader()
                saveScheduleToFirestore(scheduleName, scheduleDate)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a schedule name and date.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        cancelButton.setOnClickListener { dismiss() }

        return view
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = "${month + 1}/$dayOfMonth/$year"
                scheduleDateTextView.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun saveScheduleToFirestore(scheduleName: String, scheduleDate: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            val scheduleRef = db.collection("Users").document(userId).collection("Schedules")

            val scheduleId = scheduleRef.document().id
            val scheduleData = ScheduleItem(scheduleId, scheduleName, scheduleDate, "Pending")

            scheduleRef.document(scheduleId)
                .set(scheduleData)
                .addOnSuccessListener {
                    (activity as? MainActivity)?.hideLoader()
                    showSuccessDialog(scheduleData)
                }
                .addOnFailureListener {
                    (activity as? MainActivity)?.hideLoader()
                    Toast.makeText(requireContext(), "Failed to add schedule.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showSuccessDialog(scheduleItem: ScheduleItem) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Schedule Added")
            .setMessage("Your schedule \"${scheduleItem.scheduleName}\" for ${scheduleItem.scheduleDate} has been added successfully.")
            .setPositiveButton("OK") { _, _ ->
                onScheduleAddedListener?.onScheduleAdded(scheduleItem)
                dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setTextColor(Color.parseColor("#EDABAD"))
        }

        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.hideLoader()
    }
}
