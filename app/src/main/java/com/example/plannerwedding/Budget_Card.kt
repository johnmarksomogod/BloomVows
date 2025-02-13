package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BudgetCardFragment : Fragment() {

    private lateinit var itemTitle: TextView
    private lateinit var itemCost: TextView
    private lateinit var paidStatus: TextView

    private var budget: Budget? = null
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget__card, container, false)

        // Initialize views
        itemTitle = view.findViewById(R.id.itemTitle)
        itemCost = view.findViewById(R.id.itemCost)
        paidStatus = view.findViewById(R.id.paidStatus)

        // Display budget details
        budget?.let { displayBudget(it) }

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().getReference("budgets")

        // Click listener to toggle paid status
        view.findViewById<LinearLayout>(R.id.budgetCardContainer).setOnClickListener {
            budget?.let {
                it.paid = !it.paid // Toggle the paid status
                displayBudget(it) // Update the display
                updateBudgetStatus(it) // Update Firebase
            }
        }

        return view
    }

    private fun displayBudget(budget: Budget) {
        itemTitle.text = budget.name
        itemCost.text = "Cost: ₱${budget.amount}"
        paidStatus.text = if (budget.paid) "Paid" else "Unpaid"
    }

    private fun updateBudgetStatus(budget: Budget) {
        val budgetId = budget.id ?: return
        database.child(budget.category).child(budgetId).setValue(budget)
            .addOnSuccessListener {
                Toast.makeText(context, "Budget updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update budget", Toast.LENGTH_SHORT).show()
            }
    }

    // Set the budget data
    fun setBudget(budget: Budget) {
        this.budget = budget
        // Display the data once set
        displayBudget(budget)
    }
}
