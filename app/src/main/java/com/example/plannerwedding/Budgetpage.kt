package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class BudgetPageFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var totalBudgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var remainingText: TextView
    private lateinit var addBudgetButton: LinearLayout
    private lateinit var database: DatabaseReference

    private var totalAmount = 0.0
    private var spentAmount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budgetpage, container, false)

        progressBar = view.findViewById(R.id.budgetProgress)
        totalBudgetText = view.findViewById(R.id.totalBudgetText)
        spentText = view.findViewById(R.id.spentText)
        remainingText = view.findViewById(R.id.remainingText)
        addBudgetButton = view.findViewById(R.id.addBudgetButton)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("budgets")

        // Load budget data
        loadBudgetData()

        // Add budget button listener
        addBudgetButton.setOnClickListener {
            val dialog = BudgetDialogFragment()
            dialog.show(parentFragmentManager, "BudgetDialog")
        }

        return view
    }

    private fun loadBudgetData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalAmount = snapshot.child("totalAmount").getValue(Double::class.java) ?: 0.0
                spentAmount = snapshot.child("spentAmount").getValue(Double::class.java) ?: 0.0
                updateBudgetUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load budget", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateBudgetUI() {
        val remainingAmount = totalAmount - spentAmount
        val progress = if (totalAmount > 0) (spentAmount * 100) / totalAmount else 0

        progressBar.progress = progress.toInt()
        totalBudgetText.text = "Total Budget: ₱$totalAmount"
        spentText.text = "Spent: ₱$spentAmount"
        remainingText.text = "Remaining: ₱$remainingAmount"
    }
}
