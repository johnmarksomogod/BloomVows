package com.example.plannerwedding

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

class EnterBudget : Fragment() {
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
                    if (document.exists() && document.contains("budget")) {
                        // If budget already exists, navigate to the next page
                        findNavController().navigate(R.id.action_enterBudget_to_enterVenue)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error checking budget", Toast.LENGTH_SHORT).show()
                }
        }

        val view = inflater.inflate(R.layout.fragment_enter_budget, container, false)

        val budgetEditText = view.findViewById<EditText>(R.id.budgetAmountEditText)
        val submitButton = view.findViewById<Button>(R.id.submitButton2)

        submitButton.setOnClickListener {
            val budgetString = budgetEditText.text.toString().trim()
            if (budgetString.isEmpty() && user != null) {
                Toast.makeText(requireContext(), "Please enter your budget", Toast.LENGTH_SHORT).show()
            } else if (user != null) {
                val budgetDouble = budgetString.toDoubleOrNull()
                if (budgetDouble == null) {
                    // If the entered value is not a valid double, show an error message
                    Toast.makeText(requireContext(), "Please enter a valid numeric budget", Toast.LENGTH_SHORT).show()
                } else {
                    val userId = user.uid
                    db.collection("Users").document(userId)
                        .update("budget", budgetDouble)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Budget saved successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_enterBudget_to_enterVenue)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to save budget", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        return view
    }
}
