package com.example.plannerwedding

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EnterDate : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.contains("weddingDate")) {
                        // Wedding date already exists, navigate to EnterBudget
                        findNavController().navigate(R.id.action_enterDate_to_enterBudget)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error checking wedding date", Toast.LENGTH_SHORT).show()
                }
        }

        val view = inflater.inflate(R.layout.fragment_enter_date, container, false)

        val dateEditText = view.findViewById<EditText>(R.id.weddingDateInput)
        val nextButton = view.findViewById<Button>(R.id.nextButton)

        val calendar = Calendar.getInstance()
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
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        nextButton.setOnClickListener {
            val selectedDate = dateEditText.text.toString()
            if (selectedDate.isNotEmpty() && user != null) {
                val userId = user.uid
                db.collection("Users").document(userId)
                    .update("weddingDate", selectedDate)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Wedding date saved successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_enterDate_to_enterBudget)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save wedding date", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}