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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EnterBudget : Fragment() {

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_budget, container, false)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance("https://wedding-planner-f8418-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("budget")

        val budgetEditText = view.findViewById<EditText>(R.id.budgetAmountEditText)
        val submitButton = view.findViewById<Button>(R.id.submitButton2)

        submitButton.setOnClickListener {
            val budget = budgetEditText.text.toString().trim()

            if (budget.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your budget", Toast.LENGTH_SHORT).show()
            } else {
                saveBudgetToFirebase(budget)
            }
        }

        return view
    }

    private fun saveBudgetToFirebase(budget: String) {
        // Create a unique ID for each budget entry (total budget in this case)
        val budgetData = TotalBudget(budget.toDouble(), 0.0)  // Saving totalAmount and spentAmount

        // Save the total budget and spent amount under the 'totalBudget' node in Firebase
        database.child("totalBudget").setValue(budgetData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_enterBudget_to_enterVenue) // Navigate to next screen after saving
            } else {
                Toast.makeText(requireContext(), "Failed to save budget", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class TotalBudget(val totalAmount: Double = 0.0, val spentAmount: Double = 0.0)
}
