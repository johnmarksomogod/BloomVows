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

class EnterBudget : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_budget, container, false)

        val budgetEditText = view.findViewById<EditText>(R.id.budgetAmountEditText)
        val submitButton = view.findViewById<Button>(R.id.submitButton2)

        submitButton.setOnClickListener {
            val budget = budgetEditText.text.toString().trim()

            if (budget.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your budget", Toast.LENGTH_SHORT).show()
            } else {
                // Navigate to the EnterVenue fragment
                findNavController().navigate(R.id.action_enterBudget_to_enterVenue)
            }
        }

        return view
    }
}
