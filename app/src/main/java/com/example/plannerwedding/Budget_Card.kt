package com.example.plannerwedding

import android.app.AlertDialog
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

        // Bind budget item data if available
        budgetItem?.let { bindBudgetItem(view, it) }

        val paidBudgetIcon = view.findViewById<ImageView>(R.id.paidbudget)
        val deleteBudgetIcon = view.findViewById<ImageView>(R.id.deletebudget)

        // Set click listener for toggling paid status
        paidBudgetIcon.setOnClickListener {
            budgetItem?.let { item ->
                showConfirmationDialog(
                    "Update Payment Status",
                    "Are you sure you want to mark this item as ${if (item.paid) "Unpaid" else "Paid"}?",
                    { updateBudgetItemStatus(item) }
                )
            }
        }

        // Set click listener for deleting the item
        deleteBudgetIcon.setOnClickListener {
            budgetItem?.let { item ->
                showConfirmationDialog(
                    "Delete Budget Item",
                    "Are you sure you want to delete this budget item?",
                    { deleteBudgetItem(item, view) }
                )
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
        val userId = auth.currentUser?.uid ?: return
        item.paid = !item.paid

        db.collection("Users").document(userId).collection("Budget")
            .document(item.id ?: return)
            .set(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Budget item updated successfully!", Toast.LENGTH_SHORT).show()
                view?.let { bindBudgetItem(it, item) } // Refresh UI
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update budget item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteBudgetItem(item: BudgetItem, view: View) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).collection("Budget")
            .document(item.id ?: return)
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

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Set the budget item to be displayed in the fragment
    fun setBudgetItem(item: BudgetItem) {
        this.budgetItem = item
        view?.let { bindBudgetItem(it, item) }
    }
}
