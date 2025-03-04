package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BudgetDialogFragment : DialogFragment() {

    private lateinit var expenseNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var expenseAmountInput: EditText
    private lateinit var paidCheckbox: CheckBox
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var db: FirebaseFirestore
    private var budgetItem: BudgetItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget__dialog, container, false)

        // Initialize Views
        expenseNameInput = view.findViewById(R.id.expenseName)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        expenseAmountInput = view.findViewById(R.id.expenseAmount)
        paidCheckbox = view.findViewById(R.id.paidCheckbox)
        doneButton = view.findViewById(R.id.budgetdoneButton)
        cancelButton = view.findViewById(R.id.budgetcancelButton)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Retrieve the budget item passed from the calling fragment (if editing)
        budgetItem = arguments?.getSerializable("budgetItem") as BudgetItem?

        // Populate the views with existing budget item details (if editing)
        budgetItem?.let { populateBudgetItemDetails(it) }

        // Set up category spinner
        val categories = listOf(
            "Priority", "Wedding Venue", "Entertainment", "Food & Beverages", "Ceremony Essentials",
            "Favors and Gifts", "Decorations", "Photography and Videography", "Hair and Makeup",
            "Bride's Outfit", "Groom's Outfit", "Transportation", "Other Expenses"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        doneButton.setOnClickListener { saveBudgetItem() }
        cancelButton.setOnClickListener { dismiss() }

        return view
    }

    private fun populateBudgetItemDetails(budgetItem: BudgetItem) {
        expenseNameInput.setText(budgetItem.name)
        categorySpinner.setSelection((categorySpinner.adapter as ArrayAdapter<String>).getPosition(budgetItem.category))
        expenseAmountInput.setText(budgetItem.amount.toString())
        paidCheckbox.isChecked = budgetItem.paid
    }

    private fun saveBudgetItem() {
        val expenseName = expenseNameInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString() ?: ""
        val amount = expenseAmountInput.text.toString().trim()
        val paid = paidCheckbox.isChecked

        if (expenseName.isEmpty()) {
            Toast.makeText(requireContext(), "Expense name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount.isEmpty()) {
            Toast.makeText(requireContext(), "Amount is required", Toast.LENGTH_SHORT).show()
            return
        }

        val expenseAmount = amount.toDoubleOrNull() ?: run {
            Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetItemId = budgetItem?.id ?: db.collection("Users").document(FirebaseAuth.getInstance().currentUser?.uid ?: "").collection("Budget").document(category).collection("Items").document().id

        val updatedBudgetItem = BudgetItem(budgetItemId, expenseName, category, expenseAmount, paid)

        if (budgetItem != null) {
            updateBudgetItem(updatedBudgetItem)
        } else {
            addNewBudgetItem(updatedBudgetItem)
        }
    }

    private fun updateBudgetItem(updatedBudgetItem: BudgetItem) {
        db.collection("Users").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .collection("Budget").document(updatedBudgetItem.category)
            .collection("Items").document(updatedBudgetItem.id ?: "")
            .set(updatedBudgetItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget item updated successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update budget item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNewBudgetItem(updatedBudgetItem: BudgetItem) {
        db.collection("Users").document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .collection("Budget").document(updatedBudgetItem.category)
            .collection("Items").document(updatedBudgetItem.id ?: "")
            .set(updatedBudgetItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Budget item added successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add budget item", Toast.LENGTH_SHORT).show()
            }
    }
}
