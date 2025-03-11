package com.example.plannerwedding

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

class BudgetDialogFragment : DialogFragment() {

    private lateinit var expenseNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var expenseAmountInput: EditText
    private lateinit var paidCheckbox: CheckBox
    private lateinit var doneButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var dialogDismissListener: DialogDismissListener? = null
    private var existingBudgetItem: BudgetItem? = null
    private var totalBudget: Double = 0.0

    interface DialogDismissListener {
        fun onDialogDismiss()
    }

    fun setDialogDismissListener(listener: DialogDismissListener) {
        this.dialogDismissListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget__dialog, container, false)

        expenseNameInput = view.findViewById(R.id.expenseName)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        expenseAmountInput = view.findViewById(R.id.expenseAmount)
        paidCheckbox = view.findViewById(R.id.paidCheckbox)
        doneButton = view.findViewById(R.id.budgetdoneButton)
        cancelButton = view.findViewById(R.id.budgetcancelButton)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get the total budget from Firestore
        loadTotalBudget()

        // Check if we have an existing budget item to edit
        arguments?.let {
            if (it.containsKey("budgetItem")) {
                existingBudgetItem = it.getSerializable("budgetItem") as BudgetItem
                existingBudgetItem?.let { item ->
                    expenseNameInput.setText(item.name)
                    expenseAmountInput.setText(item.amount.toString())
                    paidCheckbox.isChecked = item.paid
                }
            }
        }

        // Set up the spinner with categories
        val categories = listOf(
            "Priority", "Wedding Venue", "Entertainment", "Food & Beverages", "Ceremony Essentials",
            "Favors and Gifts", "Decorations", "Photography and Videography", "Hair and Makeup",
            "Bride's Outfit", "Groom's Outfit", "Transportation", "Other Expenses"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // Set the selected category if we have an existing item
        existingBudgetItem?.let { item ->
            val position = categories.indexOf(item.category)
            if (position >= 0) {
                categorySpinner.setSelection(position)
            }
        }

        doneButton.setOnClickListener { saveBudgetItem() }
        cancelButton.setOnClickListener { dismiss() }

        return view
    }

    private fun loadTotalBudget() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val budgetField = document.get("budget")
                    totalBudget = when (budgetField) {
                        is Number -> budgetField.toDouble()  // Safely get the field as a Number
                        is String -> budgetField.toDoubleOrNull() ?: 0.0  // If it's a string, try to convert it
                        else -> 0.0  // Default to 0.0 if it's not a valid Number or String
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load total budget", Toast.LENGTH_SHORT).show()
            }
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

        val expenseAmount = amount.toDoubleOrNull() ?: run {
            Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
            return
        }

        if (paid && existingBudgetItem?.paid != true) {
            calculateCurrentSpentAmount { currentSpent ->
                val remainingBudget = totalBudget - currentSpent

                if (expenseAmount > remainingBudget) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Budget Warning")
                        .setMessage("This expense (₱$expenseAmount) exceeds your remaining budget (₱$remainingBudget). Do you want to continue?")
                        .setPositiveButton("Yes") { _, _ ->
                            saveItemToFirestore(expenseName, category, expenseAmount, paid)
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    saveItemToFirestore(expenseName, category, expenseAmount, paid)
                }
            }
        } else {
            saveItemToFirestore(expenseName, category, expenseAmount, paid)
        }
    }

    private fun calculateCurrentSpentAmount(callback: (Double) -> Unit) {
        val userId = auth.currentUser?.uid ?: return callback(0.0)

        db.collection("Users").document(userId).collection("Budget")
            .get()
            .addOnSuccessListener { snapshot ->
                var spentAmount = 0.0

                for (document in snapshot.documents) {
                    val item = document.toObject(BudgetItem::class.java)
                    if (item?.paid == true) {
                        // Don't count the existing item if we're editing
                        if (existingBudgetItem == null || item.id != existingBudgetItem?.id) {
                            spentAmount += item.amount
                        }
                    }
                }

                callback(spentAmount)
            }
            .addOnFailureListener {
                callback(0.0) // In case of failure, assume 0 spent
            }
    }

    private fun saveItemToFirestore(name: String, category: String, amount: Double, paid: Boolean) {
        val userId = auth.currentUser?.uid ?: return

        if (existingBudgetItem != null) {
            // Update existing item
            val updatedItem = BudgetItem(existingBudgetItem?.id, name, category, amount, paid)

            db.collection("Users").document(userId).collection("Budget")
                .document(existingBudgetItem?.id ?: return)
                .set(updatedItem)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Budget item updated successfully!", Toast.LENGTH_SHORT).show()
                    dialogDismissListener?.onDialogDismiss()
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update budget item", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add new item
            val budgetItem = BudgetItem(null, name, category, amount, paid)

            db.collection("Users").document(userId).collection("Budget")
                .add(budgetItem)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Budget item added successfully!", Toast.LENGTH_SHORT).show()
                    dialogDismissListener?.onDialogDismiss()
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add budget item", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogDismissListener?.onDialogDismiss()
    }
}
