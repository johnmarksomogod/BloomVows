package com.example.plannerwedding

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BudgetCard : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var budgetItem: BudgetItem? = null
    private var updateProgressCallback: (() -> Unit)? = null // Define a callback for updateProgress()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget__card, container, false)

        db = FirebaseFirestore.getInstance()

        budgetItem?.let { bindBudget(view, it) }

        val paidIcon = view.findViewById<ImageView>(R.id.paidbudget)
        val deleteIcon = view.findViewById<ImageView>(R.id.deletebudget)

        paidIcon.setOnClickListener {
            showConfirmationDialog("Are you sure you want to mark this item as paid?", "Yes") {
                budgetItem?.let {
                    it.paid = !it.paid
                    updateBudgetInFirestore(it)
                    bindBudget(view, it)
                    updateProgressCallback?.invoke()
                }
            }
        }

        deleteIcon.setOnClickListener {
            showConfirmationDialog("Are you sure you want to delete this budget item?", "Yes") {
                budgetItem?.let {
                    deleteBudget(it, view)
                }
            }
        }

        return view
    }

    private fun bindBudget(view: View, item: BudgetItem) {
        val title = view.findViewById<TextView>(R.id.itemTitle)
        val cost = view.findViewById<TextView>(R.id.itemCost)
        val status = view.findViewById<TextView>(R.id.paidStatus)
        val paidIcon = view.findViewById<ImageView>(R.id.paidbudget)

        title.text = item.name
        cost.text = "Cost: ₱${item.amount.toInt()}"
        status.text = if (item.paid) {
            paidIcon.setImageResource(R.drawable.heart_filled)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            "Paid"
        } else {
            paidIcon.setImageResource(R.drawable.heart1)
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Unpaid"
        }
    }

    private fun updateBudgetInFirestore(budgetItem: BudgetItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("Users").document(userId).collection("Budget")
            .document(budgetItem.id ?: return)
            .set(budgetItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget item updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update budget item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteBudget(budgetItem: BudgetItem, view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("Users").document(userId).collection("Budget")
            .document(budgetItem.id ?: return)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget item deleted", Toast.LENGTH_SHORT).show()
                val parent = view.parent as? ViewGroup
                parent?.removeView(view)

                // Call the updateProgress callback after deletion
                updateProgressCallback?.invoke()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete budget item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmationDialog(message: String, positiveButton: String, action: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, _ ->
                action()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun setUpdateProgressCallback(callback: () -> Unit) {
        this.updateProgressCallback = callback
    }

    fun setBudgetItem(budgetItem: BudgetItem) {
        this.budgetItem = budgetItem
        view?.let { bindBudget(it, budgetItem) }
    }
}