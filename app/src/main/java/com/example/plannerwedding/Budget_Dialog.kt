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
    private var currentSpent: Double = 0.0

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

        // Show loader during data fetch
        (activity as? MainActivity)?.showLoader()

        // Load total budget & spent amount
        loadTotalBudget()

        // Check if editing an existing budget item
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

        // Set up category spinner
        val categories = listOf(
            "Priority", "Wedding Venue", "Entertainment", "Food & Beverages", "Ceremony Essentials",
            "Favors and Gifts", "Decorations", "Photography and Videography", "Hair and Makeup",
            "Bride's Outfit", "Groom's Outfit", "Transportation", "Other Expenses"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // Set the selected category if editing
        existingBudgetItem?.let { item ->
            val position = categories.indexOf(item.category)
            if (position >= 0) {
                categorySpinner.setSelection(position)
            }
        }

        doneButton.setOnClickListener {
            // Show loader when saving
            (activity as? MainActivity)?.showLoader()
            saveBudgetItem()
        }

        cancelButton.setOnClickListener { dismiss() }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun loadTotalBudget() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    totalBudget = when (val budgetField = document.get("budget")) {
                        is Number -> budgetField.toDouble()
                        is String -> budgetField.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    // Load the currently spent amount
                    calculateCurrentSpentAmount { spentAmount ->
                        currentSpent = spentAmount
                        // Hide loader now that data is loaded
                        (activity as? MainActivity)?.hideLoader()
                    }
                } else {
                    // Hide loader if no document exists
                    (activity as? MainActivity)?.hideLoader()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load total budget", Toast.LENGTH_SHORT).show()
                // Hide loader in case of failure
                (activity as? MainActivity)?.hideLoader()
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
                        if (existingBudgetItem == null || item.id != existingBudgetItem?.id) {
                            spentAmount += item.amount
                        }
                    }
                }

                callback(spentAmount)
            }
            .addOnFailureListener {
                callback(0.0)
            }
    }

    private fun saveBudgetItem() {
        val expenseName = expenseNameInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString() ?: ""
        val amount = expenseAmountInput.text.toString().trim()
        val paid = paidCheckbox.isChecked

        if (expenseName.isEmpty()) {
            Toast.makeText(requireContext(), "Expense name is required", Toast.LENGTH_SHORT).show()
            // Hide loader if validation fails
            (activity as? MainActivity)?.hideLoader()
            return
        }

        val expenseAmount = amount.toDoubleOrNull() ?: run {
            Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
            // Hide loader if validation fails
            (activity as? MainActivity)?.hideLoader()
            return
        }

        val remainingBudget = totalBudget - currentSpent

        // Prevent adding an expense that exceeds the remaining budget
        if (expenseAmount > remainingBudget) {
            Toast.makeText(
                requireContext(),
                "Expense amount (₱$expenseAmount) exceeds your remaining budget (₱$remainingBudget)!",
                Toast.LENGTH_LONG
            ).show()
            // Hide loader if validation fails
            (activity as? MainActivity)?.hideLoader()
            return
        }

        // If marking as paid, ensure it doesn't exceed the total budget
        if (paid && (currentSpent + expenseAmount) > totalBudget) {
            Toast.makeText(
                requireContext(),
                "Cannot mark as Paid! Insufficient remaining budget.",
                Toast.LENGTH_LONG
            ).show()
            // Hide loader if validation fails
            (activity as? MainActivity)?.hideLoader()
            return
        }

        saveItemToFirestore(expenseName, category, expenseAmount, paid)
    }

    private fun saveItemToFirestore(name: String, category: String, amount: Double, paid: Boolean) {
        val userId = auth.currentUser?.uid ?: return

        if (existingBudgetItem != null) {
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
                    // Hide loader in case of failure
                    (activity as? MainActivity)?.hideLoader()
                }
        } else {
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
                    // Hide loader in case of failure
                    (activity as? MainActivity)?.hideLoader()
                }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogDismissListener?.onDialogDismiss()
    }
}