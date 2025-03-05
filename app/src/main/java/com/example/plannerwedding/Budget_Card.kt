package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class BudgetCard : Fragment() {

    private var budgetItem: BudgetItem? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget__card, container, false)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Bind the budget item data if available
        budgetItem?.let { bindBudgetItem(view, it) }

        val paidBudgetIcon = view.findViewById<ImageView>(R.id.paidbudget)
        val deleteBudgetIcon = view.findViewById<ImageView>(R.id.deletebudget)
        val budgetCardContainer = view.findViewById<LinearLayout>(R.id.budgetCardContainer)

        // Set click listeners for the icons
        paidBudgetIcon.setOnClickListener {
            budgetItem?.let {
                it.paid = !it.paid // Toggle the payment status
                bindBudgetItem(view, it) // Rebind the updated item
                updateBudgetItemStatus(it) // Update the Firebase database
            }
        }

        deleteBudgetIcon.setOnClickListener {
            budgetItem?.let {
                deleteBudgetItem(it, view) // Delete the item from Firebase and the UI
            }
        }

        return view
    }

    private fun bindBudgetItem(view: View, item: BudgetItem) {
        val title = view.findViewById<TextView>(R.id.itemTitle)
        val cost = view.findViewById<TextView>(R.id.itemCost)
        val paidStatus = view.findViewById<TextView>(R.id.paidStatus)
        val paidIcon = view.findViewById<ImageView>(R.id.paidbudget)

        title.text = item.name
        cost.text = "Cost: ₱${item.amount}"
        paidStatus.text = if (item.paid) {
            paidIcon.setImageResource(R.drawable.heart_filled)
            paidStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            "Paid"
        } else {
            paidIcon.setImageResource(R.drawable.heart1)
            paidStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Unpaid"
        }
    }

    private fun updateBudgetItemStatus(item: BudgetItem) {
        val itemId = item.id ?: return
        db.collection("Users").document(auth.currentUser?.uid ?: "")
            .collection("Budget").document(item.category)
            .collection("Items").document(itemId)
            .set(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Budget item updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update budget item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteBudgetItem(item: BudgetItem, view: View) {
        val itemId = item.id ?: return
        db.collection("Users").document(auth.currentUser?.uid ?: "")
            .collection("Budget").document(item.category)
            .collection("Items").document(itemId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Budget item deleted successfully!", Toast.LENGTH_SHORT).show()

                // Remove the budget item from the UI as well
                val budgetCardContainer = view.findViewById<LinearLayout>(R.id.budgetCardContainer)
                budgetCardContainer?.let {
                    val parent = it.parent as? ViewGroup
                    parent?.removeView(it)  // Remove item view from parent container
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete budget item", Toast.LENGTH_SHORT).show()
            }
    }

    // Set the budget item to be displayed in the fragment
    fun setBudgetItem(item: BudgetItem) {
        this.budgetItem = item
        view?.let { bindBudgetItem(it, item) }
    }
}
