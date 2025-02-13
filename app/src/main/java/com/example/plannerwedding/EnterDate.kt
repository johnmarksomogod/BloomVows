package com.example.plannerwedding

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EnterDate : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_date, container, false)

        val dateEditText = view.findViewById<EditText>(R.id.weddingDateInput)
        val nextButton = view.findViewById<Button>(R.id.nextButton)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)  // Reset time to midnight
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        dateEditText.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dateEditText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Prevent the user from selecting past dates
            datePicker.datePicker.minDate = System.currentTimeMillis()

            datePicker.show()
        }

        nextButton.setOnClickListener {
            val selectedDate = dateEditText.text.toString()
            if (selectedDate.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("wedding") // Ensuring correct path

                database.child("weddingDate").setValue(selectedDate)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Date saved: $selectedDate")
                        findNavController().navigate(R.id.action_enterDate_to_enterBudget)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save date", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
