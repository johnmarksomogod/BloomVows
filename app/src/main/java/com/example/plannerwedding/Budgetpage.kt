package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BudgetPageFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var totalBudgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var remainingText: TextView
    private lateinit var addBudgetButton: LinearLayout
    private lateinit var db: FirebaseFirestore

    private var totalBudget = 0.0
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

        db = FirebaseFirestore.getInstance()

        loadBudgetData()

        addBudgetButton.setOnClickListener {
            val dialog = BudgetDialogFragment()
            dialog.show(parentFragmentManager, "BudgetDialog")
        }

        return view
    }

    private fun loadBudgetData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val budgetField = document.get("budget")

                    totalBudget = when (budgetField) {
                        is Number -> budgetField.toDouble()  // Works for Double, Long, Int
                        is String -> budgetField.toDoubleOrNull() ?: 0.0 // Convert string to double safely
                        else -> 0.0 // Default if budget is not set
                    }

                    loadBudgetItems() // Load budget items after fetching budget
                } else {
                    totalBudget = 0.0
                    updateProgress()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load budget", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadBudgetItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Users").document(userId).collection("Budget")
            .get()
            .addOnSuccessListener { snapshot ->
                spentAmount = 0.0

                for (document in snapshot.documents) {
                    val budgetItem = document.toObject(BudgetItem::class.java)
                    if (budgetItem?.paid == true) {
                        spentAmount += budgetItem.amount
                    }
                }
                updateProgress()
            }
    }

    private fun updateProgress() {
        val remainingBudget = totalBudget - spentAmount
        progressBar.progress = if (totalBudget > 0) (spentAmount * 100 / totalBudget).toInt() else 0
        spentText.text = "Spent: ₱$spentAmount"
        remainingText.text = "Remaining: ₱$remainingBudget"
        totalBudgetText.text = "Total Budget: ₱$totalBudget"
    }
}
